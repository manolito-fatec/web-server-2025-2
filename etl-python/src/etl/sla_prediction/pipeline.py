"""
Created on 10/23/2025 00:11

@author: AndreWakugawa 
"""

import datetime
import logging

from config import settings
from etl.connectors import MongoConnector
from etl.logger import SlaPredictionLogger
from etl.sla_prediction.extractor import SlaPredictionExtractor
from etl.sla_prediction.loader import SlaPredictionLoader

log = logging.getLogger(__name__)


class SlaPredictionPipeline:
    """Orchestrates the SLA prediction extraction and loading pipeline."""

    def __init__(self, model_config):
        self.start_time = datetime.datetime.now(datetime.timezone.utc)
        self.status = "SUCCESS"
        self.error_info = None
        self.predictions_count = 0
        self.extractor = SlaPredictionExtractor(model_config) 

    def run(self):
        """Main entry point to run the SLA prediction pipeline."""
        log.info("SLA Prediction pipeline started.")
        prediction_results = []

        try:
            log.info("[Stage 1/2] Starting data extraction from Postgres and prediction using ONNX model.")
            prediction_results = self.extractor.execute() 
            
            if self.extractor.json_list is None:
                 raise Exception("SLA prediction extraction/prediction phase failed critically.")
            
            log.info(f"[Stage 1/2] Extraction and prediction completed. Found {len(prediction_results)} predictions.")

            if not prediction_results:
                log.warning("No SLA predictions were generated. Skipping load stage.")
            else:
                log.info("[Stage 2/2] Starting data load to MongoDB.")
                with MongoConnector() as client:
                    predictions_collection = client[settings.MONGO_INSIGHTS_DB_NAME][settings.MONGO_SLA_PREDICTIONS_COLLECTION]
                    loader = SlaPredictionLoader(mongo_collection=predictions_collection)
                    self.predictions_count = loader.execute(prediction_results)
                log.info(f"[Stage 2/2] Data load completed. Documents inserted: {self.predictions_count}.")

        except Exception as e:
            self.status = "FAILURE"
            self.error_info = str(e)
            log.error(f"Critical pipeline failure during SLA prediction: {self.error_info}", exc_info=True)

        finally:
            self._log_final_summary()
            log.info(f"SLA Prediction pipeline finished with status: {self.status}.")

    def _log_final_summary(self):
        """Connects to MongoDB to record the execution summary."""
        log.info("Recording final execution summary for SLA Prediction pipeline.")
        try:
            with MongoConnector() as client:
                log_collection = client[settings.MONGO_LOG_DB_NAME][settings.MONGO_LOG_COLLECTION]
                logger = SlaPredictionLogger(mongo_collection=log_collection)
                logger.log_summary_predictions( 
                    start_time=self.start_time,
                    status=self.status,
                    predictions_generated_count=self.predictions_count, 
                    error_message=self.error_info
                )

                log.info("Execution summary successfully recorded to MongoDB.")
        except Exception as e:
            log.critical(f"Failed to record final execution summary for SLA Prediction pipeline: {e}", exc_info=True)
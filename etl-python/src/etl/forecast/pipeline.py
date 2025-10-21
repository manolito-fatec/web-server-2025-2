"""
Created on 10/17/2025 21:08

@author: paulo-arantes
"""

import datetime
import logging

from config import settings
from etl.connectors import MongoConnector
from etl.logger import ForecasterLogger
from etl.forecast.extractor import TicketForecasterExtractor
from etl.forecast.loader import TicketForecasterLoader
log = logging.getLogger(__name__)


class TicketForecasterPipeline:
    """Orchestrates the entire insight generation pipeline."""

    def __init__(self, model_config):
        self.start_time = datetime.datetime.now(datetime.timezone.utc)
        self.status = "SUCCESS"
        self.error_info = None
        self.forecaster_count = 0
        self.files_count = 0
        self.extractor = TicketForecasterExtractor(model_config)

    def run(self):
        """Main entry point to run the Forecaster pipeline."""
        log.info("Insights Tickets Forecaster pipeline started.")
        try:
            log.info("[Stage 1/3] Starting data extraction from Postgres.")
            json = self.extractor.execute()
            if not json:
                raise Exception("Data extraction from Postgres failed.")
            log.info("[Stage 1/3] Data extraction completed successfully.")

            if not json:
                log.warning("No forecaster were generated. Skipping load stage.")
            else:
                log.info("[Stage 3/3] Starting data load to MongoDB.")
                with MongoConnector() as client:
                    ticket_forecaster_collection = client[settings.MONGO_INSIGHTS_DB_NAME][settings.MONGO_FORECASTER_COLLECTION]
                    loader = TicketForecasterLoader(mongo_collection=ticket_forecaster_collection)
                    self.forecaster_count = loader.execute(json)
                log.info(f"[Stage 3/3] Data load completed. Documents inserted: {self.forecaster_count}.")

        except Exception as e:
            self.status = "FAILURE"
            self.error_info = str(e)
            log.error(f"Critical pipeline failure: {self.error_info}", exc_info=True)

        finally:
            self._log_final_summary()
            log.info(f"Forecaster generation pipeline finished with status: {self.status}.")

    def _log_final_summary(self):
        """Connects to MongoDB to record the execution summary."""
        log.info("Recording final execution summary.")
        try:
            with MongoConnector() as client:
                log_collection = client[settings.MONGO_LOG_DB_NAME][settings.MONGO_LOG_COLLECTION]
                logger = ForecasterLogger(mongo_collection=log_collection)
                logger.log_summary_insights(
                    start_time=self.start_time,
                    status=self.status,
                    foracaster_generated_count=self.forecaster_count,
                    files_processed_count=self.files_count,
                    error_message=self.error_info
                )
                log.info("Execution summary successfully recorded to MongoDB.")
        except Exception as e:
            log.critical(f"Failed to record final execution summary: {e}")
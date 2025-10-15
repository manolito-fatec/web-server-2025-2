"""
Created on 10/13/2025 19:24

@author: otavio-calderan
"""

import datetime
import logging

from config import settings
from etl.connectors import MongoConnector
from etl.insights.extractor import InsightsExtractor
from etl.insights.generator import InsightsGenerator
from etl.insights.loader import InsightsLoader
from etl.logger import InsightsLogger

log = logging.getLogger(__name__)


class InsightsPipeline:
    """Orchestrates the entire insight generation pipeline."""

    def __init__(self):
        self.start_time = datetime.datetime.now(datetime.timezone.utc)
        self.status = "SUCCESS"
        self.error_info = None
        self.insights_count = 0
        self.files_count = 0
        self.extractor = InsightsExtractor(etl_config=settings.INSIGHTS_ETL_CONFIG)
        self.generator = InsightsGenerator(api_key=settings.GEMINI_API_KEY, etl_config=settings.INSIGHTS_ETL_CONFIG)

    def run(self):
        """Main entry point to run the insights pipeline."""
        log.info("Insights generation pipeline started.")
        try:
            log.info("[Stage 1/3] Starting data extraction from Postgres.")
            if not self.extractor.execute():
                raise Exception("Data extraction from Postgres failed.")
            log.info("[Stage 1/3] Data extraction completed successfully.")

            log.info("[Stage 2/3] Starting insight generation with AI model.")
            insights_data = self.generator.execute()
            self.files_count = self.generator.files_processed_count
            log.info(f"[Stage 2/3] Insight generation completed. Total insights: {len(insights_data)}.")

            if not insights_data:
                log.warning("No insights were generated. Skipping load stage.")
            else:
                log.info("[Stage 3/3] Starting data load to MongoDB.")
                with MongoConnector() as client:
                    insights_collection = client[settings.MONGO_INSIGHTS_DB_NAME][settings.MONGO_INSIGHTS_COLLECTION]
                    loader = InsightsLoader(mongo_collection=insights_collection)
                    self.insights_count = loader.execute(insights_data)
                log.info(f"[Stage 3/3] Data load completed. Documents inserted: {self.insights_count}.")

        except Exception as e:
            self.status = "FAILURE"
            self.error_info = str(e)
            log.error(f"Critical pipeline failure: {self.error_info}", exc_info=True)

        finally:
            self._log_final_summary()
            log.info(f"Insights generation pipeline finished with status: {self.status}.")

    def _log_final_summary(self):
        """Connects to MongoDB to record the execution summary."""
        log.info("Recording final execution summary.")
        try:
            with MongoConnector() as client:
                log_collection = client[settings.MONGO_LOG_DB_NAME][settings.MONGO_LOG_COLLECTION]
                logger = InsightsLogger(mongo_collection=log_collection)
                logger.log_summary_insights(
                    start_time=self.start_time,
                    status=self.status,
                    insights_generated_count=self.insights_count,
                    files_processed_count=self.files_count,
                    error_message=self.error_info
                )
                log.info("Execution summary successfully recorded to MongoDB.")
        except Exception as e:
            log.critical(f"Failed to record final execution summary: {e}")
"""
Created on 10/13/2025 21:01 

@author: otavio-calderan
"""

import schedule
import time
import logging
from config import settings
from .pipeline import AnonymizationPipeline
from .backup import DatabaseBackup
from .insights.pipeline import InsightsPipeline
from .forecast.pipeline import TicketForecasterPipeline

log = logging.getLogger(__name__)


def run_anonymization_job():
    """Runs the anonymization pipeline and, if successful, the database backup."""
    try:
        log.info("JOB INITIATED: Anonymization pipeline...")
        pipeline = AnonymizationPipeline()
        pipeline.run()
        log.info("JOB COMPLETED: Anonymization pipeline finished.")

        if pipeline.status == "SUCCESS":
            log.info("Anonymization pipeline was successful. Proceeding with database backup.")
            backup_task = DatabaseBackup()
            backup_task.execute()
        else:
            log.warning("Anonymization pipeline completed with failures. Backup will be skipped.")

    except Exception as e:
        log.error(f"JOB FAILED: Anonymization pipeline job failed with error: {e}", exc_info=True)


def run_insights_job():
    """Runs the complete insight generation pipeline."""
    try:
        log.info("JOB INITIATED: Insights generation pipeline...")
        pipeline = InsightsPipeline()
        pipeline.run()
        log.info("JOB COMPLETED: Insights generation pipeline finished successfully.")
    except Exception as e:
        log.error(f"JOB FAILED: Insights pipeline job failed with a critical error: {e}", exc_info=True)

def run_tickets_forecaster_job():
    """Runs the complete insight generation pipeline."""
    try:
        log.info("JOB INITIATED: Insights Tikcets forecaster generation pipeline...")
        pipeline = TicketForecasterPipeline(settings.INSIGHTS_TICKETS_FORECASTER_ETL_CONFIG)
        pipeline.run()
        log.info("JOB COMPLETED: Tickets forecaster generation pipeline finished successfully.")
    except Exception as e:
        log.error(f"JOB FAILED: Tickets forecaster job failed with a critical error: {e}", exc_info=True)

def start_scheduler_loop():
    """Configures and starts the main scheduler loop for all jobs."""
    log.info("ETL Scheduler starting up...")

    log.info("Performing initial run of all jobs on startup...")
    run_anonymization_job()
    run_insights_job()
    run_tickets_forecaster_job()
    schedule.every().day.at("03:00").do(run_anonymization_job)
    log.info("JOB SCHEDULED: Anonymization pipeline will run daily at 03:00 AM.")

    schedule.every().monday.at("04:00").do(run_insights_job)
    log.info("JOB SCHEDULED: Insights pipeline will run every Monday at 04:00 AM.")

    schedule.every().day.at("05:00").do(run_tickets_forecaster_job)
    log.info("JOB SCHEDULED: Forecaster pipeline will run daily at 05:00 AM.")

    log.info("Scheduler is now running. Waiting for pending jobs...")
    while True:
        schedule.run_pending()
        time.sleep(1)

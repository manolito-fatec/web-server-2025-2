"""
Created on 10/13/2025 21:01 

@author: otavio-calderan
"""

import logging
import os
import schedule
import sys
import time
from config import settings

from .backup import DatabaseBackup
from .forecast.pipeline import TicketForecasterPipeline
from .insights.pipeline import InsightsPipeline
from .pipeline import AnonymizationPipeline
from .sla_prediction.pipeline import SlaPredictionPipeline

log = logging.getLogger(__name__)

FLAG_FILE_PATH = "/app/restore_status/restore_complete.signal"


def wait_for_restore_signal():
    """
    Blocks execution until the 'restore_complete.signal' file exists.
    """
    log.info("Waiting for restore signal from 'etl-restore-watcher'...")

    max_retries = 60

    for attempt in range(max_retries):
        if os.path.exists(FLAG_FILE_PATH):
            log.info(f"Signal found! ({FLAG_FILE_PATH}). Restore is complete.")
            return True
        else:
            log.warning(f"Waiting for signal... (Attempt {attempt + 1}/{max_retries})")
            time.sleep(10)

    log.critical("Timeout! The 'etl-restore-watcher' never signaled completion.")
    return False


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
    """Runs the complete tickets forecaster generation pipeline."""
    try:
        log.info("JOB INITIATED: Insights Tikcets forecaster generation pipeline...")
        pipeline = TicketForecasterPipeline(settings.INSIGHTS_TICKETS_FORECASTER_ETL_CONFIG)
        pipeline.run()
        log.info("JOB COMPLETED: Tickets forecaster generation pipeline finished successfully.")
    except Exception as e:
        log.error(f"JOB FAILED: Tickets forecaster job failed with a critical error: {e}", exc_info=True)


def run_sla_predictions_job():
    """Runs the SLA prediction pipeline."""
    try:
        log.info("JOB INITIATED: SLA predictions pipeline...")
        pipeline = SlaPredictionPipeline(settings.SLA_PREDICTIONS_ETL_CONFIG)
        pipeline.run()
        log.info("JOB COMPLETED: SLA predictions pipeline finished successfully.")
    except Exception as e:
        log.error(f"JOB FAILED: SLA predictions job failed with a critical error: {e}", exc_info=True)


def start_scheduler_loop():
    """Configures and starts the main scheduler loop for all jobs."""
    log.info("ETL Scheduler starting up...")

    if not wait_for_restore_signal():
        log.critical("Scheduler is exiting, restore was not signaled by watcher.")
        sys.exit(1)

    log.info("Performing initial run of all jobs on startup...")
    run_anonymization_job()
    run_insights_job()
    run_tickets_forecaster_job()
    run_sla_predictions_job()

    try:
        os.remove(FLAG_FILE_PATH)
        log.info(f"Restore signal consumed (deleted): {FLAG_FILE_PATH}")
    except OSError as e:
        log.warning(f"Could not remove signal file (might be already gone): {e}")

    schedule.every().day.at("03:00").do(run_anonymization_job)
    log.info("JOB SCHEDULED: Anonymization pipeline will run daily at 03:00 AM.")

    schedule.every().monday.at("04:00").do(run_insights_job)
    log.info("JOB SCHEDULED: Insights pipeline will run every Monday at 04:00 AM.")

    schedule.every().day.at("05:00").do(run_tickets_forecaster_job)
    log.info("JOB SCHEDULED: Forecaster pipeline will run daily at 05:00 AM.")

    schedule.every().day.at("06:00").do(run_sla_predictions_job)
    log.info("JOB SCHEDULED: SLA Predictions pipeline will run daily at 06:00 AM.")

    log.info("Scheduler is now running. Waiting for pending jobs...")
    while True:
        schedule.run_pending()
        time.sleep(1)

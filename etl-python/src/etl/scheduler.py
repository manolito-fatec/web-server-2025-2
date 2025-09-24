import schedule
import time
import logging

from .pipeline import AnonymizationPipeline

log = logging.getLogger(__name__)


def run_pipeline_job():
    try:
        log.info("Anonymization pipeline job initiated")
        pipeline = AnonymizationPipeline()
        pipeline.run()
        log.info("Anonymization pipeline job completed successfully.")
    except Exception as e:
        log.error(f"Anonymization pipeline job failed with error: {e}")


def scheduler_loop():
    log.info("Performing initial run of the anonymization pipeline on startup.")
    run_pipeline_job()

    log.info("Anonymization pipeline scheduled to run daily at 03:00 PM.")

    schedule.every().day.at("03:00").do(run_pipeline_job)

    while True:
        schedule.run_pending()
        time.sleep(1)

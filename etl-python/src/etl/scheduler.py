import schedule
import time
import logging

from .pipeline import AnonymizationPipeline
from .backup import DatabaseBackup

log = logging.getLogger(__name__)


def run_pipeline_job():
    try:
        log.info("Anonymization pipeline job initiated")
        pipeline = AnonymizationPipeline()
        pipeline.run()
        log.info("Anonymization pipeline job completed successfully.")

        if pipeline.status == "SUCCESS":
            log.info("Pipeline finished successfully. Proceeding with database backup.")
            backup_task = DatabaseBackup()
            backup_task.execute()
        else:
            log.warning("Pipeline completed with failures. Backup will be skipped.")

    except Exception as e:
        log.error(f"Anonymization pipeline job failed with error: {e}")


def scheduler_loop():
    log.info("Performing initial run of the anonymization pipeline on startup.")
    run_pipeline_job()

    log.info("Anonymization pipeline scheduled to run daily at 03:00 AM.")

    schedule.every().day.at("03:00").do(run_pipeline_job)

    while True:
        schedule.run_pending()
        time.sleep(1)

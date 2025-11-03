"""
Created on 10/31/2025 19:41 

@author: otavio-calderan
"""

import sys
import logging
import time

from config.setup_mongo import setup_audit_logs, setup_product_insights, setup_tickets_forecaster
from config.setup_log import configurar_logger

from etl.pipeline import AnonymizationPipeline

log = logging.getLogger(__name__)

if __name__ == "__main__":

    configurar_logger()
    log.info("Starting on-demand cleanup process (cleanup.py)...")

    try:
        setup_audit_logs()
        setup_product_insights()
        setup_tickets_forecaster()
        log.info("MongoDB configuration verified.")
    except Exception as e:
        log.error(f"CRITICAL ERROR: Failed to configure MongoDB. Cleanup will not be executed. Error: {e}")
        sys.exit(1)

    log.warning("WARNING: This script will make permanent cleanup changes to the database.")
    time.sleep(3)

    try:
        log.info("Starting the AnonymizationPipeline...")
        pipeline = AnonymizationPipeline()
        pipeline.run()

        if pipeline.status == "SUCCESS":
            log.info("AnonymizationPipeline completed SUCCESSFULLY.")
        else:
            log.error(f"AnonymizationPipeline failed. Error: {pipeline.error_info}")
            sys.exit(1)

    except Exception as e:
        log.error(f"CRITICAL ERROR during cleanup pipeline execution: {e}")
        sys.exit(1)

    log.info("Cleanup process (cleanup.py) finished successfully.")

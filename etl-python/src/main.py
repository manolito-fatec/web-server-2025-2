"""
Created on 09/10/2025 00:50 

@author: otavio-calderan
"""

import sys
import time
import logging
from config.setup_mongo import setup_audit_logs
from etl.pipeline import AnonymizationPipeline

log = logging.getLogger(__name__)

if __name__ == "__main__":

    try:
        setup_audit_logs()
    except Exception as e:
        log.error(f"CRITICAL ERROR: Failed to configure MongoDB. The ETL will not be executed. Error: {e}")
        sys.exit(1)

    log.warning("WARNING: This script will make permanent changes to the database.")
    time.sleep(3)

    pipeline = AnonymizationPipeline()
    pipeline.run()

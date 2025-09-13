"""
Created on 09/10/2025 19:16 

@author: otavio-calderan
"""

from pymongo import MongoClient
from pymongo.errors import OperationFailure
from config import settings
import logging

log = logging.getLogger(__name__)

FIVE_YEARS_IN_SECONDS = 157680000

def setup_audit_logs():
    """
    Ensures that the 'auditLogs' collection and its indexes in MongoDB exist according to the design.
    """
    mongo_client = None
    try:
        mongo_client = MongoClient(settings.MONGO_URI)
        db = mongo_client[settings.MONGO_DATABASE]
        log.debug(f"Successfully connected to the database '{settings.MONGO_DATABASE}'.")

        log.info(f"\nSetting up the collection '{settings.MONGO_COLLECTION}'...")
        audit_logs = db[settings.MONGO_COLLECTION]

        log.info("Creating primary compound index (actor.type, actor.userId)...")
        audit_logs.create_index([("actor.type", 1), ("actor.userId", 1), ("timestamp", -1)])

        log.info("Creating index for scripts (actor.scriptName)...")
        audit_logs.create_index([("actor.scriptName", 1), ("timestamp", -1)])

        log.info("Creating index by service and action...")
        audit_logs.create_index([("service", 1), ("action", 1), ("timestamp", -1)])

        log.info(f"Creating/verifying TTL retention index ({FIVE_YEARS_IN_SECONDS} seconds)...")
        try:
            audit_logs.create_index([("timestamp", 1)], expireAfterSeconds=FIVE_YEARS_IN_SECONDS)
        except OperationFailure as e:
            if "expireAfterSeconds differs" in str(e):
                log.error("TTL index already exists with a different value. Recreating...")
                audit_logs.drop_index("timestamp_1")
                audit_logs.create_index([("timestamp", 1)], expireAfterSeconds=FIVE_YEARS_IN_SECONDS)
                log.info("TTL index successfully recreated.")
            else:
                raise e

        log.info(f"Collection '{settings.MONGO_COLLECTION}' successfully configured.")

    except Exception as e:
        log.error(f"\nERROR: Failed to configure MongoDB infrastructure. {e}")
    finally:
        if mongo_client:
            mongo_client.close()
            log.info("\nMongoDB connection closed.")

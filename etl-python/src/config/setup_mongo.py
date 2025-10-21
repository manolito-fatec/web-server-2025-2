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
INSIGHTS_COLLECTION_NAME = "product_insights"
TICKETS_FORECASTER_COLLECTION_NAME = "tickets_forecaster"

def setup_audit_logs():
    """
    Ensures that the 'auditLogs' collection and its indexes in MongoDB exist according to the design.
    """
    mongo_client = None
    try:
        mongo_client = MongoClient(settings.MONGO_URI)
        db = mongo_client[settings.MONGO_LOG_DB_NAME]
        log.debug(f"Successfully connected to the database '{settings.MONGO_LOG_DB_NAME}'.")

        log.info(f"\nSetting up the collection '{settings.MONGO_LOG_COLLECTION}'...")
        audit_logs = db[settings.MONGO_LOG_COLLECTION]

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

        log.info(f"Collection '{settings.MONGO_LOG_COLLECTION}' successfully configured.")

    except Exception as e:
        log.error(f"\nERROR: Failed to configure MongoDB infrastructure. {e}")
    finally:
        if mongo_client:
            mongo_client.close()
            log.info("\nMongoDB connection closed.")


def setup_product_insights():
    """
    Ensures that the 'product_insights' collection and its indexes in MongoDB exist according to the design.
    """
    mongo_client = None
    try:
        mongo_client = MongoClient(settings.MONGO_URI)
        db = mongo_client[settings.MONGO_INSIGHTS_DB_NAME]
        log.debug(f"Successfully connected to the database '{settings.MONGO_INSIGHTS_DB_NAME}'.")

        log.info(f"\nSetting up the collection '{INSIGHTS_COLLECTION_NAME}'...")
        insights_collection = db[INSIGHTS_COLLECTION_NAME]

        log.info("Creating primary index by company and date (company_name, dth)...")
        insights_collection.create_index([("company_name", 1), ("dth", -1)])

        log.info("Creating secondary index by product and date (product_name, dth)...")
        insights_collection.create_index([("product_name", 1), ("dth", -1)])

        log.info(f"Creating/verifying TTL retention index ({FIVE_YEARS_IN_SECONDS} seconds)...")
        try:
            insights_collection.create_index([("dth", 1)], expireAfterSeconds=FIVE_YEARS_IN_SECONDS)
        except OperationFailure as e:
            if "expireAfterSeconds differs" in str(e):
                log.warning("TTL index already exists with a different value. Recreating...")
                insights_collection.drop_index("dth_1")
                insights_collection.create_index([("dth", 1)], expireAfterSeconds=FIVE_YEARS_IN_SECONDS)
                log.info("TTL index successfully recreated.")
            elif "index with same options exists" not in str(e):
                raise e

        log.info(f"Collection '{INSIGHTS_COLLECTION_NAME}' successfully configured.")

    except Exception as e:
        log.error(f"\nERROR: Failed to configure MongoDB infrastructure for insights. {e}")
    finally:
        if mongo_client:
            mongo_client.close()
            log.info("\nMongoDB connection closed.")

def setup_tickets_forecaster():
    """
    Ensures that the 'product_insights' collection and its indexes in MongoDB exist according to the design.
    """
    mongo_client = None
    try:
        mongo_client = MongoClient(settings.MONGO_URI)
        db = mongo_client[settings.MONGO_INSIGHTS_DB_NAME]
        log.debug(f"Successfully connected to the database '{settings.MONGO_INSIGHTS_DB_NAME}'.")

        if TICKETS_FORECASTER_COLLECTION_NAME not in db.list_collection_names():
                db.create_collection(TICKETS_FORECASTER_COLLECTION_NAME)
                log.info(f"Collection '{TICKETS_FORECASTER_COLLECTION_NAME}' created in the database.")
        log.info(f"\nSetting up the collection '{TICKETS_FORECASTER_COLLECTION_NAME}'...")
        forecaster_collection = db[TICKETS_FORECASTER_COLLECTION_NAME]

        log.info("Creating primary index by company and date (companyId, dth)...")
        forecaster_collection.create_index([("companyId", 1), ("dth", -1)])

        log.info("Creating secondary index by product and date (productId, dth)...")
        forecaster_collection.create_index([("productId", 1), ("dth", -1)])

        log.info(f"Creating/verifying TTL retention index ({FIVE_YEARS_IN_SECONDS} seconds)...")
        try:
            forecaster_collection.create_index([("dth", 1)], expireAfterSeconds=FIVE_YEARS_IN_SECONDS)
        except OperationFailure as e:
            if "expireAfterSeconds differs" in str(e):
                log.warning("TTL index already exists with a different value. Recreating...")
                forecaster_collection.drop_index("dth_1")
                forecaster_collection.create_index([("dth", 1)], expireAfterSeconds=FIVE_YEARS_IN_SECONDS)
                log.info("TTL index successfully recreated.")
            elif "index with same options exists" not in str(e):
                raise e

        log.info(f"Collection '{TICKETS_FORECASTER_COLLECTION_NAME}' successfully configured.")

    except Exception as e:
        log.error(f"\nERROR: Failed to configure MongoDB infrastructure for forecaster. {e}")
    finally:
        if mongo_client:
            mongo_client.close()
            log.info("\nMongoDB connection closed.")

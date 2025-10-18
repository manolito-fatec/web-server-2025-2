"""
Created on 10/13/2025 19:24 

@author: otavio-calderan
"""

import logging
from pymongo.errors import BulkWriteError

log = logging.getLogger(__name__)


class InsightsLoader:
    """
    Responsible for loading the generated insight documents into a
    MongoDB collection using an external connector.
    """

    def __init__(self, mongo_collection):
        self.collection = mongo_collection

    def execute(self, insights_documents):
        """
        Receives a list of documents and bulk-inserts them into MongoDB.
        """
        if not insights_documents:
            return 0

        try:
            log.info(f"Bulk inserting {len(insights_documents)} documents...")
            result = self.collection.insert_many(insights_documents, ordered=False)
            inserted_count = len(result.inserted_ids)
            return inserted_count
        except BulkWriteError as bwe:
            inserted_count = bwe.details['nInserted']
            log.error(f"Bulk write errors occurred, but {inserted_count} documents were successfully inserted.")
            return inserted_count
        except Exception as e:
            log.error(f"Error inserting documents into MongoDB: {e}")
            return 0

"""
Created on 10/17/2025 21:50 

@author: paulo-arantes
"""

import logging
from pymongo.errors import BulkWriteError

log = logging.getLogger(__name__)


class TicketForecasterLoader:
    """
    Responsible for loading the generated Forecaster documents into a
    MongoDB collection using an external connector.
    """

    def __init__(self, mongo_collection):
        self.collection = mongo_collection

    def execute(self, tickets_forecaster_documents):
        """
        Receives a list of documents and bulk-inserts them into MongoDB.
        """
        if not tickets_forecaster_documents:
            return 0

        try:
            log.info(f"Bulk inserting {len(tickets_forecaster_documents)} documents...")
            result = self.collection.insert_many(tickets_forecaster_documents, ordered=False)
            inserted_count = len(result.inserted_ids)
            return inserted_count
        except BulkWriteError as bwe:
            inserted_count = bwe.details['nInserted']
            log.error(f"Bulk write errors occurred, but {inserted_count} documents were successfully inserted.")
            return inserted_count
        except Exception as e:
            log.error(f"Error inserting documents into MongoDB: {e}")
            return 0

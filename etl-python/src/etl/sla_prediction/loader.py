"""
Created on 10/23/2025 00:04 

@author: AndreWakugawa
"""

import logging
from pymongo.errors import BulkWriteError

log = logging.getLogger(__name__)


class SlaPredictionLoader:
    """
    Responsible for loading the generated SLA prediction documents into a
    MongoDB collection.
    """

    def __init__(self, mongo_collection):
        self.collection = mongo_collection

    def execute(self, sla_predictions_documents):
        """
        Receives a list of SLA prediction documents and bulk-inserts them 
        into the MongoDB collection.

        Returns:
            int: The number of documents successfully inserted.
        """
        if not sla_predictions_documents:
            return 0

        try:
            log.info(f"Attempting to bulk insert {len(sla_predictions_documents)} SLA prediction documents...")
            result = self.collection.insert_many(sla_predictions_documents, ordered=False)
            inserted_count = len(result.inserted_ids)
            log.info(f"Successfully inserted {inserted_count} SLA prediction documents.")
            return inserted_count
        except BulkWriteError as bwe:
            inserted_count = bwe.details.get('nInserted', 0)
            log.error(f"Bulk write errors occurred during SLA prediction insertion.")
            log.error(f"Successfully inserted {inserted_count} documents despite errors.")
            return inserted_count
            
        except Exception as e:
            log.error(f"An unexpected error occurred inserting SLA prediction documents into MongoDB: {e}", exc_info=True)
            return 0
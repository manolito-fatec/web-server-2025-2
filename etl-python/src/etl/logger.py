"""
Created on 10/09/2025 00:41

@author: otavio-calderan
"""

import datetime
from config import settings
import logging

log = logging.getLogger(__name__)

class AuditLogger:
    """Class responsible for logging audit logs in MongoDB."""
    def __init__(self, mongo_collection):
        self.collection = mongo_collection

    def log_individual_update(self, db_name: str, table_name: str, record_id: any):
        """Logs an audit log for a single anonymized record."""
        log_document = {
            "timestamp": datetime.datetime.now(datetime.timezone.utc),
            "level": "AUDIT", "service": settings.SERVICE_NAME, "action": "LEGACY_RECORD_ANONYMIZED",
            "actor": {"type": "SYSTEM_SCRIPT", "scriptName": settings.SCRIPT_NAME},
            "details": {
                "targetDatabase": db_name, "targetTable": table_name, "targetId": record_id,
                "message": f"Registro {record_id} da tabela {table_name} foi anonimizado."
            }
        }
        self.collection.insert_one(log_document)

    def log_schema_change(self, db_name: str, table_name: str, deleted_columns: list):
        """Logs an audit log for a schema change (column deletion)."""
        column_list = ', '.join(deleted_columns)
        log_document = {
            "timestamp": datetime.datetime.now(datetime.timezone.utc),
            "level": "AUDIT", "service": settings.SERVICE_NAME, "action": "SCHEMA_CHANGE_COLUMNS_DELETED",
            "actor": {"type": "SYSTEM_SCRIPT", "scriptName": settings.SCRIPT_NAME},
            "details": {
                "targetDatabase": db_name, "targetTable": table_name,
                "message": f"As colunas [{column_list}] foram excluídas da tabela {table_name}."
            }
        }
        self.collection.insert_one(log_document)

    def log_summary(self, start_time, total_scanned, total_anonymized, status, error_message=None):
        """Logs a summary log at the end of the script's execution."""
        end_time = datetime.datetime.now(datetime.timezone.utc)
        summary_doc = {
            "timestamp": end_time, "level": "AUDIT", "service": settings.SERVICE_NAME,
            "action": "ETL_EXECUTION_COMPLETED", "actor": {"type": "SYSTEM_SCRIPT", "scriptName": settings.SCRIPT_NAME},
            "details": {
                "status": status, "startTime": start_time, "endTime": end_time,
                "durationInSeconds": (end_time - start_time).total_seconds(),
                "recordsScanned": total_scanned, "recordsAnonymized": total_anonymized,
                "message": f"Execução do ETL de anonimização concluída. Status: {status}"
            }
        }
        if error_message:
            summary_doc["details"]["errorMessage"] = str(error_message)

        self.collection.insert_one(summary_doc)
        log.info("Summary of the execution recorded in MongoDB.")

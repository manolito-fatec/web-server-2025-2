"""
Created on 10/09/2025 00:41

@author: otavio-calderan
"""

import datetime
from pymongo import MongoClient

SCRIPT_NAME = "personal_data/run.py"
SERVICE_NAME = "ETL-LEGACY-ANONYMIZATION"


def log_individual_update(mongo_collection, db_name: str, table_name: str, record_id: any):
    """Registra um log de auditoria para um único registro anonimizado."""
    log_document = {
        "timestamp": datetime.datetime.now(datetime.timezone.utc),
        "level": "AUDIT",
        "service": SERVICE_NAME,
        "action": "LEGACY_RECORD_ANONYMIZED",
        "actor": {
            "type": "SYSTEM_SCRIPT",
            "scriptName": SCRIPT_NAME
        },
        "details": {
            "targetDatabase": db_name,
            "targetTable": table_name,
            "targetId": record_id,
            "message": f"Registro {record_id} da tabela {table_name} foi anonimizado com sucesso."
        }
    }
    mongo_collection.insert_one(log_document)


def log_summary(mongo_collection, start_time, total_scanned, total_anonymized, status, error_message=None):
    """Registra um log de sumário ao final da execução do script."""
    end_time = datetime.datetime.now(datetime.timezone.utc)
    duration = (end_time - start_time).total_seconds()

    summary_doc = {
        "timestamp": end_time,
        "level": "AUDIT",
        "service": SERVICE_NAME,
        "action": "ETL_EXECUTION_COMPLETED",
        "actor": {
            "type": "SYSTEM_SCRIPT",
            "scriptName": SCRIPT_NAME
        },
        "details": {
            "status": status,
            "startTime": start_time,
            "endTime": end_time,
            "durationInSeconds": duration,
            "recordsScanned": total_scanned,
            "recordsAnonymized": total_anonymized,
            "message": f"Execução do ETL de anonimização concluída. Status: {status}"
        }
    }
    if error_message:
        summary_doc["details"]["errorMessage"] = str(error_message)

    mongo_collection.insert_one(summary_doc)

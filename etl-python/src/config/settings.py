"""
Created on 09/12/2025 08:58 

@author: otavio-calderan
"""

import os

from dotenv import load_dotenv

load_dotenv()

DB_HOST = os.getenv("DB_HOST")
DB_USER = os.getenv("DB_USER")
DB_PASSWORD = os.getenv("DB_PASSWORD")
DB_NAME = os.getenv("DB_NAME")
DB_PORT = os.getenv("DB_PORT")

MONGO_URI = os.getenv("MONGO_URI")
MONGO_DATABASE = os.getenv("MONGO_DATABASE")
MONGO_COLLECTION = os.getenv("MONGO_COLLECTION", "auditLogs")

TABLES_TO_ANONYMIZE = [
    {
        "table_name": "agents",
        "columns_to_delete": ["email", "phone", "full_name"],
        "pk_column": "agent_id"
    },
    {
        "table_name": "audit_logs",
        "columns_to_delete": ["performed_by"],
        "pk_column": "audit_id"
    },
    {
        "table_name": "ticket_interactions",
        "ner_columns": ["message"],
        "regex_columns": ["message"],
        "pk_column": "interaction_id"
    },
    {
        "table_name": "tickets",
        "ner_columns": ["title", "description"],
        "regex_columns": ["title", "description"],
        "pk_column": "ticket_id"
    },
    {
        "table_name": "users",
        "columns_to_delete": ["email", "phone", "cpf", "full_name"],
        "pk_column": "user_id"
    }
]

BATCH_SIZE = 500

SCRIPT_NAME = "etl/pipeline.py"
SERVICE_NAME = "ETL-LEGACY-ANONYMIZATION"
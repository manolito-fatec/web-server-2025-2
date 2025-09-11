"""
Created on 09/10/2025 00:42

@author: otavio-calderan
"""

import os
import psycopg2
import psycopg2.extras
import datetime
import time
from dotenv import load_dotenv
from pymongo import MongoClient

from .anonymizer import anonymize_text
from .logger import log_individual_update, log_summary

load_dotenv()

DB_HOST = os.getenv("DB_HOST")
DB_USER = os.getenv("DB_USER")
DB_PASSWORD = os.getenv("DB_PASSWORD")
DB_NAME = os.getenv("DB_NAME")
DB_PORT = os.getenv("DB_PORT")

MONGO_URI = os.getenv("MONGO_URI")
MONGO_DATABASE = os.getenv("MONGO_DATABASE")
MONGO_COLLECTION = os.getenv("MONGO_COLLECTION")

TABLES_TO_ANONYMIZE = [
    ("Agents", ["FullName", "Email", "Phone"], "AgentId"),
    ("AuditLogs", ["PerformedBy"], "AuditId"),
    ("TicketInteractions", ["Message"], "InteractionId"),
    ("Tickets", ["Title", "Description"], "TicketId"),
    ("Users", ["FullName", "Email", "Phone", "CPF"], "UserId")
]
BATCH_SIZE = 500


def run_anonymization_etl():
    """Função que orquestra o ETL de anonimização, agora encapsulada."""
    start_time = datetime.datetime.now(datetime.timezone.utc)
    total_records_scanned = 0
    total_records_anonymized = 0
    status = "SUCCESS"
    error_info = None

    pg_conn = None
    mongo_client = None

    try:
        print("Estabelecendo conexão com o PostgreSQL...")
        pg_conn = psycopg2.connect(
            dbname=DB_NAME, user=DB_USER, password=DB_PASSWORD, host=DB_HOST, port=DB_PORT
        )
        print("Conexão com PostgreSQL estabelecida.")

        print("Estabelecendo conexão com o MongoDB...")
        mongo_client = MongoClient(MONGO_URI)
        audit_collection = mongo_client[MONGO_DATABASE][MONGO_COLLECTION]
        print("Conexão com MongoDB estabelecida.")

        for table_name, columns, pk_column in TABLES_TO_ANONYMIZE:
            print(f"\n--- Processando tabela: {table_name} ---")
            
            all_records_to_update = []
            
            with pg_conn.cursor(name=f'fetch_{table_name}', cursor_factory=psycopg2.extras.DictCursor) as pg_cursor:
                select_columns = f'"{pk_column}", ' + ', '.join([f'"{col}"' for col in columns])
                pg_cursor.execute(f'SELECT {select_columns} FROM "{table_name}"')
                
                while True:
                    rows = pg_cursor.fetchmany(BATCH_SIZE)
                    if not rows:
                        break

                    total_records_scanned += len(rows)

                    for row in rows:
                        update_payload = {"pk_value": row[pk_column]}
                        needs_update = False

                        for col in columns:
                            original_value = row[col]
                            anonymized_value, was_changed = anonymize_text(original_value)
                            if was_changed:
                                needs_update = True
                                update_payload[col] = anonymized_value
                        
                        if needs_update:
                            all_records_to_update.append(update_payload)
            
            print(f"Leitura da tabela '{table_name}' concluída. {total_records_scanned} registros lidos no total.")
            print(f"Encontrados {len(all_records_to_update)} registros para anonimizar.")

            if all_records_to_update:
                with pg_conn.cursor() as update_cursor:
                    for record in all_records_to_update:
                        pk_value = record.pop('pk_value')
                        set_clause = ", ".join([f'"{col}" = %s' for col in record.keys()])
                        values = list(record.values())
                        values.append(pk_value)
                        
                        update_query = f'UPDATE "{table_name}" SET {set_clause} WHERE "{pk_column}" = %s'
                        update_cursor.execute(update_query, values)
                        
                        log_individual_update(audit_collection, DB_NAME, table_name, pk_value)
                        total_records_anonymized += 1
                
                pg_conn.commit()
                print(f"Tabela '{table_name}' anonimizada e commitada. {total_records_anonymized} registros "
                      f"atualizados no total.")

    except Exception as e:
        status = "FAILURE"
        error_info = e
        print(f"\nERRO CRÍTICO: O processo foi interrompido. {e}")
        if pg_conn:
            pg_conn.rollback()
    finally:
        print("\n--- Finalizando o processo ---")
        if mongo_client:
            log_summary(
                mongo_client[MONGO_DATABASE][MONGO_COLLECTION],
                start_time,
                total_records_scanned,
                total_records_anonymized,
                status,
                error_info
            )
            print("Sumário da execução registrado no MongoDB.")
            mongo_client.close()
        
        if pg_conn:
            pg_conn.close()
            print("Conexão com PostgreSQL fechada.")
        
        print(f"Execução finalizada. Status: {status}")

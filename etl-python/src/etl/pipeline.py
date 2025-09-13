"""
Created on 09/10/2025 00:42

@author: otavio-calderan
"""

import datetime
import logging

import psycopg2.extras
from config import settings

from .anonymizer import Anonymizer
from .connectors import PostgresConnector, MongoConnector
from .logger import AuditLogger

log = logging.getLogger(__name__)


class AnonymizationPipeline:
    """Orchestrates the complete ETL process for data de-identification."""

    def __init__(self):
        self.start_time = datetime.datetime.now(datetime.timezone.utc)
        self.total_scanned = 0
        self.total_anonymized = 0
        self.status = "SUCCESS"
        self.error_info = None
        self.pg_connector = PostgresConnector()
        self.anonymizer = Anonymizer()

    def run(self):
        """Main entry point to run the ETL pipeline."""
        try:
            with self.pg_connector as pg_conn:
                for table_config in settings.TABLES_TO_ANONYMIZE:
                    self._process_table(table_config, pg_conn)

        except Exception as e:
            self.status = "FAILURE"
            self.error_info = e
            log.error(f"\nCRITICAL ERROR: The process was interrupted. {e}")
        finally:
            self._log_final_summary()
            log.info(f"Execution finished. Status: {self.status}")

    def _process_table(self, table_config: dict, pg_conn):
        """Processes a single table: deletes columns, then anonymizes data."""
        table_name = table_config['table_name']

        self._handle_column_deletion(table_config, pg_conn)

        records_to_update = self._extract_and_transform(table_config, pg_conn)

        if records_to_update:
            log.info(f"Found {len(records_to_update)} records to anonymize in table '{table_name}'.")
            self._load_updates(records_to_update, table_config, pg_conn)
        else:
            log.info(f"No records needed anonymization in table '{table_name}'.")

    def _handle_column_deletion(self, table_config: dict, pg_conn):
        """Deletes specified columns from a table if they exist."""
        table_name = table_config['table_name']
        columns_to_delete = table_config.get('columns_to_delete', [])

        if not columns_to_delete:
            return

        with pg_conn.cursor() as cursor:
            drop_clauses = ', '.join([f'DROP COLUMN IF EXISTS "{col}"' for col in columns_to_delete])
            query = f'ALTER TABLE "{table_name}" {drop_clauses}'

            log.info(f"Executing column deletion on table '{table_name}': {query}")
            cursor.execute(query)

            with MongoConnector() as mongo_client:
                audit_collection = mongo_client[settings.MONGO_DATABASE][settings.MONGO_COLLECTION]
                logger = AuditLogger(audit_collection)
                logger.log_schema_change(settings.DB_NAME, table_name, columns_to_delete)

            log.info(f"Successfully deleted columns {columns_to_delete} from table '{table_name}'.")

    def _extract_and_transform(self, table_config: dict, pg_conn) -> list:
        """Extracts data in batches and applies NER and Regex anonymization."""
        table_name = table_config['table_name']
        pk_column = table_config['pk_column']

        ner_columns = table_config.get('ner_columns', [])
        regex_columns = table_config.get('regex_columns', [])

        all_columns_to_process = list(set(ner_columns + regex_columns))

        if not all_columns_to_process:
            log.info(f"No columns configured for anonymization in table '{table_name}'. Skipping.")
            return []

        all_records_to_update = []

        with pg_conn.cursor(name=f'fetch_{table_name}', cursor_factory=psycopg2.extras.DictCursor) as pg_cursor:
            select_cols = f'"{pk_column}", ' + ', '.join([f'"{c}"' for c in all_columns_to_process])
            pg_cursor.execute(f'SELECT {select_cols} FROM "{table_name}"')

            while True:
                rows = pg_cursor.fetchmany(settings.BATCH_SIZE)
                if not rows:
                    break

                self.total_scanned += len(rows)

                for row in rows:
                    update_payload = {"pk_value": row[pk_column]}
                    needs_update = False

                    for col in all_columns_to_process:
                        original_value = row[col]
                        anonymized_val = original_value
                        changed_by_ner = False
                        changed_by_regex = False

                        if col in ner_columns:
                            anonymized_val, changed_by_ner = self.anonymizer.anonymize_with_ner(anonymized_val)

                        if col in regex_columns:
                            anonymized_val, changed_by_regex = self.anonymizer.anonymize_with_regex(anonymized_val)

                        if changed_by_ner or changed_by_regex:
                            needs_update = True
                            update_payload[col] = anonymized_val

                    if needs_update:
                        all_records_to_update.append(update_payload)

        return all_records_to_update

    def _load_updates(self, records: list, table_config: dict, pg_conn):
        """Executes the UPDATE commands in the database and logs the individual records."""
        table_name = table_config['table_name']
        pk_column = table_config['pk_column']

        with MongoConnector() as mongo_client:
            audit_collection = mongo_client[settings.MONGO_DATABASE][settings.MONGO_COLLECTION]
            logger = AuditLogger(audit_collection)

            with pg_conn.cursor() as update_cursor:
                for record in records:
                    pk_value = record.pop('pk_value')
                    set_clause = ", ".join([f'"{col}" = %s' for col in record.keys()])
                    values = list(record.values())

                    update_query = f'UPDATE "{table_name}" SET {set_clause} WHERE "{pk_column}" = %s'
                    update_cursor.execute(update_query, values + [pk_value])

                    logger.log_individual_update(settings.DB_NAME, table_name, pk_value)
                    self.total_anonymized += 1

            log.info(f"{len(records)} records were updated in table '{table_name}'.")

    def _log_final_summary(self):
        """Connects to MongoDB only to log the final summary."""
        try:
            with MongoConnector() as mongo_client:
                audit_collection = mongo_client[settings.MONGO_DATABASE][settings.MONGO_COLLECTION]
                logger = AuditLogger(audit_collection)
                logger.log_summary(self.start_time, self.total_scanned, self.total_anonymized, self.status,
                                   self.error_info)
        except Exception as e:
            log.error(f"ERROR RECORDING FINAL SUMMARY: {e}")
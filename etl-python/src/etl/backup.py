"""
Created on 09/24/2025 14:45

@author: otavio-calderan
"""

import os
import subprocess
import logging
from datetime import datetime

log = logging.getLogger(__name__)

class DatabaseBackup:
    def __init__(self, backup_directory="/app/dump"):
        """
        Initializes the backup utility by reading connection settings
        directly from environment variables.
        """
        self.host = os.environ.get('DB_HOST')
        self.port = os.environ.get('DB_PORT')
        self.user = os.environ.get('DB_USER')
        self.password = os.environ.get('DB_PASSWORD')
        self.dbname = os.environ.get('DB_NAME')
        self.backup_directory = backup_directory

        if not all([self.host, self.port, self.user, self.password, self.dbname]):
            raise ValueError("Uma ou mais variáveis de ambiente do banco de dados não foram definidas.")

    def execute(self):
        try:
            os.makedirs(self.backup_directory, exist_ok=True)

            timestamp = datetime.now().strftime('%Y-%m-%d_%H%M%S')
            file_path = os.path.join(self.backup_directory, f"backup_{self.dbname}_{timestamp}.dump")

            log.info(f"Starting database backup to '{file_path}'...")

            command = [
                'pg_dump',
                '--host', self.host,
                '--port', self.port,
                '--username', self.user,
                '--dbname', self.dbname,
                '--file', file_path,
                '--format', 'c',
                '--blobs',
                '--verbose'
            ]

            env = os.environ.copy()
            env['PGPASSWORD'] = self.password

            process = subprocess.run(
                command,
                env=env,
                check=True,
                capture_output=True,
                text=True
            )

            log.info(f"Database backup completed successfully. Details: {process.stderr}")

        except FileNotFoundError:
            log.error(
                "BACKUP ERROR: 'pg_dump' command not found. Please ensure postgresql-client is installed in the container.")
        except subprocess.CalledProcessError as e:
            log.error(f"BACKUP ERROR: pg_dump failed with exit code {e.returncode}.")
            log.error(f"Error message: {e.stderr}")
        except Exception as e:
            log.error(f"An unexpected error occurred during the backup process: {e}")
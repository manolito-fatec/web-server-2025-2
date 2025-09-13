"""
Created on 09/12/2025 09:01 

@author: otavio-calderan
"""

import logging

import psycopg2
from config import settings
from pymongo import MongoClient

log = logging.getLogger(__name__)


class PostgresConnector:
    """Manages the connection to PostgreSQL using a context manager"""

    def __init__(self):
        self.connection_params = {"dbname": settings.DB_NAME, "user": settings.DB_USER,
            "password": settings.DB_PASSWORD, "host": settings.DB_HOST, "port": settings.DB_PORT}
        self.connection = None

    def __enter__(self):
        self.connection = psycopg2.connect(**self.connection_params)
        return self.connection

    def __exit__(self, exc_type, exc_val, exc_tb):
        if self.connection:
            if exc_type:
                self.connection.rollback()
                log.error("An error occurred. Rollback executed in PostgreSQL.")
            else:
                self.connection.commit()
            self.connection.close()
            log.info("PostgreSQL connection closed.")


class MongoConnector:
    """Manages the connection to MongoDB using a context manager."""

    def __init__(self):
        self.client = None

    def __enter__(self):
        self.client = MongoClient(settings.MONGO_URI)
        return self.client

    def __exit__(self, exc_type, exc_val, exc_tb):
        if self.client:
            self.client.close()
            log.info("MongoDB connection closed.")

#!/bin/bash

set -e

BACKUP_DIR="/app/dump"

LATEST_BACKUP=$(ls -t ${BACKUP_DIR}/*.dump | head -n 1)

if [ -z "$LATEST_BACKUP" ]; then
    echo "Error: No backup file (*.dump) found in ${BACKUP_DIR}. Exiting."
    exit 1
fi

echo "Found latest backup file: ${LATEST_BACKUP}"
echo "Waiting for the database to be ready..."

echo "Starting database restore from ${LATEST_BACKUP}..."

export PGPASSWORD=$DB_PASSWORD

pg_restore \
    --host=db \
    --username=${DB_USER} \
    --dbname=${DB_NAME} \
    --no-owner \
    --clean \
    "${LATEST_BACKUP}"

unset PGPASSWORD

echo "Database restore completed successfully!"
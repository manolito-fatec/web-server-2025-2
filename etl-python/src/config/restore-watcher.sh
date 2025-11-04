#!/bin/bash
set -e

# --- Config ---
INBOX_DIR="/app/restore"
PROCESSED_DIR="/app/dump_processed"
SCRIPT_CLEANUP="python -u cleanup.py"
STATUS_DIR="/app/restore_status"
FLAG_FILE="${STATUS_DIR}/restore_complete.signal"

# --- Setup ---
mkdir -p "${INBOX_DIR}"
mkdir -p "${PROCESSED_DIR}"
mkdir -p "${STATUS_DIR}"

export PGPASSWORD=$DB_PASSWORD

process_file() {
    local FILE_PATH="$1"
    local FILENAME=$(basename "${FILE_PATH}")

    echo "-----------------------------------"
    echo "Restore process started for: ${FILENAME}"

    rm -f "${FLAG_FILE}"
    echo "'Completed' signal removed. Starting restore..."
    sleep 3

    echo "Starting pg_restore..."
    pg_restore \
        --host=db \
        --username=${DB_USER} \
        --dbname=${DB_NAME} \
        --no-owner \
        --clean \
        --if-exists \
        "${FILE_PATH}"
    echo "pg_restore completed."

    echo "Starting cleanup script (cleanup.py)..."
    if ! $SCRIPT_CLEANUP; then
        echo "CRITICAL ERROR: Cleanup failed after restore."
        mv "${FILE_PATH}" "${PROCESSED_DIR}/ERROR_${FILENAME}"
    else
        echo "Cleanup completed successfully."
        mv "${FILE_PATH}" "${PROCESSED_DIR}/${FILENAME}"

        touch "${FLAG_FILE}"
        echo "'Completed' signal created at ${FLAG_FILE}"
    fi
    echo "-----------------------------------"
}

echo "--- Restore Watcher Started ---"
echo "Processing pre-existing files in ${INBOX_DIR}..."
shopt -s nullglob
for file in "${INBOX_DIR}"/*.dump; do
    process_file "${file}"
done
shopt -u nullglob
echo "Initial processing complete."

echo "Now monitoring folder: ${INBOX_DIR}"
inotifywait -m -e moved_to -e create --include '.*\.dump$' "${INBOX_DIR}" |
while read -r directory event filename; do
    echo "New event '${event}' detected for file: ${filename}"
    process_file "${directory}${filename}"
done

unset PGPASSWORD
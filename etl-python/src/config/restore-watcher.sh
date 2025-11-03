#!/bin/bash
set -e

INBOX_DIR="/app/restore"
PROCESSED_DIR="/app/dump_processed"
SCRIPT_CLEANUP="python -u cleanup.py"
STATUS_DIR="/app/restore_status"
FLAG_FILE="${STATUS_DIR}/restore_complete.signal"

mkdir -p "${INBOX_DIR}"
mkdir -p "${PROCESSED_DIR}"
mkdir -p "${STATUS_DIR}"

export PGPASSWORD=$DB_PASSWORD

process_file() {
    local FILE_PATH="$1"
    local FILENAME=$(basename "${FILE_PATH}")

    echo "-----------------------------------"
    echo "Processo de restore iniciado para: ${FILENAME}"

    rm -f "${FLAG_FILE}"
    echo "Sinal de 'concluído' removido. Iniciando restore..."
    sleep 3

    echo "Iniciando pg_restore..."
    pg_restore \
        --host=db \
        --username=${DB_USER} \
        --dbname=${DB_NAME} \
        --no-owner \
        --clean \
        --if-exists \
        "${FILE_PATH}"
    echo "pg_restore concluído."

    echo "Iniciando script de limpeza (cleanup.py)..."
    if ! $SCRIPT_CLEANUP; then
        echo "ERRO CRÍTICO: Falha na limpeza após o restore."
        mv "${FILE_PATH}" "${PROCESSED_DIR}/ERROR_${FILENAME}"
    else
        echo "Limpeza concluída com sucesso."
        mv "${FILE_PATH}" "${PROCESSED_DIR}/${FILENAME}"

        touch "${FLAG_FILE}"
        echo "Sinal de 'concluído' criado em ${FLAG_FILE}"
    fi
    echo "-----------------------------------"
}

echo "--- Restore Watcher Iniciado ---"
shopt -s nullglob
for file in "${INBOX_DIR}"/*.dump; do
    process_file "${file}"
done
shopt -u nullglob

echo "Monitorando pasta: ${INBOX_DIR}"
inotifywait -m -e moved_to -e create --include '.*\.dump$' "${INBOX_DIR}" |
while read -r directory event filename; do
    process_file "${directory}${filename}"
done

unset PGPASSWORD
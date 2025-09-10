"""
Created on 09/10/2025 00:50 

@author: otavio-calderan
"""
import sys
import time
from config.setup_mongo import setup_audit_logs

from personal_data.run import run_anonymization_etl

if __name__ == "__main__":
    print("Iniciando container de ETL...")

    print("\nVerificando e configurando a infraestrutura do MongoDB...")
    try:
        setup_audit_logs()
        print("Infraestrutura do MongoDB configurada com sucesso.")
    except Exception as e:
        print(f"ERRO CRÍTICO: Falha ao configurar o MongoDB. O ETL não será executado. Erro: {e}")
        sys.exit(1)

    print("\nIniciando script de anonimização de dados legados (LGPD)...")
    print("AVISO: Este script fará alterações permanentes no banco de dados.")
    time.sleep(3)
    run_anonymization_etl()

    print("\nTodos os processos de ETL foram concluídos.")

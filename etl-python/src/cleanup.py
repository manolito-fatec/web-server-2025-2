"""
Created on 10/31/2025 19:41 

@author: otavio-calderan
"""

import sys
import logging
import time

from config.setup_mongo import setup_audit_logs, setup_product_insights, setup_tickets_forecaster
from config.setup_log import configurar_logger

from etl.pipeline import AnonymizationPipeline

log = logging.getLogger(__name__)

if __name__ == "__main__":

    configurar_logger()
    log.info("Iniciando processo de limpeza sob demanda (cleanup.py)...")

    try:
        setup_audit_logs()
        setup_product_insights()
        setup_tickets_forecaster()
        log.info("Configuração do MongoDB verificada.")
    except Exception as e:
        log.error(f"ERRO CRÍTICO: Falha ao configurar o MongoDB. A limpeza não será executada. Erro: {e}")
        sys.exit(1)

    log.warning("AVISO: Este script fará mudanças permanentes de limpeza no banco de dados.")
    time.sleep(3)

    try:
        log.info("Iniciando o AnonymizationPipeline...")
        pipeline = AnonymizationPipeline()
        pipeline.run()

        if pipeline.status == "SUCCESS":
            log.info("AnonymizationPipeline concluído com SUCESSO.")
        else:
            log.error(f"AnonymizationPipeline falhou. Erro: {pipeline.error_info}")
            sys.exit(1)

    except Exception as e:
        log.error(f"ERRO CRÍTICO durante a execução do pipeline de limpeza: {e}")
        sys.exit(1)

    log.info("Processo de limpeza (cleanup.py) finalizado com sucesso.")

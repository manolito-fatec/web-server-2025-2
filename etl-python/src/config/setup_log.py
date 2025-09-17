"""
Created on 09/12/2025 19:30 

@author: otavio-calderan
"""


import logging
import sys


def configurar_logger():
    """
    Set up the root logger for the application.
    """
    format = '%(asctime)s - %(name)s - %(levelname)s - %(message)s'

    log = logging.getLogger()

    log.setLevel(logging.DEBUG)

    if not log.handlers:
        handler_console = logging.StreamHandler(sys.stdout)
        handler_console.setLevel(logging.INFO)
        handler_console.setFormatter(logging.Formatter(format))

        handler_arquivo = logging.FileHandler('app.log')
        handler_arquivo.setLevel(logging.DEBUG)
        handler_arquivo.setFormatter(logging.Formatter(format))

        log.addHandler(handler_console)
        log.addHandler(handler_arquivo)

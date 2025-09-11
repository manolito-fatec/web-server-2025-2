"""
Created on 09/10/2025 19:16 

@author: otavio-calderan
"""
import os
from dotenv import load_dotenv
from pymongo import MongoClient
from pymongo.errors import OperationFailure

load_dotenv()

MONGO_URI = os.getenv("MONGO_URI")
MONGO_DATABASE = os.getenv("MONGO_DATABASE")
AUDIT_LOGS_COLLECTION = os.getenv("MONGO_COLLECTION", "auditLogs")

FIVE_YEARS_IN_SECONDS = 157680000


def setup_audit_logs():
    """
    Garante que a collection 'auditLogs' e seus índices no MongoDB existam conforme o design.
    """
    mongo_client = None
    try:
        print("Conectando ao MongoDB...")
        mongo_client = MongoClient(MONGO_URI)
        db = mongo_client[MONGO_DATABASE]
        print(f"Conectado com sucesso ao banco de dados '{MONGO_DATABASE}'.")

        print(f"\nConfigurando a collection '{AUDIT_LOGS_COLLECTION}'...")
        audit_logs = db[AUDIT_LOGS_COLLECTION]

        print("Criando índice composto principal (actor.type, actor.userId)...")
        audit_logs.create_index([("actor.type", 1), ("actor.userId", 1), ("timestamp", -1)])

        print("Criando índice para scripts (actor.scriptName)...")
        audit_logs.create_index([("actor.scriptName", 1), ("timestamp", -1)])

        print("Criando índice por serviço e ação...")
        audit_logs.create_index([("service", 1), ("action", 1), ("timestamp", -1)])

        print(f"Criando/verificando índice de retenção TTL ({FIVE_YEARS_IN_SECONDS} segundos)...")
        try:
            audit_logs.create_index([("timestamp", 1)], expireAfterSeconds=FIVE_YEARS_IN_SECONDS)
        except OperationFailure as e:
            if "expireAfterSeconds differs" in str(e):
                print("Índice TTL já existe com um valor diferente. Recriando...")
                audit_logs.drop_index("timestamp_1")
                audit_logs.create_index([("timestamp", 1)], expireAfterSeconds=FIVE_YEARS_IN_SECONDS)
                print("Índice TTL recriado com sucesso.")
            else:
                raise e

        print(f"Collection '{AUDIT_LOGS_COLLECTION}' configurada com sucesso.")

    except Exception as e:
        print(f"\nERRO: Falha ao configurar a infraestrutura do MongoDB. {e}")
    finally:
        if mongo_client:
            mongo_client.close()
            print("\nConexão com o MongoDB fechada.")


if __name__ == "__main__":
    print("Iniciando script de setup da collection de auditoria do MongoDB...")
    setup_audit_logs()
    print("\nSetup concluído.")

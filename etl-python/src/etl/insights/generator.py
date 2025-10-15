"""
Created on 10/13/2025 19:24 

@author: otavio-calderan
"""

import google.generativeai as genai
import json
import logging
import os
import pandas as pd
from datetime import datetime, timezone
from zoneinfo import ZoneInfo

log = logging.getLogger(__name__)


class InsightsGenerator:
    """
    Responsible for reading the extracted data, analyzing it with Pandas,
    interacting with the Gemini API to generate insights, and returning
    a list of JSON documents ready to be loaded.
    """

    def __init__(self, api_key, etl_config):
        self.etl_config = etl_config
        genai.configure(api_key=api_key)
        self.model = genai.GenerativeModel(self.etl_config['gemini_model'])
        self.sao_paulo_tz = ZoneInfo("America/Sao_Paulo")
        self.files_processed_count = 0

    def _prepare_context(self, df_produto):
        """Prepares the raw text from the tickets for the LLM."""
        contexto_textual = ""
        for _, row in df_produto.iterrows():
            contexto_textual += f"--- Subcategoria: {row['subcategory_name']} ---\n"
            contexto_textual += f"Título: {row['title']}\n"
            contexto_textual += f"Descrição: {row['description']}\n"
        return contexto_textual

    def _create_prompt(self, contexto, company, product, stats):
        """Creates a prompt with statistics for the API."""
        return f"""
        Você é um Product Manager, com foco em análise de dados para tomada de decisão.
    
        **Contexto:**
        Você está analisando os chamados de suporte para a empresa "{company}", focando no produto "{product}".
        Uma análise quantitativa prévia dos chamados revelou os seguintes temas (subcategorias) como os mais problemáticos:
    
        **Estatísticas dos Principais Problemas:**
        {stats}
    
        **Sua Tarefa:**
        Com base nas estatísticas acima e nos detalhes dos chamados fornecidos abaixo, sua missão é gerar um plano de ação conciso. Para cada um dos temas principais:
        1.  Sintetize o problema central.
        2.  Proponha uma lista de ações claras, específicas e implementáveis para resolver a causa raiz desses problemas.
    
        **Formato da Resposta:**
        Sua resposta DEVE ser um objeto JSON válido, sem nenhum texto ou formatação adicional. A estrutura deve ser a seguinte:
        {{
          "company_name": "{company}",
          "product_name": "{product}",
          "insights": [
            {{
              "theme": "Nome do Tema Principal 1 (Subcategoria)",
              "percentage": "XX%",
              "actions": [
                "Ação sugerida 1 para o Tema 1.",
                "Ação sugerida 2 para o Tema 1."
              ]
            }},
            {{
              "theme": "Nome do Tema Principal 2 (Subcategoria)",
              "percentage": "YY%",
              "actions": [
                "Ação sugerida 1 para o Tema 2."
              ]
            }}
          ]
        }}
    
        **Dados Brutos dos Chamados para Análise Qualitativa:**
        {contexto}
        """

    def execute(self):
        """
        Executes the insight generation process for all CSV files
        found in the input directory.
        """
        input_dir = self.etl_config['input_dir']
        csv_files = [f for f in os.listdir(input_dir) if f.endswith('.csv')]

        self.files_processed_count = len(csv_files)

        if not csv_files:
            return []

        all_insights = []
        for i, csv_file in enumerate(csv_files):
            caminho_completo = os.path.join(input_dir, csv_file)
            log.info(f"Processing file [{i + 1}/{len(csv_files)}]: {csv_file}")

            try:
                df_cliente = pd.read_csv(caminho_completo)
                if df_cliente.empty:
                    log.warning(f"File {csv_file} is empty. Skipping.")
                    continue

                company_name = df_cliente.iloc[0]['company_name']

                for product_name in df_cliente['product_name'].unique():
                    log.info(
                        f"Analyzing product: '{product_name}' for company '{company_name}'...")
                    start_time_utc = datetime.now(timezone.utc)

                    df_produto = df_cliente[df_cliente['product_name'] == product_name]

                    dist = df_produto['subcategory_name'].value_counts(normalize=True)
                    top_themes = dist.head(self.etl_config['top_n_subcategories'])
                    stats_str = "".join([f"- {theme}: {perc:.0%}\n" for theme, perc in top_themes.items()])

                    contexto = self._prepare_context(df_produto)
                    prompt = self._create_prompt(contexto, company_name, product_name, stats_str)

                    log.info("Sending prompt to Gemini API for product")
                    response = self.model.generate_content(prompt)

                    json_str = response.text.strip()[response.text.find('{'): response.text.rfind('}') + 1]
                    parsed_json = json.loads(json_str)

                    log.info(f"API response received for '{product_name}':\n"
                             f"{json.dumps(parsed_json, indent=2, ensure_ascii=False)}")

                    end_time_utc = datetime.now(timezone.utc)
                    parsed_json['starttime'] = start_time_utc.astimezone(self.sao_paulo_tz).isoformat()
                    parsed_json['endtime'] = end_time_utc.astimezone(self.sao_paulo_tz).isoformat()
                    parsed_json['dth'] = end_time_utc.astimezone(self.sao_paulo_tz).isoformat()

                    all_insights.append(parsed_json)

            except Exception as e:
                log.error(f"Critical error processing file {csv_file} or in API call: {e}")

        return all_insights

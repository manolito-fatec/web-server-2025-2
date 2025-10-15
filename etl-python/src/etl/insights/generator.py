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

    def _prepare_context(self, df_product):
        """Prepares the raw text from the tickets for the LLM."""
        textual_context = ""
        for _, row in df_product.iterrows():
            textual_context += f"--- Subcategoria: {row['subcategory_name']} ---\n"
            textual_context += f"Título: {row['title']}\n"
            textual_context += f"Descrição: {row['description']}\n"
        return textual_context

    def _create_prompt(self, context, company_id, company, product_id, product, stats):
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
          "company_id": {company_id},
          "company_name": "{company}",
          "product_id": {product_id},
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
        {context}
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
            completed_path = os.path.join(input_dir, csv_file)
            log.info(f"Processing file [{i + 1}/{len(csv_files)}]: {csv_file}")

            try:
                df_client = pd.read_csv(completed_path)
                if df_client.empty:
                    log.warning(f"File {csv_file} is empty. Skipping.")
                    continue

                grouped = df_client.groupby(['company_id', 'product_id'])

                for (company_id, product_id), df_product in grouped:
                    company_name = df_product.iloc[0]['company_name']
                    product_name = df_product.iloc[0]['product_name']

                    log.info(
                        f"Analyzing product: '{product_name}' (ID: {product_id}) for company '{company_name}' (ID: {company_id})...")
                    start_time_utc = datetime.now(timezone.utc)

                    dist = df_product['subcategory_name'].value_counts(normalize=True)
                    top_themes = dist.head(self.etl_config['top_n_subcategories'])
                    stats_str = "".join([f"- {theme}: {perc:.0%}\n" for theme, perc in top_themes.items()])

                    context = self._prepare_context(df_product)
                    prompt = self._create_prompt(
                        context, company_id, company_name, product_id, product_name, stats_str
                    )

                    log.info(f"Sending prompt to Gemini API for product '{product_name}'...")
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

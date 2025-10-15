"""
Created on 10/13/2025 19:24 

@author: otavio-calderan
"""

import duckdb
import logging
import os
import pandas as pd

from config import settings

log = logging.getLogger(__name__)


class InsightsExtractor:
    """
    Responsible for extracting ticket data from Postgres and saving it
    into intermediate CSV files for analysis.
    """

    def __init__(self, etl_config):
        self.etl_config = etl_config
        self.conn_string = (f"dbname={settings.DB_NAME} user={settings.DB_USER} "
                            f"password={settings.DB_PASSWORD} host={settings.DB_HOST} "
                            f"port={settings.DB_PORT}")

    def _build_query(self):
        """Builds the SQL query for data extraction."""
        return f"""
        WITH SubcategoryRanks AS (
            SELECT
                co.name AS company_name, p.name AS product_name, sc.name AS subcategory_name,
                ROW_NUMBER() OVER(PARTITION BY co.name, p.name ORDER BY COUNT(t.ticket_id) DESC) as rank_num
            FROM postgres_db.public.tickets AS t
            JOIN postgres_db.public.companies AS co ON t.company_id = co.company_id
            JOIN postgres_db.public.products AS p ON t.product_id = p.product_id
            JOIN postgres_db.public.subcategories AS sc ON t.subcategory_id = sc.subcategory_id
            JOIN postgres_db.public.statuses AS st ON t.current_status_id = st.status_id
            WHERE st.name = '{self.etl_config['open_status']}'
            GROUP BY company_name, product_name, subcategory_name
        ),
        CompanyQuota AS (
            SELECT company_name, FLOOR({self.etl_config['max_tickets_per_company']} / (COUNT(DISTINCT product_name) * {self.etl_config['top_n_subcategories']})) AS tickets_per_slot_quota
            FROM SubcategoryRanks
            WHERE rank_num <= {self.etl_config['top_n_subcategories']}
            GROUP BY company_name
        ),
        RankedTickets AS (
            SELECT
                co.company_id, p.product_id,
                co.name AS company_name, p.name AS product_name, sc.name AS subcategory_name, t.title, t.description, st.name AS status_name, t.created_at,
                cq.tickets_per_slot_quota,
                ROW_NUMBER() OVER(PARTITION BY co.name, p.name, sc.name ORDER BY t.created_at DESC) as ticket_rank
            FROM postgres_db.public.tickets AS t
            JOIN postgres_db.public.companies AS co ON t.company_id = co.company_id
            JOIN postgres_db.public.products AS p ON t.product_id = p.product_id
            JOIN postgres_db.public.subcategories AS sc ON t.subcategory_id = sc.subcategory_id
            JOIN postgres_db.public.statuses AS st ON t.current_status_id = st.status_id
            JOIN SubcategoryRanks sr ON co.name = sr.company_name AND p.name = sr.product_name AND sc.name = sr.subcategory_name
            JOIN CompanyQuota cq ON co.name = cq.company_name
            WHERE sr.rank_num <= {self.etl_config['top_n_subcategories']} AND st.name = '{self.etl_config['open_status']}'
        )
        SELECT company_id, product_id, company_name, product_name, subcategory_name, title, description, status_name
        FROM RankedTickets
        WHERE ticket_rank <= tickets_per_slot_quota;
        """

    def execute(self):
        """Executes the complete extraction process."""
        try:
            con = duckdb.connect(database=':memory:')
            con.execute("INSTALL postgres;")
            con.execute("LOAD postgres;")
            con.execute(f"ATTACH '{self.conn_string}' AS postgres_db (TYPE POSTGRES);")
        except Exception as e:
            log.error(f"Fatal error connecting to the database: {e}")
            return False

        query = self._build_query()

        try:
            df_tickets = con.execute(query).fetchdf()

            if df_tickets.empty:
                log.warning("No tickets found with the specified criteria.")
                return True

            output_dir = self.etl_config['input_dir']
            os.makedirs(output_dir, exist_ok=True)
            for comp in df_tickets['company_name'].unique():
                df_company = df_tickets[df_tickets['company_name'] == comp]
                safe_filename = str(comp).lower().replace(' ', '_').replace('/', '_') + ".csv"
                output_path = os.path.join(output_dir, safe_filename)
                df_company.to_csv(output_path, index=False, encoding='utf-8-sig')
            return True
        except Exception as e:
            log.error(f"Error during data extraction or saving: {e}")
            return False
        finally:
            con.close()

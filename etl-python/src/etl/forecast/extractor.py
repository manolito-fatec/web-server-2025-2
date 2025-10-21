
import duckdb
import logging
import pandas as pd
import pickle
from config import settings 
from datetime import date
from pandas.tseries.offsets import MonthEnd

log = logging.getLogger(__name__)

class TicketForecasterExtractor:
    """
    Responsible for extracting historical ticket data per month for each product-company pair
    and applying a pre-trained Prophet model to generate forecasts.
    """

    def __init__(self, forecast_config):
        self.forecast_config = forecast_config
        self.conn_string = (f"dbname={settings.DB_NAME} user={settings.DB_USER} "
                            f"password={settings.DB_PASSWORD} host={settings.DB_HOST} "
                            f"port={settings.DB_PORT}")
        self.json_list = []

    def _connect_to_db(self):
        """Establishes the connection to DuckDB and attaches the Postgres database."""
        try:
            con = duckdb.connect(database=':memory:')
            con.execute("INSTALL postgres;")
            con.execute("LOAD postgres;")
            con.execute(f"ATTACH '{self.conn_string}' AS postgres_db (TYPE POSTGRES);")
            return con
        except Exception as e:
            log.error(f"Fatal error connecting to or attaching the database: {e}")
            raise 

    def _get_product_company_pairs(self, con):
        """
        Queries the database to get all unique pairs of 
        product_id, company_id, product_name, and company_name.
        """
        log.info("Extracting unique product-company pairs...")
        query_pairs = """
        SELECT DISTINCT
            p.product_id,
            p.name AS product_name,
            c.company_id,
            c.name AS company_name
        FROM postgres_db.tickets t
        INNER JOIN postgres_db.products p ON t.product_id = p.product_id
        INNER JOIN postgres_db.companies c ON t.company_id = c.company_id
        ORDER BY p.product_id;
         """
        return con.execute(query_pairs).fetchdf()

    def _get_historical_data(self, con, product_id, company_id):
        """
        Queries the historical ticket data, grouped by month,
        for a specific product_id and company_id.
        """
        query_history = """
            SELECT
            DATE_TRUNC('month', t.created_at) AS month_start_date,
            COUNT(t.ticket_id) AS total_tickets
            FROM
                postgres_db.public.tickets t
            WHERE
                t.product_id = ? AND t.company_id = ?
            GROUP BY
                month_start_date
            ORDER BY
                month_start_date ASC;
        """

        df_history = con.execute(query_history, (product_id, company_id)).fetchdf()

        if df_history.empty:
            log.warning(f"No historical data found for Product ID: {product_id}, Company ID: {company_id}.")
            return None

        df_history = df_history.rename(columns={'month_start_date': 'ds', 'total_tickets': 'y'})
 
        df_history['ds'] = pd.to_datetime(df_history['ds']).dt.to_period('M').dt.to_timestamp(how='end')
        return df_history

    def _load_model(self):
        """Loads the pre-trained Prophet model from the pickle file."""
        path = self.forecast_config.get('model_path')
        log.info(f"Loading Prophet model from: {path}")
        try:
            with open(path, 'rb') as file:
                return pickle.load(file)
        except FileNotFoundError:
            log.error(f"Model file not found at: {path}")
            raise

    def _generate_forecast(self, modelo, df_history, product_id, product_name, company_id, company_name):
        """Generates the forecast and applies the mean adjustment."""
        periods = self.forecast_config.get('forecast_periods')
        log.info(f"Generating forecast for the next {periods} months...")
        max_date = df_history['ds'].max()
        future_dates = pd.date_range(start=max_date + MonthEnd(1), end=max_date + MonthEnd(periods), freq='ME')
        future = pd.DataFrame({'ds': future_dates})
        forecast = modelo.predict(future)

        yhat_mean = forecast['yhat'].mean()
        y_mean = df_history['y'].mean()

        p = y_mean / yhat_mean if yhat_mean != 0 and y_mean is not None else 1
 
        forecast['adjusted_yhat'] = round(forecast['yhat'] * p).astype(int)
        log.info(f"Mean adjustment applied: y_mean={y_mean:.2f}, yhat_mean={yhat_mean:.2f}, Factor (p)={p:.4f}")


        for index, row in forecast.iterrows():
            forecast_data = {
                "productId": product_id,
                "productName": product_name,
                "companyId": company_id,
                "companyName": company_name,
                "futureDate": row['ds'].isoformat(),
                "totalTickets": row['adjusted_yhat'],
                "dth": date.today().isoformat()
            }
            self.json_list.append(forecast_data)

    def execute(self):
        """Executes the complete ticket forecasting process."""
        con = None
        try:
            con = self._connect_to_db()
            df_pairs = self._get_product_company_pairs(con)

            if df_pairs.empty:
                log.warning("No product-company pairs found in the tickets data.")
                return []

            modelo = self._load_model()

            log.info(f"Starting forecast for {len(df_pairs)} product-company pairs.")

            for index, row in df_pairs.iterrows():
                product_id = row['product_id']
                company_id = row['company_id']
                product_name = row['product_name']
                company_name = row['company_name']

                df_history = self._get_historical_data(con, product_id, company_id)

                if df_history is not None:
                    try:
                        log.info(f"Processing: {product_name} ({product_id}) for {company_name} ({company_id})")
                        self._generate_forecast(modelo, df_history, product_id, product_name, company_id, company_name)
                    except Exception as e:
                        log.error(f"Error generating forecast for {product_name}/{company_name}: {e}")

            log.info(f"Forecast complete. Total of {len(self.json_list)} records generated.")
            return self.json_list

        except Exception as e:
            log.error(f"General error in forecaster execution: {e}")
            return []
        finally:
            if con:
                con.close()
                log.info("Database connection closed.")
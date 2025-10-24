import duckdb
import logging
import pandas as pd
import numpy as np
import onnxruntime as ort
from config import settings
from datetime import date

log = logging.getLogger(__name__)

FEATURE_COLS = [
    'resolution_mins',       # Numérica
    'created_hour',          # Numérica
    'created_day_of_week',   # Numérica
    'is_weekend',            # Categórica
    'status_change_count',   # Numérica
    'subcategory_name',    # Categórica
    'product_name',        # Categórica
    'priority_name',       # Categórica
    'department_name',     # Categórica
    'company_name',        # Categórica
    'current_status_name'  # Categórica
]

NUMERICAL_FEATURES_ONNX = ['resolution_mins', 'created_hour', 'created_day_of_week', 'status_change_count']
CATEGORICAL_FEATURES_ONNX = ['is_weekend', 'subcategory_name', 'product_name', 'priority_name', 'department_name', 'company_name', 'current_status_name']


class SlaPredictionExtractor:
    """
    Extracts ticket data, loads the ONNX model, generates SLA predictions,
    and formats the results. The ONNX model expects multiple named inputs.
    """

    def __init__(self, etl_config):
        self.etl_config = etl_config
        self.conn_string = (f"dbname={settings.DB_NAME} user={settings.DB_USER} "
                            f"password={settings.DB_PASSWORD} host={settings.DB_HOST} "
                            f"port={settings.DB_PORT}")
        self.json_list = []
        self.onnx_input_names = []

    def _connect_to_db(self):
        try:
            con = duckdb.connect(database=':memory:')
            con.execute("INSTALL postgres;")
            con.execute("LOAD postgres;")
            con.execute(f"ATTACH '{self.conn_string}' AS postgres_db (TYPE POSTGRES);")
            return con
        except Exception as e:
            log.error(f"Fatal error connecting or attaching the database: {e}")
            raise

    def _load_model(self):
        """Loads the ONNX model and gets input/output names."""
        path = self.etl_config.get('model_path')
        log.info(f"Loading ONNX model from: {path}")
        try:
            session = ort.InferenceSession(path)
            self.onnx_input_names = [inp.name for inp in session.get_inputs()]
            log.info(f"ONNX Model Input Names: {self.onnx_input_names}")
            
            output_names = [output.name for output in session.get_outputs()]
            log.info(f"ONNX Model Output Names: {output_names}")
            
            if set(self.onnx_input_names) != set(FEATURE_COLS):
                 log.warning("WARNING: Input names in ONNX do not exactly match FEATURE_COLS!")
                 log.warning(f"ONNX expects: {self.onnx_input_names}")
                 log.warning(f"FEATURE_COLS: {FEATURE_COLS}")
            
            return session, output_names
        except FileNotFoundError:
            log.error(f"Model file not found at: {path}")
            raise
        except Exception as e:
            log.error(f"Error loading ONNX model: {e}")
            raise

    def _get_prediction_data(self, con):
        """Fetches the necessary data for prediction from the database."""
        log.info("Extracting data for SLA prediction...")

        query_data = f"""
        WITH StatusHistoryFeatures AS (
            SELECT
                tsh.ticket_id,
                COUNT(tsh.history_id) - 1 AS status_change_count 
            FROM
                postgres_db.public.ticket_status_history AS tsh
            GROUP BY
                tsh.ticket_id
        )
        SELECT
            t.ticket_id,
            t.company_id,
            t.subcategory_id,
            co.name AS company_name, 
            p.name AS product_name, 
            
            -- Features Numericas
            COALESCE(sp.resolution_mins, 0)::FLOAT AS resolution_mins,
            EXTRACT(HOUR FROM t.created_at)::FLOAT AS created_hour,
            EXTRACT(DOW FROM t.created_at)::FLOAT AS created_day_of_week, -- 0=Domingo, 6=Sábado
            COALESCE(shf.status_change_count, 0)::FLOAT AS status_change_count,

            -- Features Categóricas
            CASE WHEN EXTRACT(DOW FROM t.created_at) IN (0, 6) THEN '1' ELSE '0' END AS is_weekend,
            COALESCE(s.name, 'Desconhecida') AS subcategory_name,
            COALESCE(p.name, 'Desconhecido') AS product_name,
            COALESCE(pr.name, 'Desconhecida') AS priority_name,
            COALESCE(d.name, 'Não Atribuído') AS department_name,
            COALESCE(co.name, 'Desconhecida') AS company_name,
            COALESCE(st_curr.name, 'Desconhecido') AS current_status_name

        FROM postgres_db.public.tickets AS t
        JOIN postgres_db.public.statuses AS st_curr ON t.current_status_id = st_curr.status_id
        LEFT JOIN postgres_db.public.sla_plans AS sp ON t.sla_plan_id = sp.sla_plan_id
        LEFT JOIN postgres_db.public.subcategories AS s ON t.subcategory_id = s.subcategory_id
        LEFT JOIN postgres_db.public.products AS p ON t.product_id = p.product_id
        LEFT JOIN postgres_db.public.priorities AS pr ON t.priority_id = pr.priority_id
        LEFT JOIN postgres_db.public.companies AS co ON t.company_id = co.company_id
        LEFT JOIN postgres_db.public.agents AS ag ON t.assigned_agent_id = ag.agent_id
        LEFT JOIN postgres_db.public.departments AS d ON ag.department_id = d.department_id
        LEFT JOIN StatusHistoryFeatures AS shf ON t.ticket_id = shf.ticket_id

        WHERE
            st_curr.name = 'Aberto'

        ORDER BY
            t.created_at ASC; 
        """
        try:
            df_data = con.execute(query_data).fetchdf()
            log.info(f"Found {len(df_data)} open tickets for prediction.")
            
            for col in FEATURE_COLS:
                 if col not in df_data.columns:
                      log.error(f"Expected column '{col}' not found in query results!")
                      raise ValueError(f"Missing column: '{col}'")

            return df_data
        except Exception as e:
            log.error(f"Error executing prediction data query: {e}")
            raise

    def _prepare_onnx_input_dict(self, df_subset):
        """Prepares the input dictionary in the format expected by ONNX Runtime."""
        input_dict = {}
        
        for col in NUMERICAL_FEATURES_ONNX:
            if col in df_subset:
                 input_dict[col] = df_subset[col].values.astype(np.float32).reshape(-1, 1)
            else:
                 raise ValueError(f"Numerical column '{col}' is missing from DataFrame for ONNX input preparation.")

        for col in CATEGORICAL_FEATURES_ONNX:
             if col in df_subset:
                  input_dict[col] = df_subset[col].values.astype(object).reshape(-1, 1)
             else:
                  raise ValueError(f"Categorical column '{col}' is missing from DataFrame for ONNX input preparation.")
        
        ordered_input_dict = {name: input_dict[name] for name in self.onnx_input_names if name in input_dict}
        
        if len(ordered_input_dict) != len(self.onnx_input_names):
             missing_keys = set(self.onnx_input_names) - set(ordered_input_dict.keys())
             raise ValueError(f"Missing ONNX inputs in prepared dictionary: {missing_keys}")
             
        return ordered_input_dict


    def _run_predictions(self, session, output_names, df_data):
        """Generates predictions using the ONNX model and formats the output."""
        
        if df_data.empty:
            log.warning("No data to predict, skipping inference.")
            return

        try:
            onnx_input_dict = self._prepare_onnx_input_dict(df_data) 
            log.info(f"Running inference on {len(df_data)} records...")

            result = session.run(output_names, onnx_input_dict) 

            if isinstance(result[1], list) and len(result[1]) > 0 and isinstance(result[1][0], dict):
                 predictions_proba_breach = np.array([d.get(1, 0.0) for d in result[1]])
            elif isinstance(result[1], np.ndarray) and result[1].shape[1] == 2:
                 predictions_proba_breach = result[1][:, 1]
            else:
                 log.error(f"Unexpected format for output_probability: {type(result[1])}")
                 predictions_proba_breach = np.zeros(len(df_data))

            df_data['sla_breach_probability'] = predictions_proba_breach
            
            log.info("Inference complete. Formatting results...")

            today = date.today().isoformat()
            cols_to_keep = [
                'ticket_id',
                'company_id',
                'company_name',
                'product_id',
                'product_name',
                'subcategory_id',
                'subcategory_name',
                'sla_breach_probability'
            ]
            if 'product_id' not in df_data.columns:
                cols_to_keep.remove('product_id')

            final_cols = [col for col in cols_to_keep if col in df_data.columns]
            if len(final_cols) != len(cols_to_keep):
                missing_output_cols = set(cols_to_keep) - set(final_cols)
                log.warning(f"Columns intended for output JSON are missing from DataFrame: {missing_output_cols}")

            df_output = df_data[cols_to_keep].copy()
            df_output['dth'] = today
            
            self.json_list = df_output.to_dict(orient='records')
                
        except KeyError as e:
            log.error(f"FATAL: Key error preparing ONNX input or processing result. Column: {e}")
            log.error("Check if FEATURE_COLS and SQL query are correct and if ONNX output names are as expected.")
            raise
        except Exception as e:
            log.error(f"Error during ONNX model inference: {e}", exc_info=True)
            raise

    def execute(self):
        """Executes the complete SLA prediction process."""
        con = None
        try:
            con = self._connect_to_db()
            session, output_names = self._load_model()
            df_data = self._get_prediction_data(con)

            if df_data.empty:
                log.warning("No open tickets found. Pipeline finished.")
                return []

            self._run_predictions(session, output_names, df_data)

            log.info(f"SLA Prediction complete. Total of {len(self.json_list)} records generated.")
            return self.json_list

        except Exception as e:
            log.error(f"General error in SlaPredictionExtractor execution: {e}", exc_info=True)
            return []
        finally:
            if con:
                con.close()
                log.info("Database connection closed.")
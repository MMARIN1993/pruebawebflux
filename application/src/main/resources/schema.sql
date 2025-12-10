-- DDL para la tabla poliza
CREATE TABLE IF NOT EXISTS poliza (
  uuid VARCHAR(100) PRIMARY KEY,
  policy_id VARCHAR(100),
  tipo VARCHAR(10) NOT NULL,
  fecha_inicio DATE NOT NULL,
  valor DECIMAL(19,2) NOT NULL
);


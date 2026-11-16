-- =============================================================================
-- Esquema de la base de datos api_paises (MySQL 8.0.19 o superior).
-- Diferencias con el script del enunciado:
--   * codigo (ISO alfa-3, p. ej. ECU) es UNIQUE: reimportar actualiza en vez de duplicar.
--   * Campos obligatorios NOT NULL, poblacion no negativa y region restringida.
--   * Indices para la busqueda por region y por nombre.
--   * utf8mb4 para nombres con tildes y caracteres especiales.
-- =============================================================================
CREATE DATABASE IF NOT EXISTS api_paises
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE api_paises;

CREATE TABLE IF NOT EXISTS pais (
    id        INT          NOT NULL AUTO_INCREMENT,
    codigo    CHAR(3)      NOT NULL,
    nombre    VARCHAR(100) NOT NULL,
    capital   VARCHAR(100) NULL,
    region    VARCHAR(20)  NOT NULL,
    poblacion BIGINT       NOT NULL DEFAULT 0,
    moneda    VARCHAR(100) NULL,
    CONSTRAINT pk_pais PRIMARY KEY (id),
    CONSTRAINT uq_pais_codigo UNIQUE (codigo),
    CONSTRAINT ck_pais_poblacion CHECK (poblacion >= 0),
    CONSTRAINT ck_pais_region CHECK (region IN ('AFRICA', 'AMERICAS', 'ANTARTIDA', 'ASIA', 'EUROPA', 'OCEANIA')),
    INDEX idx_pais_region (region),
    INDEX idx_pais_nombre (nombre)
) ENGINE = InnoDB;

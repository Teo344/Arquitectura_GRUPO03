-- =============================================================================
-- Esquema de la base de datos api_paises para SQL Server 2019+ (incluye Express).
-- Equivalente al script de MySQL (bdd/mysql/01_esquema_api_paises.sql):
--   * codigo (ISO alfa-3, p. ej. ECU) es UNIQUE: reimportar actualiza en vez de duplicar.
--   * Campos obligatorios NOT NULL, poblacion no negativa y region restringida.
--   * Indices para la busqueda por region y por nombre.
--   * NVARCHAR para nombres con caracteres especiales (p. ej. "Åland Islands").
--   * Intercalacion CI_AI: las busquedas ignoran mayusculas y tildes.
-- Se puede ejecutar varias veces: solo crea lo que falta.
-- La aplicacion se conecta con autenticacion de Windows (Integrated Security).
--
-- Ejecutar:  sqlcmd -S .\SQLEXPRESS -E -f 65001 -i 01_esquema_api_paises.sql
-- =============================================================================
IF DB_ID(N'api_paises') IS NULL
    CREATE DATABASE api_paises COLLATE Latin1_General_100_CI_AI;
GO

USE api_paises;
GO

IF OBJECT_ID(N'dbo.pais', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.pais (
        id        INT IDENTITY(1, 1) NOT NULL,
        codigo    CHAR(3)       NOT NULL,
        nombre    NVARCHAR(100) NOT NULL,
        capital   NVARCHAR(100) NULL,
        region    VARCHAR(20)   NOT NULL,
        poblacion BIGINT        NOT NULL CONSTRAINT df_pais_poblacion DEFAULT 0,
        moneda    NVARCHAR(100) NULL,
        CONSTRAINT pk_pais PRIMARY KEY (id),
        CONSTRAINT uq_pais_codigo UNIQUE (codigo),
        CONSTRAINT ck_pais_poblacion CHECK (poblacion >= 0),
        CONSTRAINT ck_pais_region CHECK (region IN ('AFRICA', 'AMERICAS', 'ANTARTIDA', 'ASIA', 'EUROPA', 'OCEANIA'))
    );

    CREATE INDEX idx_pais_region ON dbo.pais (region);
    CREATE INDEX idx_pais_nombre ON dbo.pais (nombre);
END
GO

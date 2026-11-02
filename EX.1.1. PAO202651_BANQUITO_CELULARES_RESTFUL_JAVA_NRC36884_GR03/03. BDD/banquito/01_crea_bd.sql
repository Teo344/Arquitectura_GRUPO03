-- =============================================================
-- BanQuito - Grupo 03 (NRC 36884)
-- Script 01: creación de la base de datos del Core bancario
--            y del módulo de crédito
-- Motor: MySQL 8.4 o superior (InnoDB, utf8mb4)
--
-- CLIENTE, CUENTA y MOVIMIENTO son las tablas del modelo Core del
-- enunciado, con sus mismos nombres y tipos; se agregan las
-- restricciones que el modelo no muestra (únicos, CHECK, FK).
-- CREDITO y CUOTA son el módulo de crédito: el crédito otorgado y
-- su tabla de amortización, en el mismo esquema del Core.
-- =============================================================

DROP DATABASE IF EXISTS banquito;

CREATE DATABASE banquito
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE banquito;

-- -------------------------------------------------------------
-- Core bancario (modelo del enunciado)
-- -------------------------------------------------------------

CREATE TABLE CLIENTE (
    COD_CLIENTE        INT            NOT NULL AUTO_INCREMENT,
    CEDULA             VARCHAR(100)   NOT NULL,
    NOMBRE             VARCHAR(100)   NOT NULL,
    GENERO             VARCHAR(1)     NOT NULL,
    FECHA_NACIMIENTO   DATE           NOT NULL,
    CONSTRAINT PK_CLIENTE PRIMARY KEY (COD_CLIENTE),
    CONSTRAINT UQ_CLIENTE_CEDULA UNIQUE (CEDULA),
    CONSTRAINT CHK_CLIENTE_GENERO CHECK (GENERO IN ('M', 'F'))
) ENGINE = InnoDB;

CREATE TABLE CUENTA (
    NUM_CUENTA         VARCHAR(8)     NOT NULL,
    COD_CLIENTE        INT            NOT NULL,
    SALDO              NUMERIC(10,2)  NOT NULL DEFAULT 0,
    CONSTRAINT PK_CUENTA PRIMARY KEY (NUM_CUENTA),
    CONSTRAINT CHK_CUENTA_SALDO CHECK (SALDO >= 0),
    CONSTRAINT FK_CUENTA_CLIENTE FOREIGN KEY (COD_CLIENTE)
        REFERENCES CLIENTE (COD_CLIENTE) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB;

-- TIPO: DEP (depósito) o RET (retiro). El índice por cuenta y fecha
-- sirve a las reglas de "último mes" y "últimos 3 meses".
CREATE TABLE MOVIMIENTO (
    COD_MOVIMIENTO     INT            NOT NULL AUTO_INCREMENT,
    NUM_CUENTA         VARCHAR(8)     NOT NULL,
    TIPO               VARCHAR(3)     NOT NULL,
    VALOR              NUMERIC(10,2)  NOT NULL,
    FECHA              DATE           NOT NULL,
    CONSTRAINT PK_MOVIMIENTO PRIMARY KEY (COD_MOVIMIENTO),
    CONSTRAINT CHK_MOVIMIENTO_TIPO CHECK (TIPO IN ('DEP', 'RET')),
    CONSTRAINT CHK_MOVIMIENTO_VALOR CHECK (VALOR > 0),
    CONSTRAINT FK_MOVIMIENTO_CUENTA FOREIGN KEY (NUM_CUENTA)
        REFERENCES CUENTA (NUM_CUENTA) ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX IX_MOVIMIENTO_CUENTA_FECHA (NUM_CUENTA, FECHA)
) ENGINE = InnoDB;

-- -------------------------------------------------------------
-- Módulo de crédito
-- -------------------------------------------------------------

-- Crédito de cuota fija, pago mensual, plazo de 3 a 18 meses y tasa
-- anual del 16.5 %. Un crédito ACTIVO impide otorgar otro (regla 4).
CREATE TABLE CREDITO (
    COD_CREDITO        INT            NOT NULL AUTO_INCREMENT,
    COD_CLIENTE        INT            NOT NULL,
    FECHA              DATE           NOT NULL,
    MONTO              NUMERIC(10,2)  NOT NULL,
    NUM_CUOTAS         INT            NOT NULL,
    TASA_ANUAL         NUMERIC(5,2)   NOT NULL DEFAULT 16.50,
    VALOR_CUOTA        NUMERIC(10,2)  NOT NULL,
    ESTADO             VARCHAR(10)    NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT PK_CREDITO PRIMARY KEY (COD_CREDITO),
    CONSTRAINT CHK_CREDITO_MONTO CHECK (MONTO > 0),
    CONSTRAINT CHK_CREDITO_CUOTAS CHECK (NUM_CUOTAS BETWEEN 3 AND 18),
    CONSTRAINT CHK_CREDITO_TASA CHECK (TASA_ANUAL > 0),
    CONSTRAINT CHK_CREDITO_VALOR_CUOTA CHECK (VALOR_CUOTA > 0),
    CONSTRAINT CHK_CREDITO_ESTADO CHECK (ESTADO IN ('ACTIVO', 'CANCELADO')),
    CONSTRAINT FK_CREDITO_CLIENTE FOREIGN KEY (COD_CLIENTE)
        REFERENCES CLIENTE (COD_CLIENTE) ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX IX_CREDITO_CLIENTE_ESTADO (COD_CLIENTE, ESTADO)
) ENGINE = InnoDB;

-- Tabla de amortización: una fila por cuota. La clave incluye el
-- crédito, así cada crédito numera sus cuotas desde 1.
CREATE TABLE CUOTA (
    COD_CREDITO        INT            NOT NULL,
    NUM_CUOTA          INT            NOT NULL,
    VALOR_CUOTA        NUMERIC(10,2)  NOT NULL,
    INTERES_PAGADO     NUMERIC(10,2)  NOT NULL,
    CAPITAL_PAGADO     NUMERIC(10,2)  NOT NULL,
    SALDO              NUMERIC(10,2)  NOT NULL,
    CONSTRAINT PK_CUOTA PRIMARY KEY (COD_CREDITO, NUM_CUOTA),
    CONSTRAINT CHK_CUOTA_NUMERO CHECK (NUM_CUOTA >= 1),
    CONSTRAINT CHK_CUOTA_VALORES CHECK (VALOR_CUOTA > 0 AND INTERES_PAGADO >= 0 AND CAPITAL_PAGADO > 0),
    CONSTRAINT CHK_CUOTA_SALDO CHECK (SALDO >= 0),
    CONSTRAINT FK_CUOTA_CREDITO FOREIGN KEY (COD_CREDITO)
        REFERENCES CREDITO (COD_CREDITO) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB;

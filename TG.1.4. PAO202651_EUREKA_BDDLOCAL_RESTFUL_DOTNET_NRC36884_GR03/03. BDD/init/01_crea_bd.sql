-- =============================================================
-- EurekaBank - Grupo 03 (NRC 36884)
-- Script 01: creación de la base de datos y sus tablas
-- Motor: SQL Server 2019 o superior (Express)
--
-- Traducción a T-SQL del script de MySQL de TG.1.1: mismas 10
-- tablas, columnas, restricciones y datos.
--   * Intercalación UTF-8 sin distinguir mayúsculas ni acentos
--     (equivale a utf8mb4_0900_ai_ci de MySQL).
--   * AUTO_INCREMENT -> IDENTITY(1,1).
--   * vch_emplclave de 64 caracteres para el hash SHA-256.
--   * Importes de movimientos estrictamente positivos y saldos
--     no negativos (RN-01, RN-03).
-- =============================================================

USE master;
GO

IF DB_ID('eurekabank') IS NOT NULL
BEGIN
    ALTER DATABASE eurekabank SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE eurekabank;
END
GO

CREATE DATABASE eurekabank COLLATE Latin1_General_100_CI_AI_SC_UTF8;
GO

USE eurekabank;
GO

-- -------------------------------------------------------------
-- Catálogos
-- -------------------------------------------------------------

CREATE TABLE moneda (
    chr_monecodigo        CHAR(2)       NOT NULL,
    vch_monedescripcion   VARCHAR(20)   NOT NULL,
    CONSTRAINT pk_moneda PRIMARY KEY (chr_monecodigo)
);

CREATE TABLE tipomovimiento (
    chr_tipocodigo        CHAR(3)       NOT NULL,
    vch_tipodescripcion   VARCHAR(40)   NOT NULL,
    vch_tipoaccion        VARCHAR(10)   NOT NULL,
    vch_tipoestado        VARCHAR(15)   NOT NULL CONSTRAINT df_tipomovimiento_estado DEFAULT 'ACTIVO',
    CONSTRAINT pk_tipomovimiento PRIMARY KEY (chr_tipocodigo),
    CONSTRAINT chk_tipomovimiento_accion CHECK (vch_tipoaccion IN ('INGRESO', 'SALIDA')),
    CONSTRAINT chk_tipomovimiento_estado CHECK (vch_tipoestado IN ('ACTIVO', 'ANULADO', 'CANCELADO'))
);

CREATE TABLE sucursal (
    chr_sucucodigo        CHAR(3)       NOT NULL,
    vch_sucunombre        VARCHAR(50)   NOT NULL,
    vch_sucuciudad        VARCHAR(30)   NOT NULL,
    vch_sucudireccion     VARCHAR(50)   NULL,
    int_sucucontcuenta    INT           NOT NULL,
    CONSTRAINT pk_sucursal PRIMARY KEY (chr_sucucodigo)
);

-- -------------------------------------------------------------
-- Seguridad: empleados, usuarios, asignaciones y sesiones
-- -------------------------------------------------------------

CREATE TABLE empleado (
    chr_emplcodigo        CHAR(4)       NOT NULL,
    vch_emplpaterno       VARCHAR(25)   NOT NULL,
    vch_emplmaterno       VARCHAR(25)   NOT NULL,
    vch_emplnombre        VARCHAR(30)   NOT NULL,
    vch_emplciudad        VARCHAR(30)   NOT NULL,
    vch_empldireccion     VARCHAR(50)   NULL,
    CONSTRAINT pk_empleado PRIMARY KEY (chr_emplcodigo)
);

-- La clave se guarda como SHA-256 en hexadecimal en minúsculas (64 caracteres).
CREATE TABLE usuario (
    chr_emplcodigo        CHAR(4)       NOT NULL,
    vch_emplusuario       VARCHAR(20)   NOT NULL,
    vch_emplclave         CHAR(64)      NOT NULL,
    vch_emplestado        VARCHAR(15)   NOT NULL CONSTRAINT df_usuario_estado DEFAULT 'ACTIVO',
    CONSTRAINT pk_usuario PRIMARY KEY (chr_emplcodigo),
    CONSTRAINT uq_usuario_login UNIQUE (vch_emplusuario),
    CONSTRAINT chk_usuario_estado CHECK (vch_emplestado IN ('ACTIVO', 'ANULADO', 'CANCELADO')),
    CONSTRAINT fk_usuario_empleado FOREIGN KEY (chr_emplcodigo) REFERENCES empleado (chr_emplcodigo)
);

-- Asignación vigente del empleado a una sucursal: dtt_asigfechabaja IS NULL.
CREATE TABLE asignado (
    chr_asigcodigo        CHAR(6)       NOT NULL,
    chr_sucucodigo        CHAR(3)       NOT NULL,
    chr_emplcodigo        CHAR(4)       NOT NULL,
    dtt_asigfechaalta     DATE          NOT NULL,
    dtt_asigfechabaja     DATE          NULL,
    CONSTRAINT pk_asignado PRIMARY KEY (chr_asigcodigo),
    CONSTRAINT fk_asignado_sucursal FOREIGN KEY (chr_sucucodigo) REFERENCES sucursal (chr_sucucodigo),
    CONSTRAINT fk_asignado_empleado FOREIGN KEY (chr_emplcodigo) REFERENCES empleado (chr_emplcodigo)
);

-- Registro de ingreso (RF-01) y salida (RF-02) de cada sesión.
CREATE TABLE logsession (
    int_logid             INT           IDENTITY(1,1) NOT NULL,
    chr_emplcodigo        CHAR(4)       NOT NULL,
    dtt_logingreso        DATETIME      NOT NULL,
    dtt_logsalida         DATETIME      NULL,
    vch_logip             VARCHAR(45)   NOT NULL CONSTRAINT df_logsession_ip DEFAULT 'NONE',
    vch_loghostname       VARCHAR(100)  NOT NULL CONSTRAINT df_logsession_hostname DEFAULT 'NONE',
    CONSTRAINT pk_logsession PRIMARY KEY (int_logid),
    CONSTRAINT fk_logsession_empleado FOREIGN KEY (chr_emplcodigo) REFERENCES empleado (chr_emplcodigo)
);

-- -------------------------------------------------------------
-- Negocio: clientes, cuentas y movimientos
-- -------------------------------------------------------------

CREATE TABLE cliente (
    chr_cliecodigo        CHAR(5)       NOT NULL,
    vch_cliepaterno       VARCHAR(25)   NOT NULL,
    vch_cliematerno       VARCHAR(25)   NOT NULL,
    vch_clienombre        VARCHAR(30)   NOT NULL,
    chr_cliedni           CHAR(8)       NOT NULL,
    vch_clieciudad        VARCHAR(30)   NOT NULL,
    vch_cliedireccion     VARCHAR(50)   NOT NULL,
    vch_clietelefono      VARCHAR(20)   NULL,
    vch_clieemail         VARCHAR(50)   NULL,
    CONSTRAINT pk_cliente PRIMARY KEY (chr_cliecodigo)
);

-- Código de cuenta: código de sucursal (3) + correlativo (5).
CREATE TABLE cuenta (
    chr_cuencodigo        CHAR(8)       NOT NULL,
    chr_monecodigo        CHAR(2)       NOT NULL,
    chr_sucucodigo        CHAR(3)       NOT NULL,
    chr_emplcreacuenta    CHAR(4)       NOT NULL,
    chr_cliecodigo        CHAR(5)       NOT NULL,
    dec_cuensaldo         DECIMAL(12,2) NOT NULL,
    dtt_cuenfechacreacion DATE          NOT NULL,
    vch_cuenestado        VARCHAR(15)   NOT NULL CONSTRAINT df_cuenta_estado DEFAULT 'ACTIVO',
    int_cuencontmov       INT           NOT NULL,
    chr_cuenclave         CHAR(6)       NOT NULL,
    CONSTRAINT pk_cuenta PRIMARY KEY (chr_cuencodigo),
    CONSTRAINT chk_cuenta_estado CHECK (vch_cuenestado IN ('ACTIVO', 'ANULADO', 'CANCELADO')),
    CONSTRAINT chk_cuenta_saldo CHECK (dec_cuensaldo >= 0),
    CONSTRAINT fk_cuenta_moneda FOREIGN KEY (chr_monecodigo) REFERENCES moneda (chr_monecodigo),
    CONSTRAINT fk_cuenta_sucursal FOREIGN KEY (chr_sucucodigo) REFERENCES sucursal (chr_sucucodigo),
    CONSTRAINT fk_cuenta_empleado FOREIGN KEY (chr_emplcreacuenta) REFERENCES empleado (chr_emplcodigo),
    CONSTRAINT fk_cuenta_cliente FOREIGN KEY (chr_cliecodigo) REFERENCES cliente (chr_cliecodigo)
);

-- chr_cuenreferencia: la otra cuenta de una transferencia (tipos 008 y 009).
CREATE TABLE movimiento (
    chr_cuencodigo        CHAR(8)       NOT NULL,
    int_movinumero        INT           NOT NULL,
    dtt_movifecha         DATE          NOT NULL,
    chr_emplcodigo        CHAR(4)       NOT NULL,
    chr_tipocodigo        CHAR(3)       NOT NULL,
    dec_moviimporte       DECIMAL(12,2) NOT NULL,
    chr_cuenreferencia    CHAR(8)       NULL,
    CONSTRAINT pk_movimiento PRIMARY KEY (chr_cuencodigo, int_movinumero),
    CONSTRAINT chk_movimiento_importe CHECK (dec_moviimporte > 0),
    CONSTRAINT fk_movimiento_cuenta FOREIGN KEY (chr_cuencodigo) REFERENCES cuenta (chr_cuencodigo),
    CONSTRAINT fk_movimiento_empleado FOREIGN KEY (chr_emplcodigo) REFERENCES empleado (chr_emplcodigo),
    CONSTRAINT fk_movimiento_tipo FOREIGN KEY (chr_tipocodigo) REFERENCES tipomovimiento (chr_tipocodigo),
    CONSTRAINT fk_movimiento_referencia FOREIGN KEY (chr_cuenreferencia) REFERENCES cuenta (chr_cuencodigo)
);
GO

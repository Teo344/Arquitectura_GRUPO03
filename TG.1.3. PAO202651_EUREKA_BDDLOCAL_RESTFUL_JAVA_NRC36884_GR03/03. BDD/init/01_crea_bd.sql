-- =============================================================
-- EurekaBank - Grupo 03 (NRC 36884)
-- Script 01: creación de la base de datos y sus tablas
-- Motor: MySQL 8.4 LTS (InnoDB, utf8mb4)
--
-- Basado en el esquema base de EurekaBank (entrega GR08), con
-- las correcciones definidas en el ERS:
--   * Solo las 10 tablas que usan RF-01 a RF-07.
--   * Nombres de tablas y columnas en minúsculas: MySQL en Linux
--     (contenedor Docker) distingue mayúsculas en los nombres.
--   * vch_emplclave de 64 caracteres para el hash SHA-256.
--   * Importes de movimientos estrictamente positivos y saldos
--     no negativos (RN-01, RN-03).
-- =============================================================

DROP DATABASE IF EXISTS eurekabank;

CREATE DATABASE eurekabank
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE eurekabank;

-- -------------------------------------------------------------
-- Catálogos
-- -------------------------------------------------------------

CREATE TABLE moneda (
    chr_monecodigo        CHAR(2)       NOT NULL,
    vch_monedescripcion   VARCHAR(20)   NOT NULL,
    CONSTRAINT pk_moneda PRIMARY KEY (chr_monecodigo)
) ENGINE = InnoDB;

CREATE TABLE tipomovimiento (
    chr_tipocodigo        CHAR(3)       NOT NULL,
    vch_tipodescripcion   VARCHAR(40)   NOT NULL,
    vch_tipoaccion        VARCHAR(10)   NOT NULL,
    vch_tipoestado        VARCHAR(15)   NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT pk_tipomovimiento PRIMARY KEY (chr_tipocodigo),
    CONSTRAINT chk_tipomovimiento_accion CHECK (vch_tipoaccion IN ('INGRESO', 'SALIDA')),
    CONSTRAINT chk_tipomovimiento_estado CHECK (vch_tipoestado IN ('ACTIVO', 'ANULADO', 'CANCELADO'))
) ENGINE = InnoDB;

CREATE TABLE sucursal (
    chr_sucucodigo        CHAR(3)       NOT NULL,
    vch_sucunombre        VARCHAR(50)   NOT NULL,
    vch_sucuciudad        VARCHAR(30)   NOT NULL,
    vch_sucudireccion     VARCHAR(50)   NULL,
    int_sucucontcuenta    INT           NOT NULL,
    CONSTRAINT pk_sucursal PRIMARY KEY (chr_sucucodigo)
) ENGINE = InnoDB;

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
) ENGINE = InnoDB;

-- La clave se guarda como SHA-256 en hexadecimal (64 caracteres).
CREATE TABLE usuario (
    chr_emplcodigo        CHAR(4)       NOT NULL,
    vch_emplusuario       VARCHAR(20)   NOT NULL,
    vch_emplclave         CHAR(64)      NOT NULL,
    vch_emplestado        VARCHAR(15)   NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT pk_usuario PRIMARY KEY (chr_emplcodigo),
    CONSTRAINT uq_usuario_login UNIQUE (vch_emplusuario),
    CONSTRAINT chk_usuario_estado CHECK (vch_emplestado IN ('ACTIVO', 'ANULADO', 'CANCELADO')),
    CONSTRAINT fk_usuario_empleado FOREIGN KEY (chr_emplcodigo)
        REFERENCES empleado (chr_emplcodigo) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB;

-- Asignación vigente del empleado a una sucursal: dtt_asigfechabaja IS NULL.
CREATE TABLE asignado (
    chr_asigcodigo        CHAR(6)       NOT NULL,
    chr_sucucodigo        CHAR(3)       NOT NULL,
    chr_emplcodigo        CHAR(4)       NOT NULL,
    dtt_asigfechaalta     DATE          NOT NULL,
    dtt_asigfechabaja     DATE          NULL,
    CONSTRAINT pk_asignado PRIMARY KEY (chr_asigcodigo),
    CONSTRAINT fk_asignado_sucursal FOREIGN KEY (chr_sucucodigo)
        REFERENCES sucursal (chr_sucucodigo) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_asignado_empleado FOREIGN KEY (chr_emplcodigo)
        REFERENCES empleado (chr_emplcodigo) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB;

-- Registro de ingreso (RF-01) y salida (RF-02) de cada sesión.
CREATE TABLE logsession (
    int_logid             INT           NOT NULL AUTO_INCREMENT,
    chr_emplcodigo        CHAR(4)       NOT NULL,
    dtt_logingreso        DATETIME      NOT NULL,
    dtt_logsalida         DATETIME      NULL,
    vch_logip             VARCHAR(45)   NOT NULL DEFAULT 'NONE',
    vch_loghostname       VARCHAR(100)  NOT NULL DEFAULT 'NONE',
    CONSTRAINT pk_logsession PRIMARY KEY (int_logid),
    CONSTRAINT fk_logsession_empleado FOREIGN KEY (chr_emplcodigo)
        REFERENCES empleado (chr_emplcodigo) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB;

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
) ENGINE = InnoDB;

-- Código de cuenta: código de sucursal (3) + correlativo (5).
CREATE TABLE cuenta (
    chr_cuencodigo        CHAR(8)       NOT NULL,
    chr_monecodigo        CHAR(2)       NOT NULL,
    chr_sucucodigo        CHAR(3)       NOT NULL,
    chr_emplcreacuenta    CHAR(4)       NOT NULL,
    chr_cliecodigo        CHAR(5)       NOT NULL,
    dec_cuensaldo         DECIMAL(12,2) NOT NULL,
    dtt_cuenfechacreacion DATE          NOT NULL,
    vch_cuenestado        VARCHAR(15)   NOT NULL DEFAULT 'ACTIVO',
    int_cuencontmov       INT           NOT NULL,
    chr_cuenclave         CHAR(6)       NOT NULL,
    CONSTRAINT pk_cuenta PRIMARY KEY (chr_cuencodigo),
    CONSTRAINT chk_cuenta_estado CHECK (vch_cuenestado IN ('ACTIVO', 'ANULADO', 'CANCELADO')),
    CONSTRAINT chk_cuenta_saldo CHECK (dec_cuensaldo >= 0),
    CONSTRAINT fk_cuenta_moneda FOREIGN KEY (chr_monecodigo)
        REFERENCES moneda (chr_monecodigo) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_cuenta_sucursal FOREIGN KEY (chr_sucucodigo)
        REFERENCES sucursal (chr_sucucodigo) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_cuenta_empleado FOREIGN KEY (chr_emplcreacuenta)
        REFERENCES empleado (chr_emplcodigo) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_cuenta_cliente FOREIGN KEY (chr_cliecodigo)
        REFERENCES cliente (chr_cliecodigo) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB;

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
    CONSTRAINT fk_movimiento_cuenta FOREIGN KEY (chr_cuencodigo)
        REFERENCES cuenta (chr_cuencodigo) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_movimiento_empleado FOREIGN KEY (chr_emplcodigo)
        REFERENCES empleado (chr_emplcodigo) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_movimiento_tipo FOREIGN KEY (chr_tipocodigo)
        REFERENCES tipomovimiento (chr_tipocodigo) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_movimiento_referencia FOREIGN KEY (chr_cuenreferencia)
        REFERENCES cuenta (chr_cuencodigo) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB;

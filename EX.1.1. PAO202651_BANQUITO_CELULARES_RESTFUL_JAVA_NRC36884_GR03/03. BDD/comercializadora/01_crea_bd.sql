-- =============================================================
-- Comercializadora de Teléfonos Celulares - Grupo 03 (NRC 36884)
-- Script 01: creación de la base de datos de facturación
-- Motor: MySQL 8.4 o superior (InnoDB, utf8mb4)
--
-- Es una aplicación distinta del Core BanQuito y tiene su propia BD:
--   * USUARIO: empleados que inician sesión en los 4 clientes.
--   * TELEFONO: catálogo de teléfonos a la venta (sin inventario).
--   * FACTURA / DETALLE_FACTURA: ventas en efectivo o a crédito.
-- El comprador es cliente del banco: la factura guarda su cédula, su
-- código y su nombre tal como los devuelve el Core. No hay FK hacia
-- el banco porque son bases de datos distintas.
-- =============================================================

DROP DATABASE IF EXISTS comercializadora;

CREATE DATABASE comercializadora
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE comercializadora;

-- -------------------------------------------------------------
-- Seguridad
-- -------------------------------------------------------------

-- La clave se guarda como SHA-256 en hexadecimal (64 caracteres).
CREATE TABLE USUARIO (
    COD_USUARIO        INT            NOT NULL AUTO_INCREMENT,
    USUARIO            VARCHAR(20)    NOT NULL,
    CLAVE              CHAR(64)       NOT NULL,
    NOMBRE             VARCHAR(100)   NOT NULL,
    ESTADO             VARCHAR(10)    NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT PK_USUARIO PRIMARY KEY (COD_USUARIO),
    CONSTRAINT UQ_USUARIO_USUARIO UNIQUE (USUARIO),
    CONSTRAINT CHK_USUARIO_ESTADO CHECK (ESTADO IN ('ACTIVO', 'ANULADO'))
) ENGINE = InnoDB;

-- -------------------------------------------------------------
-- Catálogo
-- -------------------------------------------------------------

-- Eliminar un teléfono del catálogo lo deja INACTIVO, así no se
-- pierden las facturas que ya lo vendieron.
CREATE TABLE TELEFONO (
    COD_TELEFONO       INT            NOT NULL AUTO_INCREMENT,
    MARCA              VARCHAR(50)    NOT NULL,
    MODELO             VARCHAR(100)   NOT NULL,
    PRECIO             NUMERIC(10,2)  NOT NULL,
    ESTADO             VARCHAR(10)    NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT PK_TELEFONO PRIMARY KEY (COD_TELEFONO),
    CONSTRAINT CHK_TELEFONO_PRECIO CHECK (PRECIO > 0),
    CONSTRAINT CHK_TELEFONO_ESTADO CHECK (ESTADO IN ('ACTIVO', 'INACTIVO'))
) ENGINE = InnoDB;

-- -------------------------------------------------------------
-- Facturación
-- -------------------------------------------------------------

-- EFECTIVO: descuento del 42 % y sin crédito.
-- CREDITO:  sin descuento; lleva el crédito que otorgó el banco y el
--           número de cuotas (3 a 18).
-- El servidor calcula los valores; la BD solo garantiza su coherencia.
CREATE TABLE FACTURA (
    NUM_FACTURA        INT            NOT NULL AUTO_INCREMENT,
    FECHA              DATETIME       NOT NULL,
    COD_USUARIO        INT            NOT NULL,
    CEDULA_CLIENTE     VARCHAR(100)   NOT NULL,
    COD_CLIENTE_BANCO  INT            NOT NULL,
    NOMBRE_CLIENTE     VARCHAR(100)   NOT NULL,
    FORMA_PAGO         VARCHAR(10)    NOT NULL,
    SUBTOTAL           NUMERIC(10,2)  NOT NULL,
    DESCUENTO          NUMERIC(10,2)  NOT NULL DEFAULT 0,
    TOTAL              NUMERIC(10,2)  NOT NULL,
    COD_CREDITO_BANCO  INT            NULL,
    NUM_CUOTAS         INT            NULL,
    CONSTRAINT PK_FACTURA PRIMARY KEY (NUM_FACTURA),
    CONSTRAINT CHK_FACTURA_FORMA_PAGO CHECK (FORMA_PAGO IN ('EFECTIVO', 'CREDITO')),
    CONSTRAINT CHK_FACTURA_VALORES CHECK (SUBTOTAL > 0 AND DESCUENTO >= 0 AND TOTAL = SUBTOTAL - DESCUENTO),
    CONSTRAINT CHK_FACTURA_CREDITO CHECK (
        (FORMA_PAGO = 'EFECTIVO' AND COD_CREDITO_BANCO IS NULL AND NUM_CUOTAS IS NULL)
        OR (FORMA_PAGO = 'CREDITO' AND DESCUENTO = 0 AND COD_CREDITO_BANCO IS NOT NULL
            AND NUM_CUOTAS BETWEEN 3 AND 18)),
    CONSTRAINT FK_FACTURA_USUARIO FOREIGN KEY (COD_USUARIO)
        REFERENCES USUARIO (COD_USUARIO) ON DELETE RESTRICT ON UPDATE RESTRICT,
    INDEX IX_FACTURA_CEDULA (CEDULA_CLIENTE)
) ENGINE = InnoDB;

CREATE TABLE DETALLE_FACTURA (
    NUM_FACTURA        INT            NOT NULL,
    COD_TELEFONO       INT            NOT NULL,
    CANTIDAD           INT            NOT NULL,
    PRECIO_UNITARIO    NUMERIC(10,2)  NOT NULL,
    SUBTOTAL           NUMERIC(10,2)  NOT NULL,
    CONSTRAINT PK_DETALLE_FACTURA PRIMARY KEY (NUM_FACTURA, COD_TELEFONO),
    CONSTRAINT CHK_DETALLE_CANTIDAD CHECK (CANTIDAD > 0),
    CONSTRAINT CHK_DETALLE_VALORES CHECK (PRECIO_UNITARIO > 0 AND SUBTOTAL = CANTIDAD * PRECIO_UNITARIO),
    CONSTRAINT FK_DETALLE_FACTURA FOREIGN KEY (NUM_FACTURA)
        REFERENCES FACTURA (NUM_FACTURA) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT FK_DETALLE_TELEFONO FOREIGN KEY (COD_TELEFONO)
        REFERENCES TELEFONO (COD_TELEFONO) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB;

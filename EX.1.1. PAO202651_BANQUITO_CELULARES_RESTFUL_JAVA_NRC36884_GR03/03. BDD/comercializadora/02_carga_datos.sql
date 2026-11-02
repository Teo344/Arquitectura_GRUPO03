-- =============================================================
-- Comercializadora de Teléfonos Celulares - Grupo 03 (NRC 36884)
-- Script 02: datos de trabajo
--
-- Usuario principal para iniciar sesión:  MONSTER / MONSTER9
-- Las claves se guardan con SHA2(clave, 256).
--
-- Los precios del catálogo cubren los casos de crédito frente a los
-- montos máximos del banco (2250.00 cliente 1, 450.00 cliente 2,
-- 945.00 cliente 6): hay teléfonos por debajo de 450, entre 450 y
-- 2250 y por encima de 2250.
-- No se cargan facturas: se generan desde los clientes.
-- =============================================================

USE comercializadora;

-- -------------------------------------------------------------
-- Usuarios (JPEREZ está ANULADO para probar el rechazo del login)
-- -------------------------------------------------------------
INSERT INTO USUARIO (USUARIO, CLAVE, NOMBRE, ESTADO) VALUES
('MONSTER', SHA2('MONSTER9', 256), 'Operador Monster Grupo03', 'ACTIVO'),
('JPEREZ',  SHA2('JPEREZ1', 256),  'Juan Pérez',               'ANULADO');

-- -------------------------------------------------------------
-- Catálogo de teléfonos
-- -------------------------------------------------------------
INSERT INTO TELEFONO (MARCA, MODELO, PRECIO, ESTADO) VALUES
('Samsung',  'Galaxy A15',        199.00, 'ACTIVO'),
('Xiaomi',   'Redmi Note 13',     279.00, 'ACTIVO'),
('Motorola', 'Moto G84',          349.00, 'ACTIVO'),
('Samsung',  'Galaxy A55',        549.00, 'ACTIVO'),
('Xiaomi',   '14T',               899.00, 'ACTIVO'),
('Apple',    'iPhone 15',        1199.00, 'ACTIVO'),
('Samsung',  'Galaxy S24 Ultra', 1899.00, 'ACTIVO'),
('Apple',    'iPhone 16 Pro Max', 2399.00, 'ACTIVO');

COMMIT;

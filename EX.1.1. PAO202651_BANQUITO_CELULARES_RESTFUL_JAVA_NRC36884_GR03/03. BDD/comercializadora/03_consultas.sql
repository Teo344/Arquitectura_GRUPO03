-- =============================================================
-- Comercializadora de Teléfonos Celulares - Grupo 03 (NRC 36884)
-- Script 03: consultas de verificación y de referencia
--
-- No modifica datos. Comprueba la carga y es la referencia de los
-- SQL que usará el servidor de la comercializadora.
-- =============================================================

USE comercializadora;

-- -------------------------------------------------------------
-- 1. Cantidad de registros por tabla
--    Esperado: usuario 2, telefono 8 (facturas 0 hasta que se venda)
-- -------------------------------------------------------------
SELECT 'usuario' AS tabla, COUNT(*) AS registros FROM USUARIO
UNION ALL SELECT 'telefono',        COUNT(*) FROM TELEFONO
UNION ALL SELECT 'factura',         COUNT(*) FROM FACTURA
UNION ALL SELECT 'detalle_factura', COUNT(*) FROM DETALLE_FACTURA;

-- -------------------------------------------------------------
-- 2. Catálogo a la venta (solo teléfonos ACTIVOS), con el precio
--    en efectivo (42 % de descuento) como referencia.
-- -------------------------------------------------------------
SELECT COD_TELEFONO,
       MARCA,
       MODELO,
       PRECIO,
       ROUND(PRECIO * 0.58, 2) AS PRECIO_EFECTIVO
FROM TELEFONO
WHERE ESTADO = 'ACTIVO'
ORDER BY PRECIO;

-- -------------------------------------------------------------
-- 3. Login: MONSTER / MONSTER9
--    Devuelve una fila si las credenciales son correctas y el
--    usuario está ACTIVO. En el servidor la clave llega como
--    parámetro y se compara su SHA-256 (hexadecimal en minúsculas).
-- -------------------------------------------------------------
SET @usuario = 'MONSTER';
SET @clave   = 'MONSTER9';

SELECT COD_USUARIO, USUARIO, NOMBRE, ESTADO
FROM USUARIO
WHERE USUARIO = @usuario
  AND CLAVE   = SHA2(@clave, 256)
  AND ESTADO  = 'ACTIVO';

-- -------------------------------------------------------------
-- 4. Facturas con su detalle (vacío hasta la primera venta)
-- -------------------------------------------------------------
SELECT f.NUM_FACTURA, f.FECHA, f.CEDULA_CLIENTE, f.NOMBRE_CLIENTE, f.FORMA_PAGO,
       f.SUBTOTAL, f.DESCUENTO, f.TOTAL, f.COD_CREDITO_BANCO, f.NUM_CUOTAS,
       d.COD_TELEFONO, t.MARCA, t.MODELO, d.CANTIDAD, d.PRECIO_UNITARIO, d.SUBTOTAL AS SUBTOTAL_DETALLE
FROM FACTURA f
JOIN DETALLE_FACTURA d ON d.NUM_FACTURA = f.NUM_FACTURA
JOIN TELEFONO t        ON t.COD_TELEFONO = d.COD_TELEFONO
ORDER BY f.NUM_FACTURA, d.COD_TELEFONO;

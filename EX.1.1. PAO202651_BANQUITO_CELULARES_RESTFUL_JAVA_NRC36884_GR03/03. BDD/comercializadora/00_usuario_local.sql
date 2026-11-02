-- =============================================================
-- Comercializadora de Teléfonos Celulares - Grupo 03 (NRC 36884)
-- Script 00: usuario de la aplicación para el MySQL local
--
-- Se ejecuta como root después de 01_crea_bd.sql y 02_carga_datos.sql.
-- La comercializadora consulta, factura y mantiene el catálogo
-- (eliminar = marcar INACTIVO): no necesita DELETE, DROP ni ALTER, y
-- no ve la BD del banco (la consulta por servicios web).
-- Cambiar la clave y usar la misma en la configuración del servidor.
-- =============================================================

CREATE USER IF NOT EXISTS 'comercializadora_app'@'localhost' IDENTIFIED BY 'cambiar_comercializadora';

GRANT SELECT, INSERT, UPDATE ON comercializadora.* TO 'comercializadora_app'@'localhost';

FLUSH PRIVILEGES;

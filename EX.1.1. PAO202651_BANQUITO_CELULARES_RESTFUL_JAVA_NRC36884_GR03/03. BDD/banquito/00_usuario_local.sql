-- =============================================================
-- BanQuito - Grupo 03 (NRC 36884)
-- Script 00: usuario de la aplicación Core para el MySQL local
--
-- Se ejecuta como root después de 01_crea_bd.sql y 02_carga_datos.sql.
-- El Core solo consulta, registra créditos y cuotas y actualiza su
-- estado: no necesita DELETE, DROP ni ALTER, y no ve la BD de la
-- comercializadora.
-- Cambiar la clave y usar la misma en la configuración del servidor Core.
-- =============================================================

CREATE USER IF NOT EXISTS 'banquito_app'@'localhost' IDENTIFIED BY 'cambiar_banquito';

GRANT SELECT, INSERT, UPDATE ON banquito.* TO 'banquito_app'@'localhost';

FLUSH PRIVILEGES;

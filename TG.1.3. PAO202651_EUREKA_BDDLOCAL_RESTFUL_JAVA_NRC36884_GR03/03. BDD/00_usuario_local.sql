-- =============================================================
-- EurekaBank - Grupo 03 (NRC 36884)
-- Script 00: usuario de la aplicación para el MySQL local
--
-- Solo para la instalación local (sin Docker): en Docker el
-- usuario lo crea docker-compose con MYSQL_USER / MYSQL_PASSWORD.
-- Se ejecuta como root después de 01_crea_bd.sql y 02_carga_datos.sql.
--
-- El servidor solo consulta, inserta movimientos y sesiones y
-- actualiza saldos: no necesita DELETE, DROP ni ALTER.
-- Cambiar la clave y usar la misma en db.properties del servidor.
-- =============================================================

CREATE USER IF NOT EXISTS 'eureka'@'localhost' IDENTIFIED BY 'cambiar_eureka';

GRANT SELECT, INSERT, UPDATE ON eurekabank.* TO 'eureka'@'localhost';

FLUSH PRIVILEGES;

-- =============================================================
-- EurekaBank - Grupo 03 (NRC 36884)
-- Script 00: usuario de la aplicación (SQL Server Express local)
--
-- Se ejecuta con un usuario administrador (sqlcmd -E) después de
-- 01_crea_bd.sql y 02_carga_datos.sql, y cada vez que se recrea la
-- BD: el login se conserva, pero el usuario de la BD se borra con ella.
-- Requiere autenticación mixta (SQL Server y Windows).
--
-- El servidor solo consulta, inserta movimientos y sesiones y
-- actualiza saldos: no necesita DELETE, DROP ni ALTER.
-- Cambiar la clave y usar la misma en conexion.config del servidor.
-- =============================================================

USE master;
GO

IF SUSER_ID('eureka') IS NULL
    CREATE LOGIN eureka WITH PASSWORD = 'cambiar_eureka', CHECK_POLICY = OFF, DEFAULT_DATABASE = eurekabank;
GO

USE eurekabank;
GO

IF USER_ID('eureka') IS NULL
    CREATE USER eureka FOR LOGIN eureka;
GO

GRANT SELECT, INSERT, UPDATE ON SCHEMA::dbo TO eureka;
GO

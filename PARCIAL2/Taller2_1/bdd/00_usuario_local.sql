-- =============================================================================
-- Usuario de la aplicacion ApiPaisesMVC.
-- Ejecutar como root una sola vez. La aplicacion NO usa root: este usuario
-- solo puede leer y escribir datos en api_paises (principio de minimo privilegio).
-- Cambiar la clave y copiarla en src/main/resources/config.properties (db.clave).
-- =============================================================================
CREATE USER IF NOT EXISTS 'paises'@'localhost' IDENTIFIED BY 'cambiar_paises';

GRANT SELECT, INSERT, UPDATE, DELETE ON api_paises.* TO 'paises'@'localhost';

FLUSH PRIVILEGES;

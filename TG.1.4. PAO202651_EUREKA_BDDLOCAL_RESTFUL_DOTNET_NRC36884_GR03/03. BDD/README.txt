EurekaBank - Grupo 03 - Base de datos (SQL Server)
==================================================

Archivos
  init/01_crea_bd.sql      Crea la BD eurekabank y sus 10 tablas (borra la BD si ya existe).
  init/02_carga_datos.sql  Carga los datos semilla (usuario principal MONSTER / MONSTER9).
  00_usuario_local.sql     Crea el login 'eureka' de la aplicación y le da permisos en la BD.
  03_consultas.sql         Consultas de verificación; no modifica datos.

Son los mismos datos y estructura que la BD MySQL de TG.1.1, traducidos a T-SQL.

Instalación de SQL Server Express (una sola vez)
  1. Descargar SQL Server Express (2019 o superior) e instalar con la opción "Básica"
     o "Personalizada". El nombre de instancia por defecto es SQLEXPRESS.
  2. Activar la autenticación mixta (SQL Server y Windows): en la instalación
     personalizada, o después en SSMS > propiedades del servidor > Seguridad,
     y reiniciar el servicio "SQL Server (SQLEXPRESS)".

Crear y cargar la BD (desde esta carpeta, consola de PowerShell)
  sqlcmd -S localhost\SQLEXPRESS -E -f 65001 -i init\01_crea_bd.sql
  sqlcmd -S localhost\SQLEXPRESS -E -f 65001 -i init\02_carga_datos.sql
  sqlcmd -S localhost\SQLEXPRESS -E -f 65001 -i 00_usuario_local.sql

  -E entra con el usuario de Windows (administrador de la instancia) y -f 65001
  lee los scripts como UTF-8. Antes de ejecutar 00 se puede cambiar la clave;
  debe ser la misma de conexion.json en el servidor.

Verificación
  sqlcmd -S localhost\SQLEXPRESS -U eureka -P cambiar_eureka -d eurekabank -Q "SELECT COUNT(*) FROM cuenta"
  Debe mostrar 6. También se puede ejecutar 03_consultas.sql: la consulta 5 debe
  mostrar ok_saldo = 1 y ok_contador = 1 en todas las cuentas.

Reiniciar los datos (antes de cada ronda de pruebas)
  Volver a ejecutar los tres comandos: 01 borra y recrea la BD, 02 carga los datos
  semilla y 00 vuelve a crear el usuario de la BD (el login se conserva).

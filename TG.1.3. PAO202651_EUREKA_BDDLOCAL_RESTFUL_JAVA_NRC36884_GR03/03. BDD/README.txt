EurekaBank - Grupo 03 - Base de datos
=====================================

Archivos
  init/01_crea_bd.sql      Crea la BD eurekabank y sus 10 tablas (borra la BD si ya existe).
  init/02_carga_datos.sql  Carga los datos semilla (usuario principal MONSTER / MONSTER9).
  00_usuario_local.sql     Crea el usuario 'eureka' de la aplicación (solo MySQL local).
  03_consultas.sql         Consultas de verificación; no modifica datos.
  docker-compose.yml       Alternativa con Docker (no se usa en la instalación local).

Instalación en el MySQL local (Windows)
  1. Iniciar el servicio de MySQL (consola como administrador):
       net start MySQL267
  2. Desde esta carpeta, crear y cargar la BD como root:
       "C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe" -u root -p < init\01_crea_bd.sql
       "C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe" -u root -p < init\02_carga_datos.sql
  3. Crear el usuario de la aplicación (cambiar antes la clave en el script):
       "C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe" -u root -p < 00_usuario_local.sql
  4. En el servidor, copiar db.properties.example como db.properties y poner la misma clave.

Reiniciar los datos (antes de cada ronda de pruebas)
  Volver a ejecutar los pasos 2 y 3: 01 borra y recrea la BD, 02 carga los datos semilla.
  Las pruebas de transacciones del servidor parten de los saldos iniciales.

Verificación
  Ejecutar 03_consultas.sql: la consulta 1 debe dar los conteos indicados y la
  consulta 5 debe mostrar ok_saldo = 1 y ok_contador = 1 en todas las cuentas.

BanQuito + Comercializadora de Celulares - Grupo 03 - Bases de datos (MySQL local)
==================================================================================

Son dos aplicaciones y cada una tiene su propia base de datos:

  banquito/          Core bancario (CLIENTE, CUENTA, MOVIMIENTO) y módulo de crédito
                     (CREDITO, CUOTA). Usuario de la aplicación: banquito_app.
  comercializadora/  Facturación de celulares (USUARIO, TELEFONO, FACTURA,
                     DETALLE_FACTURA). Usuario de la aplicación: comercializadora_app.

En cada carpeta:
  01_crea_bd.sql       Crea la BD y sus tablas (borra la BD si ya existe).
  02_carga_datos.sql   Carga los datos de trabajo.
  00_usuario_local.sql Crea el usuario de la aplicación (solo SELECT, INSERT, UPDATE).
  03_consultas.sql     Consultas de verificación; no modifica datos.

Los movimientos del banco tienen fechas relativas a la fecha actual, así las
reglas de "último mes" y "últimos 3 meses" funcionan el día que se presente.

Instalación (PowerShell, desde esta carpeta)
  1. Iniciar el servicio de MySQL (consola como administrador):
       net start MySQL267
  2. Crear y cargar las dos BD como root (PowerShell no admite "<", por eso se usa source):
       $mysql = "C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe"
       & $mysql -u root -p --default-character-set=utf8mb4 -e "source banquito/01_crea_bd.sql"
       & $mysql -u root -p --default-character-set=utf8mb4 -e "source banquito/02_carga_datos.sql"
       & $mysql -u root -p --default-character-set=utf8mb4 -e "source banquito/00_usuario_local.sql"
       & $mysql -u root -p --default-character-set=utf8mb4 -e "source comercializadora/01_crea_bd.sql"
       & $mysql -u root -p --default-character-set=utf8mb4 -e "source comercializadora/02_carga_datos.sql"
       & $mysql -u root -p --default-character-set=utf8mb4 -e "source comercializadora/00_usuario_local.sql"
  3. Cambiar las claves de los usuarios en los scripts 00 antes de ejecutarlos si se
     desea; deben coincidir con la configuración de cada servidor.

Verificación
  & $mysql -u banquito_app -pcambiar_banquito --default-character-set=utf8mb4 -e "source banquito/03_consultas.sql"
  & $mysql -u comercializadora_app -pcambiar_comercializadora --default-character-set=utf8mb4 -e "source comercializadora/03_consultas.sql"

  Esperado en banquito:
    - 6 clientes, 7 cuentas, 52 movimientos, 1 crédito y 6 cuotas.
    - Sujetos de crédito: clientes 1, 2 y 6. El 3 falla por edad, el 4 por no
      tener depósitos en el último mes y el 5 por tener un crédito activo.
      La cédula 9999999999 no es cliente.
    - Monto máximo: cliente 1 = 2250.00, cliente 2 = 450.00, cliente 6 = 945.00.
    - La tabla de amortización del cliente 5 es la del ejemplo del enunciado.
    - ok_saldo = 1 en todas las cuentas.
  Esperado en comercializadora: 2 usuarios, 8 teléfonos y el login de MONSTER.

Reiniciar los datos (antes de presentar)
  Volver a ejecutar 01 y 02 de cada BD. Los usuarios de la aplicación se conservan.

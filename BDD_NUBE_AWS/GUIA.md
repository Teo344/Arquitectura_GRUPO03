# Bases de datos en la nube (AWS) — Grupo 03

Guía para usar las bases de datos del grupo en AWS (**MySQL** y **SQL Server**): encenderlas, conectarse, cargar datos y apagarlas para no gastar créditos.

> **Regla de oro:** la nube cobra por cada hora encendida. **Enciende solo la base que vayas a usar y apágala al terminar** (sección 5).

---

## 0. Qué hay en la nube

```
 Tu PC                                   AWS (región Ohio, us-east-2)
┌────────────────────────┐   túnel SSM   ┌────────────────────────┐ 3306 ┌──────────────────────────┐
│ DBeaver / proyecto     │ ════════════► │ EC2 grupo03-puente-bdd │ ───► │ RDS grupo03-mysql        │
│  MySQL:  127.0.0.1:3307│   (cifrado)   │ (solo hace de puente)  │      │ MySQL 8.4                │
│  SQL S.: 127.0.0.1,1434│               │                        │ 1433 ┌──────────────────────────┐
└────────────────────────┘               └────────────────────────┘ ───► │ RDS grupo03-mssql        │
                                                                         │ SQL Server Express 2022  │
                                                                         └──────────────────────────┘
```

| Dato | MySQL | SQL Server |
|---|---|---|
| Instancia RDS | `grupo03-mysql` | `grupo03-mssql` |
| Motor | MySQL 8.4 | SQL Server Express 2022 |
| Endpoint interno | `grupo03-mysql.cduuooaemmbx.us-east-2.rds.amazonaws.com:3306` | `grupo03-mssql.cduuooaemmbx.us-east-2.rds.amazonaws.com:1433` |
| **Cómo te conectas tú** (con el túnel abierto) | **`127.0.0.1` puerto `3307`** | **`127.0.0.1,1434`** (host `127.0.0.1`, puerto `1434`) |
| Bases | `eurekabank`, `banquito`, `comercializadora` | las que se carguen (p. ej. `eurekabank`) |
| Zona horaria | `America/Bogota` (UTC-5) | `SA Pacific Standard Time` (UTC-5) |
| Usuario administrador | `admin` | `admin` |

**Comunes a las dos:**
- **Región:** **us-east-2 (Ohio)**, siempre. Si en la consola no ves nada, revisa la región.
- **EC2 puente:** `grupo03-puente-bdd` (`i-083d8c63fe17abd80`). Sirve para las dos bases.
- **Hora:** las dos zonas horarias equivalen a la hora de Ecuador, así que `NOW()`, `CURDATE()` y `GETDATE()` dan la hora local.

Las bases **no son públicas**: desde internet no se llega a ellas. Solo se entra por el túnel, que pasa por la EC2.

### El script `aws-bdd.ps1`

| Comando | Qué hace |
|---|---|
| `.\aws-bdd.ps1 login` | Inicia sesión en AWS desde el navegador |
| `.\aws-bdd.ps1 estado` | Dice si la EC2 y cada base están encendidas |
| `.\aws-bdd.ps1 encender mysql` | Enciende la EC2 + MySQL y espera a que estén listas |
| `.\aws-bdd.ps1 encender sqlserver` | Enciende la EC2 + SQL Server |
| `.\aws-bdd.ps1 encender` | Enciende las dos bases |
| `.\aws-bdd.ps1 tunel mysql` | Túnel de MySQL → `127.0.0.1:3307` (dejar la ventana abierta) |
| `.\aws-bdd.ps1 tunel sqlserver` | Túnel de SQL Server → `127.0.0.1,1434` (dejar la ventana abierta) |
| `.\aws-bdd.ps1 apagar mysql` / `apagar sqlserver` | Apaga esa base; la EC2 solo se apaga si ya no queda ninguna base encendida |
| `.\aws-bdd.ps1 apagar` | Apaga las dos bases y la EC2 |

> 💡 Enciende **solo la base que necesitas**. Las horas gratuitas de RDS se comparten entre las dos instancias: con las dos encendidas se consumen el doble de rápido.

---

## 1. Preparación (una sola vez por computadora)

### 1.1 Acceso a la cuenta
- **Dueño de la cuenta:** Mateo. Él envía la invitación desde *Projects → Curiosity Unlocked → Invite new team member*.
- Acepta la invitación que llega a tu correo e inicia sesión. Debes ver el proyecto **Curiosity Unlocked**.

### 1.2 Instalar las herramientas
En PowerShell:
```powershell
winget install --id Amazon.AWSCLI -e
winget install --id Amazon.SessionManagerPlugin -e
```
Si `winget` falla, usa los instaladores:
[AWS CLI](https://awscli.amazonaws.com/AWSCLIV2.msi) y
[Session Manager Plugin](https://s3.amazonaws.com/session-manager-downloads/plugin/latest/windows/SessionManagerPluginSetup.exe).

También necesitas **[DBeaver Community](https://dbeaver.io/download/)**, que sirve para las dos bases.

**Cierra todas las terminales y VS Code**, abre una terminal nueva y comprueba:
```powershell
aws --version                    # aws-cli/2.x
session-manager-plugin --version # un número de versión
```

### 1.3 Permitir que PowerShell ejecute el script
Solo una vez:
```powershell
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
```

### 1.4 Iniciar sesión en AWS
Desde la carpeta `BDD_NUBE_AWS` del repo:
```powershell
cd <ruta-del-repo>\BDD_NUBE_AWS
.\aws-bdd.ps1 login
```
Se abre el navegador: elige la cuenta **Curiosity Unlocked** y autoriza. Luego comprueba:
```powershell
.\aws-bdd.ps1 estado
```
Si muestra la EC2 y las dos bases, ya estás listo. La sesión dura varios días; cuando expire, el script te pedirá repetir `login`.

### 1.5 Conexiones en DBeaver
Las contraseñas te las pasa Mateo **por mensaje privado**. Nunca van en el repo ni en el chat grupal.

**MySQL** — *Database → New Database Connection → **MySQL***
| Campo | Valor |
|---|---|
| Server Host / Port | `127.0.0.1` / **`3307`** (no 3306: ese es tu MySQL local) |
| Database | *vacío* |
| Username / Password | `admin` / *(privada)* |
| Driver properties | `allowPublicKeyRetrieval` = `true`, `useSSL` = `false` |
| Connection name | **`AWS grupo03 MySQL (tunel)`** |

**SQL Server** — *Database → New Database Connection → **SQL Server***
| Campo | Valor |
|---|---|
| Host / Port | `127.0.0.1` / **`1434`** |
| Database/Schema | `master` |
| Authentication | **SQL Server Authentication** |
| Username / Password | `admin` / *(privada)* |
| ✅ **Trust Server Certificate** | marcado (si no, falla por el certificado) |
| ✅ **Show all databases** | marcado |
| Connection name | **`AWS grupo03 SQL Server (tunel)`** |

En los dos casos, si DBeaver pide descargar el driver, acepta. Luego haz *Test Connection* y *Finish*.

> Los nombres con "AWS" sirven para no confundir estas conexiones con tus bases locales.

---

## 2. Rutina de trabajo (cada vez)

```powershell
cd <ruta-del-repo>\BDD_NUBE_AWS
```

**1. Avisa en el grupo** qué base vas a usar.

**2. Revisa el estado:**
```powershell
.\aws-bdd.ps1 estado
```

**3. Si está apagada, enciende solo la que necesitas.** Tarda entre 5 y 15 minutos:
```powershell
.\aws-bdd.ps1 encender mysql        # o: encender sqlserver
```

**4. Abre el túnel en una ventana de PowerShell aparte:**
```powershell
.\aws-bdd.ps1 tunel mysql           # o: tunel sqlserver
```
Cuando aparezca **`Waiting for connections...`**, **deja esa ventana abierta** mientras trabajes. Para usar las dos bases a la vez, abre cada túnel en su propia ventana.

**5. Conéctate** con DBeaver o ejecuta tu proyecto (sección 4).

**Comprobar que estás en la nube y con la hora correcta:**

MySQL:
```sql
SELECT @@hostname, @@version, @@time_zone, NOW();
-- ip-172-31-..., 8.4.x, America/Bogota, hora de Ecuador
```

SQL Server:
```sql
SELECT @@SERVERNAME, SERVERPROPERTY('Edition'), GETDATE(), SYSDATETIMEOFFSET();
-- ..., Express Edition, hora de Ecuador, ...-05:00
```

---

## 3. Cargar o modificar datos

### 3.1 Ejecutar un script `.sql` en DBeaver
1. *File → Open File...* → el script.
2. En el **selector de conexión activa** (arriba del editor), elige la conexión **AWS** correcta: MySQL o SQL Server.
3. Ejecuta el **script completo** con **`Alt + X`**. (`Ctrl + Enter` ejecuta solo la sentencia donde está el cursor).
4. Revisa abajo que no haya errores en rojo.

> ⚠️ Los scripts `01_crea_bd.sql` **borran la base** antes de crearla: `DROP DATABASE`.
> - Si lo ejecutas en una conexión **local**, borras tu base local.
> - Si lo ejecutas en una de **AWS**, borras los datos que está usando todo el grupo.
>
> **Avisa en el grupo antes de volver a ejecutar un `01_...`.**

### 3.2 MySQL — base para un proyecto nuevo
1. Ejecuta `01_crea_bd.sql` y luego `02_carga_datos.sql`.
2. Crea el usuario de la aplicación con host **`'%'`**, no `'localhost'`. En AWS la conexión llega desde la EC2:
   ```sql
   CREATE USER IF NOT EXISTS 'miapp_app'@'%' IDENTIFIED BY 'clave_segura';
   GRANT SELECT, INSERT, UPDATE ON miapp.* TO 'miapp_app'@'%';
   FLUSH PRIVILEGES;
   ```
3. Cosas a tener en cuenta:
   - **Mayúsculas en los nombres de tablas:** la base corre en Linux y las distingue. Usa en el código exactamente el mismo nombre que en el script.
   - **Triggers / procedures / functions:** RDS necesita un ajuste extra para crearlos. Si un proyecto los necesita, avisa a Mateo.

### 3.3 SQL Server — base para un proyecto nuevo
1. Conéctate a la base **`master`**.
2. Ejecuta `01_crea_bd.sql` y luego `02_carga_datos.sql` con **`Alt + X`**.
3. **`GO` separa lotes.** Después de `CREATE DATABASE` debe ir un `GO` antes de `USE` / `CREATE TABLE`; si no, las tablas no se crean bien. Los scripts de Eureka .NET ya lo traen. DBeaver respeta `GO` al ejecutar con `Alt + X`.
4. Crea el login y el usuario de la aplicación. En SQL Server no existe el `'%'` de MySQL: el script local sirve tal cual (`00_usuario_local.sql`), solo cambia la clave:
   ```sql
   USE master;
   GO
   CREATE LOGIN miapp_app WITH PASSWORD = 'clave_segura', DEFAULT_DATABASE = miapp;
   GO
   USE miapp;
   GO
   CREATE USER miapp_app FOR LOGIN miapp_app;
   ALTER ROLE db_datareader ADD MEMBER miapp_app;
   ALTER ROLE db_datawriter ADD MEMBER miapp_app;
   GO
   ```
5. Verifica:
   ```sql
   SELECT name FROM sys.databases;
   ```
6. Restricciones de RDS:
   - `admin` **no es `sysadmin`**. Puede crear bases, logins y usuarios, pero no puede usar comandos de servidor como `sp_configure`, `RESTORE` desde disco o `xp_cmdshell`.
   - La **collation UTF-8** de los scripts (`Latin1_General_100_CI_AI_SC_UTF8`) funciona, porque es SQL Server 2022.

### 3.4 Copias de seguridad
AWS hace una copia automática diaria de cada base y la guarda 1 día. Antes de un cambio grande, exporta lo importante con DBeaver: clic derecho en la tabla o la base → *Export Data*.

---

## 4. Conectar los proyectos

Con el túnel abierto, el proyecto ve la base en `127.0.0.1`. Solo cambian el host, el puerto y el usuario de la app.

### MySQL (túnel en 3307)
**Java (`db.properties`, que no se sube al repo):**
```properties
db.url=jdbc:mysql://127.0.0.1:3307/eurekabank?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Guayaquil
db.usuario=eureka
db.clave=<clave del usuario de la app>
```
**.NET (MySqlConnector):**
```
Server=127.0.0.1;Port=3307;Database=eurekabank;User ID=eureka;Password=<clave>;SslMode=None;AllowPublicKeyRetrieval=True
```

### SQL Server (túnel en 1434)
**.NET (`conexion.json` / `appsettings.json` / `Web.config`):**
```
Data Source=127.0.0.1,1434;Initial Catalog=eurekabank;User ID=eureka;Password=<clave>;Encrypt=True;TrustServerCertificate=True
```
En SQL Server, el puerto va **después de una coma**, no de dos puntos.

**Java (driver `mssql-jdbc`):**
```
jdbc:sqlserver://127.0.0.1:1434;databaseName=eurekabank;encrypt=true;trustServerCertificate=true
```

### Notas
- `serverTimezone=America/Guayaquil` en Java es correcto: lo resuelve Java.
- Si el proyecto no conecta (*Communications link failure*, *network-related error*, *timeout*), casi siempre **el túnel de esa base está cerrado**.
- **Cuando el servidor se despliegue dentro de la EC2:** ya no se usa túnel, y se apunta directo al endpoint con el puerto real.
  - MySQL: `grupo03-mysql.cduuooaemmbx.us-east-2.rds.amazonaws.com:3306`
  - SQL Server: `grupo03-mssql.cduuooaemmbx.us-east-2.rds.amazonaws.com,1433`

---

## 5. Cerrar todo (para no gastar)

**1.** Detén tu proyecto y desconecta DBeaver (clic derecho en la conexión → *Disconnect*).

**2.** En cada ventana de túnel, presiona **`Ctrl + C`**.

**3.** **Pregunta en el grupo** si alguien más está usando alguna base. Si nadie las usa:
```powershell
.\aws-bdd.ps1 apagar
```
Responde `s`. Esto apaga MySQL, SQL Server y la EC2.

Si otra persona sigue usando una de las bases, apaga solo la tuya. El script deja la EC2 encendida mientras quede alguna base en uso:
```powershell
.\aws-bdd.ps1 apagar sqlserver      # o: apagar mysql
```

**4.** Unos minutos después, verifica:
```powershell
.\aws-bdd.ps1 estado
```
Todo debe decir **`stopped`** y debe salir *"Todo apagado"*.

### Qué cobra AWS y qué no
| Estado | Costo |
|---|---|
| EC2 y/o bases encendidas | Se consumen horas de la capa gratuita / créditos (cada base cuenta por separado) |
| Todo apagado | Solo los discos (8 + 20 + 20 GiB), dentro de la capa gratuita |
| Túnel abierto | Sin costo extra, pero necesita la EC2 y la base encendidas |

### Apagado automático todas las noches (23:00, hora de Ecuador)
AWS tiene una regla poco intuitiva: **una base RDS apagada solo puede estar así 7 días**. Al séptimo día AWS la enciende sola para su mantenimiento, y queda encendida hasta que alguien la apague.

Para que eso, o un olvido, nunca deje algo encendido más de un día, hay tres tareas programadas en AWS (*EventBridge Scheduler*, grupo `grupo03-apagado-nocturno`):

| Tarea | Hora (Ecuador) | Qué hace |
|---|---|---|
| `apagar-mysql` | 23:00 | Apaga `grupo03-mysql` |
| `apagar-sqlserver` | 23:00 | Apaga `grupo03-mssql` |
| `apagar-ec2-puente` | 23:05 | Apaga la EC2 |

- Usan el rol `grupo03-scheduler-apagado`, que **solo puede apagar** esos tres recursos: no puede encender, borrar ni crear nada.
- **Si trabajas pasadas las 23:00**, a esa hora se cortará la conexión. Vuelve a ejecutar `.\aws-bdd.ps1 encender <base>` y luego `tunel`; esa noche no se vuelve a apagar sola.
- Esto es una **red de seguridad**, no un reemplazo: **sigue apagando al terminar**. Si no, todo queda encendido hasta las 23:00.

---

## 6. Reglas del grupo (la invitación da acceso total a la cuenta)

1. **No crear nada nuevo** en AWS (instancias, bases, buckets, etc.) sin hablar con Mateo. Todo consume los créditos de su cuenta.
2. Trabajar siempre en **us-east-2 (Ohio)**.
3. **No borrar ni modificar** estos recursos: la EC2, `grupo03-mysql`, `grupo03-mssql`, el parameter group `grupo03-mysql84`, el rol `grupo03-ec2-ssm`, los grupos de seguridad (`rds-ec2-*`, `ec2-rds-*`, `launch-wizard-1`), el apagado automático (grupo `grupo03-apagado-nocturno` y rol `grupo03-scheduler-apagado`) y el presupuesto *My Zero-Spend Budget*.
4. **No compartir contraseñas** en el repo, en capturas ni en el chat grupal.
5. **Siempre apagar** al terminar, si nadie más está usando la base.

---

## 7. Problemas comunes

| Mensaje / síntoma | Causa | Solución |
|---|---|---|
| `No hay sesion de AWS o ya expiro` / `ExpiredToken` | La sesión venció | `.\aws-bdd.ps1 login` |
| `...la ejecución de scripts está deshabilitada` | Política de PowerShell | Paso 1.3 |
| `aws` o `session-manager-plugin` *no se reconoce* | Terminal abierta antes de instalar | Cierra **todas** las terminales y VS Code y abre una nueva |
| `TargetNotConnected` al abrir el túnel | La EC2 está apagada o recién encendida | `.\aws-bdd.ps1 encender <base>` y espera 1–2 min |
| `El puerto 3307/1434 ya esta en uso` | Ya tienes ese túnel abierto | Usa ese, o ciérralo con `Ctrl + C` |
| MySQL: *Public Key Retrieval is not allowed* | Falta una propiedad del driver | Paso 1.5, `allowPublicKeyRetrieval=true` |
| SQL Server: error de **certificado** / *PKIX* / *SSL* | No está marcado *Trust Server Certificate* | Paso 1.5, o `TrustServerCertificate=True` en la cadena |
| *Communications link failure* / *network-related error* | El túnel de esa base está cerrado | `.\aws-bdd.ps1 tunel <base>` |
| MySQL `Access denied for user` | Clave incorrecta o usuario creado con `'localhost'` | Revisa la clave; el usuario debe tener host `'%'` |
| SQL Server `Login failed for user` | Clave incorrecta, o el login no tiene usuario en la base | Revisa la clave y el paso 3.3.4 |
| SQL Server: las tablas no se crean | Falta `GO` después de `CREATE DATABASE` | Paso 3.3.3 |
| A las 23:00 se corta la conexión | Es el apagado automático nocturno | `.\aws-bdd.ps1 encender <base>` y `tunel` de nuevo |
| Al apagar: *no se puede apagar ahora* | La base está en `backing-up` o `modifying` | Espera unos minutos y repite `apagar` |
| La EC2 no aparece al crear una RDS | El asistente solo lista EC2 **encendidas** | `aws ec2 start-instances --region us-east-2 --instance-ids i-083d8c63fe17abd80` |
| En la consola web "no hay recursos" | Región equivocada | Cambia a **us-east-2 (Ohio)** |

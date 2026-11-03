# Guía: diagramas UML de EurekaBank en PowerDesigner (GR03)

Cómo se generaron los diagramas de casos de uso de los TG.1.1–TG.1.4 automatizando PowerDesigner, y cómo continuar con
los diagramas de actividad, secuencia, clases y arquitectura.

## Pedir los diagramas que faltan

> Lee `HERRAMIENTAS_MANUALES/instrucciones/GUIA_DIAGRAMAS_POWERDESIGNER.md` y continúa con los diagramas de actividad,
> secuencia, clases y arquitectura empezando por TG.1.1.

## 1. Contexto del proyecto

- Sistema bancario de **ventanilla**: el único actor es **Empleado (cajero)**, que inicia sesión y opera sobre cualquier
  cuenta. Los 4 clientes (consola, escritorio, web, móvil) son especializaciones de Empleado.
- Alcance: RF-01 Iniciar sesión, RF-02 Cerrar sesión, RF-03 Consultar cuenta, RF-04 Consultar movimientos,
  RF-05 Depósito, RF-06 Retiro, RF-07 Transferencia. No hay creación de cuentas ni de clientes.
- Casos de uso (iguales en los 4 TG):
  - Principales: ECUD1–ECUD7 = RF-01–RF-07.
  - Incluidos: ECUD8 Validar token de sesión, ECUD9 Validar datos de la operación, ECUD10 Registrar movimiento transaccional.
  - Extendidos (errores): ECUD11 credenciales incorrectas (AUTH-001/002), ECUD12 sesión inválida o expirada (AUTH-003),
    ECUD13 cuenta inexistente o inactiva (CTA-001/002), ECUD14 fondos insuficientes (MOV-002),
    ECUD15 transferencia inválida (MOV-003/004), ECUD16 falla de la base de datos (SYS-001).

| TG | Tecnología | BD | Servidor |
|---|---|---|---|
| TG.1.1 | SOAP Java (JAX-WS, Payara 7, Java 21) | MySQL 26.7 local | `04. SERVIDOR/WS_EUREKA_JAVA_SOAP` |
| TG.1.2 | SOAP .NET (WCF, .NET Framework 4.8, IIS) | SQL Server Express local | `04. SERVIDOR/WS_EUREKA_DOTNET_SOAP` |
| TG.1.3 | REST Java (Jakarta REST, Payara 7) | MySQL 26.7 local | `04. SERVIDOR/WS_EUREKA_JAVA_REST` |
| TG.1.4 | REST .NET (ASP.NET Core, base TI.1.4) | por confirmar | **no implementado todavía** |

Fuentes de verdad de cada TG: ERS (`01. UML/01 ERS`), ECUD (`01. UML/02 ECUD`), código del servidor y scripts de
`03. BDD` (10 tablas: usuario, empleado, asignado, sucursal, logsession, cliente, moneda, cuenta, tipomovimiento,
movimiento). Los diagramas deben reflejar el **código real**: paquetes `ec.edu.monster.controladores`, `servicios`,
`datos` (DAO), `modelo`, `seguridad` y `vista`.

Detalles que se deben respetar:

- **SOAP:** token en la cabecera SOAP (`<token>`); errores como SOAP Fault con `ErrorServicio` (codigo, mensaje);
  servicios `AutenticacionWS`, `CuentaWS` y `TransaccionWS`.
- **REST:** endpoints bajo `/api`, token en `Authorization: Bearer`, errores en JSON con HTTP 401 (AUTH), 500 (SYS) y 400 (resto).
- **Transacciones:** bloqueo de fila (`FOR UPDATE` en MySQL, `WITH (UPDLOCK, ROWLOCK)` en SQL Server) con commit/rollback.
- **Cliente móvil:** "Android con Java (modelo, controlador y servicio) y Kotlin con Jetpack Compose (vista)", nunca "solo Kotlin".
- No hacer commits. No modificar `temp/` (es referencia y está en `.gitignore`).

## 2. Lo que ya está hecho: casos de uso

En cada TG:

```
<TG>/<ESPACIO>.sws        SOAP_BDDLOCAL_JAVA · SOAP_BDDLOCAL_DOTNET · RESTFUL_BDDLOCAL_JAVA · RESTFUL_BDDLOCAL_DOTNET
<TG>/01. UML/03 DIAGRAMAS/01_CASOS_USO/
    01_CASOS_USO.prj
    CU_General_EurekaBank_<Tec>.oom/.png    solo los 7 casos críticos
    CU_Iniciar_Sesion · CU_Cerrar_Sesion · CU_Consultar_Cuenta · CU_Consultar_Movimientos
    CU_Registrar_Deposito · CU_Registrar_Retiro · CU_Registrar_Transferencia   (.oom/.png con sus include/extend)
```

Los títulos llevan el sufijo de la tecnología (por ejemplo "ECUD7: REGISTRAR TRANSFERENCIA – SOAP JAVA"). El estilo
visual es el de TI.1.1.

Para regenerarlos (sobrescribe los cambios hechos a mano):

```bash
bash HERRAMIENTAS_MANUALES/herramientas/powerdesigner/gen_casos_uso_todos.sh       # los 4 TG
bash HERRAMIENTAS_MANUALES/herramientas/powerdesigner/gen_tg.sh "<carpeta TG>" <ESPACIO> <modelo_general> "<título>" "<sufijo>"
```

## 3. Herramientas (`herramientas/powerdesigner/`)

| Archivo | Uso |
|---|---|
| `cu_criticos.vbs` | Diagrama general: Empleado, 4 clientes y los 7 casos críticos. |
| `cu_detalle.vbs` | Diagrama a partir de una especificación (`ACTOR`, `CU`, `ASOC`, `DEP`, `RECT`, `TITULO`). Base reutilizable para otros tipos de diagrama. |
| `generar_specs.py` | Genera `specs/spec_<n>.txt` con los include/extend de cada caso crítico dispuestos en arco. |
| `finalizar_oom.py` | Cambia el título del límite del sistema y asigna GUID nuevos a lo heredado de la plantilla. |
| `exportar.vbs` | Reabre el modelo y exporta el PNG (al reabrir se recalculan las etiquetas de los enlaces). |
| `crear_prj.py` | Crea un `.prj` a partir del de TI.1.1 con la lista de modelos indicada. |
| `verificar_prj.vbs` | Abre el `.prj` y el `.sws` en PowerDesigner y lista sus modelos. |
| `gen_tg.sh` / `gen_casos_uso_todos.sh` | Orquestan todo lo anterior para un TG o para los 4. |
| `generar_specs_da.py` | Genera `specs/actividad/<clave>/DA_*.txt` (los 7 diagramas de actividad) en una cuadrícula de filas y columnas: Cliente, columna de errores, flujo principal y BD. |
| `da_detalle.vbs` | Diagrama de actividad a partir de un spec (`AREA`, `CALLE`, `INI`, `FIN`, `ACT`, `DEC`, `FLU`). Las calles se configuran antes de los nodos. |
| `finalizar_da.py` | Mueve los flujos después de las calles en el XML (si no, las calles los tapan) y asigna GUID nuevos. |
| `exportar_diagrama.vbs` | Exporta el PNG del diagrama por defecto de cualquier modelo. |
| `agregar_sws.py` | Agrega o reemplaza un proyecto en el `.sws` sin quitar los demás. |
| `gen_da.sh` / `gen_da_tg.sh` | Generan los diagramas de actividad de un TG; `gen_da_tg.sh` también crea `02_ACTIVIDAD.prj` y lo agrega al `.sws`. |
| `generar_specs_ds.py` | Genera `specs/secuencia/<clave>/DS_*.txt`: participantes y mensajes en orden (llamada, retorno `R` o reflexivo); las posiciones se calculan. |
| `ds_detalle.vbs` | Diagrama de secuencia a partir de un spec (`PART`, `MSG`, `MARCO`): líneas de vida con la paleta de TI.1.1 y mensajes. |
| `finalizar_ds.py` | Agrega las activaciones en el XML (no se pueden crear por COM; se abren con una llamada y se cierran con el retorno), recalcula los extremos de los mensajes con la x real de cada línea de vida, alarga las líneas de vida y asigna GUID nuevos. |
| `gen_ds.sh` / `gen_ds_tg.sh` | Generan los diagramas de secuencia de un TG **sin PNG** en la carpeta del TG (la imagen de revisión queda en `tmp/revision`); `gen_ds_tg.sh` también crea `03_SECUENCIA.prj` y lo agrega al `.sws`. |
| `extraer_clases_java.py` | Lee el código Java del servidor (`ec.edu.monster`, sin `pruebas`) y genera `specs/clases/<modelo>.txt`: paquetes, clases con estereotipo, atributos y operaciones, asociaciones (campos, incluidos `@Inject`) y dependencias (`<<create>>`, `<<throw>>`, `<<use>>`, `<<HandlerChain>>`). |
| `dc_detalle.vbs` | Crea el diagrama de clases desde ese spec y acomoda las clases por paquete en columnas según su tamaño real. Los parámetros se crean antes del nombre para que PowerDesigner acepte las sobrecargas. |
| `finalizar_dc.py` | Dibuja el recuadro y el nombre de cada paquete en el XML y asigna GUID nuevos. |
| `gen_dc_tg.sh` | Hace todo para un TG: `.oom` sin PNG en el TG (revisión en `tmp/revision`), `04_CLASES.prj` y su entrada en el `.sws`. |
| `ea_detalle.vbs` | Diagrama de arquitectura (EAM) desde `specs/arquitectura/<clave>.txt` (`TITULO`, `AREA` anidadas, `APP`, `SRV`, `COMP`, `BD`, `ENLACE`), con la paleta de TI.1.1. |
| `finalizar_ea.py` | Anida cada símbolo en el `<c:SubSymbols>` de su área (si no, los nombres de las áreas no salen arriba y las áreas tapan los enlaces), deja los enlaces al final, pone los textos del título y asigna GUID nuevos. |
| `gen_ea_tg.sh` | Hace todo para un TG: `.eam` sin PNG en el TG (revisión en `tmp/revision`), `05_ARQUITECTURA.prj` y su entrada en el `.sws`. `crear_prj.py` ya distingue `.oom` y `.eam` por la extensión. |
| `extraer_clases_cs.py` | Versión C# del extractor (TG.1.2 y TG.1.4): propiedades como atributos, `static readonly`/`const`, atributos `[ServiceContract]`, `[DataContract]` y `[Protegido]`, e interfaces con su realización (`REAL`). Se usa con `gen_dc_tg.sh ... cs`. |

Los `.vbs` están en UTF-16 con BOM a propósito; si se editan, hay que mantener esa codificación.

## 4. Lo que falta

Para cada TG, crear los proyectos `02_ACTIVIDAD`, `03_SECUENCIA`, `04_CLASES` y `05_ARQUITECTURA` dentro de
`<TG>/01. UML/03 DIAGRAMAS/<proyecto>/<proyecto>.prj` y agregarlos al `.sws` existente, sin quitar `01_CASOS_USO`.
Seguir la organización de los trabajos guía: `temp/.../GR04` y `GR08` (`01. UML/03. DIAGRAMAS`) y nuestros TI.1.x.

Plantillas de PowerDesigner 16.1 en TI.1.1 (`TI.1.1.../01. UML/03 DIAGRAMAS/`), para copiar como base:

| Proyecto | Plantilla |
|---|---|
| 02_ACTIVIDAD | `02_ACTIVIDAD/Diagrama_Actividad_Conversor.oom` |
| 03_SECUENCIA | `03_SECUENCIA/DS_02_INGRESO_SISTEMA.oom` (y los otros `DS_*`) |
| 04_CLASES | `04_CLASES/WS_ConUni_DC.oom` |
| 05_ARQUITECTURA | `05_ARQUITECTURA/Arquitectura.eam` y `WS_ConUni_DA.eam` (modelo EAM) |

Contenido sugerido (confirmarlo con el grupo antes de generar en masa):

- **Actividad:** un `DA_<Caso>.oom` por caso crítico (7), con particiones Cliente / Servidor / BD y las decisiones de error del ECUD.
  **Hecho en TG.1.1** con:
  `bash gen_da_tg.sh "<carpeta TG>" SOAP_BDDLOCAL_JAVA TG11 "Payara 7 / JAX-WS SOAP" MySQL`.
  Los textos de `generar_specs_da.py` son de SOAP Java; para los otros TG hay que ajustar los nombres de clases,
  el protocolo (REST: códigos HTTP en lugar de SOAP Fault) y la BD.
- **Secuencia:** `DS_<nn>_<CASO>.oom` por caso crítico, más los de error (401/400/500 en REST; SOAP Fault en SOAP).
  Las líneas de vida son las clases reales (por ejemplo Vista → Controlador → proxy SOAP/REST → `CuentaWS` o recurso →
  `ServicioCuenta` → `CuentaDao` → BD).
  **Hecho en TG.1.1** (13 diagramas: DS_01–DS_07 casos principales y DS_08–DS_13 errores de ECUD11–ECUD16) con:
  `bash gen_ds_tg.sh "<carpeta TG>" SOAP_BDDLOCAL_JAVA TG11`. Por pedido del grupo, en secuencia solo se entregan los
  `.oom` (sin PNG). Los clientes de TG.1.1 aún no tienen código: sus líneas de vida son genéricas (`*_VISTA`,
  `*_CONTROLADOR`, `CLIENTE_WS_EUREKA`). PowerDesigner reajusta el marco de la interacción a las líneas de vida al abrir
  el modelo, así que el texto del último lazo reflexivo puede sobresalir un poco.
- **Clases:** un diagrama del servidor por paquetes, a partir del código real (TG.1.4 cuando exista su servidor).
  **Hecho en TG.1.1** (`WS_EUREKA_JAVA_SOAP_DC.oom`, 32 clases en 6 paquetes) con:
  `bash gen_dc_tg.sh "<carpeta TG>" SOAP_BDDLOCAL_JAVA "<carpeta TG>/04. SERVIDOR/WS_EUREKA_JAVA_SOAP/src/main/java" WS_EUREKA_JAVA_SOAP_DC`.
  Sirve igual para TG.1.3 (Java). Para .NET (TG.1.2 y TG.1.4) hace falta otro extractor.
- **Arquitectura:** los 4 clientes, el servidor por capas, la BD y el protocolo (SOAP/REST), en EAM como en TI.1.1.
  **Hecho en TG.1.1** (`Arquitectura_EurekaBank_SOAP_Java.eam`) con:
  `bash gen_ea_tg.sh "<carpeta TG>" SOAP_BDDLOCAL_JAVA TG11 Arquitectura_EurekaBank_SOAP_Java`. Para otro TG se copia
  `specs/arquitectura/TG11.txt` y se cambian textos (protocolo, servidor, BD y nombres de clases).

Empezar por TG.1.1, generar **un** diagrama de muestra, mostrar el PNG y esperar la aprobación antes de seguir.

### TG.1.2 (SOAP .NET) — hecho

Mismos proyectos que TG.1.1 en `SOAP_BDDLOCAL_DOTNET.sws`, con el perfil `dotnet` de `generar_specs_da.py` y
`generar_specs_ds.py`. El perfil pasa los métodos del servidor a PascalCase y cambia `ManejadorAutenticacion` por un
`IDispatchMessageInspector` (`AfterReceiveRequest`). También usa `SqlTransaction` (`BeginTransaction`, `Commit` y
`Rollback`), `WITH (UPDLOCK, ROWLOCK)`, `SqlConnection.Open()`, `FaultException<ErrorServicio>` y `BD_SQLSERVER`.

```bash
T="TG.1.2. PAO202651_EUREKA_BDDLOCAL_SOAP_DOTNET_NRC36884_GR03"
bash gen_da_tg.sh "$T" SOAP_BDDLOCAL_DOTNET TG12 "IIS / WCF SOAP · .NET Framework 4.8" "SQL Server Express" dotnet
bash gen_ds_tg.sh "$T" SOAP_BDDLOCAL_DOTNET TG12 dotnet
bash gen_dc_tg.sh "$T" SOAP_BDDLOCAL_DOTNET "$T/04. SERVIDOR/WS_EUREKA_DOTNET_SOAP" WS_EUREKA_DOTNET_SOAP_DC cs
bash gen_ea_tg.sh "$T" SOAP_BDDLOCAL_DOTNET TG12 Arquitectura_EurekaBank_SOAP_DotNet
```

### TG.1.3 (REST Java) — hecho

Mismos proyectos en `RESTFUL_BDDLOCAL_JAVA.sws`, con el perfil `rest` de `generar_specs_da.py` y `generar_specs_ds.py`.
El perfil cambia las peticiones SOAP por rutas HTTP bajo `/api` con `Authorization: Bearer`. `ManejadorAutenticacion`
pasa a ser un `ContainerRequestFilter` enlazado con `@Protegido`, que en los errores usa `abortWith`. Los recursos JAX-RS
reciben DTO de solicitud, y los errores salen como JSON `ErrorServicio` (401 AUTH, 400 datos, 500 SYS) desde
`MapeadorExcepciones`. En actividad, cada rama de error muestra su código HTTP. `extraer_clases_java.py` reconoce
`@interface`, `@Path` (estereotipo Resource), `@Provider`, `@ApplicationPath` y `@Protegido`.

```bash
T="TG.1.3. PAO202651_EUREKA_BDDLOCAL_RESTFUL_JAVA_NRC36884_GR03"
bash gen_da_tg.sh "$T" RESTFUL_BDDLOCAL_JAVA TG13 "Payara 7 / Jakarta REST · JSON" MySQL rest
bash gen_ds_tg.sh "$T" RESTFUL_BDDLOCAL_JAVA TG13 rest
bash gen_dc_tg.sh "$T" RESTFUL_BDDLOCAL_JAVA "$T/04. SERVIDOR/WS_EUREKA_JAVA_REST/src/main/java" WS_EUREKA_JAVA_REST_DC java
bash gen_ea_tg.sh "$T" RESTFUL_BDDLOCAL_JAVA TG13 Arquitectura_EurekaBank_REST_Java
```

### TG.1.4 (REST .NET) — hecho

Mismos proyectos en `RESTFUL_BDDLOCAL_DOTNET.sws`, con el perfil `restnet`, que aplica `rest` y después las equivalencias
de ASP.NET Core y ADO.NET. Los controladores `[ApiController]` reemplazan a los recursos; en secuencia se llaman
`*_CONTROLADOR_API` para no confundirlos con los controladores del cliente. `ManejadorAutenticacion` es un
`IAuthorizationFilter` (`OnAuthorization`) que se activa con `[Protegido]` (`TypeFilterAttribute`) y guarda la sesión en
`HttpContext.Items`. `ManejadorErrores` (`IExceptionFilter`) responde los errores en JSON. También se usan
`SqlTransaction`, `UPDLOCK` y `BD_SQLSERVER`. `extraer_clases_cs.py` reconoce `[ApiController]`, los filtros
`AuthorizationFilter`/`ExceptionFilter`, la dependencia `<<TypeFilter>>` y los parámetros con atributos (`[FromBody(...)]`).

```bash
T="TG.1.4. PAO202651_EUREKA_BDDLOCAL_RESTFUL_DOTNET_NRC36884_GR03"
bash gen_da_tg.sh "$T" RESTFUL_BDDLOCAL_DOTNET TG14 "Kestrel / ASP.NET Core 8 · JSON" "SQL Server Express" restnet
bash gen_ds_tg.sh "$T" RESTFUL_BDDLOCAL_DOTNET TG14 restnet
bash gen_dc_tg.sh "$T" RESTFUL_BDDLOCAL_DOTNET "$T/04. SERVIDOR/WS_EUREKA_DOTNET_REST" WS_EUREKA_DOTNET_REST_DC cs
bash gen_ea_tg.sh "$T" RESTFUL_BDDLOCAL_DOTNET TG14 Arquitectura_EurekaBank_REST_DotNet
```

### MER: conceptual, lógico y físico (`02. MER`)

Se generan desde el script de creación de cada BD: `<proyecto>/02. MER/MER_Conceptual_<sistema>.cdm`,
`MER_Logico_…ldm`, `MER_Fisico_…pdm` y `MER.prj` con todos los modelos `MER_*` de la carpeta (agregado al `.sws` como
proyecto `MER`). Un proyecto con varias BD (EX.1.1) tiene un juego de tres modelos por BD en el mismo `MER.prj`. Los PNG
de revisión quedan en `tmp/revision`.

| Archivo | Uso |
|---|---|
| `perfiles_mer.py` | Lo que el script no dice, por perfil (`eureka`, `banquito`, `comercializadora`): descripción de tablas y columnas (`'TABLA.COLUMNA'` o `'COLUMNA'`), posición de cada tabla en la cuadrícula y verbo de cada FK. Para otra BD se agrega un perfil. |
| `generar_specs_mer.py` | Lee el script (MySQL o T-SQL) y escribe `specs/mer/<clave>.txt`: tablas, columnas, PK/UQ, índices, CHECK (los `IN (...)` como lista de valores; los de una columna en la columna y los de varias en la tabla) y FK. |
| `mer_detalle.vbs` | Construye el físico por COM (`app.CreateModel(KIND_PDM, "\|DBMS=…")`), genera el lógico y el conceptual con `m.GenerateModel(Nothing, kind, "", True)`, pone los verbos (en relaciones y en enlaces de asociación) y acomoda los tres diagramas en la cuadrícula del spec. |
| `finalizar_mer.py` | Recalcula el rectángulo de cada enlace con sus puntos (si no, la imagen sale con espacio vacío arriba). |
| `limpiar_sws.py` | Quita de los `.sws` de TG y EX los modelos MER sueltos que PowerDesigner agrega al cerrarse. |
| `gen_mer_tg.sh` | Hace todo para una BD. `crear_prj.py` ya reconoce `.cdm`, `.ldm` y `.pdm`. |

Los tres modelos usan los nombres del script (tablas y columnas); la descripción legible va como comentario.

```bash
S="03. BDD/init/01_crea_bd.sql"
bash gen_mer_tg.sh "TG.1.1. PAO202651_EUREKA_BDDLOCAL_SOAP_JAVA_NRC36884_GR03" SOAP_BDDLOCAL_JAVA TG11 "MySQL 5.0" "EurekaBank SOAP Java" EurekaBank_SOAP_Java "$S" eureka
bash gen_mer_tg.sh "TG.1.2. PAO202651_EUREKA_BDDLOCAL_SOAP_DOTNET_NRC36884_GR03" SOAP_BDDLOCAL_DOTNET TG12 "Microsoft SQL Server 2008" "EurekaBank SOAP .NET" EurekaBank_SOAP_DotNet "$S" eureka
bash gen_mer_tg.sh "TG.1.3. PAO202651_EUREKA_BDDLOCAL_RESTFUL_JAVA_NRC36884_GR03" RESTFUL_BDDLOCAL_JAVA TG13 "MySQL 5.0" "EurekaBank REST Java" EurekaBank_REST_Java "$S" eureka
bash gen_mer_tg.sh "TG.1.4. PAO202651_EUREKA_BDDLOCAL_RESTFUL_DOTNET_NRC36884_GR03" RESTFUL_BDDLOCAL_DOTNET TG14 "Microsoft SQL Server 2008" "EurekaBank REST .NET" EurekaBank_REST_DotNet "$S" eureka
E="EX.1.1. PAO202651_BANQUITO_CELULARES_RESTFUL_JAVA_NRC36884_GR03"
bash gen_mer_tg.sh "$E" BANQUITO_CELULARES_RESTFUL_JAVA EX11_BANQUITO "MySQL 5.0" "BanQuito REST Java" BanQuito_REST_Java "03. BDD/banquito/01_crea_bd.sql" banquito
bash gen_mer_tg.sh "$E" BANQUITO_CELULARES_RESTFUL_JAVA EX11_COMERCIALIZADORA "MySQL 5.0" "Comercializadora REST Java" Comercializadora_REST_Java "03. BDD/comercializadora/01_crea_bd.sql" comercializadora
```

**Hecho en los 4 TG y en EX.1.1.** TG.1.3 tiene el mismo esquema que TG.1.1 (MySQL) y TG.1.4 el mismo que TG.1.2 (SQL
Server). En EX.1.1, el conceptual de la comercializadora convierte `DETALLE_FACTURA` (su PK son solo las dos FK) en una
asociación N:M entre FACTURA y TELEFONO; los enlaces de asociación están en `md.AssociationsLinks` y el verbo va en `Role`.

En SQL Server el spec agrega el nombre de cada DEFAULT (`df_…`), que se guarda en el atributo extendido
`ExtDeftConstName`. El perfil SQL Server 2008 sí escribe en el script generado los CHECK, el UNIQUE y los DEFAULT con
nombre.

- PowerDesigner tiene una sola regla de validación por tabla: si una tabla tiene varios CHECK de varias columnas
  (FACTURA), se unen con `and` y el nombre de los siguientes va como comentario.
- Índices: `t.Indexes.CreateNew()` y, por columna, `ix.IndexColumns.CreateNew()` con `Set ic.Column = columna`.
- No sirve la ingeniería inversa del script: el perfil MySQL 5.0 de PowerDesigner no entiende `CONSTRAINT x PRIMARY KEY`
  y crea columnas falsas.
- Al crear una referencia, PowerDesigner empareja la FK con la columna hija del mismo código y, si no existe, crea otra
  (`chr_emplcreacuenta`, `chr_cuenreferencia`); el script reapunta el join y borra la columna creada.
- Los modelos generados pasan los códigos a mayúsculas (`FK_CUENTA_CLIENTE`) y los scripts pueden venir en
  mayúsculas o minúsculas: las claves internas (celdas, verbos) se guardan y se comparan con `LCase`.
- `ClassKind`: CDM `509178224`, LDM `1598421368`, PDM `-840675807`.
- Verificado generando el SQL desde el `.pdm` (`GetPackageOptions`, `GenerationPathName` con `\` final y
  `GenerateDatabase`): coincide con el script salvo que el perfil MySQL 5.0 no escribe los CHECK (quedan en el modelo)
  y escribe el UNIQUE como `key`.
- Cada ejecución hace que PowerDesigner guarde al cerrar el último `.sws` que cargó: revisar `git status` y restaurar
  los `.sws` de otros TG que aparezcan modificados.

### EX.1.1 (BanQuito y Comercializadora de celulares, REST Java) — casos de uso

Otro dominio: dos aplicaciones REST, el Core BanQuito (módulo de crédito) y la Comercializadora de celulares.
Los diagramas salen del ERS y del ECUD de EX.1.1, contrastados con el código de los dos servidores.
`generar_specs_cu_ex.py` crea 20 specs en `specs/casos_uso/EX11`: dos diagramas generales (ilustraciones 1 y 2 del
ECUD) y uno por cada ECUD1–ECUD18. `gen_cu_specs.sh` los genera, sin PNG en el proyecto, y crea `01_CASOS_USO.prj` y
`BANQUITO_CELULARES_RESTFUL_JAVA.sws`.

```bash
python -I generar_specs_cu_ex.py && python -I verificar_cruces_cu.py specs/casos_uso/EX11/*.txt
bash gen_cu_specs.sh "EX.1.1. PAO202651_BANQUITO_CELULARES_RESTFUL_JAVA_NRC36884_GR03" BANQUITO_CELULARES_RESTFUL_JAVA EX11 [modelo ...]
```

Decisiones de construcción:
- Actores (ERS 8.2.3):
  - Empleado (ACT-001), con los 4 clientes como especializaciones (ACT-002 a 005).
  - Comercializadora `<<sistema>>` (ACT-006), único actor del Core en ECUD13–16.
  - Core BanQuito `<<sistema>>` (ACT-007), actor secundario de RF-07, 08, 09, 10 y 12.
- Solo `<<include>>`. El ECUD no define casos extendidos: los errores son flujos alternativos. El campo "extendidos"
  de ECUD13–18 solo anota quién los incluye.
- ECUD17 (token) lo incluyen ECUD2–12 y ECUD18 (clave de API) lo incluyen ECUD13–16.
- Las inclusiones entre sistemas (ECUD7 → 13 y 14; ECUD10 → 13, 14 y 15; ECUD12 → 16) se dibujan dentro de un
  segundo límite, "CORE BANQUITO" (líneas `CAJA` del spec, que agrega `finalizar_cu.py`).
- En ECUD8, 9 y 12 el Core aparece como actor secundario. Esos casos usan operaciones de apoyo del Core que no son
  casos de uso del ECUD: simulación, buscar cliente por cédula y créditos del cliente (`PasarelaBanquito`).
- ECUD17 no está en el diagrama general (lo incluyen 11 casos). Se muestra en cada detalle y en su propio diagrama.
- Las disposiciones en arco (diagrama general y ECUD17) se eligieron con `verificar_cruces_cu.py`, para que ninguna
  línea cruce otra elipse.
- `cu_detalle.vbs` acepta `GEN|hijo|padre` y un estereotipo opcional en `ACTOR`.

Inconsistencia del ECUD: en ECUD2 los incluidos dicen "N/A". Pero ECUD17 dice que lo incluyen ECUD2 a ECUD12, y el
código protege `DELETE /sesiones/actual` con `@Protegido`. El diagrama sigue a ECUD17 y al código.

### EX.1.1 — actividad, secuencia, clases y arquitectura

Ningún proyecto deja PNG: las imágenes de revisión quedan en `tmp/revision`, como en los commits de los TG.
`gen_da.sh` ya no deja PNG en el proyecto. `gen_da_tg.sh` y `gen_ds_tg.sh` aceptan `-` para usar specs ya generados y
arman el `.prj` con todos los specs de la clave. `gen_dc_tg.sh` incluye en `04_CLASES.prj` todos los modelos de la
carpeta.

```bash
T="EX.1.1. PAO202651_BANQUITO_CELULARES_RESTFUL_JAVA_NRC36884_GR03"; W=BANQUITO_CELULARES_RESTFUL_JAVA
python -I generar_specs_da_ex.py && bash gen_da_tg.sh "$T" $W EX11 - -
python -I generar_specs_ds_ex.py && bash gen_ds_tg.sh "$T" $W EX11 -
bash gen_dc_tg.sh "$T" $W "$T/04. SERVIDOR/WS_BANQUITO_JAVA_REST/src/main/java" WS_BANQUITO_JAVA_REST_DC java
bash gen_dc_tg.sh "$T" $W "$T/04. SERVIDOR/WS_COMERCIALIZADORA_JAVA_REST/src/main/java" WS_COMERCIALIZADORA_JAVA_REST_DC java
bash gen_ea_tg.sh "$T" $W EX11 Arquitectura_BanQuito_Comercializadora_REST_Java
```

- Actividad: un diagrama por ECUD1–16. `generar_specs_da_ex.py` describe cada caso como una lista de pasos
  (`C`, `S`, `D`, `B`, `K`, `R`) y los ubica en la cuadrícula. Los carriles son Cliente y Servidor, más BD comercializadora
  y Core BanQuito si el caso los usa. En ECUD13–16 los carriles son Comercializadora, Core y BD banquito. Cada rama de
  error muestra su código HTTP. Si el flujo vuelve al cliente a mitad del caso (ECUD12), se cierra antes un bloque de
  error. Los nombres de actividad no se pueden repetir en un modelo.
- Secuencia: DS01–12 de la Comercializadora (el Core es una sola línea de vida, `CORE_BANQUITO`, a la que llama
  `PASARELA_BANQUITO` con X-Api-Key) y DS13–16 del Core (actor Comercializadora, `FILTRO_CLAVE_API`, `SERVICIO_CREDITO`,
  DAO y `BD_BANQUITO`). Los errores DS17–21 son AUTH-003, AUTH-004 → EXT-001, Core sin respuesta, CRE-001 y SYS-001.
- Clases: un modelo por servidor en el mismo `04_CLASES.prj`.
- Arquitectura: los 4 clientes, el dominio de Payara 7 con los dos servicios por capas y MySQL 26.7 con las dos BD. En
  EAM tampoco se pueden repetir nombres de componentes.

## 5. Técnica de PowerDesigner (lecciones aprendidas)

- PowerDesigner 16.1 está en `C:\Program Files (x86)\Sybase\PowerDesigner 16`. Se automatiza con **VBScript** usando el
  `cscript` de **32 bits**: `C:\Windows\SysWOW64\cscript.exe //nologo script.vbs`, con
  `CreateObject("PowerDesigner.Application")`. Desde PowerShell no se pueden leer las propiedades de los símbolos.
- Los `.vbs` deben guardarse en **UTF-16 LE con BOM**; si no, las tildes salen mal.
- En VBScript, `And` no corta la evaluación y los nombres de variables no distinguen mayúsculas.
- Flujo que funcionó:
  1. Copiar la plantilla de TI.1.1 del mismo tipo y abrirla con `app.OpenModel(copia)`. Al inicio, cerrar todos los
     modelos abiertos (`app.Models.Item(i).Close 1`).
  2. Vaciar el diagrama conservando el rectángulo y el título. Al borrar un símbolo también se borran sus enlaces, así
     que hay que repetir un ciclo que borre el primer símbolo sobrante hasta que no quede ninguno. Después borrar los
     objetos de cada colección con `Do While coll.Count > 0: coll.Item(0).Delete`.
  3. Crear los objetos con las colecciones del modelo (`m.Actors.CreateNew()`, `m.UseCases.CreateNew()`,
     `m.Dependencies`, `m.Generalizations`, `m.UseCaseAssociations`; para actividad y secuencia hay que buscar las
     colecciones equivalentes, eso aún no está probado). Dibujarlos con `d.AttachObject(obj)` y posicionarlos con
     `sym.Rect = app.NewRect(izq, arriba, der, abajo)`. Para que no cambien de tamaño, poner antes
     `sym.AutoAdjustToText = False` y `sym.ManuallyResized = True` dentro de `On Error Resume Next`.
  4. Enlaces: `Set ls = d.AttachLinkObject(link)`, `ls.CornerStyle = 0` (línea recta) y `ls.ListOfPoints` con una lista
     de `app.NewPtList`/`app.NewPoint` que vaya del **centro** del origen al centro del destino; PowerDesigner la recorta
     en los bordes. Extremos: en una dependencia, `Object1` = origen y `Object2` = destino; en una generalización,
     `Object1` = hijo y `Object2` = padre; en una asociación de casos de uso, `Object1` = caso y `Object2` = actor.
  5. Guardar con `m.Save ruta` y cerrar con `m.Close 1`.
  6. El texto del título no se puede cambiar por COM (`TextSymbol.Text` falla): se cambia en el XML del archivo
     guardado reemplazando `<a:Text>…</a:Text>`.
  7. Asignar GUID nuevos: reemplazar en el XML los `ObjectID` (y el `ID` del encabezado) que coincidan con los de la
     plantilla (`finalizar_oom.py`).
  8. Exportar la imagen con un script aparte que **reabra** el modelo: `diagrama.ExportImage ruta.png`.
- Entre cada ejecución hay que esperar a que termine `pdshell16.exe` (puede tardar unos 3 minutos). Si no, aparece
  "Error catastrófico" o archivos bloqueados. Reintentar hasta 3 veces y validar que el `.oom` pese más de 20 KB.
- **Proyecto `.prj`:** copiar el de TI.1.1 del mismo tipo, ponerle un GUID nuevo, quitar el `<c:Symbols>` de su
  ProjectDiagram y reescribir `<c:ModelsAndFiles>` con un `<o:ModelDocument>` por modelo: `Location`
  `file:///%[PRJ_ROOT_DIR]%/<archivo>`, `ModelDocument.ModelID` = ObjectID del modelo (el de `<o:Model Id="o2">`) y
  `ModelClassID` `18112060-1A4B-11D1-83D9-444553540000` para OOM. Para EAM, tomar el `ModelClassID` del `.prj` de
  `05_ARQUITECTURA` de TI.1.1. `crear_prj.py` hace esto para OOM.
- **Espacio de trabajo `.sws`:** una línea `<Model ID="{GUID del .prj}" Name="<proyecto>"
  Type="{FF4B6538-9856-42CF-B9B9-304ED370CEAA}" URL="01. UML/03 DIAGRAMAS/<proyecto>/<proyecto>.prj"/>` por proyecto.
- **Verificación:** abrir el `.prj` con `app.OpenModel` y revisar `p.ModelsAndFiles.Count`; cargar el `.sws` con
  `app.ActiveWorkspace.Load ruta` (`verificar_prj.vbs`). Revisar visualmente cada PNG.
- En bash, usar siempre `rm -f "${VAR:?}/..."` (la verificación de seguridad bloquea `rm` con variables sin protección).
- Con muchos enlaces, evitar que las líneas crucen otros símbolos; por ejemplo, disponer los elementos en arco alrededor
  del principal, como en los casos de uso.
- No renombrar los `.oom` desde el explorador de Windows: el `.prj` los busca por nombre de archivo.

## 6. Al terminar

Resumir por TG qué proyectos y archivos se crearon (`.oom`/`.eam` y `.png`), confirmar que se verificó la apertura en
PowerDesigner y listar lo pendiente (por ejemplo, TG.1.4 hasta que exista su servidor).

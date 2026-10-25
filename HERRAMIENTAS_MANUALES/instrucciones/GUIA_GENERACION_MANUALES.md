# Guía para generar los manuales técnicos (GR03)

Proceso usado para construir los manuales de **TI.1.2 (SOAP .NET sin BDD)** y **TI.1.4 (RESTful .NET sin BDD)**. Sirve para pedir un manual nuevo sin repetir las instrucciones.

---

## 1. Cómo solicitar un manual nuevo

Basta con un mensaje como este:

> Lee `HERRAMIENTAS_MANUALES/instrucciones/GUIA_GENERACION_MANUALES.md` y construye el manual del proyecto `TI.X.Y. PAO202651_<TEC>_<LENG>_<BDD>_NRC36884_GR03`.

Las herramientas están en `HERRAMIENTAS_MANUALES/herramientas/` (ver la sección 8).

Si no se indica otra cosa, se aplican las **decisiones por defecto** de la sección 3. Solo se preguntará lo que no se pueda deducir del código; por ejemplo, el tipo de despliegue o un cliente que no exista.

---

## 2. Estructura de referencia del manual

La estructura sale del manual de referencia `MANUAL_SOAP_DOTNET_SINBDD_GR5.docx` y la plantilla visual es la del grupo GR03.

**Portada**
- Logo y título: `MANUAL TÉCNICO … CON <SOAP|RESTFUL> SIN|CON BDD EN <JAVA|DOTNET>`.
- Integrantes: CRIOLLO LLUMIQUINGA MATEO GABRIEL, HERNÁNDEZ ALMEIDA CARLOS ORLANDO y REVILLA ANCHAPAXI ANTONIO ADRIAN.
- Tutor: Ing. Eduardo Mauricio Campaña Ortega.
- Fecha.

**Índices**
1. Índice de contenido: Título 1 a Título 3.
2. Índice de ilustraciones: Título 4, una entrada por cada "Figura N".
3. Índice de tablas: Título 5, una entrada por cada "Tabla N".

**Capítulos.** Cada Título 1 empieza en página nueva.

| Nº | Capítulo | Contenido |
|---|---|---|
| 1 | INTRODUCCIÓN | Qué es el sistema, capas, tecnologías, alcance (fase 1) |
| 2 | OBJETIVOS | Objetivo General y Objetivos Específicos |
| 3 | MARCO TEÓRICO | Tecnología (SOAP/WSDL o REST/JSON), SOA, framework del servidor, MVC, servidor de despliegue, MAUI/Blazor y **Requisitos del sistema**. Los requisitos van en dos tablas: RQF → operación o ruta, y código de error → situación o estado HTTP |
| 4 | DESARROLLO DEL APLICATIVO | Servidor, Cliente Consola, Proyecto compartido, Cliente Escritorio, Cliente Móvil, Cliente Web. Cada parte se divide en **Paso N: …** |
| 5 | CONCLUSIONES | Viñetas |
| 6 | RECOMENDACIONES | Viñetas: seguridad, sincronizar contratos, fase 2, persistencia |
| 7 | REFERENCIAS BIBLIOGRÁFICAS | Formato APA, alineadas a la izquierda |

**Convenciones de contenido**
- Cada paso lleva texto explicativo y, cuando aplica, una **Figura N** (captura) o una **Tabla N** (código o datos).
- El código va en una **tabla de una sola celda**, en Calibri 11 de color `4F81BD`, con la leyenda "Tabla N – Nombre del archivo".
- Las clases clave se copian **completas** desde el código real; nunca se inventan.
- Las capturas son **marcadores**: un recuadro gris punteado con el texto `[Insertar captura: …]`, que el equipo sustituye después.

---

## 3. Decisiones por defecto (ya aprobadas)

| Tema | Decisión |
|---|---|
| Capturas | Marcadores `[Insertar captura: …]` |
| Código | Clases clave completas, leídas del proyecto |
| Archivo | Nuevo en `05. DOCUMENTACION/`. Si ya existe `MANUAL_<TEC>_<LENG>_<BDD>_GR03.docx`, se genera con sufijo (p. ej. `_DOTNET`) y el equipo decide si reemplaza el anterior |
| Cliente móvil | MAUI Android. Si el proyecto de escritorio usa `net10.0-android` con `#if ANDROID`, se documenta con el código real (`10.0.2.2` y `usesCleartextTraffic`) |
| Despliegue SOAP .NET | IIS Express en local y luego IIS, puerto **8095** |
| Despliegue REST .NET | Kestrel en local y luego **IIS con el Hosting Bundle**, puerto **8096**, grupo de aplicaciones "Sin código administrado" y permisos para `IIS_IUSRS` |
| Clientes web | Blazor Server: SOAP en el puerto 8097 y REST en el 8098 |
| Credenciales de ejemplo | `MONSTER` / `MONSTER9`, token de 30 minutos |
| Códigos de error | AUTH-001 (token inválido o ausente), AUTH-002 (sesión expirada), AUTH-003 (credenciales incorrectas), UNIT-001 (unidad no soportada), DATA-001 (datos inconsistentes) |

---

## 4. Proceso paso a paso

### Paso 1. Reunir el insumo y leer solo lo necesario
1. Leer la **ERS** (`01. UML/01 ERS/ERS_*.docx`) para obtener los requisitos RQF, los actores, las reglas y los códigos de error.
2. Revisar la estructura de `04. SERVIDOR`, `04. CLICON`, `04. CLIESC` (Shared y Desktop), `04. CLIMOV` y `04. CLIWEB`.
3. **Para ahorrar recursos:** comparar con `diff` el servidor nuevo contra el de un proyecto ya documentado. Las capas de dominio, conversores y servicios suelen ser idénticas, así que solo se leen a fondo los archivos que cambian: controladores, seguridad, errores, `Program.cs`, configuración y clientes.
4. Anotar los puertos, rutas o operaciones, las variables de entorno y los archivos de configuración (`appsettings.json` y `launchSettings.json` en .NET; `web.xml` y `pom.xml` en Java).

### Paso 2. Escribir el contenido (módulo Python)
Se crea `herramientas/contenidos/contenido_<tec>_<leng>_<bdd>.py` copiando el contenido más parecido (SOAP o REST). El módulo tiene:
- `RAIZ`: la carpeta del proyecto, relativa a la raíz del repositorio.
- `construir()`: arma el documento con la clase `Doc`.
- `PORTADA = [(texto_plantilla, texto_nuevo)]`: los reemplazos del título. La portada de la plantilla dice **"CON SOAP SIN BDD EN DOTNET"**. Por ejemplo, para un proyecto RESTful Java con BDD: `[('CON SOAP', 'CON RESTFUL'), ('SIN BDD', 'CON BDD'), ('EN DOTNET', 'EN JAVA')]`.

API de la clase `Doc` (`docxlib.py`):

| Método | Resultado |
|---|---|
| `h1_(texto)` | Título 1 numerado que empieza en página nueva |
| `h2(texto)` | Título 2. Reinicia el contador de pasos |
| `paso_(titulo)` | Título 3 con el texto "Paso N: titulo" |
| `p(texto)` | Párrafo justificado. `**negrita**`, y `` `código` `` se muestra en cursiva |
| `vineta(texto, num=8)` | Viñeta. Para las referencias se usa `num=7` y `jc='left'` |
| `figura_(titulo, captura)` | Leyenda "Figura N" (Título 4) y el recuadro marcador |
| `codigo(titulo, texto)` | Leyenda "Tabla N" (Título 5) y la tabla de código de una celda |
| `datos(titulo, cabecera, filas, anchos)` | Tabla de datos. La suma de `anchos` debe ser **8504** |
| `vacio()` | Párrafo vacío |

El código se lee del proyecto con `leer(ruta)` o `varios(r1, r2…)`, usando la codificación `utf-8-sig`, y nunca se escribe a mano.

### Paso 3. Generar el .docx (`build.py`)
```
cd HERRAMIENTAS_MANUALES/herramientas
python -m pip install -r requirements.txt   # solo la primera vez
python build.py <salida.docx> contenido_<tec>_<leng>_<bdd>
```
Conviene generar fuera del repositorio, por ejemplo en una carpeta temporal o en `salida/` (está ignorada), y copiar el archivo a `05. DOCUMENTACION` al final.

El generador hace lo siguiente sobre `plantilla/plantilla_gr03.docx`, que contiene la portada y los índices del manual GR03 sin el cuerpo:
1. Conserva la portada, los tres índices y el `sectPr` final. Borra todo desde "INTRODUCCIÓN" y los párrafos vacíos de relleno que quedan después del último índice.
2. Aplica los reemplazos de `PORTADA`.
3. Reconstruye los índices con campos **independientes del idioma**:
   - Contenido: `TOC \o "1-3" \h \z \u`
   - Ilustraciones: `TOC \o "4-4" \h \z \u`
   - Tablas: `TOC \o "5-5" \h \z \u`
4. Inserta el XML generado antes del `sectPr`.
5. Elimina las relaciones e imágenes del manual anterior que ya no se usan.
6. Añade los estilos `TDC4` y `TDC5` sin sangría, para que las figuras y tablas no aparezcan desplazadas en sus índices.
7. Añade `w:updateFields` a `settings.xml`, antes de `hdrShapeDefaults`, `footnotePr` o `compat`.
8. Comprime el resultado, con `[Content_Types].xml` como primer archivo.

**Estilos de la plantilla:**
- Títulos: `Ttulo1`, `Ttulo2` y `Ttulo3` con `numId 5` y niveles `ilvl` 0, 1 y 2.
- Leyendas: Figura con `Ttulo4` y Tabla con `Ttulo5`.
- Viñetas: `Prrafodelista` con `numId 8`.
- Referencias: `numId 7`.
- Tablas de código: estilo `a`.

### Paso 4. Validar
```
python <skill docx>/scripts/office/validate.py salida.docx
```
El resultado debe ser "All validations PASSED!".

### Paso 5. Actualizar los índices y exportar a PDF con Word (COM)
LibreOffice no está instalado, así que se usa Word desde PowerShell:
```
pwsh -File actualizar_indices.ps1 -Docx <salida.docx> -Pdf
```
El script:
1. Actualiza los tres índices dos veces.
2. Muestra las entradas de cada índice y el número de páginas.
3. Guarda el documento y lo exporta a PDF si se pasa `-Pdf`.
4. Quita `w:updateFields` de `settings.xml` para que Word no pregunte al abrir.

Después se vuelve a validar.

### Paso 6. Revisión visual ligera
Se generan las imágenes de las páginas con `pdftoppm -r 50` (incluido en MiKTeX) y una **única hoja de contacto** de unas 12 páginas con PIL. En ella se revisan la portada, los índices, un capítulo de teoría, un paso con código, una tabla de datos y las referencias. Se confirma el número de entradas de cada índice y que coincida con el número de figuras y tablas.

### Paso 7. Entrega
- Copiar el archivo a `05. DOCUMENTACION/` sin tocar los documentos existentes.
- Informar el número de páginas, figuras y tablas, y las entradas de cada índice.
- Avisar de los restos de otras versiones en la ERS o en la presentación, como "Java Swing" o "EN JAVA".

---

## 5. Problemas encontrados y su solución

| Problema | Solución |
|---|---|
| Índices de figuras o tablas vacíos ("No se encontraron entradas") | El modificador `\t "Heading 4,1"` falla en Windows es-EC, porque el separador de listas es `;` y los nombres de estilo están localizados. Se usa `\o "4-4"` y `\o "5-5"` |
| Entradas con sangría en los índices de figuras y tablas | Estilos `TDC4` y `TDC5` con `w:ind w:left="0"` |
| `\u` dentro de una cadena Python se interpreta como escape Unicode | Usar cadenas `r'...'` |
| `python -I` no encuentra `python-docx` o `lxml` | Instalar con `python -m pip install lxml python-docx` y ejecutar sin `-I` desde la carpeta del generador |
| `soffice.py` falla (AF_UNIX o LibreOffice ausente) | Usar Word COM (Paso 5) |
| Página en blanco entre los índices y la Introducción | Borrar los párrafos vacíos de relleno y usar `pageBreakBefore` en el Título 1 |
| Comillas escapadas dentro de una f-string | Calcular el `rPr` antes, fuera de la f-string |

---

## 6. Estado de los proyectos

| Proyecto | Manual | Observaciones |
|---|---|---|
| TI.1.1 SOAP Java sin BDD | Existente (Java, plantilla original) | — |
| TI.1.2 SOAP .NET sin BDD | **Generado** (≈102 págs., 37 figuras, 62 tablas) | WCF .NET Framework 4.8, IIS en el puerto 8095, token en la cabecera SOAP. La ERS aún dice "EN JAVA" |
| TI.1.3 RESTful Java sin BDD | Existente (Java) | — |
| TI.1.4 RESTful .NET sin BDD | **Generado** (≈98 págs., 36 figuras, 60 tablas) | ASP.NET Core .NET 8, IIS con Hosting Bundle en el puerto 8096, token Bearer. La ERS menciona "Java Swing" y la presentación tiene un nombre de SOAP Java |
| TI.2.1 a TI.2.4 (con BDD) | Pendientes | Hay que añadir al marco teórico el motor de BDD, y al Servidor pasos para el script de creación, la cadena de conexión, la capa de acceso a datos o repositorios, y una tabla con el esquema de tablas y campos. En las recomendaciones se quita "persistencia" |

---

## 7. Lista de verificación final

- [ ] La portada tiene la tecnología y el lenguaje correctos (por ejemplo, "CON RESTFUL SIN BDD EN DOTNET").
- [ ] Los tres índices tienen entradas y los números de Figura y Tabla son consecutivos.
- [ ] Todo el código copiado coincide con el proyecto: rutas, puertos y nombres.
- [ ] Los requisitos y códigos de error coinciden con la ERS.
- [ ] Cada cliente existente está documentado, y si una carpeta está vacía se indica en el manual.
- [ ] El documento pasa la validación OOXML.
- [ ] El manual anterior no se modificó y el nuevo está en `05. DOCUMENTACION`.

---

## 8. Herramientas (`HERRAMIENTAS_MANUALES/herramientas/`)

| Archivo | Función |
|---|---|
| `build.py` | Genera el .docx a partir de la plantilla y un módulo de contenido |
| `docxlib.py` | Clase `Doc` con los títulos, pasos, figuras, tablas de código y datos y viñetas en el formato de la plantilla |
| `contenidos/contenido_soap_dotnet_sinbdd.py` | Contenido del manual TI.1.2 |
| `contenidos/contenido_restful_dotnet_sinbdd.py` | Contenido del manual TI.1.4 |
| `plantilla/plantilla_gr03.docx` | Portada, índices y estilos del manual GR03, sin el cuerpo |
| `actualizar_indices.ps1` | Actualiza los índices con Word, exporta a PDF y limpia `updateFields` |
| `requirements.txt` | Dependencia `lxml` |

Para regenerar un manual ya hecho, por ejemplo después de cambiar el código del proyecto:
```
python build.py ../salida/MANUAL_RESTFUL_DOTNET_SINBDD_GR03.docx contenido_restful_dotnet_sinbdd
pwsh -File actualizar_indices.ps1 -Docx ../salida/MANUAL_RESTFUL_DOTNET_SINBDD_GR03.docx
```
Al regenerar se pierden las capturas que se hayan pegado en el Word. Por eso se regenera solo antes de insertar las capturas, o se copian luego los cambios a mano.

# Casos de uso de ApiPaisesMVC_SQLServer (Taller 2.2)

ApiPaisesMVC_SQLServer es una aplicación web ASP.NET Core MVC que consulta países en la API pública
REST Countries, los guarda en SQL Server con Entity Framework Core y permite gestionarlos. Los casos
CU-01 a CU-08 son los mismos del Taller 2.1 (Java); CU-09 y CU-10 corresponden a la actividad
avanzada.

## Actores

| Actor | Tipo | Descripción |
|-------|------|-------------|
| Usuario | Principal | Persona que usa la aplicación desde el navegador. |
| API REST Countries | Secundario (sistema externo) | Proveedor de datos de países (v5, requiere API key). |
| API de universidades (Hipolabs) | Secundario (sistema externo) | Proveedor de universidades por país. |
| Base de datos SQL Server | Secundario (sistema) | Guarda países y universidades en `ApiPaisesDB`. |

## Diagrama general

```mermaid
flowchart LR
    U([Usuario])
    API[[API REST Countries]]
    UNI[[API Universidades]]
    BD[(SQL Server)]

    subgraph Sistema ApiPaisesMVC_SQLServer
        CU01((CU-01 Importar países))
        CU02((CU-02 Buscar país por nombre en la API))
        CU03((CU-03 Registrar país))
        CU04((CU-04 Listar / buscar países))
        CU05((CU-05 Actualizar país))
        CU06((CU-06 Eliminar país))
        CU07((CU-07 Filtrar por región))
        CU08((CU-08 Generar reportes))
        CU09((CU-09 Importar universidades))
        CU10((CU-10 Consultar universidades))
        INC1((Consumir API))
        INC2((Validar país))
    end

    U --- CU01 & CU02 & CU03 & CU04 & CU05 & CU06 & CU07 & CU08 & CU09 & CU10
    CU01 -. include .-> INC1
    CU02 -. include .-> INC1
    CU03 -. include .-> INC2
    CU05 -. include .-> INC2
    INC1 --- API
    CU09 --- UNI
    CU01 & CU02 & CU03 & CU04 & CU05 & CU06 & CU07 & CU08 & CU09 & CU10 --- BD
```

## Relación con las actividades del taller

| Caso de uso | Actividad del taller |
|-------------|----------------------|
| CU-01 | Base del taller (botón "Importar Información", secciones 14 a 16) |
| CU-02 | Actividad 1: consumir la API para un país específico |
| CU-03 a CU-06 | Actividad 2: CRUD completo (crear, leer, actualizar, eliminar) |
| CU-04 | Actividad 3: búsquedas por nombre, región y capital |
| CU-07 | Actividad 4: filtros dinámicos (América, Europa, Asia, África, Oceanía) |
| CU-08 | Actividad 5: reportes 1, 2 y 3 |
| CU-09 y CU-10 | Actividad avanzada: API de universidades y entidad `Universidad` |

## CU-01 Importar países desde la API

| | |
|---|---|
| Actor | Usuario |
| Precondición | La API key está configurada y SQL Server está disponible. |
| Postcondición | La tabla `Paises` tiene todos los países de la API, sin duplicados. |

#### Flujo principal
1. El usuario pulsa "Importar desde la API" y, si ya hay países guardados, confirma en el diálogo.
2. El sistema consulta la API página por página, con hasta 100 países en cada una.
3. El sistema convierte cada país del JSON (Newtonsoft.Json) y descarta los que llegan incompletos (sin nombre, código o región).
4. El sistema guarda los países en una sola transacción: inserta los nuevos y actualiza los que ya existen con el mismo código ISO.
5. El sistema muestra cuántos países recibió, cuántos son nuevos y cuántos actualizó.

#### Flujos alternos
- 2a. La API no responde, tarda demasiado o rechaza la clave: el sistema muestra el motivo y no modifica la base.
- 4a. Falla la base de datos: el sistema deshace la transacción y muestra el error.

## CU-02 Buscar país por nombre en la API

| | |
|---|---|
| Actor | Usuario |
| Precondición | Ninguna. |
| Postcondición | El usuario ve las coincidencias y, si lo decide, guarda una. |

#### Flujo principal
1. El usuario escribe un nombre (por ejemplo "ecuador") y pulsa "Buscar", o elige una de las búsquedas de ejemplo.
2. El sistema valida que el nombre no esté vacío.
3. El sistema consulta la API, que busca en el nombre común, el oficial, los alternativos y los nativos.
4. El sistema muestra las coincidencias con capital, región, subregión, población, área y moneda, e indica cuáles ya están guardadas.
5. Opcional: el usuario pulsa "Guardar en SQL Server" (o "Actualizar datos" si ya estaba guardado). El sistema lo inserta o, si el código ya existe, lo actualiza.

#### Flujos alternos
- 2a. El nombre está vacío: el sistema pide escribirlo y no consulta la API.
- 3a. La API no está disponible: el sistema busca en la base local y avisa que esos datos son locales (fallback).
- 4a. No hay coincidencias: el sistema informa que no encontró países.

## CU-03 Registrar país

| | |
|---|---|
| Actor | Usuario |
| Precondición | SQL Server está disponible. |
| Postcondición | El país queda guardado con un id nuevo. |

#### Flujo principal
1. El usuario pulsa "Nuevo país" y completa código, nombre, capital, región, subregión, población, área y moneda en el panel lateral.
2. El sistema valida los datos (incluye *Validar país*).
3. El sistema verifica que el código no exista.
4. El sistema guarda el país, cierra el panel y abre la página de la tabla donde quedó el país, resaltado.

#### Flujos alternos
- 2a. Hay datos no válidos: el sistema muestra todos los errores juntos y conserva lo que el usuario escribió.
- 3a. El código ya existe: el sistema informa el conflicto.

#### Validar país (include)
- Código: obligatorio, 3 letras (ISO alfa-3).
- Nombre: obligatorio.
- Región: una de África, América, Antártida, Asia, Europa u Oceanía.
- Población: número entero, no negativo (acepta separadores de miles).
- Área: opcional, número no negativo en km².
- Nombre, capital, subregión y moneda: máximo 100 caracteres.

## CU-04 Listar y buscar países

| | |
|---|---|
| Actor | Usuario |
| Precondición | SQL Server está disponible. |
| Postcondición | Ninguna (solo lectura). |

#### Flujo principal
1. El usuario abre la pantalla de países.
2. El sistema muestra los países ordenados por nombre en una tabla paginada de 10 en 10.
3. El usuario puede cambiar de página, elegir cuántas filas ver (10, 25 o 50) u ordenar por otra columna.
4. Opcional (Actividad 3): el usuario elige buscar por nombre, capital o región, escribe un texto y pulsa "Buscar".
5. El sistema muestra los países que coinciden. Al buscar por nombre también acepta el código, y al buscar por región acepta el nombre de la región o la subregión.
6. El usuario pulsa el lápiz de un país y ve su detalle en el panel lateral.

#### Flujos alternos
- 2a. No hay países: el sistema muestra un mensaje y sugiere importarlos.
- 5a. No hay coincidencias: el sistema lo informa y ofrece limpiar la búsqueda.

## CU-05 Actualizar país

| | |
|---|---|
| Actor | Usuario |
| Precondición | El país existe. |
| Postcondición | El país tiene los datos nuevos. |

#### Flujo principal
1. El usuario abre el país en el panel lateral y modifica sus datos.
2. El sistema valida los datos (incluye *Validar país*).
3. El sistema verifica que el código no pertenezca a otro país.
4. El sistema guarda los cambios, cierra el panel y resalta el país en la tabla.

#### Flujos alternos
- 1a. El usuario intenta salir con cambios sin guardar: el sistema pide confirmar que se descartan.
- 2a. Hay datos no válidos: el sistema muestra los errores.
- 3a. El código es de otro país: el sistema informa el conflicto.
- 4a. El país ya no existe porque otro usuario lo borró: el sistema lo informa.

## CU-06 Eliminar país

| | |
|---|---|
| Actor | Usuario |
| Precondición | El país existe. |
| Postcondición | El país ya no está en la base. |

#### Flujo principal
1. El usuario pulsa la papelera de un país en la tabla, o "Eliminar" en el panel lateral.
2. El sistema pide confirmación mostrando el código y el nombre, con el foco en "Cancelar".
3. El usuario confirma y el sistema elimina el país.

#### Flujos alternos
- 3a. El usuario cancela: no se elimina nada.
- 3b. El país ya no existe: el sistema lo informa.

## CU-07 Filtrar países por región

| | |
|---|---|
| Actor | Usuario |
| Precondición | SQL Server está disponible. |
| Postcondición | Ninguna (solo lectura). |

#### Flujo principal
1. El sistema muestra un chip por región con la cantidad de países guardados en cada una (Todas, África, América, Antártida, Asia, Europa, Oceanía).
2. El usuario elige una región.
3. El sistema muestra los países de esa región, ordenados por nombre y desde la primera página. El filtro se combina con la búsqueda de CU-04.

#### Flujos alternos
- 3a. No hay países en la región: el sistema lo informa y ofrece ver todas las regiones.

## CU-08 Generar reportes

| | |
|---|---|
| Actor | Usuario |
| Precondición | SQL Server está disponible. |
| Postcondición | Ninguna (solo lectura). |

#### Flujo principal
1. El usuario abre "Reportes".
2. El sistema calcula con la base de datos:
   - Reporte 1: el número de países por región, de mayor a menor, con un gráfico de barras;
   - Reporte 2: los cinco países más poblados (el primero es el país más poblado);
   - Reporte 3: el promedio poblacional por región, con un gráfico de barras;
   - además, el total de países y la región con más países, informando si hay empates.
3. El sistema muestra cada reporte con su consulta SQL equivalente.

#### Flujos alternos
- 2a. No hay países: el sistema indica que no hay datos para el reporte.

## CU-09 Importar universidades de un país

| | |
|---|---|
| Actor | Usuario |
| Precondición | SQL Server está disponible. |
| Postcondición | La tabla `Universidades` tiene las universidades del país, sin duplicados. |

#### Flujo principal
1. El usuario escribe un país en inglés (por defecto "Ecuador") y pulsa "Importar universidades".
2. El sistema consulta `http://universities.hipolabs.com/search?country={país}`.
3. El sistema toma de cada universidad el nombre, el primer dominio, la primera página web y el país.
4. El sistema guarda en una transacción: inserta las nuevas y actualiza las que ya existen (mismo nombre y país).
5. El sistema muestra cuántas recibió, cuántas son nuevas y cuántas actualizó.

#### Flujos alternos
- 1a. El país está vacío: el sistema pide escribirlo.
- 2a. La API no responde: el sistema muestra el motivo y no modifica la base.
- 2b. La API no tiene universidades para ese país: el sistema sugiere escribirlo en inglés.

## CU-10 Consultar universidades

| | |
|---|---|
| Actor | Usuario |
| Precondición | SQL Server está disponible. |
| Postcondición | Ninguna (solo lectura). |

#### Flujo principal
1. El usuario abre "Universidades".
2. El sistema muestra las universidades guardadas (nombre, dominio, sitio web y país) en una tabla paginada, con un chip por país que indica cuántas tiene.
3. Opcional: el usuario elige un país y el sistema muestra solo sus universidades.
4. Opcional: el usuario escribe un texto y pulsa "Buscar". El sistema muestra las universidades cuyo nombre, dominio o sitio web lo contienen, dentro del país elegido si hay uno.

#### Flujos alternos
- 2a. No hay universidades: el sistema sugiere importarlas.
- 4a. No hay coincidencias: el sistema lo informa y ofrece limpiar la búsqueda.

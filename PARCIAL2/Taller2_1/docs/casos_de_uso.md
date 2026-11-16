# Casos de uso — ApiPaisesMVC (Taller 2.1)

Sistema de escritorio en Java que consulta países en la API pública REST Countries,
los guarda en MySQL y permite gestionarlos.

## Actores

| Actor | Tipo | Descripción |
|-------|------|-------------|
| Usuario | Principal | Persona que opera la aplicación de escritorio. |
| API REST Countries | Secundario (sistema externo) | Proveedor de datos de países (v5, requiere API key). |
| Base de datos MySQL | Secundario (sistema) | Almacena los países (`api_paises.pais`). |

## Diagrama general

```mermaid
flowchart LR
    U([Usuario])
    API[[API REST Countries]]
    BD[(MySQL)]

    subgraph Sistema ApiPaisesMVC
        CU01((CU-01 Importar países))
        CU02((CU-02 Buscar país por nombre en la API))
        CU03((CU-03 Registrar país))
        CU04((CU-04 Listar / consultar países))
        CU05((CU-05 Actualizar país))
        CU06((CU-06 Eliminar país))
        CU07((CU-07 Buscar por región))
        CU08((CU-08 Generar reporte estadístico))
        INC1((Consumir API))
        INC2((Validar país))
    end

    U --- CU01 & CU02 & CU03 & CU04 & CU05 & CU06 & CU07 & CU08
    CU01 -. include .-> INC1
    CU02 -. include .-> INC1
    CU03 -. include .-> INC2
    CU05 -. include .-> INC2
    INC1 --- API
    CU01 & CU02 & CU03 & CU04 & CU05 & CU06 & CU07 & CU08 --- BD
```

## Relación con las actividades del taller

| Caso de uso | Actividad del taller |
|-------------|----------------------|
| CU-01 | Base del taller (botón "Importar países") |
| CU-02 | Actividad 1: consulta de un país por nombre |
| CU-03 a CU-06 | Actividad 2: CRUD (crear, leer, actualizar, eliminar) |
| CU-04 | Actividad 3: mostrar los países en un JTable |
| CU-07 | Actividad 4: búsqueda por región |
| CU-08 | Actividad 5: reportes estadísticos |

---

## CU-01 Importar países desde la API

| | |
|---|---|
| **Actor** | Usuario |
| **Precondición** | La API key está configurada y MySQL está disponible. |
| **Postcondición** | La tabla `pais` tiene todos los países de la API, sin duplicados. |

**Flujo principal**
1. El usuario pulsa **Importar países**.
2. El sistema consulta la API página por página (hasta 100 países por página).
3. El sistema convierte cada país del JSON y descarta los incompletos (sin nombre, código o región).
4. El sistema guarda los países en una sola transacción: inserta los nuevos y actualiza los que ya existen (mismo código ISO).
5. El sistema muestra cuántos países recibió, cuántos son nuevos y cuántos actualizó.

**Flujos alternos**
- 2a. La API no responde, tarda demasiado o rechaza la clave: el sistema muestra el motivo y no modifica la base.
- 4a. Falla la base de datos: el sistema deshace la transacción y muestra el error.

## CU-02 Buscar país por nombre en la API

| | |
|---|---|
| **Actor** | Usuario |
| **Precondición** | Ninguna. |
| **Postcondición** | El usuario ve las coincidencias y, si lo elige, una queda guardada. |

**Flujo principal**
1. El usuario escribe un nombre (p. ej. "ecuador") y pulsa **Buscar**.
2. El sistema valida que el nombre no esté vacío.
3. El sistema consulta la API (busca en nombre común, oficial, alternativos y nativos).
4. El sistema muestra las coincidencias.
5. Opcional: el usuario elige un resultado y pulsa **Guardar**. El sistema lo inserta o, si el código ya existe, lo actualiza.

**Flujos alternos**
- 2a. El nombre está vacío: el sistema pide ingresarlo y no consulta la API.
- 3a. La API no está disponible: el sistema busca en la base local y avisa que los datos son locales (fallback).
- 4a. No hay coincidencias: el sistema informa que no encontró países.

## CU-03 Registrar país

| | |
|---|---|
| **Actor** | Usuario |
| **Precondición** | MySQL está disponible. |
| **Postcondición** | El país queda guardado con un id nuevo. |

**Flujo principal**
1. El usuario completa código, nombre, capital, región, población y moneda.
2. El sistema valida los datos (incluye *Validar país*).
3. El sistema verifica que el código no exista.
4. El sistema guarda el país y lo muestra en la lista.

**Flujos alternos**
- 2a. Hay datos no válidos: el sistema muestra **todos** los errores juntos.
- 3a. El código ya existe: el sistema informa el conflicto.

**Validar país (include)**
- Código: obligatorio, 3 letras (ISO alfa-3).
- Nombre: obligatorio.
- Región: una de África, América, Antártida, Asia, Europa u Oceanía.
- Población: número entero, no negativo.
- Nombre, capital y moneda: máximo 100 caracteres.

## CU-04 Listar / consultar países

| | |
|---|---|
| **Actor** | Usuario |
| **Precondición** | MySQL está disponible. |
| **Postcondición** | Ninguna (solo lectura). |

**Flujo principal**
1. El usuario abre la pantalla de países.
2. El sistema muestra todos los países ordenados por nombre en una tabla (JTable).
3. El usuario selecciona un país y ve su detalle.

**Flujos alternos**
- 2a. No hay países: el sistema muestra la tabla vacía y sugiere importar.

## CU-05 Actualizar país

| | |
|---|---|
| **Actor** | Usuario |
| **Precondición** | El país existe. |
| **Postcondición** | El país tiene los datos nuevos. |

**Flujo principal**
1. El usuario selecciona un país y modifica sus datos.
2. El sistema valida los datos (incluye *Validar país*).
3. El sistema verifica que el código no pertenezca a otro país.
4. El sistema guarda los cambios.

**Flujos alternos**
- 2a. Hay datos no válidos: el sistema muestra los errores.
- 3a. El código es de otro país: el sistema informa el conflicto.
- 4a. El país ya no existe (otro usuario lo borró): el sistema lo informa.

## CU-06 Eliminar país

| | |
|---|---|
| **Actor** | Usuario |
| **Precondición** | El país existe. |
| **Postcondición** | El país ya no está en la base. |

**Flujo principal**
1. El usuario selecciona un país y pulsa **Eliminar**.
2. El sistema pide confirmación.
3. El usuario confirma y el sistema elimina el país.

**Flujos alternos**
- 3a. El usuario cancela: no se elimina nada.
- 3b. El país ya no existe: el sistema lo informa.

## CU-07 Buscar países por región

| | |
|---|---|
| **Actor** | Usuario |
| **Precondición** | MySQL está disponible. |
| **Postcondición** | Ninguna (solo lectura). |

**Flujo principal**
1. El usuario elige una región: América, Europa, Asia, África, Oceanía o Antártida.
2. El sistema muestra los países de esa región, ordenados por nombre.

**Flujos alternos**
- 1a. No eligió región: el sistema pide elegir una.
- 2a. No hay países en la región: el sistema muestra la lista vacía.

## CU-08 Generar reporte estadístico

| | |
|---|---|
| **Actor** | Usuario |
| **Precondición** | MySQL está disponible. |
| **Postcondición** | Ninguna (solo lectura). |

**Flujo principal**
1. El usuario abre **Reportes**.
2. El sistema calcula con la base de datos:
   - Número de países por región (de mayor a menor).
   - País más poblado.
   - Región con más países.
3. El sistema muestra el reporte.

**Flujos alternos**
- 2a. No hay países: el sistema indica que no hay datos para el reporte.

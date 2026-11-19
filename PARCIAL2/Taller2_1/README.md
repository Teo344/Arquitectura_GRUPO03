# Taller 2.1: consumo de una API pública con Java, MySQL y MVC

| Carpeta | Contenido |
|---------|-----------|
| `docs/casos_de_uso.md` | Actores, diagrama y especificación de CU-01 a CU-08 |
| `docs/arquitectura.md` | Capas MVC, secuencias, principios SOLID y diferencias con el enunciado |
| `bdd/` | Scripts SQL: usuario de la aplicación y esquema `api_paises` |
| `ApiPaisesMVC/` | Proyecto Maven (JDK 21): vista Swing, controladores, servicio, modelo y pruebas |

## Puesta en marcha

1. Crear la base de datos en MySQL 8.0.19 o superior. Como root, en MySQL Workbench o DBeaver,
   ejecutar `bdd/00_usuario_local.sql` (antes cambiar la clave del usuario) y después
   `bdd/01_esquema_api_paises.sql`.
2. Conseguir una API key. La API v3.1 que usa el enunciado ya fue retirada, y la v5 pide una cuenta
   gratuita en <https://restcountries.com/sign-up> (1000 solicitudes al mes). Importar todos los
   países gasta unas 3 solicitudes.
3. Copiar `ApiPaisesMVC/src/main/resources/config.properties.example` como `config.properties` en
   la misma carpeta y completar `db.clave` y `api.clave`. La clave demo `rc_live_demo` también
   funciona, pero con ella la API devuelve un solo país de muestra.
4. Ejecutar: abrir `ApiPaisesMVC` en NetBeans y pulsar *Run*, o en consola `mvn compile exec:java`.
   La pantalla de inicio indica si MySQL responde y si la clave de la API es propia o la demo.
5. Correr las pruebas con `mvn test`. Usan dobles en memoria, así que no necesitan MySQL ni red.

## Preguntas de análisis

Respuestas a las preguntas del enunciado, aplicadas a lo que se construyó en este proyecto.

### ¿Qué ventajas ofrece MVC respecto a una arquitectura monolítica sin capas?

En un monolito sin capas, el botón "Importar" de la ventana abriría la conexión HTTP, leería el
JSON, armaría el SQL y actualizaría la tabla en el mismo método. Aquí cada parte está en su capa:

- la vista (`PaisesVista`) solo muestra la tabla y el panel lateral;
- el controlador (`PaisControlador`) recibe la acción y convierte el formulario en un `Pais`;
- el modelo y el servicio (`PaisServicio`, `RestCountriesCliente`, `PaisDAOMySQL`) consumen la API,
  validan y guardan.

Eso se notó en el propio taller: el rediseño completo de las pantallas (barra superior, tabla
paginada, panel lateral) se hizo sin tocar el servicio ni el DAO, y las pruebas siguieron pasando.
También se puede probar cada capa por separado: las pruebas del servicio usan un DAO en memoria y
no necesitan MySQL ni internet.

### ¿Por qué se utiliza un DAO en la capa Modelo?

El DAO (Data Access Object) es la única clase que conoce el SQL y la conexión a MySQL. El resto del
sistema trabaja con la interfaz `PaisDAO` (`insertar`, `actualizar`, `listarTodos`,
`buscarPorRegion`, `contarPorRegion`, `listarMasPoblados`...) sin saber cómo se guardan los datos.

En este proyecto eso permitió:

- concentrar en `PaisDAOMySQL` el uso de `PreparedStatement` (sin riesgo de inyección SQL),
  try-with-resources para cerrar siempre las conexiones, y el upsert en lote con
  `INSERT ... ON DUPLICATE KEY UPDATE` dentro de una transacción;
- cambiar la consulta del reporte de "país más poblado" a "los 5 más poblados" tocando solo el DAO y
  el servicio;
- probar el servicio con `PaisDAOEnMemoria`, que implementa la misma interfaz;
- pasar a otra base (por ejemplo, la MySQL del grupo en AWS) cambiando solo `config.properties`.

### ¿Qué ventajas tiene JSON frente a XML en APIs REST? ¿Tiene XML alguna ventaja?

REST Countries solo responde en JSON (el cliente lo pide con `Accept: application/json`), y en este
caso JSON tiene ventajas claras:

- es más liviano: un país con nombre, código, capital, región, población y monedas ocupa menos
  bytes que su versión en XML, y al importar 250 países eso se nota;
- se lee directamente como objetos y listas: `RestCountriesMapper` usa Gson para recorrer
  `data.objects`, `names.common` o la lista `capitals` sin escribir un parser;
- tiene tipos básicos (números, booleanos, `null`), así la población llega como número y la marca
  `primary` de la capital como booleano.

XML sí tiene ventajas en otros contextos: esquemas formales (XSD) para validar documentos,
espacios de nombres, atributos, y estándares empresariales como SOAP, que el grupo usó en los
talleres TG.1.1 y TG.1.2. Para consumir una API pública moderna como esta, JSON es la opción
natural.

### ¿Qué sucede si la API pública no está disponible?

La aplicación sigue funcionando con los datos guardados en MySQL:

- `RestCountriesCliente` usa `HttpClient` con tiempo máximo de conexión y de respuesta (15 segundos
  por defecto), así que una API lenta no congela la ventana. Además, las llamadas se hacen en
  segundo plano con `SwingWorker`.
- Cada error se traduce a un mensaje claro: 401 (clave no válida), 403 (cuota agotada), 429
  (demasiadas consultas), 5xx (servicio caído) o falta de conexión.
- Al importar, si la API falla no se modifica la base; la tabla queda como estaba.
- Al buscar por nombre, `PaisServicio` aplica un fallback: si la API no responde, busca en MySQL y
  la pantalla avisa que los datos son locales.
- La pantalla de inicio comprueba MySQL sin llamar a la API, para no gastar cuota.

Esto pasó de verdad durante el taller: la API v3.1 que usa el enunciado fue retirada y hoy responde
con un aviso de deprecación, por eso el proyecto usa la v5 con API key.

### ¿Cómo se podría implementar una caché local para evitar llamadas repetitivas a la API?

La base MySQL ya funciona como una caché persistente: los países se importan una vez (unas 3
solicitudes de la cuota de 1000 al mes) y luego todas las consultas, filtros y reportes se hacen
contra la base, sin volver a llamar a la API.

Lo que todavía llama a la API cada vez es "Buscar en la API". Para cachear esas búsquedas se puede
agregar un decorador que implemente la misma interfaz:

```java
public final class PaisApiClienteConCache implements PaisApiCliente {
    private final PaisApiCliente real;
    private final Map<String, Entrada> cache = new ConcurrentHashMap<>();
    private final Duration vigencia = Duration.ofHours(24);

    @Override
    public List<Pais> buscarPorNombre(String nombre) {
        String clave = nombre.strip().toLowerCase(Locale.ROOT);
        Entrada guardada = cache.get(clave);
        if (guardada != null && guardada.hasta().isAfter(Instant.now())) {
            return guardada.paises();          // sin llamar a la API
        }
        List<Pais> paises = real.buscarPorNombre(nombre);
        cache.put(clave, new Entrada(paises, Instant.now().plus(vigencia)));
        return paises;
    }
    // obtenerTodos() puede delegar directo: la importación siempre quiere datos frescos
}
```

En `App` se envolvería el cliente real (`new PaisApiClienteConCache(new RestCountriesCliente(...))`)
y ni el servicio ni los controladores cambiarían. Para una caché compartida entre varias
instancias se usaría Redis, o una tabla en MySQL con la fecha de la última consulta.

### ¿Desplegaría esta solución en la nube utilizando Docker y Kubernetes?

Docker sí, pero para la base de datos. La aplicación es de escritorio (Swing), así que la ventana
corre en la PC de cada usuario y no tiene sentido meterla en un contenedor. Lo que sí se puede
contenerizar es MySQL con los scripts de `bdd/`, para que todos los integrantes tengan la misma
versión y el mismo esquema con un `docker compose up`.

Kubernetes no lo usaría para este proyecto. Es un taller académico con pocos usuarios y sin
requisitos de alta disponibilidad, y el propio enunciado indica que en esos casos alcanza con una
opción más simple y barata. De hecho, el grupo ya tiene la base en la nube con una RDS MySQL en AWS.
Kubernetes tendría sentido si la lógica se convirtiera en una API REST con muchos usuarios que
necesite escalar automáticamente.

### ¿Qué principio SOLID se aplica al consumir la API con Java, MySQL y MVC? ¿Cuál es el más importante?

Se aplican los cinco:

- **SRP**: `RestCountriesMapper` solo convierte JSON, `PaisDAOMySQL` solo hace SQL, `PaisServicio`
  solo tiene reglas de negocio y las vistas solo muestran datos.
- **OCP**: otro proveedor de países sería una clase nueva que implemente `PaisApiCliente`, sin
  modificar el servicio.
- **LSP**: `PaisDAOMySQL` y `PaisDAOEnMemoria` son intercambiables para el servicio.
- **ISP**: `PaisApiCliente` tiene solo dos métodos y `ReporteControlador` está separado del CRUD.
- **DIP**: `PaisServicio` recibe `PaisDAO` y `PaisApiCliente` por constructor, y `App` es el único
  lugar que crea las implementaciones concretas.

El más importante para consumir una API pública es DIP. Cuando la v3.1 fue retirada, el cambio a
la v5 (con API key, paginación y otro formato de JSON) quedó encerrado en `RestCountriesCliente` y
`RestCountriesMapper`; el servicio, los controladores y las vistas no se enteraron. Es el mismo
principio que permite probar todo sin red.

### ¿Cómo podría migrarse esta aplicación a una arquitectura de microservicios?

En pasos, reutilizando las capas que ya existen:

1. Convertir la lógica en una API REST (por ejemplo con Jakarta EE en Payara, como en los talleres
   RESTful del grupo): `PaisServicio` y `PaisDAOMySQL` se reutilizan y se exponen endpoints como
   `GET /paises`, `POST /paises/importar` o `GET /reportes`. La aplicación Swing pasa a ser un
   cliente que consume esa API.
2. Separar por responsabilidad: un servicio de países (importación, CRUD y búsquedas, dueño de la
   tabla `pais`) y un servicio de reportes que lee los datos de países.
3. Darle a cada servicio su propia base o esquema y comunicarlos por HTTP o por mensajes.
4. Agregar un API Gateway como punto de entrada único y empaquetar cada servicio en un contenedor
   Docker.

### ¿Qué ventajas y desventajas tiene migrar a microservicios? ¿Sería recomendable para este proyecto?

Ventajas: se podría escalar solo la parte con más carga, desplegar un servicio sin detener los
demás y aislar fallos; si el servicio que importa desde la API cae, las consultas y reportes
siguen respondiendo.

Desventajas: mucha más complejidad. Habría que mantener varios servicios, bases y contenedores, la
comunicación entre ellos, el monitoreo y la seguridad de cada uno. Las pruebas son más difíciles y
la importación, que hoy es una sola transacción en MySQL, pasaría a coordinarse entre servicios.

Para este proyecto no es recomendable. Es una aplicación con un solo módulo de negocio, pocos
usuarios y una sola base, y un MVC bien separado como el actual es más simple de mantener. La
separación en interfaces y servicios que ya tiene deja el camino listo si algún día crece.

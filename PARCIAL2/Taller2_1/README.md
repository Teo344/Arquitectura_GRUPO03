# Taller 2.1 — Consumo de API pública con Java, MySQL y MVC

| Carpeta | Contenido |
|---------|-----------|
| `docs/casos_de_uso.md` | Actores, diagrama y especificación de CU-01 a CU-08 |
| `docs/arquitectura.md` | Capas MVC, secuencias, principios SOLID y diferencias con el enunciado |
| `bdd/` | Scripts SQL: usuario de la aplicación y esquema `api_paises` |
| `ApiPaisesMVC/` | Proyecto Maven (JDK 21): vista Swing, controladores, servicio, modelo y pruebas. |

## Puesta en marcha

1. **Base de datos** (MySQL 8.0.19 o superior), como root en MySQL Workbench:
   ejecutar `bdd/00_usuario_local.sql` (cambiar antes la clave del usuario) y luego
   `bdd/01_esquema_api_paises.sql`.
2. **API key**: la API v3.1 del enunciado fue retirada. La v5 requiere una cuenta
   gratuita en <https://restcountries.com/sign-up> (1000 solicitudes al mes).
   Importar todos los países usa unas 3 solicitudes.
3. **Configuración**: copiar
   `ApiPaisesMVC/src/main/resources/config.properties.example` como
   `config.properties` en la misma carpeta y completar `db.clave` y `api.clave`.
   Con la clave demo `rc_live_demo` todo funciona, pero la API solo devuelve un país de muestra.
4. **Ejecutar**: abrir `ApiPaisesMVC` en NetBeans y pulsar *Run*, o en consola
   `mvn compile exec:java`. La pantalla de inicio muestra si MySQL responde y si la clave de la
   API es propia o la demo.
5. **Pruebas**: `mvn test`. No necesitan MySQL ni red porque usan dobles en memoria.

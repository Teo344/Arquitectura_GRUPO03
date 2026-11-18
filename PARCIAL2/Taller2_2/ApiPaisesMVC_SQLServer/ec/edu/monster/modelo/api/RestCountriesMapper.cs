using System.Text.RegularExpressions;
using ec.edu.monster.modelo.entidad;
using ec.edu.monster.modelo.excepcion;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace ec.edu.monster.modelo.api
{
    /// <summary>
    /// Convierte el JSON de REST Countries v5 en objetos <see cref="Pais"/> con Newtonsoft.Json. Es la
    /// única clase que conoce la estructura de la respuesta; en el enunciado este trabajo lo hacía el
    /// controlador. Estructura esperada (solo los campos que se piden):
    /// <code>
    /// { "data": {
    ///     "objects": [ { "names": {"common": "Ecuador"}, "codes": {"alpha_3": "ECU"},
    ///                    "capitals": [ {"name": "Quito", "attributes": {"primary": true}} ],
    ///                    "region": "Americas", "subregion": "South America",
    ///                    "area": {"kilometers": 276841}, "population": 18000000,
    ///                    "currencies": [ {"code": "USD", "name": "United States dollar"} ] } ],
    ///     "meta": { "total": 249, "more": true } } }
    /// </code>
    /// Los países sin nombre, código ISO o región conocida se descartan (y se registran en el log);
    /// los que no tienen capital, subregión o moneda se aceptan con ese dato vacío.
    /// </summary>
    public sealed class RestCountriesMapper
    {
        private static readonly Regex CodigoIso = new Regex("^[A-Za-z]{3}$");

        private readonly ILogger<RestCountriesMapper>? log;

        public RestCountriesMapper(ILogger<RestCountriesMapper>? log = null)
        {
            this.log = log;
        }

        /// <summary>Una página de resultados.</summary>
        /// <param name="Paises">países válidos de la página</param>
        /// <param name="Recibidos">objetos que venían en la página, válidos o no (sirve para avanzar el
        /// offset de la paginación)</param>
        /// <param name="HayMas">la API indica que existen más páginas</param>
        /// <param name="EsDemo">la respuesta proviene de la clave demo (datos de muestra)</param>
        public sealed record Pagina(IReadOnlyList<Pais> Paises, int Recibidos, bool HayMas, bool EsDemo);

        /// <exception cref="ExcepcionApiExterna">si el JSON no es válido o no tiene la forma esperada</exception>
        public Pagina LeerPagina(string? json)
        {
            JObject data = LeerData(json);
            if (data["objects"] is not JArray objetos)
            {
                throw new ExcepcionApiExterna("La respuesta de la API no contiene la lista de países.");
            }
            List<Pais> paises = new List<Pais>();
            foreach (JToken elemento in objetos)
            {
                if (elemento is JObject objeto)
                {
                    Pais? pais = APais(objeto);
                    if (pais != null)
                    {
                        paises.Add(pais);
                    }
                }
            }
            bool hayMas = Booleano(data["meta"] as JObject, "more");
            return new Pagina(paises, objetos.Count, hayMas, data.ContainsKey("_demo"));
        }

        private static JObject LeerData(string? json)
        {
            JToken raiz;
            try
            {
                raiz = JToken.Parse(json ?? "");
            }
            catch (JsonReaderException e)
            {
                throw new ExcepcionApiExterna("La API devolvió un JSON no válido.", e);
            }
            if (raiz is not JObject objeto || objeto["data"] is not JObject data)
            {
                throw new ExcepcionApiExterna("La API devolvió una respuesta con un formato inesperado.");
            }
            return data;
        }

        private Pais? APais(JObject json)
        {
            string? nombre = Texto(json["names"] as JObject, "common");
            string? codigo = Texto(json["codes"] as JObject, "alpha_3");
            string? valorRegion = Texto(json, "region");
            Region? region = RegionExtensiones.DesdeApi(valorRegion);

            if (nombre == null || codigo == null || !CodigoIso.IsMatch(codigo) || region == null)
            {
                log?.LogWarning("Se descarta un país incompleto de la API: nombre={Nombre}, código={Codigo}, región={Region}",
                    nombre, codigo, valorRegion);
                return null;
            }
            return Pais.Nuevo(codigo, nombre, CapitalPrincipal(json["capitals"]), region,
                Poblacion(json["population"]), MonedaPrincipal(json["currencies"]),
                Texto(json, "subregion"), Area(json["area"]));
        }

        /// <summary>
        /// La capital marcada como principal; si ninguna lo está, la primera. Acepta también una lista
        /// de textos simples (forma de la v3.1).
        /// </summary>
        private static string? CapitalPrincipal(JToken? capitales)
        {
            if (capitales is not JArray lista)
            {
                return null;
            }
            string? primera = null;
            foreach (JToken elemento in lista)
            {
                string? nombre;
                bool principal = false;
                if (elemento is JObject capital)
                {
                    nombre = Texto(capital, "name");
                    principal = Booleano(capital["attributes"] as JObject, "primary");
                }
                else
                {
                    nombre = ComoTexto(elemento);
                }
                if (nombre != null && principal)
                {
                    return nombre;
                }
                primera ??= nombre;
            }
            return primera;
        }

        /// <summary>
        /// Nombre de la primera moneda. La v5 envía una lista de objetos; también se acepta un mapa
        /// código → objeto (forma de versiones anteriores).
        /// </summary>
        private static string? MonedaPrincipal(JToken? monedas)
        {
            JToken? primera = null;
            if (monedas is JArray lista && lista.Count > 0)
            {
                primera = lista[0];
            }
            else if (monedas is JObject mapa)
            {
                primera = mapa.Properties().Select(p => p.Value).FirstOrDefault();
            }
            if (primera == null)
            {
                return null;
            }
            return primera is JObject objeto ? Texto(objeto, "name") : ComoTexto(primera);
        }

        private static long Poblacion(JToken? valor)
        {
            if (valor != null && (valor.Type == JTokenType.Integer || valor.Type == JTokenType.Float))
            {
                return Math.Max(0, (long)valor.Value<decimal>());
            }
            return 0;
        }

        /// <summary>Área en km²: la v5 la envía como objeto (kilometers); la v3.1, como número.</summary>
        private static double Area(JToken? valor)
        {
            JToken? kilometros = valor is JObject objeto ? objeto["kilometers"] : valor;
            if (kilometros != null && (kilometros.Type == JTokenType.Integer || kilometros.Type == JTokenType.Float))
            {
                return Math.Max(0, kilometros.Value<double>());
            }
            return 0;
        }

        // ---------------------------------------------- lectura segura del árbol

        private static string? Texto(JObject? padre, string campo)
        {
            return padre == null ? null : ComoTexto(padre[campo]);
        }

        private static string? ComoTexto(JToken? valor)
        {
            if (valor != null && valor.Type == JTokenType.String)
            {
                string texto = valor.Value<string>()!.Trim();
                return texto.Length == 0 ? null : texto;
            }
            return null;
        }

        private static bool Booleano(JObject? padre, string campo)
        {
            JToken? valor = padre?[campo];
            return valor != null && valor.Type == JTokenType.Boolean && valor.Value<bool>();
        }
    }
}

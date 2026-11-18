using ec.edu.monster.modelo.entidad;
using ec.edu.monster.modelo.excepcion;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace ec.edu.monster.modelo.api
{
    /// <summary>
    /// Convierte el JSON de Hipolabs en objetos <see cref="Universidad"/>. Estructura:
    /// <code>
    /// [ { "name": "Escuela Politécnica Nacional", "domains": ["epn.edu.ec"],
    ///     "web_pages": ["http://www.epn.edu.ec/"], "country": "Ecuador",
    ///     "alpha_two_code": "EC", "state-province": null } ]
    /// </code>
    /// Se guarda el primer dominio y la primera página web. Las que no tienen nombre se descartan.
    /// </summary>
    public sealed class HipolabsMapper
    {
        /// <exception cref="ExcepcionApiExterna">si el JSON no es una lista válida</exception>
        public IReadOnlyList<Universidad> LeerLista(string? json)
        {
            JToken raiz;
            try
            {
                raiz = JToken.Parse(json ?? "");
            }
            catch (JsonReaderException e)
            {
                throw new ExcepcionApiExterna("La API de universidades devolvió un JSON no válido.", e);
            }
            if (raiz is not JArray lista)
            {
                throw new ExcepcionApiExterna("La API de universidades devolvió una respuesta con un formato inesperado.");
            }

            List<Universidad> universidades = new List<Universidad>();
            foreach (JToken elemento in lista)
            {
                if (elemento is not JObject objeto)
                {
                    continue;
                }
                string? nombre = Texto(objeto["name"]);
                if (nombre == null)
                {
                    continue;
                }
                universidades.Add(Universidad.Nueva(nombre, Primero(objeto["domains"]),
                    Primero(objeto["web_pages"]), Texto(objeto["country"])));
            }
            return universidades;
        }

        private static string? Primero(JToken? valor)
        {
            if (valor is JArray lista)
            {
                return lista.Select(Texto).FirstOrDefault(t => t != null);
            }
            return Texto(valor);
        }

        private static string? Texto(JToken? valor)
        {
            if (valor != null && valor.Type == JTokenType.String)
            {
                string texto = valor.Value<string>()!.Trim();
                return texto.Length == 0 ? null : texto;
            }
            return null;
        }
    }
}

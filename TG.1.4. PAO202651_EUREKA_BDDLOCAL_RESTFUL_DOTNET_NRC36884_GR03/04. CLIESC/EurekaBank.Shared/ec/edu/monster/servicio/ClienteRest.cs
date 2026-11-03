using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;
using ec.edu.monster.modelo;

namespace ec.edu.monster.servicio
{
    /// <summary>
    /// Única clase que conoce HTTP, JSON y las rutas de la API REST de EurekaBank (/api) del
    /// servidor WS_EUREKA_DOTNET_REST. Envía el token en la cabecera Authorization: Bearer y
    /// convierte los errores JSON (estado ERROR, codigo, mensaje) en ErrorServicioModelo.
    /// </summary>
    public sealed class ClienteRest
    {
        private const string EstadoOk = "OK";
        private const string TipoJson = "application/json";

        private static readonly JsonSerializerOptions OpcionesJson = new(JsonSerializerDefaults.Web);

        private readonly HttpClient http;
        private readonly string urlBase;

        public ClienteRest(HttpClient http, OpcionesCliente opciones)
        {
            this.http = http;
            urlBase = opciones.UrlServidor.TrimEnd('/') + "/api";
        }

        public Task<JsonElement> GetAsync(string ruta, string? token)
        {
            return EnviarAsync(HttpMethod.Get, ruta, token, null);
        }

        public Task<JsonElement> PostAsync(string ruta, string? token, object? cuerpo)
        {
            return EnviarAsync(HttpMethod.Post, ruta, token, cuerpo);
        }

        /// <summary>Escapa un valor del usuario para usarlo como segmento de la ruta.</summary>
        public static string Segmento(string valor)
        {
            return Uri.EscapeDataString(valor);
        }

        /// <summary>Devuelve el cuerpo JSON de una respuesta correcta (estado OK).</summary>
        private async Task<JsonElement> EnviarAsync(HttpMethod metodo, string ruta, string? token, object? cuerpo)
        {
            JsonElement respuesta;
            int codigoHttp;
            try
            {
                using HttpRequestMessage solicitud = new(metodo, urlBase + ruta);
                solicitud.Headers.Accept.Add(new MediaTypeWithQualityHeaderValue(TipoJson));
                if (token != null)
                {
                    solicitud.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);
                }
                if (cuerpo != null)
                {
                    solicitud.Content = new StringContent(JsonSerializer.Serialize(cuerpo, OpcionesJson), Encoding.UTF8, TipoJson);
                }

                using HttpResponseMessage resultado = await http.SendAsync(solicitud);
                codigoHttp = (int)resultado.StatusCode;
                string contenido = await resultado.Content.ReadAsStringAsync();
                respuesta = LeerJson(contenido, codigoHttp);
            }
            catch (Exception ex) when (ex is HttpRequestException || ex is TaskCanceledException)
            {
                throw new ErrorServicioModelo(
                    $"No se pudo conectar con el servidor {urlBase}. Verifica que el servidor REST esté iniciado.", ex);
            }

            string? estado = Texto(respuesta, "estado");
            if (estado != EstadoOk)
            {
                string? mensaje = Texto(respuesta, "mensaje");
                throw new ErrorServicioModelo(Texto(respuesta, "codigo"), string.IsNullOrWhiteSpace(mensaje)
                    ? (codigoHttp >= 400 ? $"El servidor respondió con el código HTTP {codigoHttp}." : "El servidor devolvió una respuesta inesperada.")
                    : mensaje);
            }
            return respuesta;
        }

        private static JsonElement LeerJson(string contenido, int codigoHttp)
        {
            if (string.IsNullOrWhiteSpace(contenido))
            {
                throw new ErrorServicioModelo(null, $"El servidor respondió con el código HTTP {codigoHttp} sin contenido.");
            }
            try
            {
                using JsonDocument documento = JsonDocument.Parse(contenido);
                if (documento.RootElement.ValueKind != JsonValueKind.Object)
                {
                    throw new ErrorServicioModelo(null, "El servidor devolvió una respuesta inesperada.");
                }
                return documento.RootElement.Clone();
            }
            catch (JsonException ex)
            {
                throw new ErrorServicioModelo($"No se pudo leer la respuesta del servidor (HTTP {codigoHttp}).", ex);
            }
        }

        /// <summary>Texto de la propiedad, o null si no existe o es null.</summary>
        public static string? Texto(JsonElement objeto, string nombre)
        {
            if (objeto.ValueKind != JsonValueKind.Object || !objeto.TryGetProperty(nombre, out JsonElement valor))
            {
                return null;
            }
            return valor.ValueKind switch
            {
                JsonValueKind.Null or JsonValueKind.Undefined => null,
                JsonValueKind.String => valor.GetString(),
                _ => valor.GetRawText()
            };
        }

        public static decimal? Decimal(JsonElement objeto, string nombre)
        {
            string? valor = Texto(objeto, nombre);
            if (string.IsNullOrWhiteSpace(valor))
            {
                return null;
            }
            if (decimal.TryParse(valor, NumberStyles.Number, CultureInfo.InvariantCulture, out decimal numero))
            {
                return numero;
            }
            throw new ErrorServicioModelo(null, $"El servidor devolvió un valor no numérico en {nombre}.");
        }

        public static int? Entero(JsonElement objeto, string nombre)
        {
            string? valor = Texto(objeto, nombre);
            if (string.IsNullOrWhiteSpace(valor))
            {
                return null;
            }
            if (int.TryParse(valor, NumberStyles.Integer, CultureInfo.InvariantCulture, out int numero))
            {
                return numero;
            }
            throw new ErrorServicioModelo(null, $"El servidor devolvió un valor no numérico en {nombre}.");
        }

        /// <summary>Objeto anidado, o null si no existe.</summary>
        public static JsonElement? Objeto(JsonElement objeto, string nombre)
        {
            return objeto.ValueKind == JsonValueKind.Object && objeto.TryGetProperty(nombre, out JsonElement valor)
                   && valor.ValueKind == JsonValueKind.Object ? valor : null;
        }

        /// <summary>Elementos de un arreglo; vacío si no existe.</summary>
        public static IEnumerable<JsonElement> Arreglo(JsonElement objeto, string nombre)
        {
            return objeto.ValueKind == JsonValueKind.Object && objeto.TryGetProperty(nombre, out JsonElement valor)
                   && valor.ValueKind == JsonValueKind.Array ? valor.EnumerateArray().ToList() : Enumerable.Empty<JsonElement>();
        }
    }
}

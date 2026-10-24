using System.Net;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text.Json;
using SistemaConversorREST.UI.Shared.Interfaces;
using SistemaConversorREST.UI.Shared.Modelos;

namespace SistemaConversorREST.UI.Shared.Adaptadores;

public sealed class AdaptadorConversionRest : IConversionRest
{
    private static readonly JsonSerializerOptions JsonOptions = new(JsonSerializerDefaults.Web);
    private readonly HttpClient _http;

    public AdaptadorConversionRest(HttpClient http) => _http = http;

    public async Task<IReadOnlyList<CategoriaCliente>> ConsultarCatalogoAsync(string token)
    {
        var respuesta = await EnviarAsync(HttpMethod.Get, "api/catalogo/categorias", token);
        return (IReadOnlyList<CategoriaCliente>?)respuesta.Categorias ?? Array.Empty<CategoriaCliente>();
    }

    public async Task<IReadOnlyList<UnidadCliente>> ConsultarUnidadesAsync(string categoria, string token)
    {
        var ruta = $"api/catalogo/categorias/{Uri.EscapeDataString(categoria)}/unidades";
        var respuesta = await EnviarAsync(HttpMethod.Get, ruta, token);
        return (IReadOnlyList<UnidadCliente>?)respuesta.Unidades ?? Array.Empty<UnidadCliente>();
    }

    public Task<double> ConvertirLongitudAsync(SolicitudConversionCliente solicitud, string token)
        => ConvertirAsync("api/conversiones/longitud", solicitud, token);

    public Task<double> ConvertirMasaAsync(SolicitudConversionCliente solicitud, string token)
        => ConvertirAsync("api/conversiones/masa", solicitud, token);

    public Task<double> ConvertirTemperaturaAsync(SolicitudConversionCliente solicitud, string token)
        => ConvertirAsync("api/conversiones/temperatura", solicitud, token);

    private async Task<double> ConvertirAsync(string ruta, SolicitudConversionCliente solicitud, string token)
    {
        var respuesta = await EnviarAsync(HttpMethod.Post, ruta, token, solicitud);
        if (!respuesta.Resultado.HasValue)
            throw new InvalidOperationException("El servidor no devolvió un resultado numérico.");
        return respuesta.Resultado.Value;
    }

    private async Task<RespuestaApi> EnviarAsync(HttpMethod metodo, string ruta, string token, object? cuerpo = null)
    {
        using var solicitud = new HttpRequestMessage(metodo, ruta);
        if (!string.IsNullOrWhiteSpace(token))
            solicitud.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);
        if (cuerpo is not null)
            solicitud.Content = JsonContent.Create(cuerpo, options: JsonOptions);

        using var respuesta = await _http.SendAsync(solicitud);
        RespuestaApi? datos;
        try
        {
            datos = await respuesta.Content.ReadFromJsonAsync<RespuestaApi>(JsonOptions);
        }
        catch (JsonException ex)
        {
            throw new HttpRequestException("El servidor REST devolvió una respuesta JSON no válida.", ex);
        }

        if (respuesta.StatusCode == HttpStatusCode.Unauthorized)
        {
            throw new HttpRequestException(
                "La sesión no es válida o expiró. Vuelve al menú, cierra sesión e inicia sesión nuevamente.",
                null,
                respuesta.StatusCode);
        }

        if (!respuesta.IsSuccessStatusCode)
        {
            var mensaje = datos?.Mensaje ?? $"Error HTTP {(int)respuesta.StatusCode} al consultar el servicio REST.";
            throw new HttpRequestException(mensaje, null, respuesta.StatusCode);
        }

        if (datos is null || !string.Equals(datos.Estado, "OK", StringComparison.OrdinalIgnoreCase))
            throw new InvalidOperationException(datos?.Mensaje ?? "El servidor devolvió una respuesta vacía.");

        return datos;
    }
}

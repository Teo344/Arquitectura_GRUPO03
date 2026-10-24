using System.Net.Http.Json;
using System.Text.Json;
using SistemaConversorREST.UI.Shared.Interfaces;
using SistemaConversorREST.UI.Shared.Modelos;

namespace SistemaConversorREST.UI.Shared.Adaptadores;

public sealed class AdaptadorAutenticacionRest : IAutenticacionRest
{
    private static readonly JsonSerializerOptions JsonOptions = new(JsonSerializerDefaults.Web);
    private readonly HttpClient _http;

    public AdaptadorAutenticacionRest(HttpClient http) => _http = http;

    public async Task<SesionCliente> IniciarSesionAsync(string usuario, string clave)
    {
        using var respuesta = await _http.PostAsJsonAsync(
            "api/autenticacion/login",
            new CredencialesRequest(usuario, clave),
            JsonOptions);

        var resultado = await LeerRespuestaAsync(respuesta);
        if (string.IsNullOrWhiteSpace(resultado.Token))
            throw new InvalidOperationException("El servidor no devolvió un token de sesión.");

        return new SesionCliente(usuario, resultado.Token, resultado.ExpiraEn);
    }

    private static async Task<RespuestaApi> LeerRespuestaAsync(HttpResponseMessage respuesta)
    {
        RespuestaApi? datos;
        try
        {
            datos = await respuesta.Content.ReadFromJsonAsync<RespuestaApi>(JsonOptions);
        }
        catch (JsonException ex)
        {
            throw new HttpRequestException("El servidor REST devolvió una respuesta JSON no válida.", ex);
        }

        if (!respuesta.IsSuccessStatusCode)
        {
            var mensaje = datos?.Mensaje ?? $"Error HTTP {(int)respuesta.StatusCode} al iniciar sesión.";
            throw new HttpRequestException(mensaje, null, respuesta.StatusCode);
        }

        if (datos is null || !string.Equals(datos.Estado, "OK", StringComparison.OrdinalIgnoreCase))
            throw new InvalidOperationException(datos?.Mensaje ?? "El servidor devolvió una respuesta vacía.");

        return datos;
    }

    private sealed record CredencialesRequest(string Usuario, string Clave);
}

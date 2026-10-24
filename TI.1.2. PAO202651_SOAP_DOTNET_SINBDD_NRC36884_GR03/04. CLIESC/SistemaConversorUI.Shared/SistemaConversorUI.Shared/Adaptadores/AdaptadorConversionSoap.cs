using System.Globalization;
using System.Net.Http;
using System.Text;
using System.Xml.Linq;
using SistemaConversorUI.Shared.Interfaces;
using SistemaConversorUI.Shared.Modelos;

namespace SistemaConversorUI.Shared.Adaptadores;

public sealed class AdaptadorConversionSoap : IConversionSoap
{
    private static readonly XNamespace Soap = "http://schemas.xmlsoap.org/soap/envelope/";
    private static readonly XNamespace Servicio = "http://wservicios.controladores.monster.edu.ec/";
    private readonly HttpClient _http;
    private readonly Uri _urlServicio;

    public AdaptadorConversionSoap(HttpClient http, OpcionesCliente opciones)
    {
        _http = http;
        _urlServicio = new Uri(new Uri(opciones.UrlServidor), "ConversionUnidadesWS.svc");
    }

    public async Task<IReadOnlyList<CategoriaCliente>> ConsultarCatalogoAsync(string token)
    {
        var respuesta = await EnviarAsync("consultarCatalogo", token);
        return respuesta.Descendants()
            .Where(x => x.Name.LocalName == "categoria" && x.Elements().Any(e => e.Name.LocalName == "codigo"))
            .Select(x => new CategoriaCliente(
                Valor(x, "codigo"),
                Valor(x, "nombre"),
                bool.TryParse(Valor(x, "convertible"), out var convertible) && convertible))
            .ToList();
    }

    public async Task<IReadOnlyList<UnidadCliente>> ConsultarUnidadesAsync(string categoria, string token)
    {
        var respuesta = await EnviarAsync(
            "consultarUnidades",
            token,
            new XElement(Servicio + "categoria", categoria));

        return respuesta.Descendants()
            .Where(x => x.Name.LocalName == "unidad" && x.Elements().Any(e => e.Name.LocalName == "codigo"))
            .Select(x => new UnidadCliente(
                Valor(x, "codigo"),
                Valor(x, "nombre"),
                Valor(x, "simbolo")))
            .ToList();
    }

    public Task<double> ConvertirLongitudAsync(SolicitudConversionCliente solicitud, string token)
        => ConvertirAsync("convertirLongitud", solicitud, token);

    public Task<double> ConvertirMasaAsync(SolicitudConversionCliente solicitud, string token)
        => ConvertirAsync("convertirMasa", solicitud, token);

    public Task<double> ConvertirTemperaturaAsync(SolicitudConversionCliente solicitud, string token)
        => ConvertirAsync("convertirTemperatura", solicitud, token);

    private async Task<double> ConvertirAsync(string operacion, SolicitudConversionCliente solicitud, string token)
    {
        var solicitudXml = new XElement(Servicio + "solicitud",
            new XElement(Servicio + "valor", solicitud.Valor.ToString("R", CultureInfo.InvariantCulture)),
            new XElement(Servicio + "unidadOrigen", solicitud.UnidadOrigen),
            new XElement(Servicio + "unidadDestino", solicitud.UnidadDestino));

        var respuesta = await EnviarAsync(operacion, token, solicitudXml);
        var resultadoTexto = Valor(respuesta, "resultado");
        if (!double.TryParse(resultadoTexto, NumberStyles.Float, CultureInfo.InvariantCulture, out var resultado))
            throw new InvalidOperationException("El servicio no devolvió un resultado numérico válido.");

        return resultado;
    }

    private async Task<XElement> EnviarAsync(string operacion, string token, params XElement[] parametros)
    {
        var sobre = new XDocument(
            new XElement(Soap + "Envelope",
                new XAttribute(XNamespace.Xmlns + "soap", Soap),
                new XAttribute(XNamespace.Xmlns + "svc", Servicio),
                new XElement(Soap + "Header", new XElement(Servicio + "token", token)),
                new XElement(Soap + "Body", new XElement(Servicio + operacion, parametros))));

        using var solicitud = new HttpRequestMessage(HttpMethod.Post, _urlServicio);
        solicitud.Headers.Add("SOAPAction", $"\"{Servicio}ConversionUnidadesWS/{operacion}\"");
        solicitud.Content = new StringContent(
            sobre.ToString(SaveOptions.DisableFormatting), Encoding.UTF8, "text/xml");

        using var respuestaHttp = await _http.SendAsync(solicitud);
        var contenido = await respuestaHttp.Content.ReadAsStringAsync();

        XDocument documento;
        try
        {
            documento = XDocument.Parse(contenido);
        }
        catch (System.Xml.XmlException)
        {
            if (!respuestaHttp.IsSuccessStatusCode && string.IsNullOrWhiteSpace(contenido))
            {
                throw new InvalidOperationException(
                    "El servidor rechazó la solicitud protegida o reinició la sesión. Vuelve al menú, cierra sesión e inicia sesión nuevamente.");
            }

            throw new HttpRequestException(
                $"El servidor devolvió una respuesta no XML (HTTP {(int)respuestaHttp.StatusCode}).");
        }

        var fault = documento.Descendants().FirstOrDefault(x => x.Name.LocalName == "Fault");
        if (!respuestaHttp.IsSuccessStatusCode || fault is not null)
        {
            var mensaje = documento.Descendants()
                .FirstOrDefault(x => x.Name.LocalName is "faultstring" or "mensaje")?.Value;
            throw new InvalidOperationException(mensaje ?? $"Error HTTP {(int)respuestaHttp.StatusCode} del servicio SOAP.");
        }

        // WCF expone el retorno con el nombre indicado por MessageParameter("return").
        var resultado = documento.Descendants()
            .FirstOrDefault(x => x.Name.LocalName == "return")
            ?? documento.Descendants().FirstOrDefault(x => x.Name.LocalName == $"{operacion}Result");
        if (resultado is null)
            throw new InvalidOperationException("La respuesta SOAP no contiene el resultado esperado.");

        var estado = Valor(resultado, "estado");
        if (!string.IsNullOrWhiteSpace(estado) && estado != "OK")
            throw new InvalidOperationException(Valor(resultado, "mensaje") ?? "El servicio rechazó la operación.");

        return resultado;
    }

    private static string Valor(XElement elemento, string nombre)
        => elemento.DescendantsAndSelf().FirstOrDefault(x => x.Name.LocalName == nombre)?.Value ?? string.Empty;
}

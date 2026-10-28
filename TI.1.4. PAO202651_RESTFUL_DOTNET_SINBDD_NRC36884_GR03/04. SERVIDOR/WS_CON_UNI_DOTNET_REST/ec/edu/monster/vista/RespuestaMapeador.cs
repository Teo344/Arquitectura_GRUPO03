using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using ec.edu.monster.modelo;
using ec.edu.monster.seguridad;
using ec.edu.monster.servicios;

namespace ec.edu.monster.vista
{
    /// <summary>Convierte objetos del modelo en <see cref="RespuestaDto"/> (RQF-009).</summary>
    public static class RespuestaMapeador
    {
        public const string EstadoOk = "OK";
        public const string CodigoOk = "200";

        public static RespuestaDto DeToken(TokenEmitido token)
        {
            RespuestaDto respuesta = Exito("Autenticación exitosa");
            respuesta.Token = token.Token;
            respuesta.ExpiraEn = token.ExpiraEn.ToString("o", CultureInfo.InvariantCulture);
            return respuesta;
        }

        public static RespuestaDto DeCambioClave() => Exito("Clave actualizada; inicie sesión nuevamente");

        public static RespuestaDto DeCatalogo(IEnumerable<Categoria> categorias)
        {
            RespuestaDto respuesta = Exito("Catálogo consultado");
            respuesta.Categorias = categorias
                .Select(c => new CategoriaDto { Codigo = c.Codigo, Nombre = c.Nombre, Convertible = c.Convertible })
                .ToList();
            return respuesta;
        }

        public static RespuestaDto DeUnidades(IEnumerable<Unidad> unidades)
        {
            RespuestaDto respuesta = Exito("Unidades consultadas");
            respuesta.Unidades = unidades
                .Select(u => new UnidadDto { Codigo = u.Codigo, Nombre = u.Nombre, Simbolo = u.Simbolo, Categoria = u.Categoria.Codigo })
                .ToList();
            return respuesta;
        }

        public static RespuestaDto DeConversion(ResultadoConversion resultado)
        {
            RespuestaDto respuesta = Exito("Conversión realizada");
            respuesta.Resultado = resultado.Resultado;
            respuesta.UnidadOrigen = resultado.Origen.Codigo;
            respuesta.UnidadDestino = resultado.Destino.Codigo;
            return respuesta;
        }

        private static RespuestaDto Exito(string mensaje)
        {
            return new RespuestaDto { Estado = EstadoOk, Codigo = CodigoOk, Mensaje = mensaje };
        }
    }
}

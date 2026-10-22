using System;
using System.Collections.Concurrent;
using System.Linq;
using Microsoft.Extensions.Configuration;
using WS_CON_UNI_DOTNET_REST.Modelo;
using WS_CON_UNI_DOTNET_REST.Servicios;

namespace WS_CON_UNI_DOTNET_REST.Seguridad
{
    /// <summary>Token emitido por <see cref="GestorToken"/> y su instante de expiración.</summary>
    public class TokenEmitido
    {
        public string Token { get; set; }
        public DateTime ExpiraEn { get; set; }
    }

    /// <summary>
    /// Credenciales y sesiones en memoria (RQF-001, RQF-007). Guarda el usuario vigente y emite
    /// tokens opacos (GUID) con su expiración. Se registra como Singleton (equivale al
    /// @ApplicationScoped de Java); al reiniciar se vuelve a las credenciales de appsettings.json
    /// y se pierden las sesiones.
    /// </summary>
    public sealed class GestorToken
    {
        private sealed class Sesion
        {
            public string Usuario;
            public DateTime ExpiraEn;
        }

        private readonly ConcurrentDictionary<string, Sesion> sesiones = new ConcurrentDictionary<string, Sesion>();
        private readonly int minutos;
        private readonly string usuario;
        private volatile string clave;

        public GestorToken(IConfiguration configuracion)
        {
            minutos = configuracion.GetValue<int>("Token:Minutos");
            usuario = configuracion["Usuario:Nombre"];
            clave = configuracion["Usuario:Clave"];
        }

        public bool CredencialesValidas(string nombre, string clave)
        {
            return usuario == nombre && this.clave == clave;
        }

        /// <summary>Cambia la clave y cierra todas las sesiones.</summary>
        public void CambiarClave(string claveNueva)
        {
            clave = claveNueva;
            sesiones.Clear();
        }

        public TokenEmitido Generar(string usuario)
        {
            DateTime ahora = DateTime.UtcNow;
            foreach (var vencida in sesiones.Where(s => s.Value.ExpiraEn < ahora).ToList())
            {
                sesiones.TryRemove(vencida.Key, out _);
            }
            string token = Guid.NewGuid().ToString();
            DateTime expiraEn = ahora.AddMinutes(minutos);
            sesiones[token] = new Sesion { Usuario = usuario, ExpiraEn = expiraEn };
            return new TokenEmitido { Token = token, ExpiraEn = expiraEn };
        }

        /// <summary>Devuelve el usuario del token. Inexistente: AUTH-001; expirado: AUTH-002.</summary>
        public string Validar(string token)
        {
            if (string.IsNullOrWhiteSpace(token) || !sesiones.TryGetValue(token.Trim(), out Sesion sesion))
            {
                throw new ExcepcionServicio(CodigoError.Auth001);
            }
            if (sesion.ExpiraEn < DateTime.UtcNow)
            {
                sesiones.TryRemove(token.Trim(), out _);
                throw new ExcepcionServicio(CodigoError.Auth002);
            }
            return sesion.Usuario;
        }
    }
}

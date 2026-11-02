using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics;
using System.Threading;
using Microsoft.Data.SqlClient;
using Microsoft.Extensions.Configuration;
using ec.edu.monster.datos;
using ec.edu.monster.modelo;
using ec.edu.monster.servicios;

namespace ec.edu.monster.seguridad
{
    /// <summary>Token emitido por <see cref="GestorToken"/> y su instante de expiración.</summary>
    public class TokenEmitido
    {
        public string Token { get; set; }
        public DateTime ExpiraEn { get; set; }
    }

    /// <summary>
    /// Sesiones en memoria (RQF-001, RQF-002, RNF-007). Emite tokens opacos (GUID) que caducan tras
    /// Sesion:Minutos de inactividad: cada validación correcta renueva el plazo. Se registra como
    /// Singleton (equivale al @ApplicationScoped de Java); al reiniciar se pierden las sesiones.
    /// </summary>
    public sealed class GestorToken
    {
        private sealed class Entrada
        {
            public readonly Sesion Sesion;
            private long ultimoAccesoTicks;

            public Entrada(Sesion sesion, DateTime ultimoAcceso)
            {
                Sesion = sesion;
                ultimoAccesoTicks = ultimoAcceso.Ticks;
            }

            /// <summary>En UTC; Interlocked porque varias peticiones pueden renovar la misma sesión.</summary>
            public DateTime UltimoAcceso
            {
                get { return new DateTime(Interlocked.Read(ref ultimoAccesoTicks), DateTimeKind.Utc); }
                set { Interlocked.Exchange(ref ultimoAccesoTicks, value.Ticks); }
            }
        }

        private readonly ConcurrentDictionary<string, Entrada> sesiones = new ConcurrentDictionary<string, Entrada>();
        private readonly LogSesionDao logSesionDao = new LogSesionDao();
        private readonly int minutos;

        public GestorToken(IConfiguration configuracion)
        {
            minutos = configuracion.GetValue("Sesion:Minutos", 30);
        }

        public TokenEmitido Generar(Sesion sesion)
        {
            DateTime ahora = DateTime.UtcNow;
            PurgarVencidas(ahora);
            string token = Guid.NewGuid().ToString();
            sesiones[token] = new Entrada(sesion, ahora);
            return new TokenEmitido { Token = token, ExpiraEn = ahora.AddMinutes(minutos) };
        }

        /// <summary>Devuelve la sesión del token y renueva su plazo. Ausente, desconocido o vencido: AUTH-003.</summary>
        public Sesion Validar(string token)
        {
            Entrada entrada = Vigente(token);
            entrada.UltimoAcceso = DateTime.UtcNow;
            return entrada.Sesion;
        }

        /// <summary>Cierra la sesión del token y la devuelve (RQF-002).</summary>
        public Sesion Invalidar(string token)
        {
            Entrada entrada = Vigente(token);
            sesiones.TryRemove(token.Trim(), out _);
            return entrada.Sesion;
        }

        private Entrada Vigente(string token)
        {
            if (string.IsNullOrWhiteSpace(token) || !sesiones.TryGetValue(token.Trim(), out Entrada entrada))
            {
                throw new ExcepcionServicio(CodigoError.Auth003);
            }
            if (Vencida(entrada, DateTime.UtcNow))
            {
                if (Quitar(token.Trim(), entrada))
                {
                    RegistrarSalida(entrada);
                }
                throw new ExcepcionServicio(CodigoError.Auth003);
            }
            return entrada;
        }

        private void PurgarVencidas(DateTime ahora)
        {
            foreach (KeyValuePair<string, Entrada> sesion in sesiones)
            {
                if (Vencida(sesion.Value, ahora) && Quitar(sesion.Key, sesion.Value))
                {
                    RegistrarSalida(sesion.Value);
                }
            }
        }

        private bool Vencida(Entrada entrada, DateTime ahora)
        {
            return entrada.UltimoAcceso.AddMinutes(minutos) < ahora;
        }

        /// <summary>Quita el token solo si sigue apuntando a esa entrada (otra petición pudo quitarlo antes).</summary>
        private bool Quitar(string token, Entrada entrada)
        {
            return ((ICollection<KeyValuePair<string, Entrada>>)sesiones)
                .Remove(new KeyValuePair<string, Entrada>(token, entrada));
        }

        /// <summary>
        /// Una sesión que caduca sin cerrarse registra como salida el momento en que venció. Si
        /// falla la BD solo se registra en el log: la sesión ya no es válida de todas formas.
        /// </summary>
        private void RegistrarSalida(Entrada entrada)
        {
            try
            {
                using (SqlConnection cn = ConexionBD.Instancia.ObtenerConexion())
                {
                    logSesionDao.RegistrarSalida(cn, entrada.Sesion.LogId, entrada.UltimoAcceso.AddMinutes(minutos));
                }
            }
            catch (Exception e) when (e is ExcepcionServicio || e is SqlException)
            {
                Trace.TraceWarning("No se pudo registrar la salida de la sesión {0}", entrada.Sesion.LogId);
            }
        }
    }
}

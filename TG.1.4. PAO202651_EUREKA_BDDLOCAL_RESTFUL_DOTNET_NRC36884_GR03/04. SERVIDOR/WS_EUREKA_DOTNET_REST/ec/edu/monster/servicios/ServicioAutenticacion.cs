using System;
using Microsoft.Data.SqlClient;
using System.Diagnostics;
using ec.edu.monster.datos;
using ec.edu.monster.modelo;
using ec.edu.monster.seguridad;

namespace ec.edu.monster.servicios
{
    /// <summary>Token emitido y datos del empleado que inició sesión.</summary>
    public class SesionIniciada
    {
        public TokenEmitido Token { get; set; }
        public Sesion Sesion { get; set; }
    }

    /// <summary>Inicio (RQF-001) y cierre (RQF-002) de sesión del empleado.</summary>
    public class ServicioAutenticacion
    {
        private readonly UsuarioDao usuarioDao = new UsuarioDao();
        private readonly LogSesionDao logSesionDao = new LogSesionDao();
        private readonly GestorToken gestorToken;

        public ServicioAutenticacion(GestorToken gestorToken)
        {
            this.gestorToken = gestorToken;
        }

        /// <summary>
        /// AUTH-001 no indica si falló el usuario o la clave. Un usuario sin sucursal vigente no
        /// puede atender en ventanilla: AUTH-002.
        /// </summary>
        public SesionIniciada IniciarSesion(string usuario, string clave, string ip, string equipo)
        {
            if (string.IsNullOrWhiteSpace(usuario) || string.IsNullOrEmpty(clave))
            {
                throw new ExcepcionServicio(CodigoError.Data001);
            }
            try
            {
                using (SqlConnection cn = ConexionBD.Instancia.ObtenerConexion())
                {
                    Empleado empleado = usuarioDao.Buscar(cn, usuario.Trim(), UtilHash.Sha256(clave))
                        ?? throw new ExcepcionServicio(CodigoError.Auth001);
                    if (!Estado.EsActivo(empleado.Estado) || empleado.CodigoSucursal == null)
                    {
                        throw new ExcepcionServicio(CodigoError.Auth002);
                    }
                    int logId = logSesionDao.RegistrarIngreso(cn, empleado.Codigo, ip, equipo);
                    Sesion sesion = new Sesion
                    {
                        CodigoEmpleado = empleado.Codigo,
                        Usuario = empleado.Usuario,
                        Nombre = empleado.Nombre,
                        CodigoSucursal = empleado.CodigoSucursal,
                        Sucursal = empleado.Sucursal,
                        LogId = logId
                    };
                    return new SesionIniciada { Token = gestorToken.Generar(sesion), Sesion = sesion };
                }
            }
            catch (SqlException e)
            {
                Trace.TraceError("Error de base de datos al iniciar sesión: {0}", e.Message);
                throw new ExcepcionServicio(CodigoError.Sys001);
            }
        }

        /// <summary>Invalida el token y registra la salida. Un token ya inválido da AUTH-003.</summary>
        public void CerrarSesion(string token)
        {
            Sesion sesion = gestorToken.Invalidar(token);
            try
            {
                using (SqlConnection cn = ConexionBD.Instancia.ObtenerConexion())
                {
                    logSesionDao.RegistrarSalida(cn, sesion.LogId, DateTime.UtcNow);
                }
            }
            catch (SqlException e)
            {
                Trace.TraceError("Error de base de datos al cerrar sesión: {0}", e.Message);
                throw new ExcepcionServicio(CodigoError.Sys001);
            }
        }
    }
}

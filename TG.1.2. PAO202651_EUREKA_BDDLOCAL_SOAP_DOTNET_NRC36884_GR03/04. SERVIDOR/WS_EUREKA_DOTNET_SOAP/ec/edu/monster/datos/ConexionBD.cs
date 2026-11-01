using System;
using System.Configuration;
using System.Data.SqlClient;
using System.Diagnostics;
using ec.edu.monster.modelo;
using ec.edu.monster.servicios;

namespace ec.edu.monster.datos
{
    /// <summary>
    /// Singleton de acceso a la base de datos (RNF-010). Lee la cadena "eurekabank" de
    /// conexion.config una sola vez; cada llamada a <see cref="ObtenerConexion"/> entrega una
    /// conexión nueva y abierta que el llamador cierra (ADO.NET la toma de su pool). No se
    /// comparte una única conexión porque cada transacción necesita la suya.
    /// </summary>
    public sealed class ConexionBD
    {
        /// <summary>Una sola instancia para toda la aplicación.</summary>
        public static readonly ConexionBD Instancia = new ConexionBD();

        private readonly string cadena;

        /// <summary>
        /// Si la configuración falla no se lanza la excepción aquí (rompería la carga de la
        /// clase): se registra y cada ObtenerConexion responde SYS-001.
        /// </summary>
        private ConexionBD()
        {
            try
            {
                cadena = ConfigurationManager.ConnectionStrings["eurekabank"]?.ConnectionString;
            }
            catch (ConfigurationErrorsException e)
            {
                Trace.TraceError("No se pudo leer la cadena de conexión: {0}", e.Message);
            }
            if (string.IsNullOrEmpty(cadena))
            {
                Trace.TraceError("Falta la cadena de conexión 'eurekabank' en conexion.config");
            }
        }

        public SqlConnection ObtenerConexion()
        {
            if (string.IsNullOrEmpty(cadena))
            {
                throw new ExcepcionServicio(CodigoError.Sys001);
            }
            SqlConnection conexion = new SqlConnection(cadena);
            try
            {
                conexion.Open();
                return conexion;
            }
            catch (Exception e) when (e is SqlException || e is InvalidOperationException)
            {
                conexion.Dispose();
                Trace.TraceError("No se pudo conectar a la base de datos: {0}", e.Message);
                throw new ExcepcionServicio(CodigoError.Sys001);
            }
        }
    }
}

using System;
using System.Diagnostics;
using Microsoft.Data.SqlClient;
using ec.edu.monster.modelo;
using ec.edu.monster.servicios;

namespace ec.edu.monster.datos
{
    /// <summary>
    /// Singleton de acceso a la base de datos (RNF-010). Program.cs le pasa una sola vez la cadena
    /// "eurekabank" de conexion.json; cada llamada a <see cref="ObtenerConexion"/> entrega una
    /// conexión nueva y abierta que el llamador cierra (ADO.NET la toma de su pool). No se
    /// comparte una única conexión porque cada transacción necesita la suya.
    /// </summary>
    public sealed class ConexionBD
    {
        /// <summary>Una sola instancia para toda la aplicación.</summary>
        public static readonly ConexionBD Instancia = new ConexionBD();

        private volatile string cadena;

        private ConexionBD()
        {
        }

        /// <summary>Se llama al arrancar el servidor (o al inicio de una prueba).</summary>
        public void Configurar(string cadena)
        {
            if (string.IsNullOrEmpty(cadena))
            {
                Trace.TraceError("Falta la cadena de conexión 'eurekabank' en conexion.json");
            }
            this.cadena = cadena;
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

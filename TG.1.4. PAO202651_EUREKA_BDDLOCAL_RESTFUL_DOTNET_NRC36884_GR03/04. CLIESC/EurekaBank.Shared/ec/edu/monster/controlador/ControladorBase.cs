using System;
using ec.edu.monster.modelo;

namespace ec.edu.monster.controlador
{
    /// <summary>
    /// Comportamiento común de los controladores: las llamadas REST son asíncronas y no bloquean
    /// la interfaz. Si la vista ya se cerró, las respuestas que lleguen después se descartan.
    /// </summary>
    public abstract class ControladorBase
    {
        private volatile bool activo = true;

        protected bool Activo
        {
            get { return activo; }
        }

        /// <summary>La vista se cerró: las respuestas que lleguen después se descartan.</summary>
        public void Detener()
        {
            activo = false;
        }

        /// <summary>AUTH-003: el token ya no es válido y hay que volver a iniciar sesión.</summary>
        protected static bool RequiereNuevoLogin(Exception error)
        {
            return error is ErrorServicioModelo servicio && servicio.RequiereNuevoLogin;
        }

        /// <summary>Mensaje para el empleado con el código del ERS cuando el servidor lo envía.</summary>
        protected static string MensajeDe(Exception error)
        {
            string mensaje = string.IsNullOrWhiteSpace(error.Message) ? "No se pudo completar la operación." : error.Message;
            if (error is ErrorServicioModelo servicio && servicio.Codigo != null)
            {
                return mensaje + " (" + servicio.Codigo + ")";
            }
            return mensaje;
        }
    }
}

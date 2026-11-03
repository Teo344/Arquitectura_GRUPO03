using System;
using System.IO;
using ec.edu.monster.vista;

namespace ec.edu.monster
{
    internal class Program
    {
        static void Main(string[] args)
        {
            var mensajes = new MensajesVista();
            try
            {
                new LoginVista().Iniciar();
            }
            catch (EndOfStreamException)
            {
                // El fin de entrada es una salida normal.
            }
            catch (Exception ex)
            {
                mensajes.MostrarErrorFatal(ex);
            }
            mensajes.EsperarSalida();
        }
    }
}

using ec.edu.monster.vista;

namespace ec.edu.monster
{
    internal class Program
    {
        static async Task Main(string[] args)
        {
            var mensajes = new MensajesVista();
            try
            {
                await new LoginVista().IniciarAsync();
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

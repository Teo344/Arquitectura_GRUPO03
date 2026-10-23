using System.Text;
using ec.edu.monster.modelo;

namespace ec.edu.monster.vista
{
    public class MensajesVista
    {
        public void MostrarTitulo(string titulo)
        {
            string texto = SinTildes(titulo);
            Console.WriteLine();
            Escribir(texto, ConsoleColor.Yellow);
            Console.WriteLine(new string('-', texto.Length));
        }

        public void MostrarMensaje(string mensaje) => Console.WriteLine(SinTildes(mensaje));

        public void MostrarError(string mensaje) => Escribir("ERROR: " + SinTildes(mensaje), ConsoleColor.Red);

        public void MostrarError(ErrorRESTModelo error)
        {
            string codigo = error.Codigo == null ? "" : error.Codigo + ": ";
            MostrarError(codigo + error.Message);
        }

        public void MostrarBanner(string banner) => Escribir(banner, ConsoleColor.Cyan);

        public void MostrarErrorFatal(Exception error) => Escribir("Error fatal: " + SinTildes(error.Message), ConsoleColor.Red);

        public void EsperarSalida()
        {
            Console.WriteLine("Presiona cualquier tecla para salir...");
            if (!Console.IsInputRedirected) Console.ReadKey(true);
        }

        private static void Escribir(string texto, ConsoleColor color)
        {
            Console.ForegroundColor = color;
            Console.WriteLine(texto);
            Console.ResetColor();
        }

        // Compatibilidad con terminales ASCII: "m²" se muestra como "m2" y "°C" como "C".
        private static string SinTildes(string? texto)
        {
            if (texto == null) return "Error desconocido";
            var resultado = new StringBuilder();
            foreach (char caracter in texto.Normalize(NormalizationForm.FormKD))
                if (caracter <= 0x7F)
                    resultado.Append(caracter);
            return resultado.ToString();
        }
    }
}

using System;
using System.Collections.Generic;
using System.Globalization;
using System.Text;
using ec.edu.monster.modelo;

namespace ec.edu.monster.vista
{
    /// <summary>Presentación de mensajes de consola, con el mismo estilo que la consola Java del TG.1.1.</summary>
    public class MensajesVista
    {
        private const int AnchoRecuadro = 62;

        public void MostrarBanner(string banner)
        {
            Escribir(banner, ConsoleColor.Cyan);
        }

        public void MostrarTitulo(string titulo)
        {
            string texto = SinTildes(titulo);
            Console.WriteLine();
            Escribir(texto, ConsoleColor.Yellow);
            Console.WriteLine(new string('-', texto.Length));
        }

        public void MostrarMensaje(string mensaje)
        {
            Console.WriteLine(SinTildes(mensaje));
        }

        public void MostrarExito(string mensaje)
        {
            Escribir(SinTildes(mensaje), ConsoleColor.Green);
        }

        public void MostrarError(string mensaje)
        {
            Escribir("ERROR: " + SinTildes(mensaje), ConsoleColor.Red);
        }

        /// <summary>Muestra el código del ERS (AUTH-001, MOV-002, ...) junto al mensaje del servidor.</summary>
        public void MostrarErrorServicio(ErrorServicioModelo error)
        {
            string codigo = error.Codigo == null ? "" : "[" + error.Codigo + "] ";
            MostrarError(codigo + error.Message);
        }

        /// <summary>Recuadro de texto en cian: datos de sesión, fichas y comprobantes.</summary>
        public void MostrarRecuadro(IEnumerable<string> lineas)
        {
            int anchoTexto = AnchoRecuadro - 4;
            string borde = "+" + new string('-', AnchoRecuadro - 2) + "+";
            var texto = new StringBuilder(borde).AppendLine();
            foreach (string linea in lineas)
            {
                string contenido = SinTildes(linea);
                if (contenido.Length > anchoTexto) contenido = contenido.Substring(0, anchoTexto);
                texto.Append("| ").Append(contenido.PadRight(anchoTexto)).Append(" |").AppendLine();
            }
            texto.Append(borde);
            Escribir(texto.ToString(), ConsoleColor.Cyan);
        }

        public void MostrarErrorFatal(Exception error)
        {
            Escribir("Error fatal: " + SinTildes(error.Message), ConsoleColor.Red);
        }

        public void EsperarSalida()
        {
            Console.WriteLine("Presione cualquier tecla para salir...");
            if (!Console.IsInputRedirected) Console.ReadKey(true);
        }

        /// <summary>Importe con dos decimales y separador de miles: 6,900.00</summary>
        public static string FormatoImporte(decimal? importe)
        {
            return importe.HasValue ? importe.Value.ToString("N2", CultureInfo.InvariantCulture) : "-";
        }

        private static void Escribir(string texto, ConsoleColor color)
        {
            Console.ForegroundColor = color;
            Console.WriteLine(texto);
            Console.ResetColor();
        }

        // La consola no muestra bien tildes ni la letra ñ: "Sesión" -> "Sesion", "Rumiñahui" -> "Ruminahui"
        private static string SinTildes(string texto)
        {
            if (texto == null) return "Error desconocido";
            var resultado = new StringBuilder();
            foreach (char caracter in texto.Normalize(NormalizationForm.FormKD))
                if (caracter <= 0x7F) resultado.Append(caracter);
            return resultado.ToString();
        }
    }
}

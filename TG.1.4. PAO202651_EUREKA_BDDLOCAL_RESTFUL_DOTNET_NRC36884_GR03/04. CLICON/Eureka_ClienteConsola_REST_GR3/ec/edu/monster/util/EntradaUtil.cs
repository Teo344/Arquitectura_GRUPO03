using System.Globalization;
using System.Text;

namespace ec.edu.monster.util
{
    /// <summary>Lectura de datos desde el teclado; solo convierte tipos, las reglas las valida el controlador.</summary>
    public static class EntradaUtil
    {
        public static string LeerTextoNoVacio(string mensaje)
        {
            while (true)
            {
                Console.Write(mensaje);
                string texto = LeerLinea().Trim();
                if (texto.Length > 0) return texto;
                Console.Error.WriteLine("Entrada invalida. El valor no puede quedar vacio.");
            }
        }

        public static int LeerEntero(string mensaje)
        {
            while (true)
            {
                Console.Write(mensaje);
                int valor;
                if (int.TryParse(LeerLinea().Trim(), NumberStyles.Integer, CultureInfo.InvariantCulture, out valor))
                    return valor;
                Console.Error.WriteLine("Entrada invalida. Debe ingresar un numero entero.");
            }
        }

        /// <summary>Importe en decimal para no perder precisión; acepta coma o punto decimal.</summary>
        public static decimal LeerImporte(string mensaje)
        {
            while (true)
            {
                Console.Write(mensaje);
                decimal valor;
                if (decimal.TryParse(LeerLinea().Trim().Replace(',', '.'), NumberStyles.Number,
                    CultureInfo.InvariantCulture, out valor))
                    return valor;
                Console.Error.WriteLine("Entrada invalida. Debe ingresar un importe numerico, por ejemplo 150.50");
            }
        }

        /// <summary>
        /// La clave no se muestra mientras se escribe. Si la entrada está redirigida (sin teclado)
        /// se lee como texto normal.
        /// </summary>
        public static string LeerClave(string mensaje)
        {
            while (true)
            {
                Console.Write(mensaje);
                string clave = Console.IsInputRedirected ? LeerLinea() : LeerOculto();
                if (clave.Length > 0) return clave;
                Console.Error.WriteLine("Entrada invalida. La clave no puede quedar vacia.");
            }
        }

        public static bool LeerConfirmacion(string mensaje)
        {
            while (true)
            {
                Console.Write(mensaje + " (S/N): ");
                string valor = LeerLinea().Trim();
                if (valor.Equals("S", StringComparison.OrdinalIgnoreCase)) return true;
                if (valor.Equals("N", StringComparison.OrdinalIgnoreCase)) return false;
                Console.Error.WriteLine("Entrada invalida. Responda S o N.");
            }
        }

        private static string LeerOculto()
        {
            var clave = new StringBuilder();
            while (true)
            {
                ConsoleKeyInfo tecla = Console.ReadKey(true);
                if (tecla.Key == ConsoleKey.Enter)
                {
                    Console.WriteLine();
                    return clave.ToString();
                }
                if (tecla.Key == ConsoleKey.Backspace)
                {
                    if (clave.Length > 0) clave.Length--;
                }
                else if (!char.IsControl(tecla.KeyChar))
                {
                    clave.Append(tecla.KeyChar);
                }
            }
        }

        // El fin de entrada se propaga hasta Main para evitar nuevas lecturas.
        private static string LeerLinea()
        {
            string? linea = Console.ReadLine();
            if (linea == null) throw new EndOfStreamException("Fin de la entrada.");
            return linea;
        }
    }
}

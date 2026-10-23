using System.Globalization;

namespace ec.edu.monster.util
{
    public static class EntradaUtil
    {
        public static string LeerTextoNoVacio(string mensaje)
        {
            while (true)
            {
                Console.Write(mensaje);
                string texto = LeerLinea().Trim();
                if (texto.Length > 0)
                    return texto;
                Console.Error.WriteLine("Entrada invalida. El valor no puede quedar vacio.");
            }
        }

        public static int LeerEntero(string mensaje)
        {
            while (true)
            {
                Console.Write(mensaje);
                if (int.TryParse(LeerLinea().Trim(), NumberStyles.Integer, CultureInfo.InvariantCulture, out int valor))
                    return valor;
                Console.Error.WriteLine("Entrada invalida. Debe ingresar un numero entero.");
            }
        }

        public static double LeerDecimal(string mensaje)
        {
            while (true)
            {
                Console.Write(mensaje);
                string texto = LeerLinea().Trim().Replace(',', '.');
                if (double.TryParse(texto, NumberStyles.Float, CultureInfo.InvariantCulture, out double valor))
                    return valor;
                Console.Error.WriteLine("Entrada invalida. Debe ingresar un numero decimal.");
            }
        }

        private static string LeerLinea()
        {
            // El fin de entrada se propaga hasta Main para evitar nuevas lecturas.
            return Console.ReadLine() ?? throw new EndOfStreamException("Fin de la entrada.");
        }
    }
}

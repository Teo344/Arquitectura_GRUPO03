using System;
using System.Globalization;
using System.IO;

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

        public static double LeerDecimal(string mensaje)
        {
            while (true)
            {
                Console.Write(mensaje);
                double valor;
                if (double.TryParse(LeerLinea().Trim().Replace(',', '.'), NumberStyles.Float,
                    CultureInfo.InvariantCulture, out valor))
                    return valor;
                Console.Error.WriteLine("Entrada invalida. Debe ingresar un numero decimal.");
            }
        }

        // El fin de entrada se propaga hasta Main para evitar nuevas lecturas.
        private static string LeerLinea()
        {
            string linea = Console.ReadLine();
            if (linea == null) throw new EndOfStreamException("Fin de la entrada.");
            return linea;
        }
    }
}

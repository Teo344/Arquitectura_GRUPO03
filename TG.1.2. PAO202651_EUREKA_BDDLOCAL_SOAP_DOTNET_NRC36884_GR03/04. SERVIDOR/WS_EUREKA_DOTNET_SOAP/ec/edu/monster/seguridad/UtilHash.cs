using System;
using System.Security.Cryptography;
using System.Text;

namespace ec.edu.monster.seguridad
{
    /// <summary>
    /// Hash de contraseñas (RNF-007). Devuelve el SHA-256 en hexadecimal en minúsculas, el mismo
    /// formato con el que 02_carga_datos.sql guardó las claves (HASHBYTES('SHA2_256', ...)).
    /// </summary>
    public static class UtilHash
    {
        public static string Sha256(string texto)
        {
            using (SHA256 algoritmo = SHA256.Create())
            {
                byte[] hash = algoritmo.ComputeHash(Encoding.UTF8.GetBytes(texto));
                return BitConverter.ToString(hash).Replace("-", "").ToLowerInvariant();
            }
        }
    }
}

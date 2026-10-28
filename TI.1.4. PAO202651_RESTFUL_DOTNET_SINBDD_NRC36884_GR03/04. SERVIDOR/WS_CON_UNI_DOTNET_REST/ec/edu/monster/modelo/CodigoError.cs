namespace ec.edu.monster.modelo
{
    /// <summary>
    /// Códigos de error que viajan en el cuerpo JSON de las respuestas de error (RQF-010).
    /// </summary>
    public sealed class CodigoError
    {
        public static readonly CodigoError Auth001 = new CodigoError("AUTH-001", "Token inválido o ausente");
        public static readonly CodigoError Auth002 = new CodigoError("AUTH-002", "Sesión expirada");
        public static readonly CodigoError Auth003 = new CodigoError("AUTH-003", "Credenciales incorrectas");
        public static readonly CodigoError Unit001 = new CodigoError("UNIT-001", "Unidad no soportada");
        public static readonly CodigoError Data001 = new CodigoError("DATA-001", "Datos inconsistentes");

        public string Codigo { get; }
        public string Mensaje { get; }

        private CodigoError(string codigo, string mensaje)
        {
            Codigo = codigo;
            Mensaje = mensaje;
        }
    }
}

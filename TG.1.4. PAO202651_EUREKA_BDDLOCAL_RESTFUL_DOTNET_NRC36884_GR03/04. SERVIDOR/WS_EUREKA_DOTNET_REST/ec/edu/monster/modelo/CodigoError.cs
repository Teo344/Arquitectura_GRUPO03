namespace ec.edu.monster.modelo
{
    /// <summary>
    /// Códigos de error del ERS (sección 8.2.7) que viajan en el cuerpo JSON de las respuestas de error (RNF-009).
    /// </summary>
    public sealed class CodigoError
    {
        public static readonly CodigoError Auth001 = new CodigoError("AUTH-001", "Usuario o contraseña incorrectos");
        public static readonly CodigoError Auth002 = new CodigoError("AUTH-002", "El usuario no se encuentra activo");
        public static readonly CodigoError Auth003 = new CodigoError("AUTH-003", "Sesión inválida o expirada; inicie sesión nuevamente");
        public static readonly CodigoError Data001 = new CodigoError("DATA-001", "Datos incompletos o con formato inválido");
        public static readonly CodigoError Cta001 = new CodigoError("CTA-001", "La cuenta no existe");
        public static readonly CodigoError Cta002 = new CodigoError("CTA-002", "La cuenta no se encuentra activa");
        public static readonly CodigoError Mov001 = new CodigoError("MOV-001", "El importe debe ser mayor que cero y tener como máximo dos decimales");
        public static readonly CodigoError Mov002 = new CodigoError("MOV-002", "Saldo insuficiente para realizar la operación");
        public static readonly CodigoError Mov003 = new CodigoError("MOV-003", "La cuenta de destino debe ser diferente de la cuenta de origen");
        public static readonly CodigoError Mov004 = new CodigoError("MOV-004", "Las cuentas deben tener la misma moneda");
        public static readonly CodigoError Sys001 = new CodigoError("SYS-001", "No se pudo completar la operación; servicio o base de datos no disponible");

        public string Codigo { get; }
        public string Mensaje { get; }

        private CodigoError(string codigo, string mensaje)
        {
            Codigo = codigo;
            Mensaje = mensaje;
        }
    }
}

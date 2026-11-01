namespace ec.edu.monster.modelo
{
    /// <summary>Estados de usuarios y cuentas (CHECK de las tablas usuario y cuenta).</summary>
    public static class Estado
    {
        public const string Activo = "ACTIVO";
        public const string Anulado = "ANULADO";
        public const string Cancelado = "CANCELADO";

        public static bool EsActivo(string estado) => estado == Activo;
    }
}

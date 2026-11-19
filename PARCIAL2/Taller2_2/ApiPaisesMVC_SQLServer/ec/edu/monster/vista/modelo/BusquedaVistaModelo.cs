using ec.edu.monster.modelo.entidad;

namespace ec.edu.monster.vista.modelo
{
    /// <summary>Datos de BusquedaVista (Actividad 1): texto buscado y resultado de la API o del respaldo.</summary>
    public class BusquedaVistaModelo
    {
        /// <summary>Clave de TempData con el código del país recién guardado.</summary>
        public const string ClaveGuardado = "busqueda-guardado";

        public string? Nombre { get; set; }

        public ResultadoBusqueda? Resultado { get; set; }

        public string? Error { get; set; }

        /// <summary>Código del país que se acaba de guardar (muestra el chip "Guardado").</summary>
        public string? CodigoGuardado { get; set; }

        /// <summary>Códigos ya guardados en SQL Server; null si la base no respondió.</summary>
        public ISet<string>? CodigosGuardados { get; set; }

        /// <summary>El país ya está en la base (por el código o porque se acaba de guardar).</summary>
        public bool EstaGuardado(Pais pais) =>
            CodigoGuardado == pais.Codigo || (CodigosGuardados?.Contains(pais.Codigo) ?? false);

        public bool DesdeApi => Resultado?.Origen == OrigenDatos.API_EXTERNA;
    }
}

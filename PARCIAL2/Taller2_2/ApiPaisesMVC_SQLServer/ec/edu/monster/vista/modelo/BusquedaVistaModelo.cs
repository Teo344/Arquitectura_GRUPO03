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

        public bool DesdeApi => Resultado?.Origen == OrigenDatos.API_EXTERNA;
    }
}

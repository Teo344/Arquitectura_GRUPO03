using System;

namespace ec.edu.monster.modelo
{
    /// <summary>Comunica errores del servicio sin exponer tipos WCF a las vistas.</summary>
    public class ErrorSOAPModelo : Exception
    {
        public string Codigo { get; }

        public ErrorSOAPModelo(string mensaje) : base(mensaje)
        {
        }

        public ErrorSOAPModelo(string codigo, string mensaje) : base(mensaje)
        {
            Codigo = codigo;
        }

        public bool RequiereNuevoLogin => Codigo == "AUTH-001" || Codigo == "AUTH-002";
    }
}

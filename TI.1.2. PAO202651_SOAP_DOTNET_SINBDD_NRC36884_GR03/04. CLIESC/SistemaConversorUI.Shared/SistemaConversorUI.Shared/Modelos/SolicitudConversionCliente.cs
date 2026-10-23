using System;
using System.Collections.Generic;
using System.Text;

namespace SistemaConversorUI.Shared.Modelos
{
    public sealed record SolicitudConversionCliente(
    double Valor,
    string UnidadOrigen,
    string UnidadDestino);
}

using System;
using System.Collections.Generic;
using System.Text;

namespace SistemaConversorUI.Shared.Modelos
{
    public sealed record SesionCliente(string Usuario, string Token, string? ExpiraEn);
}

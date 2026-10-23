using System.Globalization;
using ec.edu.monster.controlador;
using ec.edu.monster.modelo;
using ec.edu.monster.util;

namespace ec.edu.monster.vista
{
    public class ConversionVista
    {
        private readonly MensajesVista mensajes = new MensajesVista();
        private readonly ConversionControlador conversionControlador;
        private readonly CatalogoControlador catalogoControlador;

        public ConversionVista(ConversionControlador conversionControlador, CatalogoControlador catalogoControlador)
        {
            this.conversionControlador = conversionControlador;
            this.catalogoControlador = catalogoControlador;
        }

        // true: volver al login; false: terminar el programa.
        public async Task<bool> EjecutarMenuAsync()
        {
            while (true)
            {
                mensajes.MostrarTitulo("MENU PRINCIPAL");
                mensajes.MostrarMensaje("1. Convertir Longitud");
                mensajes.MostrarMensaje("2. Convertir Masa");
                mensajes.MostrarMensaje("3. Convertir Temperatura");
                mensajes.MostrarMensaje("4. Consultar Catalogo de Unidades");
                mensajes.MostrarMensaje("5. Cerrar Sesion");
                mensajes.MostrarMensaje("0. Salir");

                int opcion = EntradaUtil.LeerEntero("Seleccione una opcion: ");
                try
                {
                    switch (opcion)
                    {
                        case 1: await ProcesarConversionAsync(TipoConversion.Longitud); break;
                        case 2: await ProcesarConversionAsync(TipoConversion.Masa); break;
                        case 3: await ProcesarConversionAsync(TipoConversion.Temperatura); break;
                        case 4: await new CatalogoVista(catalogoControlador).MostrarCatalogoAsync(); break;
                        case 5: return true;
                        case 0:
                            mensajes.MostrarMensaje("Saliendo del menu de conversion.");
                            return false;
                        default:
                            mensajes.MostrarError("Opcion no valida. Intente nuevamente.");
                            break;
                    }
                }
                catch (ErrorRESTModelo ex)
                {
                    mensajes.MostrarError(ex);
                    if (ex.RequiereNuevoLogin)
                        return true;
                }
                catch (ArgumentException ex)
                {
                    mensajes.MostrarError(ex.Message);
                }
            }
        }

        private async Task ProcesarConversionAsync(TipoConversion tipo)
        {
            mensajes.MostrarTitulo("CONVERSION DE " + tipo.ToString().ToUpperInvariant());
            List<UnidadModelo> unidades = await catalogoControlador.ConsultarUnidadesAsync(tipo.ToString().ToUpperInvariant());
            if (unidades.Count == 0)
            {
                mensajes.MostrarError("El servidor no devolvio unidades para esta categoria.");
                return;
            }

            double valor = EntradaUtil.LeerDecimal("Ingrese el valor a convertir: ");
            string unidadInicial = SolicitarUnidad("inicial", unidades);
            string unidadFinal = SolicitarUnidad("final", unidades);
            double resultado = await conversionControlador.ConvertirAsync(
                new ConversionModelo(tipo, valor, unidadInicial, unidadFinal));
            mensajes.MostrarMensaje(string.Format(CultureInfo.InvariantCulture,
                "Resultado: {0:F6} {1} = {2:F6} {3}", valor, unidadInicial, resultado, unidadFinal));
        }

        private string SolicitarUnidad(string tipoUnidad, List<UnidadModelo> unidades)
        {
            mensajes.MostrarMensaje("Seleccione la unidad " + tipoUnidad + ":");
            for (int indice = 0; indice < unidades.Count; indice++)
            {
                UnidadModelo unidad = unidades[indice];
                mensajes.MostrarMensaje((indice + 1) + ". " + unidad.Codigo + " - " + unidad.Nombre);
            }
            while (true)
            {
                int opcion = EntradaUtil.LeerEntero("Opcion de unidad: ");
                if (opcion >= 1 && opcion <= unidades.Count)
                    return unidades[opcion - 1].Codigo;
                mensajes.MostrarError("Unidad no valida. Elija una opcion de la lista.");
            }
        }
    }
}

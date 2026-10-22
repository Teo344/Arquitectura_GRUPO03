using System;
using System.Collections.Generic;
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
        public bool EjecutarMenu()
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
                        case 1: ProcesarConversion(TipoConversion.Longitud); break;
                        case 2: ProcesarConversion(TipoConversion.Masa); break;
                        case 3: ProcesarConversion(TipoConversion.Temperatura); break;
                        case 4: new CatalogoVista(catalogoControlador).MostrarCatalogo(); break;
                        case 5: return true;
                        case 0:
                            mensajes.MostrarMensaje("Saliendo del menu de conversion.");
                            return false;
                        default:
                            mensajes.MostrarError("Opcion no valida. Intente nuevamente.");
                            break;
                    }
                }
                catch (ErrorSOAPModelo ex)
                {
                    mensajes.MostrarError(ex);
                    if (ex.RequiereNuevoLogin) return true;
                }
                catch (ArgumentException ex)
                {
                    mensajes.MostrarError(ex.Message);
                }
            }
        }

        private void ProcesarConversion(TipoConversion tipo)
        {
            mensajes.MostrarTitulo("CONVERSION DE " + tipo.ToString().ToUpperInvariant());
            List<UnidadModelo> unidadesPermitidas = catalogoControlador.ConsultarUnidades(tipo.ToString().ToUpperInvariant());
            if (unidadesPermitidas.Count == 0)
            {
                mensajes.MostrarError("El servidor no devolvio unidades para esta categoria.");
                return;
            }

            double valor = EntradaUtil.LeerDecimal("Ingrese el valor a convertir: ");
            string unidadInicial = SolicitarUnidad("inicial", unidadesPermitidas);
            string unidadFinal = SolicitarUnidad("final", unidadesPermitidas);
            var modelo = new ConversionModelo(tipo, valor, unidadInicial, unidadFinal);
            double resultado = conversionControlador.Convertir(modelo);
            mensajes.MostrarMensaje(string.Format(CultureInfo.InvariantCulture,
                "Resultado: {0:F6} {1} = {2:F6} {3}", valor, unidadInicial, resultado, unidadFinal));
        }

        private string SolicitarUnidad(string tipoUnidad, List<UnidadModelo> unidadesPermitidas)
        {
            mensajes.MostrarMensaje("Seleccione la unidad " + tipoUnidad + ":");
            for (int indice = 0; indice < unidadesPermitidas.Count; indice++)
            {
                UnidadModelo unidad = unidadesPermitidas[indice];
                mensajes.MostrarMensaje((indice + 1) + ". " + unidad.Codigo + " - " + unidad.Nombre);
            }
            while (true)
            {
                int opcion = EntradaUtil.LeerEntero("Opcion de unidad: ");
                if (opcion >= 1 && opcion <= unidadesPermitidas.Count)
                    return unidadesPermitidas[opcion - 1].Codigo;
                mensajes.MostrarError("Unidad no valida. Elija una opcion de la lista.");
            }
        }
    }
}

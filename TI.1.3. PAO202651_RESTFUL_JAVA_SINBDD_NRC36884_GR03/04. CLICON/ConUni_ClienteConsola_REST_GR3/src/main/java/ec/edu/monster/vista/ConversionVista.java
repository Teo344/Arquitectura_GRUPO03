package ec.edu.monster.vista;

import ec.edu.monster.controlador.CatalogoControlador;
import ec.edu.monster.controlador.ConversionControlador;
import ec.edu.monster.modelo.ConversionModelo;
import ec.edu.monster.modelo.ErrorRESTModelo;
import ec.edu.monster.modelo.TipoConversion;
import ec.edu.monster.modelo.UnidadModelo;
import ec.edu.monster.util.EntradaUtil;
import java.util.List;

public class ConversionVista {

    private final MensajesVista mensajes = new MensajesVista();
    private final ConversionControlador conversionControlador;
    private final CatalogoControlador catalogoControlador;

    public ConversionVista(ConversionControlador conversionControlador,
            CatalogoControlador catalogoControlador) {
        this.conversionControlador = conversionControlador;
        this.catalogoControlador = catalogoControlador;
    }

    // true: volver al login; false: terminar el programa.
    public boolean ejecutarMenu() {
        while (true) {
            mensajes.mostrarTitulo("MENU PRINCIPAL");
            mensajes.mostrarMensaje("1. Convertir Longitud");
            mensajes.mostrarMensaje("2. Convertir Masa");
            mensajes.mostrarMensaje("3. Convertir Temperatura");
            mensajes.mostrarMensaje("4. Consultar Catalogo de Unidades");
            mensajes.mostrarMensaje("5. Cerrar Sesion");
            mensajes.mostrarMensaje("0. Salir");

            int opcion = EntradaUtil.leerEntero("Seleccione una opcion: ");
            try {
                switch (opcion) {
                    case 1 -> procesarConversion(TipoConversion.LONGITUD);
                    case 2 -> procesarConversion(TipoConversion.MASA);
                    case 3 -> procesarConversion(TipoConversion.TEMPERATURA);
                    case 4 -> new CatalogoVista(catalogoControlador).mostrarCatalogo();
                    case 5 -> { return true; }
                    case 0 -> {
                        mensajes.mostrarMensaje("Saliendo del menu de conversion.");
                        return false;
                    }
                    default -> mensajes.mostrarError(
                            "Opcion no valida. Intente nuevamente.");
                }
            } catch (ErrorRESTModelo ex) {
                String codigo = ex.getCodigo() == null ? "" : ex.getCodigo() + ": ";
                mensajes.mostrarError(codigo + ex.getMessage());
                if (ex.requiereNuevoLogin()) {
                    return true;
                }
            } catch (RuntimeException ex) {
                mensajes.mostrarError(ex.getMessage());
            }
        }
    }

    private void procesarConversion(TipoConversion tipo) {
        mensajes.mostrarTitulo("CONVERSION DE "
                + tipo.getNombreVisible().toUpperCase());
        List<UnidadModelo> unidadesPermitidas = catalogoControlador
                .consultarUnidades(tipo.name());
        if (unidadesPermitidas.isEmpty()) {
            mensajes.mostrarError("El servidor no devolvio unidades para esta categoria.");
            return;
        }

        double valor = EntradaUtil.leerDecimal("Ingrese el valor a convertir: ");
        String unidadInicial = solicitarUnidad("inicial", unidadesPermitidas);
        String unidadFinal = solicitarUnidad("final", unidadesPermitidas);
        ConversionModelo modelo = new ConversionModelo(
                tipo, valor, unidadInicial, unidadFinal);
        double resultado = conversionControlador.convertir(modelo);
        mensajes.mostrarMensaje(String.format(
                "Resultado: %.6f %s = %.6f %s",
                valor, unidadInicial, resultado, unidadFinal));
    }

    private String solicitarUnidad(String tipoUnidad,
            List<UnidadModelo> unidadesPermitidas) {
        mensajes.mostrarMensaje("Seleccione la unidad " + tipoUnidad + ":");
        for (int indice = 0; indice < unidadesPermitidas.size(); indice++) {
            UnidadModelo unidad = unidadesPermitidas.get(indice);
            mensajes.mostrarMensaje((indice + 1) + ". " + unidad.getCodigo()
                    + " - " + unidad.getNombre());
        }
        while (true) {
            int opcion = EntradaUtil.leerEntero("Opcion de unidad: ");
            if (opcion >= 1 && opcion <= unidadesPermitidas.size()) {
                return unidadesPermitidas.get(opcion - 1).getCodigo();
            }
            mensajes.mostrarError("Unidad no valida. Elija una opcion de la lista.");
        }
    }
}

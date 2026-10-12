package ec.edu.monster.vista;

import ec.edu.monster.controlador.ConversionControlador;
import ec.edu.monster.modelo.ConversionModelo;
import ec.edu.monster.modelo.MensajesModelo;
import ec.edu.monster.modelo.TipoConversion;
import ec.edu.monster.util.EntradaUtil;
import jakarta.xml.ws.soap.SOAPFaultException;

public class ConversionVista {

    private static final String[] UNIDADES_LONGITUD = {
        "M", "CM", "KM", "FT", "YD", "MI"
    };

    private static final String[] UNIDADES_MASA = {
        "G", "KG", "OZ", "LB", "QQ"
    };

    private static final String[] UNIDADES_TEMPERATURA = {
        "C", "K", "F", "N", "RE"
    };

    private final MensajesModelo mensajes = new MensajesModelo();
    private final ConversionControlador conversionControlador;

    public ConversionVista(ConversionControlador conversionControlador) {
        this.conversionControlador = conversionControlador;
    }

    public void ejecutarMenu() {
        int opcion;

        do {
            mensajes.mostrarTitulo("MENU PRINCIPAL");
            mensajes.mostrarMensaje("1. Convertir Longitud");
            mensajes.mostrarMensaje("2. Convertir Masa");
            mensajes.mostrarMensaje("3. Convertir Temperatura");
            mensajes.mostrarMensaje("0. Salir");

            opcion = EntradaUtil.leerEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1 -> procesarConversion(TipoConversion.LONGITUD, UNIDADES_LONGITUD);
                case 2 -> procesarConversion(TipoConversion.MASA, UNIDADES_MASA);
                case 3 -> procesarConversion(TipoConversion.TEMPERATURA, UNIDADES_TEMPERATURA);
                case 0 -> mensajes.mostrarMensaje("Saliendo del menu de conversion.");
                default -> mensajes.mostrarError("Opcion no valida. Intente nuevamente.");
            }
        } while (opcion != 0);
    }

    private void procesarConversion(TipoConversion tipo, String[] unidadesPermitidas) {
        mensajes.mostrarTitulo("CONVERSION DE " + tipo.getNombreVisible().toUpperCase());

        double valor = EntradaUtil.leerDecimal("Ingrese el valor a convertir: ");
        String unidadInicial = solicitarUnidad("inicial", unidadesPermitidas);
        String unidadFinal = solicitarUnidad("final", unidadesPermitidas);

        ConversionModelo modelo = new ConversionModelo(tipo, valor, unidadInicial, unidadFinal);

        try {
            double resultado = conversionControlador.convertir(modelo);
            String mensaje = String.format("Resultado: %.6f %s = %.6f %s", valor, unidadInicial, resultado, unidadFinal);
            mensajes.mostrarMensaje(mensaje);
        } catch (SOAPFaultException ex) {
            mensajes.mostrarError(ex.getFault().getFaultString());
        } catch (Exception ex) {
            mensajes.mostrarError(ex.getMessage());
        }
    }

    private String solicitarUnidad(String tipoUnidad, String[] unidadesPermitidas) {
        mensajes.mostrarMensaje("Seleccione la unidad " + tipoUnidad + ":");
        for (int indice = 0; indice < unidadesPermitidas.length; indice++) {
            mensajes.mostrarMensaje((indice + 1) + ". " + unidadesPermitidas[indice]);
        }

        while (true) {
            int opcion = EntradaUtil.leerEntero("Opcion de unidad: ");
            if (opcion >= 1 && opcion <= unidadesPermitidas.length) {
                return unidadesPermitidas[opcion - 1];
            }
            mensajes.mostrarError("Unidad no valida. Elija una opcion de la lista.");
        }
    }
}

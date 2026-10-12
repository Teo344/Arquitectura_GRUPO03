package ec.edu.monster.controlador;

import client.ws.ClienteWSConversor;
import ec.edu.monster.modelo.ConversionModelo;
import ec.edu.monster.modelo.TipoConversion;

public class ConversionControlador {

    private final ClienteWSConversor clienteWs;

    public ConversionControlador(ClienteWSConversor clienteWs) {
        this.clienteWs = clienteWs;
    }

    public double convertir(ConversionModelo modelo) {
        validarSolicitud(modelo);

        TipoConversion tipo = modelo.getTipoConversion();

        return switch (tipo) {
            case LONGITUD -> clienteWs.convertirLongitud(
                    modelo.getValor(),
                    modelo.getUnidadInicial(),
                    modelo.getUnidadFinal());
            case MASA -> clienteWs.convertirMasa(
                    modelo.getValor(),
                    modelo.getUnidadInicial(),
                    modelo.getUnidadFinal());
            case TEMPERATURA -> clienteWs.convertirTemperatura(
                    modelo.getValor(),
                    modelo.getUnidadInicial(),
                    modelo.getUnidadFinal());
        };
    }

    private void validarSolicitud(ConversionModelo modelo) {
        if (modelo == null) {
            throw new IllegalArgumentException("La solicitud de conversion es obligatoria.");
        }
        if (modelo.getTipoConversion() == null) {
            throw new IllegalArgumentException("Debe seleccionar el tipo de conversion.");
        }
        if (modelo.getUnidadInicial() == null || modelo.getUnidadInicial().isBlank()) {
            throw new IllegalArgumentException("La unidad inicial es obligatoria.");
        }
        if (modelo.getUnidadFinal() == null || modelo.getUnidadFinal().isBlank()) {
            throw new IllegalArgumentException("La unidad final es obligatoria.");
        }
    }
}

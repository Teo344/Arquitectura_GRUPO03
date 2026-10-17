package ec.edu.monster.controlador;

import ec.edu.monster.modelo.ConversionModelo;
import ec.edu.monster.modelo.TipoConversion;
import ec.edu.monster.servicio.ClienteRESTConversor;

public class ConversionControlador {

    private final ClienteRESTConversor clienteRest;

    public ConversionControlador(ClienteRESTConversor clienteRest) {
        this.clienteRest = clienteRest;
    }

    public double convertir(ConversionModelo modelo) {
        validarSolicitud(modelo);
        TipoConversion tipo = modelo.getTipoConversion();
        return switch (tipo) {
            case LONGITUD -> clienteRest.convertirLongitud(
                    modelo.getValor(), modelo.getUnidadInicial(), modelo.getUnidadFinal());
            case MASA -> clienteRest.convertirMasa(
                    modelo.getValor(), modelo.getUnidadInicial(), modelo.getUnidadFinal());
            case TEMPERATURA -> clienteRest.convertirTemperatura(
                    modelo.getValor(), modelo.getUnidadInicial(), modelo.getUnidadFinal());
        };
    }

    private void validarSolicitud(ConversionModelo modelo) {
        if (modelo == null) {
            throw new IllegalArgumentException("La solicitud de conversión es obligatoria.");
        }
        if (modelo.getTipoConversion() == null) {
            throw new IllegalArgumentException("Debe seleccionar el tipo de conversión.");
        }
        if (!Double.isFinite(modelo.getValor())) {
            throw new IllegalArgumentException("El valor debe ser un número finito.");
        }
        if (modelo.getUnidadInicial() == null || modelo.getUnidadInicial().isBlank()) {
            throw new IllegalArgumentException("La unidad inicial es obligatoria.");
        }
        if (modelo.getUnidadFinal() == null || modelo.getUnidadFinal().isBlank()) {
            throw new IllegalArgumentException("La unidad final es obligatoria.");
        }
    }
}

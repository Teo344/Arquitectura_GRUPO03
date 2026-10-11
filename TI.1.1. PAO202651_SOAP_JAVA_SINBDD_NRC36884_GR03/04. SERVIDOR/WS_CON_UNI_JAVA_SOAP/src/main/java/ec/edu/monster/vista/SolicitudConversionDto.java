package ec.edu.monster.vista;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Solicitud de conversión. El token no va aquí: viaja en la cabecera SOAP.
 *
 * @author carlo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "solicitudConversion", propOrder = {"valor", "unidadOrigen", "unidadDestino"})
public class SolicitudConversionDto {

    public Double valor;
    public String unidadOrigen;
    public String unidadDestino;
}

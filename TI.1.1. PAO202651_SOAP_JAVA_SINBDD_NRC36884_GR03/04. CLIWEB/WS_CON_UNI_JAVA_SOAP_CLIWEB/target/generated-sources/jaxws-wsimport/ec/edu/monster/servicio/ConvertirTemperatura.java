
package ec.edu.monster.servicio;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para convertirTemperatura complex type.</p>
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.</p>
 * 
 * <pre>{@code
 * <complexType name="convertirTemperatura">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="solicitud" type="{http://wservicios.controladores.monster.edu.ec/}solicitudConversion" minOccurs="0"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "convertirTemperatura", propOrder = {
    "solicitud"
})
public class ConvertirTemperatura {

    protected SolicitudConversion solicitud;

    /**
     * Obtiene el valor de la propiedad solicitud.
     * 
     * @return
     *     possible object is
     *     {@link SolicitudConversion }
     *     
     */
    public SolicitudConversion getSolicitud() {
        return solicitud;
    }

    /**
     * Define el valor de la propiedad solicitud.
     * 
     * @param value
     *     allowed object is
     *     {@link SolicitudConversion }
     *     
     */
    public void setSolicitud(SolicitudConversion value) {
        this.solicitud = value;
    }

}

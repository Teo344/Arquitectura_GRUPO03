
package ec.edu.monster.servicio;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para cambiarClave complex type.</p>
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.</p>
 * 
 * <pre>{@code
 * <complexType name="cambiarClave">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="usuario" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="claveActual" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="claveNueva" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cambiarClave", propOrder = {
    "usuario",
    "claveActual",
    "claveNueva"
})
public class CambiarClave {

    protected String usuario;
    protected String claveActual;
    protected String claveNueva;

    /**
     * Obtiene el valor de la propiedad usuario.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsuario() {
        return usuario;
    }

    /**
     * Define el valor de la propiedad usuario.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsuario(String value) {
        this.usuario = value;
    }

    /**
     * Obtiene el valor de la propiedad claveActual.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClaveActual() {
        return claveActual;
    }

    /**
     * Define el valor de la propiedad claveActual.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClaveActual(String value) {
        this.claveActual = value;
    }

    /**
     * Obtiene el valor de la propiedad claveNueva.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClaveNueva() {
        return claveNueva;
    }

    /**
     * Define el valor de la propiedad claveNueva.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClaveNueva(String value) {
        this.claveNueva = value;
    }

}

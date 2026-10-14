
package ec.edu.monster.servicio;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para respuesta complex type.</p>
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.</p>
 * 
 * <pre>{@code
 * <complexType name="respuesta">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="estado" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="codigo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="mensaje" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="resultado" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/>
 *         <element name="unidadOrigen" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="unidadDestino" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="token" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="expiraEn" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="categorias" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <element name="categoria" type="{http://wservicios.controladores.monster.edu.ec/}categoria" maxOccurs="unbounded" minOccurs="0"/>
 *                 </sequence>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="unidades" minOccurs="0">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <element name="unidad" type="{http://wservicios.controladores.monster.edu.ec/}unidad" maxOccurs="unbounded" minOccurs="0"/>
 *                 </sequence>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "respuesta", propOrder = {
    "estado",
    "codigo",
    "mensaje",
    "resultado",
    "unidadOrigen",
    "unidadDestino",
    "token",
    "expiraEn",
    "categorias",
    "unidades"
})
public class Respuesta {

    protected String estado;
    protected String codigo;
    protected String mensaje;
    protected Double resultado;
    protected String unidadOrigen;
    protected String unidadDestino;
    protected String token;
    protected String expiraEn;
    protected Respuesta.Categorias categorias;
    protected Respuesta.Unidades unidades;

    /**
     * Obtiene el valor de la propiedad estado.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEstado() {
        return estado;
    }

    /**
     * Define el valor de la propiedad estado.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEstado(String value) {
        this.estado = value;
    }

    /**
     * Obtiene el valor de la propiedad codigo.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * Define el valor de la propiedad codigo.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodigo(String value) {
        this.codigo = value;
    }

    /**
     * Obtiene el valor de la propiedad mensaje.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMensaje() {
        return mensaje;
    }

    /**
     * Define el valor de la propiedad mensaje.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMensaje(String value) {
        this.mensaje = value;
    }

    /**
     * Obtiene el valor de la propiedad resultado.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getResultado() {
        return resultado;
    }

    /**
     * Define el valor de la propiedad resultado.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setResultado(Double value) {
        this.resultado = value;
    }

    /**
     * Obtiene el valor de la propiedad unidadOrigen.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnidadOrigen() {
        return unidadOrigen;
    }

    /**
     * Define el valor de la propiedad unidadOrigen.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnidadOrigen(String value) {
        this.unidadOrigen = value;
    }

    /**
     * Obtiene el valor de la propiedad unidadDestino.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnidadDestino() {
        return unidadDestino;
    }

    /**
     * Define el valor de la propiedad unidadDestino.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnidadDestino(String value) {
        this.unidadDestino = value;
    }

    /**
     * Obtiene el valor de la propiedad token.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getToken() {
        return token;
    }

    /**
     * Define el valor de la propiedad token.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setToken(String value) {
        this.token = value;
    }

    /**
     * Obtiene el valor de la propiedad expiraEn.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExpiraEn() {
        return expiraEn;
    }

    /**
     * Define el valor de la propiedad expiraEn.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExpiraEn(String value) {
        this.expiraEn = value;
    }

    /**
     * Obtiene el valor de la propiedad categorias.
     * 
     * @return
     *     possible object is
     *     {@link Respuesta.Categorias }
     *     
     */
    public Respuesta.Categorias getCategorias() {
        return categorias;
    }

    /**
     * Define el valor de la propiedad categorias.
     * 
     * @param value
     *     allowed object is
     *     {@link Respuesta.Categorias }
     *     
     */
    public void setCategorias(Respuesta.Categorias value) {
        this.categorias = value;
    }

    /**
     * Obtiene el valor de la propiedad unidades.
     * 
     * @return
     *     possible object is
     *     {@link Respuesta.Unidades }
     *     
     */
    public Respuesta.Unidades getUnidades() {
        return unidades;
    }

    /**
     * Define el valor de la propiedad unidades.
     * 
     * @param value
     *     allowed object is
     *     {@link Respuesta.Unidades }
     *     
     */
    public void setUnidades(Respuesta.Unidades value) {
        this.unidades = value;
    }


    /**
     * <p>Clase Java para anonymous complex type.</p>
     * 
     * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <complexContent>
     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       <sequence>
     *         <element name="categoria" type="{http://wservicios.controladores.monster.edu.ec/}categoria" maxOccurs="unbounded" minOccurs="0"/>
     *       </sequence>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "categoria"
    })
    public static class Categorias {

        protected List<Categoria> categoria;

        /**
         * Gets the value of the categoria property.
         * 
         * <p>This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the categoria property.</p>
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * </p>
         * <pre>
         * getCategoria().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Categoria }
         * </p>
         * 
         * 
         * @return
         *     The value of the categoria property.
         */
        public List<Categoria> getCategoria() {
            if (categoria == null) {
                categoria = new ArrayList<>();
            }
            return this.categoria;
        }

    }


    /**
     * <p>Clase Java para anonymous complex type.</p>
     * 
     * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.</p>
     * 
     * <pre>{@code
     * <complexType>
     *   <complexContent>
     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       <sequence>
     *         <element name="unidad" type="{http://wservicios.controladores.monster.edu.ec/}unidad" maxOccurs="unbounded" minOccurs="0"/>
     *       </sequence>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }</pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "unidad"
    })
    public static class Unidades {

        protected List<Unidad> unidad;

        /**
         * Gets the value of the unidad property.
         * 
         * <p>This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the unidad property.</p>
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * </p>
         * <pre>
         * getUnidad().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Unidad }
         * </p>
         * 
         * 
         * @return
         *     The value of the unidad property.
         */
        public List<Unidad> getUnidad() {
            if (unidad == null) {
                unidad = new ArrayList<>();
            }
            return this.unidad;
        }

    }

}

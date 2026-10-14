
package ec.edu.monster.servicio;

import javax.xml.namespace.QName;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ec.edu.monster.servicio package. 
 * <p>An ObjectFactory allows you to programmatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private static final QName _ErrorServicio_QNAME = new QName("http://wservicios.controladores.monster.edu.ec/", "ErrorServicio");
    private static final QName _ConsultarCatalogo_QNAME = new QName("http://wservicios.controladores.monster.edu.ec/", "consultarCatalogo");
    private static final QName _ConsultarCatalogoResponse_QNAME = new QName("http://wservicios.controladores.monster.edu.ec/", "consultarCatalogoResponse");
    private static final QName _ConsultarUnidades_QNAME = new QName("http://wservicios.controladores.monster.edu.ec/", "consultarUnidades");
    private static final QName _ConsultarUnidadesResponse_QNAME = new QName("http://wservicios.controladores.monster.edu.ec/", "consultarUnidadesResponse");
    private static final QName _ConvertirLongitud_QNAME = new QName("http://wservicios.controladores.monster.edu.ec/", "convertirLongitud");
    private static final QName _ConvertirLongitudResponse_QNAME = new QName("http://wservicios.controladores.monster.edu.ec/", "convertirLongitudResponse");
    private static final QName _ConvertirMasa_QNAME = new QName("http://wservicios.controladores.monster.edu.ec/", "convertirMasa");
    private static final QName _ConvertirMasaResponse_QNAME = new QName("http://wservicios.controladores.monster.edu.ec/", "convertirMasaResponse");
    private static final QName _ConvertirTemperatura_QNAME = new QName("http://wservicios.controladores.monster.edu.ec/", "convertirTemperatura");
    private static final QName _ConvertirTemperaturaResponse_QNAME = new QName("http://wservicios.controladores.monster.edu.ec/", "convertirTemperaturaResponse");
    private static final QName _Token_QNAME = new QName("http://wservicios.controladores.monster.edu.ec/", "token");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ec.edu.monster.servicio
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Respuesta }
     * 
     * @return
     *     the new instance of {@link Respuesta }
     */
    public Respuesta createRespuesta() {
        return new Respuesta();
    }

    /**
     * Create an instance of {@link ErrorServicio }
     * 
     * @return
     *     the new instance of {@link ErrorServicio }
     */
    public ErrorServicio createErrorServicio() {
        return new ErrorServicio();
    }

    /**
     * Create an instance of {@link ConsultarCatalogo }
     * 
     * @return
     *     the new instance of {@link ConsultarCatalogo }
     */
    public ConsultarCatalogo createConsultarCatalogo() {
        return new ConsultarCatalogo();
    }

    /**
     * Create an instance of {@link ConsultarCatalogoResponse }
     * 
     * @return
     *     the new instance of {@link ConsultarCatalogoResponse }
     */
    public ConsultarCatalogoResponse createConsultarCatalogoResponse() {
        return new ConsultarCatalogoResponse();
    }

    /**
     * Create an instance of {@link ConsultarUnidades }
     * 
     * @return
     *     the new instance of {@link ConsultarUnidades }
     */
    public ConsultarUnidades createConsultarUnidades() {
        return new ConsultarUnidades();
    }

    /**
     * Create an instance of {@link ConsultarUnidadesResponse }
     * 
     * @return
     *     the new instance of {@link ConsultarUnidadesResponse }
     */
    public ConsultarUnidadesResponse createConsultarUnidadesResponse() {
        return new ConsultarUnidadesResponse();
    }

    /**
     * Create an instance of {@link ConvertirLongitud }
     * 
     * @return
     *     the new instance of {@link ConvertirLongitud }
     */
    public ConvertirLongitud createConvertirLongitud() {
        return new ConvertirLongitud();
    }

    /**
     * Create an instance of {@link ConvertirLongitudResponse }
     * 
     * @return
     *     the new instance of {@link ConvertirLongitudResponse }
     */
    public ConvertirLongitudResponse createConvertirLongitudResponse() {
        return new ConvertirLongitudResponse();
    }

    /**
     * Create an instance of {@link ConvertirMasa }
     * 
     * @return
     *     the new instance of {@link ConvertirMasa }
     */
    public ConvertirMasa createConvertirMasa() {
        return new ConvertirMasa();
    }

    /**
     * Create an instance of {@link ConvertirMasaResponse }
     * 
     * @return
     *     the new instance of {@link ConvertirMasaResponse }
     */
    public ConvertirMasaResponse createConvertirMasaResponse() {
        return new ConvertirMasaResponse();
    }

    /**
     * Create an instance of {@link ConvertirTemperatura }
     * 
     * @return
     *     the new instance of {@link ConvertirTemperatura }
     */
    public ConvertirTemperatura createConvertirTemperatura() {
        return new ConvertirTemperatura();
    }

    /**
     * Create an instance of {@link ConvertirTemperaturaResponse }
     * 
     * @return
     *     the new instance of {@link ConvertirTemperaturaResponse }
     */
    public ConvertirTemperaturaResponse createConvertirTemperaturaResponse() {
        return new ConvertirTemperaturaResponse();
    }

    /**
     * Create an instance of {@link Categoria }
     * 
     * @return
     *     the new instance of {@link Categoria }
     */
    public Categoria createCategoria() {
        return new Categoria();
    }

    /**
     * Create an instance of {@link Unidad }
     * 
     * @return
     *     the new instance of {@link Unidad }
     */
    public Unidad createUnidad() {
        return new Unidad();
    }

    /**
     * Create an instance of {@link SolicitudConversion }
     * 
     * @return
     *     the new instance of {@link SolicitudConversion }
     */
    public SolicitudConversion createSolicitudConversion() {
        return new SolicitudConversion();
    }

    /**
     * Create an instance of {@link Respuesta.Categorias }
     * 
     * @return
     *     the new instance of {@link Respuesta.Categorias }
     */
    public Respuesta.Categorias createRespuestaCategorias() {
        return new Respuesta.Categorias();
    }

    /**
     * Create an instance of {@link Respuesta.Unidades }
     * 
     * @return
     *     the new instance of {@link Respuesta.Unidades }
     */
    public Respuesta.Unidades createRespuestaUnidades() {
        return new Respuesta.Unidades();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ErrorServicio }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ErrorServicio }{@code >}
     */
    @XmlElementDecl(namespace = "http://wservicios.controladores.monster.edu.ec/", name = "ErrorServicio")
    public JAXBElement<ErrorServicio> createErrorServicio(ErrorServicio value) {
        return new JAXBElement<>(_ErrorServicio_QNAME, ErrorServicio.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConsultarCatalogo }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ConsultarCatalogo }{@code >}
     */
    @XmlElementDecl(namespace = "http://wservicios.controladores.monster.edu.ec/", name = "consultarCatalogo")
    public JAXBElement<ConsultarCatalogo> createConsultarCatalogo(ConsultarCatalogo value) {
        return new JAXBElement<>(_ConsultarCatalogo_QNAME, ConsultarCatalogo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConsultarCatalogoResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ConsultarCatalogoResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://wservicios.controladores.monster.edu.ec/", name = "consultarCatalogoResponse")
    public JAXBElement<ConsultarCatalogoResponse> createConsultarCatalogoResponse(ConsultarCatalogoResponse value) {
        return new JAXBElement<>(_ConsultarCatalogoResponse_QNAME, ConsultarCatalogoResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConsultarUnidades }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ConsultarUnidades }{@code >}
     */
    @XmlElementDecl(namespace = "http://wservicios.controladores.monster.edu.ec/", name = "consultarUnidades")
    public JAXBElement<ConsultarUnidades> createConsultarUnidades(ConsultarUnidades value) {
        return new JAXBElement<>(_ConsultarUnidades_QNAME, ConsultarUnidades.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConsultarUnidadesResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ConsultarUnidadesResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://wservicios.controladores.monster.edu.ec/", name = "consultarUnidadesResponse")
    public JAXBElement<ConsultarUnidadesResponse> createConsultarUnidadesResponse(ConsultarUnidadesResponse value) {
        return new JAXBElement<>(_ConsultarUnidadesResponse_QNAME, ConsultarUnidadesResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConvertirLongitud }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ConvertirLongitud }{@code >}
     */
    @XmlElementDecl(namespace = "http://wservicios.controladores.monster.edu.ec/", name = "convertirLongitud")
    public JAXBElement<ConvertirLongitud> createConvertirLongitud(ConvertirLongitud value) {
        return new JAXBElement<>(_ConvertirLongitud_QNAME, ConvertirLongitud.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConvertirLongitudResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ConvertirLongitudResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://wservicios.controladores.monster.edu.ec/", name = "convertirLongitudResponse")
    public JAXBElement<ConvertirLongitudResponse> createConvertirLongitudResponse(ConvertirLongitudResponse value) {
        return new JAXBElement<>(_ConvertirLongitudResponse_QNAME, ConvertirLongitudResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConvertirMasa }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ConvertirMasa }{@code >}
     */
    @XmlElementDecl(namespace = "http://wservicios.controladores.monster.edu.ec/", name = "convertirMasa")
    public JAXBElement<ConvertirMasa> createConvertirMasa(ConvertirMasa value) {
        return new JAXBElement<>(_ConvertirMasa_QNAME, ConvertirMasa.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConvertirMasaResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ConvertirMasaResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://wservicios.controladores.monster.edu.ec/", name = "convertirMasaResponse")
    public JAXBElement<ConvertirMasaResponse> createConvertirMasaResponse(ConvertirMasaResponse value) {
        return new JAXBElement<>(_ConvertirMasaResponse_QNAME, ConvertirMasaResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConvertirTemperatura }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ConvertirTemperatura }{@code >}
     */
    @XmlElementDecl(namespace = "http://wservicios.controladores.monster.edu.ec/", name = "convertirTemperatura")
    public JAXBElement<ConvertirTemperatura> createConvertirTemperatura(ConvertirTemperatura value) {
        return new JAXBElement<>(_ConvertirTemperatura_QNAME, ConvertirTemperatura.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ConvertirTemperaturaResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ConvertirTemperaturaResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://wservicios.controladores.monster.edu.ec/", name = "convertirTemperaturaResponse")
    public JAXBElement<ConvertirTemperaturaResponse> createConvertirTemperaturaResponse(ConvertirTemperaturaResponse value) {
        return new JAXBElement<>(_ConvertirTemperaturaResponse_QNAME, ConvertirTemperaturaResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://wservicios.controladores.monster.edu.ec/", name = "token")
    public JAXBElement<String> createToken(String value) {
        return new JAXBElement<>(_Token_QNAME, String.class, null, value);
    }

}

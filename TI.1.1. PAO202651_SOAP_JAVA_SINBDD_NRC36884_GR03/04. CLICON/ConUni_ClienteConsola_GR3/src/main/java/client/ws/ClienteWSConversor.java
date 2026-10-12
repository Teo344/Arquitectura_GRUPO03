package client.ws;

import jakarta.xml.ws.Binding;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.handler.Handler;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

public class ClienteWSConversor {

    private static final String ESPACIO_NOMBRES = "http://wservicios.controladores.monster.edu.ec/";
    private static final String PROPIEDAD_BASE_WS = "ws.base.url";
    private static final String VARIABLE_BASE_WS = "WS_BASE_URL";

    private static final QName QNOMBRE_SERVICIO = new QName(ESPACIO_NOMBRES, "ConversionUnidadesWS");
    private static final QName QNOMBRE_PUERTO = new QName(ESPACIO_NOMBRES, "ConversionUnidadesWSPort");
    private static final QName QNOMBRE_SERVICIO_LOGIN = new QName(ESPACIO_NOMBRES, "AutenticacionWS");
    private static final QName QNOMBRE_PUERTO_LOGIN = new QName(ESPACIO_NOMBRES, "AutenticacionWSPort");

    private static final List<String> BASES_URL_POR_DEFECTO = List.of(
            "http://localhost:8080/WS_CON_UNI_JAVA_SOAP"
    );

    private final ContratoWSConversor puerto;
    private ContratoWSConversor.Autenticacion puertoLogin;
    private String tokenSesion;
    private String endpointActivo;

    public ClienteWSConversor() {
        this.puerto = inicializarPuerto();
    }

    private ContratoWSConversor inicializarPuerto() {
        List<String> basesUrl = construirBasesUrl();
        List<String> wsdlsIntentados = new ArrayList<>();

        for (String baseUrl : basesUrl) {
            String endpoint = baseUrl + "/ConversionUnidadesWS";
            List<String> wsdlsCandidatos = List.of(
                    endpoint + "?WSDL",
                    endpoint + "?wsdl"
            );

            for (String wsdl : wsdlsCandidatos) {
                try {
                    Service servicio = Service.create(new URL(wsdl), QNOMBRE_SERVICIO);
                    ContratoWSConversor puertoTemporal = servicio.getPort(QNOMBRE_PUERTO, ContratoWSConversor.class);

                    BindingProvider proveedor = (BindingProvider) puertoTemporal;
                    proveedor.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
                    configurarManejadorToken(proveedor);

                    Service servicioLogin = Service.create(new URL(baseUrl + "/AutenticacionWS?wsdl"), QNOMBRE_SERVICIO_LOGIN);
                    this.puertoLogin = servicioLogin.getPort(QNOMBRE_PUERTO_LOGIN, ContratoWSConversor.Autenticacion.class);

                    this.endpointActivo = endpoint;
                    return puertoTemporal;
                } catch (Exception ex) {
                    wsdlsIntentados.add(wsdl);
                }
            }
        }

        throw new IllegalStateException(
                "No fue posible inicializar el cliente SOAP. "
                + "No se encontro el WSDL en ninguna URL candidata: " + String.join(", ", wsdlsIntentados)
                + ". Verifique que 04. SERVIDOR este desplegado y activo en Payara."
        );
    }

    private List<String> construirBasesUrl() {
        List<String> basesUrl = new ArrayList<>();

        String baseConfigurada = obtenerBaseConfigurada();
        if (baseConfigurada != null && !baseConfigurada.isBlank()) {
            basesUrl.add(normalizarBase(baseConfigurada));
        }

        for (String baseDefecto : BASES_URL_POR_DEFECTO) {
            if (!basesUrl.contains(baseDefecto)) {
                basesUrl.add(baseDefecto);
            }
        }

        return basesUrl;
    }

    private String obtenerBaseConfigurada() {
        String desdePropiedad = System.getProperty(PROPIEDAD_BASE_WS);
        if (desdePropiedad != null && !desdePropiedad.isBlank()) {
            return desdePropiedad;
        }

        String desdeVariable = System.getenv(VARIABLE_BASE_WS);
        if (desdeVariable != null && !desdeVariable.isBlank()) {
            return desdeVariable;
        }

        return null;
    }

    private String normalizarBase(String base) {
        String baseNormalizada = base.trim();
        while (baseNormalizada.endsWith("/")) {
            baseNormalizada = baseNormalizada.substring(0, baseNormalizada.length() - 1);
        }
        return baseNormalizada;
    }

    private void configurarManejadorToken(BindingProvider proveedor) {
        Binding enlace = proveedor.getBinding();
        List<Handler> cadenaManejadores = new ArrayList<>(enlace.getHandlerChain());
        cadenaManejadores.add(new ManejadorTokenSOAP(() -> tokenSesion));
        enlace.setHandlerChain(cadenaManejadores);
    }

    public String login(String usuario, String contrasenia) {
        this.tokenSesion = puertoLogin.login(usuario, contrasenia).token;
        return this.tokenSesion;
    }

    public double convertirLongitud(double valor, String unidadInicial, String unidadFinal) {
        return puerto.convertirLongitud(new ContratoWSConversor.Solicitud(valor, unidadInicial, unidadFinal)).resultado;
    }

    public double convertirMasa(double valor, String unidadInicial, String unidadFinal) {
        return puerto.convertirMasa(new ContratoWSConversor.Solicitud(valor, unidadInicial, unidadFinal)).resultado;
    }

    public double convertirTemperatura(double valor, String unidadInicial, String unidadFinal) {
        return puerto.convertirTemperatura(new ContratoWSConversor.Solicitud(valor, unidadInicial, unidadFinal)).resultado;
    }

    public String getTokenSesion() {
        return tokenSesion;
    }

    public String getEndpointActivo() {
        return endpointActivo;
    }

    public void cerrarSesion() {
        this.tokenSesion = null;
    }
}

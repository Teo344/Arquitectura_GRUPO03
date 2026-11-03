package ec.edu.monster.servicio;

import ec.edu.monster.modelo.CuentaModelo;
import ec.edu.monster.modelo.ErrorServicioModelo;
import ec.edu.monster.modelo.MovimientoModelo;
import ec.edu.monster.modelo.ResultadoTransaccionModelo;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.servicio.ContratoWSEureka.Respuesta;
import jakarta.xml.soap.Detail;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.soap.SOAPFaultException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Única capa que conoce SOAP: crea los puertos de AutenticacionWS, CuentaWS y
 * TransaccionWS, guarda la sesión activa y convierte las respuestas y los SOAP
 * Fault en objetos del modelo. Es la misma clase del cliente de escritorio; en
 * la web hay una instancia por sesión del navegador (HttpSession).
 */
public class ClienteWSEureka {

    private static final String ARCHIVO_CONFIGURACION = "/servidor.properties";
    private static final String PROPIEDAD_URL = "servidor.url";
    private static final String ESTADO_OK = "OK";
    private static final String SESION_INVALIDA = "AUTH-003";

    private static final int TIEMPO_CONEXION_MS = 8000;
    private static final int TIEMPO_RESPUESTA_MS = 20000;

    private final String baseUrl;
    private final ContratoWSEureka.Autenticacion puertoAutenticacion;
    private final ContratoWSEureka.Cuenta puertoCuenta;
    private final ContratoWSEureka.Transaccion puertoTransaccion;
    private SesionModelo sesion;

    public ClienteWSEureka() {
        this.baseUrl = obtenerBaseConfigurada();
        this.puertoAutenticacion = crearPuerto("AutenticacionWS", ContratoWSEureka.Autenticacion.class);
        this.puertoCuenta = crearPuerto("CuentaWS", ContratoWSEureka.Cuenta.class);
        this.puertoTransaccion = crearPuerto("TransaccionWS", ContratoWSEureka.Transaccion.class);
    }

    // -Dservidor.url tiene prioridad sobre servidor.properties (RNF-010)
    private static String obtenerBaseConfigurada() {
        String base = System.getProperty(PROPIEDAD_URL);
        if (base == null || base.isBlank()) {
            Properties propiedades = new Properties();
            try (InputStream entrada = ClienteWSEureka.class.getResourceAsStream(ARCHIVO_CONFIGURACION)) {
                if (entrada == null) {
                    throw new IllegalStateException("No se encontró el archivo " + ARCHIVO_CONFIGURACION + ".");
                }
                propiedades.load(entrada);
            } catch (IOException ex) {
                throw new IllegalStateException("No se pudo leer " + ARCHIVO_CONFIGURACION + ".", ex);
            }
            base = propiedades.getProperty(PROPIEDAD_URL);
        }
        if (base == null || base.isBlank()) {
            throw new IllegalStateException("La URL del servidor (" + PROPIEDAD_URL + ") es obligatoria.");
        }
        String normalizada = base.trim().replaceAll("/+$", "");
        String esquema = URI.create(normalizada).getScheme();
        if (!"http".equalsIgnoreCase(esquema) && !"https".equalsIgnoreCase(esquema)) {
            throw new IllegalStateException("La URL del servidor debe usar HTTP o HTTPS.");
        }
        return normalizada;
    }

    private <T> T crearPuerto(String servicio, Class<T> contrato) {
        String endpoint = baseUrl + "/" + servicio;
        try {
            Service servicioSoap = Service.create(URI.create(endpoint + "?wsdl").toURL(),
                    new QName(ContratoWSEureka.ESPACIO_NOMBRES, servicio));
            T puerto = servicioSoap.getPort(new QName(ContratoWSEureka.ESPACIO_NOMBRES, servicio + "Port"), contrato);
            Map<String, Object> contexto = ((BindingProvider) puerto).getRequestContext();
            contexto.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
            contexto.put("com.sun.xml.ws.connect.timeout", TIEMPO_CONEXION_MS);
            contexto.put("com.sun.xml.ws.request.timeout", TIEMPO_RESPUESTA_MS);
            return puerto;
        } catch (IOException | WebServiceException ex) {
            throw new ErrorServicioModelo("No se pudo conectar con " + endpoint
                    + ". Verifique que el servidor esté desplegado en Payara.", ex);
        }
    }

    public String getEndpointActivo() {
        return baseUrl;
    }

    public SesionModelo getSesion() {
        return sesion;
    }

    // ---------------------------------------------------------------- Autenticación

    /**
     * equipo es el que el servidor guarda en logsession (RQF-001): en la web,
     * la dirección del navegador del empleado, no la del servidor de Payara.
     */
    public SesionModelo iniciarSesion(String usuario, String clave, String equipo) {
        sesion = null;
        Respuesta respuesta = invocar(() -> puertoAutenticacion.iniciarSesion(usuario, clave, equipo));
        if (respuesta.token == null || respuesta.token.isBlank()) {
            throw new ErrorServicioModelo(null, "El servidor no devolvió el token de sesión.");
        }
        try {
            sesion = new SesionModelo(respuesta.token, Instant.parse(respuesta.expiraEn), respuesta.codigoEmpleado,
                    respuesta.nombreEmpleado, respuesta.codigoSucursal, respuesta.sucursal);
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new ErrorServicioModelo("El servidor devolvió una expiración de sesión inválida.", ex);
        }
        return sesion;
    }

    /**
     * Invalida el token en el servidor (RQF-002). La sesión local se elimina
     * siempre; si el token ya había caducado (AUTH-003) no se considera error.
     */
    public void cerrarSesion() {
        if (sesion == null) {
            return;
        }
        String token = sesion.getToken();
        sesion = null;
        try {
            invocar(() -> puertoAutenticacion.cerrarSesion(token));
        } catch (ErrorServicioModelo ex) {
            if (!ex.requiereNuevoLogin()) {
                throw ex;
            }
        }
    }

    // ---------------------------------------------------------------- Consultas

    public CuentaModelo consultarCuenta(String cuenta) {
        String token = tokenActivo();
        ContratoWSEureka.DatosCuenta datos = invocar(() -> puertoCuenta.consultarCuenta(token, cuenta)).cuenta;
        if (datos == null) {
            throw new ErrorServicioModelo(null, "El servidor no devolvió los datos de la cuenta.");
        }
        return new CuentaModelo(datos.codigo, datos.titular, datos.moneda, datos.saldo, datos.estado,
                datos.sucursal, datos.fechaCreacion);
    }

    /** Una cuenta sin movimientos devuelve una lista vacía. */
    public List<MovimientoModelo> consultarMovimientos(String cuenta) {
        String token = tokenActivo();
        List<ContratoWSEureka.DatosMovimiento> datos
                = invocar(() -> puertoCuenta.consultarMovimientos(token, cuenta)).movimientos;
        if (datos == null) {
            return List.of();
        }
        List<MovimientoModelo> movimientos = new ArrayList<>(datos.size());
        for (ContratoWSEureka.DatosMovimiento movimiento : datos) {
            movimientos.add(new MovimientoModelo(movimiento.numero == null ? 0 : movimiento.numero,
                    movimiento.fecha, movimiento.tipo, movimiento.accion, movimiento.importe,
                    movimiento.cuentaReferencia));
        }
        return List.copyOf(movimientos);
    }

    // ---------------------------------------------------------------- Transacciones

    public ResultadoTransaccionModelo depositar(String cuenta, BigDecimal importe) {
        String token = tokenActivo();
        return aResultado(invocar(() -> puertoTransaccion.depositar(token, cuenta, importe)));
    }

    public ResultadoTransaccionModelo retirar(String cuenta, BigDecimal importe) {
        String token = tokenActivo();
        return aResultado(invocar(() -> puertoTransaccion.retirar(token, cuenta, importe)));
    }

    public ResultadoTransaccionModelo transferir(String cuentaOrigen, String cuentaDestino, BigDecimal importe) {
        String token = tokenActivo();
        return aResultado(invocar(() -> puertoTransaccion.transferir(token, cuentaOrigen, cuentaDestino, importe)));
    }

    private static ResultadoTransaccionModelo aResultado(Respuesta respuesta) {
        return new ResultadoTransaccionModelo(respuesta.mensaje, respuesta.nuevoSaldo, respuesta.numeroMovimiento,
                respuesta.numeroMovimientoDestino);
    }

    // ---------------------------------------------------------------- Soporte

    private String tokenActivo() {
        if (sesion == null) {
            throw new ErrorServicioModelo(SESION_INVALIDA, "Debe iniciar sesión.");
        }
        return sesion.getToken();
    }

    /**
     * Ejecuta la llamada SOAP y traduce sus errores: un SOAP Fault trae el
     * detalle ErrorServicio (codigo, mensaje); cualquier otra falla es de
     * comunicación con el servidor.
     */
    private Respuesta invocar(Supplier<Respuesta> llamada) {
        Respuesta respuesta;
        try {
            respuesta = llamada.get();
        } catch (SOAPFaultException ex) {
            ErrorServicioModelo error = leerFault(ex);
            if (error.requiereNuevoLogin()) {
                sesion = null;
            }
            throw error;
        } catch (WebServiceException ex) {
            throw new ErrorServicioModelo("No se pudo comunicar con el servidor " + baseUrl
                    + ". Verifique que Payara esté activo.", ex);
        }
        if (respuesta == null || !ESTADO_OK.equals(respuesta.estado)) {
            throw new ErrorServicioModelo(null, "El servidor devolvió una respuesta inesperada.");
        }
        return respuesta;
    }

    private static ErrorServicioModelo leerFault(SOAPFaultException ex) {
        String mensaje = ex.getFault().getFaultString();
        Detail detalle = ex.getFault().getDetail();
        if (detalle != null) {
            Iterator<?> entradas = detalle.getDetailEntries();
            while (entradas.hasNext()) {
                if (entradas.next() instanceof Element entrada && "ErrorServicio".equals(entrada.getLocalName())) {
                    String codigo = textoHijo(entrada, "codigo");
                    String texto = textoHijo(entrada, "mensaje");
                    return new ErrorServicioModelo(codigo, texto != null ? texto : mensaje);
                }
            }
        }
        return new ErrorServicioModelo(null, mensaje);
    }

    private static String textoHijo(Element padre, String nombre) {
        NodeList hijos = padre.getChildNodes();
        for (int i = 0; i < hijos.getLength(); i++) {
            Node hijo = hijos.item(i);
            String local = hijo.getLocalName() != null ? hijo.getLocalName() : hijo.getNodeName();
            if (hijo.getNodeType() == Node.ELEMENT_NODE && nombre.equals(local)) {
                return hijo.getTextContent();
            }
        }
        return null;
    }
}

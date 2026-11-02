package ec.edu.monster.servicio;

import ec.edu.monster.BuildConfig;
import ec.edu.monster.modelo.ErrorServicioModelo;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Envía sobres SOAP 1.1 al servidor WS_EUREKA_JAVA_SOAP y lee sus respuestas.
 * Android no incluye JAX-WS, por eso el sobre se arma y se interpreta aquí. Es
 * la única clase que conoce el protocolo; los servicios solo arman parámetros.
 */
public class ClienteSoap {

    public static final String ESPACIO_NOMBRES = "http://wservicios.controladores.monster.edu.ec/";

    // URL del servidor desplegado en Payara; se configura en gradle.properties (servidor.url)
    public static final String URL_BASE_POR_DEFECTO = BuildConfig.URL_SERVIDOR;

    private static final int TIEMPO_ESPERA_MS = 15000;
    private static final String ESTADO_OK = "OK";

    private final String urlBase;

    public ClienteSoap() {
        this(URL_BASE_POR_DEFECTO);
    }

    public ClienteSoap(String urlBase) {
        this.urlBase = urlBase.replaceAll("/+$", "");
    }

    /**
     * Invoca una operación y devuelve el elemento return de una respuesta
     * correcta (estado OK).
     *
     * @param servicio   AutenticacionWS, CuentaWS o TransaccionWS
     * @param operacion  nombre de la operación (iniciarSesion, depositar, ...)
     * @param token      token de sesión para la cabecera SOAP, o null si no aplica
     * @param parametros XML de los parámetros de la operación (ya escapado)
     */
    public Element invocar(String servicio, String operacion, String token, String parametros) {
        HttpURLConnection conexion = null;
        try {
            conexion = (HttpURLConnection) URI.create(urlBase + "/" + servicio).toURL().openConnection();
            conexion.setConnectTimeout(TIEMPO_ESPERA_MS);
            conexion.setReadTimeout(TIEMPO_ESPERA_MS);
            conexion.setInstanceFollowRedirects(false);
            conexion.setRequestMethod("POST");
            conexion.setDoOutput(true);
            conexion.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            conexion.setRequestProperty("SOAPAction", "\"\"");

            byte[] sobre = construirSobre(operacion, token, parametros).getBytes(StandardCharsets.UTF_8);
            try (OutputStream salida = conexion.getOutputStream()) {
                salida.write(sobre);
            }

            int codigoHttp = conexion.getResponseCode();
            InputStream entrada = codigoHttp >= 400 ? conexion.getErrorStream() : conexion.getInputStream();
            if (entrada == null) {
                throw new ErrorServicioModelo(null, "El servidor respondió con el código HTTP " + codigoHttp + ".");
            }

            Document documento;
            try (InputStream lectura = entrada) {
                documento = leerXml(lectura);
            }

            Element fault = primerElemento(documento.getDocumentElement(), "Fault");
            if (fault != null) {
                throw desdeFault(fault);
            }

            Element retorno = primerElemento(documento.getDocumentElement(), "return");
            if (retorno == null || !ESTADO_OK.equals(texto(retorno, "estado"))) {
                throw new ErrorServicioModelo(null, "El servidor devolvió una respuesta inesperada.");
            }
            return retorno;
        } catch (ErrorServicioModelo ex) {
            throw ex;
        } catch (IOException ex) {
            throw new ErrorServicioModelo("No se pudo conectar con el servidor. Verifica que Payara esté iniciado.", ex);
        } catch (Exception ex) {
            throw new ErrorServicioModelo("No se pudo leer la respuesta del servidor.", ex);
        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }

    private static String construirSobre(String operacion, String token, String parametros) {
        StringBuilder sobre = new StringBuilder()
                .append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"")
                .append(" xmlns:ws=\"").append(ESPACIO_NOMBRES).append("\">");
        if (token != null) {
            sobre.append("<soapenv:Header><ws:token>").append(escapar(token)).append("</ws:token></soapenv:Header>");
        }
        return sobre.append("<soapenv:Body><ws:").append(operacion).append('>')
                .append(parametros == null ? "" : parametros)
                .append("</ws:").append(operacion).append("></soapenv:Body></soapenv:Envelope>")
                .toString();
    }

    // Sin DTD ni entidades externas: evita ataques XXE con respuestas manipuladas
    private static Document leerXml(InputStream entrada) throws Exception {
        DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
        fabrica.setNamespaceAware(true);
        fabrica.setExpandEntityReferences(false);
        try {
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        } catch (Exception ignorada) {
            // El parser de Android no soporta esta opción; no admite DTD externas de todos modos
        }
        DocumentBuilder lector = fabrica.newDocumentBuilder();
        return lector.parse(entrada);
    }

    /** El servidor envía el detalle del error como ErrorServicio(codigo, mensaje). */
    private static ErrorServicioModelo desdeFault(Element fault) {
        Element detalle = primerElemento(fault, "ErrorServicio");
        String codigo = detalle == null ? null : texto(detalle, "codigo");
        String mensaje = detalle == null ? null : texto(detalle, "mensaje");
        if (mensaje == null || mensaje.trim().isEmpty()) {
            mensaje = texto(fault, "faultstring");
        }
        return new ErrorServicioModelo(codigo, mensaje == null ? "El servidor rechazó la solicitud." : mensaje);
    }

    /** Escapa los caracteres especiales para incluir texto del usuario dentro del XML. */
    public static String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    /** Elemento XML simple: &lt;nombre&gt;valor escapado&lt;/nombre&gt;. */
    public static String elemento(String nombre, String valor) {
        return "<" + nombre + ">" + escapar(valor) + "</" + nombre + ">";
    }

    /** Texto del hijo directo con ese nombre, o null si no existe. */
    public static String texto(Element padre, String nombre) {
        Element hijo = hijo(padre, nombre);
        return hijo == null ? null : hijo.getTextContent();
    }

    public static BigDecimal decimal(Element padre, String nombre) {
        String valor = texto(padre, nombre);
        try {
            return valor == null || valor.trim().isEmpty() ? null : new BigDecimal(valor.trim());
        } catch (NumberFormatException ex) {
            throw new ErrorServicioModelo("El servidor devolvió un valor no numérico en " + nombre + ".", ex);
        }
    }

    public static Integer entero(Element padre, String nombre) {
        String valor = texto(padre, nombre);
        try {
            return valor == null || valor.trim().isEmpty() ? null : Integer.valueOf(valor.trim());
        } catch (NumberFormatException ex) {
            throw new ErrorServicioModelo("El servidor devolvió un valor no numérico en " + nombre + ".", ex);
        }
    }

    /** Hijo directo con ese nombre local (sin importar el namespace). */
    public static Element hijo(Element padre, String nombre) {
        List<Element> encontrados = hijos(padre, nombre);
        return encontrados.isEmpty() ? null : encontrados.get(0);
    }

    /** Hijos directos con ese nombre local (sin importar el namespace). */
    public static List<Element> hijos(Element padre, String nombre) {
        List<Element> encontrados = new ArrayList<>();
        if (padre == null) {
            return encontrados;
        }
        NodeList nodos = padre.getChildNodes();
        for (int i = 0; i < nodos.getLength(); i++) {
            Node nodo = nodos.item(i);
            if (nodo.getNodeType() == Node.ELEMENT_NODE && nombre.equals(nombreLocal(nodo))) {
                encontrados.add((Element) nodo);
            }
        }
        return encontrados;
    }

    private static Element primerElemento(Element raiz, String nombre) {
        NodeList nodos = raiz.getElementsByTagNameNS("*", nombre);
        return nodos.getLength() == 0 ? null : (Element) nodos.item(0);
    }

    private static String nombreLocal(Node nodo) {
        return nodo.getLocalName() != null ? nodo.getLocalName() : nodo.getNodeName();
    }
}

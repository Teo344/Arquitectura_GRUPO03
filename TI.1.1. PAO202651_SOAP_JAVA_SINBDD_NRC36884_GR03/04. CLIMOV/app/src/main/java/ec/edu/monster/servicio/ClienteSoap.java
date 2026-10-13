package ec.edu.monster.servicio;

import ec.edu.monster.BuildConfig;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
 * Envía sobres SOAP 1.1 al servidor WS_CON_UNI_JAVA_SOAP y lee sus respuestas.
 * Android no incluye JAX-WS, por eso el sobre se arma y se interpreta aquí.
 */
public class ClienteSoap {

    public static final String ESPACIO_NOMBRES = "http://wservicios.controladores.monster.edu.ec/";

    // URL del servidor desplegado en Payara; se configura en gradle.properties (servidor.url)
    public static final String URL_BASE_POR_DEFECTO = BuildConfig.URL_SERVIDOR;

    private static final int TIEMPO_ESPERA_MS = 15000;

    private final String urlBase;

    public ClienteSoap() {
        this(URL_BASE_POR_DEFECTO);
    }

    public ClienteSoap(String urlBase) {
        this.urlBase = urlBase.replaceAll("/+$", "");
    }

    /**
     * Invoca una operación y devuelve el elemento return de la respuesta.
     *
     * @param servicio  AutenticacionWS o ConversionUnidadesWS
     * @param operacion nombre de la operación (login, consultarCatalogo, ...)
     * @param token     token de sesión para la cabecera SOAP, o null si no aplica
     * @param parametros XML de los parámetros de la operación (ya escapado)
     */
    public Element invocar(String servicio, String operacion, String token, String parametros)
            throws ExcepcionServicio {
        HttpURLConnection conexion = null;
        try {
            conexion = (HttpURLConnection) URI.create(urlBase + "/" + servicio).toURL().openConnection();
            conexion.setConnectTimeout(TIEMPO_ESPERA_MS);
            conexion.setReadTimeout(TIEMPO_ESPERA_MS);
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
                throw new ExcepcionServicio("HTTP-" + codigoHttp,
                        "El servidor respondió con el código HTTP " + codigoHttp + ".");
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
            if (retorno == null) {
                throw new ExcepcionServicio("RESPUESTA", "El servidor devolvió una respuesta vacía.");
            }
            return retorno;
        } catch (ExcepcionServicio ex) {
            throw ex;
        } catch (IOException ex) {
            throw new ExcepcionServicio(ExcepcionServicio.SIN_CONEXION,
                    "No se pudo conectar con el servidor. Verifica que esté iniciado.");
        } catch (Exception ex) {
            throw new ExcepcionServicio("RESPUESTA", "No se pudo leer la respuesta del servidor.");
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
    private static ExcepcionServicio desdeFault(Element fault) {
        Element detalle = primerElemento(fault, "ErrorServicio");
        String codigo = detalle == null ? null : texto(detalle, "codigo");
        String mensaje = detalle == null ? null : texto(detalle, "mensaje");
        if (mensaje == null || mensaje.trim().isEmpty()) {
            mensaje = texto(fault, "faultstring");
        }
        return new ExcepcionServicio(codigo == null ? "SOAP" : codigo,
                mensaje == null ? "El servidor rechazó la solicitud." : mensaje);
    }

    /** Toda respuesta exitosa del servidor trae estado OK (respuesta estructurada). */
    public static void validar(Element respuesta) throws ExcepcionServicio {
        if (!"OK".equalsIgnoreCase(texto(respuesta, "estado"))) {
            String mensaje = texto(respuesta, "mensaje");
            throw new ExcepcionServicio(texto(respuesta, "codigo"),
                    mensaje == null ? "El servidor no pudo completar la operación." : mensaje);
        }
    }

    /** Escapa los caracteres especiales para incluir texto dentro del XML. */
    public static String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    /** Texto del hijo directo con ese nombre, o null si no existe. */
    public static String texto(Element padre, String nombre) {
        Element hijo = hijo(padre, nombre);
        return hijo == null ? null : hijo.getTextContent();
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

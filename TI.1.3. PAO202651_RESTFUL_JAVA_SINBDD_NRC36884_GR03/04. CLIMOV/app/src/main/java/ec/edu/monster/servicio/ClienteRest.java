package ec.edu.monster.servicio;

import ec.edu.monster.BuildConfig;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Envía peticiones HTTP con JSON al servidor WS_CON_UNI_JAVA_REST y lee sus respuestas.
 * El token de sesión viaja en la cabecera "Authorization: Bearer &lt;token&gt;".
 */
public class ClienteRest {

    // URL base del servidor desplegado en Payara; se configura en gradle.properties (servidor.url)
    public static final String URL_BASE_POR_DEFECTO = BuildConfig.URL_SERVIDOR;

    private static final int TIEMPO_ESPERA_MS = 15000;

    private final String urlBase;

    public ClienteRest() {
        this(URL_BASE_POR_DEFECTO);
    }

    public ClienteRest(String urlBase) {
        this.urlBase = urlBase.replaceAll("/+$", "");
    }

    public JSONObject get(String ruta, String token) throws ExcepcionServicio {
        return invocar("GET", ruta, token, null);
    }

    public JSONObject post(String ruta, String token, JSONObject cuerpo) throws ExcepcionServicio {
        return invocar("POST", ruta, token, cuerpo);
    }

    private JSONObject invocar(String metodo, String ruta, String token, JSONObject cuerpo)
            throws ExcepcionServicio {
        HttpURLConnection conexion = null;
        try {
            conexion = (HttpURLConnection) URI.create(urlBase + ruta).toURL().openConnection();
            conexion.setConnectTimeout(TIEMPO_ESPERA_MS);
            conexion.setReadTimeout(TIEMPO_ESPERA_MS);
            conexion.setRequestMethod(metodo);
            conexion.setRequestProperty("Accept", "application/json");
            if (token != null) {
                conexion.setRequestProperty("Authorization", "Bearer " + token);
            }
            if (cuerpo != null) {
                conexion.setDoOutput(true);
                conexion.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                try (OutputStream salida = conexion.getOutputStream()) {
                    salida.write(cuerpo.toString().getBytes(StandardCharsets.UTF_8));
                }
            }

            int codigoHttp = conexion.getResponseCode();
            InputStream entrada = codigoHttp >= 400 ? conexion.getErrorStream() : conexion.getInputStream();
            if (entrada == null) {
                throw errorHttp(codigoHttp);
            }

            JSONObject respuesta;
            try (InputStream lectura = entrada) {
                respuesta = new JSONObject(leerTexto(lectura));
            } catch (JSONException ex) {
                // Una página de error que no es JSON (ruta o contexto inexistente)
                throw codigoHttp >= 400 ? errorHttp(codigoHttp)
                        : new ExcepcionServicio("RESPUESTA", "No se pudo leer la respuesta del servidor.");
            }
            validar(respuesta);
            return respuesta;
        } catch (ExcepcionServicio ex) {
            throw ex;
        } catch (IOException ex) {
            throw new ExcepcionServicio(ExcepcionServicio.SIN_CONEXION,
                    "No se pudo conectar con el servidor. Verifica que esté iniciado.");
        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }

    /** Toda respuesta del servidor trae estado: OK, o ERROR con su codigo y mensaje. */
    private static void validar(JSONObject respuesta) throws ExcepcionServicio {
        if (!"OK".equalsIgnoreCase(texto(respuesta, "estado"))) {
            String codigo = texto(respuesta, "codigo");
            String mensaje = texto(respuesta, "mensaje");
            throw new ExcepcionServicio(codigo == null ? "RESPUESTA" : codigo,
                    mensaje == null ? "El servidor no pudo completar la operación." : mensaje);
        }
    }

    private static ExcepcionServicio errorHttp(int codigoHttp) {
        return new ExcepcionServicio("HTTP-" + codigoHttp,
                "El servidor respondió con el código HTTP " + codigoHttp + ".");
    }

    private static String leerTexto(InputStream entrada) throws IOException {
        ByteArrayOutputStream contenido = new ByteArrayOutputStream();
        byte[] bloque = new byte[4096];
        int leidos;
        while ((leidos = entrada.read(bloque)) != -1) {
            contenido.write(bloque, 0, leidos);
        }
        return new String(contenido.toByteArray(), StandardCharsets.UTF_8);
    }

    /** Texto de un campo, o null si no existe o es null en el JSON. */
    public static String texto(JSONObject objeto, String campo) {
        return objeto == null || objeto.isNull(campo) ? null : objeto.optString(campo);
    }

    /** Arma un objeto JSON a partir de pares nombre, valor. */
    public static JSONObject crearJson(Object... nombreValor) throws ExcepcionServicio {
        try {
            JSONObject objeto = new JSONObject();
            for (int i = 0; i < nombreValor.length; i += 2) {
                objeto.put((String) nombreValor[i], nombreValor[i + 1]);
            }
            return objeto;
        } catch (JSONException ex) {
            throw new ExcepcionServicio("DATA-001", "Datos inconsistentes");
        }
    }

    /** Codifica un valor para usarlo dentro de la ruta (por ejemplo, la categoría). */
    public static String codificar(String valor) {
        try {
            return URLEncoder.encode(valor == null ? "" : valor, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException ex) {
            return valor;
        }
    }
}

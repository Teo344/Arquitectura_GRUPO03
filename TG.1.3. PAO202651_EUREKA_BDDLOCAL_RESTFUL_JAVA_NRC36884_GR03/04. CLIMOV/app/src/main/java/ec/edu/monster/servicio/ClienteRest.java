package ec.edu.monster.servicio;

import ec.edu.monster.BuildConfig;
import ec.edu.monster.modelo.ErrorServicioModelo;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Envía peticiones HTTP con JSON a la API REST WS_EUREKA_JAVA_REST (/api) y lee
 * sus respuestas. Es la única clase que conoce HTTP y JSON; el token de sesión
 * viaja solo en la cabecera "Authorization: Bearer &lt;token&gt;".
 */
public class ClienteRest {

    // URL base de la API desplegada en Payara; se configura en gradle.properties (servidor.url)
    public static final String URL_BASE_POR_DEFECTO = BuildConfig.URL_SERVIDOR;

    private static final int TIEMPO_ESPERA_MS = 15000;
    private static final String ESTADO_OK = "OK";

    private final String urlBase;

    public ClienteRest() {
        this(URL_BASE_POR_DEFECTO);
    }

    public ClienteRest(String urlBase) {
        this.urlBase = urlBase.replaceAll("/+$", "");
    }

    public JSONObject get(String ruta, String token) {
        return invocar("GET", ruta, token, null);
    }

    /** Un POST sin datos (cerrar-sesion) envía un objeto JSON vacío. */
    public JSONObject post(String ruta, String token, JSONObject cuerpo) {
        return invocar("POST", ruta, token, cuerpo == null ? new JSONObject() : cuerpo);
    }

    private JSONObject invocar(String metodo, String ruta, String token, JSONObject cuerpo) {
        HttpURLConnection conexion = null;
        try {
            conexion = (HttpURLConnection) URI.create(urlBase + ruta).toURL().openConnection();
            conexion.setConnectTimeout(TIEMPO_ESPERA_MS);
            conexion.setReadTimeout(TIEMPO_ESPERA_MS);
            // Sin redirecciones: el token nunca se reenvía a otro servidor
            conexion.setInstanceFollowRedirects(false);
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
                        : new ErrorServicioModelo(null, "No se pudo leer la respuesta del servidor.");
            }

            if (codigoHttp < 200 || codigoHttp >= 300) {
                // Error estructurado del servidor: {estado: "ERROR", codigo, mensaje}
                String mensaje = texto(respuesta, "mensaje");
                throw new ErrorServicioModelo(texto(respuesta, "codigo"),
                        mensaje == null ? "El servidor respondió con el código HTTP " + codigoHttp + "." : mensaje);
            }
            if (!ESTADO_OK.equals(texto(respuesta, "estado"))) {
                throw new ErrorServicioModelo(null, "El servidor devolvió una respuesta inesperada.");
            }
            return respuesta;
        } catch (ErrorServicioModelo ex) {
            throw ex;
        } catch (IOException ex) {
            throw new ErrorServicioModelo("No se pudo conectar con el servidor. Verifica que Payara esté iniciado.", ex);
        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }

    private static ErrorServicioModelo errorHttp(int codigoHttp) {
        return new ErrorServicioModelo(null, "El servidor respondió con el código HTTP " + codigoHttp + ".");
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

    /** Importe o saldo como BigDecimal, o null si no viene. */
    public static BigDecimal decimal(JSONObject objeto, String campo) {
        String valor = texto(objeto, campo);
        try {
            return valor == null || valor.trim().isEmpty() ? null : new BigDecimal(valor.trim());
        } catch (NumberFormatException ex) {
            throw new ErrorServicioModelo("El servidor devolvió un valor no numérico en " + campo + ".", ex);
        }
    }

    public static Integer entero(JSONObject objeto, String campo) {
        BigDecimal valor = decimal(objeto, campo);
        return valor == null ? null : valor.intValue();
    }

    /** Arma un objeto JSON a partir de pares nombre, valor (nunca concatenando texto). */
    public static JSONObject crearJson(Object... nombreValor) {
        try {
            JSONObject objeto = new JSONObject();
            for (int i = 0; i < nombreValor.length; i += 2) {
                objeto.put((String) nombreValor[i], nombreValor[i + 1]);
            }
            return objeto;
        } catch (JSONException ex) {
            throw new ErrorServicioModelo("No se pudieron preparar los datos de la solicitud.", ex);
        }
    }

    /** Codifica un valor para usarlo dentro de la ruta (por ejemplo, la cuenta). */
    public static String codificar(String valor) {
        try {
            return URLEncoder.encode(valor == null ? "" : valor, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException ex) {
            return valor;
        }
    }
}

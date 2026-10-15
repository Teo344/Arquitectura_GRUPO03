package ec.edu.monster.vista;

import ec.edu.monster.modelo.Categoria;
import ec.edu.monster.modelo.Unidad;
import ec.edu.monster.seguridad.GestorToken.TokenEmitido;
import ec.edu.monster.servicios.ServicioConversion.ResultadoConversion;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Convierte objetos del modelo en {@link RespuestaDto} (RQF-009).
 *
 * @author carlo
 */
@ApplicationScoped
public class RespuestaMapeador {

    public static final String ESTADO_OK = "OK";
    public static final String CODIGO_OK = "200";
    public static final String MENSAJE_LOGIN = "Autenticación exitosa";
    public static final String MENSAJE_CAMBIO_CLAVE = "Clave actualizada; inicie sesión nuevamente";
    public static final String MENSAJE_CATALOGO = "Catálogo consultado";
    public static final String MENSAJE_UNIDADES = "Unidades consultadas";
    public static final String MENSAJE_CONVERSION = "Conversión realizada";

    public RespuestaDto deToken(TokenEmitido token) {
        RespuestaDto respuesta = exito(MENSAJE_LOGIN);
        respuesta.token = token.token();
        respuesta.expiraEn = token.expiraEn().toString();
        return respuesta;
    }

    public RespuestaDto deCambioClave() {
        return exito(MENSAJE_CAMBIO_CLAVE);
    }

    public RespuestaDto deCatalogo(List<Categoria> categorias) {
        RespuestaDto respuesta = exito(MENSAJE_CATALOGO);
        respuesta.categorias = categorias.stream()
                .map(c -> new CategoriaDto(c.name(), c.getNombre(), c.isConvertible()))
                .toList();
        return respuesta;
    }

    public RespuestaDto deUnidades(List<Unidad> unidades) {
        RespuestaDto respuesta = exito(MENSAJE_UNIDADES);
        respuesta.unidades = unidades.stream()
                .map(u -> new UnidadDto(u.getCodigo(), u.getNombre(), u.getSimbolo(), u.getCategoria().name()))
                .toList();
        return respuesta;
    }

    public RespuestaDto deConversion(ResultadoConversion resultado) {
        RespuestaDto respuesta = exito(MENSAJE_CONVERSION);
        respuesta.resultado = resultado.resultado();
        respuesta.unidadOrigen = resultado.origen().getCodigo();
        respuesta.unidadDestino = resultado.destino().getCodigo();
        return respuesta;
    }

    private RespuestaDto exito(String mensaje) {
        RespuestaDto respuesta = new RespuestaDto();
        respuesta.estado = ESTADO_OK;
        respuesta.codigo = CODIGO_OK;
        respuesta.mensaje = mensaje;
        return respuesta;
    }
}

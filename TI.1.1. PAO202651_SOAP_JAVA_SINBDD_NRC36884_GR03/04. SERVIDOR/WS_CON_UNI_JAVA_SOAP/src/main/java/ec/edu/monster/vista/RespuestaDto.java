package ec.edu.monster.vista;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * Respuesta exitosa de todas las operaciones (RQF-009). Los campos que no
 * aplican a la operación quedan nulos y no se serializan.
 *
 * @author carlo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "respuesta", propOrder = {"estado", "codigo", "mensaje", "resultado", "unidadOrigen",
    "unidadDestino", "token", "expiraEn", "categorias", "unidades"})
public class RespuestaDto {

    public String estado;
    public String codigo;
    public String mensaje;
    public Double resultado;
    public String unidadOrigen;
    public String unidadDestino;
    public String token;
    public String expiraEn;

    @XmlElementWrapper(name = "categorias")
    @XmlElement(name = "categoria")
    public List<CategoriaDto> categorias;

    @XmlElementWrapper(name = "unidades")
    @XmlElement(name = "unidad")
    public List<UnidadDto> unidades;
}

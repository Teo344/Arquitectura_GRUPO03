package ec.edu.monster.vista;

import jakarta.json.bind.annotation.JsonbPropertyOrder;

/**
 * Cuerpo de toda respuesta de error, con el formato Problem Details del RFC
 * 9457 (application/problem+json). codigo es una extensión: el código de
 * error del sistema (CRE-001, DATA-001…) que leen los clientes.
 *
 * @author carlo
 */
@JsonbPropertyOrder({"type", "title", "status", "detail", "instance", "codigo"})
public class ProblemaDto {

    public static final String TIPO_PROBLEMA = "application/problem+json";

    public String type = "about:blank";
    public String title;
    public int status;
    public String detail;
    public String instance;
    public String codigo;

    public ProblemaDto() {
    }

    public ProblemaDto(String title, int status, String detail, String instance, String codigo) {
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
        this.codigo = codigo;
    }
}

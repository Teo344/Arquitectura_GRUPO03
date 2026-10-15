package ec.edu.monster.seguridad;

import jakarta.ws.rs.NameBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca los recursos que requieren token. Es el equivalente REST de
 * {@code @HandlerChain}: ManejadorAutenticacion solo se ejecuta en lo que
 * lleva esta anotación.
 *
 * @author carlo
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Protegido {
}

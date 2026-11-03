<%-- Componentes.tarjetaResultado: encabezado de éxito con icono y las filas del comprobante (la primera destacada). --%>
<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="mensaje" required="true" %>
<%@ attribute name="filas" required="true" type="java.util.List" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<section class="tarjeta" aria-live="polite">
    <div class="encabezado">
        <span class="icono-caja icono-caja-44 icono-caja-exito"><t:icono nombre="EXITO" tamano="22"/></span>
        <div>
            <p class="sobretitulo texto-exito">Operación registrada</p>
            <h2 class="titulo"><c:out value="${mensaje}"/></h2>
        </div>
    </div>
    <hr class="divisor">
    <dl class="ficha">
        <c:forEach items="${filas}" var="fila" varStatus="estado">
            <t:filaDato etiqueta="${fila[0]}" valor="${fila[1]}" destacado="${estado.first}"/>
        </c:forEach>
    </dl>
</section>

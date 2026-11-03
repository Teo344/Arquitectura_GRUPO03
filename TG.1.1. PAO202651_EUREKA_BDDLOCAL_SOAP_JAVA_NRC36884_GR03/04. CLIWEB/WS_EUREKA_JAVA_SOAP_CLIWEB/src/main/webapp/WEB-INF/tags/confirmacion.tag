<%--
    Componentes.confirmar: resumen antes de registrar una operación, con Cancelar y Registrar.
    Se muestra abierto; eureka.js lo convierte en modal con el velo azul oscuro y Esc cancela.
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="titulo" required="true" %>
<%@ attribute name="filas" required="true" type="java.util.List" %>
<%@ attribute name="accion" required="true" %>
<%@ attribute name="cancelar" required="true" %>
<%@ attribute name="textoConfirmar" required="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<dialog class="dialogo" open data-modal data-cancelar="${cancelar}" aria-labelledby="dialogo-titulo">
    <form method="post" action="${accion}">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
        <h2 class="titulo" id="dialogo-titulo"><c:out value="${titulo}"/></h2>
        <p class="cuerpo secundario">Revisa los datos antes de registrar la operación.</p>
        <dl class="ficha">
            <c:forEach items="${filas}" var="fila">
                <t:filaDato etiqueta="${fila[0]}" valor="${fila[1]}" destacado="${false}"/>
            </c:forEach>
        </dl>
        <div class="dialogo-acciones">
            <a class="boton boton-texto" href="${cancelar}">Cancelar</a>
            <button type="submit" class="boton boton-primario" data-cargando autofocus>
                <span class="boton-texto-contenido"><c:out value="${textoConfirmar}"/></span>
            </button>
        </div>
    </form>
</dialog>

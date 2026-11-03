<%--
    Confirmación antes de una acción importante (facturar, dar de baja): resumen y dos botones.
    Se muestra abierta; monstercell.js la convierte en modal con el velo azul oscuro y Esc cancela.
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="titulo" required="true" %>
<%@ attribute name="filas" required="true" type="java.util.List" %>
<%@ attribute name="accion" required="true" %>
<%@ attribute name="cancelar" required="true" %>
<%@ attribute name="textoConfirmar" required="true" %>
<%@ attribute name="texto" %>
<%@ attribute name="campoOculto" %>
<%@ attribute name="valorOculto" %>
<%@ attribute name="peligro" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<dialog class="dialogo" open data-modal data-cancelar="${cancelar}" aria-labelledby="dialogo-titulo">
    <form method="post" action="${accion}">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
        <c:if test="${not empty campoOculto}"><input type="hidden" name="${campoOculto}" value="<c:out value='${valorOculto}'/>"></c:if>
        <h2 class="titulo" id="dialogo-titulo"><c:out value="${titulo}"/></h2>
        <p class="cuerpo secundario"><c:out value="${empty texto ? 'Revisa los datos antes de continuar.' : texto}"/></p>
        <dl class="ficha">
            <c:forEach items="${filas}" var="fila" varStatus="estado">
                <t:filaDato etiqueta="${fila[0]}" valor="${fila[1]}" destacado="${estado.last and not peligro}"/>
            </c:forEach>
        </dl>
        <div class="dialogo-acciones">
            <a class="boton boton-texto" href="${cancelar}">Cancelar</a>
            <button type="submit" class="boton ${peligro ? 'boton-peligro' : 'boton-primario'}" data-cargando autofocus>
                <span class="boton-texto-contenido"><c:out value="${textoConfirmar}"/></span>
            </button>
        </div>
    </form>
</dialog>

<%-- Componentes.estadoVacio: mensaje centrado para cuando todavía no hay datos que mostrar. --%>
<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="icono" required="true" %>
<%@ attribute name="titulo" required="true" %>
<%@ attribute name="texto" required="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<div class="estado-vacio">
    <span class="icono-caja icono-caja-56 icono-caja-azul"><t:icono nombre="${icono}" tamano="28"/></span>
    <h3 class="titulo"><c:out value="${titulo}"/></h3>
    <p class="cuerpo secundario"><c:out value="${texto}"/></p>
</div>

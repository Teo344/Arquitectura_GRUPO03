<%-- Componentes.TarjetaAccion: icono, título y descripción; toda la tarjeta es el enlace. --%>
<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="href" required="true" %>
<%@ attribute name="titulo" required="true" %>
<%@ attribute name="descripcion" required="true" %>
<%@ attribute name="icono" required="true" %>
<%@ attribute name="acento" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<a class="tarjeta tarjeta-accion" href="${href}">
    <span class="icono-caja icono-caja-48 ${acento ? 'icono-caja-cian' : 'icono-caja-azul'}"><t:icono nombre="${icono}" tamano="24"/></span>
    <span class="tarjeta-accion-textos">
        <span class="titulo"><c:out value="${titulo}"/></span>
        <span class="cuerpo secundario"><c:out value="${descripcion}"/></span>
    </span>
</a>

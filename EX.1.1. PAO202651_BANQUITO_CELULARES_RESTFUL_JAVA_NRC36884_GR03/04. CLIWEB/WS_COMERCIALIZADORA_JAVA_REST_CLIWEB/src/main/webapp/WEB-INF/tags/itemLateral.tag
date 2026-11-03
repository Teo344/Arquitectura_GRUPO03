<%-- Enlace de la barra lateral: icono y texto; la sección actual lleva aria-current y el acento cian. --%>
<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="href" required="true" %>
<%@ attribute name="texto" required="true" %>
<%@ attribute name="icono" required="true" %>
<%@ attribute name="activo" type="java.lang.Boolean" %>
<%@ attribute name="contador" type="java.lang.Integer" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<li>
    <a class="lateral-enlace${activo ? ' activo' : ''}" href="${href}"${activo ? ' aria-current="page"' : ''}>
        <t:icono nombre="${icono}" tamano="20"/>
        <span class="crece"><c:out value="${texto}"/></span>
        <c:if test="${contador > 0}"><span class="lateral-contador" aria-label="${contador} en la venta">${contador}</span></c:if>
    </a>
</li>

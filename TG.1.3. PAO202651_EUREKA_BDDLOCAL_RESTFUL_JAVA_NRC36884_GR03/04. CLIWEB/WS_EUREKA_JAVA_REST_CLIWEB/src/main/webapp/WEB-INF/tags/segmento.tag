<%-- Opción de Componentes.SelectorSegmentado: la elegida se marca con fondo blanco, borde y negrita, no solo con color. --%>
<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="href" required="true" %>
<%@ attribute name="texto" required="true" %>
<%@ attribute name="icono" %>
<%@ attribute name="elegido" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<a class="segmento${elegido ? ' elegido' : ''}" href="${href}" ${elegido ? 'aria-current="page"' : ''}>
    <c:if test="${not empty icono}"><t:icono nombre="${icono}" tamano="18"/></c:if>
    <span><c:out value="${texto}"/></span>
</a>

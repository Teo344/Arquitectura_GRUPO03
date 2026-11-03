<%-- Componentes.chip: etiqueta pequeña con radio 6. variante: azul, cian, exito o advertencia. --%>
<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="texto" required="true" %>
<%@ attribute name="variante" %>
<%@ attribute name="icono" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<span class="chip chip-${empty variante ? 'azul' : variante}">
    <c:if test="${not empty icono}"><t:icono nombre="${icono}" tamano="14"/></c:if>
    <c:out value="${texto}"/>
</span>

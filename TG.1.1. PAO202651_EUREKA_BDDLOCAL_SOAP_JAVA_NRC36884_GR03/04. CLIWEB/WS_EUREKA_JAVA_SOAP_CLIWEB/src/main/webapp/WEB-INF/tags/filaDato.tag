<%-- Componentes.filaDato: fila etiqueta / valor de una ficha; la destacada usa el título H4. --%>
<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="etiqueta" required="true" %>
<%@ attribute name="valor" %>
<%@ attribute name="destacado" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<div class="fila-dato">
    <dt class="cuerpo secundario"><c:out value="${etiqueta}"/></dt>
    <dd class="${destacado ? 'titulo' : 'cuerpo-grande-seminegrita'}"><c:out value="${empty valor ? '-' : valor}"/></dd>
</div>

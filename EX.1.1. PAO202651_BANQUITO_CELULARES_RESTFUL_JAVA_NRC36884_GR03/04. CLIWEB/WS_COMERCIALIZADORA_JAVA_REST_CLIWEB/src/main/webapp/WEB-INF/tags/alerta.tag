<%-- Componentes.Alerta: icono y texto, el estado nunca se comunica solo con color. No se muestra si no hay mensaje. --%>
<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="texto" %>
<%@ attribute name="error" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:if test="${not empty texto}">
    <div class="alerta ${error ? 'alerta-error' : 'alerta-info'}" role="${error ? 'alert' : 'status'}">
        <t:icono nombre="${error ? 'ERROR' : 'INFO'}"/>
        <span class="visualmente-oculto">${error ? 'Error:' : 'Información:'}</span>
        <p><c:out value="${texto}"/></p>
    </div>
</c:if>

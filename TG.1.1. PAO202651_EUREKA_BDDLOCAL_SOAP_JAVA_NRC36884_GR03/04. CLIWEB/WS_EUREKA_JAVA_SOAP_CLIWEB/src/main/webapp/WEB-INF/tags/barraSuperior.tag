<%-- Componentes.barraSuperior: barra #0B1D44 con Volver opcional, título, subtítulo y una acción a la derecha (cuerpo). --%>
<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ attribute name="titulo" required="true" %>
<%@ attribute name="subtitulo" required="true" %>
<%@ attribute name="volver" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<header class="barra">
    <div class="barra-fila">
        <c:if test="${not empty volver}">
            <a class="boton-icono" href="${volver}" title="Volver" aria-label="Volver"><t:icono nombre="VOLVER" tamano="24"/></a>
        </c:if>
        <div class="barra-textos">
            <h1 class="barra-titulo"><c:out value="${titulo}"/></h1>
            <p class="barra-subtitulo"><c:out value="${subtitulo}"/></p>
        </div>
        <jsp:doBody/>
    </div>
</header>

<%--
    Documento HTML común de MonsterCell: tema "Azul", fuentes y comportamiento de la página.
    Con seccion (pantallas con sesión) el cuerpo va junto a la barra lateral; sin ella (login), solo.
--%>
<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ attribute name="titulo" required="true" %>
<%@ attribute name="seccion" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>MonsterCell | <c:out value="${titulo}"/></title>
    <link rel="icon" type="image/svg+xml" href="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Crect width='24' height='24' rx='6.7' fill='%231D56CC'/%3E%3Cpath fill='white' transform='translate(4.8 4.8) scale(0.6)' d='M17,1.01L7,1c-1.1,0 -2,0.9 -2,2v18c0,1.1 0.9,2 2,2h10c1.1,0 2,-0.9 2,-2V3c0,-1.1 -0.9,-1.99 -2,-1.99zM17,19H7V5h10v14z'/%3E%3C/svg%3E">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/monstercell.css">
    <%-- Con JavaScript la barra lateral de pantallas angostas es un panel; sin él queda arriba del contenido --%>
    <script>document.documentElement.classList.add('js');</script>
    <script src="${pageContext.request.contextPath}/js/monstercell.js" defer></script>
</head>
<body>
<c:choose>
    <c:when test="${not empty seccion}">
        <div class="app">
            <t:lateral seccion="${seccion}"/>
            <div class="app-principal">
                <jsp:doBody/>
            </div>
        </div>
    </c:when>
    <c:otherwise>
        <jsp:doBody/>
    </c:otherwise>
</c:choose>
</body>
</html>

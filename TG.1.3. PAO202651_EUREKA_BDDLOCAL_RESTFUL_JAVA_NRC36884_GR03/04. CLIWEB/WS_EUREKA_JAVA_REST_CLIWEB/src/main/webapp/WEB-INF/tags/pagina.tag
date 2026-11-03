<%-- Documento HTML común (la Ventana del escritorio): tema, fuentes y comportamiento de la página. --%>
<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ attribute name="titulo" required="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>EurekaBank | <c:out value="${titulo}"/></title>
    <link rel="icon" type="image/svg+xml" href="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Crect width='24' height='24' rx='6.7' fill='%231D56CC'/%3E%3Cpath fill='white' transform='translate(4.8 4.8) scale(0.6)' d='M4,10h3v7H4zM10.5,10h3v7h-3zM2,19h20v3H2zM17,10h3v7h-3zM12,1L2,6v2h20V6z'/%3E%3C/svg%3E">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/eureka.css">
    <script src="${pageContext.request.contextPath}/js/eureka.js" defer></script>
</head>
<body>
<jsp:doBody/>
</body>
</html>

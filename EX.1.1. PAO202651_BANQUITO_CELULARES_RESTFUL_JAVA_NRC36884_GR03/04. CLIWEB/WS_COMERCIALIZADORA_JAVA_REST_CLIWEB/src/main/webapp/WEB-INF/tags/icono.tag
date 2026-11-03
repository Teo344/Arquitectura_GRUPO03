<%-- Iconos vectoriales de 24 x 24 (los de EurekaBank más los de la tienda), en el color del texto que los rodea. --%>
<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="nombre" required="true" %>
<%@ attribute name="tamano" %>
<%@ attribute name="clase" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="lado" value="${empty tamano ? 20 : tamano}"/>
<c:choose>
    <c:when test="${nombre == 'BANCO'}"><c:set var="d" value="M4,10h3v7H4zM10.5,10h3v7h-3zM2,19h20v3H2zM17,10h3v7h-3zM12,1L2,6v2h20V6z"/></c:when>
    <c:when test="${nombre == 'CUENTA'}"><c:set var="d" value="M21,18v1c0,1.1 -0.9,2 -2,2H5c-1.11,0 -2,-0.9 -2,-2V5c0,-1.1 0.89,-2 2,-2h14c1.1,0 2,0.9 2,2v1h-9c-1.11,0 -2,0.9 -2,2v8c0,1.1 0.89,2 2,2h9zM12,16h10V8H12v8zM16,13.5c-0.83,0 -1.5,-0.67 -1.5,-1.5s0.67,-1.5 1.5,-1.5 1.5,0.67 1.5,1.5 -0.67,1.5 -1.5,1.5z"/></c:when>
    <c:when test="${nombre == 'MOVIMIENTOS'}"><c:set var="d" value="M19.5,3.5L18,2l-1.5,1.5L15,2l-1.5,1.5L12,2l-1.5,1.5L9,2L7.5,3.5L6,2v14H3v3c0,1.66 1.34,3 3,3h12c1.66,0 3,-1.34 3,-3V2L19.5,3.5zM19,19c0,0.55 -0.45,1 -1,1s-1,-0.45 -1,-1v-3H8V5h11V19zM9,7h6v2H9zM16,7h2v2h-2zM9,10h6v2H9zM16,10h2v2h-2z"/></c:when>
    <c:when test="${nombre == 'DEPOSITO'}"><c:set var="d" value="M20,12l-1.41,-1.41L13,16.17V4h-2v12.17l-5.58,-5.59L4,12l8,8 8,-8z"/></c:when>
    <c:when test="${nombre == 'RETIRO'}"><c:set var="d" value="M4,12l1.41,1.41L11,7.83V20h2V7.83l5.58,5.59L20,12l-8,-8 -8,8z"/></c:when>
    <c:when test="${nombre == 'TRANSFERENCIA'}"><c:set var="d" value="M6.99,11L3,15l3.99,4v-3H14v-2H6.99v-3zM21,9l-3.99,-4v3H10v2h7.01v3L21,9z"/></c:when>
    <c:when test="${nombre == 'SALIR'}"><c:set var="d" value="M17,7l-1.41,1.41L18.17,11H8v2h10.17l-2.58,2.58L17,17l5,-5zM4,5h8V3H4c-1.1,0 -2,0.9 -2,2v14c0,1.1 0.9,2 2,2h8v-2H4V5z"/></c:when>
    <c:when test="${nombre == 'MENU'}"><c:set var="d" value="M3,18h18v-2H3v2zM3,13h18v-2H3v2zM3,6v2h18V6H3z"/></c:when>
    <c:when test="${nombre == 'CERRAR'}"><c:set var="d" value="M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z"/></c:when>
    <c:when test="${nombre == 'VOLVER'}"><c:set var="d" value="M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z"/></c:when>
    <c:when test="${nombre == 'INFO'}"><c:set var="d" value="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM13,17h-2v-6h2v6zM13,9h-2L11,7h2v2z"/></c:when>
    <c:when test="${nombre == 'ERROR'}"><c:set var="d" value="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM13,17h-2v-2h2v2zM13,13h-2L11,7h2v6z"/></c:when>
    <c:when test="${nombre == 'EXITO'}"><c:set var="d" value="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM10,17l-5,-5 1.41,-1.41L10,14.17l7.59,-7.59L19,8l-9,9z"/></c:when>
    <c:when test="${nombre == 'ADVERTENCIA'}"><c:set var="d" value="M1,21h22L12,2 1,21zM13,18h-2v-2h2v2zM13,14h-2v-4h2v4z"/></c:when>
    <c:when test="${nombre == 'VER'}"><c:set var="d" value="M12,4.5C7,4.5 2.73,7.61 1,12c1.73,4.39 6,7.5 11,7.5s9.27,-3.11 11,-7.5c-1.73,-4.39 -6,-7.5 -11,-7.5zM12,17c-2.76,0 -5,-2.24 -5,-5s2.24,-5 5,-5 5,2.24 5,5 -2.24,5 -5,5zM12,9c-1.66,0 -3,1.34 -3,3s1.34,3 3,3 3,-1.34 3,-3 -1.34,-3 -3,-3z"/></c:when>
    <c:when test="${nombre == 'OCULTAR'}"><c:set var="d" value="M12,7c2.76,0 5,2.24 5,5 0,0.65 -0.13,1.26 -0.36,1.83l2.92,2.92c1.51,-1.26 2.7,-2.89 3.43,-4.75 -1.73,-4.39 -6,-7.5 -11,-7.5 -1.4,0 -2.74,0.25 -3.98,0.7l2.16,2.16C10.74,7.13 11.35,7 12,7zM2,4.27l2.28,2.28 0.46,0.46C3.08,8.3 1.78,10.02 1,12c1.73,4.39 6,7.5 11,7.5 1.55,0 3.03,-0.3 4.38,-0.84l0.42,0.42L19.73,22 21,20.73 3.27,3 2,4.27zM7.53,9.8l1.55,1.55c-0.05,0.21 -0.08,0.43 -0.08,0.65 0,1.66 1.34,3 3,3 0.22,0 0.44,-0.03 0.65,-0.08l1.55,1.55c-0.67,0.33 -1.41,0.53 -2.2,0.53 -2.76,0 -5,-2.24 -5,-5 0,-0.79 0.2,-1.53 0.53,-2.2zM11.84,9.02l3.15,3.15 0.02,-0.16c0,-1.66 -1.34,-3 -3,-3l-0.17,0.01z"/></c:when>
    <c:when test="${nombre == 'CELULAR'}"><c:set var="d" value="M17,1.01L7,1c-1.1,0 -2,0.9 -2,2v18c0,1.1 0.9,2 2,2h10c1.1,0 2,-0.9 2,-2V3c0,-1.1 -0.9,-1.99 -2,-1.99zM17,19H7V5h10v14z"/></c:when>
    <c:when test="${nombre == 'CARRITO'}"><c:set var="d" value="M7,18c-1.1,0 -1.99,0.9 -1.99,2S5.9,22 7,22s2,-0.9 2,-2 -0.9,-2 -2,-2zM1,2v2h2l3.6,7.59 -1.35,2.45c-0.16,0.28 -0.25,0.61 -0.25,0.96 0,1.1 0.9,2 2,2h12v-2H7.42c-0.14,0 -0.25,-0.11 -0.25,-0.25l0.03,-0.12 0.9,-1.63h7.45c0.75,0 1.41,-0.41 1.75,-1.03l3.58,-6.49c0.08,-0.14 0.12,-0.31 0.12,-0.48 0,-0.55 -0.45,-1 -1,-1H5.21l-0.94,-2H1zM17,18c-1.1,0 -1.99,0.9 -1.99,2s0.89,2 1.99,2 2,-0.9 2,-2 -0.9,-2 -2,-2z"/></c:when>
    <c:when test="${nombre == 'FACTURA'}"><c:set var="d" value="M18,17H6v-2h12v2zM18,13H6v-2h12v2zM18,9H6V7h12v2zM3,22l1.5,-1.5L6,22l1.5,-1.5L9,22l1.5,-1.5L12,22l1.5,-1.5L15,22l1.5,-1.5L18,22l1.5,-1.5L21,22V2l-1.5,1.5L18,2l-1.5,1.5L15,2l-1.5,1.5L12,2l-1.5,1.5L9,2 7.5,3.5 6,2 4.5,3.5 3,2v20z"/></c:when>
    <c:when test="${nombre == 'CATALOGO'}"><c:set var="d" value="M4,11h5V5H4v6zM4,18h5v-6H4v6zM10,18h5v-6h-5v6zM16,18h5v-6h-5v6zM10,11h5V5h-5v6zM16,5v6h5V5h-5z"/></c:when>
    <c:when test="${nombre == 'CREDITO'}"><c:set var="d" value="M20,4H4c-1.11,0 -1.99,0.89 -1.99,2L2,18c0,1.11 0.89,2 2,2h16c1.11,0 2,-0.89 2,-2V6c0,-1.11 -0.89,-2 -2,-2zM20,18H4v-6h16v6zM20,8H4V6h16v2z"/></c:when>
    <c:when test="${nombre == 'CALCULADORA'}"><c:set var="d" value="M19,3H5C3.9,3 3,3.9 3,5v14c0,1.1 0.9,2 2,2h14c1.1,0 2,-0.9 2,-2V5C21,3.9 20.1,3 19,3zM13.03,7.06L14.09,6l1.41,1.41L16.91,6l1.06,1.06 -1.41,1.41 1.41,1.41 -1.06,1.06L15.5,9.54l-1.41,1.41 -1.06,-1.06 1.41,-1.41L13.03,7.06zM6.25,7.72h5v1.5h-5V7.72zM11.5,16h-2v2H8v-2H6v-1.5h2v-2h1.5v2h2V16zM18,17.25h-5v-1.5h5V17.25zM18,14.75h-5v-1.5h5V14.75z"/></c:when>
    <c:when test="${nombre == 'PERSONA'}"><c:set var="d" value="M12,12c2.21,0 4,-1.79 4,-4s-1.79,-4 -4,-4 -4,1.79 -4,4 1.79,4 4,4zM12,14c-2.67,0 -8,1.34 -8,4v2h16v-2c0,-2.66 -5.33,-4 -8,-4z"/></c:when>
    <c:when test="${nombre == 'BUSCAR'}"><c:set var="d" value="M15.5,14h-0.79l-0.28,-0.27C15.41,12.59 16,11.11 16,9.5 16,5.91 13.09,3 9.5,3S3,5.91 3,9.5 5.91,16 9.5,16c1.61,0 3.09,-0.59 4.23,-1.57l0.27,0.28v0.79l5,4.99L20.49,19l-4.99,-5zM9.5,14C7.01,14 5,11.99 5,9.5S7.01,5 9.5,5 14,7.01 14,9.5 11.99,14 9.5,14z"/></c:when>
    <c:when test="${nombre == 'AGREGAR'}"><c:set var="d" value="M19,13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></c:when>
    <c:when test="${nombre == 'QUITAR'}"><c:set var="d" value="M19,13H5v-2h14v2z"/></c:when>
    <c:when test="${nombre == 'EDITAR'}"><c:set var="d" value="M3,17.25V21h3.75L17.81,9.94l-3.75,-3.75L3,17.25zM20.71,7.04c0.39,-0.39 0.39,-1.02 0,-1.41l-2.34,-2.34c-0.39,-0.39 -1.02,-0.39 -1.41,0l-1.83,1.83 3.75,3.75 1.83,-1.83z"/></c:when>
    <c:when test="${nombre == 'ELIMINAR'}"><c:set var="d" value="M6,19c0,1.1 0.9,2 2,2h8c1.1,0 2,-0.9 2,-2V7H6v12zM19,4h-3.5l-1,-1h-5l-1,1H5v2h14V4z"/></c:when>
    <c:when test="${nombre == 'IMPRIMIR'}"><c:set var="d" value="M19,8H5c-1.66,0 -3,1.34 -3,3v6h4v4h12v-4h4v-6c0,-1.66 -1.34,-3 -3,-3zM16,19H8v-5h8v5zM19,12c-0.55,0 -1,-0.45 -1,-1s0.45,-1 1,-1 1,0.45 1,1 -0.45,1 -1,1zM18,3H6v4h12V3z"/></c:when>
</c:choose>
<svg class="icono ${clase}" width="${lado}" height="${lado}" viewBox="0 0 24 24" aria-hidden="true" focusable="false"><path fill="currentColor" d="${d}"/></svg>

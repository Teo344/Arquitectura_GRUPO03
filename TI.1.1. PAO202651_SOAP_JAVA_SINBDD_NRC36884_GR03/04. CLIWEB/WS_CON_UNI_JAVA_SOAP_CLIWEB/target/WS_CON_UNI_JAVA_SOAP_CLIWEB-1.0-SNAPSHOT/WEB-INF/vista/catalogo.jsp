<%-- Catálogo de categorías y unidades consultadas desde el backend SOAP (CatalogoView). --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Conversión de Unidades Monster | Catálogo</title>
    <link rel="stylesheet" href="${raiz}/css/monster.css">
</head>
<body>
<div class="pagina pagina-ajustada">
    <header class="barra barra-catalogo">
        <div>
            <div class="barra-titulo">MONSTER · Catálogo de unidades</div>
            <div class="barra-subtitulo">Consulta las categorías y unidades disponibles</div>
        </div>
        <a class="boton-secundario" href="${raiz}/menu">Volver al inicio</a>
    </header>

    <main class="catalogo-cuerpo">
        <section class="panel-blanco">
            <h2 class="panel-titulo">Categorías</h2>
            <div class="desplazable">
                <div class="categorias">
                    <c:forEach var="cat" items="${categorias}" varStatus="i">
                        <a class="tarjeta-categoria${cat.codigo == seleccionada.codigo ? ' seleccionada' : ''}"
                           href="${raiz}/catalogo?categoria=${cat.codigo}">
                            <div class="tarjeta-categoria-contenido">
                                <div class="tarjeta-categoria-tipo">${i.count < 10 ? '0' : ''}${i.count}&nbsp;&nbsp; ${cat.isConvertible() ? 'CONVERSIÓN DISPONIBLE' : 'SOLO CONSULTA'}</div>
                                <div class="tarjeta-categoria-nombre"><c:out value="${cat.nombre}"/></div>
                                <div class="tarjeta-categoria-descripcion">
                                    <c:choose>
                                        <c:when test="${cat.codigo == 'LONGITUD'}">Distancias y medidas lineales.</c:when>
                                        <c:when test="${cat.codigo == 'MASA'}">Medidas de masa y peso.</c:when>
                                        <c:when test="${cat.codigo == 'VOLUMEN'}">Capacidad y espacio ocupado.</c:when>
                                        <c:when test="${cat.codigo == 'TIEMPO'}">Unidades para medir duración.</c:when>
                                        <c:when test="${cat.codigo == 'TEMPERATURA'}">Escalas de temperatura.</c:when>
                                        <c:when test="${cat.codigo == 'AREA'}">Superficie y extensión.</c:when>
                                        <c:otherwise>Categoría del catálogo de unidades.</c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </a>
                    </c:forEach>
                </div>
            </div>
        </section>

        <section class="panel-blanco">
            <h2 class="detalle-titulo">
                <c:choose>
                    <c:when test="${not empty seleccionada}"><c:out value="${seleccionada.nombre}"/></c:when>
                    <c:otherwise>Selecciona una categoría</c:otherwise>
                </c:choose>
            </h2>
            <p class="detalle-texto">Unidades registradas para esta categoría</p>
            <div class="desplazable">
                <c:choose>
                    <c:when test="${empty seleccionada}">
                        <div class="mensaje-unidades"><p>Elige una tarjeta para consultar las unidades de esa categoría.</p></div>
                    </c:when>
                    <c:when test="${empty unidades}">
                        <div class="mensaje-unidades"><p>No hay unidades registradas para esta categoría.</p></div>
                    </c:when>
                    <c:otherwise>
                        <div class="unidades">
                            <c:forEach var="u" items="${unidades}">
                                <div class="fila-unidad">
                                    <div class="fila-unidad-datos">
                                        <div class="fila-unidad-nombre"><c:out value="${u.nombre}"/></div>
                                        <div class="fila-unidad-codigo">Código: <c:out value="${u.codigo}"/></div>
                                    </div>
                                    <div class="fila-unidad-simbolo"><c:out value="${u.simbolo}"/></div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>
    </main>

    <p class="catalogo-estado${estadoError ? ' error' : ''}"><c:out value="${estado}"/></p>
</div>
</body>
</html>

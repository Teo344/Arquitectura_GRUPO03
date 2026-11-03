<%-- Facturas emitidas, las más recientes primero, con filtro por cédula del comprador. --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="m" uri="http://monster.edu.ec/monstercell" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="raiz" value="${pageContext.request.contextPath}"/>
<t:pagina titulo="Facturas" seccion="facturas">
<t:barraSuperior titulo="Facturas" subtitulo="Ventas emitidas, las más recientes primero"/>

<main class="contenido">
    <div class="pos-encabezado">
        <div>
            <p class="sobretitulo">Historial</p>
            <h2 class="titular-pequeno">Facturas emitidas</h2>
        </div>
        <form class="buscador" method="get" action="${raiz}/facturas" role="search">
            <t:icono nombre="BUSCAR"/>
            <label class="visualmente-oculto" for="cedula">Filtrar por cédula</label>
            <input id="cedula" name="cedula" type="search" inputmode="numeric" value="<c:out value='${cedula}'/>"
                   placeholder="Filtrar por cédula" autocomplete="off">
        </form>
    </div>
    <c:if test="${not empty mensaje}"><div class="espacio-m"><t:alerta texto="${mensaje}" error="${mensajeError}"/></div></c:if>

    <c:choose>
        <c:when test="${empty facturas}">
            <div class="espacio-l">
                <t:estadoVacio icono="FACTURA" titulo="${empty cedula ? 'Todavía no hay facturas' : 'Sin facturas para esta cédula'}"
                               texto="${empty cedula ? 'Las ventas que factures aparecerán aquí.' : 'Prueba con otra cédula o quita el filtro.'}"/>
            </div>
        </c:when>
        <c:otherwise>
            <p class="leyenda secundario espacio-m">${facturas.size()}${facturas.size() == 1 ? ' factura' : ' facturas'}<c:if test="${not empty cedula}"> de la cédula <c:out value="${cedula}"/> · <a class="enlace" href="${raiz}/facturas">ver todas</a></c:if></p>
            <div class="tarjeta tarjeta-tabla espacio-s">
                <div class="tabla-contenedor">
                    <table class="tabla">
                        <caption class="visualmente-oculto">Facturas emitidas</caption>
                        <thead>
                            <tr><th scope="col">N.º</th><th scope="col">Fecha</th><th scope="col">Cliente</th><th scope="col">Pago</th><th scope="col" class="numero">Total</th><th scope="col"><span class="visualmente-oculto">Acciones</span></th></tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${facturas}" var="f">
                                <tr class="fila-enlace">
                                    <td data-etiqueta="N.º"><a class="enlace-fila" href="${raiz}/facturas/${f.numFactura}">${f.numeroVisible}</a></td>
                                    <td data-etiqueta="Fecha">${m:fecha(f.fecha)}</td>
                                    <td data-etiqueta="Cliente"><span class="celda-pila"><span class="cuerpo-grande-seminegrita"><c:out value="${f.nombreCliente}"/></span><span class="leyenda secundario"><c:out value="${f.cedula}"/></span></span></td>
                                    <td data-etiqueta="Pago">
                                        <c:choose>
                                            <c:when test="${f.credito}"><t:chip texto="Crédito · ${f.numCuotas} cuotas" variante="cian" icono="CREDITO"/></c:when>
                                            <c:otherwise><t:chip texto="Efectivo" variante="exito"/></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td data-etiqueta="Total" class="numero cuerpo-grande-negrita">${m:importe(f.total)}</td>
                                    <td class="acciones"><a class="boton boton-texto boton-compacto" href="${raiz}/facturas/${f.numFactura}">Ver</a></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</main>
</t:pagina>

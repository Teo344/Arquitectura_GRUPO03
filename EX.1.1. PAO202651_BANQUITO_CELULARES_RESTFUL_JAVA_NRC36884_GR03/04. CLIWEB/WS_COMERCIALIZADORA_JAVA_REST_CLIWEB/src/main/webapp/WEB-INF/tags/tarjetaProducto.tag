<%--
    Tarjeta de un teléfono en el punto de venta: mosaico con el ícono (el catálogo no tiene fotos),
    marca, modelo, precio, precio en efectivo y el botón Agregar. data-buscar alimenta el filtro en vivo.
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="telefono" required="true" type="ec.edu.monster.modelo.TelefonoModelo" %>
<%@ attribute name="enCarrito" type="java.lang.Integer" %>
<%@ attribute name="busqueda" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="m" uri="http://monster.edu.ec/monstercell" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<article class="tarjeta producto" id="producto-${telefono.codTelefono}" data-buscar="<c:out value='${fn:toLowerCase(telefono.nombre)}'/>">
    <div class="producto-mosaico" aria-hidden="true">
        <t:icono nombre="CELULAR" tamano="40"/>
        <c:if test="${enCarrito > 0}"><span class="producto-en-carrito">${enCarrito} en la venta</span></c:if>
    </div>
    <div class="producto-cuerpo">
        <t:chip texto="${telefono.marca}" variante="azul"/>
        <h3 class="producto-modelo"><c:out value="${telefono.modelo}"/></h3>
        <p class="producto-precio">${m:importe(telefono.precio)}</p>
        <p class="producto-efectivo">Efectivo: ${m:importe(telefono.precioEfectivo)}</p>
    </div>
    <form method="post" action="${pageContext.request.contextPath}/venta/agregar">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
        <input type="hidden" name="codTelefono" value="${telefono.codTelefono}">
        <c:if test="${not empty busqueda}"><input type="hidden" name="q" value="<c:out value='${busqueda}'/>"></c:if>
        <button type="submit" class="boton boton-secundario boton-ancho" data-cargando
                aria-label="Agregar <c:out value='${telefono.nombre}'/> a la venta">
            <t:icono nombre="AGREGAR" tamano="18"/><span class="boton-texto-contenido">Agregar</span>
        </button>
    </form>
</article>

<%-- Línea del carrito: teléfono, contador − n + (máximo 100), subtotal y quitar. --%>
<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="linea" required="true" type="ec.edu.monster.modelo.LineaCarrito" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="m" uri="http://monster.edu.ec/monstercell" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="raiz" value="${pageContext.request.contextPath}"/>
<c:set var="codigo" value="${linea.telefono.codTelefono}"/>
<li class="linea">
    <div class="linea-textos">
        <span class="cuerpo-grande-seminegrita"><c:out value="${linea.telefono.modelo}"/></span>
        <span class="leyenda secundario"><c:out value="${linea.telefono.marca}"/> · ${m:importe(linea.telefono.precio)} c/u</span>
    </div>
    <div class="contador" role="group" aria-label="Cantidad de <c:out value='${linea.telefono.nombre}'/>">
        <form method="post" action="${raiz}/venta/cantidad">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="codTelefono" value="${codigo}">
            <input type="hidden" name="cantidad" value="${linea.cantidad - 1}">
            <button type="submit" class="contador-boton" aria-label="Quitar una unidad"><t:icono nombre="QUITAR" tamano="18"/></button>
        </form>
        <span class="contador-valor" aria-live="polite">${linea.cantidad}</span>
        <form method="post" action="${raiz}/venta/cantidad">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="codTelefono" value="${codigo}">
            <input type="hidden" name="cantidad" value="${linea.cantidad + 1}">
            <button type="submit" class="contador-boton" aria-label="Agregar una unidad" ${linea.cantidad >= 100 ? 'disabled' : ''}><t:icono nombre="AGREGAR" tamano="18"/></button>
        </form>
    </div>
    <span class="linea-subtotal cuerpo-grande-negrita">${m:importe(linea.subtotal)}</span>
    <form class="linea-quitar" method="post" action="${raiz}/venta/quitar">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
        <input type="hidden" name="codTelefono" value="${codigo}">
        <button type="submit" class="boton-icono boton-quitar" title="Quitar de la venta"
                aria-label="Quitar <c:out value='${linea.telefono.nombre}'/> de la venta"><t:icono nombre="ELIMINAR" tamano="20"/></button>
    </form>
</li>

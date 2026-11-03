<%-- Factura de venta lista para imprimir: encabezado de la tienda, cliente, detalle, totales y crédito. --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="m" uri="http://monster.edu.ec/monstercell" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="raiz" value="${pageContext.request.contextPath}"/>
<t:pagina titulo="Factura ${factura.numeroVisible}" seccion="facturas">
<t:barraSuperior titulo="Factura N.º ${factura.numeroVisible}" subtitulo="${factura.formaPago.nombreVisible} · ${m:fecha(factura.fecha)}" volver="${raiz}/facturas"/>

<main class="contenido contenido-factura">
    <c:if test="${not empty mensaje}"><div class="no-imprimir"><t:alerta texto="${mensaje}" error="${mensajeError}"/></div></c:if>

    <article class="tarjeta factura espacio-m">
        <header class="factura-encabezado">
            <div class="login-marca-logo">
                <span class="icono-caja icono-caja-44 icono-caja-accion"><t:icono nombre="CELULAR" tamano="22"/></span>
                <div>
                    <p class="titulo">MonsterCell</p>
                    <p class="leyenda secundario">Comercializadora de teléfonos celulares · Grupo 03</p>
                </div>
            </div>
            <div class="factura-numero">
                <p class="sobretitulo">Factura</p>
                <p class="titular-pequeno">N.º ${factura.numeroVisible}</p>
                <p class="leyenda secundario">${m:fecha(factura.fecha)} · vendedor <c:out value="${factura.usuario}"/></p>
            </div>
        </header>

        <section class="factura-cliente">
            <div>
                <p class="sobretitulo">Cliente</p>
                <p class="cuerpo-grande-seminegrita"><c:out value="${factura.nombreCliente}"/></p>
                <p class="leyenda secundario">Cédula <c:out value="${factura.cedula}"/> · cliente BanQuito N.º ${factura.codClienteBanco}</p>
            </div>
            <div>
                <p class="sobretitulo">Forma de pago</p>
                <c:choose>
                    <c:when test="${factura.credito}"><t:chip texto="Crédito BanQuito · ${factura.numCuotas} cuotas" variante="cian" icono="CREDITO"/></c:when>
                    <c:otherwise><t:chip texto="Efectivo · 42 % de descuento" variante="exito"/></c:otherwise>
                </c:choose>
            </div>
        </section>

        <div class="tabla-contenedor espacio-l">
            <table class="tabla">
                <caption class="visualmente-oculto">Detalle de la factura</caption>
                <thead>
                    <tr><th scope="col">Cant.</th><th scope="col">Descripción</th><th scope="col" class="numero">Precio unitario</th><th scope="col" class="numero">Subtotal</th></tr>
                </thead>
                <tbody>
                    <c:forEach items="${factura.detalles}" var="d">
                        <tr>
                            <td data-etiqueta="Cantidad">${d.cantidad}</td>
                            <td data-etiqueta="Descripción"><c:out value="${d.marca} ${d.modelo}"/></td>
                            <td data-etiqueta="Precio unitario" class="numero">${m:importe(d.precioUnitario)}</td>
                            <td data-etiqueta="Subtotal" class="numero cuerpo-grande-seminegrita">${m:importe(d.subtotal)}</td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>

        <div class="factura-pie espacio-l">
            <div class="factura-credito">
                <c:if test="${factura.credito}">
                    <div class="bloque-cian">
                        <span class="icono-caja icono-caja-44 icono-caja-cian"><t:icono nombre="CREDITO" tamano="22"/></span>
                        <div>
                            <p class="cuerpo-grande-seminegrita">Crédito BanQuito N.º ${factura.codCreditoBanco}</p>
                            <p class="leyenda secundario">${factura.numCuotas} cuotas mensuales fijas al 16.5 % anual</p>
                            <a class="enlace leyenda no-imprimir" href="${raiz}/creditos/${factura.codCreditoBanco}?cedula=${factura.cedula}">Ver tabla de amortización</a>
                        </div>
                    </div>
                </c:if>
            </div>
            <div class="factura-totales">
                <t:resumenTotales subtotal="${factura.subtotal}" descuento="${factura.descuento}" total="${factura.total}"/>
            </div>
        </div>
    </article>

    <div class="acciones-pagina no-imprimir">
        <button type="button" class="boton boton-secundario" data-imprimir><t:icono nombre="IMPRIMIR" tamano="18"/><span>Imprimir</span></button>
        <a class="boton boton-primario boton-auto" href="${raiz}/venta"><t:icono nombre="CARRITO" tamano="18"/><span>Nueva venta</span></a>
    </div>
</main>
</t:pagina>

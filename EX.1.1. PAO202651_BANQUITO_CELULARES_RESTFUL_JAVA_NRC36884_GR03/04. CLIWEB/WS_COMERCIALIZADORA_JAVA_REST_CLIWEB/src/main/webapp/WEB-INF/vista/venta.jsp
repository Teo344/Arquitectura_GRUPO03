<%--
    Punto de venta: catálogo a la izquierda y la venta actual (carrito) a la derecha, fija al desplazar.
    El carrito guía el orden: comprador → teléfonos → forma de pago → totales → Facturar.
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="m" uri="http://monster.edu.ec/monstercell" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="raiz" value="${pageContext.request.contextPath}"/>
<c:set var="evaluacion" value="${carrito.evaluacion}"/>
<c:set var="bloqueaCredito" value="${carrito.credito and not empty evaluacion and evaluacion.cliente and not evaluacion.sujetoCredito}"/>
<t:pagina titulo="Nueva venta" seccion="venta">
<t:barraSuperior titulo="Nueva venta" subtitulo="Verifica al comprador, arma la venta y factura en efectivo o a crédito"/>

<main class="contenido pos">
    <section class="pos-catalogo" aria-labelledby="titulo-catalogo">
        <div class="pos-encabezado">
            <div>
                <p class="sobretitulo">Catálogo</p>
                <h2 class="titulo" id="titulo-catalogo">Teléfonos a la venta</h2>
            </div>
            <form class="buscador" method="get" action="${raiz}/venta" role="search">
                <t:icono nombre="BUSCAR"/>
                <label class="visualmente-oculto" for="q">Buscar por marca o modelo</label>
                <input id="q" name="q" type="search" value="<c:out value='${q}'/>" placeholder="Buscar marca o modelo"
                       autocomplete="off" data-filtro-catalogo>
            </form>
        </div>
        <p class="leyenda secundario espacio-xs" data-contador-catalogo>
            ${catalogo.size()}${catalogo.size() == 1 ? ' modelo' : ' modelos'}<c:if test="${not empty q}"> para «<c:out value="${q}"/>» · <a class="enlace" href="${raiz}/venta">ver todos</a></c:if>
        </p>

        <c:choose>
            <c:when test="${empty catalogo and not empty q}">
                <div class="espacio-m"><t:estadoVacio icono="BUSCAR" titulo="Sin resultados" texto="No hay teléfonos que coincidan con la búsqueda."/></div>
            </c:when>
            <c:when test="${empty catalogo}">
                <div class="espacio-m"><t:estadoVacio icono="CATALOGO" titulo="Catálogo vacío" texto="Agrega teléfonos desde Catálogo › Teléfonos para empezar a vender."/></div>
            </c:when>
            <c:otherwise>
                <div class="productos espacio-m">
                    <c:forEach items="${catalogo}" var="telefono">
                        <t:tarjetaProducto telefono="${telefono}" enCarrito="${carrito.cantidades[telefono.codTelefono]}" busqueda="${q}"/>
                    </c:forEach>
                </div>
                <div class="espacio-m" data-sin-resultados hidden>
                    <t:estadoVacio icono="BUSCAR" titulo="Sin resultados" texto="No hay teléfonos que coincidan con la búsqueda."/>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <aside class="pos-carrito" id="carrito" aria-labelledby="titulo-venta">
        <div class="tarjeta carrito">
            <div class="encabezado">
                <span class="icono-caja icono-caja-44 icono-caja-cian"><t:icono nombre="CARRITO" tamano="22"/></span>
                <div class="crece">
                    <p class="sobretitulo">Venta actual</p>
                    <h2 class="titulo" id="titulo-venta">
                        <c:choose>
                            <c:when test="${carrito.vacio}">Sin teléfonos</c:when>
                            <c:otherwise>${carrito.articulos}${carrito.articulos == 1 ? ' unidad' : ' unidades'}</c:otherwise>
                        </c:choose>
                    </h2>
                </div>
                <c:if test="${not carrito.vacio or not empty carrito.cedula}">
                    <form method="post" action="${raiz}/venta/vaciar">
                        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                        <button type="submit" class="boton boton-texto boton-compacto">Vaciar</button>
                    </form>
                </c:if>
            </div>

            <c:if test="${not empty mensaje}"><div class="espacio-m"><t:alerta texto="${mensaje}" error="${mensajeError}"/></div></c:if>

            <%-- 1. Comprador --%>
            <section class="carrito-paso">
                <h3 class="carrito-paso-titulo"><span class="paso">1</span>Comprador</h3>
                <form class="fila-campo" method="post" action="${raiz}/venta/comprador" novalidate>
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <div class="crece">
                        <t:campo nombre="cedula" etiqueta="Cédula del comprador" marcador="1712345678" valor="${carrito.cedula}"
                                 modoEntrada="numeric"/>
                    </div>
                    <button type="submit" class="boton boton-secundario" data-cargando>
                        <t:icono nombre="PERSONA" tamano="18"/><span class="boton-texto-contenido">Verificar</span>
                    </button>
                </form>
                <c:choose>
                    <c:when test="${not empty evaluacion}"><div class="espacio-s"><t:tarjetaEvaluacion evaluacion="${evaluacion}" compacta="${true}"/></div></c:when>
                    <c:otherwise><p class="leyenda secundario espacio-xs">BanQuito confirma que es cliente y si puede comprar a crédito.</p></c:otherwise>
                </c:choose>
            </section>

            <%-- 2. Teléfonos --%>
            <section class="carrito-paso">
                <h3 class="carrito-paso-titulo"><span class="paso">2</span>Teléfonos</h3>
                <c:choose>
                    <c:when test="${carrito.vacio}">
                        <p class="carrito-vacio cuerpo secundario"><t:icono nombre="CELULAR" tamano="18"/>Agrega teléfonos desde el catálogo.</p>
                    </c:when>
                    <c:otherwise>
                        <ul class="lineas">
                            <c:forEach items="${carrito.lineas}" var="linea"><t:lineaCarrito linea="${linea}"/></c:forEach>
                        </ul>
                    </c:otherwise>
                </c:choose>
            </section>

            <%-- 3. Forma de pago --%>
            <section class="carrito-paso">
                <h3 class="carrito-paso-titulo"><span class="paso">3</span>Forma de pago</h3>
                <form method="post" action="${raiz}/venta/pago" data-enviar-al-cambiar>
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="numCuotas" value="${carrito.numCuotas}">
                    <div class="selector" role="group" aria-label="Forma de pago">
                        <button type="submit" name="formaPago" value="EFECTIVO" class="segmento${carrito.credito ? '' : ' elegido'}" aria-pressed="${not carrito.credito}">
                            <t:icono nombre="FACTURA" tamano="18"/><span>Efectivo −42 %</span>
                        </button>
                        <button type="submit" name="formaPago" value="CREDITO" class="segmento${carrito.credito ? ' elegido' : ''}" aria-pressed="${carrito.credito}">
                            <t:icono nombre="CREDITO" tamano="18"/><span>Crédito BanQuito</span>
                        </button>
                    </div>
                </form>
                <c:if test="${carrito.credito}">
                    <form class="fila-campo espacio-s" method="post" action="${raiz}/venta/pago" data-enviar-al-cambiar>
                        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                        <input type="hidden" name="formaPago" value="CREDITO">
                        <div class="campo crece">
                            <label for="numCuotas">Número de cuotas mensuales</label>
                            <div class="campo-caja">
                                <select id="numCuotas" name="numCuotas" class="campo-select">
                                    <c:forEach items="${carrito.opcionesCuotas}" var="n">
                                        <option value="${n}" ${n == carrito.numCuotas ? 'selected' : ''}>${n} cuotas</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <noscript><button type="submit" class="boton boton-secundario">Aplicar</button></noscript>
                    </form>
                    <c:if test="${not carrito.vacio}">
                        <div class="cuota-estimada espacio-s">
                            <c:choose>
                                <c:when test="${not empty cuotaEstimada}">
                                    <span class="cuerpo secundario">Cuota mensual estimada (16.5 % anual)</span>
                                    <span class="titulo texto-cian">${m:importe(cuotaEstimada)}</span>
                                    <a class="enlace leyenda" href="${raiz}/venta?tabla=1#carrito">Ver tabla de cuotas</a>
                                </c:when>
                                <c:otherwise>
                                    <span class="leyenda secundario">No se pudo calcular la cuota: <c:out value="${simulacionNoDisponible}"/></span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </c:if>
                </c:if>
            </section>

            <%-- 4. Totales, avisos y Facturar --%>
            <section class="carrito-paso">
                <t:resumenTotales subtotal="${carrito.subtotal}" descuento="${carrito.descuento}" total="${carrito.total}"/>
                <c:if test="${bloqueaCredito}">
                    <div class="espacio-s"><t:alerta texto="El comprador no es sujeto de crédito: puede pagar en efectivo." error="${true}"/></div>
                </c:if>
                <c:if test="${carrito.superaMontoMaximo}">
                    <div class="espacio-s"><t:alerta texto="El total supera el monto máximo de crédito (${m:importe(evaluacion.montoMaximo)}). Quita teléfonos o paga en efectivo." error="${true}"/></div>
                </c:if>
                <form class="espacio-m" method="post" action="${raiz}/venta/preparar">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <button type="submit" class="boton boton-primario" data-cargando ${carrito.vacio or bloqueaCredito or carrito.superaMontoMaximo ? 'disabled' : ''}>
                        <t:icono nombre="FACTURA" tamano="18"/><span class="boton-texto-contenido">Facturar ${carrito.vacio ? '' : m:importe(carrito.total)}</span>
                    </button>
                </form>
                <p class="leyenda secundario centrado espacio-xs">Los precios se confirman con el catálogo al facturar.</p>
            </section>
        </div>
    </aside>
</main>

<%-- Pantallas angostas: el carrito queda debajo del catálogo; esta barra mantiene la venta a la vista. --%>
<c:if test="${not carrito.vacio}">
    <a class="barra-venta-movil" href="#carrito">
        <t:icono nombre="CARRITO" tamano="22"/>
        <span class="crece">${carrito.articulos}${carrito.articulos == 1 ? ' unidad' : ' unidades'} · <strong>${m:importe(carrito.total)}</strong></span>
        <span class="barra-venta-movil-accion">Ver venta</span>
    </a>
</c:if>

<c:if test="${not empty resumenFactura}">
    <t:confirmacion titulo="Confirmar venta" filas="${resumenFactura}" textoConfirmar="Facturar"
                    texto="${carrito.credito ? 'BanQuito otorgará el crédito por el total y se emitirá la factura.' : 'Se emitirá la factura con el descuento por pago en efectivo.'}"
                    accion="${raiz}/venta/facturar" cancelar="${raiz}/venta#carrito"/>
</c:if>

<c:if test="${not empty tablaSimulacion}">
    <dialog class="dialogo dialogo-ancho" open data-modal data-cancelar="${raiz}/venta#carrito" aria-labelledby="titulo-simulacion">
        <h2 class="titulo" id="titulo-simulacion">Tabla de cuotas estimada</h2>
        <p class="cuerpo secundario">${m:importe(carrito.total)} a ${carrito.numCuotas} cuotas, 16.5 % anual. Es una simulación: el crédito se otorga al facturar.</p>
        <div class="espacio-m"><t:tablaAmortizacion cuotas="${tablaSimulacion}"/></div>
        <div class="dialogo-acciones"><a class="boton boton-primario" href="${raiz}/venta#carrito">Cerrar</a></div>
    </dialog>
</c:if>
</t:pagina>

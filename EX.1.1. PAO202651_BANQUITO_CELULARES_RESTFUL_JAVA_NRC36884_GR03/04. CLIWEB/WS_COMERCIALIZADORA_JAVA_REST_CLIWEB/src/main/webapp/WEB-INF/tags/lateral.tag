<%--
    Barra lateral (#0B1D44) de todas las pantallas con sesión: marca, navegación por secciones
    (la actual resaltada) y, abajo, el empleado, la vigencia de la sesión y Salir.
    En pantallas de menos de 1024 px es un panel que se abre con el botón de menú de la barra superior.
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="seccion" required="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="m" uri="http://monster.edu.ec/monstercell" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="raiz" value="${pageContext.request.contextPath}"/>
<c:set var="sesion" value="${sessionScope.clienteRest.sesion}"/>
<c:set var="carrito" value="${sessionScope['venta.carrito']}"/>
<aside class="lateral" id="lateral" aria-label="Navegación principal">
    <div class="lateral-marca">
        <span class="icono-caja icono-caja-40 icono-caja-accion"><t:icono nombre="CELULAR" tamano="22"/></span>
        <div class="crece">
            <p class="lateral-marca-nombre">MonsterCell</p>
            <p class="leyenda texto-borde-suave">Punto de venta · Grupo 03</p>
        </div>
        <button type="button" class="boton-icono lateral-cerrar" data-cerrar-lateral title="Cerrar menú" aria-label="Cerrar menú">
            <t:icono nombre="CERRAR" tamano="22"/>
        </button>
    </div>

    <nav class="lateral-nav">
        <p class="lateral-grupo">Ventas</p>
        <ul>
            <t:itemLateral href="${raiz}/venta" texto="Nueva venta" icono="CARRITO" activo="${seccion == 'venta'}"
                           contador="${empty carrito ? 0 : carrito.articulos}"/>
            <t:itemLateral href="${raiz}/facturas" texto="Facturas" icono="FACTURA" activo="${seccion == 'facturas'}"/>
        </ul>
        <p class="lateral-grupo">Catálogo</p>
        <ul>
            <t:itemLateral href="${raiz}/telefonos" texto="Teléfonos" icono="CATALOGO" activo="${seccion == 'telefonos'}"/>
        </ul>
        <p class="lateral-grupo">Crédito BanQuito</p>
        <ul>
            <t:itemLateral href="${raiz}/clientes" texto="Consultar cliente" icono="CREDITO" activo="${seccion == 'clientes'}"/>
            <t:itemLateral href="${raiz}/simulador" texto="Simulador de cuotas" icono="CALCULADORA" activo="${seccion == 'simulador'}"/>
        </ul>
    </nav>

    <div class="lateral-pie">
        <div class="lateral-usuario">
            <span class="avatar" aria-hidden="true">${m:iniciales(sesion.nombre)}</span>
            <div class="crece">
                <p class="lateral-usuario-nombre"><c:out value="${empty sesion.nombre ? 'Vendedor' : sesion.nombre}"/></p>
                <p class="leyenda texto-borde-suave">Usuario <c:out value="${m:valorO(sesion.usuario)}"/></p>
            </div>
        </div>
        <p class="lateral-sesion leyenda"><t:icono nombre="INFO" tamano="16"/>Sesión activa hasta las ${m:hora(sesion.expiraEn)}</p>
        <form method="post" action="${raiz}/salir">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <button type="submit" class="boton lateral-salir" data-cargando>
                <t:icono nombre="SALIR" tamano="18"/><span class="boton-texto-contenido">Cerrar sesión</span>
            </button>
        </form>
    </div>
</aside>
<div class="lateral-velo" data-cerrar-lateral hidden></div>

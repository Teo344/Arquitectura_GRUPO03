<%-- Vista del menú principal, igual a MenuVista: saludo, vigencia de la sesión y las cinco operaciones. --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="e" uri="http://monster.edu.ec/eureka" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="raiz" value="${pageContext.request.contextPath}"/>
<t:pagina titulo="Menú principal">
<t:barraSuperior titulo="Hola, ${empty sesion.nombreEmpleado ? 'empleado' : sesion.nombreEmpleado}"
                 subtitulo="Sucursal ${e:valorO(sesion.codigoSucursal)} · ${e:valorO(sesion.sucursal)}">
    <form method="post" action="${raiz}/salir">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
        <button type="submit" class="boton boton-peligro" data-cargando>
            <t:icono nombre="SALIR" tamano="18"/><span class="boton-texto-contenido">Salir</span>
        </button>
    </form>
</t:barraSuperior>

<main class="contenido">
    <p class="sobretitulo">Panel de operaciones</p>
    <h2 class="titular-pequeno">¿Qué deseas hacer hoy?</h2>
    <div class="espacio-m">
        <t:chip texto="Sesión activa hasta las ${e:hora(sesion.expiraEn)} · se renueva con cada operación"
                variante="cian" icono="INFO"/>
    </div>

    <section class="seccion">
        <p class="sobretitulo">Consultas</p>
        <h3 class="titulo espacio-xxs">Información de cuentas</h3>
        <div class="cuadricula">
            <t:tarjetaAccion href="${raiz}/consultas?modo=cuenta" titulo="Consultar cuenta"
                             descripcion="Titular, moneda, saldo y estado de una cuenta." icono="CUENTA"/>
            <t:tarjetaAccion href="${raiz}/consultas?modo=movimientos" titulo="Movimientos"
                             descripcion="Historial de ingresos y salidas de una cuenta." icono="MOVIMIENTOS"/>
        </div>
    </section>

    <section class="seccion">
        <p class="sobretitulo">Transacciones</p>
        <h3 class="titulo espacio-xxs">Registrar una operación</h3>
        <div class="cuadricula">
            <t:tarjetaAccion href="${raiz}/transacciones?tipo=DEPOSITO" titulo="Depósito"
                             descripcion="Suma un importe al saldo de una cuenta." icono="DEPOSITO"/>
            <t:tarjetaAccion href="${raiz}/transacciones?tipo=RETIRO" titulo="Retiro"
                             descripcion="Descuenta un importe con saldo suficiente." icono="RETIRO"/>
            <t:tarjetaAccion href="${raiz}/transacciones?tipo=TRANSFERENCIA" titulo="Transferencia"
                             descripcion="Mueve fondos entre cuentas de la misma moneda." icono="TRANSFERENCIA"
                             acento="${true}"/>
        </div>
    </section>
</main>
</t:pagina>

<%-- Vista de consultas (RQF-003 y RQF-004), igual a CuentaVista: búsqueda a la izquierda y resultado a la derecha. --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="e" uri="http://monster.edu.ec/eureka" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="raiz" value="${pageContext.request.contextPath}"/>
<c:set var="esCuenta" value="${modo == 'cuenta'}"/>
<t:pagina titulo="Consultas">
<t:barraSuperior titulo="Consultas" subtitulo="Cuentas y movimientos" volver="${raiz}/menu"/>

<main class="contenido dos-columnas columnas-42">
    <form class="tarjeta pila" method="get" action="${raiz}/consultas">
        <input type="hidden" name="modo" value="${modo}">
        <p class="sobretitulo">Consulta</p>
        <h2 class="titulo espacio-xxs">Buscar una cuenta</h2>
        <nav class="selector espacio-m" aria-label="Tipo de consulta">
            <t:segmento href="${raiz}/consultas?modo=cuenta" texto="Datos de la cuenta" icono="CUENTA" elegido="${esCuenta}"/>
            <t:segmento href="${raiz}/consultas?modo=movimientos" texto="Movimientos" icono="MOVIMIENTOS" elegido="${not esCuenta}"/>
        </nav>
        <div class="espacio-m">
            <t:campo nombre="cuenta" etiqueta="Número de cuenta" marcador="00100001" valor="${cuenta}"
                     ayuda="8 dígitos, por ejemplo 00100001" modoEntrada="numeric" enfocar="${true}"/>
        </div>
        <button type="submit" class="boton boton-primario espacio-l" data-cargando>
            <t:icono nombre="${esCuenta ? 'CUENTA' : 'MOVIMIENTOS'}" tamano="18"/>
            <span class="boton-texto-contenido">${esCuenta ? 'Consultar cuenta' : 'Ver movimientos'}</span>
        </button>
        <div class="espacio-m"><t:alerta texto="${mensaje}" error="${mensajeError}"/></div>
    </form>

    <div class="resultado">
        <c:choose>
            <c:when test="${esCuenta and empty datosCuenta}">
                <t:estadoVacio icono="CUENTA" titulo="Sin consulta"
                               texto="Escribe un número de cuenta para ver su titular, saldo y estado."/>
            </c:when>
            <c:when test="${esCuenta}">
                <section class="tarjeta">
                    <div class="encabezado">
                        <span class="icono-caja icono-caja-44 icono-caja-azul"><t:icono nombre="CUENTA" tamano="22"/></span>
                        <div class="crece">
                            <p class="sobretitulo">Cuenta</p>
                            <h2 class="titulo"><c:out value="${e:valorO(datosCuenta.codigo)}"/></h2>
                        </div>
                        <t:chipEstado estado="${datosCuenta.estado}"/>
                    </div>
                    <p class="cuerpo secundario espacio-l">Saldo disponible</p>
                    <p class="titular-mediano texto-azul-oscuro"><c:out value="${e:importe(datosCuenta.saldo)}"/></p>
                    <p class="boton-letra texto-cian"><c:out value="${datosCuenta.moneda}"/></p>
                    <hr class="divisor espacio-m">
                    <dl class="ficha">
                        <t:filaDato etiqueta="Titular" valor="${e:valorO(datosCuenta.titular)}"/>
                        <t:filaDato etiqueta="Sucursal" valor="${e:valorO(datosCuenta.sucursal)}"/>
                        <t:filaDato etiqueta="Fecha de creación" valor="${e:valorO(datosCuenta.fechaCreacion)}"/>
                    </dl>
                </section>
            </c:when>
            <c:when test="${movimientos == null}">
                <t:estadoVacio icono="MOVIMIENTOS" titulo="Sin consulta"
                               texto="Escribe un número de cuenta para ver su historial de movimientos."/>
            </c:when>
            <c:when test="${empty movimientos}">
                <t:estadoVacio icono="INFO" titulo="Cuenta ${cuentaMovimientos}" texto="La cuenta no registra movimientos."/>
            </c:when>
            <c:otherwise>
                <section class="tarjeta">
                    <div class="encabezado">
                        <div class="crece">
                            <p class="sobretitulo">Cuenta <c:out value="${cuentaMovimientos}"/></p>
                            <h2 class="titulo">Historial de movimientos</h2>
                        </div>
                        <t:chip texto="${movimientos.size()} movimientos" variante="azul"/>
                    </div>
                    <ul class="movimientos">
                        <c:forEach items="${movimientos}" var="m">
                            <t:itemMovimiento movimiento="${m}" importe="${e:importe(m.importe)}"/>
                        </c:forEach>
                    </ul>
                </section>
            </c:otherwise>
        </c:choose>
    </div>
</main>
</t:pagina>

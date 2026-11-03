<%-- Simulador de cuotas: la tabla de amortización de un crédito sin otorgarlo. --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="m" uri="http://monster.edu.ec/monstercell" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="raiz" value="${pageContext.request.contextPath}"/>
<t:pagina titulo="Simulador de cuotas" seccion="simulador">
<t:barraSuperior titulo="Simulador de cuotas" subtitulo="Crédito BanQuito · 16.5 % anual, de 3 a 18 cuotas"/>

<main class="contenido dos-columnas columnas-40">
    <form class="tarjeta pila" method="get" action="${raiz}/simulador" novalidate>
        <div class="encabezado">
            <span class="icono-caja icono-caja-44 icono-caja-cian"><t:icono nombre="CALCULADORA" tamano="22"/></span>
            <div class="crece">
                <p class="sobretitulo">Simulación</p>
                <h2 class="titulo">Calcular cuotas</h2>
            </div>
        </div>
        <div class="espacio-m"><t:campo nombre="monto" etiqueta="Monto a financiar" marcador="450.00" valor="${monto}" modoEntrada="decimal" enfocar="${true}"/></div>
        <div class="campo espacio-m">
            <label for="cuotas">Número de cuotas</label>
            <div class="campo-caja">
                <select id="cuotas" name="cuotas" class="campo-select">
                    <c:forEach begin="3" end="18" var="n"><option value="${n}" ${n == numCuotas ? 'selected' : ''}>${n} cuotas</option></c:forEach>
                </select>
            </div>
        </div>
        <button type="submit" class="boton boton-primario espacio-l" data-cargando>
            <t:icono nombre="CALCULADORA" tamano="18"/><span class="boton-texto-contenido">Calcular</span>
        </button>
        <div class="espacio-m"><t:alerta texto="${mensaje}" error="${mensajeError}"/></div>
        <p class="leyenda secundario espacio-m">Ejemplo del enunciado: 450.00 a 6 cuotas da una cuota de 78.65.</p>
    </form>

    <section class="resultado pila">
        <c:choose>
            <c:when test="${empty cuotas}">
                <t:estadoVacio icono="CALCULADORA" titulo="Sin simulación" texto="Ingresa un monto y el número de cuotas para ver la tabla de amortización."/>
            </c:when>
            <c:otherwise>
                <div class="resumen-credito resumen-credito-2">
                    <div class="tarjeta dato-clave"><span class="cuerpo secundario">Cuota fija</span><span class="titular-pequeno texto-cian">${m:importe(resumen.valorCuota)}</span><span class="leyenda secundario">${resumen.numCuotas} cuotas mensuales</span></div>
                    <div class="tarjeta dato-clave"><span class="cuerpo secundario">Interés total</span><span class="titular-pequeno">${m:importe(resumen.interesTotal)}</span><span class="leyenda secundario">Total a pagar ${m:importe(resumen.totalPagado)}</span></div>
                </div>
                <div class="tarjeta tarjeta-tabla espacio-m"><t:tablaAmortizacion cuotas="${cuotas}"/></div>
            </c:otherwise>
        </c:choose>
    </section>
</main>
</t:pagina>

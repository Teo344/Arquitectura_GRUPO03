<%-- Tabla de amortización de un crédito otorgado por BanQuito (servicio 4 del banco). --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="m" uri="http://monster.edu.ec/monstercell" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="raiz" value="${pageContext.request.contextPath}"/>
<t:pagina titulo="Crédito ${codCredito}" seccion="clientes">
<t:barraSuperior titulo="Crédito BanQuito N.º ${codCredito}" subtitulo="Tabla de amortización · cuota fija mensual"
                 volver="${empty cedula ? raiz.concat('/clientes') : raiz.concat('/clientes?cedula=').concat(cedula)}"/>

<main class="contenido">
    <c:if test="${not empty mensaje}"><t:alerta texto="${mensaje}" error="${mensajeError}"/></c:if>
    <c:if test="${not empty resumen}">
        <div class="resumen-credito">
            <div class="tarjeta dato-clave"><span class="cuerpo secundario">Monto financiado</span><span class="titular-pequeno texto-azul-oscuro">${m:importe(resumen.monto)}</span></div>
            <div class="tarjeta dato-clave"><span class="cuerpo secundario">Cuota fija</span><span class="titular-pequeno texto-cian">${m:importe(resumen.valorCuota)}</span><span class="leyenda secundario">${resumen.numCuotas} cuotas mensuales</span></div>
            <div class="tarjeta dato-clave"><span class="cuerpo secundario">Interés total</span><span class="titular-pequeno">${m:importe(resumen.interesTotal)}</span><span class="leyenda secundario">Tasa 16.5 % anual</span></div>
            <div class="tarjeta dato-clave"><span class="cuerpo secundario">Total a pagar</span><span class="titular-pequeno">${m:importe(resumen.totalPagado)}</span></div>
        </div>
        <div class="tarjeta tarjeta-tabla espacio-m"><t:tablaAmortizacion cuotas="${cuotas}" titulo="Tabla de amortización del crédito ${codCredito}"/></div>
    </c:if>
</main>
</t:pagina>

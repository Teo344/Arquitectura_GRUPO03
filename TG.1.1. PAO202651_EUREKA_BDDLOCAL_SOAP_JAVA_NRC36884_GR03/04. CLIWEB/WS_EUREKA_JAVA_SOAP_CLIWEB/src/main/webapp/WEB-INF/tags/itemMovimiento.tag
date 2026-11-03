<%-- Componentes.itemMovimiento: icono y signo indican ingreso o salida (además del color), con número, fecha y referencia. --%>
<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="movimiento" required="true" type="ec.edu.monster.modelo.MovimientoModelo" %>
<%@ attribute name="importe" required="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ingreso" value="${movimiento.accion == 'INGRESO'}"/>
<li class="movimiento">
    <span class="movimiento-circulo ${ingreso ? 'ingreso' : 'salida'}" title="${ingreso ? 'Ingreso' : 'Salida'}">
        <t:icono nombre="${ingreso ? 'DEPOSITO' : 'RETIRO'}"/>
    </span>
    <span class="movimiento-textos">
        <span class="cuerpo-grande-seminegrita"><c:out value="${empty movimiento.tipo ? '-' : movimiento.tipo}"/></span>
        <span class="leyenda secundario">N.º ${movimiento.numero} · <c:out value="${empty movimiento.fecha ? '-' : movimiento.fecha}"/><c:if test="${not empty movimiento.cuentaReferencia}"> · Ref. <c:out value="${movimiento.cuentaReferencia}"/></c:if></span>
    </span>
    <span class="movimiento-importe">
        <span class="cuerpo-grande-negrita ${ingreso ? 'texto-exito' : 'texto-error'}">${ingreso ? '+ ' : '− '}<c:out value="${importe}"/></span>
        <span class="leyenda secundario"><c:out value="${movimiento.accion}"/></span>
    </span>
</li>

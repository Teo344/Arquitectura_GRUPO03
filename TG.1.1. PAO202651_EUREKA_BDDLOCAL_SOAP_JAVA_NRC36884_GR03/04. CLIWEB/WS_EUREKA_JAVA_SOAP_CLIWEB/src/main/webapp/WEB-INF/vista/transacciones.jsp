<%-- Vista de transacciones (RQF-005 a RQF-007), igual a TransaccionVista: formulario, confirmación y comprobante. --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="e" uri="http://monster.edu.ec/eureka" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="raiz" value="${pageContext.request.contextPath}"/>
<c:set var="esTransferencia" value="${tipo.name() == 'TRANSFERENCIA'}"/>
<t:pagina titulo="Transacciones">
<t:barraSuperior titulo="Transacciones" subtitulo="Depósitos, retiros y transferencias" volver="${raiz}/menu"/>

<main class="contenido dos-columnas columnas-45">
    <form class="tarjeta pila" method="post" action="${raiz}/transacciones/preparar" novalidate>
        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
        <input type="hidden" name="tipo" value="${tipo.name()}">
        <p class="sobretitulo">Nueva operación</p>
        <h2 class="titulo espacio-xxs">Registrar ${fn:toLowerCase(tipo.nombreVisible)}</h2>
        <nav class="selector espacio-m" aria-label="Tipo de transacción">
            <c:forEach items="${tipos}" var="opcion">
                <t:segmento href="${raiz}/transacciones?tipo=${opcion.name()}" texto="${opcion.nombreVisible}"
                            icono="${opcion.name()}" elegido="${opcion == tipo}"/>
            </c:forEach>
        </nav>
        <div class="espacio-m">
            <t:campo nombre="cuenta" etiqueta="${esTransferencia ? 'Cuenta de origen' : 'Número de cuenta'}"
                     marcador="00100001" valor="${formulario.cuenta}" ayuda="8 dígitos, por ejemplo 00100001"
                     modoEntrada="numeric" enfocar="${empty pendiente}"/>
        </div>
        <c:if test="${esTransferencia}">
            <div class="espacio-m">
                <t:campo nombre="cuentaDestino" etiqueta="Cuenta de destino" marcador="00200002"
                         valor="${formulario.cuentaDestino}" ayuda="Debe ser distinta de la de origen y de la misma moneda"
                         modoEntrada="numeric"/>
            </div>
        </c:if>
        <div class="espacio-m">
            <t:campo nombre="importe" etiqueta="Importe" marcador="150.00" valor="${formulario.importe}"
                     ayuda="Mayor que cero y con máximo dos decimales" modoEntrada="decimal"/>
        </div>
        <button type="submit" class="boton boton-primario espacio-l" data-cargando>
            <t:icono nombre="${tipo.name()}" tamano="18"/><span class="boton-texto-contenido">Continuar</span>
        </button>
        <div class="espacio-m"><t:alerta texto="${mensaje}" error="${mensajeError}"/></div>
    </form>

    <div class="resultado">
        <c:choose>
            <c:when test="${empty registrada or empty resultado}">
                <t:estadoVacio icono="MOVIMIENTOS" titulo="Sin operaciones registradas"
                               texto="Completa el formulario y confirma para ver aquí el comprobante con el nuevo saldo."/>
            </c:when>
            <c:otherwise>
                <t:comprobante mensaje="${empty resultado.mensaje ? 'Operación registrada' : resultado.mensaje}"
                               filas="${e:comprobante(registrada, resultado)}"/>
            </c:otherwise>
        </c:choose>
    </div>
</main>

<c:if test="${not empty pendiente}">
    <t:confirmacion titulo="Confirmar ${fn:toLowerCase(pendiente.tipo.nombreVisible)}"
                    filas="${e:resumen(pendiente)}" textoConfirmar="Registrar"
                    accion="${raiz}/transacciones/registrar" cancelar="${raiz}/transacciones?tipo=${tipo.name()}"/>
</c:if>
</t:pagina>

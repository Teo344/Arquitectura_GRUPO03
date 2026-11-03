<%--
    Resultado del banco para un comprador: nombre, chip de sujeto de crédito (nunca solo color),
    motivos si no lo es y monto máximo. compacta = versión del carrito (el chip va bajo el nombre).
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="evaluacion" required="true" type="ec.edu.monster.modelo.EvaluacionClienteModelo" %>
<%@ attribute name="compacta" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="m" uri="http://monster.edu.ec/monstercell" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<div class="evaluacion ${compacta ? 'evaluacion-compacta' : ''}" aria-live="polite">
    <c:choose>
        <c:when test="${not evaluacion.cliente}">
            <div class="encabezado">
                <span class="icono-caja icono-caja-44 icono-caja-error"><t:icono nombre="ERROR" tamano="22"/></span>
                <div class="crece">
                    <p class="cuerpo-grande-seminegrita">No es cliente de BanQuito</p>
                    <p class="leyenda secundario">Cédula <c:out value="${evaluacion.cedula}"/> · solo los clientes del banco pueden comprar</p>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <c:set var="chip">
                <c:choose>
                    <c:when test="${evaluacion.sujetoCredito}"><t:chip texto="Sujeto de crédito" variante="exito" icono="EXITO"/></c:when>
                    <c:otherwise><t:chip texto="No sujeto de crédito" variante="advertencia" icono="ADVERTENCIA"/></c:otherwise>
                </c:choose>
            </c:set>
            <div class="encabezado">
                <span class="icono-caja icono-caja-44 icono-caja-azul"><t:icono nombre="PERSONA" tamano="22"/></span>
                <div class="crece">
                    <p class="cuerpo-grande-seminegrita"><c:out value="${evaluacion.nombre}"/></p>
                    <p class="leyenda secundario">Cliente BanQuito N.º ${evaluacion.codCliente}<c:if test="${not compacta}"> · cédula <c:out value="${evaluacion.cedula}"/></c:if></p>
                    <c:if test="${compacta}"><div class="evaluacion-chip">${chip}</div></c:if>
                </div>
                <c:if test="${not compacta}">${chip}</c:if>
            </div>
            <c:if test="${not evaluacion.sujetoCredito}">
                <ul class="motivos">
                    <c:forEach items="${evaluacion.motivos}" var="motivo">
                        <li><t:icono nombre="ADVERTENCIA" tamano="16"/><span><c:out value="${motivo.descripcion}"/></span></li>
                    </c:forEach>
                </ul>
            </c:if>
            <div class="evaluacion-monto">
                <span class="cuerpo secundario">Monto máximo de crédito</span>
                <span class="${compacta ? 'titulo' : 'titular-mediano'} texto-azul-oscuro">${m:importe(evaluacion.montoMaximo)}</span>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%-- Totales de una venta o factura: subtotal, descuento (solo si hay; en verde, con signo −) y total destacado. --%>
<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="subtotal" required="true" type="java.math.BigDecimal" %>
<%@ attribute name="descuento" required="true" type="java.math.BigDecimal" %>
<%@ attribute name="total" required="true" type="java.math.BigDecimal" %>
<%@ attribute name="etiquetaTotal" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="m" uri="http://monster.edu.ec/monstercell" %>
<dl class="totales" aria-live="polite">
    <div class="fila-dato"><dt class="cuerpo secundario">Subtotal</dt><dd class="cuerpo-grande-seminegrita">${m:importe(subtotal)}</dd></div>
    <c:if test="${descuento.signum() > 0}">
        <div class="fila-dato"><dt class="cuerpo secundario">Descuento efectivo (42 %)</dt>
            <dd class="cuerpo-grande-seminegrita texto-exito">− ${m:importe(descuento)}</dd></div>
    </c:if>
    <div class="fila-dato totales-total"><dt class="titulo">${empty etiquetaTotal ? 'Total' : etiquetaTotal}</dt><dd class="titular-pequeno texto-azul-oscuro">${m:importe(total)}</dd></div>
</dl>

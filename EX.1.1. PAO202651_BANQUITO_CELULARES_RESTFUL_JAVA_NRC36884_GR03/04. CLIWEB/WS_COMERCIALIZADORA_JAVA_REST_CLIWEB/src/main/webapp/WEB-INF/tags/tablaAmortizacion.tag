<%-- Tabla de amortización de cuota fija: N.º, cuota, interés, capital y saldo (la última cuota cierra en 0.00). --%>
<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="cuotas" required="true" type="java.util.List" %>
<%@ attribute name="titulo" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="m" uri="http://monster.edu.ec/monstercell" %>
<div class="tabla-contenedor">
    <table class="tabla tabla-numerica">
        <caption class="visualmente-oculto"><c:out value="${empty titulo ? 'Tabla de amortización' : titulo}"/></caption>
        <thead>
            <tr><th scope="col">N.º</th><th scope="col">Cuota</th><th scope="col">Interés</th><th scope="col">Capital</th><th scope="col">Saldo</th></tr>
        </thead>
        <tbody>
            <c:forEach items="${cuotas}" var="cuota" varStatus="estado">
                <tr class="${estado.last ? 'fila-final' : ''}">
                    <td data-etiqueta="N.º">${cuota.numero}</td>
                    <td data-etiqueta="Cuota">${m:importe(cuota.valorCuota)}</td>
                    <td data-etiqueta="Interés">${m:importe(cuota.interes)}</td>
                    <td data-etiqueta="Capital">${m:importe(cuota.capital)}</td>
                    <td data-etiqueta="Saldo">${m:importe(cuota.saldo)}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>

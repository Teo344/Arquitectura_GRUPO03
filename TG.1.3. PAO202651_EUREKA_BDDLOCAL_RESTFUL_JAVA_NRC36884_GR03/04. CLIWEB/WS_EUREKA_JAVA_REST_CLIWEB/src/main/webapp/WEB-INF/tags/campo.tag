<%-- Componentes.CampoTexto: etiqueta visible encima, campo de 48 de alto y ayuda debajo. Las claves se pueden mostrar u ocultar. --%>
<%@ tag pageEncoding="UTF-8" body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="nombre" required="true" %>
<%@ attribute name="etiqueta" required="true" %>
<%@ attribute name="valor" %>
<%@ attribute name="marcador" %>
<%@ attribute name="ayuda" %>
<%@ attribute name="clave" type="java.lang.Boolean" %>
<%@ attribute name="modoEntrada" %>
<%@ attribute name="autocompletar" %>
<%@ attribute name="enfocar" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<div class="campo">
    <label for="${nombre}"><c:out value="${etiqueta}"/></label>
    <div class="campo-caja">
        <input id="${nombre}" name="${nombre}" type="${clave ? 'password' : 'text'}" value="<c:out value='${valor}'/>"
               <c:if test="${not empty marcador}">placeholder="<c:out value='${marcador}'/>"</c:if>
               <c:if test="${not empty modoEntrada}">inputmode="${modoEntrada}"</c:if>
               <c:if test="${not empty ayuda}">aria-describedby="${nombre}-ayuda"</c:if>
               autocomplete="${empty autocompletar ? 'off' : autocompletar}" ${enfocar ? 'autofocus' : ''}>
        <c:if test="${clave}">
            <button type="button" class="boton-icono boton-ver" data-ver-clave="${nombre}"
                    title="Mostrar contraseña" aria-label="Mostrar contraseña">
                <span class="ver"><t:icono nombre="VER"/></span><span class="ocultar"><t:icono nombre="OCULTAR"/></span>
            </button>
        </c:if>
    </div>
    <c:if test="${not empty ayuda}"><p class="campo-ayuda" id="${nombre}-ayuda"><c:out value="${ayuda}"/></p></c:if>
</div>

<%-- Mantenimiento del catálogo: formulario (nuevo o editar) a la izquierda y la lista a la derecha. --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="m" uri="http://monster.edu.ec/monstercell" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="raiz" value="${pageContext.request.contextPath}"/>
<c:set var="editando" value="${not empty formulario.codTelefono}"/>
<t:pagina titulo="Catálogo" seccion="telefonos">
<t:barraSuperior titulo="Catálogo" subtitulo="Agrega, edita o da de baja los modelos a la venta"/>

<main class="contenido dos-columnas columnas-40">
    <form class="tarjeta pila" method="post" action="${raiz}/telefonos/guardar" novalidate>
        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
        <input type="hidden" name="codTelefono" value="<c:out value='${formulario.codTelefono}'/>">
        <div class="encabezado">
            <span class="icono-caja icono-caja-44 ${editando ? 'icono-caja-cian' : 'icono-caja-azul'}"><t:icono nombre="${editando ? 'EDITAR' : 'AGREGAR'}" tamano="22"/></span>
            <div class="crece">
                <p class="sobretitulo">${editando ? 'Editar' : 'Nuevo'}</p>
                <h2 class="titulo">${editando ? 'Editar teléfono' : 'Agregar teléfono'}</h2>
            </div>
        </div>
        <div class="espacio-m"><t:campo nombre="marca" etiqueta="Marca" marcador="Samsung" valor="${formulario.marca}" enfocar="${true}"/></div>
        <div class="espacio-m"><t:campo nombre="modelo" etiqueta="Modelo" marcador="Galaxy A55" valor="${formulario.modelo}"/></div>
        <div class="espacio-m">
            <t:campo nombre="precio" etiqueta="Precio de venta" marcador="549.00" valor="${formulario.precio}" modoEntrada="decimal"
                     ayuda="En efectivo se vende con 42 % de descuento"/>
        </div>
        <button type="submit" class="boton boton-primario espacio-l" data-cargando>
            <t:icono nombre="${editando ? 'EDITAR' : 'AGREGAR'}" tamano="18"/>
            <span class="boton-texto-contenido">${editando ? 'Guardar cambios' : 'Agregar al catálogo'}</span>
        </button>
        <c:if test="${editando}"><a class="boton boton-texto espacio-xs" href="${raiz}/telefonos">Cancelar edición</a></c:if>
        <div class="espacio-m"><t:alerta texto="${mensaje}" error="${mensajeError}"/></div>
    </form>

    <section class="resultado">
        <c:choose>
            <c:when test="${empty telefonos}">
                <t:estadoVacio icono="CATALOGO" titulo="Catálogo vacío" texto="Agrega el primer teléfono con el formulario."/>
            </c:when>
            <c:otherwise>
                <div class="tarjeta tarjeta-tabla">
                    <div class="encabezado tarjeta-tabla-encabezado">
                        <div class="crece">
                            <p class="sobretitulo">A la venta</p>
                            <h2 class="titulo">Teléfonos del catálogo</h2>
                        </div>
                        <t:chip texto="${telefonos.size()} modelos" variante="azul"/>
                    </div>
                    <div class="tabla-contenedor">
                        <table class="tabla">
                            <caption class="visualmente-oculto">Teléfonos del catálogo</caption>
                            <thead>
                                <tr><th scope="col">Teléfono</th><th scope="col" class="numero">Precio</th><th scope="col" class="numero">Efectivo</th><th scope="col"><span class="visualmente-oculto">Acciones</span></th></tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${telefonos}" var="tel">
                                    <tr class="${tel.codTelefono == formulario.codTelefono ? 'fila-activa' : ''}">
                                        <td data-etiqueta="Teléfono"><span class="celda-pila"><span class="leyenda secundario"><c:out value="${tel.marca}"/></span><span class="cuerpo-grande-seminegrita"><c:out value="${tel.modelo}"/></span></span></td>
                                        <td data-etiqueta="Precio" class="numero cuerpo-grande-seminegrita">${m:importe(tel.precio)}</td>
                                        <td data-etiqueta="Efectivo" class="numero texto-cian">${m:importe(tel.precioEfectivo)}</td>
                                        <td class="acciones">
                                            <a class="boton-icono" href="${raiz}/telefonos?editar=${tel.codTelefono}" title="Editar" aria-label="Editar <c:out value='${tel.nombre}'/>"><t:icono nombre="EDITAR"/></a>
                                            <a class="boton-icono boton-quitar" href="${raiz}/telefonos?eliminar=${tel.codTelefono}" title="Dar de baja" aria-label="Dar de baja <c:out value='${tel.nombre}'/>"><t:icono nombre="ELIMINAR"/></a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>

<c:if test="${not empty porEliminar}">
    <t:confirmacion titulo="Dar de baja ${porEliminar.nombre}" filas="${filasEliminar}" textoConfirmar="Dar de baja" peligro="${true}"
                    texto="El teléfono dejará de aparecer en el punto de venta. Las facturas emitidas no cambian."
                    accion="${raiz}/telefonos/eliminar" cancelar="${raiz}/telefonos"
                    campoOculto="codTelefono" valorOculto="${porEliminar.codTelefono}"/>
</c:if>
</t:pagina>

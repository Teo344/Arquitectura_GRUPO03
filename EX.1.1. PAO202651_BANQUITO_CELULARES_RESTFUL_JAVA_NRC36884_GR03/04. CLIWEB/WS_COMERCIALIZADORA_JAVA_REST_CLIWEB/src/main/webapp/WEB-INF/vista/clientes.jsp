<%-- Crédito BanQuito: evaluación de un cliente (sujeto de crédito, monto máximo) y sus créditos. --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="m" uri="http://monster.edu.ec/monstercell" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="raiz" value="${pageContext.request.contextPath}"/>
<t:pagina titulo="Crédito BanQuito" seccion="clientes">
<t:barraSuperior titulo="Crédito BanQuito" subtitulo="Evaluación y créditos de un cliente"/>

<main class="contenido dos-columnas columnas-40">
    <form class="tarjeta pila" method="get" action="${raiz}/clientes" novalidate>
        <div class="encabezado">
            <span class="icono-caja icono-caja-44 icono-caja-cian"><t:icono nombre="CREDITO" tamano="22"/></span>
            <div class="crece">
                <p class="sobretitulo">Consulta</p>
                <h2 class="titulo">Buscar un cliente</h2>
            </div>
        </div>
        <div class="espacio-m">
            <t:campo nombre="cedula" etiqueta="Cédula" marcador="1712345678" valor="${cedula}" modoEntrada="numeric"
                     ayuda="10 dígitos. El banco aplica las 4 reglas del crédito directo." enfocar="${true}"/>
        </div>
        <button type="submit" class="boton boton-primario espacio-l" data-cargando>
            <t:icono nombre="BUSCAR" tamano="18"/><span class="boton-texto-contenido">Consultar en BanQuito</span>
        </button>
        <div class="espacio-m"><t:alerta texto="${mensaje}" error="${mensajeError}"/></div>
        <a class="enlace leyenda espacio-m" href="${raiz}/simulador">Simular una tabla de cuotas</a>
    </form>

    <section class="resultado pila">
        <c:choose>
            <c:when test="${empty evaluacion}">
                <t:estadoVacio icono="PERSONA" titulo="Sin consulta"
                               texto="Escribe la cédula de un cliente para ver si es sujeto de crédito, su monto máximo y sus créditos."/>
            </c:when>
            <c:otherwise>
                <div class="tarjeta"><t:tarjetaEvaluacion evaluacion="${evaluacion}"/></div>
                <c:if test="${evaluacion.cliente}">
                    <div class="tarjeta espacio-m">
                        <div class="encabezado">
                            <div class="crece">
                                <p class="sobretitulo">Historial</p>
                                <h2 class="titulo">Créditos directos</h2>
                            </div>
                            <t:chip texto="${creditos.size()} ${creditos.size() == 1 ? 'crédito' : 'créditos'}" variante="azul"/>
                        </div>
                        <c:choose>
                            <c:when test="${empty creditos}">
                                <p class="cuerpo secundario espacio-m">El cliente no tiene créditos con BanQuito.</p>
                            </c:when>
                            <c:otherwise>
                                <ul class="lista-creditos">
                                    <c:forEach items="${creditos}" var="cr">
                                        <li>
                                            <a class="credito-item" href="${raiz}/creditos/${cr.codCredito}?cedula=${evaluacion.cedula}">
                                                <span class="icono-caja icono-caja-44 icono-caja-cian"><t:icono nombre="CREDITO" tamano="22"/></span>
                                                <span class="crece">
                                                    <span class="cuerpo-grande-seminegrita">Crédito N.º ${cr.codCredito} · ${m:importe(cr.monto)}</span><br>
                                                    <span class="leyenda secundario">${m:fecha(cr.fecha)} · ${cr.numCuotas} cuotas de ${m:importe(cr.valorCuota)}</span>
                                                </span>
                                                <t:chipEstado estado="${cr.estado}"/>
                                            </a>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:if>
            </c:otherwise>
        </c:choose>
    </section>
</main>
</t:pagina>

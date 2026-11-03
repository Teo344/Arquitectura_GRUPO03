<%-- Vista de acceso (RQF-001), igual a LoginVista: foto con la marca a la izquierda y el formulario a la derecha. --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="raiz" value="${pageContext.request.contextPath}"/>
<t:pagina titulo="Iniciar sesión">
<main class="login">
    <section class="login-marca" style="background-image: url('${raiz}/img/fotoLogin.jpg')">
        <div class="login-marca-logo">
            <span class="icono-caja icono-caja-36 icono-caja-accion"><t:icono nombre="BANCO"/></span>
            <span class="sobretitulo texto-blanco">EurekaBank</span>
        </div>
        <div class="login-marca-textos">
            <h1 class="titular-mediano texto-blanco">Banca de ventanilla,<br>simple y segura</h1>
            <p class="cuerpo texto-borde-suave">Consulta cuentas y registra depósitos, retiros y transferencias en tiempo real.</p>
            <div class="fila-chips">
                <t:chip texto="SOAP" variante="cian-oscuro"/>
                <t:chip texto="Java" variante="azul"/>
                <t:chip texto="Grupo 03" variante="azul"/>
            </div>
        </div>
    </section>

    <section class="login-panel">
        <div class="login-contenido">
            <form class="tarjeta pila" method="post" action="${raiz}/login" novalidate>
                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                <p class="sobretitulo">Acceso de empleados</p>
                <h2 class="titular-pequeno espacio-xs">Iniciar sesión</h2>
                <p class="cuerpo secundario">Ingresa con tu usuario del sistema EurekaBank.</p>
                <div class="espacio-l">
                    <t:campo nombre="usuario" etiqueta="Usuario" marcador="Ej. MONSTER" valor="${usuario}"
                             autocompletar="username" enfocar="${empty usuario}"/>
                </div>
                <div class="espacio-m">
                    <t:campo nombre="clave" etiqueta="Contraseña" clave="${true}" autocompletar="current-password"
                             enfocar="${not empty usuario}"/>
                </div>
                <button type="submit" class="boton boton-primario espacio-l" data-cargando>
                    <span class="boton-texto-contenido">Ingresar</span>
                </button>
                <div class="espacio-m"><t:alerta texto="${mensaje}" error="${mensajeError}"/></div>
            </form>
            <p class="leyenda secundario centrado espacio-m">Cliente web · EurekaBank SOAP · Grupo 03</p>
        </div>
    </section>
</main>
</t:pagina>

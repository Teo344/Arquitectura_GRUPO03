<%-- Pantalla de autenticación con identidad visual Azul (LoginView). --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Conversión de Unidades Monster | Iniciar sesión</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/monster.css">
</head>
<body>
<main class="login">
    <div class="login-tarjeta">
        <section class="login-visual">
            <div class="figura figura-circulo-grande"></div>
            <div class="figura figura-circulo-pequeno"></div>
            <div class="login-visual-marca">MONSTER</div>
            <div class="login-visual-pie">
                <h2>Convierte unidades</h2>
                <p>de forma sencilla y segura</p>
            </div>
        </section>

        <section class="login-formulario">
            <form class="login-contenido" method="post" action="${pageContext.request.contextPath}/login" id="formLogin">
                <p class="login-sobretitulo">ACCESO SEGURO</p>
                <h1 class="login-titulo">Sistema de conversión</h1>
                <h1 class="login-titulo">de unidades</h1>
                <p class="login-grupo">GRUPO #3</p>
                <p class="login-protocolo">Conexión mediante REST</p>

                <div class="bienvenida">
                    <strong>¡Bienvenido!</strong>
                    <span>Inicia sesión para convertir tus unidades</span>
                </div>

                <div class="campo-login">
                    <label for="usuario">Usuario</label>
                    <input id="usuario" name="usuario" value="<c:out value='${usuario}'/>" autocomplete="username" autofocus>
                </div>
                <div class="campo-login">
                    <label for="clave">Contraseña</label>
                    <input id="clave" name="clave" type="password" autocomplete="current-password">
                </div>

                <button type="submit" class="boton-primario" id="botonIngresar">Iniciar sesión</button>

                <p class="login-pie">Cliente web Java listo para conversión.</p>
                <c:choose>
                    <c:when test="${not empty estado}">
                        <p class="mensaje${estadoError ? ' error' : ' exito'}" id="mensaje"><c:out value="${estado}"/></p>
                    </c:when>
                    <c:otherwise>
                        <p class="mensaje" id="mensaje">Conéctate al servicio de conversiones</p>
                    </c:otherwise>
                </c:choose>
            </form>
        </section>
    </div>
</main>
<script>
    // Igual que LoginController: deshabilita el botón mientras responde el servidor.
    document.getElementById('formLogin').addEventListener('submit', function () {
        var mensaje = document.getElementById('mensaje');
        document.getElementById('botonIngresar').disabled = true;
        mensaje.className = 'mensaje exito';
        mensaje.textContent = 'Conectando con el servidor…';
    });
</script>
</body>
</html>

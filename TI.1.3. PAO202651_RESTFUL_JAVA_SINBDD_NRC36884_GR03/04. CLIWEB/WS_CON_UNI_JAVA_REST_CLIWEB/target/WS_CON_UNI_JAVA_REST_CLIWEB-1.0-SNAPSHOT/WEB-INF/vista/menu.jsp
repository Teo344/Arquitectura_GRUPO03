<%-- Menú principal del cliente con la identidad visual Azul (MenuView). --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Conversión de Unidades Monster | Menú Principal</title>
    <link rel="stylesheet" href="${raiz}/css/monster.css">
</head>
<body>
<div class="pagina">
    <header class="barra barra-menu">
        <span class="barra-titulo">MONSTER</span>
        <div class="barra-acciones">
            <span class="barra-tipo-cliente">Cliente web</span>
            <a class="boton-cerrar-sesion" href="${raiz}/salir">Cerrar sesión</a>
        </div>
    </header>

    <main class="menu-area">
        <div class="menu-encabezado">
            <p class="menu-sobre">MONSTER</p>
            <h1 class="menu-titulo">¿Qué deseas hacer?</h1>
            <p class="menu-detalle">Elige una opción para continuar</p>
        </div>

        <div class="menu-tarjetas">
            <section class="menu-tarjeta">
                <div class="menu-tarjeta-cima">
                    <svg width="74" height="74" viewBox="0 0 74 74" aria-hidden="true">
                        <rect x="2" y="2" width="70" height="70" rx="8" fill="#EDF4FF"/>
                        <g stroke="#12A9C9" stroke-width="3.5" stroke-linecap="round" stroke-linejoin="round" fill="none">
                            <path d="M19 29H55M47 21L55 29M47 37L55 29M55 47H19M27 39L19 47M27 55L19 47"/>
                        </g>
                    </svg>
                    <span class="menu-insignia">01</span>
                </div>
                <div class="menu-tarjeta-textos">
                    <h2>Convertir unidades</h2>
                    <p>Realiza conversiones de longitud, masa y temperatura.</p>
                </div>
                <div><a class="boton-primario" href="${raiz}/conversion">Abrir conversor</a></div>
            </section>

            <section class="menu-tarjeta">
                <div class="menu-tarjeta-cima">
                    <svg width="74" height="74" viewBox="0 0 74 74" aria-hidden="true">
                        <rect x="2" y="2" width="70" height="70" rx="8" fill="#EDF4FF"/>
                        <g fill="#12A9C9">
                            <circle cx="19.5" cy="24.5" r="2.5"/>
                            <circle cx="19.5" cy="37.5" r="2.5"/>
                            <circle cx="19.5" cy="50.5" r="2.5"/>
                        </g>
                        <path d="M30 24H57M30 37H57M30 50H57" stroke="#12A9C9" stroke-width="3.5" stroke-linecap="round"/>
                    </svg>
                    <span class="menu-insignia">02</span>
                </div>
                <div class="menu-tarjeta-textos">
                    <h2>Consultar catálogo</h2>
                    <p>Explora las categorías y unidades disponibles en el sistema.</p>
                </div>
                <div><a class="boton-secundario" href="${raiz}/catalogo">Ver catálogo</a></div>
            </section>
        </div>

        <p class="menu-pie">Conversión de Unidades Monster</p>
    </main>
</div>
</body>
</html>

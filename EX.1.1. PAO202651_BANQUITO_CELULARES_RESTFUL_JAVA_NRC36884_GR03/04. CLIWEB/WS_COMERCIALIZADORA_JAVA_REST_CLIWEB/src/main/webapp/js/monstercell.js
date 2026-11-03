/*
 * Comportamiento de las pantallas de MonsterCell (el mismo de los clientes web
 * de EurekaBank, más el punto de venta). Todo funciona también sin JavaScript:
 * esto solo mejora la interacción.
 */
(function () {
    'use strict';

    // Boton.setCargando(true): mientras responde el servidor el botón muestra el
    // indicador y se deshabilita, así no se envía dos veces la misma operación.
    document.querySelectorAll('form').forEach(function (formulario) {
        formulario.addEventListener('submit', function () {
            var boton = formulario.querySelector('[data-cargando]');
            if (boton) {
                boton.classList.add('cargando');
                setTimeout(function () { boton.disabled = true; }, 0);
            }
        });
    });

    // CampoTexto con clave: botón para mostrar u ocultar la contraseña.
    document.querySelectorAll('[data-ver-clave]').forEach(function (boton) {
        boton.addEventListener('click', function () {
            var campo = document.getElementById(boton.getAttribute('data-ver-clave'));
            var visible = campo.type === 'password';
            campo.type = visible ? 'text' : 'password';
            boton.classList.toggle('visible', visible);
            var texto = visible ? 'Ocultar contraseña' : 'Mostrar contraseña';
            boton.title = texto;
            boton.setAttribute('aria-label', texto);
            campo.focus();
        });
    });

    // alConfirmar del escritorio: Enter en un campo pasa al siguiente; en el
    // último campo envía el formulario.
    document.querySelectorAll('form').forEach(function (formulario) {
        var campos = Array.prototype.slice.call(formulario.querySelectorAll('input[type=text], input[type=password]'));
        campos.forEach(function (campo, indice) {
            campo.addEventListener('keydown', function (evento) {
                if (evento.key === 'Enter' && indice < campos.length - 1) {
                    evento.preventDefault();
                    campos[indice + 1].focus();
                }
            });
        });
    });

    // Componentes.confirmar: el diálogo llega abierto desde el servidor; aquí se
    // vuelve modal (velo azul oscuro, el resto de la página inactivo) y Esc cancela.
    var dialogo = document.querySelector('dialog[data-modal]');
    if (dialogo && typeof dialogo.showModal === 'function') {
        dialogo.close();
        dialogo.setAttribute('data-abierto', '');
        dialogo.showModal();
        dialogo.addEventListener('cancel', function (evento) {
            evento.preventDefault();
            window.location.href = dialogo.getAttribute('data-cancelar');
        });
        var registrar = dialogo.querySelector('button[type=submit]');
        if (registrar) {
            registrar.focus();
        }
    }

    // Punto de venta: el buscador filtra las tarjetas del catálogo mientras se
    // escribe (sin recargar); Enter sigue haciendo la búsqueda en el servidor.
    var filtro = document.querySelector('[data-filtro-catalogo]');
    if (filtro) {
        var tarjetas = Array.prototype.slice.call(document.querySelectorAll('[data-buscar]'));
        var contador = document.querySelector('[data-contador-catalogo]');
        var sinResultados = document.querySelector('[data-sin-resultados]');
        filtro.addEventListener('input', function () {
            var texto = filtro.value.trim().toLowerCase();
            var visibles = 0;
            tarjetas.forEach(function (tarjeta) {
                var coincide = texto === '' || tarjeta.getAttribute('data-buscar').indexOf(texto) >= 0;
                tarjeta.hidden = !coincide;
                if (coincide) {
                    visibles++;
                }
            });
            if (contador) {
                contador.textContent = visibles + (visibles === 1 ? ' modelo' : ' modelos');
            }
            if (sinResultados) {
                sinResultados.hidden = visibles > 0;
            }
        });
    }

    // Las listas desplegables (número de cuotas) aplican el cambio al elegir.
    document.querySelectorAll('form[data-enviar-al-cambiar] select').forEach(function (lista) {
        lista.addEventListener('change', function () {
            lista.form.submit();
        });
    });

    // Barra lateral en pantallas angostas: el botón de menú la abre como panel;
    // el velo, la X y Esc la cierran, y el foco vuelve al botón de menú.
    var lateral = document.getElementById('lateral');
    var abrirLateral = document.querySelector('[data-abrir-lateral]');
    var veloLateral = document.querySelector('.lateral-velo');
    if (lateral && abrirLateral) {
        var cambiarLateral = function (abierta) {
            document.body.classList.toggle('lateral-abierta', abierta);
            abrirLateral.setAttribute('aria-expanded', abierta ? 'true' : 'false');
            if (veloLateral) {
                veloLateral.hidden = !abierta;
            }
            if (abierta) {
                var primerEnlace = lateral.querySelector('.lateral-enlace');
                if (primerEnlace) {
                    primerEnlace.focus();
                }
            } else {
                abrirLateral.focus();
            }
        };
        abrirLateral.addEventListener('click', function () {
            cambiarLateral(true);
        });
        document.querySelectorAll('[data-cerrar-lateral]').forEach(function (cerrar) {
            cerrar.addEventListener('click', function () {
                cambiarLateral(false);
            });
        });
        document.addEventListener('keydown', function (evento) {
            if (evento.key === 'Escape' && document.body.classList.contains('lateral-abierta')) {
                cambiarLateral(false);
            }
        });
    }

    // Factura: imprimir (la hoja de estilos oculta la barra y los botones).
    document.querySelectorAll('[data-imprimir]').forEach(function (boton) {
        boton.addEventListener('click', function () {
            window.print();
        });
    });
})();

/*
 * Comportamiento de las pantallas que en el escritorio hacen los componentes
 * Swing. Todo funciona también sin JavaScript: esto solo mejora la interacción.
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
})();

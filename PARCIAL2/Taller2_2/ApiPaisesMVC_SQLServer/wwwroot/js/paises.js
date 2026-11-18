// Comportamiento mínimo de la vista (sin librerías): confirmaciones con <dialog>, indicador de carga
// en los botones y aviso de cambios sin guardar. Toda la lógica de negocio está en el servidor.
(function () {
    "use strict";

    var pendiente = null;

    function abrir(dialogo, accion) {
        pendiente = accion;
        dialogo.showModal();
        // El foco empieza en "Cancelar": confirmar una acción destructiva exige un paso más
        var cancelar = dialogo.querySelector("[data-cerrar]");
        if (cancelar) {
            cancelar.focus();
        }
    }

    function mostrarCargando(formulario) {
        var boton = formulario.querySelector("button[type=submit][data-cargando]");
        if (!boton) {
            return;
        }
        boton.disabled = true;
        boton.setAttribute("aria-busy", "true");
        boton.innerHTML = '<span class="ap-indicador" aria-hidden="true"></span><span></span>';
        boton.lastChild.textContent = boton.getAttribute("data-cargando");
    }

    document.querySelectorAll("dialog.ap-dialogo").forEach(function (dialogo) {
        dialogo.querySelectorAll("[data-cerrar]").forEach(function (boton) {
            boton.addEventListener("click", function () {
                pendiente = null;
                dialogo.close();
            });
        });
        var aceptar = dialogo.querySelector("[data-aceptar]");
        if (aceptar) {
            aceptar.addEventListener("click", function () {
                var accion = pendiente;
                pendiente = null;
                dialogo.close();
                if (accion) {
                    accion();
                }
            });
        }
    });

    // Formularios que piden confirmación (importar, eliminar)
    document.querySelectorAll("form[data-confirmar]").forEach(function (formulario) {
        formulario.addEventListener("submit", function (evento) {
            var selector = formulario.getAttribute("data-confirmar");
            if (formulario.dataset.confirmado === "si" || !selector) {
                return;
            }
            var dialogo = document.querySelector(selector);
            if (!dialogo) {
                return;
            }
            evento.preventDefault();
            abrir(dialogo, function () {
                formulario.dataset.confirmado = "si";
                mostrarCargando(formulario);
                formulario.submit();
            });
        });
    });

    // Indicador de carga en los demás formularios
    document.querySelectorAll("form:not([data-confirmar])").forEach(function (formulario) {
        formulario.addEventListener("submit", function () {
            mostrarCargando(formulario);
        });
    });

    // Aviso al salir del formulario de país con cambios sin guardar: compara los valores actuales con
    // los que tenía al cargar la página (si volvió con errores, lo escrito aún no se guardó)
    var formularioPais = document.querySelector("form[data-vigilar-cambios]");
    var dialogoDescartar = document.getElementById("dialogo-descartar");
    if (formularioPais && dialogoDescartar) {
        var serializar = function () {
            return new URLSearchParams(new FormData(formularioPais)).toString();
        };
        var valoresIniciales = serializar();
        var conErrores = formularioPais.getAttribute("data-con-errores") === "si";
        var enviando = false;
        formularioPais.addEventListener("submit", function () {
            enviando = true;
        });
        document.querySelectorAll("a[data-proteger]").forEach(function (enlace) {
            enlace.addEventListener("click", function (evento) {
                var hayCambios = !enviando && (conErrores || serializar() !== valoresIniciales);
                if (!hayCambios) {
                    return;
                }
                evento.preventDefault();
                abrir(dialogoDescartar, function () {
                    window.location.href = enlace.href;
                });
            });
        });
    }
})();

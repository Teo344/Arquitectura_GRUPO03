// Comportamiento mínimo de la vista (sin librerías): confirmaciones con <dialog>, indicador de carga
// en los botones, aviso de cambios sin guardar y el panel lateral (Esc cierra, el foco no sale de
// él). Toda la lógica de negocio está en el servidor; sin JavaScript las pantallas siguen funcionando.
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

    function mostrarCargando(formulario, boton) {
        boton = boton || formulario.querySelector("button[type=submit][data-cargando]")
            || (formulario.id && document.querySelector("button[form='" + formulario.id + "'][data-cargando]"));
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

    // Formularios que piden confirmación (importar, eliminar). Los atributos data-detalle-* del
    // formulario completan el resumen del diálogo (p. ej. el código y el nombre del país de la fila)
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
            dialogo.querySelectorAll("[data-detalle]").forEach(function (celda) {
                var clave = celda.getAttribute("data-detalle");
                celda.textContent = formulario.getAttribute("data-detalle-" + clave) || "—";
            });
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

    // Panel lateral: el contenido de atrás no se desplaza, el foco empieza en el primer campo, Tab no
    // sale del panel y Esc lo cierra (pasando por el aviso de cambios sin guardar)
    var panel = document.querySelector("[data-panel]");
    if (panel) {
        document.body.classList.add("ap-con-panel");
        var enfocables = function () {
            return Array.prototype.filter.call(
                panel.querySelectorAll("a[href], button:not([disabled]), input:not([type=hidden]), select, textarea"),
                function (elemento) { return elemento.offsetParent !== null; });
        };
        var primerCampo = panel.querySelector(".ap-panel__cuerpo input:not([type=hidden]), .ap-panel__cuerpo select");
        if (primerCampo) {
            primerCampo.focus();
        }
        document.addEventListener("keydown", function (evento) {
            if (document.querySelector("dialog[open]")) {
                return;
            }
            if (evento.key === "Escape") {
                var cerrar = panel.querySelector(".ap-panel__cerrar");
                if (cerrar) {
                    evento.preventDefault();
                    cerrar.click();
                }
                return;
            }
            if (evento.key !== "Tab") {
                return;
            }
            var lista = enfocables();
            if (lista.length === 0) {
                return;
            }
            var primero = lista[0];
            var ultimo = lista[lista.length - 1];
            if (!panel.contains(document.activeElement)) {
                evento.preventDefault();
                primero.focus();
            } else if (evento.shiftKey && document.activeElement === primero) {
                evento.preventDefault();
                ultimo.focus();
            } else if (!evento.shiftKey && document.activeElement === ultimo) {
                evento.preventDefault();
                primero.focus();
            }
        });
    }
})();

<%-- Conversor de unidades con formulario e historial de la sesión actual (ConversionView). --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="raiz" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Conversión de Unidades Monster | Conversor</title>
    <link rel="stylesheet" href="${raiz}/css/monster.css">
</head>
<body>
<div class="pagina pagina-ajustada">
    <header class="barra">
        <div>
            <div class="barra-titulo">MONSTER</div>
            <div class="barra-subtitulo">Conversor de unidades · longitud, masa y temperatura</div>
        </div>
        <a class="boton-secundario" href="${raiz}/menu">Volver al inicio</a>
    </header>

    <main class="conversion-contenido">
        <section class="tarjeta">
            <h2 class="tarjeta-titulo">Conversor de unidades</h2>
            <p class="tarjeta-detalle">Realiza una conversión con las opciones del servidor.</p>

            <form class="conversion-campos" method="post" action="${raiz}/conversion" id="formConversion">
                <div class="conversion-campo">
                    <label for="categoria">Tipo de conversión</label>
                    <%-- Cambiar de categoría pide al controlador las unidades de esa categoría --%>
                    <select id="categoria" name="categoria"
                            onchange="window.location = '${raiz}/conversion?categoria=' + encodeURIComponent(this.value)">
                        <c:forEach var="cat" items="${categorias}">
                            <option value="${cat.codigo}" ${cat.codigo == categoria.codigo ? 'selected' : ''}><c:out value="${cat.nombre}"/></option>
                        </c:forEach>
                    </select>
                </div>

                <div class="conversion-campo">
                    <label for="valor">Valor</label>
                    <input id="valor" name="valor" value="<c:out value='${valor}'/>"
                           title="Escribe el valor numérico que deseas convertir">
                </div>

                <div class="conversion-fila-doble">
                    <div class="conversion-campo">
                        <label for="origen">Unidad inicial</label>
                        <select id="origen" name="origen">
                            <c:forEach var="u" items="${unidades}">
                                <option value="${u.codigo}" ${u.codigo == origen.codigo ? 'selected' : ''}><c:out value="${u.nombre} (${u.simbolo})"/></option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="conversion-campo">
                        <label for="destino">Unidad final</label>
                        <select id="destino" name="destino">
                            <c:forEach var="u" items="${unidades}">
                                <option value="${u.codigo}" ${u.codigo == destino.codigo ? 'selected' : ''}><c:out value="${u.nombre} (${u.simbolo})"/></option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <div class="conversion-botones">
                    <button type="submit" class="boton-primario" id="botonConvertir">Convertir</button>
                    <button type="button" class="boton-secundario" id="botonLimpiar">Limpiar</button>
                </div>
            </form>

            <p class="estado${estadoError ? ' error' : ''}" id="estado"><c:out value="${estado}"/></p>
        </section>

        <section class="tarjeta">
            <h2 class="tarjeta-titulo">Historial de conversiones</h2>
            <p class="tarjeta-detalle">Resultados obtenidos en esta sesión.</p>
            <ul class="historial" id="historial">
                <c:forEach var="registro" items="${historial}">
                    <li class="historial-item">
                        <span class="historial-fecha"><c:out value="${registro.fecha}"/></span>
                        <span><c:out value="${registro.detalle}"/></span>
                    </li>
                </c:forEach>
            </ul>
            <p class="nota">El historial se conserva mientras esta ventana esté abierta.</p>
        </section>
    </main>
</div>
<script>
    var estado = document.getElementById('estado');
    function mostrarEstado(texto, error) {
        estado.textContent = texto;
        estado.className = error ? 'estado error' : 'estado';
    }
    // Equivalente a view.procesando(true) del ConversionController de escritorio.
    document.getElementById('formConversion').addEventListener('submit', function () {
        document.getElementById('botonConvertir').disabled = true;
        mostrarEstado('Enviando conversión al servicio…', false);
    });
    // Igual que el botón Limpiar del escritorio REST: solo reinicia el valor.
    document.getElementById('botonLimpiar').addEventListener('click', function () {
        document.getElementById('valor').value = '0';
        mostrarEstado('Listo para convertir.', false);
    });
    // Selección simple de la lista, como el JList del historial.
    document.getElementById('historial').addEventListener('click', function (evento) {
        var item = evento.target.closest('.historial-item');
        if (!item) return;
        this.querySelectorAll('.seleccionado').forEach(function (e) { e.classList.remove('seleccionado'); });
        item.classList.add('seleccionado');
    });
</script>
</body>
</html>

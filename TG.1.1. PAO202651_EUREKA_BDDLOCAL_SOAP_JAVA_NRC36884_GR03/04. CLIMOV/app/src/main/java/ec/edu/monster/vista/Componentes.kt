package ec.edu.monster.vista

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ec.edu.monster.R
import ec.edu.monster.modelo.MovimientoModelo
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// Componentes comunes de las pantallas, construidos solo con los valores de Tema.kt.

// ================================================================ Responsive

/** Entrega la clase de ancho del espacio disponible (compacto, medio o amplio). */
@Composable
fun ConAncho(modifier: Modifier = Modifier, contenido: @Composable (ClaseAncho) -> Unit) {
    BoxWithConstraints(modifier) {
        contenido(Ancho.clase(maxWidth))
    }
}

/** Centra el contenido y limita su ancho en pantallas grandes (tablet, escritorio). */
@Composable
fun ContenidoCentrado(
    maxAncho: Dp,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier.widthIn(max = maxAncho).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Espacio.m),
            content = contenido
        )
    }
}

// ================================================================ Estructura

/** Barra de navegación superior (#0B1D44 con texto blanco y #B8D2FF), con una acción opcional a la derecha. */
@Composable
fun BarraSuperior(
    titulo: String,
    subtitulo: String,
    alVolver: (() -> Unit)? = null,
    accion: (@Composable () -> Unit)? = null
) {
    Surface(color = AzulOscuro) {
        ContenidoCentrado(Ancho.CONTENIDO) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Espacio.m, vertical = Espacio.s),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (alVolver != null) {
                    IconButton(onClick = alVolver) {
                        Icon(painterResource(R.drawable.ic_volver), contentDescription = "Volver", tint = Blanco)
                    }
                    Spacer(Modifier.width(Espacio.xxs))
                }
                Column(Modifier.weight(1f)) {
                    Text(titulo, color = Blanco, style = MaterialTheme.typography.titleLarge,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(subtitulo, color = BordeSuave, style = MaterialTheme.typography.bodySmall,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (accion != null) {
                    Spacer(Modifier.width(Espacio.s))
                    accion()
                }
            }
        }
    }
}

@Composable
fun Sobretitulo(texto: String, modifier: Modifier = Modifier, color: Color = AzulAccion) {
    Text(texto.uppercase(), modifier = modifier, color = color, style = MaterialTheme.typography.labelMedium)
}

/** Título de sección: sobretítulo y título H4. */
@Composable
fun TituloSeccion(sobretitulo: String, titulo: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Sobretitulo(sobretitulo)
        Spacer(Modifier.height(Espacio.xxs))
        Text(titulo, color = Texto, style = MaterialTheme.typography.titleLarge)
    }
}

/** Tarjeta: fondo blanco, borde #D3DCEA, radio 16, sombra md (tinte #12306A) y padding 24. */
@Composable
fun Tarjeta(modifier: Modifier = Modifier, contenido: @Composable ColumnScope.() -> Unit) {
    val forma = RoundedCornerShape(16.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, forma, ambientColor = TinteSombra, spotColor = TinteSombra.copy(alpha = 0.35f)),
        shape = forma,
        color = Blanco,
        border = BorderStroke(1.dp, Divisor)
    ) {
        Column(Modifier.padding(Espacio.l), content = contenido)
    }
}

// ================================================================ Botones

/** Botón primario: fondo #1D56CC, texto blanco, 48 dp de alto, radio 10. Uno por vista. */
@Composable
fun BotonPrimario(
    texto: String,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier,
    cargando: Boolean = false,
    icono: Int? = null
) {
    Button(
        onClick = alPulsar,
        enabled = !cargando,
        modifier = modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AzulAccion,
            contentColor = Blanco,
            disabledContainerColor = FondoSeccion,
            disabledContentColor = DeshabilitadoTexto
        )
    ) {
        if (cargando) {
            CircularProgressIndicator(Modifier.size(18.dp), color = DeshabilitadoTexto, strokeWidth = 2.dp)
            Spacer(Modifier.width(Espacio.xs))
        } else if (icono != null) {
            Icon(painterResource(icono), contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Espacio.xs))
        }
        Text(texto, style = MaterialTheme.typography.labelLarge)
    }
}

/** Botón secundario: fondo #EDF4FF, texto #1744A3, borde #B8D2FF. */
@Composable
fun BotonSecundario(texto: String, alPulsar: () -> Unit, modifier: Modifier = Modifier, habilitado: Boolean = true) {
    Button(
        onClick = alPulsar,
        enabled = habilitado,
        modifier = modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, BordeSuave),
        colors = ButtonDefaults.buttonColors(
            containerColor = AzulSuave,
            contentColor = AzulHover,
            disabledContainerColor = FondoSeccion,
            disabledContentColor = DeshabilitadoTexto
        )
    ) {
        Text(texto, style = MaterialTheme.typography.labelLarge)
    }
}

/** Botón de peligro: fondo #C0392B y texto blanco, compacto para la barra superior. */
@Composable
fun BotonPeligro(
    texto: String,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier,
    icono: Int? = null,
    habilitado: Boolean = true
) {
    Button(
        onClick = alPulsar,
        enabled = habilitado,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = Espacio.s),
        colors = ButtonDefaults.buttonColors(
            containerColor = ErrorRelleno,
            contentColor = Blanco,
            disabledContainerColor = FondoSeccion,
            disabledContentColor = DeshabilitadoTexto
        )
    ) {
        if (icono != null) {
            Icon(painterResource(icono), contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(texto, style = MaterialTheme.typography.labelLarge)
    }
}

// ================================================================ Formularios

/**
 * Campo con etiqueta visible encima y ayuda debajo (48 dp, borde 1.5 #B1BFD4,
 * foco #1D56CC). Las claves se pueden mostrar u ocultar.
 */
@Composable
fun CampoTexto(
    etiqueta: String,
    valor: String,
    alCambiar: (String) -> Unit,
    modifier: Modifier = Modifier,
    ayuda: String? = null,
    marcador: String? = null,
    esClave: Boolean = false,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    accionTeclado: ImeAction = ImeAction.Next,
    alConfirmar: () -> Unit = {},
    habilitado: Boolean = true
) {
    var claveVisible by remember { mutableStateOf(false) }
    Column(modifier.fillMaxWidth()) {
        Text(etiqueta, color = Texto, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = alCambiar,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            enabled = habilitado,
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            placeholder = marcador?.let { { Text(it, color = Placeholder, style = MaterialTheme.typography.bodyLarge) } },
            visualTransformation = if (esClave && !claveVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (esClave) {
                {
                    IconButton(onClick = { claveVisible = !claveVisible }) {
                        Icon(
                            painterResource(if (claveVisible) R.drawable.ic_ocultar else R.drawable.ic_ver),
                            contentDescription = if (claveVisible) "Ocultar contraseña" else "Mostrar contraseña",
                            tint = Placeholder
                        )
                    }
                }
            } else null,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (esClave) KeyboardType.Password else tipoTeclado,
                imeAction = accionTeclado
            ),
            keyboardActions = KeyboardActions(onDone = { alConfirmar() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AzulAccion,
                unfocusedBorderColor = BordeCampo,
                errorBorderColor = ErrorRelleno,
                focusedContainerColor = Blanco,
                unfocusedContainerColor = Blanco,
                disabledContainerColor = FondoSeccion,
                disabledBorderColor = Divisor,
                cursorColor = AzulAccion,
                focusedTextColor = Texto,
                unfocusedTextColor = Texto,
                disabledTextColor = DeshabilitadoTexto
            )
        )
        if (ayuda != null) {
            Spacer(Modifier.height(Espacio.xxs))
            Text(ayuda, color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/**
 * Selector de opciones excluyentes (pestañas en píldora): la opción elegida se
 * marca con fondo blanco, borde y texto en negrita, no solo con color.
 */
@Composable
fun <T> SelectorSegmentado(
    opciones: List<T>,
    seleccionada: T,
    alSeleccionar: (T) -> Unit,
    texto: (T) -> String,
    modifier: Modifier = Modifier,
    icono: ((T) -> Int)? = null,
    habilitado: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(FondoSeccion, RoundedCornerShape(10.dp))
            .padding(Espacio.xxs),
        horizontalArrangement = Arrangement.spacedBy(Espacio.xxs)
    ) {
        opciones.forEach { opcion ->
            val elegida = opcion == seleccionada
            Surface(
                selected = elegida,
                onClick = { alSeleccionar(opcion) },
                enabled = habilitado,
                modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                shape = RoundedCornerShape(8.dp),
                color = if (elegida) Blanco else Color.Transparent,
                border = if (elegida) BorderStroke(1.dp, BordeSuave) else null
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Espacio.xs, vertical = Espacio.s),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (icono != null) {
                        Icon(
                            painterResource(icono(opcion)), contentDescription = null,
                            tint = if (elegida) AzulAccion else Placeholder, modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        texto(opcion),
                        color = if (elegida) AzulHover else TextoSecundario,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (elegida) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ================================================================ Mensajes y estados

/** Alerta con icono y texto: el estado nunca se comunica solo con color. */
@Composable
fun Alerta(mensaje: String, esError: Boolean, modifier: Modifier = Modifier) {
    val fondo = if (esError) ErrorFondo else InfoFondo
    val relleno = if (esError) ErrorRelleno else InfoRelleno
    val colorTexto = if (esError) ErrorTexto else InfoTexto
    val icono = if (esError) R.drawable.ic_error else R.drawable.ic_info
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(fondo, RoundedCornerShape(10.dp))
            .border(1.dp, relleno, RoundedCornerShape(10.dp))
            .padding(Espacio.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(icono), contentDescription = if (esError) "Error" else "Información",
            tint = relleno, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(Espacio.xs))
        Text(mensaje, color = colorTexto, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Etiqueta pequeña (chip) con radio 6. */
@Composable
fun Chip(texto: String, fondo: Color, colorTexto: Color, icono: Int? = null, colorIcono: Color = colorTexto) {
    Row(
        modifier = Modifier
            .background(fondo, RoundedCornerShape(6.dp))
            .padding(horizontal = Espacio.xs, vertical = Espacio.xxs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icono != null) {
            Icon(painterResource(icono), contentDescription = null, tint = colorIcono, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(Espacio.xxs))
        }
        Text(texto, color = colorTexto, style = MaterialTheme.typography.bodySmall)
    }
}

/** Estado de una cuenta: ACTIVO en verde con check; cualquier otro en ámbar con advertencia. */
@Composable
fun ChipEstado(estado: String?) {
    val activo = estado == "ACTIVO"
    Chip(
        texto = estado ?: "SIN ESTADO",
        fondo = if (activo) ExitoFondo else AdvertenciaFondo,
        colorTexto = if (activo) ExitoTexto else AdvertenciaTexto,
        icono = if (activo) R.drawable.ic_exito else R.drawable.ic_advertencia,
        colorIcono = if (activo) ExitoRelleno else AdvertenciaRelleno
    )
}

/** Icono dentro de un cuadro de color suave (radio 10). */
@Composable
fun IconoEnCaja(icono: Int, fondo: Color, color: Color, tamano: Dp = 44.dp) {
    Box(
        modifier = Modifier.size(tamano).background(fondo, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(icono), contentDescription = null, tint = color, modifier = Modifier.size(tamano / 2))
    }
}

/** Mensaje centrado para cuando todavía no hay datos que mostrar. */
@Composable
fun EstadoVacio(icono: Int, titulo: String, texto: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Divisor, RoundedCornerShape(16.dp))
            .background(Superficie, RoundedCornerShape(16.dp))
            .padding(Espacio.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconoEnCaja(icono, AzulSuave, AzulAccion, 56.dp)
        Spacer(Modifier.height(Espacio.m))
        Text(titulo, color = Texto, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(Espacio.xxs))
        Text(texto, color = TextoSecundario, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
    }
}

// ================================================================ Datos

/** Fila etiqueta / valor de una ficha. */
@Composable
fun FilaDato(etiqueta: String, valor: String, modifier: Modifier = Modifier, destacado: Boolean = false) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = Espacio.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(etiqueta, color = TextoSecundario, style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f))
        Spacer(Modifier.width(Espacio.m))
        Text(
            valor,
            color = Texto,
            style = if (destacado) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.SemiBold,
            textAlign = TextAlign.End
        )
    }
}

/** Tarjeta de acción del menú: icono, título y descripción; toda la tarjeta es pulsable. */
@Composable
fun TarjetaAccion(
    titulo: String,
    descripcion: String,
    icono: Int,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier,
    acento: Boolean = false,
    habilitado: Boolean = true
) {
    val forma = RoundedCornerShape(16.dp)
    Surface(
        onClick = alPulsar,
        enabled = habilitado,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 112.dp)
            .shadow(3.dp, forma, ambientColor = TinteSombra, spotColor = TinteSombra.copy(alpha = 0.25f)),
        shape = forma,
        color = Blanco,
        border = BorderStroke(1.dp, Divisor)
    ) {
        Row(Modifier.padding(Espacio.l), verticalAlignment = Alignment.CenterVertically) {
            IconoEnCaja(
                icono = icono,
                fondo = if (acento) CianSuave else AzulSuave,
                color = if (acento) CianTexto else AzulAccion,
                tamano = 48.dp
            )
            Spacer(Modifier.width(Espacio.m))
            Column(Modifier.weight(1f)) {
                Text(titulo, color = Texto, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(2.dp))
                Text(descripcion, color = TextoSecundario, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/**
 * Movimiento del historial: icono y signo indican si es ingreso o salida
 * (además del color), con fecha, número y cuenta de referencia.
 */
@Composable
fun ItemMovimiento(movimiento: MovimientoModelo, modifier: Modifier = Modifier) {
    val ingreso = movimiento.accion == "INGRESO"
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = Espacio.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(if (ingreso) ExitoFondo else ErrorFondo, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painterResource(if (ingreso) R.drawable.ic_deposito else R.drawable.ic_retiro),
                contentDescription = if (ingreso) "Ingreso" else "Salida",
                tint = if (ingreso) ExitoRelleno else ErrorRelleno,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(Espacio.s))
        Column(Modifier.weight(1f)) {
            Text(movimiento.tipo ?: "-", color = Texto, style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val referencia = movimiento.cuentaReferencia?.let { "  ·  Ref. $it" } ?: ""
            Text("N.º ${movimiento.numero}  ·  ${movimiento.fecha ?: "-"}$referencia",
                color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.width(Espacio.s))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                (if (ingreso) "+ " else "− ") + formatoImporte(movimiento.importe),
                color = if (ingreso) ExitoTexto else ErrorTexto,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(movimiento.accion ?: "", color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** Separador de 1 dp #D3DCEA. */
@Composable
fun LineaDivisoria() {
    HorizontalDivider(thickness = 1.dp, color = Divisor)
}

/** Ficha de una operación registrada: encabezado de éxito con icono y las filas de datos. */
@Composable
fun TarjetaResultado(mensaje: String, filas: List<Pair<String, String>>, modifier: Modifier = Modifier) {
    Tarjeta(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconoEnCaja(R.drawable.ic_exito, ExitoFondo, ExitoRelleno, 44.dp)
            Spacer(Modifier.width(Espacio.s))
            Column {
                Sobretitulo("Operación registrada", color = ExitoTexto)
                Text(mensaje, color = Texto, style = MaterialTheme.typography.titleLarge)
            }
        }
        Spacer(Modifier.height(Espacio.m))
        LineaDivisoria()
        filas.forEachIndexed { indice, (etiqueta, valor) ->
            FilaDato(etiqueta, valor, destacado = indice == 0)
        }
    }
}

/** Confirmación antes de registrar una operación: resumen y dos acciones. */
@Composable
fun DialogoConfirmacion(
    titulo: String,
    filas: List<Pair<String, String>>,
    textoConfirmar: String,
    alConfirmar: () -> Unit,
    alCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = alCancelar,
        containerColor = Blanco,
        shape = RoundedCornerShape(24.dp),
        title = { Text(titulo, color = Texto, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text("Revisa los datos antes de registrar la operación.", color = TextoSecundario,
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(Espacio.s))
                filas.forEach { (etiqueta, valor) -> FilaDato(etiqueta, valor) }
            }
        },
        confirmButton = {
            Button(
                onClick = alConfirmar,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulAccion, contentColor = Blanco)
            ) { Text(textoConfirmar, style = MaterialTheme.typography.labelLarge) }
        },
        dismissButton = {
            TextButton(onClick = alCancelar, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Cancelar", color = AzulHover, style = MaterialTheme.typography.labelLarge)
            }
        }
    )
}

// ================================================================ Formato de datos para mostrar

private val FORMATO_IMPORTE = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))

/** Importe con separador de miles y dos decimales: 6,900.00 */
fun formatoImporte(importe: BigDecimal?): String =
    importe?.let { FORMATO_IMPORTE.format(it.setScale(2, RoundingMode.HALF_UP)) } ?: "-"

/** Hora local (HH:mm) de una fecha ISO-8601 en UTC, por ejemplo 2026-11-02T20:03:25.938Z. */
fun horaLocal(iso: String?): String {
    if (iso == null || iso.length < 19) {
        return "-"
    }
    return try {
        val lector = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        lector.timeZone = TimeZone.getTimeZone("UTC")
        val fecha = lector.parse(iso.substring(0, 19)) ?: return "-"
        SimpleDateFormat("HH:mm", Locale.US).format(fecha)
    } catch (ex: Exception) {
        "-"
    }
}


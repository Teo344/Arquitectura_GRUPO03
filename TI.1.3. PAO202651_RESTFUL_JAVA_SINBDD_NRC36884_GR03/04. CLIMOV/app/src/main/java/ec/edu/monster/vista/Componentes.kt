package ec.edu.monster.vista

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import ec.edu.monster.R

// Componentes comunes de las pantallas, construidos con los valores de Tema.kt.

/** Barra de navegación superior (#0B1D44 con texto blanco y #B8D2FF), con una acción opcional a la derecha. */
@Composable
fun BarraSuperior(
    titulo: String,
    subtitulo: String,
    alVolver: (() -> Unit)? = null,
    accion: (@Composable () -> Unit)? = null
) {
    Surface(color = AzulOscuro) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (alVolver != null) {
                IconButton(onClick = alVolver) {
                    Icon(painterResource(R.drawable.ic_volver), contentDescription = "Volver", tint = Blanco)
                }
                Spacer(Modifier.width(4.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(titulo, color = Blanco, style = MaterialTheme.typography.titleLarge)
                Text(subtitulo, color = BordeSuave, style = MaterialTheme.typography.bodySmall)
            }
            if (accion != null) {
                Spacer(Modifier.width(12.dp))
                accion()
            }
        }
    }
}

@Composable
fun Sobretitulo(texto: String, modifier: Modifier = Modifier) {
    Text(texto.uppercase(), modifier = modifier, color = AzulAccion, style = MaterialTheme.typography.labelMedium)
}

/** Tarjeta: fondo blanco, borde #D3DCEA, radio 16, sombra md y padding 24. */
@Composable
fun Tarjeta(modifier: Modifier = Modifier, contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        border = BorderStroke(1.dp, Divisor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(24.dp), content = contenido)
    }
}

/** Botón primario: fondo #1D56CC, texto blanco, 48 dp de alto, radio 10. Uno por vista. */
@Composable
fun BotonPrimario(
    texto: String,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier,
    cargando: Boolean = false
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
            Spacer(Modifier.width(8.dp))
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
fun BotonPeligro(texto: String, alPulsar: () -> Unit, modifier: Modifier = Modifier, icono: Int? = null) {
    Button(
        onClick = alPulsar,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ErrorRelleno, contentColor = Blanco)
    ) {
        if (icono != null) {
            Icon(painterResource(icono), contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(texto, style = MaterialTheme.typography.labelLarge)
    }
}

/** Campo con etiqueta visible encima (borde #B1BFD4, foco #1D56CC, error #C0392B). */
@Composable
fun CampoTexto(
    etiqueta: String,
    valor: String,
    alCambiar: (String) -> Unit,
    modifier: Modifier = Modifier,
    esClave: Boolean = false,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    accionTeclado: ImeAction = ImeAction.Next,
    alConfirmar: () -> Unit = {},
    habilitado: Boolean = true
) {
    Column(modifier.fillMaxWidth()) {
        Text(etiqueta, color = Texto, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = alCambiar,
            modifier = Modifier.fillMaxWidth(),
            enabled = habilitado,
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            visualTransformation = if (esClave) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado, imeAction = accionTeclado),
            keyboardActions = KeyboardActions(onDone = { alConfirmar() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AzulAccion,
                unfocusedBorderColor = BordeCampo,
                errorBorderColor = ErrorRelleno,
                focusedContainerColor = Blanco,
                unfocusedContainerColor = Blanco,
                disabledContainerColor = FondoSeccion,
                cursorColor = AzulAccion,
                focusedTextColor = Texto,
                unfocusedTextColor = Texto,
                disabledTextColor = DeshabilitadoTexto
            )
        )
    }
}

/** Lista desplegable con etiqueta visible encima, con el mismo aspecto que los campos. */
@Composable
fun <T> Selector(
    etiqueta: String,
    opciones: List<T>,
    seleccionada: T?,
    alSeleccionar: (T) -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    textoOpcion: (T) -> String = { it.toString() }
) {
    var abierto by remember { mutableStateOf(false) }
    Column(modifier.fillMaxWidth()) {
        Text(etiqueta, color = Texto, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))
        Box {
            Surface(
                onClick = { if (opciones.isNotEmpty()) abierto = true },
                enabled = habilitado,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(10.dp),
                color = if (habilitado) Blanco else FondoSeccion,
                border = BorderStroke(1.dp, BordeCampo)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = seleccionada?.let(textoOpcion) ?: "Cargando opciones...",
                        modifier = Modifier.weight(1f),
                        color = if (seleccionada == null || !habilitado) Placeholder else Texto,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Icon(painterResource(R.drawable.ic_desplegar), contentDescription = null, tint = TextoSecundario)
                }
            }
            DropdownMenu(
                expanded = abierto && habilitado,
                onDismissRequest = { abierto = false },
                modifier = Modifier.background(Blanco)
            ) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(textoOpcion(opcion), style = MaterialTheme.typography.bodyLarge, color = Texto) },
                        onClick = {
                            abierto = false
                            alSeleccionar(opcion)
                        }
                    )
                }
            }
        }
    }
}

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
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(icono), contentDescription = if (esError) "Error" else "Información",
            tint = relleno, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(mensaje, color = colorTexto, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Etiqueta pequeña (chip) con radio 6. */
@Composable
fun Chip(texto: String, fondo: Color, colorTexto: Color) {
    Text(
        text = texto,
        color = colorTexto,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier
            .background(fondo, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

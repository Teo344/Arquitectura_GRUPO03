package ec.edu.monster.vista

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ec.edu.monster.R
import ec.edu.monster.modelo.CarritoModelo
import ec.edu.monster.modelo.CreditoModelo
import ec.edu.monster.modelo.CuotaModelo
import ec.edu.monster.modelo.EvaluacionClienteModelo
import ec.edu.monster.modelo.FacturaModelo
import ec.edu.monster.modelo.LineaCarrito
import ec.edu.monster.modelo.TelefonoModelo
import java.math.BigDecimal

// Componentes de la tienda, los mismos del cliente web (WEB-INF/tags) y del escritorio,
// construidos solo con los valores de Tema.kt.

// ================================================================ Estructura

/** Encabezado de una tarjeta: icono en caja, sobretítulo, título y una acción opcional. */
@Composable
fun EncabezadoTarjeta(
    icono: Int,
    sobretitulo: String,
    titulo: String,
    acento: Boolean = true,
    accion: (@Composable () -> Unit)? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconoEnCaja(icono, if (acento) CianSuave else AzulSuave, if (acento) CianTexto else AzulAccion)
        Spacer(Modifier.width(Espacio.s))
        Column(Modifier.weight(1f)) {
            Sobretitulo(sobretitulo)
            Text(titulo, color = Texto, style = MaterialTheme.typography.titleLarge)
        }
        accion?.invoke()
    }
}

/** Título de un paso de la venta: número en círculo y texto. */
@Composable
fun PasoVenta(numero: Int, titulo: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(24.dp).background(AzulSuave, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(numero.toString(), color = AzulAccion, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(Espacio.xs))
        Text(titulo, color = Texto, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
    Spacer(Modifier.height(Espacio.s))
}

/** Separación entre pasos de la venta: margen y línea divisoria. */
@Composable
fun SeparadorPaso() {
    Spacer(Modifier.height(Espacio.l))
    LineaDivisoria()
    Spacer(Modifier.height(Espacio.m))
}

@Composable
fun BotonTexto(texto: String, alPulsar: () -> Unit, modifier: Modifier = Modifier, habilitado: Boolean = true) {
    TextButton(onClick = alPulsar, modifier = modifier.heightIn(min = 44.dp), enabled = habilitado) {
        Text(texto, color = if (habilitado) AzulHover else DeshabilitadoTexto, style = MaterialTheme.typography.labelLarge)
    }
}

/** Buscador con icono: filtra mientras se escribe o al confirmar con el teclado. */
@Composable
fun Buscador(
    valor: String,
    alCambiar: (String) -> Unit,
    marcador: String,
    modifier: Modifier = Modifier,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    alConfirmar: () -> Unit = {}
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        textStyle = MaterialTheme.typography.bodyLarge,
        placeholder = { Text(marcador, color = Placeholder, style = MaterialTheme.typography.bodyLarge) },
        leadingIcon = { Icon(painterResource(R.drawable.ic_buscar), contentDescription = null, tint = Placeholder) },
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado, imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { alConfirmar() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AzulAccion,
            unfocusedBorderColor = BordeCampo,
            focusedContainerColor = Blanco,
            unfocusedContainerColor = Blanco,
            cursorColor = AzulAccion,
            focusedTextColor = Texto,
            unfocusedTextColor = Texto
        )
    )
}

/** Lista desplegable del número de cuotas: de 3 a 18, como el select del cliente web. */
@Composable
fun SelectorCuotas(etiqueta: String, valor: Int, alCambiar: (Int) -> Unit, habilitado: Boolean = true) {
    var abierto by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        Text(etiqueta, color = Texto, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(6.dp))
        Box {
            Surface(
                onClick = { abierto = true },
                enabled = habilitado,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(10.dp),
                color = Blanco,
                border = BorderStroke(1.5.dp, BordeCampo)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = Espacio.s),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$valor cuotas", color = Texto, style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f))
                    Text("▾", color = Placeholder, style = MaterialTheme.typography.bodyLarge)
                }
            }
            DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
                for (n in CarritoModelo.CUOTAS_MINIMO..CarritoModelo.CUOTAS_MAXIMO) {
                    DropdownMenuItem(
                        text = { Text("$n cuotas", color = Texto, style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            abierto = false
                            alCambiar(n)
                        }
                    )
                }
            }
        }
    }
}

// ================================================================ Catálogo

/** Tarjeta de un teléfono: mosaico con el icono, marca, modelo, precio, precio en efectivo y Agregar. */
@Composable
fun TarjetaProducto(telefono: TelefonoModelo, enVenta: Int, alAgregar: () -> Unit, modifier: Modifier = Modifier) {
    val forma = RoundedCornerShape(16.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(3.dp, forma, ambientColor = TinteSombra, spotColor = TinteSombra.copy(alpha = 0.25f)),
        shape = forma,
        color = Blanco,
        border = BorderStroke(1.dp, Divisor)
    ) {
        Column(Modifier.padding(Espacio.m), verticalArrangement = Arrangement.spacedBy(Espacio.s)) {
            Box(
                modifier = Modifier.fillMaxWidth().height(88.dp).background(AzulSuave, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(R.drawable.ic_celular), contentDescription = null, tint = AzulAccion,
                    modifier = Modifier.size(40.dp))
                if (enVenta > 0) {
                    Box(Modifier.align(Alignment.TopEnd).padding(Espacio.xs)) {
                        Chip("$enVenta en la venta", CianSuave, CianTextoOscuro)
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(Espacio.xxs)) {
                Chip(valorO(telefono.marca), AzulSuave, AzulHover)
                Text(valorO(telefono.modelo), color = Texto, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(formatoImporte(telefono.precio), color = AzulOscuro, style = MaterialTheme.typography.headlineSmall)
                Text("Efectivo: ${formatoImporte(telefono.precioEfectivo)}", color = CianTexto, fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold)
            }
            BotonSecundario("Agregar", alAgregar)
        }
    }
}

// ================================================================ Venta

/** Contador − n + de una línea de la venta. */
@Composable
private fun ContadorCantidad(valor: Int, maximo: Int, alCambiar: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Espacio.xxs)) {
        BotonContador(R.drawable.ic_quitar, "Quitar una unidad", true) { alCambiar(valor - 1) }
        Text(valor.toString(), color = Texto, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 28.dp))
        BotonContador(R.drawable.ic_agregar, "Agregar una unidad", valor < maximo) { alCambiar(valor + 1) }
    }
}

@Composable
private fun BotonContador(icono: Int, descripcion: String, habilitado: Boolean, alPulsar: () -> Unit) {
    Surface(
        onClick = alPulsar,
        enabled = habilitado,
        modifier = Modifier.size(36.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (habilitado) Blanco else FondoSeccion,
        border = BorderStroke(1.dp, BordeSuave)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(painterResource(icono), contentDescription = descripcion,
                tint = if (habilitado) AzulHover else DeshabilitadoTexto, modifier = Modifier.size(18.dp))
        }
    }
}

/** Línea de la venta: arriba el teléfono y su subtotal; abajo el contador y quitar. */
@Composable
fun LineaVenta(linea: LineaCarrito, alCambiarCantidad: (Int) -> Unit, alQuitar: () -> Unit) {
    val telefono = linea.telefono
    Column(Modifier.fillMaxWidth().padding(vertical = Espacio.s), verticalArrangement = Arrangement.spacedBy(Espacio.xs)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(valorO(telefono.modelo), color = Texto, style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${valorO(telefono.marca)} · ${formatoImporte(telefono.precio)} c/u", color = TextoSecundario,
                    style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.width(Espacio.s))
            Text(formatoImporte(linea.subtotal), color = Texto, style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            ContadorCantidad(linea.cantidad, CarritoModelo.CANTIDAD_MAXIMA, alCambiarCantidad)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = alQuitar) {
                Icon(painterResource(R.drawable.ic_eliminar), contentDescription = "Quitar ${telefono.nombre} de la venta",
                    tint = ErrorTexto, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/** Totales de una venta o factura: subtotal, descuento (solo si hay, en verde y con signo −) y total destacado. */
@Composable
fun ResumenTotales(subtotal: BigDecimal?, descuento: BigDecimal?, total: BigDecimal?) {
    Column {
        FilaTotal("Subtotal", formatoImporte(subtotal), Texto)
        if (descuento != null && descuento.signum() > 0) {
            FilaTotal("Descuento efectivo (42 %)", "− ${formatoImporte(descuento)}", ExitoTexto)
        }
        Spacer(Modifier.height(Espacio.xs))
        LineaDivisoria()
        Row(Modifier.fillMaxWidth().padding(top = Espacio.s), verticalAlignment = Alignment.CenterVertically) {
            Text("Total", color = Texto, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Text(formatoImporte(total), color = AzulOscuro, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun FilaTotal(etiqueta: String, valor: String, colorValor: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = Espacio.xxs), verticalAlignment = Alignment.CenterVertically) {
        Text(etiqueta, color = TextoSecundario, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(valor, color = colorValor, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

// ================================================================ Crédito BanQuito

/**
 * Resultado del banco para un comprador: nombre, chip de sujeto de crédito
 * (nunca solo color), motivos si no lo es y monto máximo. compacta es la
 * versión del carrito.
 */
@Composable
fun TarjetaEvaluacion(evaluacion: EvaluacionClienteModelo, compacta: Boolean = false) {
    val contenedor = if (compacta) {
        Modifier
            .fillMaxWidth()
            .background(Superficie, RoundedCornerShape(10.dp))
            .border(1.dp, Divisor, RoundedCornerShape(10.dp))
            .padding(Espacio.s)
    } else {
        Modifier.fillMaxWidth()
    }
    Column(contenedor) {
        if (!evaluacion.isCliente) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconoEnCaja(R.drawable.ic_error, ErrorFondo, ErrorRelleno)
                Spacer(Modifier.width(Espacio.s))
                Column {
                    Text("No es cliente de BanQuito", color = Texto, style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold)
                    Text("Cédula ${valorO(evaluacion.cedula)} · solo los clientes del banco pueden comprar",
                        color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
                }
            }
            return@Column
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconoEnCaja(R.drawable.ic_persona, AzulSuave, AzulAccion)
            Spacer(Modifier.width(Espacio.s))
            Column(Modifier.weight(1f)) {
                Text(valorO(evaluacion.nombre), color = Texto, style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold)
                Text(
                    "Cliente BanQuito N.º ${evaluacion.codCliente}" +
                        if (compacta) "" else " · cédula ${valorO(evaluacion.cedula)}",
                    color = TextoSecundario, style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Spacer(Modifier.height(Espacio.xs))
        Row {
            if (evaluacion.isSujetoCredito) {
                Chip("Sujeto de crédito", ExitoFondo, ExitoTexto, R.drawable.ic_exito, ExitoRelleno)
            } else {
                Chip("No sujeto de crédito", AdvertenciaFondo, AdvertenciaTexto, R.drawable.ic_advertencia,
                    AdvertenciaRelleno)
            }
        }
        if (!evaluacion.isSujetoCredito) {
            Spacer(Modifier.height(Espacio.s))
            evaluacion.motivos.forEach { motivo ->
                Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.ic_advertencia), contentDescription = null,
                        tint = AdvertenciaRelleno, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(Espacio.xs))
                    Text(valorO(motivo.descripcion), color = AdvertenciaTexto, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(Modifier.height(if (compacta) Espacio.s else Espacio.m))
        if (compacta) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Monto máximo de crédito", color = TextoSecundario, style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f))
                Text(formatoImporte(evaluacion.montoMaximo), color = AzulOscuro,
                    style = MaterialTheme.typography.titleLarge)
            }
        } else {
            Text("Monto máximo de crédito", color = TextoSecundario, style = MaterialTheme.typography.bodyMedium)
            Text(formatoImporte(evaluacion.montoMaximo), color = AzulOscuro,
                style = MaterialTheme.typography.headlineMedium)
        }
    }
}

/** Dato destacado de un crédito: etiqueta, valor y una nota opcional. */
@Composable
fun DatoClave(etiqueta: String, valor: String, modifier: Modifier = Modifier, nota: String? = null, colorValor: Color = Texto) {
    Tarjeta(modifier) {
        Text(etiqueta, color = TextoSecundario, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(Espacio.xxs))
        Text(valor, color = colorValor, style = MaterialTheme.typography.headlineSmall)
        if (nota != null) {
            Spacer(Modifier.height(Espacio.xxs))
            Text(nota, color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** Datos clave de un crédito: monto financiado, cuota fija, interés total y total a pagar. */
@Composable
fun ResumenCreditoDatos(resumen: ResumenCredito, columnas: Int) {
    val datos: List<@Composable (Modifier) -> Unit> = listOf(
        { DatoClave("Monto financiado", formatoImporte(resumen.monto), it, colorValor = AzulOscuro) },
        { DatoClave("Cuota fija", formatoImporte(resumen.valorCuota), it, "${resumen.numCuotas} cuotas mensuales", CianTexto) },
        { DatoClave("Interés total", formatoImporte(resumen.interesTotal), it, "Tasa 16.5 % anual") },
        { DatoClave("Total a pagar", formatoImporte(resumen.totalPagado), it) }
    )
    Column(verticalArrangement = Arrangement.spacedBy(Espacio.m)) {
        datos.chunked(columnas).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(Espacio.m)) {
                fila.forEach { dato -> dato(Modifier.weight(1f)) }
            }
        }
    }
}

/** Tabla de amortización con las columnas del enunciado; la última cuota cierra en 0.00 y va resaltada. */
@Composable
fun TablaAmortizacion(cuotas: List<CuotaModelo>) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().background(FondoSeccion).padding(horizontal = Espacio.xs, vertical = Espacio.s)) {
            CeldaEncabezado("# Cuota", 0.7f, TextAlign.Start)
            CeldaEncabezado("Valor Cuota", 1f, TextAlign.End)
            CeldaEncabezado("Interés Pagado", 1f, TextAlign.End)
            CeldaEncabezado("Capital Pagado", 1f, TextAlign.End)
            CeldaEncabezado("Saldo", 1f, TextAlign.End)
        }
        cuotas.forEachIndexed { indice, cuota ->
            val final = indice == cuotas.lastIndex
            val color = if (final) ExitoTexto else Texto
            val peso = if (final) FontWeight.Bold else FontWeight.Normal
            LineaDivisoria()
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(if (final) ExitoFondo else Blanco)
                    .padding(horizontal = Espacio.xs, vertical = Espacio.s)
            ) {
                CeldaNumero(cuota.numero.toString(), 0.7f, TextAlign.Start, color, peso)
                CeldaNumero(formatoImporte(cuota.valorCuota), 1f, TextAlign.End, color, peso)
                CeldaNumero(formatoImporte(cuota.interes), 1f, TextAlign.End, color, peso)
                CeldaNumero(formatoImporte(cuota.capital), 1f, TextAlign.End, color, peso)
                CeldaNumero(formatoImporte(cuota.saldo), 1f, TextAlign.End, color, peso)
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.CeldaEncabezado(texto: String, peso: Float, alineacion: TextAlign) {
    Text(texto.uppercase(), color = TextoSecundario, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
        textAlign = alineacion, modifier = Modifier.weight(peso))
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.CeldaNumero(
    texto: String,
    peso: Float,
    alineacion: TextAlign,
    color: Color,
    grosor: FontWeight
) {
    Text(texto, color = color, fontSize = 13.sp, fontWeight = grosor, textAlign = alineacion, maxLines = 1,
        modifier = Modifier.weight(peso))
}

/** Tabla completa de un crédito: datos clave y tarjeta con la tabla. */
@Composable
fun DetalleCredito(cuotas: List<CuotaModelo>, columnas: Int) {
    val resumen = ResumenCredito.de(cuotas)
    if (resumen == null) {
        EstadoVacio(R.drawable.ic_credito, "Sin cuotas", "El banco no devolvió cuotas para este crédito.")
        return
    }
    ResumenCreditoDatos(resumen, columnas)
    TarjetaTabla { TablaAmortizacion(cuotas) }
}

/** Tarjeta sin relleno para que la tabla llegue hasta los bordes. */
@Composable
fun TarjetaTabla(modifier: Modifier = Modifier, contenido: @Composable () -> Unit) {
    val forma = RoundedCornerShape(16.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, forma, ambientColor = TinteSombra, spotColor = TinteSombra.copy(alpha = 0.35f)),
        shape = forma,
        color = Blanco,
        border = BorderStroke(1.dp, Divisor)
    ) {
        contenido()
    }
}

/** Crédito de la lista de un cliente: toda la fila abre su tabla de amortización. */
@Composable
fun ItemCredito(credito: CreditoModelo, alPulsar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = alPulsar)
            .padding(vertical = Espacio.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconoEnCaja(R.drawable.ic_credito, CianSuave, CianTexto)
        Spacer(Modifier.width(Espacio.s))
        Column(Modifier.weight(1f)) {
            Text("Crédito N.º ${credito.codCredito} · ${formatoImporte(credito.monto)}", color = Texto,
                style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text("${formatoFecha(credito.fecha)} · ${credito.numCuotas} cuotas de ${formatoImporte(credito.valorCuota)}",
                color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.width(Espacio.xs))
        ChipEstado(credito.estado)
    }
}

// ================================================================ Factura

@Composable
fun ChipFormaPago(factura: FacturaModelo, detalle: Boolean = false) {
    if (factura.isCredito) {
        Chip(
            (if (detalle) "Crédito BanQuito" else "Crédito") + " · ${factura.numCuotas} cuotas",
            CianSuave, CianTextoOscuro, R.drawable.ic_credito, CianTexto
        )
    } else {
        Chip(if (detalle) "Efectivo · 42 % de descuento" else "Efectivo", ExitoFondo, ExitoTexto)
    }
}

/** Factura de la lista: número, fecha, comprador, forma de pago y total; toda la fila la abre. */
@Composable
fun ItemFactura(factura: FacturaModelo, alPulsar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = alPulsar)
            .padding(Espacio.m),
        verticalArrangement = Arrangement.spacedBy(Espacio.xxs)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(factura.numeroVisible, color = AzulAccion, style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(formatoFecha(factura.fecha), color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
        }
        Text(valorO(factura.nombreCliente), color = Texto, style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold)
        Text("Cédula ${valorO(factura.cedula)}", color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f)) { ChipFormaPago(factura) }
            Text(formatoImporte(factura.total), color = Texto, style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold)
        }
    }
}

/** Factura completa, la misma del cliente web: tienda, cliente, detalle, crédito y totales. */
@Composable
fun FacturaDetalle(factura: FacturaModelo, alVerTabla: () -> Unit) {
    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconoEnCaja(R.drawable.ic_celular, AzulAccion, Blanco)
            Spacer(Modifier.width(Espacio.s))
            Column {
                Text("MonsterCell", color = Texto, style = MaterialTheme.typography.titleLarge)
                Text("Comercializadora de teléfonos celulares · Grupo 03", color = TextoSecundario,
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(Espacio.m))
        Sobretitulo("Factura")
        Text("N.º ${factura.numeroVisible}", color = Texto, style = MaterialTheme.typography.headlineSmall)
        Text("${formatoFecha(factura.fecha)} · vendedor ${valorO(factura.usuario)}", color = TextoSecundario,
            style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(Espacio.m))
        LineaDivisoria()
        Spacer(Modifier.height(Espacio.m))
        Sobretitulo("Cliente")
        Spacer(Modifier.height(Espacio.xxs))
        Text(valorO(factura.nombreCliente), color = Texto, style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold)
        Text("Cédula ${valorO(factura.cedula)} · cliente BanQuito N.º ${factura.codClienteBanco}",
            color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(Espacio.m))
        Sobretitulo("Forma de pago")
        Spacer(Modifier.height(Espacio.xxs))
        Row { ChipFormaPago(factura, detalle = true) }
        Spacer(Modifier.height(Espacio.l))
        Row(Modifier.fillMaxWidth().background(FondoSeccion).padding(horizontal = Espacio.xs, vertical = Espacio.s)) {
            CeldaEncabezado("Cant.", 0.5f, TextAlign.Start)
            CeldaEncabezado("Descripción", 1.6f, TextAlign.Start)
            CeldaEncabezado("Precio unitario", 1f, TextAlign.End)
            CeldaEncabezado("Subtotal", 1f, TextAlign.End)
        }
        factura.detalles.forEach { detalle ->
            LineaDivisoria()
            Row(Modifier.fillMaxWidth().padding(horizontal = Espacio.xs, vertical = Espacio.s)) {
                CeldaNumero(detalle.cantidad.toString(), 0.5f, TextAlign.Start, Texto, FontWeight.Normal)
                Text("${valorO(detalle.marca)} ${valorO(detalle.modelo)}", color = Texto, fontSize = 13.sp,
                    modifier = Modifier.weight(1.6f))
                CeldaNumero(formatoImporte(detalle.precioUnitario), 1f, TextAlign.End, Texto, FontWeight.Normal)
                CeldaNumero(formatoImporte(detalle.subtotal), 1f, TextAlign.End, Texto, FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(Espacio.l))
        if (factura.isCredito) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CianSuave, RoundedCornerShape(16.dp))
                    .padding(Espacio.m)
            ) {
                IconoEnCaja(R.drawable.ic_credito, Blanco, CianTexto)
                Spacer(Modifier.width(Espacio.s))
                Column {
                    Text("Crédito BanQuito N.º ${factura.codCreditoBanco}", color = Texto,
                        style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Text("${factura.numCuotas} cuotas mensuales fijas al 16.5 % anual", color = TextoSecundario,
                        style = MaterialTheme.typography.bodySmall)
                    BotonTexto("Ver tabla de amortización", alVerTabla)
                }
            }
            Spacer(Modifier.height(Espacio.m))
        }
        ResumenTotales(factura.subtotal, factura.descuento, factura.total)
    }
}

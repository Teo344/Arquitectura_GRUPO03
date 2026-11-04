package ec.edu.monster.vista

import android.content.Intent
import android.graphics.Color as ColorAndroid
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import ec.edu.monster.R
import ec.edu.monster.controlador.ControladorBase
import ec.edu.monster.controlador.VentaControlador
import ec.edu.monster.modelo.CarritoModelo
import ec.edu.monster.modelo.CuotaModelo
import ec.edu.monster.modelo.FacturaModelo
import ec.edu.monster.modelo.FormaPago
import ec.edu.monster.modelo.TelefonoModelo
import ec.edu.monster.servicio.CreditoRestService
import ec.edu.monster.servicio.FacturaRestService
import ec.edu.monster.servicio.TelefonoRestService
import kotlinx.coroutines.launch

/**
 * Punto de venta (CU-07, CU-09 y CU-10), el mismo del cliente web: catálogo y
 * venta actual en 4 pasos. La vista solo captura datos y muestra lo que le
 * entrega el VentaControlador.
 */
class VentaActivity : ComponentActivity(), ControladorBase.Vista {

    companion object {
        const val EXTRA_CARRITO = "carrito"
    }

    private lateinit var controlador: VentaControlador

    // El carrito es un objeto del modelo: cada cambio sube la versión para redibujar la venta
    private var versionVenta by mutableIntStateOf(0)
    private var catalogo by mutableStateOf<List<TelefonoModelo>>(emptyList())
    private var busqueda by mutableStateOf("")
    private var cedula by mutableStateOf("")
    private var esperandoCatalogo by mutableStateOf(false)
    private var esperandoVerificacion by mutableStateOf(false)
    private var esperandoFactura by mutableStateOf(false)
    private var tablaEstimada by mutableStateOf<List<CuotaModelo>?>(null)
    private var calculandoCuota by mutableStateOf(false)
    private var cuotaNoDisponible by mutableStateOf<String?>(null)
    private var verTablaEstimada by mutableStateOf(false)
    private var porConfirmar by mutableStateOf(false)
    private var factura by mutableStateOf<FacturaModelo?>(null)
    private var tablaFactura by mutableStateOf<List<CuotaModelo>?>(null)
    private var mensaje by mutableStateOf<String?>(null)
    private var mensajeEsError by mutableStateOf(false)

    private val venta: CarritoModelo
        get() {
            versionVenta
            return controlador.carrito
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sesion = MenuActivity.leerSesion(intent)
        if (sesion == null) {
            MenuActivity.abrirLogin(this, "Inicia sesión para continuar.")
            return
        }
        val carrito = IntentCompat.getSerializableExtra(intent, EXTRA_CARRITO, CarritoModelo::class.java) ?: CarritoModelo()
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(ColorAndroid.TRANSPARENT, ColorAndroid.TRANSPARENT))
        controlador = VentaControlador(this, TelefonoRestService(), CreditoRestService(), FacturaRestService(), sesion, carrito)
        cedula = carrito.cedula
        setContent { TemaMonsterCell { Pantalla() } }
        controlador.cargarCatalogo()
    }

    /** El menú guarda la venta: al salir se le devuelve el carrito como quedó. */
    override fun finish() {
        if (::controlador.isInitialized) {
            setResult(RESULT_OK, Intent().putExtra(EXTRA_CARRITO, controlador.carrito))
        }
        super.finish()
    }

    override fun onDestroy() {
        if (::controlador.isInitialized) {
            controlador.detener()
        }
        super.onDestroy()
    }

    // ---- Métodos que usa el controlador ----

    override fun setProcesando(procesando: Boolean) {
        if (procesando) {
            mensaje = null
        }
    }

    override fun mostrarMensaje(texto: String, esError: Boolean) {
        mensaje = texto
        mensajeEsError = esError
    }

    override fun volverAlLogin(mensaje: String) {
        MenuActivity.abrirLogin(this, "$mensaje. Inicia sesión nuevamente.")
    }

    fun setCargandoCatalogo(cargando: Boolean) {
        esperandoCatalogo = cargando
    }

    fun setVerificando(cargando: Boolean) {
        esperandoVerificacion = cargando
        if (cargando) {
            mensaje = null
        }
    }

    fun setFacturando(cargando: Boolean) {
        esperandoFactura = cargando
        if (cargando) {
            mensaje = null
        }
    }

    fun mostrarCatalogo(telefonos: List<TelefonoModelo>) {
        catalogo = telefonos
    }

    fun actualizarVenta() {
        versionVenta++
    }

    fun limpiarComprador() {
        cedula = ""
    }

    fun mostrarSimulacion(tabla: List<CuotaModelo>?) {
        tablaEstimada = tabla
        calculandoCuota = false
        cuotaNoDisponible = null
    }

    fun mostrarSimulacionCalculando() {
        tablaEstimada = null
        calculandoCuota = true
        cuotaNoDisponible = null
    }

    fun mostrarSimulacionNoDisponible(texto: String) {
        tablaEstimada = null
        calculandoCuota = false
        cuotaNoDisponible = texto
    }

    fun pedirConfirmacion() {
        mensaje = null
        porConfirmar = true
    }

    fun mostrarFacturaRegistrada(registrada: FacturaModelo, texto: String) {
        factura = registrada
        mostrarMensaje(texto, false)
    }

    fun mostrarTablaFactura(cuotas: List<CuotaModelo>) {
        tablaFactura = cuotas
    }

    private fun cerrarFactura() {
        factura = null
        mensaje = null
    }

    // ---- Interfaz ----

    @Composable
    private fun Pantalla() {
        val registrada = factura
        val tabla = tablaFactura
        when {
            registrada != null && tabla != null -> {
                BackHandler { tablaFactura = null }
                PantallaTabla(registrada, tabla)
            }
            registrada != null -> {
                BackHandler { cerrarFactura() }
                PantallaFactura(registrada)
            }
            else -> PantallaVenta()
        }
    }

    @Composable
    private fun PantallaVenta() {
        val desplazamiento = rememberScrollState()
        val alcance = rememberCoroutineScope()
        val irAVenta = remember { BringIntoViewRequester() }
        Column(Modifier.fillMaxSize().background(Superficie)) {
            BarraSuperior("Nueva venta", "Verifica al comprador, arma la venta y factura", alVolver = { finish() })
            Box(Modifier.weight(1f)) {
                ConAncho(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(desplazamiento)
                        .imePadding()
                        .navigationBarsPadding()
                        .padding(Espacio.m)
                ) { clase ->
                    if (clase == ClaseAncho.AMPLIO) {
                        // Pantalla amplia: catálogo a la izquierda y venta actual a la derecha, como en la web
                        ContenidoCentrado(Ancho.CONTENIDO) {
                            Row(horizontalArrangement = Arrangement.spacedBy(Espacio.l)) {
                                Column(Modifier.weight(1f)) { Catalogo(2) }
                                Column(Modifier.width(400.dp)) { VentaActual() }
                            }
                        }
                    } else {
                        // Pantalla angosta: la venta queda debajo del catálogo y la barra inferior lleva a ella
                        ContenidoCentrado(Ancho.CONTENIDO) {
                            Catalogo(if (clase == ClaseAncho.MEDIO) 2 else 1)
                            Column(Modifier.bringIntoViewRequester(irAVenta)) { VentaActual() }
                            if (!venta.isVacio) {
                                Spacer(Modifier.height(72.dp))
                            }
                        }
                    }
                }
                if (!venta.isVacio) {
                    ConAncho(Modifier.align(Alignment.BottomCenter).fillMaxWidth()) { clase ->
                        if (clase != ClaseAncho.AMPLIO) {
                            BarraVenta { alcance.launch { irAVenta.bringIntoView() } }
                        }
                    }
                }
            }
        }

        if (porConfirmar) {
            DialogoConfirmacion(
                titulo = "Confirmar venta",
                filas = resumen(),
                textoConfirmar = "Facturar",
                texto = if (venta.isCredito) "BanQuito otorgará el crédito por el total y se emitirá la factura."
                else "Se emitirá la factura con el descuento por pago en efectivo.",
                alConfirmar = {
                    porConfirmar = false
                    controlador.facturar()
                },
                alCancelar = { porConfirmar = false }
            )
        }
        val estimada = tablaEstimada
        if (verTablaEstimada && estimada != null) {
            DialogoTablaEstimada(estimada)
        }
    }

    /** Mantiene la venta a la vista en pantallas angostas, como la barra del cliente web. */
    @Composable
    private fun BarraVenta(alVer: () -> Unit) {
        Surface(
            onClick = alVer,
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(Espacio.m),
            shape = RoundedCornerShape(16.dp),
            color = AzulOscuro
        ) {
            Row(
                modifier = Modifier.padding(horizontal = Espacio.m, vertical = Espacio.s),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(R.drawable.ic_carrito), contentDescription = null, tint = Blanco,
                    modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(Espacio.s))
                Text("${textoUnidades(venta.articulos)} · ${formatoImporte(venta.total)}", color = Blanco,
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f))
                Box(
                    Modifier
                        .background(AzulAccion, RoundedCornerShape(10.dp))
                        .padding(horizontal = Espacio.s, vertical = Espacio.xs)
                ) {
                    Text("Ver venta", color = Blanco, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }

    @Composable
    private fun Catalogo(columnas: Int) {
        val filtro = busqueda.trim().lowercase()
        val visibles = catalogo.filter { filtro.isEmpty() || it.nombre.lowercase().contains(filtro) }
        Column(verticalArrangement = Arrangement.spacedBy(Espacio.s)) {
            TituloSeccion("Catálogo", "Teléfonos a la venta")
            Buscador(busqueda, { busqueda = it }, "Buscar marca o modelo")
            Text("${visibles.size}${if (visibles.size == 1) " modelo" else " modelos"}", color = TextoSecundario,
                style = MaterialTheme.typography.bodySmall)
            when {
                esperandoCatalogo && catalogo.isEmpty() -> Box(Modifier.fillMaxWidth().padding(Espacio.xl), Alignment.Center) {
                    CircularProgressIndicator(color = AzulAccion)
                }
                catalogo.isEmpty() -> EstadoVacio(R.drawable.ic_catalogo, "Catálogo vacío",
                    "Agrega teléfonos desde Catálogo › Teléfonos para empezar a vender.")
                visibles.isEmpty() -> EstadoVacio(R.drawable.ic_buscar, "Sin resultados",
                    "No hay teléfonos que coincidan con la búsqueda.")
                else -> visibles.chunked(columnas).forEach { fila ->
                    Row(horizontalArrangement = Arrangement.spacedBy(Espacio.m)) {
                        fila.forEach { telefono ->
                            TarjetaProducto(
                                telefono = telefono,
                                enVenta = venta.cantidades[telefono.codTelefono] ?: 0,
                                alAgregar = { controlador.agregar(telefono) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(columnas - fila.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }

    @Composable
    private fun VentaActual() {
        val carrito = venta
        Tarjeta {
            EncabezadoTarjeta(
                icono = R.drawable.ic_carrito,
                sobretitulo = "Venta actual",
                titulo = if (carrito.isVacio) "Sin teléfonos" else textoUnidades(carrito.articulos),
                accion = if (!carrito.isVacio || carrito.cedula.isNotEmpty()) {
                    { BotonTexto("Vaciar", { controlador.vaciar() }) }
                } else null
            )
            mensaje?.let {
                Spacer(Modifier.height(Espacio.m))
                Alerta(it, mensajeEsError)
            }

            SeparadorPaso()
            PasoVenta(1, "Comprador")
            CampoTexto(
                etiqueta = "Cédula del comprador",
                valor = cedula,
                alCambiar = {
                    cedula = it
                    controlador.cambiarCedula(it)
                },
                marcador = "1712345678",
                tipoTeclado = KeyboardType.Number,
                accionTeclado = ImeAction.Done,
                alConfirmar = { controlador.verificarComprador(cedula) },
                habilitado = !esperandoVerificacion
            )
            Spacer(Modifier.height(Espacio.xs))
            BotonSecundario(if (esperandoVerificacion) "Verificando..." else "Verificar", { controlador.verificarComprador(cedula) },
                habilitado = !esperandoVerificacion)
            Spacer(Modifier.height(Espacio.s))
            val evaluacion = carrito.evaluacion
            if (evaluacion != null) {
                TarjetaEvaluacion(evaluacion, compacta = true)
            } else {
                Text("BanQuito confirma que es cliente y si puede comprar a crédito.", color = TextoSecundario,
                    style = MaterialTheme.typography.bodySmall)
            }

            SeparadorPaso()
            PasoVenta(2, "Teléfonos")
            if (carrito.isVacio) {
                Text("Agrega teléfonos desde el catálogo.", color = TextoSecundario,
                    style = MaterialTheme.typography.bodyMedium)
            } else {
                carrito.lineas.forEachIndexed { indice, linea ->
                    if (indice > 0) {
                        LineaDivisoria()
                    }
                    LineaVenta(
                        linea = linea,
                        alCambiarCantidad = { controlador.cambiarCantidad(linea.telefono.codTelefono, it) },
                        alQuitar = { controlador.quitar(linea.telefono.codTelefono) }
                    )
                }
            }

            SeparadorPaso()
            PasoVenta(3, "Forma de pago")
            SelectorSegmentado(
                opciones = FormaPago.entries,
                seleccionada = carrito.formaPago,
                alSeleccionar = { controlador.cambiarFormaPago(it) },
                texto = { if (it == FormaPago.EFECTIVO) "Efectivo −42 %" else it.nombreVisible },
                icono = { if (it == FormaPago.EFECTIVO) R.drawable.ic_factura else R.drawable.ic_credito }
            )
            if (carrito.isCredito) {
                Spacer(Modifier.height(Espacio.s))
                SelectorCuotas("Número de cuotas mensuales", carrito.numCuotas, { controlador.cambiarCuotas(it) })
                if (!carrito.isVacio) {
                    Spacer(Modifier.height(Espacio.s))
                    CuotaEstimada()
                }
            }

            SeparadorPaso()
            ResumenTotales(carrito.subtotal, carrito.descuento, carrito.total)
            val motivo = if (carrito.isVacio) null else controlador.motivoParaNoFacturar()
            if (motivo != null) {
                Spacer(Modifier.height(Espacio.s))
                Alerta(motivo, true)
            }
            Spacer(Modifier.height(Espacio.m))
            BotonPrimario(
                texto = if (carrito.isVacio) "Facturar" else "Facturar ${formatoImporte(carrito.total)}",
                alPulsar = { controlador.preparar() },
                cargando = esperandoFactura,
                icono = R.drawable.ic_factura,
                habilitado = motivo == null && !carrito.isVacio
            )
            Spacer(Modifier.height(Espacio.xs))
            Text("Los precios se confirman con el catálogo al facturar.", color = TextoSecundario,
                style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth())
        }
    }

    @Composable
    private fun CuotaEstimada() {
        val cuota = tablaEstimada?.let { controlador.cuotaEstimada() }
        Column(
            Modifier
                .fillMaxWidth()
                .background(CianSuave, RoundedCornerShape(10.dp))
                .padding(Espacio.s)
        ) {
            when {
                cuota != null -> {
                    Text("Cuota mensual estimada (16.5 % anual)", color = TextoSecundario,
                        style = MaterialTheme.typography.bodyMedium)
                    Text(formatoImporte(cuota), color = CianTexto, style = MaterialTheme.typography.titleLarge)
                    BotonTexto("Ver tabla de cuotas", { verTablaEstimada = true })
                }
                calculandoCuota -> Text("Calculando la cuota...", color = TextoSecundario,
                    style = MaterialTheme.typography.bodySmall)
                else -> Text("No se pudo calcular la cuota: ${cuotaNoDisponible ?: "-"}", color = TextoSecundario,
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    @Composable
    private fun DialogoTablaEstimada(cuotas: List<CuotaModelo>) {
        AlertDialog(
            onDismissRequest = { verTablaEstimada = false },
            containerColor = Blanco,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Tabla de cuotas estimada", color = Texto, style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "${formatoImporte(venta.total)} a ${venta.numCuotas} cuotas, 16.5 % anual. Es una simulación: " +
                            "el crédito se otorga al facturar.",
                        color = TextoSecundario, style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(Espacio.m))
                    TablaAmortizacion(cuotas)
                }
            },
            confirmButton = {
                Button(
                    onClick = { verTablaEstimada = false },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulAccion, contentColor = Blanco)
                ) { Text("Cerrar", style = MaterialTheme.typography.labelLarge) }
            }
        )
    }

    @Composable
    private fun PantallaFactura(registrada: FacturaModelo) {
        Column(Modifier.fillMaxSize().background(Superficie)) {
            BarraSuperior(
                "Factura N.º ${registrada.numeroVisible}",
                "${registrada.formaPago.nombreVisible} · ${formatoFecha(registrada.fecha)}",
                alVolver = { cerrarFactura() }
            )
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(Espacio.m)
            ) {
                ContenidoCentrado(Ancho.FORMULARIO) {
                    mensaje?.let { Alerta(it, mensajeEsError) }
                    FacturaDetalle(registrada) { controlador.verTabla(registrada) }
                    BotonPrimario("Nueva venta", { cerrarFactura() }, icono = R.drawable.ic_carrito)
                }
            }
        }
    }

    @Composable
    private fun PantallaTabla(registrada: FacturaModelo, cuotas: List<CuotaModelo>) {
        Column(Modifier.fillMaxSize().background(Superficie)) {
            BarraSuperior(
                "Crédito BanQuito N.º ${registrada.codCreditoBanco}",
                "Tabla de amortización · cuota fija mensual",
                alVolver = { tablaFactura = null }
            )
            ConAncho(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(Espacio.m)
            ) { clase ->
                ContenidoCentrado(Ancho.CONTENIDO) {
                    DetalleCredito(cuotas, if (clase == ClaseAncho.COMPACTO) 1 else 2)
                }
            }
        }
    }

    /** Filas del diálogo "Confirmar venta"; la última es el total. */
    private fun resumen(): List<Pair<String, String>> {
        val carrito = venta
        val filas = mutableListOf(
            "Comprador" to valorO(carrito.evaluacion?.nombre),
            "Cédula" to carrito.cedula,
            "Teléfonos" to textoUnidades(carrito.articulos)
        )
        if (carrito.isCredito) {
            filas += "Forma de pago" to "Crédito BanQuito a ${carrito.numCuotas} cuotas"
            controlador.cuotaEstimada()?.let { filas += "Cuota mensual" to formatoImporte(it) }
        } else {
            filas += "Forma de pago" to "Efectivo (42 % de descuento)"
            filas += "Descuento" to "− ${formatoImporte(carrito.descuento)}"
        }
        filas += "Total a facturar" to formatoImporte(carrito.total)
        return filas
    }

    private fun textoUnidades(unidades: Int): String = "$unidades ${if (unidades == 1) "unidad" else "unidades"}"
}

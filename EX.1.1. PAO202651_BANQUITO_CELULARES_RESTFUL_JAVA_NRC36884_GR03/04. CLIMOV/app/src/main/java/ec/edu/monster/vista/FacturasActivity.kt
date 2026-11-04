package ec.edu.monster.vista

import android.graphics.Color as ColorAndroid
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import ec.edu.monster.R
import ec.edu.monster.controlador.ControladorBase
import ec.edu.monster.controlador.FacturaControlador
import ec.edu.monster.modelo.CuotaModelo
import ec.edu.monster.modelo.FacturaModelo
import ec.edu.monster.servicio.CreditoRestService
import ec.edu.monster.servicio.FacturaRestService

/** Facturas emitidas (CU-11), como en el cliente web: lista con filtro, factura y tabla de amortización. */
class FacturasActivity : ComponentActivity(), ControladorBase.Vista {

    private lateinit var controlador: FacturaControlador

    private var cedula by mutableStateOf("")
    private var filtro by mutableStateOf("")
    private var facturas by mutableStateOf<List<FacturaModelo>>(emptyList())
    private var factura by mutableStateOf<FacturaModelo?>(null)
    private var cuotas by mutableStateOf<List<CuotaModelo>?>(null)
    private var cargando by mutableStateOf(false)
    private var mensaje by mutableStateOf<String?>(null)
    private var mensajeEsError by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sesion = MenuActivity.leerSesion(intent)
        if (sesion == null) {
            MenuActivity.abrirLogin(this, "Inicia sesión para continuar.")
            return
        }
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(ColorAndroid.TRANSPARENT, ColorAndroid.TRANSPARENT))
        controlador = FacturaControlador(this, FacturaRestService(), CreditoRestService(), sesion)
        setContent { TemaMonsterCell { Pantalla() } }
        controlador.buscar("")
    }

    override fun onDestroy() {
        if (::controlador.isInitialized) {
            controlador.detener()
        }
        super.onDestroy()
    }

    // ---- Métodos que usa el controlador ----

    override fun setProcesando(procesando: Boolean) {
        cargando = procesando
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

    fun mostrarFacturas(cedulaFiltrada: String, lista: List<FacturaModelo>) {
        filtro = cedulaFiltrada
        facturas = lista
    }

    fun mostrarFactura(detalle: FacturaModelo) {
        factura = detalle
    }

    fun mostrarTabla(tabla: List<CuotaModelo>) {
        cuotas = tabla
    }

    // ---- Interfaz ----

    @Composable
    private fun Pantalla() {
        val abierta = factura
        val tabla = cuotas
        when {
            abierta != null && tabla != null -> {
                BackHandler { cuotas = null }
                PantallaTabla(abierta, tabla)
            }
            abierta != null -> {
                BackHandler { factura = null }
                PantallaFactura(abierta)
            }
            else -> PantallaLista()
        }
    }

    @Composable
    private fun PantallaLista() {
        Column(Modifier.fillMaxSize().background(Superficie)) {
            BarraSuperior("Facturas", "Ventas emitidas, las más recientes primero", alVolver = { finish() })
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(Espacio.m)
            ) {
                ContenidoCentrado(Ancho.CONTENIDO) {
                    TituloSeccion("Historial", "Facturas emitidas")
                    Buscador(cedula, { cedula = it }, "Filtrar por cédula", tipoTeclado = KeyboardType.Number,
                        alConfirmar = { controlador.buscar(cedula) })
                    BotonSecundario("Buscar", { controlador.buscar(cedula) }, habilitado = !cargando)
                    mensaje?.let { Alerta(it, mensajeEsError) }
                    Resultado()
                }
            }
        }
    }

    @Composable
    private fun Resultado() {
        when {
            cargando && facturas.isEmpty() -> Box(Modifier.fillMaxWidth().padding(Espacio.xl), Alignment.Center) {
                CircularProgressIndicator(color = AzulAccion)
            }
            facturas.isEmpty() -> EstadoVacio(
                R.drawable.ic_factura,
                if (filtro.isEmpty()) "Todavía no hay facturas" else "Sin facturas para esta cédula",
                if (filtro.isEmpty()) "Las ventas que factures aparecerán aquí."
                else "Prueba con otra cédula o quita el filtro."
            )
            else -> {
                Text(
                    "${facturas.size}${if (facturas.size == 1) " factura" else " facturas"}" +
                        if (filtro.isEmpty()) "" else " de la cédula $filtro",
                    color = TextoSecundario, style = MaterialTheme.typography.bodySmall
                )
                TarjetaTabla {
                    Column {
                        facturas.forEachIndexed { indice, elemento ->
                            if (indice > 0) {
                                LineaDivisoria()
                            }
                            ItemFactura(elemento) { controlador.abrir(elemento.numFactura) }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun PantallaFactura(abierta: FacturaModelo) {
        Column(Modifier.fillMaxSize().background(Superficie)) {
            BarraSuperior(
                "Factura N.º ${abierta.numeroVisible}",
                "${abierta.formaPago.nombreVisible} · ${formatoFecha(abierta.fecha)}",
                alVolver = { factura = null }
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
                    FacturaDetalle(abierta) { controlador.verTabla(abierta) }
                }
            }
        }
    }

    @Composable
    private fun PantallaTabla(abierta: FacturaModelo, tabla: List<CuotaModelo>) {
        Column(Modifier.fillMaxSize().background(Superficie)) {
            BarraSuperior(
                "Crédito BanQuito N.º ${abierta.codCreditoBanco}",
                "Tabla de amortización · cuota fija mensual",
                alVolver = { cuotas = null }
            )
            ConAncho(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(Espacio.m)
            ) { clase ->
                ContenidoCentrado(Ancho.CONTENIDO) {
                    DetalleCredito(tabla, if (clase == ClaseAncho.COMPACTO) 1 else 2)
                }
            }
        }
    }
}

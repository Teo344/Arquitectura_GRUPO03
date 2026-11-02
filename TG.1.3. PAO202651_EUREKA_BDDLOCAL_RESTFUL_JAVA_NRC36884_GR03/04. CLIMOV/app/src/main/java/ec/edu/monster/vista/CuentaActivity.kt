package ec.edu.monster.vista

import android.graphics.Color as ColorAndroid
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import ec.edu.monster.R
import ec.edu.monster.controlador.CuentaControlador
import ec.edu.monster.modelo.CuentaModelo
import ec.edu.monster.modelo.MovimientoModelo
import ec.edu.monster.servicio.CuentaRestService

/** Vista de consultas (RQF-003 y RQF-004): solo muestra datos y delega al CuentaControlador. */
class CuentaActivity : ComponentActivity() {

    companion object {
        const val EXTRA_MOVIMIENTOS = "movimientos"
    }

    private enum class Modo(val etiqueta: String, val icono: Int) {
        CUENTA("Datos de la cuenta", R.drawable.ic_cuenta),
        MOVIMIENTOS("Movimientos", R.drawable.ic_movimientos)
    }

    private lateinit var controlador: CuentaControlador

    private var modo by mutableStateOf(Modo.CUENTA)
    private var cuenta by mutableStateOf("")
    private var datosCuenta by mutableStateOf<CuentaModelo?>(null)
    private var movimientos by mutableStateOf<List<MovimientoModelo>?>(null)
    private var cuentaMovimientos by mutableStateOf("")
    private var mensaje by mutableStateOf<String?>(null)
    private var mensajeEsError by mutableStateOf(false)
    private var cargando by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sesion = MenuActivity.leerSesion(intent)
        if (sesion == null) {
            MenuActivity.abrirLogin(this, "Inicia sesión para continuar.")
            return
        }
        if (intent.getBooleanExtra(EXTRA_MOVIMIENTOS, false)) {
            modo = Modo.MOVIMIENTOS
        }
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(ColorAndroid.TRANSPARENT))
        controlador = CuentaControlador(this, CuentaRestService(), sesion)
        setContent { TemaEureka { Pantalla() } }
    }

    override fun onDestroy() {
        if (::controlador.isInitialized) {
            controlador.detener()
        }
        super.onDestroy()
    }

    // ---- Métodos que usa el controlador ----

    fun setProcesando(procesando: Boolean) {
        cargando = procesando
        if (procesando) {
            mensaje = null
        }
    }

    fun mostrarMensaje(texto: String, esError: Boolean) {
        mensaje = texto
        mensajeEsError = esError
    }

    fun mostrarCuenta(resultado: CuentaModelo) {
        datosCuenta = resultado
    }

    fun mostrarMovimientos(codigo: String, lista: List<MovimientoModelo>) {
        cuentaMovimientos = codigo
        movimientos = lista
    }

    fun volverAlLogin(mensaje: String) {
        MenuActivity.abrirLogin(this, "$mensaje. Inicia sesión nuevamente.")
    }

    private fun consultar() {
        if (modo == Modo.CUENTA) {
            controlador.consultarCuenta(cuenta)
        } else {
            controlador.consultarMovimientos(cuenta)
        }
    }

    // ---- Interfaz ----

    @Composable
    private fun Pantalla() {
        Column(Modifier.fillMaxSize().background(Superficie)) {
            BarraSuperior("Consultas", "Cuentas y movimientos", alVolver = { finish() })
            ConAncho(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(Espacio.m)
            ) { clase ->
                if (clase == ClaseAncho.AMPLIO) {
                    // Escritorio: búsqueda a la izquierda y resultado a la derecha
                    ContenidoCentrado(Ancho.CONTENIDO) {
                        Row(horizontalArrangement = Arrangement.spacedBy(Espacio.l)) {
                            Column(Modifier.weight(0.42f)) { Busqueda() }
                            Column(Modifier.weight(0.58f)) { Resultado() }
                        }
                    }
                } else {
                    ContenidoCentrado(Ancho.FORMULARIO) {
                        Busqueda()
                        Resultado()
                    }
                }
            }
        }
    }

    @Composable
    private fun Busqueda() {
        Tarjeta {
            Sobretitulo("Consulta")
            Spacer(Modifier.height(Espacio.xxs))
            Text("Buscar una cuenta", color = Texto, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Espacio.m))
            SelectorSegmentado(
                opciones = Modo.entries,
                seleccionada = modo,
                alSeleccionar = {
                    modo = it
                    mensaje = null
                },
                texto = { it.etiqueta },
                icono = { it.icono },
                habilitado = !cargando
            )
            Spacer(Modifier.height(Espacio.m))
            CampoTexto(
                etiqueta = "Número de cuenta",
                valor = cuenta,
                alCambiar = { cuenta = it },
                ayuda = "8 dígitos, por ejemplo 00100001",
                marcador = "00100001",
                tipoTeclado = KeyboardType.Number,
                accionTeclado = ImeAction.Done,
                alConfirmar = { consultar() },
                habilitado = !cargando
            )
            Spacer(Modifier.height(Espacio.l))
            BotonPrimario(
                if (modo == Modo.CUENTA) "Consultar cuenta" else "Ver movimientos",
                { consultar() },
                cargando = cargando,
                icono = modo.icono
            )
            mensaje?.let {
                Spacer(Modifier.height(Espacio.m))
                Alerta(it, mensajeEsError)
            }
        }
    }

    @Composable
    private fun Resultado() {
        if (modo == Modo.CUENTA) {
            val datos = datosCuenta
            if (datos == null) {
                EstadoVacio(R.drawable.ic_cuenta, "Sin consulta",
                    "Escribe un número de cuenta para ver su titular, saldo y estado.")
            } else {
                FichaCuenta(datos)
            }
        } else {
            val lista = movimientos
            when {
                lista == null -> EstadoVacio(R.drawable.ic_movimientos, "Sin consulta",
                    "Escribe un número de cuenta para ver su historial de movimientos.")
                lista.isEmpty() -> EstadoVacio(R.drawable.ic_info, "Cuenta $cuentaMovimientos",
                    "La cuenta no registra movimientos.")
                else -> ListaMovimientos(lista)
            }
        }
    }

    @Composable
    private fun FichaCuenta(datos: CuentaModelo) {
        Tarjeta {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconoEnCaja(R.drawable.ic_cuenta, AzulSuave, AzulAccion)
                Spacer(Modifier.width(Espacio.s))
                Column(Modifier.weight(1f)) {
                    Sobretitulo("Cuenta")
                    Text(datos.codigo ?: "-", color = Texto, style = MaterialTheme.typography.titleLarge)
                }
                ChipEstado(datos.estado)
            }
            Spacer(Modifier.height(Espacio.l))
            Text("Saldo disponible", color = TextoSecundario, style = MaterialTheme.typography.bodyMedium)
            Text(formatoImporte(datos.saldo), color = AzulOscuro, style = MaterialTheme.typography.headlineMedium)
            Text(datos.moneda ?: "", color = CianTexto, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(Espacio.m))
            LineaDivisoria()
            FilaDato("Titular", datos.titular ?: "-")
            FilaDato("Sucursal", datos.sucursal ?: "-")
            FilaDato("Fecha de creación", datos.fechaCreacion ?: "-")
        }
    }

    @Composable
    private fun ListaMovimientos(lista: List<MovimientoModelo>) {
        Tarjeta {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Sobretitulo("Cuenta $cuentaMovimientos")
                    Text("Historial de movimientos", color = Texto, style = MaterialTheme.typography.titleLarge)
                }
                Chip("${lista.size} movimientos", AzulSuave, AzulHover)
            }
            Spacer(Modifier.height(Espacio.s))
            lista.forEachIndexed { indice, movimiento ->
                if (indice > 0) {
                    LineaDivisoria()
                }
                ItemMovimiento(movimiento, Modifier.fillMaxWidth())
            }
        }
    }
}

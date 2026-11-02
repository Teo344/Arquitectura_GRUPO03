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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import ec.edu.monster.R
import ec.edu.monster.controlador.TransaccionControlador
import ec.edu.monster.modelo.ResultadoTransaccionModelo
import ec.edu.monster.modelo.TipoTransaccion
import ec.edu.monster.modelo.TransaccionModelo
import ec.edu.monster.servicio.TransaccionSoapService

/**
 * Vista de transacciones (RQF-005 a RQF-007): captura los datos, muestra la
 * confirmación y el comprobante; las reglas las valida el TransaccionControlador.
 */
class TransaccionActivity : ComponentActivity() {

    companion object {
        const val EXTRA_TIPO = "tipo"
    }

    private lateinit var controlador: TransaccionControlador

    private var tipo by mutableStateOf(TipoTransaccion.DEPOSITO)
    private var cuenta by mutableStateOf("")
    private var cuentaDestino by mutableStateOf("")
    private var importe by mutableStateOf("")
    private var porConfirmar by mutableStateOf<TransaccionModelo?>(null)
    private var registrada by mutableStateOf<TransaccionModelo?>(null)
    private var resultado by mutableStateOf<ResultadoTransaccionModelo?>(null)
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
        intent.getStringExtra(EXTRA_TIPO)?.let { nombre ->
            TipoTransaccion.entries.firstOrNull { it.name == nombre }?.let { tipo = it }
        }
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(ColorAndroid.TRANSPARENT))
        controlador = TransaccionControlador(this, TransaccionSoapService(), sesion)
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

    fun pedirConfirmacion(modelo: TransaccionModelo) {
        mensaje = null
        porConfirmar = modelo
    }

    fun mostrarResultado(modelo: TransaccionModelo, registro: ResultadoTransaccionModelo) {
        registrada = modelo
        resultado = registro
        importe = ""
    }

    fun volverAlLogin(mensaje: String) {
        MenuActivity.abrirLogin(this, "$mensaje. Inicia sesión nuevamente.")
    }

    private fun cambiarTipo(nuevo: TipoTransaccion) {
        tipo = nuevo
        mensaje = null
        resultado = null
        registrada = null
    }

    // ---- Interfaz ----

    private fun iconoDe(tipo: TipoTransaccion): Int = when (tipo) {
        TipoTransaccion.DEPOSITO -> R.drawable.ic_deposito
        TipoTransaccion.RETIRO -> R.drawable.ic_retiro
        TipoTransaccion.TRANSFERENCIA -> R.drawable.ic_transferencia
    }

    @Composable
    private fun Pantalla() {
        Column(Modifier.fillMaxSize().background(Superficie)) {
            BarraSuperior("Transacciones", "Depósitos, retiros y transferencias", alVolver = { finish() })
            ConAncho(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(Espacio.m)
            ) { clase ->
                if (clase == ClaseAncho.AMPLIO) {
                    // Escritorio: formulario a la izquierda y comprobante a la derecha
                    ContenidoCentrado(Ancho.CONTENIDO) {
                        Row(horizontalArrangement = Arrangement.spacedBy(Espacio.l)) {
                            Column(Modifier.weight(0.45f)) { Formulario() }
                            Column(Modifier.weight(0.55f)) { Comprobante() }
                        }
                    }
                } else {
                    ContenidoCentrado(Ancho.FORMULARIO) {
                        Formulario()
                        Comprobante()
                    }
                }
            }
        }

        porConfirmar?.let { modelo ->
            DialogoConfirmacion(
                titulo = "Confirmar ${modelo.tipo.nombreVisible.lowercase()}",
                filas = resumen(modelo),
                textoConfirmar = "Registrar",
                alConfirmar = {
                    porConfirmar = null
                    controlador.ejecutar(modelo)
                },
                alCancelar = { porConfirmar = null }
            )
        }
    }

    @Composable
    private fun Formulario() {
        Tarjeta {
            Sobretitulo("Nueva operación")
            Spacer(Modifier.height(Espacio.xxs))
            Text("Registrar ${tipo.nombreVisible.lowercase()}", color = Texto,
                style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Espacio.m))
            SelectorSegmentado(
                opciones = TipoTransaccion.entries,
                seleccionada = tipo,
                alSeleccionar = { cambiarTipo(it) },
                texto = { it.nombreVisible },
                icono = { iconoDe(it) },
                habilitado = !cargando
            )
            Spacer(Modifier.height(Espacio.m))

            val esTransferencia = tipo == TipoTransaccion.TRANSFERENCIA
            CampoTexto(
                etiqueta = if (esTransferencia) "Cuenta de origen" else "Número de cuenta",
                valor = cuenta,
                alCambiar = { cuenta = it },
                ayuda = "8 dígitos, por ejemplo 00100001",
                marcador = "00100001",
                tipoTeclado = KeyboardType.Number,
                habilitado = !cargando
            )
            if (esTransferencia) {
                Spacer(Modifier.height(Espacio.m))
                CampoTexto(
                    etiqueta = "Cuenta de destino",
                    valor = cuentaDestino,
                    alCambiar = { cuentaDestino = it },
                    ayuda = "Debe ser distinta de la de origen y de la misma moneda",
                    marcador = "00200002",
                    tipoTeclado = KeyboardType.Number,
                    habilitado = !cargando
                )
            }
            Spacer(Modifier.height(Espacio.m))
            CampoTexto(
                etiqueta = "Importe",
                valor = importe,
                alCambiar = { importe = it },
                ayuda = "Mayor que cero y con máximo dos decimales",
                marcador = "150.00",
                tipoTeclado = KeyboardType.Decimal,
                accionTeclado = ImeAction.Done,
                alConfirmar = { controlador.preparar(tipo, cuenta, cuentaDestino, importe) },
                habilitado = !cargando
            )
            Spacer(Modifier.height(Espacio.l))
            BotonPrimario(
                "Continuar",
                { controlador.preparar(tipo, cuenta, cuentaDestino, importe) },
                cargando = cargando,
                icono = iconoDe(tipo)
            )
            mensaje?.let {
                Spacer(Modifier.height(Espacio.m))
                Alerta(it, mensajeEsError)
            }
        }
    }

    @Composable
    private fun Comprobante() {
        val modelo = registrada
        val registro = resultado
        if (modelo == null || registro == null) {
            EstadoVacio(R.drawable.ic_movimientos, "Sin operaciones registradas",
                "Completa el formulario y confirma para ver aquí el comprobante con el nuevo saldo.")
            return
        }
        val filas = if (modelo.tipo == TipoTransaccion.TRANSFERENCIA) {
            listOf(
                "Nuevo saldo de origen" to formatoImporte(registro.nuevoSaldo),
                "Cuenta de origen" to (modelo.cuenta ?: "-").trim(),
                "Cuenta de destino" to (modelo.cuentaDestino ?: "-").trim(),
                "Importe" to formatoImporte(modelo.importe),
                "Movimiento en origen" to "N.º ${registro.numeroMovimiento ?: "-"}",
                "Movimiento en destino" to "N.º ${registro.numeroMovimientoDestino ?: "-"}"
            )
        } else {
            listOf(
                "Nuevo saldo" to formatoImporte(registro.nuevoSaldo),
                "Cuenta" to (modelo.cuenta ?: "-").trim(),
                "Importe" to formatoImporte(modelo.importe),
                "Movimiento" to "N.º ${registro.numeroMovimiento ?: "-"}"
            )
        }
        TarjetaResultado(registro.mensaje ?: "Operación registrada", filas)
    }

    private fun resumen(modelo: TransaccionModelo): List<Pair<String, String>> =
        if (modelo.tipo == TipoTransaccion.TRANSFERENCIA) {
            listOf(
                "Cuenta de origen" to (modelo.cuenta ?: "-").trim(),
                "Cuenta de destino" to (modelo.cuentaDestino ?: "-").trim(),
                "Importe" to formatoImporte(modelo.importe)
            )
        } else {
            listOf(
                "Operación" to modelo.tipo.nombreVisible,
                "Cuenta" to (modelo.cuenta ?: "-").trim(),
                "Importe" to formatoImporte(modelo.importe)
            )
        }
}

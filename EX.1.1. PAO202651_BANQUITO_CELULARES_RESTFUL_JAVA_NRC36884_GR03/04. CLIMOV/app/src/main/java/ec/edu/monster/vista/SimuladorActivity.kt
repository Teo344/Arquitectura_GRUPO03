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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import ec.edu.monster.R
import ec.edu.monster.controlador.ControladorBase
import ec.edu.monster.controlador.SimuladorControlador
import ec.edu.monster.modelo.CuotaModelo
import ec.edu.monster.servicio.CreditoRestService

/** Simulador de cuotas (CU-08), como en el cliente web: la tabla de un monto sin otorgar el crédito. */
class SimuladorActivity : ComponentActivity(), ControladorBase.Vista {

    private lateinit var controlador: SimuladorControlador

    private var monto by mutableStateOf("")
    private var numCuotas by mutableIntStateOf(6)
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
        controlador = SimuladorControlador(this, CreditoRestService(), sesion)
        setContent { TemaMonsterCell { Pantalla() } }
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

    fun mostrarTabla(tabla: List<CuotaModelo>) {
        cuotas = tabla
    }

    // ---- Interfaz ----

    @Composable
    private fun Pantalla() {
        Column(Modifier.fillMaxSize().background(Superficie)) {
            BarraSuperior("Simulador de cuotas", "Crédito BanQuito · 16.5 % anual, de 3 a 18 cuotas", alVolver = { finish() })
            ConAncho(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(Espacio.m)
            ) { clase ->
                if (clase == ClaseAncho.AMPLIO) {
                    ContenidoCentrado(Ancho.CONTENIDO) {
                        Row(horizontalArrangement = Arrangement.spacedBy(Espacio.l), verticalAlignment = Alignment.Top) {
                            Column(Modifier.weight(0.42f)) { Formulario() }
                            Column(Modifier.weight(0.58f), verticalArrangement = Arrangement.spacedBy(Espacio.m)) { Resultado(2) }
                        }
                    }
                } else {
                    ContenidoCentrado(Ancho.FORMULARIO) {
                        Formulario()
                        Resultado(if (clase == ClaseAncho.COMPACTO) 1 else 2)
                    }
                }
            }
        }
    }

    @Composable
    private fun Formulario() {
        Tarjeta {
            EncabezadoTarjeta(R.drawable.ic_calculadora, "Simulación", "Calcular cuotas")
            Spacer(Modifier.height(Espacio.m))
            CampoTexto(
                etiqueta = "Monto a financiar",
                valor = monto,
                alCambiar = { monto = it },
                marcador = "450.00",
                tipoTeclado = KeyboardType.Decimal,
                habilitado = !cargando
            )
            Spacer(Modifier.height(Espacio.m))
            SelectorCuotas("Número de cuotas", numCuotas, { numCuotas = it }, habilitado = !cargando)
            Spacer(Modifier.height(Espacio.l))
            BotonPrimario("Calcular", { controlador.simular(monto, numCuotas) }, cargando = cargando,
                icono = R.drawable.ic_calculadora)
            mensaje?.let {
                Spacer(Modifier.height(Espacio.m))
                Alerta(it, mensajeEsError)
            }
            Spacer(Modifier.height(Espacio.m))
            Text("Ejemplo del enunciado: 450.00 a 6 cuotas da una cuota de 78.65.", color = TextoSecundario,
                style = MaterialTheme.typography.bodySmall)
        }
    }

    @Composable
    private fun Resultado(columnas: Int) {
        val tabla = cuotas
        if (tabla == null) {
            EstadoVacio(R.drawable.ic_calculadora, "Sin simulación",
                "Ingresa un monto y el número de cuotas para ver la tabla de amortización.")
        } else {
            DetalleCredito(tabla, columnas)
        }
    }
}

package ec.edu.monster.vista

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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import ec.edu.monster.R
import ec.edu.monster.controlador.ControladorBase
import ec.edu.monster.controlador.CreditoControlador
import ec.edu.monster.modelo.CuotaModelo
import ec.edu.monster.servicio.CreditoRestService

/**
 * Crédito BanQuito de un comprador (CU-07 y CU-12), como en el cliente web:
 * evaluación, monto máximo, créditos y tabla de amortización del que elija.
 */
class ClientesActivity : ComponentActivity(), ControladorBase.Vista {

    private lateinit var controlador: CreditoControlador

    private var cedula by mutableStateOf("")
    private var consulta by mutableStateOf<CreditoControlador.ConsultaCliente?>(null)
    private var codCredito by mutableIntStateOf(0)
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
        controlador = CreditoControlador(this, CreditoRestService(), sesion)
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

    fun mostrarCliente(resultado: CreditoControlador.ConsultaCliente) {
        consulta = resultado
    }

    fun mostrarTabla(credito: Int, tabla: List<CuotaModelo>) {
        codCredito = credito
        cuotas = tabla
    }

    // ---- Interfaz ----

    @Composable
    private fun Pantalla() {
        val tabla = cuotas
        if (tabla != null) {
            BackHandler { cuotas = null }
            PantallaTabla(tabla)
        } else {
            PantallaConsulta()
        }
    }

    @Composable
    private fun PantallaConsulta() {
        Column(Modifier.fillMaxSize().background(Superficie)) {
            BarraSuperior("Crédito BanQuito", "Evaluación y créditos de un cliente", alVolver = { finish() })
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
                            Column(Modifier.weight(0.42f)) { Busqueda() }
                            Column(Modifier.weight(0.58f), verticalArrangement = Arrangement.spacedBy(Espacio.m)) { Resultado() }
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
            EncabezadoTarjeta(R.drawable.ic_credito, "Consulta", "Buscar un cliente")
            Spacer(Modifier.height(Espacio.m))
            CampoTexto(
                etiqueta = "Cédula",
                valor = cedula,
                alCambiar = { cedula = it },
                marcador = "1712345678",
                ayuda = "10 dígitos. El banco aplica las 4 reglas del crédito directo.",
                tipoTeclado = KeyboardType.Number,
                accionTeclado = ImeAction.Done,
                alConfirmar = { controlador.consultar(cedula) },
                habilitado = !cargando
            )
            Spacer(Modifier.height(Espacio.l))
            BotonPrimario("Consultar en BanQuito", { controlador.consultar(cedula) }, cargando = cargando,
                icono = R.drawable.ic_buscar)
            mensaje?.let {
                Spacer(Modifier.height(Espacio.m))
                Alerta(it, mensajeEsError)
            }
        }
    }

    @Composable
    private fun Resultado() {
        val resultado = consulta
        if (resultado == null) {
            EstadoVacio(R.drawable.ic_persona, "Sin consulta",
                "Escribe la cédula de un cliente para ver si es sujeto de crédito, su monto máximo y sus créditos.")
            return
        }
        Tarjeta { TarjetaEvaluacion(resultado.evaluacion) }
        if (resultado.evaluacion.isCliente) {
            val creditos = resultado.creditos
            Tarjeta {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) { TituloSeccion("Historial", "Créditos directos") }
                    Chip("${creditos.size} ${if (creditos.size == 1) "crédito" else "créditos"}", AzulSuave, AzulHover)
                }
                Spacer(Modifier.height(Espacio.m))
                if (creditos.isEmpty()) {
                    Text("El cliente no tiene créditos con BanQuito.", color = TextoSecundario,
                        style = MaterialTheme.typography.bodyMedium)
                } else {
                    creditos.forEachIndexed { indice, credito ->
                        if (indice > 0) {
                            LineaDivisoria()
                        }
                        ItemCredito(credito) { controlador.verTabla(credito.codCredito) }
                    }
                }
            }
        }
    }

    @Composable
    private fun PantallaTabla(tabla: List<CuotaModelo>) {
        Column(Modifier.fillMaxSize().background(Superficie)) {
            BarraSuperior("Crédito BanQuito N.º $codCredito", "Tabla de amortización · cuota fija mensual",
                alVolver = { cuotas = null })
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

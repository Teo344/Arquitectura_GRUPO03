package ec.edu.monster.vista

import android.graphics.Color as ColorAndroid
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import ec.edu.monster.controlador.ConversionControlador
import ec.edu.monster.modelo.Categoria
import ec.edu.monster.modelo.SesionUsuario
import ec.edu.monster.modelo.Unidad
import ec.edu.monster.servicio.ConversionSoapService

/** Vista del conversor: muestra categorías, unidades y resultados que entrega el ConversionControlador. */
class ConversionActivity : ComponentActivity() {

    private lateinit var controlador: ConversionControlador

    private var categorias by mutableStateOf<List<Categoria>>(emptyList())
    private var categoria by mutableStateOf<Categoria?>(null)
    private var unidades by mutableStateOf<List<Unidad>>(emptyList())
    private var origen by mutableStateOf<Unidad?>(null)
    private var destino by mutableStateOf<Unidad?>(null)
    private var valor by mutableStateOf("0")
    private var resultado by mutableStateOf<String?>(null)
    private var estado by mutableStateOf("Listo para convertir")
    private var estadoEsError by mutableStateOf(false)
    private var cargando by mutableStateOf(false)
    private val historial = mutableStateListOf<Pair<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sesion = IntentCompat.getSerializableExtra(intent, MenuActivity.EXTRA_SESION, SesionUsuario::class.java)
        if (sesion == null) {
            finish()
            return
        }
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(ColorAndroid.TRANSPARENT))
        controlador = ConversionControlador(this, ConversionSoapService(), sesion)
        setContent { TemaMonster { Pantalla() } }
        controlador.cargarCategorias()
    }

    override fun onDestroy() {
        if (::controlador.isInitialized) {
            controlador.detener()
        }
        super.onDestroy()
    }

    // ---- Métodos que usa el controlador ----

    fun mostrarCategorias(lista: List<Categoria>) {
        categorias = lista
        categoria = lista.firstOrNull()
    }

    fun mostrarUnidades(lista: List<Unidad>) {
        unidades = lista
        origen = lista.getOrNull(0)
        destino = lista.getOrNull(1) ?: lista.getOrNull(0)
        resultado = null
    }

    fun mostrarResultado(texto: String) {
        resultado = texto
    }

    fun agregarAlHistorial(fecha: String, detalle: String) {
        historial.add(0, fecha to detalle)
        if (historial.size > 10) {
            historial.removeAt(historial.lastIndex)
        }
    }

    fun mostrarEstado(texto: String, esError: Boolean) {
        estado = texto
        estadoEsError = esError
    }

    fun setProcesando(procesando: Boolean) {
        cargando = procesando
    }

    fun volverAlLogin(mensaje: String) {
        MenuActivity.abrirLogin(this, mensaje)
    }

    fun limpiarFormulario() {
        valor = "0"
        origen = unidades.getOrNull(0)
        destino = unidades.getOrNull(1) ?: unidades.getOrNull(0)
        resultado = null
    }

    // ---- Interfaz ----

    @Composable
    private fun Pantalla() {
        Scaffold(
            containerColor = Superficie,
            topBar = {
                BarraSuperior("MONSTER", "Conversor de unidades · longitud, masa y temperatura", alVolver = { finish() })
            }
        ) { relleno ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(relleno)
                    .consumeWindowInsets(relleno)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Tarjeta {
                    Sobretitulo("Conversión")
                    Spacer(Modifier.height(16.dp))
                    // Mientras se procesa, el formulario queda bloqueado para que el resultado
                    // corresponda a los datos enviados; cualquier cambio borra el resultado anterior
                    Selector("Categoría", categorias, categoria, {
                        categoria = it
                        controlador.seleccionarCategoria(it)
                    }, habilitado = !cargando, textoOpcion = { it.nombre })
                    Spacer(Modifier.height(16.dp))
                    CampoTexto(
                        etiqueta = "Valor",
                        valor = valor,
                        alCambiar = {
                            valor = it
                            resultado = null
                        },
                        tipoTeclado = KeyboardType.Decimal,
                        accionTeclado = ImeAction.Done,
                        habilitado = !cargando
                    )
                    Spacer(Modifier.height(16.dp))
                    Selector("Unidad de origen", unidades, origen, {
                        origen = it
                        resultado = null
                    }, habilitado = !cargando)
                    Spacer(Modifier.height(16.dp))
                    Selector("Unidad de destino", unidades, destino, {
                        destino = it
                        resultado = null
                    }, habilitado = !cargando)
                    Spacer(Modifier.height(24.dp))
                    BotonPrimario(
                        "Convertir",
                        { controlador.convertir(categoria, origen, destino, valor) },
                        cargando = cargando
                    )
                    Spacer(Modifier.height(12.dp))
                    BotonSecundario("Limpiar", { controlador.limpiar() }, habilitado = !cargando)
                    Spacer(Modifier.height(16.dp))
                    Alerta(estado, estadoEsError)
                }

                resultado?.let {
                    Spacer(Modifier.height(16.dp))
                    Tarjeta {
                        Sobretitulo("Resultado")
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = AzulOscuro, style = MaterialTheme.typography.headlineSmall)
                    }
                }

                if (historial.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Tarjeta {
                        Sobretitulo("Historial")
                        historial.forEachIndexed { indice, (fecha, detalle) ->
                            if (indice > 0) {
                                HorizontalDivider(color = Divisor)
                            }
                            Column(Modifier.padding(vertical = 8.dp)) {
                                Text(fecha, color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
                                Text(detalle, color = Texto, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

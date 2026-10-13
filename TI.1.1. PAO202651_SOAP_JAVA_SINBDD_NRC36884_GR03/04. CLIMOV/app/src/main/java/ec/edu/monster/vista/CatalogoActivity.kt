package ec.edu.monster.vista

import android.graphics.Color as ColorAndroid
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import ec.edu.monster.controlador.CatalogoControlador
import ec.edu.monster.modelo.Categoria
import ec.edu.monster.modelo.SesionUsuario
import ec.edu.monster.modelo.Unidad
import ec.edu.monster.servicio.ConversionSoapService

/** Vista del catálogo: muestra las categorías y unidades que entrega el CatalogoControlador. */
class CatalogoActivity : ComponentActivity() {

    private lateinit var controlador: CatalogoControlador

    private var categorias by mutableStateOf<List<Categoria>>(emptyList())
    private var seleccionada by mutableStateOf<Categoria?>(null)
    private var unidades by mutableStateOf<List<Unidad>>(emptyList())
    private var estado by mutableStateOf("El catálogo se consultará al servidor.")
    private var estadoEsError by mutableStateOf(false)
    private var cargando by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sesion = IntentCompat.getSerializableExtra(intent, MenuActivity.EXTRA_SESION, SesionUsuario::class.java)
        if (sesion == null) {
            finish()
            return
        }
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(ColorAndroid.TRANSPARENT))
        controlador = CatalogoControlador(this, ConversionSoapService(), sesion)
        setContent { TemaMonster { Pantalla() } }
        controlador.cargarCatalogo()
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
    }

    fun seleccionarCategoria(categoria: Categoria) {
        seleccionada = categoria
        unidades = emptyList()
    }

    fun mostrarUnidades(lista: List<Unidad>) {
        unidades = lista
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

    // ---- Interfaz ----

    @Composable
    private fun Pantalla() {
        Scaffold(
            containerColor = Superficie,
            topBar = {
                BarraSuperior(
                    "MONSTER · Catálogo de unidades",
                    "Consulta las categorías y unidades disponibles",
                    alVolver = { finish() }
                )
            }
        ) { relleno ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(relleno)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Sobretitulo("Categorías")
                Spacer(Modifier.height(8.dp))
                categorias.forEach { categoria ->
                    FilaCategoria(categoria, categoria == seleccionada)
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(8.dp))
                Tarjeta {
                    Text(
                        seleccionada?.nombre ?: "Selecciona una categoría",
                        color = AzulOscuro,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text("Unidades registradas para esta categoría", color = TextoSecundario,
                        style = MaterialTheme.typography.bodyMedium)
                    unidades.forEachIndexed { indice, unidad ->
                        if (indice > 0) {
                            HorizontalDivider(color = Divisor)
                        }
                        FilaUnidad(unidad)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Alerta(estado, estadoEsError)
            }
        }
    }

    @Composable
    private fun FilaCategoria(categoria: Categoria, activa: Boolean) {
        Surface(
            onClick = { if (!cargando) controlador.seleccionarCategoria(categoria) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = RoundedCornerShape(10.dp),
            color = if (activa) AzulSuave else Blanco,
            border = BorderStroke(if (activa) 2.dp else 1.dp, if (activa) AzulAccion else Divisor)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    categoria.nombre,
                    modifier = Modifier.weight(1f),
                    color = if (activa) AzulHover else Texto,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (categoria.isConvertible) {
                    Chip("Convertible", CianSuave, CianTexto)
                } else {
                    Chip("Solo consulta", FondoSeccion, TextoSecundario)
                }
            }
        }
    }

    @Composable
    private fun FilaUnidad(unidad: Unidad) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(unidad.nombre, color = Texto, style = MaterialTheme.typography.bodyLarge)
                Text("Código: " + unidad.codigo, color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.width(8.dp))
            Chip(unidad.simbolo, AzulSuave, AzulHover)
        }
    }
}

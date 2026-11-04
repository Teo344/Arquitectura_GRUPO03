package ec.edu.monster.vista

import android.graphics.Color as ColorAndroid
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ec.edu.monster.R
import ec.edu.monster.controlador.ControladorBase
import ec.edu.monster.controlador.TelefonoControlador
import ec.edu.monster.modelo.TelefonoModelo
import ec.edu.monster.servicio.TelefonoRestService
import java.math.RoundingMode

/** Mantenimiento del catálogo (CU-03 a CU-06), el mismo del cliente web: formulario y lista de teléfonos. */
class TelefonosActivity : ComponentActivity(), ControladorBase.Vista {

    private lateinit var controlador: TelefonoControlador

    private var telefonos by mutableStateOf<List<TelefonoModelo>>(emptyList())
    private var codigoEditado by mutableStateOf<Int?>(null)
    private var marca by mutableStateOf("")
    private var modelo by mutableStateOf("")
    private var precio by mutableStateOf("")
    private var porEliminar by mutableStateOf<TelefonoModelo?>(null)
    private var esperandoLista by mutableStateOf(false)
    private var esperandoGuardado by mutableStateOf(false)
    private var ocupada by mutableStateOf(false)
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
        controlador = TelefonoControlador(this, TelefonoRestService(), sesion)
        setContent { TemaMonsterCell { Pantalla() } }
        controlador.cargar()
    }

    override fun onDestroy() {
        if (::controlador.isInitialized) {
            controlador.detener()
        }
        super.onDestroy()
    }

    // ---- Métodos que usa el controlador ----

    override fun setProcesando(procesando: Boolean) {
        ocupada = procesando
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

    fun setCargando(cargando: Boolean) {
        esperandoLista = cargando
    }

    fun setGuardando(cargando: Boolean) {
        esperandoGuardado = cargando
        if (cargando) {
            mensaje = null
        }
    }

    fun mostrarTelefonos(lista: List<TelefonoModelo>) {
        telefonos = lista
    }

    fun cargarFormulario(telefono: TelefonoModelo) {
        codigoEditado = telefono.codTelefono
        marca = telefono.marca ?: ""
        modelo = telefono.modelo ?: ""
        precio = telefono.precio?.setScale(2, RoundingMode.HALF_UP)?.toPlainString() ?: ""
        mensaje = null
    }

    fun limpiarFormulario() {
        codigoEditado = null
        marca = ""
        modelo = ""
        precio = ""
    }

    fun limpiarFormularioSiEdita(codTelefono: Int) {
        if (codigoEditado == codTelefono) {
            limpiarFormulario()
        }
    }

    // ---- Interfaz ----

    @Composable
    private fun Pantalla() {
        Column(Modifier.fillMaxSize().background(Superficie)) {
            BarraSuperior("Catálogo", "Agrega, edita o da de baja los modelos a la venta", alVolver = { finish() })
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
                        Row(horizontalArrangement = Arrangement.spacedBy(Espacio.l)) {
                            Column(Modifier.weight(0.42f)) { Formulario() }
                            Column(Modifier.weight(0.58f)) { Lista() }
                        }
                    }
                } else {
                    ContenidoCentrado(Ancho.FORMULARIO) {
                        Formulario()
                        Lista()
                    }
                }
            }
        }

        porEliminar?.let { telefono ->
            DialogoConfirmacion(
                titulo = "Dar de baja ${telefono.nombre}",
                filas = listOf(
                    "Marca" to valorO(telefono.marca),
                    "Modelo" to valorO(telefono.modelo),
                    "Precio" to formatoImporte(telefono.precio)
                ),
                textoConfirmar = "Dar de baja",
                texto = "El teléfono dejará de aparecer en el punto de venta. Las facturas emitidas no cambian.",
                peligro = true,
                alConfirmar = {
                    porEliminar = null
                    controlador.darDeBaja(telefono)
                },
                alCancelar = { porEliminar = null }
            )
        }
    }

    @Composable
    private fun Formulario() {
        val editando = codigoEditado != null
        Tarjeta {
            EncabezadoTarjeta(
                icono = if (editando) R.drawable.ic_editar else R.drawable.ic_agregar,
                sobretitulo = if (editando) "Editar" else "Nuevo",
                titulo = if (editando) "Editar teléfono" else "Agregar teléfono",
                acento = editando
            )
            Spacer(Modifier.height(Espacio.m))
            CampoTexto("Marca", marca, { marca = it }, marcador = "Samsung", habilitado = !esperandoGuardado)
            Spacer(Modifier.height(Espacio.m))
            CampoTexto("Modelo", modelo, { modelo = it }, marcador = "Galaxy A55", habilitado = !esperandoGuardado)
            Spacer(Modifier.height(Espacio.m))
            CampoTexto(
                etiqueta = "Precio de venta",
                valor = precio,
                alCambiar = { precio = it },
                marcador = "549.00",
                ayuda = "En efectivo se vende con 42 % de descuento",
                tipoTeclado = KeyboardType.Decimal,
                accionTeclado = ImeAction.Done,
                alConfirmar = { guardar() },
                habilitado = !esperandoGuardado
            )
            Spacer(Modifier.height(Espacio.l))
            BotonPrimario(
                texto = if (editando) "Guardar cambios" else "Agregar al catálogo",
                alPulsar = { guardar() },
                cargando = esperandoGuardado,
                icono = if (editando) R.drawable.ic_editar else R.drawable.ic_agregar
            )
            if (editando) {
                Spacer(Modifier.height(Espacio.xs))
                BotonTexto("Cancelar edición", { limpiarFormulario() }, Modifier.fillMaxWidth())
            }
            mensaje?.let {
                Spacer(Modifier.height(Espacio.m))
                Alerta(it, mensajeEsError)
            }
        }
    }

    private fun guardar() {
        controlador.guardar(codigoEditado, marca, modelo, precio)
    }

    @Composable
    private fun Lista() {
        when {
            esperandoLista && telefonos.isEmpty() -> Box(Modifier.fillMaxWidth().padding(Espacio.xl), Alignment.Center) {
                CircularProgressIndicator(color = AzulAccion)
            }
            telefonos.isEmpty() -> EstadoVacio(R.drawable.ic_catalogo, "Catálogo vacío",
                "Agrega el primer teléfono con el formulario.")
            else -> TarjetaTabla {
                Column {
                    Row(Modifier.padding(Espacio.l), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f)) { TituloSeccion("A la venta", "Teléfonos del catálogo") }
                        Chip("${telefonos.size} modelos", AzulSuave, AzulHover)
                    }
                    telefonos.forEach { telefono ->
                        LineaDivisoria()
                        Fila(telefono)
                    }
                }
            }
        }
    }

    @Composable
    private fun Fila(telefono: TelefonoModelo) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (telefono.codTelefono == codigoEditado) CianSuave else Blanco)
                .padding(start = Espacio.m, end = Espacio.xs, top = Espacio.s, bottom = Espacio.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(valorO(telefono.marca), color = TextoSecundario, style = MaterialTheme.typography.bodySmall)
                Text(valorO(telefono.modelo), color = Texto, style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold)
                Row {
                    Text(formatoImporte(telefono.precio), color = Texto, style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(Espacio.s))
                    Text("Efectivo ${formatoImporte(telefono.precioEfectivo)}", color = CianTexto,
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
            IconButton(onClick = { controlador.editar(telefono.codTelefono) }, enabled = !ocupada) {
                Icon(painterResource(R.drawable.ic_editar), contentDescription = "Editar ${telefono.nombre}",
                    tint = AzulHover, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = { porEliminar = telefono }, enabled = !ocupada) {
                Icon(painterResource(R.drawable.ic_eliminar), contentDescription = "Dar de baja ${telefono.nombre}",
                    tint = ErrorTexto, modifier = Modifier.size(20.dp))
            }
        }
    }
}

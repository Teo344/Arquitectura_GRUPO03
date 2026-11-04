package ec.edu.monster.vista

import android.app.Activity
import android.content.Intent
import android.graphics.Color as ColorAndroid
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.IntentCompat
import ec.edu.monster.R
import ec.edu.monster.controlador.MenuControlador
import ec.edu.monster.modelo.CarritoModelo
import ec.edu.monster.modelo.SesionModelo
import ec.edu.monster.servicio.AutenticacionRestService

/** Vista del menú principal: las secciones de la barra lateral web, y delega cada opción al MenuControlador. */
class MenuActivity : ComponentActivity() {

    companion object {
        const val EXTRA_SESION = "sesion"
        const val EXTRA_MENSAJE = "mensaje"
        const val EXTRA_ES_ERROR = "esError"

        /** Cierra todas las pantallas y vuelve al login, opcionalmente con un mensaje. */
        fun abrirLogin(actividad: Activity, mensaje: String? = null, esError: Boolean = true) {
            actividad.startActivity(
                Intent(actividad, LoginActivity::class.java)
                    .putExtra(EXTRA_MENSAJE, mensaje)
                    .putExtra(EXTRA_ES_ERROR, esError)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            actividad.finish()
        }

        /** Sesión recibida de la pantalla anterior, o null si no llegó. */
        fun leerSesion(intent: Intent): SesionModelo? =
            IntentCompat.getSerializableExtra(intent, EXTRA_SESION, SesionModelo::class.java)
    }

    private lateinit var controlador: MenuControlador
    private lateinit var sesion: SesionModelo
    private var cargando by mutableStateOf(false)
    private var unidades by mutableIntStateOf(0)

    // La pantalla de venta devuelve el carrito como quedó, así la venta se conserva
    private val abrirPantallaVenta = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
        resultado.data?.let { datos ->
            IntentCompat.getSerializableExtra(datos, VentaActivity.EXTRA_CARRITO, CarritoModelo::class.java)
                ?.let { controlador.actualizarVenta(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recibida = leerSesion(intent)
        if (recibida == null) {
            abrirLogin(this, "Inicia sesión para continuar.")
            return
        }
        sesion = recibida
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(ColorAndroid.TRANSPARENT))
        controlador = MenuControlador(this, AutenticacionRestService(), sesion)
        setContent { TemaMonsterCell { Pantalla() } }
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
    }

    fun mostrarVenta(sesion: SesionModelo, carrito: CarritoModelo) {
        abrirPantallaVenta.launch(
            Intent(this, VentaActivity::class.java)
                .putExtra(EXTRA_SESION, sesion)
                .putExtra(VentaActivity.EXTRA_CARRITO, carrito)
        )
    }

    fun mostrarFacturas(sesion: SesionModelo) {
        startActivity(Intent(this, FacturasActivity::class.java).putExtra(EXTRA_SESION, sesion))
    }

    fun mostrarTelefonos(sesion: SesionModelo) {
        startActivity(Intent(this, TelefonosActivity::class.java).putExtra(EXTRA_SESION, sesion))
    }

    fun mostrarClientes(sesion: SesionModelo) {
        startActivity(Intent(this, ClientesActivity::class.java).putExtra(EXTRA_SESION, sesion))
    }

    fun mostrarSimulador(sesion: SesionModelo) {
        startActivity(Intent(this, SimuladorActivity::class.java).putExtra(EXTRA_SESION, sesion))
    }

    fun mostrarUnidades(cantidad: Int) {
        unidades = cantidad
    }

    fun volverAlLogin(mensaje: String, esError: Boolean) {
        abrirLogin(this, mensaje, esError)
    }

    // ---- Interfaz ----

    private class Opcion(
        val titulo: String,
        val descripcion: String,
        val icono: Int,
        val acento: Boolean,
        val accion: () -> Unit
    )

    @Composable
    private fun Pantalla() {
        val ventas = listOf(
            Opcion(
                "Nueva venta",
                if (unidades == 0) "Verifica al comprador, arma la venta y factura."
                else "Venta en curso: $unidades ${if (unidades == 1) "unidad" else "unidades"}.",
                R.drawable.ic_carrito, unidades > 0
            ) { controlador.abrirVenta() },
            Opcion("Facturas", "Ventas emitidas, las más recientes primero.",
                R.drawable.ic_factura, false) { controlador.abrirFacturas() }
        )
        val catalogo = listOf(
            Opcion("Teléfonos", "Agrega, edita o da de baja los modelos a la venta.",
                R.drawable.ic_catalogo, false) { controlador.abrirTelefonos() }
        )
        val credito = listOf(
            Opcion("Consultar cliente", "Evaluación y créditos de un cliente.",
                R.drawable.ic_credito, false) { controlador.abrirClientes() },
            Opcion("Simulador de cuotas", "16.5 % anual, de 3 a 18 cuotas.",
                R.drawable.ic_calculadora, false) { controlador.abrirSimulador() }
        )

        Column(Modifier.fillMaxSize().background(Superficie)) {
            BarraMarca()
            ConAncho(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(Espacio.m)
            ) { clase ->
                val columnas = when (clase) {
                    ClaseAncho.COMPACTO -> 1
                    ClaseAncho.MEDIO -> 2
                    ClaseAncho.AMPLIO -> 3
                }
                ContenidoCentrado(Ancho.CONTENIDO) {
                    Column {
                        Sobretitulo("Usuario ${valorO(sesion.usuario)}")
                        Text("Hola, ${if (sesion.nombre.isNullOrBlank()) "vendedor" else sesion.nombre}", color = Texto,
                            style = MaterialTheme.typography.headlineSmall)
                    }
                    Row {
                        Chip(
                            "Sesión activa hasta las ${horaLocal(sesion.expiraEn)} · se renueva con cada operación",
                            CianSuave, CianTextoOscuro, icono = R.drawable.ic_info, colorIcono = CianTexto
                        )
                    }
                    Spacer(Modifier.height(Espacio.xs))
                    Sobretitulo("Ventas")
                    Cuadricula(ventas, columnas)
                    Spacer(Modifier.height(Espacio.xs))
                    Sobretitulo("Catálogo")
                    Cuadricula(catalogo, columnas)
                    Spacer(Modifier.height(Espacio.xs))
                    Sobretitulo("Crédito BanQuito")
                    Cuadricula(credito, columnas)
                }
            }
        }
    }

    /** Marca sobre #0B1D44, como la barra lateral del cliente web, con Salir a la derecha. */
    @Composable
    private fun BarraMarca() {
        Surface(color = AzulOscuro) {
            ContenidoCentrado(Ancho.CONTENIDO) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = Espacio.m, vertical = Espacio.s),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconoEnCaja(R.drawable.ic_celular, AzulAccion, Blanco, Espacio.xl + Espacio.xs)
                    Spacer(Modifier.width(Espacio.s))
                    Column(Modifier.weight(1f)) {
                        Text("MonsterCell", color = Blanco, style = MaterialTheme.typography.titleLarge)
                        Text("Punto de venta · Grupo 03", color = BordeSuave, style = MaterialTheme.typography.bodySmall)
                    }
                    BotonPeligro("Salir", { controlador.cerrarSesion() }, icono = R.drawable.ic_salir,
                        habilitado = !cargando)
                }
            }
        }
    }

    /** Distribuye las tarjetas en filas de n columnas del mismo ancho. */
    @Composable
    private fun Cuadricula(opciones: List<Opcion>, columnas: Int) {
        Column(verticalArrangement = Arrangement.spacedBy(Espacio.m)) {
            opciones.chunked(columnas).forEach { fila ->
                Row(horizontalArrangement = Arrangement.spacedBy(Espacio.m)) {
                    fila.forEach { opcion ->
                        TarjetaAccion(
                            titulo = opcion.titulo,
                            descripcion = opcion.descripcion,
                            icono = opcion.icono,
                            alPulsar = opcion.accion,
                            modifier = Modifier.weight(1f),
                            acento = opcion.acento,
                            habilitado = !cargando
                        )
                    }
                    repeat(columnas - fila.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

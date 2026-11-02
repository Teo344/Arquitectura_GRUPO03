package ec.edu.monster.vista

import android.app.Activity
import android.content.Intent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.IntentCompat
import ec.edu.monster.R
import ec.edu.monster.controlador.MenuControlador
import ec.edu.monster.modelo.SesionModelo
import ec.edu.monster.modelo.TipoTransaccion
import ec.edu.monster.servicio.AutenticacionRestService

/** Vista del menú principal: muestra la sesión y delega cada opción al MenuControlador. */
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
    }

    fun mostrarCuenta(sesion: SesionModelo, verMovimientos: Boolean) {
        startActivity(
            Intent(this, CuentaActivity::class.java)
                .putExtra(EXTRA_SESION, sesion)
                .putExtra(CuentaActivity.EXTRA_MOVIMIENTOS, verMovimientos)
        )
    }

    fun mostrarTransaccion(sesion: SesionModelo, tipo: TipoTransaccion) {
        startActivity(
            Intent(this, TransaccionActivity::class.java)
                .putExtra(EXTRA_SESION, sesion)
                .putExtra(TransaccionActivity.EXTRA_TIPO, tipo.name)
        )
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
        val consultas = listOf(
            Opcion("Consultar cuenta", "Titular, moneda, saldo y estado de una cuenta.",
                R.drawable.ic_cuenta, false) { controlador.abrirConsultaCuenta() },
            Opcion("Movimientos", "Historial de ingresos y salidas de una cuenta.",
                R.drawable.ic_movimientos, false) { controlador.abrirMovimientos() }
        )
        val transacciones = listOf(
            Opcion("Depósito", "Suma un importe al saldo de una cuenta.",
                R.drawable.ic_deposito, false) { controlador.abrirTransaccion(TipoTransaccion.DEPOSITO) },
            Opcion("Retiro", "Descuenta un importe con saldo suficiente.",
                R.drawable.ic_retiro, false) { controlador.abrirTransaccion(TipoTransaccion.RETIRO) },
            Opcion("Transferencia", "Mueve fondos entre cuentas de la misma moneda.",
                R.drawable.ic_transferencia, true) { controlador.abrirTransaccion(TipoTransaccion.TRANSFERENCIA) }
        )

        Column(Modifier.fillMaxSize().background(Superficie)) {
            BarraSuperior(
                titulo = "Hola, ${sesion.nombreEmpleado ?: "empleado"}",
                subtitulo = "Sucursal ${sesion.codigoSucursal ?: "-"} · ${sesion.sucursal ?: "-"}",
                accion = {
                    BotonPeligro("Salir", { controlador.cerrarSesion() }, icono = R.drawable.ic_salir,
                        habilitado = !cargando)
                }
            )
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Sobretitulo("Panel de operaciones")
                            Text("¿Qué deseas hacer hoy?", color = Texto,
                                style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                    Row {
                        Chip(
                            "Sesión activa hasta las ${horaLocal(sesion.expiraEn)} · se renueva con cada operación",
                            CianSuave, CianTextoOscuro, icono = R.drawable.ic_info, colorIcono = CianTexto
                        )
                    }
                    Spacer(Modifier.height(Espacio.xs))
                    TituloSeccion("Consultas", "Información de cuentas")
                    Cuadricula(consultas, columnas)
                    Spacer(Modifier.height(Espacio.xs))
                    TituloSeccion("Transacciones", "Registrar una operación")
                    Cuadricula(transacciones, columnas)
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

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import ec.edu.monster.R
import ec.edu.monster.controlador.MenuControlador
import ec.edu.monster.modelo.SesionUsuario

/** Vista del menú principal: delega cada opción al MenuControlador. */
class MenuActivity : ComponentActivity() {

    companion object {
        const val EXTRA_SESION = "sesion"
        const val EXTRA_MENSAJE = "mensaje"

        /** Cierra todas las pantallas y vuelve al login, opcionalmente con un mensaje. */
        fun abrirLogin(actividad: Activity, mensaje: String? = null) {
            actividad.startActivity(
                Intent(actividad, LoginActivity::class.java)
                    .putExtra(EXTRA_MENSAJE, mensaje)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            actividad.finish()
        }
    }

    private lateinit var controlador: MenuControlador
    private lateinit var sesion: SesionUsuario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recibida = IntentCompat.getSerializableExtra(intent, EXTRA_SESION, SesionUsuario::class.java)
        if (recibida == null) {
            volverAlLogin()
            return
        }
        sesion = recibida
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(ColorAndroid.TRANSPARENT))
        controlador = MenuControlador(this, sesion)
        setContent { TemaMonster { Pantalla() } }
    }

    // ---- Métodos que usa el controlador ----

    fun mostrarConversion(sesion: SesionUsuario) {
        startActivity(Intent(this, ConversionActivity::class.java).putExtra(EXTRA_SESION, sesion))
    }

    fun mostrarCatalogo(sesion: SesionUsuario) {
        startActivity(Intent(this, CatalogoActivity::class.java).putExtra(EXTRA_SESION, sesion))
    }

    fun volverAlLogin() {
        abrirLogin(this)
    }

    // ---- Interfaz ----

    @Composable
    private fun Pantalla() {
        Scaffold(
            containerColor = Superficie,
            topBar = {
                BarraSuperior("MONSTER", "Cliente móvil REST · " + sesion.usuario, accion = {
                    BotonPeligro("Cerrar sesión", { controlador.cerrarSesion() }, icono = R.drawable.ic_salir)
                })
            }
        ) { relleno ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(relleno)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Sobretitulo("Monster", Modifier.align(Alignment.CenterHorizontally))
                Text(
                    "¿Qué deseas hacer?",
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    color = AzulOscuro,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    "Elige una opción para continuar",
                    modifier = Modifier.fillMaxWidth(),
                    color = TextoSecundario,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(24.dp))

                TarjetaOpcion(
                    numero = "01",
                    titulo = "Convertir unidades",
                    descripcion = "Realiza conversiones de longitud, masa y temperatura.",
                    icono = R.drawable.ic_convertir
                ) {
                    BotonPrimario("Abrir conversor", { controlador.abrirConversion() })
                }
                Spacer(Modifier.height(16.dp))
                TarjetaOpcion(
                    numero = "02",
                    titulo = "Consultar catálogo",
                    descripcion = "Explora las categorías y unidades disponibles en el sistema.",
                    icono = R.drawable.ic_catalogo
                ) {
                    BotonSecundario("Ver catálogo", { controlador.abrirCatalogo() })
                }

                Text(
                    "Conversión de Unidades Monster",
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    color = TextoSecundario,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    @Composable
    private fun TarjetaOpcion(
        numero: String,
        titulo: String,
        descripcion: String,
        icono: Int,
        boton: @Composable () -> Unit
    ) {
        Tarjeta {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).background(CianSuave, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(icono), contentDescription = null, tint = CianTexto)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(numero, color = CianTexto, style = MaterialTheme.typography.labelMedium)
                    Text(titulo, color = Texto, style = MaterialTheme.typography.titleLarge)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(descripcion, color = TextoSecundario, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            boton()
        }
    }
}

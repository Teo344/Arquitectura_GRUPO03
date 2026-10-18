package ec.edu.monster.vista

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as ColorAndroid
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ec.edu.monster.controlador.LoginControlador
import ec.edu.monster.modelo.SesionUsuario
import ec.edu.monster.servicio.AutenticacionRestService

/** Vista de acceso: solo muestra datos y delega las acciones al LoginControlador. */
class LoginActivity : ComponentActivity() {

    private lateinit var controlador: LoginControlador

    private var usuario by mutableStateOf("")
    private var clave by mutableStateOf("")
    private var mensaje by mutableStateOf<String?>(null)
    private var mensajeEsError by mutableStateOf(false)
    private var cargando by mutableStateOf(false)

    // Android 17+ bloquea la red local (donde está el servidor) sin este permiso
    private val solicitarPermisoRedLocal =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
            if (concedido) {
                controlador.iniciarSesion(usuario, clave)
            } else {
                mostrarMensaje("Permite el acceso a la red local para conectar con el servidor.", true)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(ColorAndroid.TRANSPARENT))
        controlador = LoginControlador(this, AutenticacionRestService())
        intent.getStringExtra(MenuActivity.EXTRA_MENSAJE)?.let { mostrarMensaje(it, true) }
        setContent { TemaMonster { Pantalla() } }
    }

    override fun onDestroy() {
        controlador.detener()
        super.onDestroy()
    }

    // ---- Métodos que usa el controlador ----

    fun mostrarMensaje(texto: String, esError: Boolean) {
        mensaje = texto
        mensajeEsError = esError
    }

    fun setProcesando(procesando: Boolean) {
        cargando = procesando
    }

    fun limpiarClave() {
        clave = ""
    }

    fun abrirMenu(sesion: SesionUsuario) {
        startActivity(Intent(this, MenuActivity::class.java).putExtra(MenuActivity.EXTRA_SESION, sesion))
        finish()
    }

    private fun iniciarSesion() {
        if (Build.VERSION.SDK_INT >= 37 &&
            checkSelfPermission(PERMISO_RED_LOCAL) != PackageManager.PERMISSION_GRANTED
        ) {
            solicitarPermisoRedLocal.launch(PERMISO_RED_LOCAL)
            return
        }
        controlador.iniciarSesion(usuario, clave)
    }

    private companion object {
        const val PERMISO_RED_LOCAL = "android.permission.ACCESS_LOCAL_NETWORK"
    }

    // ---- Interfaz ----

    @Composable
    private fun Pantalla() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Superficie)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AzulOscuro)
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Text("MONSTER", color = BordeSuave, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                Text("Convierte unidades", color = Blanco, style = MaterialTheme.typography.headlineSmall)
                Text("de forma sencilla y segura", color = BordeSuave, style = MaterialTheme.typography.bodyMedium)
            }

            Tarjeta(Modifier.padding(16.dp)) {
                Sobretitulo("Acceso seguro", Modifier.align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(8.dp))
                Text(
                    "Sistema de conversión de unidades",
                    modifier = Modifier.fillMaxWidth(),
                    color = AzulOscuro,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    "GRUPO #3",
                    modifier = Modifier.fillMaxWidth(),
                    color = CianTexto,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(24.dp))
                Text("¡Bienvenido!", color = Texto, style = MaterialTheme.typography.titleLarge)
                Text("Inicia sesión para convertir tus unidades", color = TextoSecundario,
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(24.dp))

                CampoTexto("Usuario", usuario, { usuario = it }, habilitado = !cargando)
                Spacer(Modifier.height(16.dp))
                CampoTexto(
                    etiqueta = "Contraseña",
                    valor = clave,
                    alCambiar = { clave = it },
                    esClave = true,
                    accionTeclado = ImeAction.Done,
                    alConfirmar = { iniciarSesion() },
                    habilitado = !cargando
                )
                Spacer(Modifier.height(24.dp))
                BotonPrimario("Iniciar sesión", { iniciarSesion() }, cargando = cargando)

                mensaje?.let {
                    Spacer(Modifier.height(16.dp))
                    Alerta(it, mensajeEsError)
                }
            }

            Text(
                "Cliente móvil Android REST listo para conversión.",
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                color = TextoSecundario,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

package ec.edu.monster.vista

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as ColorAndroid
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ec.edu.monster.R
import ec.edu.monster.controlador.LoginControlador
import ec.edu.monster.modelo.SesionModelo
import ec.edu.monster.servicio.AutenticacionRestService

/** Vista de acceso (CU-01): solo muestra datos y delega las acciones al LoginControlador. */
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
        intent.getStringExtra(MenuActivity.EXTRA_MENSAJE)?.let {
            mostrarMensaje(it, intent.getBooleanExtra(MenuActivity.EXTRA_ES_ERROR, true))
        }
        setContent { TemaMonsterCell { Pantalla() } }
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

    fun abrirMenu(sesion: SesionModelo) {
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
        ConAncho(Modifier.fillMaxSize().background(Superficie)) { clase ->
            if (clase == ClaseAncho.COMPACTO) {
                // Teléfono: foto arriba y la tarjeta de acceso montada sobre su borde inferior
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .navigationBarsPadding()
                ) {
                    PanelMarca(Modifier.fillMaxWidth().height(320.dp), compacto = true)
                    Box(
                        Modifier
                            .offset(y = (-40).dp)
                            .padding(horizontal = Espacio.m)
                    ) {
                        Tarjeta { FormularioAcceso() }
                    }
                    PiePagina(Modifier.offset(y = (-24).dp))
                }
            } else {
                // Tablet y escritorio: dos paneles, la foto a la izquierda y el formulario a la derecha
                Row(Modifier.fillMaxSize()) {
                    PanelMarca(
                        Modifier
                            .weight(if (clase == ClaseAncho.AMPLIO) 1.1f else 1f)
                            .fillMaxHeight(),
                        compacto = false
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .imePadding()
                            .navigationBarsPadding()
                            .statusBarsPadding()
                            .padding(Espacio.xl),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(Modifier.widthIn(max = Ancho.ACCESO)) {
                            Tarjeta { FormularioAcceso() }
                            PiePagina()
                        }
                    }
                }
            }
        }
    }

    /** Foto del grupo con un velo sólido azul oscuro (sin degradados) y los textos de marca. */
    @Composable
    private fun PanelMarca(modifier: Modifier, compacto: Boolean) {
        Box(modifier.background(AzulOscuro)) {
            Image(
                painter = painterResource(R.drawable.foto_login),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                alignment = BiasAlignment(0f, -0.35f)
            )
            Box(Modifier.matchParentSize().background(AzulOscuro.copy(alpha = 0.55f)))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(
                        start = Espacio.l, end = Espacio.l, top = Espacio.l,
                        bottom = if (compacto) Espacio.xxxl else Espacio.xl
                    ),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(36.dp).background(Blanco, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(painterResource(R.drawable.ic_celular), contentDescription = null,
                            tint = AzulAccion, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(Espacio.s))
                    Text("MONSTERCELL", color = Blanco, style = MaterialTheme.typography.labelMedium)
                }
                Column {
                    Text(
                        "Celulares con crédito directo,\nsimple y seguro",
                        color = Blanco,
                        style = if (compacto) MaterialTheme.typography.headlineSmall
                        else MaterialTheme.typography.headlineMedium
                    )
                    Spacer(Modifier.height(Espacio.xs))
                    Text(
                        "Vende en efectivo con 42 % de descuento o a crédito con BanQuito, con la tabla de cuotas al instante.",
                        color = BordeSuave,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!compacto) {
                        Spacer(Modifier.height(Espacio.m))
                        Row(horizontalArrangement = Arrangement.spacedBy(Espacio.xs)) {
                            Chip("REST", CianSuave, CianTextoOscuro)
                            Chip("Java", AzulSuave, AzulHover)
                            Chip("Grupo 03", AzulSuave, AzulHover)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun FormularioAcceso() {
        Sobretitulo("Acceso de empleados")
        Spacer(Modifier.height(Espacio.xs))
        Text("Iniciar sesión", color = Texto, style = MaterialTheme.typography.headlineSmall)
        Text("Ingresa con tu usuario de la comercializadora.", color = TextoSecundario,
            style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(Espacio.l))

        CampoTexto(
            etiqueta = "Usuario",
            valor = usuario,
            alCambiar = { usuario = it },
            marcador = "Ej. MONSTER",
            habilitado = !cargando
        )
        Spacer(Modifier.height(Espacio.m))
        CampoTexto(
            etiqueta = "Contraseña",
            valor = clave,
            alCambiar = { clave = it },
            esClave = true,
            accionTeclado = ImeAction.Done,
            alConfirmar = { iniciarSesion() },
            habilitado = !cargando
        )
        Spacer(Modifier.height(Espacio.l))
        BotonPrimario("Ingresar", { iniciarSesion() }, cargando = cargando)

        mensaje?.let {
            Spacer(Modifier.height(Espacio.m))
            Alerta(it, mensajeEsError)
        }
    }

    @Composable
    private fun PiePagina(modifier: Modifier = Modifier) {
        Text(
            "Cliente móvil · Comercializadora REST · Grupo 03",
            modifier = modifier.fillMaxWidth().padding(Espacio.m),
            color = TextoSecundario,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

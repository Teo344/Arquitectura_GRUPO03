package ec.edu.monster.vista

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import ec.edu.monster.R

// Identidad visual "Azul" (template.txt): colores, tipografía y formas de todas las pantallas.

// ---- Colores ----
val AzulAccion = Color(0xFF1D56CC)
val AzulHover = Color(0xFF1744A3)
val AzulSuave = Color(0xFFEDF4FF)
val BordeSuave = Color(0xFFB8D2FF)
val AzulOscuro = Color(0xFF0B1D44)

val Cian = Color(0xFF12A9C9)
val CianSuave = Color(0xFFE8F9FC)
val CianTexto = Color(0xFF0B6B83)
val CianTextoOscuro = Color(0xFF08404F)

val Texto = Color(0xFF141C31)
val TextoSecundario = Color(0xFF4A5A78)
val Placeholder = Color(0xFF627393)
val BordeCampo = Color(0xFFB1BFD4)
val Divisor = Color(0xFFD3DCEA)
val FondoSeccion = Color(0xFFE9EEF6)
val Superficie = Color(0xFFF5F8FC)
val Blanco = Color(0xFFFFFFFF)
val DeshabilitadoTexto = Color(0xFF8798B3)

val ErrorRelleno = Color(0xFFC0392B)
val ErrorTexto = Color(0xFFC0392B)
val ErrorFondo = Color(0xFFFCEBE8)
val InfoRelleno = Color(0xFF2F6FEB)
val InfoTexto = Color(0xFF1744A3)
val InfoFondo = Color(0xFFEDF4FF)

private val EsquemaColores = lightColorScheme(
    primary = AzulAccion,
    onPrimary = Blanco,
    primaryContainer = AzulSuave,
    onPrimaryContainer = AzulHover,
    secondary = Cian,
    onSecondary = CianTextoOscuro,
    secondaryContainer = CianSuave,
    onSecondaryContainer = CianTexto,
    tertiary = Cian,
    onTertiary = CianTextoOscuro,
    background = Blanco,
    onBackground = Texto,
    surface = Blanco,
    onSurface = Texto,
    surfaceVariant = FondoSeccion,
    onSurfaceVariant = TextoSecundario,
    surfaceContainerLowest = Blanco,
    surfaceContainerLow = Superficie,
    surfaceContainer = Blanco,
    surfaceContainerHigh = Superficie,
    surfaceContainerHighest = FondoSeccion,
    outline = BordeCampo,
    outlineVariant = Divisor,
    error = ErrorRelleno,
    onError = Blanco,
    errorContainer = ErrorFondo,
    onErrorContainer = ErrorTexto
)

// ---- Tipografía: Plus Jakarta Sans (una sola familia) ----
private val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold)
)

private fun estilo(tamano: Int, linea: Int, peso: FontWeight, espaciado: Double = 0.0) = TextStyle(
    fontFamily = PlusJakartaSans,
    fontSize = tamano.sp,
    lineHeight = linea.sp,
    fontWeight = peso,
    letterSpacing = espaciado.em
)

private val TipografiaMonster = Typography(
    displayLarge = estilo(56, 60, FontWeight.ExtraBold, -0.02),   // Display
    headlineLarge = estilo(44, 50, FontWeight.ExtraBold, -0.02),  // H1
    headlineMedium = estilo(34, 40, FontWeight.Bold, -0.015),     // H2
    headlineSmall = estilo(26, 32, FontWeight.Bold, -0.01),       // H3
    titleLarge = estilo(20, 28, FontWeight.Bold),                 // H4
    titleMedium = estilo(18, 28, FontWeight.Normal),              // Texto grande
    bodyLarge = estilo(16, 24, FontWeight.Normal),                // Texto base
    bodyMedium = estilo(14, 20, FontWeight.Normal),               // Texto pequeño
    bodySmall = estilo(12, 16, FontWeight.Medium),                // Leyenda
    labelLarge = estilo(15, 20, FontWeight.SemiBold),             // Botón
    labelMedium = estilo(12, 16, FontWeight.Bold, 0.08),          // Sobretítulo
    labelSmall = estilo(12, 16, FontWeight.Medium)
)

// ---- Formas: 6 chips, 10 botones y campos, 16 tarjetas, 24 paneles ----
private val FormasMonster = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun TemaMonster(contenido: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaColores,
        typography = TipografiaMonster,
        shapes = FormasMonster,
        content = contenido
    )
}

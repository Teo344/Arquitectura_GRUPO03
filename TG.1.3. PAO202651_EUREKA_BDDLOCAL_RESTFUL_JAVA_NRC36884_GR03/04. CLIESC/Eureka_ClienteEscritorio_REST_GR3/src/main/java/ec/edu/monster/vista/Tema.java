package ec.edu.monster.vista;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Identidad visual "Azul" del cliente móvil de TG.1.3 (vista/Tema.kt) llevada
 * a Swing: mismos colores, tipografía, formas y espaciado, para que todos los
 * clientes de EurekaBank se vean iguales.
 */
public final class Tema {

    private Tema() {
    }

    // ---- Colores: primario (Azul) ----
    public static final Color AZUL_MARCA = new Color(0x2F6FEB);
    public static final Color AZUL_ACCION = new Color(0x1D56CC);
    public static final Color AZUL_HOVER = new Color(0x1744A3);
    public static final Color AZUL_PRESIONADO = new Color(0x153A82);
    public static final Color AZUL_SUAVE = new Color(0xEDF4FF);
    public static final Color BORDE_SUAVE = new Color(0xB8D2FF);
    public static final Color AZUL_OSCURO = new Color(0x0B1D44);
    public static final Color AZUL_FOCO = new Color(0x5B93F7);

    // ---- Acento (Cian): detalles e indicadores, máximo ~10 % de la pantalla ----
    public static final Color CIAN = new Color(0x12A9C9);
    public static final Color CIAN_SUAVE = new Color(0xE8F9FC);
    public static final Color CIAN_TEXTO = new Color(0x0B6B83);
    public static final Color CIAN_TEXTO_OSCURO = new Color(0x08404F);

    // ---- Neutros (Slate azulado) ----
    public static final Color TEXTO = new Color(0x141C31);
    public static final Color TEXTO_SECUNDARIO = new Color(0x4A5A78);
    public static final Color PLACEHOLDER = new Color(0x627393);
    public static final Color BORDE_CAMPO = new Color(0xB1BFD4);
    public static final Color DIVISOR = new Color(0xD3DCEA);
    public static final Color FONDO_SECCION = new Color(0xE9EEF6);
    public static final Color SUPERFICIE = new Color(0xF5F8FC);
    public static final Color BLANCO = Color.WHITE;
    public static final Color DESHABILITADO_TEXTO = new Color(0x8798B3);

    // ---- Semánticos: relleno (iconos, bordes) / texto / fondo ----
    public static final Color EXITO_RELLENO = new Color(0x138A5B);
    public static final Color EXITO_TEXTO = new Color(0x0F7A52);
    public static final Color EXITO_FONDO = new Color(0xE7F6EF);
    public static final Color ADVERTENCIA_RELLENO = new Color(0xF5A524);
    public static final Color ADVERTENCIA_TEXTO = new Color(0x8A5300);
    public static final Color ADVERTENCIA_FONDO = new Color(0xFFF4DE);
    public static final Color ERROR_RELLENO = new Color(0xC0392B);
    public static final Color ERROR_TEXTO = new Color(0xC0392B);
    public static final Color ERROR_FONDO = new Color(0xFCEBE8);
    public static final Color INFO_RELLENO = new Color(0x2F6FEB);
    public static final Color INFO_TEXTO = new Color(0x1744A3);
    public static final Color INFO_FONDO = new Color(0xEDF4FF);

    // Tinte de las sombras (#12306A)
    public static final Color TINTE_SOMBRA = new Color(0x12306A);

    // ---- Tipografía: Plus Jakarta Sans (una sola familia), empaquetada en /fuentes ----
    private static final Font REGULAR = cargar("plus_jakarta_sans_regular.ttf", Font.PLAIN);
    private static final Font MEDIA = cargar("plus_jakarta_sans_medium.ttf", Font.PLAIN);
    private static final Font SEMINEGRITA = cargar("plus_jakarta_sans_semibold.ttf", Font.BOLD);
    private static final Font NEGRITA = cargar("plus_jakarta_sans_bold.ttf", Font.BOLD);
    private static final Font EXTRANEGRITA = cargar("plus_jakarta_sans_extrabold.ttf", Font.BOLD);

    public static final Font TITULAR_MEDIANO = NEGRITA.deriveFont(34f);     // H2
    public static final Font TITULAR_PEQUENO = NEGRITA.deriveFont(26f);     // H3
    public static final Font TITULO = NEGRITA.deriveFont(20f);              // H4
    public static final Font CUERPO_GRANDE = REGULAR.deriveFont(16f);       // Texto base
    public static final Font CUERPO = REGULAR.deriveFont(14f);              // Texto pequeño
    public static final Font LEYENDA = MEDIA.deriveFont(12f);               // Leyenda
    public static final Font BOTON = SEMINEGRITA.deriveFont(15f);           // Botón
    public static final Font SOBRETITULO = NEGRITA.deriveFont(              // Sobretítulo
            Map.of(TextAttribute.SIZE, 12f, TextAttribute.TRACKING, 0.08f));

    public static final Font CUERPO_GRANDE_SEMINEGRITA = SEMINEGRITA.deriveFont(16f);
    public static final Font CUERPO_GRANDE_NEGRITA = NEGRITA.deriveFont(16f);
    public static final Font BOTON_NEGRITA = NEGRITA.deriveFont(15f);
    public static final Font BOTON_MEDIA = MEDIA.deriveFont(15f);
    public static final Font MARCA = EXTRANEGRITA.deriveFont(
            Map.of(TextAttribute.SIZE, 12f, TextAttribute.TRACKING, 0.08f));

    // ---- Formas: 6 chips, 10 botones y campos, 16 tarjetas, 24 paneles ----
    public static final int RADIO_CHIP = 6;
    public static final int RADIO_CAMPO = 10;
    public static final int RADIO_TARJETA = 16;
    public static final int RADIO_PANEL = 24;

    // ---- Espaciado en base 4: 4, 8, 12, 16, 24, 32, 48, 64 ----
    public static final int XXS = 4;
    public static final int XS = 8;
    public static final int S = 12;
    public static final int M = 16;
    public static final int L = 24;
    public static final int XL = 32;
    public static final int XXL = 48;
    public static final int XXXL = 64;

    // ---- Anchos máximos (mismos valores que el móvil en pantallas amplias) ----
    public static final int ANCHO_FORMULARIO = 560;
    public static final int ANCHO_ACCESO = 440;
    public static final int ANCHO_CONTENIDO = 1080;

    /** Si la fuente no se puede leer se usa la del sistema para no impedir el arranque. */
    private static Font cargar(String archivo, int estiloAlternativo) {
        try (InputStream entrada = Tema.class.getResourceAsStream("/fuentes/" + archivo)) {
            if (entrada != null) {
                return Font.createFont(Font.TRUETYPE_FONT, entrada);
            }
        } catch (IOException | FontFormatException ignorada) {
            // Se usa la fuente alternativa
        }
        return new Font(Font.SANS_SERIF, estiloAlternativo, 12);
    }
}

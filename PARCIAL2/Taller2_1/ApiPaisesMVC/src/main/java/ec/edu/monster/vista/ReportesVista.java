package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import ec.edu.monster.controlador.ReporteControlador;
import ec.edu.monster.modelo.entidad.EstadisticasPaises;
import ec.edu.monster.modelo.entidad.Pais;
import ec.edu.monster.modelo.entidad.Region;
import ec.edu.monster.vista.Componentes.Alerta;
import ec.edu.monster.vista.Componentes.Boton;
import ec.edu.monster.vista.Componentes.Caja;
import ec.edu.monster.vista.Componentes.EstiloBoton;
import ec.edu.monster.vista.Componentes.IconoEnCaja;
import ec.edu.monster.vista.Componentes.Pila;
import ec.edu.monster.vista.Componentes.Texto;
import ec.edu.monster.vista.Componentes.TipoAlerta;
import ec.edu.monster.vista.GraficoBarras.Barra;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Reporte estadístico (CU-08, Actividad 5): tres indicadores con la respuesta
 * directa a cada pregunta del taller y el gráfico de países por región.
 *
 * @author carlo
 */
public class ReportesVista extends JPanel {

    private static final String VISTA_DATOS = "datos";
    private static final String VISTA_VACIA = "vacia";

    private final ReporteControlador controlador;
    private final Boton actualizar = new Boton("Actualizar", EstiloBoton.SECUNDARIO, Iconos.Nombre.ACTUALIZAR);
    private final Alerta alerta = new Alerta(true);
    private final Indicador total = new Indicador(Iconos.Nombre.PUBLICO, "Países registrados");
    private final Indicador masPoblado = new Indicador(Iconos.Nombre.PERSONAS, "País más poblado");
    private final Indicador regionMayor = new Indicador(Iconos.Nombre.UBICACION, "Región con más países");
    private final GraficoBarras grafico = new GraficoBarras();
    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);

    public ReportesVista(ReporteControlador controlador, Runnable irAPaises) {
        super(new BorderLayout(0, M));
        this.controlador = controlador;
        setBackground(SUPERFICIE);
        setBorder(new EmptyBorder(L, L, M, L));
        actualizar.addActionListener(e -> cargar());

        add(new Pila()
                .agregar(Componentes.encabezadoPagina("Actividad 5", "Reportes",
                        new Texto("Estadísticas calculadas con los países guardados en MySQL.", CUERPO,
                                TEXTO_SECUNDARIO), actualizar))
                .agregar(alerta, M), BorderLayout.NORTH);

        Boton irPaises = new Boton("Ir a Países", EstiloBoton.SECUNDARIO, Iconos.Nombre.TABLA);
        irPaises.addActionListener(e -> irAPaises.run());
        contenido.setOpaque(false);
        contenido.add(Componentes.desplazable(Componentes.centrado(datos(), Integer.MAX_VALUE, 0, false)), VISTA_DATOS);
        contenido.add(Componentes.enCentro(Componentes.estadoVacio(Iconos.Nombre.GRAFICO, "Todavía no hay datos",
                "Importa o registra países para ver las estadísticas.", irPaises)), VISTA_VACIA);
        add(contenido, BorderLayout.CENTER);
    }

    public void cargar() {
        alerta.ocultar();
        actualizar.setCargando(true, "Actualizando…");
        Tarea.de(controlador::generarReporte)
                .siempre(() -> actualizar.setCargando(false))
                .alTerminar(this::mostrar)
                .alFallar(mensaje -> alerta.mostrar(mensaje, TipoAlerta.ERROR))
                .iniciar();
    }

    private JComponent datos() {
        JPanel indicadores = new JPanel(new GridLayout(1, 3, M, 0));
        indicadores.setOpaque(false);
        indicadores.add(total);
        indicadores.add(masPoblado);
        indicadores.add(regionMayor);

        Caja tarjetaGrafico = Componentes.tarjeta();
        tarjetaGrafico.agregar(Componentes.etiqueta("Países por región", TITULO, TEXTO));
        tarjetaGrafico.agregar(new Texto("Cantidad de países guardados en cada región, de mayor a menor. Pasa el "
                + "ratón sobre una barra para ver su porcentaje.", CUERPO, TEXTO_SECUNDARIO), XXS);
        tarjetaGrafico.agregar(grafico, L);

        return new Pila().agregar(indicadores).agregar(tarjetaGrafico, S);
    }

    private void mostrar(EstadisticasPaises estadisticas) {
        if (estadisticas.estaVacio()) {
            cartas.show(contenido, VISTA_VACIA);
            return;
        }
        long cantidadTotal = estadisticas.totalPaises();
        total.setValor(Formato.entero(cantidadTotal),
                "En " + estadisticas.paisesPorRegion().size() + " regiones de la base api_paises.");

        Pais pais = estadisticas.paisMasPoblado();
        masPoblado.setValor(pais.nombre(), Formato.entero(pais.poblacion()) + " habitantes · "
                + pais.region().getNombre());

        Region primera = estadisticas.regionConMasPaises();
        long enRegion = estadisticas.paisesPorRegion().get(primera);
        // Si otras regiones tienen la misma cantidad, se dice: elegir una en silencio sería engañoso
        List<String> empatadas = estadisticas.paisesPorRegion().entrySet().stream()
                .filter(e -> e.getKey() != primera && e.getValue() == enRegion)
                .map(e -> e.getKey().getNombre())
                .toList();
        String contexto = Formato.paises(enRegion) + " · " + Formato.porcentaje((double) enRegion / cantidadTotal)
                + " del total";
        if (!empatadas.isEmpty()) {
            contexto += " · empate con " + String.join(", ", empatadas);
        }
        regionMayor.setValor(primera.getNombre(), contexto);

        grafico.setBarras(estadisticas.paisesPorRegion().entrySet().stream()
                .map(e -> new Barra(e.getKey().getNombre(), e.getValue()))
                .toList());
        cartas.show(contenido, VISTA_DATOS);
        revalidate();
        repaint();
    }

    /** Tarjeta de indicador: icono, pregunta, respuesta grande y un dato de contexto. */
    private static final class Indicador extends Caja {

        private final Texto valor = new Texto("—", TITULAR_PEQUENO, TEXTO);
        private final Texto detalle = new Texto("", CUERPO, TEXTO_SECUNDARIO);
        private final String etiqueta;

        Indicador(Iconos.Nombre icono, String etiqueta) {
            super(BLANCO, DIVISOR, RADIO_TARJETA, 4, Componentes.MARGEN_SOMBRA, L);
            this.etiqueta = etiqueta;
            JPanel cabecera = new JPanel(new BorderLayout(S, 0));
            cabecera.setOpaque(false);
            cabecera.add(new IconoEnCaja(icono, AZUL_SUAVE, AZUL_ACCION, 36, 20), BorderLayout.WEST);
            cabecera.add(Componentes.sobretitulo(etiqueta, TEXTO_SECUNDARIO), BorderLayout.CENTER);
            agregar(cabecera);
            agregar(valor, M);
            agregar(detalle, XXS);
        }

        void setValor(String texto, String contexto) {
            valor.setTexto(texto);
            detalle.setTexto(contexto);
            getAccessibleContext().setAccessibleName(etiqueta + ": " + texto + ". " + contexto);
        }
    }
}

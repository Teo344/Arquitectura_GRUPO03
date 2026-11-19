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
import ec.edu.monster.vista.Componentes.Pila;
import ec.edu.monster.vista.Componentes.Texto;
import ec.edu.monster.vista.Componentes.TipoAlerta;
import ec.edu.monster.vista.GraficoBarras.Barra;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Reporte estadístico (CU-08, Actividad 5): tres indicadores con la respuesta
 * directa a cada pregunta del taller; debajo, el gráfico de países por región
 * y el ranking de los cinco países más poblados.
 *
 * @author carlo
 */
public class ReportesVista extends JPanel {

    private static final String VISTA_DATOS = "datos";
    private static final String VISTA_VACIA = "vacia";

    private final ReporteControlador controlador;
    private final Boton actualizar = new Boton("Actualizar", EstiloBoton.SECUNDARIO, Iconos.Nombre.ACTUALIZAR);
    private final Alerta alerta = new Alerta(true);
    private final Indicador total = new Indicador("Países registrados", true);
    private final Indicador masPoblado = new Indicador("País más poblado", false);
    private final Indicador regionMayor = new Indicador("Región con más países", false);
    private final GraficoBarras grafico = new GraficoBarras();
    private final Pila ranking = new Pila();
    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);

    public ReportesVista(ReporteControlador controlador, Runnable irAPaises) {
        super(new BorderLayout(0, M));
        this.controlador = controlador;
        setBackground(SUPERFICIE);
        setBorder(new EmptyBorder(L, XL, M, XL));
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
        tarjetaGrafico.agregar(Componentes.etiqueta("Países por región", TITULO_TARJETA, TEXTO));
        tarjetaGrafico.agregar(new Texto("Cantidad de países guardados en cada región, de mayor a menor. Pasa el "
                + "ratón sobre una barra para ver su porcentaje.", PEQUENO, TEXTO_SECUNDARIO), XXS);
        tarjetaGrafico.agregar(grafico, L);

        Caja tarjetaRanking = Componentes.tarjeta();
        tarjetaRanking.agregar(Componentes.etiqueta("Los 5 más poblados", TITULO_TARJETA, TEXTO));
        tarjetaRanking.agregar(new Texto("Habitantes según los datos guardados de REST Countries.", PEQUENO,
                TEXTO_SECUNDARIO), XXS);
        tarjetaRanking.agregar(ranking, M);
        ranking.getAccessibleContext().setAccessibleName("Ranking de países más poblados");

        return new Pila()
                .agregar(indicadores)
                .agregar(Componentes.dosColumnas(tarjetaGrafico, tarjetaRanking, 0.55, XS, true), XS);
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

        ranking.removeAll();
        List<Pais> masPoblados = estadisticas.masPoblados();
        for (int i = 0; i < masPoblados.size(); i++) {
            ranking.agregar(new FilaRanking(i + 1, masPoblados.get(i), i < masPoblados.size() - 1));
        }
        cartas.show(contenido, VISTA_DATOS);
        revalidate();
        repaint();
    }

    /**
     * Tarjeta de indicador: pregunta, respuesta grande y un dato de contexto.
     * El primero va sobre azul oscuro para marcar el punto de entrada del reporte.
     */
    private static final class Indicador extends Caja {

        private final Texto valor;
        private final Texto detalle;
        private final String etiqueta;

        Indicador(String etiqueta, boolean destacado) {
            super(destacado ? AZUL_OSCURO : BLANCO, destacado ? null : DIVISOR, RADIO_TARJETA, destacado ? 0 : 4,
                    Componentes.MARGEN_SOMBRA, L);
            this.etiqueta = etiqueta;
            valor = new Texto("—", INDICADOR, destacado ? BLANCO : TEXTO);
            detalle = new Texto("", PEQUENO, destacado ? TEXTO_SOBRE_OSCURO : TEXTO_SECUNDARIO);
            agregar(Componentes.etiqueta(etiqueta, PEQUENO_SEMINEGRITA, destacado ? BORDE_SUAVE : TEXTO_SECUNDARIO));
            agregar(valor, XXS);
            agregar(detalle, XXS);
        }

        void setValor(String texto, String contexto) {
            valor.setTexto(texto);
            detalle.setTexto(contexto);
            getAccessibleContext().setAccessibleName(etiqueta + ": " + texto + ". " + contexto);
        }
    }

    /** Fila del ranking: posición, país, región y habitantes alineados a la derecha. */
    private static final class FilaRanking extends JPanel {

        private final boolean divisor;

        FilaRanking(int posicion, Pais pais, boolean divisor) {
            super(new BorderLayout(S, 0));
            this.divisor = divisor;
            setOpaque(false);
            setPreferredSize(new Dimension(0, 52));

            JLabel numero = Componentes.etiqueta(String.valueOf(posicion), PEQUENO_SEMINEGRITA, AZUL_HOVER);
            numero.setHorizontalAlignment(SwingConstants.CENTER);
            numero.setPreferredSize(new Dimension(28, 28));
            JPanel insignia = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    java.awt.Graphics2D g2 = Componentes.suave(g);
                    g2.setColor(AZUL_SUAVE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2.dispose();
                }
            };
            insignia.setOpaque(false);
            insignia.add(numero);
            add(Componentes.enCentro(insignia), BorderLayout.WEST);

            JPanel centro = new JPanel(new BorderLayout(XS, 0));
            centro.setOpaque(false);
            centro.add(Componentes.etiqueta(pais.nombre(), CUERPO_SEMINEGRITA, TEXTO), BorderLayout.CENTER);
            centro.add(Componentes.enCentro(Componentes.chip(pais.region().getNombre(), AZUL_SUAVE, AZUL_HOVER)),
                    BorderLayout.EAST);
            add(centro, BorderLayout.CENTER);

            JLabel habitantes = Componentes.etiqueta(Formato.entero(pais.poblacion()), CUERPO, TEXTO);
            habitantes.setHorizontalAlignment(SwingConstants.RIGHT);
            habitantes.setPreferredSize(new Dimension(128, 28));
            add(habitantes, BorderLayout.EAST);
            getAccessibleContext().setAccessibleName(posicion + ". " + pais.nombre() + ", "
                    + Formato.entero(pais.poblacion()) + " habitantes");
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (divisor) {
                g.setColor(new Color(FONDO_SECCION.getRGB()));
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        }
    }
}

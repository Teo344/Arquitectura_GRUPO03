package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import ec.edu.monster.controlador.PaisControlador;
import ec.edu.monster.modelo.entidad.OrigenDatos;
import ec.edu.monster.modelo.entidad.Pais;
import ec.edu.monster.modelo.entidad.ResultadoBusqueda;
import ec.edu.monster.vista.Componentes.Alerta;
import ec.edu.monster.vista.Componentes.Boton;
import ec.edu.monster.vista.Componentes.Caja;
import ec.edu.monster.vista.Componentes.CampoTexto;
import ec.edu.monster.vista.Componentes.EstiloBoton;
import ec.edu.monster.vista.Componentes.Pila;
import ec.edu.monster.vista.Componentes.Texto;
import ec.edu.monster.vista.Componentes.TipoAlerta;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Búsqueda de países por nombre en la API (CU-02). La consulta se hace solo al
 * pulsar "Buscar" (no mientras se escribe) porque cada consulta consume la
 * cuota mensual de la clave. Cada resultado indica si ya está guardado en
 * MySQL. Si la API falla, se muestra lo guardado y se avisa el origen.
 *
 * @author carlo
 */
public class BusquedaVista extends JPanel {

    private static final Logger LOG = Logger.getLogger(BusquedaVista.class.getName());
    private static final List<String> SUGERENCIAS = List.of("ecuador", "japan", "españa", "guinea");

    /** Resultado de la API y los códigos ya guardados ({@code null} si MySQL no respondió). */
    private record Busqueda(ResultadoBusqueda resultado, Set<String> guardados) {
    }

    private final PaisControlador controlador;
    private final CampoTexto nombre = new CampoTexto(null, "Ej. ecuador", null).conIcono(Iconos.Nombre.BUSCAR);
    private final Boton buscar = new Boton("Buscar", EstiloBoton.ACENTO, null);
    private final Alerta alerta = new Alerta(true);
    private final Pila resultados = new Pila();

    public BusquedaVista(PaisControlador controlador) {
        super(new BorderLayout(0, L));
        this.controlador = controlador;
        setBackground(SUPERFICIE);
        setBorder(new EmptyBorder(L, XL, M, XL));

        buscar.addActionListener(e -> buscar());
        nombre.alConfirmar(this::buscar);
        nombre.getCampo().getAccessibleContext().setAccessibleName("Nombre del país");

        add(new Pila().agregar(bandaBusqueda()).agregar(alerta, M), BorderLayout.NORTH);
        add(Componentes.desplazable(Componentes.centrado(resultados, Integer.MAX_VALUE, 0, false)),
                BorderLayout.CENTER);
        mostrarInicial();
    }

    public void alMostrar() {
        nombre.enfocar();
    }

    // ================================================================ Estructura

    /** Banda azul oscuro con el título, el campo de búsqueda y sugerencias de ejemplo. */
    private JComponent bandaBusqueda() {
        JPanel fila = new JPanel(new GridBagLayout());
        fila.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        nombre.setPreferredSize(new Dimension(620, nombre.getPreferredSize().height));
        fila.add(nombre, c);
        c.insets = new Insets(0, S, 0, 0);
        fila.add(buscar, c);
        c.weightx = 1;
        fila.add(new JLabel(), c);

        JPanel sugerencias = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        sugerencias.setOpaque(false);
        sugerencias.add(Componentes.etiqueta("Prueba con:", PEQUENO, BORDE_SUAVE));
        for (String sugerencia : SUGERENCIAS) {
            JButton chip = new ChipSugerencia(sugerencia);
            chip.addActionListener(e -> {
                nombre.setTexto(sugerencia);
                buscar();
            });
            sugerencias.add(javax.swing.Box.createHorizontalStrut(XS));
            sugerencias.add(chip);
        }

        Caja banda = new Caja(AZUL_OSCURO, null, RADIO_TARJETA, 0, 0, 0);
        banda.setRelleno(L + 4, XL, L, XL);
        banda.agregar(Componentes.sobretitulo("Actividad 1", CIAN_CLARO));
        banda.agregar(Componentes.etiqueta("Buscar un país en REST Countries", TITULAR_PEQUENO, BLANCO), XXS);
        banda.agregar(new Texto("Busca por nombre común, oficial o nativo («ecuador», «germany», «españa»). Cada "
                + "búsqueda consume una consulta de la cuota mensual de tu clave.", CUERPO, BORDE_SUAVE), XXS);
        banda.agregar(fila, M);
        banda.agregar(sugerencias, S);
        return banda;
    }

    // ================================================================ Acciones

    private void buscar() {
        String texto = nombre.getTexto().strip();
        if (texto.isEmpty()) {
            alerta.mostrar("Escribe el nombre del país que quieres buscar.", TipoAlerta.INFO);
            nombre.enfocar();
            return;
        }
        alerta.ocultar();
        buscar.setCargando(true, "Buscando…");
        Tarea.de(() -> new Busqueda(controlador.buscarEnApi(texto), codigosGuardados()))
                .siempre(() -> buscar.setCargando(false))
                .alTerminar(busqueda -> mostrar(texto, busqueda))
                .alFallar(mensaje -> alerta.mostrar(mensaje, TipoAlerta.ERROR))
                .iniciar();
    }

    /** Se ejecuta en segundo plano. Si MySQL no responde, la búsqueda sigue sin marcar los guardados. */
    private Set<String> codigosGuardados() {
        try {
            return controlador.listarPaises().stream().map(Pais::codigo).collect(Collectors.toSet());
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo saber qué países ya están guardados: {0}", e.getMessage());
            return null;
        }
    }

    private void mostrar(String texto, Busqueda busqueda) {
        ResultadoBusqueda resultado = busqueda.resultado();
        boolean desdeApi = resultado.origen() == OrigenDatos.API_EXTERNA;
        if (!desdeApi) {
            alerta.mostrar(resultado.aviso(), TipoAlerta.ADVERTENCIA);
        }
        resultados.removeAll();
        if (resultado.estaVacio()) {
            resultados.agregar(Componentes.estadoVacio(Iconos.Nombre.BUSCAR, "Sin coincidencias",
                    (desdeApi ? "La API" : "La base local") + " no tiene países con «" + texto + "». Prueba con el "
                    + "nombre en inglés o en su idioma oficial.", null));
        } else {
            int cantidad = resultado.paises().size();
            JPanel cabecera = new JPanel(new BorderLayout(M, 0));
            cabecera.setOpaque(false);
            cabecera.add(Componentes.etiqueta(cantidad + (cantidad == 1 ? " coincidencia" : " coincidencias")
                    + " para «" + texto + "»", TITULO_TARJETA, TEXTO), BorderLayout.WEST);
            cabecera.add(Componentes.enCentro(desdeApi
                    ? Componentes.chipEstado("Datos en vivo de la API", TipoAlerta.EXITO)
                    : Componentes.chipEstado("Datos de la base local", TipoAlerta.ADVERTENCIA)), BorderLayout.EAST);
            resultados.agregar(cabecera);

            JPanel cuadricula = new JPanel(new GridLayout(0, 2, M, M));
            cuadricula.setOpaque(false);
            for (Pais pais : resultado.paises()) {
                Boolean guardado = !desdeApi ? null
                        : busqueda.guardados() == null ? Boolean.FALSE : busqueda.guardados().contains(pais.codigo());
                cuadricula.add(new TarjetaResultado(pais, desdeApi, guardado));
            }
            resultados.agregar(cuadricula, S);
        }
        resultados.revalidate();
        resultados.repaint();
    }

    private void mostrarInicial() {
        resultados.removeAll();
        resultados.agregar(Componentes.estadoVacio(Iconos.Nombre.BUSCAR, "Busca un país en la API",
                "Los resultados aparecerán aquí. Podrás guardar en la base de datos los que necesites.", null), XL);
    }

    // ================================================================ Componentes

    /** Un país encontrado: código, región, datos principales y la acción de guardarlo en MySQL. */
    private final class TarjetaResultado extends Caja {

        private final JPanel zonaAccion = new JPanel(new BorderLayout(M, 0));

        /**
         * @param guardado {@code null} si el resultado viene de la base local (ya está guardado por definición)
         */
        TarjetaResultado(Pais pais, boolean desdeApi, Boolean guardado) {
            super(BLANCO, DIVISOR, RADIO_TARJETA, 2, Componentes.MARGEN_SOMBRA, 0);
            setRelleno(M + 4, L, M, L);

            JPanel cabecera = new JPanel(new BorderLayout(XS, 0));
            cabecera.setOpaque(false);
            cabecera.add(Componentes.fila(XS, Componentes.chip(pais.codigo(), CIAN_SUAVE, CIAN_TEXTO),
                    Componentes.etiqueta(pais.nombre(), TITULO_TARJETA, TEXTO)), BorderLayout.CENTER);
            cabecera.add(Componentes.enCentro(Componentes.chip(pais.region().getNombre(), AZUL_SUAVE, AZUL_HOVER)),
                    BorderLayout.EAST);

            JPanel datos = new JPanel(new GridLayout(1, 3, S, 0));
            datos.setOpaque(false);
            datos.add(dato("Capital", Formato.texto(pais.capital())));
            datos.add(dato("Población", Formato.entero(pais.poblacion())));
            datos.add(dato("Moneda", Formato.texto(pais.moneda())));

            zonaAccion.setOpaque(false);
            if (!desdeApi) {
                zonaAccion.add(Componentes.etiqueta("Guardado en la base local", PEQUENO, TEXTO_SECUNDARIO),
                        BorderLayout.WEST);
            } else {
                mostrarAccion(pais, guardado);
            }

            agregar(cabecera);
            agregar(datos, M);
            agregar(Componentes.lineaDivisoria(), M);
            agregar(zonaAccion, S);
            getAccessibleContext().setAccessibleName(pais.nombre() + ", " + pais.region().getNombre()
                    + ". Capital: " + Formato.texto(pais.capital()) + ". " + Formato.entero(pais.poblacion())
                    + " habitantes.");
        }

        private void mostrarAccion(Pais pais, boolean guardado) {
            zonaAccion.removeAll();
            JLabel estado = guardado
                    ? Componentes.etiqueta("Ya está en tu base", PEQUENO_SEMINEGRITA, EXITO_TEXTO)
                    : Componentes.etiqueta("No guardado todavía", PEQUENO, TEXTO_SECUNDARIO);
            if (guardado) {
                estado.setIcon(Iconos.icono(Iconos.Nombre.EXITO, EXITO_RELLENO, 16));
                estado.setIconTextGap(6);
            }
            Boton accion = guardado
                    ? new Boton("Actualizar datos", EstiloBoton.SECUNDARIO, Iconos.Nombre.ACTUALIZAR)
                    : new Boton("Guardar en MySQL", EstiloBoton.PRIMARIO, Iconos.Nombre.DESCARGAR);
            accion.getAccessibleContext().setAccessibleName((guardado ? "Actualizar " : "Guardar ") + pais.nombre()
                    + " en la base de datos");
            accion.addActionListener(e -> guardar(pais, accion));
            zonaAccion.add(estado, BorderLayout.WEST);
            zonaAccion.add(accion, BorderLayout.EAST);
            zonaAccion.revalidate();
            zonaAccion.repaint();
        }

        private void guardar(Pais pais, Boton boton) {
            alerta.ocultar();
            boton.setCargando(true, "Guardando…");
            Tarea.de(() -> controlador.guardarResultado(pais))
                    .siempre(() -> boton.setCargando(false))
                    .alTerminar(resultado -> {
                        mostrarAccion(pais, true);
                        alerta.mostrar(resultado.nuevos() > 0
                                ? "«" + pais.nombre() + "» se agregó a la base de datos."
                                : "«" + pais.nombre() + "» ya estaba guardado: se actualizaron sus datos.",
                                TipoAlerta.EXITO);
                    })
                    .alFallar(mensaje -> alerta.mostrar(mensaje, TipoAlerta.ERROR))
                    .iniciar();
        }

        private static JComponent dato(String etiqueta, String valor) {
            return new Pila()
                    .agregar(Componentes.etiqueta(etiqueta, LEYENDA, TEXTO_SECUNDARIO))
                    .agregar(new Texto(valor, CUERPO_SEMINEGRITA, TEXTO), 2);
        }
    }

    /** Sugerencia de búsqueda: chip translúcido sobre la banda oscura. */
    private static final class ChipSugerencia extends JButton {

        ChipSugerencia(String texto) {
            super(texto);
            setFont(PEQUENO_SEMINEGRITA);
            setForeground(BLANCO);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Componentes.MANO);
            setBorder(new EmptyBorder(0, S, 0, S));
            setToolTipText("Buscar «" + texto + "»");
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension tamano = super.getPreferredSize();
            tamano.height = 30;
            return tamano;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = Componentes.suave(graphics);
            RoundRectangle2D forma = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f,
                    getHeight(), getHeight());
            g.setColor(new Color(255, 255, 255, getModel().isRollover() ? 46 : 30));
            g.fill(forma);
            g.setColor(isFocusOwner() ? AZUL_FOCO : new Color(255, 255, 255, 62));
            g.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1f));
            g.draw(forma);
            g.dispose();
            super.paintComponent(graphics);
        }
    }
}

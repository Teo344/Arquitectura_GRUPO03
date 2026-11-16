package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import ec.edu.monster.controlador.FormularioPais;
import ec.edu.monster.controlador.PaisControlador;
import ec.edu.monster.modelo.entidad.Pais;
import ec.edu.monster.modelo.entidad.Region;
import ec.edu.monster.modelo.entidad.ResultadoImportacion;
import ec.edu.monster.vista.Componentes.Alerta;
import ec.edu.monster.vista.Componentes.Boton;
import ec.edu.monster.vista.Componentes.Caja;
import ec.edu.monster.vista.Componentes.CampoSeleccion;
import ec.edu.monster.vista.Componentes.CampoTexto;
import ec.edu.monster.vista.Componentes.EstiloBoton;
import ec.edu.monster.vista.Componentes.Pila;
import ec.edu.monster.vista.Componentes.Texto;
import ec.edu.monster.vista.Componentes.TipoAlerta;
import ec.edu.monster.vista.Tabla.Columna;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

/**
 * Gestión de países (CU-01, CU-03 a CU-07) con el patrón lista-detalle: la
 * tabla a la izquierda y el formulario siempre visible a la derecha. Solo
 * muestra datos y delega cada acción en {@link PaisControlador}.
 *
 * @author carlo
 */
public class PaisesVista extends JPanel {

    /** Opción del filtro de región; {@code region == null} significa todas. */
    private record OpcionRegion(Region region) {

        static final OpcionRegion TODAS = new OpcionRegion(null);

        String texto() {
            return region == null ? "Todas las regiones" : region.getNombre();
        }
    }

    private static final String VISTA_TABLA = "tabla";
    private static final String VISTA_VACIA = "vacia";

    private final PaisControlador controlador;

    // ---- Encabezado y lista ----
    private final Texto resumen = new Texto("Cargando países…", CUERPO, TEXTO_SECUNDARIO);
    private final Boton importar = new Boton("Importar desde la API", EstiloBoton.SECUNDARIO, Iconos.Nombre.DESCARGAR);
    private final Boton nuevo = new Boton("Nuevo país", EstiloBoton.SECUNDARIO, Iconos.Nombre.AGREGAR);
    private final Alerta alertaGeneral = new Alerta(true);
    private final CampoTexto filtro = new CampoTexto("Filtrar en la tabla", "Ej. ecuador, ECU o Quito", null);
    private final CampoSeleccion<OpcionRegion> region;
    private final Tabla<Pais> tabla;
    private final CardLayout cartasLista = new CardLayout();
    private final JPanel contenedorLista = new JPanel(cartasLista);
    private final Pila contenedorVacio = new Pila();

    // ---- Formulario ----
    private final JLabel sobretituloFormulario = Componentes.sobretitulo("Nuevo país");
    private final JLabel tituloFormulario = Componentes.etiqueta("Registrar país", TITULO, TEXTO);
    private final CampoTexto codigo = new CampoTexto("Código ISO", "Ej. ECU", "3 letras (ISO 3166 alfa-3).");
    private final CampoTexto nombre = new CampoTexto("Nombre", "Ej. Ecuador", null);
    private final CampoTexto capital = new CampoTexto("Capital (opcional)", "Ej. Quito", null);
    private final CampoSeleccion<Region> regionPais;
    private final CampoTexto poblacion = new CampoTexto("Población", "Ej. 18.001.000", "Número entero de habitantes.");
    private final CampoTexto moneda = new CampoTexto("Moneda (opcional)", "Ej. Dólar estadounidense", null);
    private final Alerta alertaFormulario = new Alerta(true);
    private final Boton guardar = new Boton("Registrar país", EstiloBoton.PRIMARIO, Iconos.Nombre.GUARDAR);
    private final Boton descartar = new Boton("Limpiar", EstiloBoton.TEXTO, null);
    private final Boton eliminar = new Boton("Eliminar", EstiloBoton.PELIGRO, Iconos.Nombre.ELIMINAR);

    private Region filtroRegion;
    private Pais enEdicion;
    private boolean modificado;
    private boolean rellenando;

    public PaisesVista(PaisControlador controlador) {
        super(new BorderLayout(0, M));
        this.controlador = controlador;
        setBackground(SUPERFICIE);
        setBorder(new EmptyBorder(L, L, M, L));

        List<OpcionRegion> opciones = new ArrayList<>();
        opciones.add(OpcionRegion.TODAS);
        controlador.regiones().forEach(r -> opciones.add(new OpcionRegion(r)));
        region = new CampoSeleccion<>("Región", opciones, OpcionRegion::texto, null);
        region.setSeleccion(OpcionRegion.TODAS);
        regionPais = new CampoSeleccion<>("Región", controlador.regiones(), Region::getNombre, "Selecciona una región");

        tabla = new Tabla<>("Países registrados", List.of(
                Columna.texto("ISO", Pais::codigo, 64, 64, false),
                Columna.texto("Nombre", Pais::nombre, 170, 120, true),
                Columna.texto("Capital", Pais::capital, 130, 96, false),
                Columna.texto("Región", Pais::region, 100, 92, false),
                Columna.numero("Población", Pais::poblacion, Formato::entero, 136),
                Columna.texto("Moneda", Pais::moneda, 150, 96, false)));
        tabla.ordenarPor(1);

        add(encabezado(), BorderLayout.NORTH);
        JPanel cuerpo = new JPanel(new BorderLayout(L, 0));
        cuerpo.setOpaque(false);
        cuerpo.add(panelLista(), BorderLayout.CENTER);
        cuerpo.add(panelFormulario(), BorderLayout.EAST);
        add(cuerpo, BorderLayout.CENTER);

        conectarEventos();
        registrarAtajos();
        limpiarFormulario();
    }

    /** La sección se muestra: se refresca la lista, porque otra sección pudo guardar países. */
    public void alMostrar() {
        cargar();
        tabla.enfocar();
    }

    // ================================================================ Estructura

    private JComponent encabezado() {
        return new Pila()
                .agregar(Componentes.encabezadoPagina("Gestión", "Países", resumen, importar, nuevo))
                .agregar(alertaGeneral, M);
    }

    private JComponent panelLista() {
        JPanel filtros = new JPanel(new GridBagLayout());
        filtros.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.SOUTH;
        c.weightx = 1;
        filtros.add(filtro, c);
        c.weightx = 0;
        c.insets = new Insets(0, M, 0, 0);
        region.setPreferredSize(new Dimension(220, region.getPreferredSize().height));
        filtros.add(region, c);

        Caja marcoTabla = new Caja(BLANCO, DIVISOR, RADIO_TARJETA, 2, Componentes.MARGEN_SOMBRA, 0);
        marcoTabla.setLayout(new BorderLayout());
        marcoTabla.setRelleno(XS, XXS, XS, XXS);
        contenedorLista.setOpaque(false);
        contenedorLista.add(tabla, VISTA_TABLA);
        contenedorLista.add(Componentes.enCentro(contenedorVacio), VISTA_VACIA);
        marcoTabla.add(contenedorLista, BorderLayout.CENTER);

        JPanel lista = new JPanel(new BorderLayout(0, S));
        lista.setOpaque(false);
        lista.add(filtros, BorderLayout.NORTH);
        lista.add(marcoTabla, BorderLayout.CENTER);
        lista.add(new Texto("Atajos: Ctrl+F filtrar · Ctrl+N nuevo · Ctrl+S guardar · Supr eliminar · "
                + "clic en un encabezado para ordenar", LEYENDA, TEXTO_SECUNDARIO), BorderLayout.SOUTH);
        return lista;
    }

    /**
     * Tarjeta del formulario: encabezado y acciones fijos, campos desplazables.
     * Así el botón de guardar y los mensajes de error siempre quedan a la vista,
     * aunque la ventana sea baja.
     */
    private JComponent panelFormulario() {
        Pila campos = new Pila()
                .agregar(codigo)
                .agregar(nombre, M)
                .agregar(capital, M)
                .agregar(regionPais, M)
                .agregar(poblacion, M)
                .agregar(moneda, M);
        // Margen para que el anillo de foco de 2 px no quede recortado y la barra no tape los campos
        campos.setBorder(new EmptyBorder(2, 2, 2, S));

        JPanel secundarias = new JPanel(new BorderLayout());
        secundarias.setOpaque(false);
        secundarias.add(descartar, BorderLayout.WEST);
        secundarias.add(eliminar, BorderLayout.EAST);
        Pila acciones = new Pila()
                .agregar(Componentes.lineaDivisoria())
                .agregar(alertaFormulario, M)
                .agregar(guardar, M)
                .agregar(secundarias, XS);

        Caja tarjeta = Componentes.tarjeta();
        tarjeta.setLayout(new BorderLayout(0, M));
        tarjeta.add(new Pila().agregar(sobretituloFormulario).agregar(tituloFormulario, XXS), BorderLayout.NORTH);
        tarjeta.add(Componentes.desplazable(Componentes.centrado(campos, Integer.MAX_VALUE, 0, false)),
                BorderLayout.CENTER);
        tarjeta.add(acciones, BorderLayout.SOUTH);
        tarjeta.setPreferredSize(new Dimension(ANCHO_FORMULARIO_LATERAL, 0));
        return tarjeta;
    }

    // ================================================================ Eventos

    private void conectarEventos() {
        importar.addActionListener(e -> importar());
        nuevo.addActionListener(e -> nuevo());
        guardar.addActionListener(e -> guardar());
        descartar.addActionListener(e -> descartar());
        eliminar.addActionListener(e -> eliminar());

        filtro.alCambiar(() -> {
            tabla.setFiltro(filtro.getTexto());
            actualizarResumenYVacio();
        });
        region.alCambiar(opcion -> {
            Region elegida = opcion == null ? null : opcion.region();
            if (elegida != filtroRegion) {
                filtroRegion = elegida;
                cargar();
            }
        });

        tabla.setPuedeCambiarSeleccion(nueva -> !modificado || confirmarDescarte());
        tabla.alSeleccionar(pais -> {
            if (pais != null) {
                editar(pais);
            }
        });
        tabla.alSuprimir(this::eliminar);

        for (CampoTexto campo : List.of(codigo, nombre, capital, poblacion, moneda)) {
            campo.alCambiar(this::marcarModificado);
            campo.alConfirmar(this::guardar);
        }
        regionPais.alCambiar(r -> marcarModificado());
    }

    private void registrarAtajos() {
        atajo(KeyEvent.VK_F, "filtrar", filtro::enfocar);
        atajo(KeyEvent.VK_N, "nuevo", this::nuevo);
        atajo(KeyEvent.VK_S, "guardar", this::guardar);
    }

    private void atajo(int tecla, String nombreAccion, Runnable accion) {
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(tecla, InputEvent.CTRL_DOWN_MASK), nombreAccion);
        getActionMap().put(nombreAccion, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                accion.run();
            }
        });
    }

    // ================================================================ Lista (CU-04, CU-07)

    private void cargar() {
        resumen.setTexto("Cargando países…");
        Region filtroActual = filtroRegion;
        Tarea.de(() -> filtroActual == null ? controlador.listarPaises() : controlador.buscarPorRegion(filtroActual))
                .alTerminar(this::mostrarPaises)
                .alFallar(mensaje -> {
                    tabla.setFilas(List.of());
                    resumen.setTexto("No se pudieron cargar los países.");
                    alertaGeneral.mostrar(mensaje, TipoAlerta.ERROR);
                    mostrarVacio("No se pudo cargar la lista", "Revisa que MySQL esté en ejecución y vuelve a intentar.",
                            botonSecundario("Reintentar", Iconos.Nombre.ACTUALIZAR, this::cargar));
                })
                .iniciar();
    }

    private void mostrarPaises(List<Pais> paises) {
        Integer idElegido = enEdicion == null ? null : enEdicion.id();
        tabla.setFilas(paises);
        tabla.setFiltro(filtro.getTexto());
        if (idElegido != null) {
            tabla.seleccionar(p -> idElegido.equals(p.id()));
        }
        actualizarResumenYVacio();
    }

    private void actualizarResumenYVacio() {
        int total = tabla.totalFilas();
        int visibles = tabla.filasVisibles();
        String alcance = filtroRegion == null ? " registrados" : " en " + filtroRegion.getNombre();
        String texto = Formato.paises(total) + alcance;
        if (!filtro.getTexto().isBlank()) {
            texto += " · mostrando " + Formato.entero(visibles);
        }
        resumen.setTexto(texto);

        if (total == 0 && filtroRegion == null) {
            mostrarVacio("Aún no hay países",
                    "Importa todos los países desde la API REST Countries o registra uno con el formulario.",
                    botonSecundario("Importar desde la API", Iconos.Nombre.DESCARGAR, this::importar));
        } else if (total == 0) {
            mostrarVacio("No hay países en " + filtroRegion.getNombre(),
                    "Elige otra región o importa los países desde la API.",
                    botonSecundario("Ver todas las regiones", null, () -> region.setSeleccion(OpcionRegion.TODAS)));
        } else if (visibles == 0) {
            mostrarVacio("Sin coincidencias", "Ningún país contiene «" + filtro.getTexto().strip() + "».",
                    botonSecundario("Limpiar filtro", null, () -> filtro.setTexto("")));
        } else {
            cartasLista.show(contenedorLista, VISTA_TABLA);
        }
    }

    private void mostrarVacio(String titulo, String texto, JComponent accion) {
        contenedorVacio.removeAll();
        contenedorVacio.agregar(Componentes.estadoVacio(Iconos.Nombre.PUBLICO, titulo, texto, accion));
        cartasLista.show(contenedorLista, VISTA_VACIA);
        contenedorVacio.revalidate();
        contenedorVacio.repaint();
    }

    private static Boton botonSecundario(String texto, Iconos.Nombre icono, Runnable accion) {
        Boton boton = new Boton(texto, EstiloBoton.SECUNDARIO, icono);
        boton.addActionListener(e -> accion.run());
        return boton;
    }

    // ================================================================ Importar (CU-01)

    private void importar() {
        int guardados = tabla.totalFilas();
        if (guardados > 0 && !Componentes.confirmar(this, "Importar desde la API",
                "Se descargarán todos los países de REST Countries. Los que ya existen (mismo código) se "
                + "actualizarán con los datos de la API y se perderán los cambios manuales hechos en ellos.",
                List.<String[]>of(new String[]{"Países en la lista", Formato.entero(guardados)}),
                "Importar", false)) {
            return;
        }
        alertaGeneral.ocultar();
        importar.setCargando(true, "Importando…");
        Tarea.de(controlador::importarPaises)
                .siempre(() -> importar.setCargando(false))
                .alTerminar(resultado -> {
                    alertaGeneral.mostrar(mensajeImportacion(resultado), TipoAlerta.EXITO);
                    cargar();
                })
                .alFallar(mensaje -> alertaGeneral.mostrar("No se pudo importar: " + mensaje, TipoAlerta.ERROR))
                .iniciar();
    }

    private static String mensajeImportacion(ResultadoImportacion resultado) {
        String mensaje = "Importación completa: " + Formato.paises(resultado.recibidos()) + " recibidos · "
                + Formato.entero(resultado.nuevos()) + " nuevos · " + Formato.entero(resultado.actualizados())
                + " actualizados.";
        if (resultado.recibidos() <= 1) {
            mensaje += " Si esperabas más, revisa api.clave en config.properties: la clave demo solo devuelve "
                    + "un país de muestra.";
        }
        return mensaje;
    }

    // ================================================================ Formulario (CU-03, CU-05, CU-06)

    private void nuevo() {
        if (modificado && !confirmarDescarte()) {
            return;
        }
        tabla.limpiarSeleccion();
        limpiarFormulario();
        codigo.enfocar();
    }

    private void editar(Pais pais) {
        enEdicion = pais;
        rellenar(pais);
        sobretituloFormulario.setText(("Editando · " + pais.codigo()).toUpperCase());
        tituloFormulario.setText(pais.nombre());
        guardar.setText("Guardar cambios");
        descartar.setText("Descartar cambios");
        eliminar.setVisible(true);
        alertaFormulario.ocultar();
        actualizarAcciones();
    }

    private void limpiarFormulario() {
        enEdicion = null;
        rellenar(null);
        sobretituloFormulario.setText("NUEVO PAÍS");
        tituloFormulario.setText("Registrar país");
        guardar.setText("Registrar país");
        descartar.setText("Limpiar");
        eliminar.setVisible(false);
        alertaFormulario.ocultar();
        actualizarAcciones();
    }

    private void rellenar(Pais pais) {
        rellenando = true;
        codigo.setTexto(pais == null ? "" : pais.codigo());
        nombre.setTexto(pais == null ? "" : pais.nombre());
        capital.setTexto(pais == null ? "" : pais.capital());
        regionPais.setSeleccion(pais == null ? null : pais.region());
        poblacion.setTexto(pais == null ? "" : Formato.entero(pais.poblacion()));
        moneda.setTexto(pais == null ? "" : pais.moneda());
        rellenando = false;
        modificado = false;
    }

    private void marcarModificado() {
        if (!rellenando && !modificado) {
            modificado = true;
            actualizarAcciones();
        }
    }

    private void actualizarAcciones() {
        descartar.setEnabled(modificado || enEdicion == null);
        revalidate();
        repaint();
    }

    private FormularioPais leerFormulario() {
        Region elegida = regionPais.getSeleccion();
        return new FormularioPais(codigo.getTexto(), nombre.getTexto(), capital.getTexto(),
                elegida == null ? null : elegida.getNombre(), poblacion.getTexto(), moneda.getTexto());
    }

    private void guardar() {
        if (!guardar.isEnabled()) {
            return;
        }
        FormularioPais formulario = leerFormulario();
        Pais original = enEdicion;
        alertaFormulario.ocultar();
        guardar.setCargando(true, "Guardando…");
        Tarea.de(() -> original == null
                ? controlador.registrarPais(formulario)
                : controlador.actualizarPais(original.id(), formulario))
                .siempre(() -> guardar.setCargando(false))
                .alTerminar(guardado -> {
                    editar(guardado);
                    alertaFormulario.mostrar("«" + guardado.nombre() + "» se guardó correctamente.", TipoAlerta.EXITO);
                    cargar();
                })
                .alFallar(mensaje -> alertaFormulario.mostrar(conVinetas(mensaje), TipoAlerta.ERROR))
                .iniciar();
    }

    private void descartar() {
        if (enEdicion != null) {
            editar(enEdicion);
        } else {
            limpiarFormulario();
            codigo.enfocar();
        }
    }

    private void eliminar() {
        Pais pais = enEdicion;
        if (pais == null) {
            return;
        }
        boolean confirmado = Componentes.confirmar(this, "Eliminar país",
                "Esta acción no se puede deshacer. Si el país viene de la API, volverá a aparecer en la próxima "
                + "importación.",
                List.of(new String[]{"Código", pais.codigo()}, new String[]{"Nombre", pais.nombre()}),
                "Eliminar", true);
        if (!confirmado) {
            return;
        }
        eliminar.setCargando(true, "Eliminando…");
        Tarea.deAccion(() -> controlador.eliminarPais(pais.id()))
                .siempre(() -> eliminar.setCargando(false))
                .alTerminar(nada -> {
                    tabla.limpiarSeleccion();
                    limpiarFormulario();
                    alertaGeneral.mostrar("«" + pais.nombre() + "» se eliminó de la base de datos.", TipoAlerta.EXITO);
                    cargar();
                })
                .alFallar(mensaje -> alertaFormulario.mostrar(mensaje, TipoAlerta.ERROR))
                .iniciar();
    }

    private boolean confirmarDescarte() {
        return Componentes.confirmar(this, "Descartar cambios",
                "Hay cambios sin guardar en el formulario. Si continúas, se perderán.",
                List.of(), "Descartar", true);
    }

    /** Varios errores de validación llegan en líneas separadas: se presentan como lista. */
    private static String conVinetas(String mensaje) {
        if (!mensaje.contains("\n")) {
            return mensaje;
        }
        return "Revisa estos datos:\n• " + mensaje.replace("\n", "\n• ");
    }
}

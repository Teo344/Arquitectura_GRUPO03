package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import ec.edu.monster.controlador.FormularioPais;
import ec.edu.monster.controlador.PaisControlador;
import ec.edu.monster.modelo.entidad.Pais;
import ec.edu.monster.modelo.entidad.Region;
import ec.edu.monster.modelo.entidad.ResultadoImportacion;
import ec.edu.monster.vista.Componentes.Alerta;
import ec.edu.monster.vista.Componentes.Boton;
import ec.edu.monster.vista.Componentes.BotonIcono;
import ec.edu.monster.vista.Componentes.Caja;
import ec.edu.monster.vista.Componentes.CampoSeleccion;
import ec.edu.monster.vista.Componentes.CampoTexto;
import ec.edu.monster.vista.Componentes.EstiloBoton;
import ec.edu.monster.vista.Componentes.Pila;
import ec.edu.monster.vista.Componentes.Texto;
import ec.edu.monster.vista.Componentes.TipoAlerta;
import ec.edu.monster.vista.Tabla.Columna;
import ec.edu.monster.vista.Tabla.Estilo;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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
 * Gestión de países (CU-01, CU-03 a CU-07). La tabla paginada ocupa todo el
 * ancho; arriba, la búsqueda y los chips de región. Registrar y editar se
 * hace en un panel lateral que se abre sobre la tabla, así la lista no pierde
 * espacio mientras no se edita. Solo muestra datos y delega cada acción en
 * {@link PaisControlador}.
 *
 * @author carlo
 */
public class PaisesVista extends JPanel {

    /** Opción del filtro de región; {@code region == null} significa todas. */
    private record OpcionRegion(Region region) {

        static final OpcionRegion TODAS = new OpcionRegion(null);

        String texto() {
            return region == null ? "Todas" : region.getNombre();
        }
    }

    /** Lo que trae cada carga: todos los países (para los conteos) y los de la región elegida. */
    private record Carga(List<Pais> todos, List<Pais> lista) {
    }

    private static final String VISTA_TABLA = "tabla";
    private static final String VISTA_VACIA = "vacia";

    private final PaisControlador controlador;
    private final PanelLateral panelLateral;

    // ---- Encabezado, filtros y lista ----
    private final Texto resumen = new Texto("Cargando países…", CUERPO, TEXTO_SECUNDARIO);
    private final Boton importar = new Boton("Importar desde la API", EstiloBoton.SECUNDARIO, Iconos.Nombre.DESCARGAR);
    private final Boton nuevo = new Boton("Nuevo país", EstiloBoton.PRIMARIO, Iconos.Nombre.AGREGAR);
    private final Alerta alertaGeneral = new Alerta(true);
    private final CampoTexto filtro = new CampoTexto(null, "Nombre, código ISO o capital", null)
            .conIcono(Iconos.Nombre.BUSCAR);
    private final GrupoChips<OpcionRegion> chipsRegion;
    private final Tabla<Pais> tabla;
    private final CardLayout cartasLista = new CardLayout();
    private final JPanel contenedorLista = new JPanel(cartasLista);
    private final Pila contenedorVacio = new Pila();

    // ---- Panel lateral (formulario) ----
    private final JPanel formulario = new JPanel(new BorderLayout());
    private final JLabel sobretituloFormulario = Componentes.sobretitulo("Nuevo país");
    private final JLabel tituloFormulario = Componentes.etiqueta("Registrar país", TITULO, TEXTO);
    private final CampoTexto codigo = new CampoTexto("Código ISO", "Ej. ECU", "3 letras (ISO 3166 alfa-3).");
    private final CampoTexto nombre = new CampoTexto("Nombre", "Ej. Ecuador", null);
    private final CampoTexto capital = new CampoTexto("Capital (opcional)", "Ej. Quito", null);
    private final CampoSeleccion<Region> regionPais;
    private final CampoTexto poblacion = new CampoTexto("Población", "Ej. 18.001.000", "Número entero de habitantes.");
    private final CampoTexto moneda = new CampoTexto("Moneda (opcional)", "Ej. Dólar estadounidense", null);
    private final Alerta avisoApi = new Alerta(false);
    private final Alerta alertaFormulario = new Alerta(true);
    private final Boton guardar = new Boton("Registrar país", EstiloBoton.PRIMARIO, Iconos.Nombre.GUARDAR);
    private final Boton cancelar = new Boton("Cancelar", EstiloBoton.TEXTO, null);
    private final Boton eliminar = new Boton("Eliminar", EstiloBoton.CONTORNO_PELIGRO, null);

    private Region filtroRegion;
    private Pais enEdicion;
    private Integer idPorSeleccionar;
    private boolean modificado;
    private boolean rellenando;

    public PaisesVista(PaisControlador controlador) {
        super(new BorderLayout());
        this.controlador = controlador;
        setBackground(SUPERFICIE);

        List<OpcionRegion> opciones = new ArrayList<>();
        opciones.add(OpcionRegion.TODAS);
        controlador.regiones().forEach(r -> opciones.add(new OpcionRegion(r)));
        chipsRegion = new GrupoChips<>("Filtrar por región", opciones, OpcionRegion::texto);
        chipsRegion.setSeleccion(OpcionRegion.TODAS);
        regionPais = new CampoSeleccion<>("Región", controlador.regiones(), Region::getNombre, "Selecciona");

        tabla = new Tabla<>("Países registrados", "países", List.of(
                Columna.texto("ISO", Pais::codigo, 84, 84, Estilo.CODIGO),
                Columna.texto("Nombre", Pais::nombre, 200, 140, Estilo.DESTACADA),
                Columna.texto("Capital", Pais::capital, 150, 100, Estilo.SECUNDARIA),
                Columna.texto("Región", p -> p.region().getNombre(), 130, 120, Estilo.ETIQUETA),
                Columna.numero("Población", Pais::poblacion, 150),
                Columna.texto("Moneda", Pais::moneda, 180, 110, Estilo.SECUNDARIA)));
        tabla.ordenarPor(1, true);
        tabla.conAcciones(this::abrirEdicion, this::eliminar);
        tabla.alActivar(this::abrirEdicion);

        JPanel contenido = new JPanel(new BorderLayout(0, M));
        contenido.setBackground(SUPERFICIE);
        contenido.setBorder(new EmptyBorder(M, XL, L, XL));
        contenido.add(new Pila()
                .agregar(Componentes.encabezadoPagina("Gestión", "Países", resumen, importar, nuevo))
                .agregar(alertaGeneral, M)
                .agregar(barraFiltros(), S), BorderLayout.NORTH);
        contenido.add(marcoTabla(), BorderLayout.CENTER);

        construirFormulario();
        panelLateral = new PanelLateral(contenido);
        panelLateral.alPedirCierre(this::cerrarPanel);
        add(panelLateral, BorderLayout.CENTER);

        conectarEventos();
        registrarAtajos();
    }

    /** La sección se muestra: se refresca la lista, porque otra sección pudo guardar países. */
    public void alMostrar() {
        cargar();
        tabla.enfocar();
    }

    // ================================================================ Estructura

    private JComponent barraFiltros() {
        JPanel barra = new JPanel(new GridBagLayout());
        barra.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        filtro.setPreferredSize(new Dimension(320, filtro.getPreferredSize().height));
        filtro.getCampo().getAccessibleContext().setAccessibleName("Buscar en la tabla");
        filtro.getCampo().setToolTipText("Buscar en la tabla (Ctrl+F)");
        barra.add(filtro, c);
        c.weightx = 1;
        c.insets = new Insets(0, M, 0, 0);
        barra.add(chipsRegion, c);
        return barra;
    }

    private JComponent marcoTabla() {
        Caja marco = new Caja(BLANCO, DIVISOR, RADIO_TARJETA, 2, Componentes.MARGEN_SOMBRA, 0);
        marco.setLayout(new BorderLayout());
        marco.setRelleno(2, 2, 2, 2);
        contenedorLista.setOpaque(false);
        contenedorLista.add(tabla, VISTA_TABLA);
        contenedorLista.add(Componentes.enCentro(contenedorVacio), VISTA_VACIA);
        marco.add(contenedorLista, BorderLayout.CENTER);
        return marco;
    }

    /**
     * Panel lateral: encabezado y acciones fijos, campos desplazables. Así el
     * botón de guardar y los errores siempre quedan a la vista.
     */
    private void construirFormulario() {
        BotonIcono cerrar = new BotonIcono(Iconos.Nombre.CERRAR, TEXTO_SECUNDARIO, 20, AZUL_SUAVE, "Cerrar (Esc)");
        cerrar.addActionListener(e -> cerrarPanel());
        JPanel cabecera = new JPanel(new BorderLayout(S, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(FONDO_SECCION);
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        cabecera.setOpaque(false);
        cabecera.setBorder(new EmptyBorder(L, L, M, M));
        cabecera.add(new Pila().agregar(sobretituloFormulario).agregar(tituloFormulario, XXS), BorderLayout.CENTER);
        JPanel columnaCerrar = new JPanel(new BorderLayout());
        columnaCerrar.setOpaque(false);
        columnaCerrar.add(cerrar, BorderLayout.NORTH);
        cabecera.add(columnaCerrar, BorderLayout.EAST);

        JPanel codigoYRegion = new JPanel(new GridLayout(1, 2, S, 0));
        codigoYRegion.setOpaque(false);
        codigoYRegion.add(codigo);
        codigoYRegion.add(regionPais);
        avisoApi.mostrar("Si vuelves a importar desde la API, estos cambios se sobrescribirán con los datos de "
                + "REST Countries.", TipoAlerta.ADVERTENCIA);
        Pila campos = new Pila()
                .agregar(codigoYRegion)
                .agregar(nombre, M)
                .agregar(capital, M)
                .agregar(poblacion, M)
                .agregar(moneda, M)
                .agregar(avisoApi, M);
        // Margen para que el anillo de foco de 2 px no quede recortado
        campos.setBorder(new EmptyBorder(M + 2, L, M, L));

        JPanel secundarias = new JPanel(new BorderLayout(XS, 0));
        secundarias.setOpaque(false);
        secundarias.add(eliminar, BorderLayout.WEST);
        secundarias.add(Componentes.fila(XS, cancelar, guardar), BorderLayout.EAST);
        Pila acciones = new Pila()
                .agregar(alertaFormulario)
                .agregar(secundarias, S);
        JPanel pie = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(FONDO_SECCION);
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        pie.setOpaque(false);
        pie.setBorder(new EmptyBorder(M, L, M, L));
        pie.add(acciones, BorderLayout.CENTER);

        formulario.setBackground(BLANCO);
        formulario.add(cabecera, BorderLayout.NORTH);
        formulario.add(Componentes.desplazable(Componentes.centrado(campos, Integer.MAX_VALUE, 0, false)),
                BorderLayout.CENTER);
        formulario.add(pie, BorderLayout.SOUTH);
        formulario.getAccessibleContext().setAccessibleName("Formulario de país");
    }

    // ================================================================ Eventos

    private void conectarEventos() {
        importar.addActionListener(e -> importar());
        nuevo.addActionListener(e -> abrirNuevo());
        nuevo.setToolTipText("Nuevo país (Ctrl+N)");
        guardar.addActionListener(e -> guardar());
        guardar.setToolTipText("Guardar (Ctrl+S)");
        cancelar.addActionListener(e -> cerrarPanel());
        eliminar.addActionListener(e -> {
            if (enEdicion != null) {
                eliminar(enEdicion);
            }
        });

        filtro.alCambiar(() -> {
            tabla.setFiltro(filtro.getTexto());
            actualizarResumenYVacio();
        });
        chipsRegion.alCambiar(opcion -> {
            filtroRegion = opcion == null ? null : opcion.region();
            cargar();
        });

        for (CampoTexto campo : List.of(codigo, nombre, capital, poblacion, moneda)) {
            campo.alCambiar(this::marcarModificado);
            campo.alConfirmar(this::guardar);
        }
        regionPais.alCambiar(r -> marcarModificado());
    }

    private void registrarAtajos() {
        atajo(KeyEvent.VK_F, "filtrar", () -> {
            if (!panelLateral.estaAbierto()) {
                filtro.enfocar();
            }
        });
        atajo(KeyEvent.VK_N, "nuevo", this::abrirNuevo);
        atajo(KeyEvent.VK_S, "guardar", () -> {
            if (panelLateral.estaAbierto()) {
                guardar();
            }
        });
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
        Tarea.de(() -> {
            List<Pais> todos = controlador.listarPaises();
            return new Carga(todos, filtroActual == null ? todos : controlador.buscarPorRegion(filtroActual));
        })
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

    private void mostrarPaises(Carga carga) {
        chipsRegion.setConteos(opcion -> opcion.region() == null ? carga.todos().size()
                : carga.todos().stream().filter(p -> p.region() == opcion.region()).count());
        tabla.setFilas(carga.lista());
        Integer id = idPorSeleccionar != null ? idPorSeleccionar : enEdicion == null ? null : enEdicion.id();
        idPorSeleccionar = null;
        if (id != null) {
            tabla.seleccionar(p -> id.equals(p.id()));
        }
        actualizarResumenYVacio();
    }

    private void actualizarResumenYVacio() {
        int total = tabla.totalFilas();
        int visibles = tabla.filasVisibles();
        String alcance = filtroRegion == null ? " registrados" : " en " + filtroRegion.getNombre();
        String texto = Formato.paises(total) + alcance;
        if (!filtro.getTexto().isBlank()) {
            texto += " · " + Formato.entero(visibles) + " coinciden con «" + filtro.getTexto().strip() + "»";
        }
        resumen.setTexto(texto);

        if (total == 0 && filtroRegion == null) {
            mostrarVacio("Aún no hay países",
                    "Importa todos los países desde la API REST Countries o registra uno con «Nuevo país».",
                    botonSecundario("Importar desde la API", Iconos.Nombre.DESCARGAR, this::importar));
        } else if (total == 0) {
            mostrarVacio("No hay países en " + filtroRegion.getNombre(),
                    "Elige otra región o importa los países desde la API.",
                    botonSecundario("Ver todas las regiones", null, this::verTodasLasRegiones));
        } else if (visibles == 0) {
            mostrarVacio("Sin coincidencias", "Ningún país contiene «" + filtro.getTexto().strip() + "».",
                    botonSecundario("Limpiar búsqueda", null, () -> filtro.setTexto("")));
        } else {
            cartasLista.show(contenedorLista, VISTA_TABLA);
        }
    }

    private void verTodasLasRegiones() {
        chipsRegion.setSeleccion(OpcionRegion.TODAS);
        filtroRegion = null;
        cargar();
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

    // ================================================================ Panel lateral (CU-03, CU-05, CU-06)

    private void abrirNuevo() {
        if (panelLateral.estaAbierto() && modificado && !confirmarDescarte()) {
            return;
        }
        tabla.limpiarSeleccion();
        limpiarFormulario();
        panelLateral.abrir(formulario, codigo.getCampo());
    }

    private void abrirEdicion(Pais pais) {
        if (panelLateral.estaAbierto() && modificado && !confirmarDescarte()) {
            return;
        }
        editar(pais);
        tabla.seleccionar(p -> p.id().equals(pais.id()));
        panelLateral.abrir(formulario, nombre.getCampo());
    }

    private void cerrarPanel() {
        if (!panelLateral.estaAbierto() || (modificado && !confirmarDescarte())) {
            return;
        }
        panelLateral.cerrar();
        enEdicion = null;
        modificado = false;
        tabla.limpiarSeleccion();
        tabla.enfocar();
    }

    private void editar(Pais pais) {
        enEdicion = pais;
        rellenar(pais);
        sobretituloFormulario.setText(("Editando · " + pais.codigo()).toUpperCase());
        tituloFormulario.setText(pais.nombre());
        guardar.setText("Guardar cambios");
        eliminar.setVisible(true);
        avisoApi.setVisible(true);
        alertaFormulario.ocultar();
        formulario.revalidate();
    }

    private void limpiarFormulario() {
        enEdicion = null;
        rellenar(null);
        sobretituloFormulario.setText("NUEVO PAÍS");
        tituloFormulario.setText("Registrar país");
        guardar.setText("Registrar país");
        eliminar.setVisible(false);
        avisoApi.setVisible(false);
        alertaFormulario.ocultar();
        formulario.revalidate();
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
        if (!rellenando) {
            modificado = true;
        }
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
        FormularioPais datos = leerFormulario();
        Pais original = enEdicion;
        alertaFormulario.ocultar();
        guardar.setCargando(true, "Guardando…");
        Tarea.de(() -> original == null
                ? controlador.registrarPais(datos)
                : controlador.actualizarPais(original.id(), datos))
                .siempre(() -> guardar.setCargando(false))
                .alTerminar(guardado -> {
                    modificado = false;
                    panelLateral.cerrar();
                    enEdicion = null;
                    idPorSeleccionar = guardado.id();
                    alertaGeneral.mostrar("«" + guardado.nombre() + "» se guardó correctamente.", TipoAlerta.EXITO);
                    cargar();
                    tabla.enfocar();
                })
                .alFallar(mensaje -> {
                    alertaFormulario.mostrar(conVinetas(mensaje), TipoAlerta.ERROR);
                    formulario.revalidate();
                })
                .iniciar();
    }

    private void eliminar(Pais pais) {
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
                    if (enEdicion != null && enEdicion.id().equals(pais.id())) {
                        modificado = false;
                        panelLateral.cerrar();
                        enEdicion = null;
                    }
                    tabla.limpiarSeleccion();
                    alertaGeneral.mostrar("«" + pais.nombre() + "» se eliminó de la base de datos.", TipoAlerta.EXITO);
                    cargar();
                })
                .alFallar(mensaje -> {
                    if (panelLateral.estaAbierto()) {
                        alertaFormulario.mostrar(mensaje, TipoAlerta.ERROR);
                    } else {
                        alertaGeneral.mostrar(mensaje, TipoAlerta.ERROR);
                    }
                })
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

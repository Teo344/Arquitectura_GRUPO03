package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import ec.edu.monster.controlador.TelefonoControlador;
import ec.edu.monster.modelo.TelefonoModelo;
import ec.edu.monster.servicio.ClienteRESTMonsterCell;
import ec.edu.monster.vista.Componentes.Alerta;
import ec.edu.monster.vista.Componentes.Boton;
import ec.edu.monster.vista.Componentes.BotonIcono;
import ec.edu.monster.vista.Componentes.CampoTexto;
import ec.edu.monster.vista.Componentes.Caja;
import ec.edu.monster.vista.Componentes.EstiloBoton;
import ec.edu.monster.vista.Componentes.Pila;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Mantenimiento del catálogo (CU-03 a CU-06), como telefonos.jsp del cliente
 * web: el formulario para agregar o editar a la izquierda y los teléfonos a la
 * venta a la derecha, con Editar y Dar de baja. Solo muestra datos y delega
 * las acciones al TelefonoControlador.
 */
public class TelefonosVista extends SeccionVista {

    private final TelefonoControlador controlador;
    private Integer codigoEditado;

    private final Alerta mensaje = new Alerta();
    private final JLabel tituloFormulario = Componentes.etiqueta("", TITULO, TEXTO);
    private final Pila zonaCodigo = new Pila();
    private final CampoTexto marca = new CampoTexto("Marca", "Ej. Samsung", "Máximo 50 caracteres.", false);
    private final CampoTexto modelo = new CampoTexto("Modelo", "Ej. Galaxy A15", "Máximo 100 caracteres.", false);
    private final CampoTexto precio = new CampoTexto("Precio", "199.00",
            "En efectivo se vende con 42 % de descuento.", false);
    private final Boton guardar = new Boton("", EstiloBoton.PRIMARIO, null);
    private final Boton cancelar = new Boton("Cancelar edición", EstiloBoton.TEXTO, null);
    private final Pila lista = new Pila();
    private final List<BotonIcono> accionesLista = new ArrayList<>();

    public TelefonosVista(PrincipalVista principal, ClienteRESTMonsterCell clienteRest) {
        super(principal);
        this.controlador = new TelefonoControlador(this, clienteRest);

        guardar.addActionListener(e -> guardar());
        precio.alConfirmar(this::guardar);
        marca.alConfirmar(modelo::enfocar);
        modelo.alConfirmar(precio::enfocar);
        cancelar.addActionListener(e -> {
            limpiarFormulario();
            mensaje.ocultar();
        });

        Pila contenido = new Pila()
                .agregar(Componentes.encabezado("Catálogo", "Teléfonos",
                        "Agrega, edita o da de baja los teléfonos que se venden en el punto de venta.", null, null))
                .agregar(mensaje, M)
                .agregar(Componentes.dosColumnas(formulario(), lista, 0.36, L - 2 * Componentes.MARGEN_SOMBRA, false),
                        L - Componentes.MARGEN_SOMBRA);
        add(Componentes.desplazable(Componentes.centrado(contenido, ANCHO_CONTENIDO, L, false)), BorderLayout.CENTER);

        limpiarFormulario();
        controlador.cargar();
    }

    // ---- Métodos que usa el controlador ----

    /** Mientras se da de baja un teléfono. */
    @Override
    public void setProcesando(boolean procesando) {
        accionesLista.forEach(boton -> boton.setEnabled(!procesando));
        guardar.setEnabled(!procesando);
    }

    @Override
    public void mostrarMensaje(String texto, boolean esError) {
        mensaje.mostrar(texto, esError);
    }

    @Override
    void detener() {
        controlador.detener();
    }

    public void setCargando(boolean cargando) {
        if (cargando && accionesLista.isEmpty()) {
            lista.vaciar();
            lista.agregar(Componentes.estadoVacio(Iconos.Nombre.CATALOGO, "Cargando catálogo",
                    "Consultando los teléfonos a la venta..."));
            lista.revalidate();
        }
    }

    public void setGuardando(boolean guardando) {
        guardar.setCargando(guardando);
        cancelar.setEnabled(!guardando);
        marca.setHabilitado(!guardando);
        modelo.setHabilitado(!guardando);
        precio.setHabilitado(!guardando);
    }

    public void mostrarTelefonos(List<TelefonoModelo> telefonos) {
        accionesLista.clear();
        lista.vaciar();
        if (telefonos.isEmpty()) {
            lista.agregar(Componentes.estadoVacio(Iconos.Nombre.CATALOGO, "Catálogo vacío",
                    "Agrega el primer teléfono con el formulario."));
        } else {
            lista.agregar(tablaTelefonos(telefonos));
        }
        lista.revalidate();
        lista.repaint();
    }

    public void limpiarFormulario() {
        codigoEditado = null;
        marca.setTexto("");
        modelo.setTexto("");
        precio.setTexto("");
        tituloFormulario.setText("Agregar teléfono");
        guardar.setText("Agregar al catálogo");
        guardar.setIcono(Iconos.Nombre.AGREGAR);
        zonaCodigo.vaciar();
        cancelar.setVisible(false);
        revalidate();
    }

    /** Si el teléfono dado de baja estaba en el formulario, el formulario se limpia. */
    public void limpiarFormularioSiEdita(int codTelefono) {
        if (codigoEditado != null && codigoEditado == codTelefono) {
            limpiarFormulario();
        }
    }

    public boolean confirmarBaja(List<String[]> filas) {
        return Componentes.confirmar(this, "Dar de baja el teléfono",
                "Sale del punto de venta y ya no se puede vender; las facturas ya emitidas no cambian.", filas,
                "Dar de baja", false);
    }

    // ---- Interfaz ----

    private void guardar() {
        controlador.guardar(codigoEditado, marca.getTexto(), modelo.getTexto(), precio.getTexto());
    }

    private void editar(TelefonoModelo telefono) {
        codigoEditado = telefono.getCodTelefono();
        marca.setTexto(telefono.getMarca());
        modelo.setTexto(telefono.getModelo());
        precio.setTexto(telefono.getPrecio().toPlainString());
        tituloFormulario.setText("Editar teléfono");
        guardar.setText("Guardar cambios");
        guardar.setIcono(Iconos.Nombre.EDITAR);
        zonaCodigo.vaciar();
        zonaCodigo.agregar(Componentes.fila(0, Componentes.chip("Código " + telefono.getCodTelefono(), AZUL_SUAVE,
                AZUL_HOVER)), XS);
        cancelar.setVisible(true);
        mensaje.ocultar();
        revalidate();
        repaint();
        marca.enfocar();
    }

    private Caja formulario() {
        Caja tarjeta = Componentes.tarjeta();
        tarjeta.agregar(Componentes.sobretitulo("Formulario"));
        tarjeta.agregar(tituloFormulario, XXS);
        tarjeta.agregar(zonaCodigo);
        tarjeta.agregar(marca, L);
        tarjeta.agregar(modelo, M);
        tarjeta.agregar(precio, M);
        tarjeta.agregar(guardar, L);
        tarjeta.agregar(cancelar, XS);
        return tarjeta;
    }

    private Caja tablaTelefonos(List<TelefonoModelo> telefonos) {
        Caja tarjeta = Componentes.tarjeta();
        JPanel titulo = new JPanel(new BorderLayout(S, 0));
        titulo.setOpaque(false);
        titulo.add(Componentes.tituloSeccion("Catálogo", "Teléfonos a la venta"), BorderLayout.CENTER);
        titulo.add(Componentes.enCentro(Componentes.chip(telefonos.size() + (telefonos.size() == 1 ? " modelo" : " modelos"),
                AZUL_SUAVE, AZUL_HOVER)), BorderLayout.EAST);
        tarjeta.agregar(titulo);

        List<Object[]> filas = new ArrayList<>();
        for (TelefonoModelo telefono : telefonos) {
            BotonIcono editar = new BotonIcono(Iconos.Nombre.EDITAR, AZUL_ACCION, 20, AZUL_SUAVE,
                    "Editar " + telefono.getNombre());
            editar.addActionListener(e -> editar(telefono));
            BotonIcono baja = new BotonIcono(Iconos.Nombre.ELIMINAR, ERROR_RELLENO, 20, ERROR_FONDO,
                    "Dar de baja " + telefono.getNombre());
            baja.addActionListener(e -> controlador.darDeBaja(telefono));
            accionesLista.add(editar);
            accionesLista.add(baja);
            filas.add(new Object[]{
                new Pila()
                        .agregar(Componentes.etiqueta(Formato.valorO(telefono.getModelo()), CUERPO_GRANDE_SEMINEGRITA, TEXTO))
                        .agregar(Componentes.etiqueta(Formato.valorO(telefono.getMarca()) + " · código "
                                + telefono.getCodTelefono(), LEYENDA, TEXTO_SECUNDARIO), 2),
                Formato.importe(telefono.getPrecio()), Formato.importe(telefono.getPrecioEfectivo()),
                Componentes.chipEstado(telefono.getEstado()), Componentes.fila(XXS, editar, baja)});
        }
        int derecha = SwingConstants.RIGHT;
        tarjeta.agregar(ComponentesTienda.tabla(new String[]{"Teléfono", "Precio", "En efectivo", "Estado", "Acciones"},
                new int[]{SwingConstants.LEFT, derecha, derecha, SwingConstants.CENTER, SwingConstants.CENTER},
                new double[]{1.8, 0.8, 0.8, 0.7, 0.6}, filas), M);
        return tarjeta;
    }
}

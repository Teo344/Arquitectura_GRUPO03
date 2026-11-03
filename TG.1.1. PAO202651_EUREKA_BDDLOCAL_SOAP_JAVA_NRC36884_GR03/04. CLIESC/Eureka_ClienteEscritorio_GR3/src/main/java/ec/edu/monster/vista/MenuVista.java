package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import ec.edu.monster.controlador.MenuControlador;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.modelo.TipoTransaccion;
import ec.edu.monster.servicio.ClienteWSEureka;
import ec.edu.monster.vista.Componentes.Boton;
import ec.edu.monster.vista.Componentes.EstiloBoton;
import ec.edu.monster.vista.Componentes.Pila;
import ec.edu.monster.vista.Componentes.TarjetaAccion;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/** Vista del menú principal: muestra la sesión y delega cada opción al MenuControlador. */
public class MenuVista extends JPanel {

    private static final int COLUMNAS = 3;

    private final Ventana ventana;
    private final MenuControlador controlador;
    private final Boton salir = new Boton("Salir", EstiloBoton.PELIGRO, Iconos.Nombre.SALIR);
    private final List<TarjetaAccion> opciones = new ArrayList<>();

    public MenuVista(Ventana ventana, ClienteWSEureka clienteWs, SesionModelo sesion) {
        super(new BorderLayout());
        this.ventana = ventana;
        this.controlador = new MenuControlador(this, clienteWs);
        setBackground(SUPERFICIE);
        ventana.setAlCerrar(controlador::salir);

        salir.addActionListener(e -> controlador.cerrarSesion());
        String nombre = sesion.getNombreEmpleado() == null ? "empleado" : sesion.getNombreEmpleado();
        add(Componentes.barraSuperior("Hola, " + nombre,
                "Sucursal " + Componentes.valorO(sesion.getCodigoSucursal()) + " · "
                + Componentes.valorO(sesion.getSucursal()), null, salir), BorderLayout.NORTH);

        Pila contenido = new Pila()
                .agregar(Componentes.sobretitulo("Panel de operaciones"))
                .agregar(Componentes.etiqueta("¿Qué deseas hacer hoy?", TITULAR_PEQUENO, TEXTO))
                .agregar(Componentes.fila(0, Componentes.chip(
                        "Sesión activa hasta las " + Componentes.horaLocal(sesion.getExpiraEn())
                        + " · se renueva con cada operación",
                        CIAN_SUAVE, CIAN_TEXTO_OSCURO, Iconos.Nombre.INFO, CIAN_TEXTO)), M)
                .agregar(Componentes.tituloSeccion("Consultas", "Información de cuentas"), XL + XS)
                .agregar(cuadricula(
                        new TarjetaAccion("Consultar cuenta", "Titular, moneda, saldo y estado de una cuenta.",
                                Iconos.Nombre.CUENTA, false, controlador::abrirConsultaCuenta),
                        new TarjetaAccion("Movimientos", "Historial de ingresos y salidas de una cuenta.",
                                Iconos.Nombre.MOVIMIENTOS, false, controlador::abrirMovimientos)), M)
                .agregar(Componentes.tituloSeccion("Transacciones", "Registrar una operación"), XL + XS)
                .agregar(cuadricula(
                        new TarjetaAccion("Depósito", "Suma un importe al saldo de una cuenta.",
                                Iconos.Nombre.DEPOSITO, false,
                                () -> controlador.abrirTransaccion(TipoTransaccion.DEPOSITO)),
                        new TarjetaAccion("Retiro", "Descuenta un importe con saldo suficiente.",
                                Iconos.Nombre.RETIRO, false,
                                () -> controlador.abrirTransaccion(TipoTransaccion.RETIRO)),
                        new TarjetaAccion("Transferencia", "Mueve fondos entre cuentas de la misma moneda.",
                                Iconos.Nombre.TRANSFERENCIA, true,
                                () -> controlador.abrirTransaccion(TipoTransaccion.TRANSFERENCIA))), M);
        add(Componentes.desplazable(Componentes.centrado(contenido, ANCHO_CONTENIDO, L, false)), BorderLayout.CENTER);
    }

    // ---- Métodos que usa el controlador ----

    public void setProcesando(boolean procesando) {
        salir.setCargando(procesando);
        opciones.forEach(opcion -> opcion.setEnabled(!procesando));
    }

    public void mostrarCuenta(ClienteWSEureka clienteWs, boolean verMovimientos) {
        ventana.mostrar(new CuentaVista(this, clienteWs, verMovimientos));
    }

    public void mostrarTransaccion(ClienteWSEureka clienteWs, TipoTransaccion tipo) {
        ventana.mostrar(new TransaccionVista(this, clienteWs, tipo));
    }

    public void volverAlLogin(String mensaje, boolean esError) {
        controlador.detener();
        ventana.abrirLogin(mensaje, esError);
    }

    public void cerrarAplicacion() {
        ventana.cerrar();
    }

    /** Las pantallas de consulta y transacción regresan aquí al pulsar Volver. */
    void regresar() {
        ventana.mostrar(this);
    }

    // ---- Interfaz ----

    /** Distribuye las tarjetas en filas de tres columnas del mismo ancho. */
    private JPanel cuadricula(TarjetaAccion... tarjetas) {
        JPanel grilla = new JPanel(new GridLayout(0, COLUMNAS, M - 2 * Componentes.MARGEN_SOMBRA,
                M - 2 * Componentes.MARGEN_SOMBRA));
        grilla.setOpaque(false);
        for (TarjetaAccion tarjeta : tarjetas) {
            opciones.add(tarjeta);
            grilla.add(tarjeta);
        }
        for (int i = tarjetas.length; i % COLUMNAS != 0; i++) {
            JPanel vacio = new JPanel();
            vacio.setOpaque(false);
            grilla.add(vacio);
        }
        return grilla;
    }
}

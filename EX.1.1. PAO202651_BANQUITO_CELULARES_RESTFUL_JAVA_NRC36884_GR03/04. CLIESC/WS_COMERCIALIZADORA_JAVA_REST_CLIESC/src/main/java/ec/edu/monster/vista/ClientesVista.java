package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import ec.edu.monster.controlador.CreditoControlador;
import ec.edu.monster.controlador.CreditoControlador.ConsultaCliente;
import ec.edu.monster.modelo.CreditoModelo;
import ec.edu.monster.modelo.CuotaModelo;
import ec.edu.monster.servicio.ClienteRESTMonsterCell;
import ec.edu.monster.vista.Componentes.Alerta;
import ec.edu.monster.vista.Componentes.Boton;
import ec.edu.monster.vista.Componentes.BotonIcono;
import ec.edu.monster.vista.Componentes.CampoTexto;
import ec.edu.monster.vista.Componentes.Caja;
import ec.edu.monster.vista.Componentes.EstiloBoton;
import ec.edu.monster.vista.Componentes.Pila;
import ec.edu.monster.vista.Componentes.Texto;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Crédito BanQuito de un comprador (CU-07 y CU-12), como clientes.jsp y
 * credito.jsp del cliente web: la evaluación del banco (sujeto de crédito y
 * monto máximo), sus créditos y la tabla de amortización de cada uno. Solo
 * muestra datos y delega las consultas al CreditoControlador.
 */
public class ClientesVista extends SeccionVista {

    private final CreditoControlador controlador;
    private ConsultaCliente consulta;

    private final Alerta mensaje = new Alerta();
    private final CampoTexto cedula = new CampoTexto("Cédula del comprador", "1712345678",
            "10 dígitos, por ejemplo 1712345678", false);
    private final Boton consultar = new Boton("Consultar en BanQuito", EstiloBoton.PRIMARIO, Iconos.Nombre.BUSCAR);
    private final Pila cuerpo = new Pila();
    private final Pila resultado = new Pila();
    private final List<BotonIcono> accionesCreditos = new ArrayList<>();

    public ClientesVista(PrincipalVista principal, ClienteRESTMonsterCell clienteRest) {
        super(principal);
        this.controlador = new CreditoControlador(this, clienteRest);
        consultar.addActionListener(e -> controlador.consultar(cedula.getTexto()));
        cedula.alConfirmar(() -> controlador.consultar(cedula.getTexto()));
        add(Componentes.desplazable(Componentes.centrado(cuerpo, ANCHO_CONTENIDO, L, false)), BorderLayout.CENTER);
        mostrarConsulta();
    }

    /** Desde una factura a crédito: directamente la tabla de ese crédito; Volver muestra al comprador. */
    public ClientesVista(PrincipalVista principal, ClienteRESTMonsterCell clienteRest, String cedulaComprador,
            int codCredito) {
        this(principal, clienteRest);
        cedula.setTexto(cedulaComprador);
        controlador.verTabla(codCredito);
    }

    // ---- Métodos que usa el controlador ----

    @Override
    public void setProcesando(boolean procesando) {
        consultar.setCargando(procesando);
        cedula.setHabilitado(!procesando);
        accionesCreditos.forEach(boton -> boton.setEnabled(!procesando));
        if (procesando) {
            mensaje.ocultar();
        }
    }

    @Override
    public void mostrarMensaje(String texto, boolean esError) {
        mensaje.mostrar(texto, esError);
    }

    @Override
    void detener() {
        controlador.detener();
    }

    public void mostrarCliente(ConsultaCliente nueva) {
        consulta = nueva;
        mostrarConsulta();
    }

    public void mostrarTabla(int codCredito, List<CuotaModelo> cuotas) {
        cuerpo.vaciar();
        String comprador = cedula.getTexto().trim();
        cuerpo.agregar(Componentes.encabezado("Crédito BanQuito", "Crédito N.º " + codCredito,
                "Tabla de amortización" + (comprador.isEmpty() ? "" : " · cédula " + comprador), this::volver, null));
        cuerpo.agregar(mensaje, M);
        Caja tarjeta = Componentes.tarjeta();
        ResumenCredito resumen = ResumenCredito.de(cuotas);
        if (resumen == null) {
            tarjeta.agregar(Componentes.estadoVacio(Iconos.Nombre.CALCULADORA, "Sin cuotas",
                    "El banco no devolvió cuotas para este crédito."));
        } else {
            tarjeta.agregar(ComponentesTienda.resumenCredito(resumen));
            tarjeta.agregar(ComponentesTienda.tablaAmortizacion(cuotas), M);
        }
        cuerpo.agregar(tarjeta, M);
        refrescar();
    }

    // ---- Interfaz ----

    private void volver() {
        mostrarConsulta();
        if (consulta == null && !cedula.getTexto().isBlank()) {
            controlador.consultar(cedula.getTexto());
        }
    }

    private void mostrarConsulta() {
        cuerpo.vaciar();
        cuerpo.agregar(Componentes.encabezado("Crédito BanQuito", "Consultar cliente",
                "Si el comprador es sujeto de crédito, su monto máximo y sus créditos en el banco.", null, null));
        cuerpo.agregar(mensaje, M);
        cuerpo.agregar(Componentes.dosColumnas(busqueda(), resultado, 0.36, L - 2 * Componentes.MARGEN_SOMBRA, false),
                L - Componentes.MARGEN_SOMBRA);
        actualizarResultado();
        refrescar();
    }

    private Caja busqueda() {
        Caja tarjeta = Componentes.tarjeta();
        tarjeta.agregar(Componentes.sobretitulo("Consulta"));
        tarjeta.agregar(Componentes.etiqueta("Buscar comprador", TITULO, TEXTO), XXS);
        tarjeta.agregar(cedula, L);
        tarjeta.agregar(consultar, L);
        tarjeta.agregar(new Texto("BanQuito revisa sus reglas de crédito: depósitos del último mes, edad y créditos"
                + " activos.", LEYENDA, TEXTO_SECUNDARIO), M);
        return tarjeta;
    }

    private void actualizarResultado() {
        accionesCreditos.clear();
        resultado.vaciar();
        if (consulta == null) {
            resultado.agregar(Componentes.estadoVacio(Iconos.Nombre.PERSONA, "Sin consulta",
                    "Escribe la cédula del comprador para ver su evaluación de crédito."));
            return;
        }
        resultado.agregar(ComponentesTienda.tarjetaEvaluacion(consulta.evaluacion(), false));
        if (consulta.evaluacion().isCliente()) {
            resultado.agregar(listaCreditos(consulta.creditos()), M - 2 * Componentes.MARGEN_SOMBRA);
        }
    }

    private Caja listaCreditos(List<CreditoModelo> creditos) {
        Caja tarjeta = Componentes.tarjeta();
        JPanel titulo = new JPanel(new BorderLayout(S, 0));
        titulo.setOpaque(false);
        titulo.add(Componentes.tituloSeccion("BanQuito", "Créditos del comprador"), BorderLayout.CENTER);
        titulo.add(Componentes.enCentro(Componentes.chip(creditos.size() + (creditos.size() == 1 ? " crédito" : " créditos"),
                AZUL_SUAVE, AZUL_HOVER)), BorderLayout.EAST);
        tarjeta.agregar(titulo);
        if (creditos.isEmpty()) {
            tarjeta.agregar(new Texto("El comprador no tiene créditos en BanQuito.", CUERPO, TEXTO_SECUNDARIO), M);
            return tarjeta;
        }
        List<Object[]> filas = new ArrayList<>();
        for (CreditoModelo credito : creditos) {
            BotonIcono ver = new BotonIcono(Iconos.Nombre.CALCULADORA, AZUL_ACCION, 20, AZUL_SUAVE,
                    "Ver la tabla de amortización del crédito " + credito.getCodCredito());
            ver.addActionListener(e -> controlador.verTabla(credito.getCodCredito()));
            accionesCreditos.add(ver);
            filas.add(new Object[]{String.valueOf(credito.getCodCredito()), Formato.fecha(credito.getFecha()),
                Formato.importe(credito.getMonto()), String.valueOf(credito.getNumCuotas()),
                Formato.importe(credito.getValorCuota()), Componentes.chipEstado(credito.getEstado()), ver});
        }
        int derecha = SwingConstants.RIGHT;
        tarjeta.agregar(ComponentesTienda.tabla(new String[]{"N.º", "Fecha", "Monto", "Cuotas", "Cuota", "Estado", "Tabla"},
                new int[]{SwingConstants.LEFT, SwingConstants.LEFT, derecha, derecha, derecha, SwingConstants.CENTER,
                    SwingConstants.CENTER},
                new double[]{0.4, 0.9, 0.9, 0.5, 0.8, 0.8, 0.4}, filas), M);
        return tarjeta;
    }

    private void refrescar() {
        cuerpo.revalidate();
        cuerpo.repaint();
    }
}

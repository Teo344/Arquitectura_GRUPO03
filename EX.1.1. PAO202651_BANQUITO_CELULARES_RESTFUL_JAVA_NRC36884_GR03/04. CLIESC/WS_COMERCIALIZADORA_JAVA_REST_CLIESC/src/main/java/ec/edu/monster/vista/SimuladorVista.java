package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import ec.edu.monster.controlador.SimuladorControlador;
import ec.edu.monster.modelo.CarritoModelo;
import ec.edu.monster.modelo.CuotaModelo;
import ec.edu.monster.servicio.ClienteRESTMonsterCell;
import ec.edu.monster.vista.Componentes.Alerta;
import ec.edu.monster.vista.Componentes.Boton;
import ec.edu.monster.vista.Componentes.CampoTexto;
import ec.edu.monster.vista.Componentes.Caja;
import ec.edu.monster.vista.Componentes.EstiloBoton;
import ec.edu.monster.vista.Componentes.Pila;
import ec.edu.monster.vista.Componentes.Texto;
import ec.edu.monster.vista.ComponentesTienda.Contador;
import java.awt.BorderLayout;
import java.util.List;

/**
 * Simulador de cuotas (CU-08), como simulador.jsp del cliente web: monto y
 * número de cuotas a la izquierda y la tabla de amortización a la derecha,
 * sin otorgar el crédito.
 */
public class SimuladorVista extends SeccionVista {

    private final SimuladorControlador controlador;

    private final Alerta mensaje = new Alerta();
    private final CampoTexto monto = new CampoTexto("Monto del crédito", "450.00",
            "Mayor que cero, con máximo dos decimales.", false);
    private final Contador cuotas = new Contador(6, CarritoModelo.CUOTAS_MINIMO, CarritoModelo.CUOTAS_MAXIMO, 96,
            n -> n + " cuotas", n -> {
            });
    private final Boton simular = new Boton("Simular", EstiloBoton.PRIMARIO, Iconos.Nombre.CALCULADORA);
    private final Pila resultado = new Pila();

    public SimuladorVista(PrincipalVista principal, ClienteRESTMonsterCell clienteRest) {
        super(principal);
        this.controlador = new SimuladorControlador(this, clienteRest);
        simular.addActionListener(e -> simular());
        monto.alConfirmar(this::simular);

        Pila contenido = new Pila()
                .agregar(Componentes.encabezado("Crédito BanQuito", "Simulador de cuotas",
                        "La tabla que tendría un crédito directo, sin otorgarlo. Tasa 16.5 % anual, cuota fija mensual.",
                        null, null))
                .agregar(mensaje, M)
                .agregar(Componentes.dosColumnas(formulario(), resultado, 0.32, L - 2 * Componentes.MARGEN_SOMBRA, false),
                        L - Componentes.MARGEN_SOMBRA);
        add(Componentes.desplazable(Componentes.centrado(contenido, ANCHO_CONTENIDO, L, false)), BorderLayout.CENTER);
        mostrarTabla(null);
    }

    // ---- Métodos que usa el controlador ----

    @Override
    public void setProcesando(boolean procesando) {
        simular.setCargando(procesando);
        monto.setHabilitado(!procesando);
        cuotas.setHabilitado(!procesando);
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

    public void mostrarTabla(List<CuotaModelo> tabla) {
        resultado.vaciar();
        ResumenCredito resumen = ResumenCredito.de(tabla);
        if (resumen == null) {
            resultado.agregar(Componentes.estadoVacio(Iconos.Nombre.CALCULADORA, "Sin simulación",
                    "Ingresa el monto y el número de cuotas para ver la tabla de amortización."));
        } else {
            Caja tarjeta = Componentes.tarjeta();
            tarjeta.agregar(Componentes.tituloSeccion("Simulación", "Tabla de amortización"));
            tarjeta.agregar(ComponentesTienda.resumenCredito(resumen), M);
            tarjeta.agregar(ComponentesTienda.tablaAmortizacion(tabla), M);
            resultado.agregar(tarjeta);
        }
        resultado.revalidate();
        resultado.repaint();
    }

    // ---- Interfaz ----

    private void simular() {
        controlador.simular(monto.getTexto(), cuotas.getValor());
    }

    private Caja formulario() {
        Caja tarjeta = Componentes.tarjeta();
        tarjeta.agregar(Componentes.sobretitulo("Datos del crédito"));
        tarjeta.agregar(Componentes.etiqueta("Simular", TITULO, TEXTO), XXS);
        tarjeta.agregar(monto, L);
        tarjeta.agregar(Componentes.etiqueta("Número de cuotas", BOTON, TEXTO), M);
        tarjeta.agregar(Componentes.fila(0, cuotas), 6);
        tarjeta.agregar(Componentes.etiqueta("Entre 3 y 18 cuotas mensuales.", LEYENDA, TEXTO_SECUNDARIO), XXS);
        tarjeta.agregar(simular, L);
        tarjeta.agregar(new Texto("Ejemplo del enunciado: 450.00 a 6 cuotas da una cuota de 78.65.", LEYENDA,
                TEXTO_SECUNDARIO), M);
        return tarjeta;
    }
}

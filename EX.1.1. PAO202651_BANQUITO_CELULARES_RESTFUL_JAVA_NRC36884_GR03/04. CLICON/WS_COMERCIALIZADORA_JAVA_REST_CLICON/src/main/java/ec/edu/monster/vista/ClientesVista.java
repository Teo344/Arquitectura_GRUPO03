package ec.edu.monster.vista;

import ec.edu.monster.controlador.CreditoControlador;
import ec.edu.monster.modelo.CreditoModelo;
import ec.edu.monster.util.EntradaUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Crédito BanQuito de un comprador (CU-07 y CU-12): evaluación, monto máximo,
 * créditos y tabla de amortización del crédito que elija.
 */
public class ClientesVista {

    private static final String FILA_CREDITO = "%6s  %-10s  %12s  %6s  %12s  %-10s";

    private final MensajesVista mensajes = new MensajesVista();
    private final ComponentesTienda componentes = new ComponentesTienda();
    private final CreditoControlador creditoControlador;

    public ClientesVista(CreditoControlador creditoControlador) {
        this.creditoControlador = creditoControlador;
    }

    public void consultar() {
        mensajes.mostrarTitulo("CONSULTAR CLIENTE");
        String cedula = EntradaUtil.leerTextoNoVacio("Cedula del comprador (10 digitos): ");
        CreditoControlador.ConsultaCliente consulta = creditoControlador.consultar(cedula);
        componentes.tarjetaEvaluacion(consulta.evaluacion());
        if (!consulta.evaluacion().isCliente()) {
            return;
        }
        if (consulta.creditos().isEmpty()) {
            mensajes.mostrarMensaje("El comprador no tiene creditos en BanQuito.");
            return;
        }
        mostrarCreditos(consulta.creditos());
        verTablas(consulta);
    }

    private void mostrarCreditos(List<CreditoModelo> creditos) {
        mensajes.mostrarTitulo("CREDITOS DEL COMPRADOR");
        List<Object[]> filas = new ArrayList<>();
        for (CreditoModelo credito : creditos) {
            filas.add(new Object[]{credito.getCodCredito(), Formato.fecha(credito.getFecha()),
                Formato.importe(credito.getMonto()), credito.getNumCuotas(), Formato.importe(credito.getValorCuota()),
                Formato.valorO(credito.getEstado())});
        }
        componentes.tabla(FILA_CREDITO, new Object[]{"No.", "Fecha", "Monto", "Cuotas", "Cuota", "Estado"}, filas);
    }

    private void verTablas(CreditoControlador.ConsultaCliente consulta) {
        while (true) {
            int codigo = EntradaUtil.leerEntero("Numero de credito para ver su tabla (0 para volver): ");
            if (codigo == 0) {
                return;
            }
            try {
                componentes.amortizacion("TABLA DE AMORTIZACION  |  CREDITO No. " + codigo,
                        creditoControlador.verTabla(consulta, codigo));
            } catch (IllegalArgumentException ex) {
                mensajes.mostrarError(ex.getMessage());
            }
        }
    }
}

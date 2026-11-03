package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import ec.edu.monster.modelo.CuotaModelo;
import ec.edu.monster.modelo.EvaluacionClienteModelo;
import ec.edu.monster.modelo.FormaPago;
import ec.edu.monster.modelo.LineaCarrito;
import ec.edu.monster.modelo.TelefonoModelo;
import ec.edu.monster.vista.Componentes.BotonIcono;
import ec.edu.monster.vista.Componentes.Boton;
import ec.edu.monster.vista.Componentes.Caja;
import ec.edu.monster.vista.Componentes.EstiloBoton;
import ec.edu.monster.vista.Componentes.IconoEnCaja;
import ec.edu.monster.vista.Componentes.Pila;
import ec.edu.monster.vista.Componentes.Texto;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Componentes de la tienda (catálogo, venta, tablas, evaluación del banco),
 * los mismos de la sección "Tienda" de css/monstercell.css del cliente web,
 * hechos solo con los valores de {@link Tema}.
 */
public final class ComponentesTienda {

    private static final Cursor MANO = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    private ComponentesTienda() {
    }

    // ================================================================ Barra lateral

    /** Opción de la barra lateral: icono, texto, resaltada si es la sección actual y un contador opcional. */
    public static class ItemLateral extends JButton {

        private final Iconos.Nombre icono;
        private boolean activo;
        private int contador;

        public ItemLateral(String texto, Iconos.Nombre icono, Runnable accion) {
            super(texto);
            this.icono = icono;
            setFont(BOTON_MEDIA);
            setHorizontalAlignment(SwingConstants.LEFT);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setBorder(new EmptyBorder(0, S, 0, S));
            setIconTextGap(S);
            setCursor(MANO);
            addActionListener(e -> accion.run());
            setActivo(false);
        }

        public final void setActivo(boolean activo) {
            this.activo = activo;
            setForeground(activo ? BLANCO : BORDE_SUAVE);
            setFont(activo ? BOTON_NEGRITA : BOTON_MEDIA);
            setIcon(Iconos.icono(icono, activo ? BLANCO : BORDE_SUAVE, 20));
            getAccessibleContext().setAccessibleDescription(activo ? "Sección actual" : null);
            repaint();
        }

        /** Unidades de la venta en curso; con 0 no se muestra. */
        public void setContador(int contador) {
            this.contador = contador;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension tamano = super.getPreferredSize();
            tamano.height = 44;
            return tamano;
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, 44);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = Componentes.suave(graphics);
            Color fondo = activo ? AZUL_ACCION
                    : getModel().isRollover() || isFocusOwner() ? new Color(255, 255, 255, 20) : null;
            if (fondo != null) {
                g.setColor(fondo);
                g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 2f * RADIO_CAMPO, 2f * RADIO_CAMPO));
            }
            if (contador > 0) {
                String texto = String.valueOf(contador);
                g.setFont(LEYENDA);
                FontMetrics medidas = g.getFontMetrics();
                int ancho = Math.max(22, medidas.stringWidth(texto) + XS * 2);
                int alto = 22;
                int x = getWidth() - S - ancho;
                int y = (getHeight() - alto) / 2;
                g.setColor(activo ? BLANCO : CIAN);
                g.fill(new RoundRectangle2D.Float(x, y, ancho, alto, alto, alto));
                g.setColor(activo ? AZUL_ACCION : AZUL_OSCURO);
                g.drawString(texto, x + (ancho - medidas.stringWidth(texto)) / 2,
                        y + (alto - medidas.getHeight()) / 2 + medidas.getAscent());
            }
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    /** Círculo con las iniciales del empleado. */
    public static JComponent avatar(String iniciales) {
        JComponent avatar = new JComponent() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = Componentes.suave(graphics);
                g.setColor(CIAN);
                g.fill(new Ellipse2D.Float(0, 0, 40, 40));
                g.setFont(BOTON_NEGRITA);
                g.setColor(AZUL_OSCURO);
                FontMetrics medidas = g.getFontMetrics();
                g.drawString(iniciales, (40 - medidas.stringWidth(iniciales)) / 2,
                        (40 - medidas.getHeight()) / 2 + medidas.getAscent());
                g.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(40, 40));
        return avatar;
    }

    // ================================================================ Formularios

    /**
     * Selector numérico con − y + (cantidades y número de cuotas). texto
     * convierte el valor en lo que se muestra: "6 cuotas".
     */
    public static class Contador extends Caja {

        private final int minimo;
        private final int maximo;
        private final IntFunction<String> texto;
        private final IntConsumer alCambiar;
        private final BotonIcono menos = new BotonIcono(Iconos.Nombre.QUITAR, AZUL_ACCION, 18, AZUL_SUAVE, "Menos");
        private final BotonIcono mas = new BotonIcono(Iconos.Nombre.AGREGAR, AZUL_ACCION, 18, AZUL_SUAVE, "Más");
        private final JLabel etiqueta;
        private int valor;

        public Contador(int valor, int minimo, int maximo, int anchoTexto, IntFunction<String> texto,
                IntConsumer alCambiar) {
            super(BLANCO, BORDE_CAMPO, RADIO_CAMPO, 0, 0, 2);
            this.minimo = minimo;
            this.maximo = maximo;
            this.texto = texto;
            this.alCambiar = alCambiar;
            etiqueta = Componentes.etiqueta("", CUERPO_GRANDE_SEMINEGRITA, TEXTO);
            etiqueta.setHorizontalAlignment(SwingConstants.CENTER);
            etiqueta.setPreferredSize(new Dimension(anchoTexto, 32));
            menos.setDisabledIcon(Iconos.icono(Iconos.Nombre.QUITAR, DESHABILITADO_TEXTO, 18));
            mas.setDisabledIcon(Iconos.icono(Iconos.Nombre.AGREGAR, DESHABILITADO_TEXTO, 18));
            menos.addActionListener(e -> cambiar(this.valor - 1));
            mas.addActionListener(e -> cambiar(this.valor + 1));
            JPanel fila = new JPanel(new BorderLayout());
            fila.setOpaque(false);
            fila.add(menos, BorderLayout.WEST);
            fila.add(etiqueta, BorderLayout.CENTER);
            fila.add(mas, BorderLayout.EAST);
            agregar(fila);
            setValor(valor);
        }

        public int getValor() {
            return valor;
        }

        /** Cambia el valor mostrado sin avisar (cuando el modelo ya lo tiene). */
        public final void setValor(int valor) {
            this.valor = Math.max(minimo, Math.min(maximo, valor));
            etiqueta.setText(texto.apply(this.valor));
            menos.setEnabled(this.valor > minimo);
            mas.setEnabled(this.valor < maximo);
        }

        public void setHabilitado(boolean habilitado) {
            menos.setEnabled(habilitado && valor > minimo);
            mas.setEnabled(habilitado && valor < maximo);
        }

        private void cambiar(int nuevo) {
            int anterior = valor;
            setValor(nuevo);
            if (valor != anterior) {
                alCambiar.accept(valor);
            }
        }
    }

    /** Número de paso (círculo azul suave) y su título: "1 Comprador". */
    public static JPanel paso(int numero, String titulo) {
        JComponent circulo = new JComponent() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = Componentes.suave(graphics);
                g.setColor(AZUL_SUAVE);
                g.fill(new Ellipse2D.Float(0, 0, 26, 26));
                g.setFont(LEYENDA.deriveFont(java.awt.Font.BOLD));
                g.setColor(AZUL_ACCION);
                String texto = String.valueOf(numero);
                FontMetrics medidas = g.getFontMetrics();
                g.drawString(texto, (26 - medidas.stringWidth(texto)) / 2,
                        (26 - medidas.getHeight()) / 2 + medidas.getAscent());
                g.dispose();
            }
        };
        circulo.setPreferredSize(new Dimension(26, 26));
        return Componentes.fila(XS, circulo, Componentes.etiqueta(titulo, CUERPO_GRANDE_NEGRITA, TEXTO));
    }

    // ================================================================ Catálogo y venta

    /**
     * Teléfono del catálogo del punto de venta: precio, precio en efectivo y
     * el botón Agregar. Si ya está en la venta se marca con su cantidad.
     */
    public static Caja tarjetaProducto(TelefonoModelo telefono, int enVenta, Runnable agregar) {
        Caja tarjeta = new Caja(BLANCO, enVenta > 0 ? AZUL_FOCO : DIVISOR, RADIO_TARJETA, 2,
                Componentes.MARGEN_SOMBRA, M);
        JPanel arriba = new JPanel(new BorderLayout(S, 0));
        arriba.setOpaque(false);
        arriba.add(new IconoEnCaja(Iconos.Nombre.CELULAR, AZUL_SUAVE, AZUL_ACCION, 40), BorderLayout.WEST);
        if (enVenta > 0) {
            arriba.add(Componentes.enCentro(Componentes.chip(enVenta + " en la venta", CIAN_SUAVE, CIAN_TEXTO_OSCURO,
                    Iconos.Nombre.CARRITO, CIAN_TEXTO)), BorderLayout.EAST);
        }
        tarjeta.agregar(arriba);
        tarjeta.agregar(Componentes.sobretitulo(Formato.valorO(telefono.getMarca()), TEXTO_SECUNDARIO), S);
        tarjeta.agregar(new Texto(Formato.valorO(telefono.getModelo()), TITULO, TEXTO), XXS);
        tarjeta.agregar(Componentes.etiqueta(Formato.importe(telefono.getPrecio()), CUERPO_GRANDE_NEGRITA, AZUL_OSCURO), XS);
        tarjeta.agregar(Componentes.etiqueta("En efectivo " + Formato.importe(telefono.getPrecioEfectivo()), LEYENDA,
                EXITO_TEXTO), 2);
        Boton boton = new Boton("Agregar", EstiloBoton.SECUNDARIO, Iconos.Nombre.AGREGAR);
        boton.getAccessibleContext().setAccessibleName("Agregar " + telefono.getNombre());
        boton.addActionListener(e -> agregar.run());
        tarjeta.agregar(boton, M);
        return tarjeta;
    }

    /** Una línea de la venta: teléfono, precio, cantidad con − y +, subtotal y quitar. */
    public static JPanel lineaVenta(LineaCarrito linea, int maximo, IntConsumer cambiarCantidad, Runnable quitar) {
        JPanel fila = new JPanel(new BorderLayout(S, 0));
        fila.setOpaque(false);
        fila.setBorder(new EmptyBorder(S, 0, S, 0));
        fila.add(new Pila()
                .agregar(new Texto(linea.getTelefono().getNombre(), CUERPO_GRANDE_SEMINEGRITA, TEXTO))
                .agregar(Componentes.etiqueta(Formato.importe(linea.getTelefono().getPrecio()) + " c/u", LEYENDA,
                        TEXTO_SECUNDARIO), 2), BorderLayout.CENTER);

        Contador cantidad = new Contador(linea.getCantidad(), 1, maximo, 36, String::valueOf, cambiarCantidad);
        JLabel subtotal = Componentes.etiqueta(Formato.importe(linea.getSubtotal()), CUERPO_GRANDE_NEGRITA, TEXTO);
        subtotal.setHorizontalAlignment(SwingConstants.RIGHT);
        subtotal.setPreferredSize(new Dimension(92, 24));
        BotonIcono eliminar = new BotonIcono(Iconos.Nombre.ELIMINAR, ERROR_RELLENO, 20, ERROR_FONDO,
                "Quitar " + linea.getTelefono().getNombre());
        eliminar.addActionListener(e -> quitar.run());
        fila.add(Componentes.enCentro(Componentes.fila(S, cantidad, subtotal, eliminar)), BorderLayout.EAST);
        return fila;
    }

    /** Forma de pago con icono, no solo color: efectivo en verde, crédito en cian. */
    public static Caja chipFormaPago(FormaPago formaPago) {
        return formaPago == FormaPago.CREDITO
                ? Componentes.chip(formaPago.getNombreVisible(), CIAN_SUAVE, CIAN_TEXTO_OSCURO, Iconos.Nombre.CREDITO, CIAN_TEXTO)
                : Componentes.chip(formaPago.getNombreVisible(), EXITO_FONDO, EXITO_TEXTO, Iconos.Nombre.EXITO, EXITO_RELLENO);
    }

    // ================================================================ Crédito BanQuito

    /**
     * Resultado del banco para un comprador: nombre, si es sujeto de crédito
     * (con icono, nunca solo color), los motivos si no lo es y el monto
     * máximo. compacta es la versión de la venta, sin sombra.
     */
    public static Caja tarjetaEvaluacion(EvaluacionClienteModelo evaluacion, boolean compacta) {
        Caja tarjeta = compacta
                ? new Caja(SUPERFICIE, DIVISOR, RADIO_TARJETA, 0, 0, M)
                : Componentes.tarjeta();
        JPanel encabezado = new JPanel(new BorderLayout(S, 0));
        encabezado.setOpaque(false);
        if (!evaluacion.isCliente()) {
            encabezado.add(Componentes.enCentro(new IconoEnCaja(Iconos.Nombre.ERROR, ERROR_FONDO, ERROR_RELLENO, 44)),
                    BorderLayout.WEST);
            encabezado.add(new Pila()
                    .agregar(Componentes.etiqueta("No es cliente de BanQuito", TITULO, TEXTO))
                    .agregar(new Texto("Cédula " + Formato.valorO(evaluacion.getCedula())
                            + " · solo los clientes del banco pueden comprar", CUERPO, TEXTO_SECUNDARIO), 2),
                    BorderLayout.CENTER);
            tarjeta.agregar(encabezado);
            return tarjeta;
        }

        encabezado.add(Componentes.enCentro(new IconoEnCaja(Iconos.Nombre.PERSONA, AZUL_SUAVE, AZUL_ACCION, 44)),
                BorderLayout.WEST);
        encabezado.add(new Pila()
                .agregar(new Texto(Formato.valorO(evaluacion.getNombre()), TITULO, TEXTO))
                .agregar(new Texto("Cliente BanQuito N.º " + evaluacion.getCodCliente() + " · cédula "
                        + Formato.valorO(evaluacion.getCedula()), CUERPO, TEXTO_SECUNDARIO), 2), BorderLayout.CENTER);
        tarjeta.agregar(encabezado);

        Caja chip = evaluacion.isSujetoCredito()
                ? Componentes.chip("Sujeto de crédito", EXITO_FONDO, EXITO_TEXTO, Iconos.Nombre.EXITO, EXITO_RELLENO)
                : Componentes.chip("No es sujeto de crédito", ADVERTENCIA_FONDO, ADVERTENCIA_TEXTO,
                        Iconos.Nombre.ADVERTENCIA, ADVERTENCIA_RELLENO);
        tarjeta.agregar(Componentes.fila(0, chip), M);
        for (EvaluacionClienteModelo.Motivo motivo : evaluacion.getMotivos()) {
            JLabel linea = Componentes.etiqueta(motivo.getDescripcion(), CUERPO, ADVERTENCIA_TEXTO);
            linea.setIcon(Iconos.icono(Iconos.Nombre.ADVERTENCIA, ADVERTENCIA_RELLENO, 16));
            linea.setIconTextGap(XS);
            tarjeta.agregar(linea, XS);
        }
        tarjeta.agregar(Componentes.lineaDivisoria(), M);
        tarjeta.agregar(Componentes.filaDato("Monto máximo de crédito", Formato.importe(evaluacion.getMontoMaximo()),
                true), XXS);
        return tarjeta;
    }

    /** Datos del encabezado de una tabla de amortización en una franja azul suave. */
    public static Caja resumenCredito(ResumenCredito resumen) {
        Caja franja = new Caja(AZUL_SUAVE, BORDE_SUAVE, RADIO_CAMPO, 0, 0, M);
        JPanel datos = new JPanel(new GridLayout(1, 0, M, 0));
        datos.setOpaque(false);
        datos.add(dato("Monto", Formato.importe(resumen.getMonto())));
        datos.add(dato("Cuotas", String.valueOf(resumen.getNumCuotas())));
        datos.add(dato("Cuota mensual", Formato.importe(resumen.getValorCuota())));
        datos.add(dato("Interés total", Formato.importe(resumen.getInteresTotal())));
        datos.add(dato("Total a pagar", Formato.importe(resumen.getTotalPagado())));
        franja.agregar(datos);
        return franja;
    }

    private static Pila dato(String etiqueta, String valor) {
        return new Pila()
                .agregar(Componentes.etiqueta(etiqueta, LEYENDA, TEXTO_SECUNDARIO))
                .agregar(Componentes.etiqueta(valor, CUERPO_GRANDE_NEGRITA, AZUL_OSCURO), 2);
    }

    /** Tabla de amortización con las columnas del enunciado. */
    public static JPanel tablaAmortizacion(List<CuotaModelo> cuotas) {
        List<Object[]> filas = new ArrayList<>();
        for (CuotaModelo cuota : cuotas) {
            filas.add(new Object[]{String.valueOf(cuota.getNumero()), Formato.importe(cuota.getValorCuota()),
                Formato.importe(cuota.getInteres()), Formato.importe(cuota.getCapital()),
                Formato.importe(cuota.getSaldo())});
        }
        int derecha = SwingConstants.RIGHT;
        return tabla(new String[]{"# Cuota", "Valor Cuota", "Interés Pagado", "Capital Pagado", "Saldo"},
                new int[]{SwingConstants.LEFT, derecha, derecha, derecha, derecha},
                new double[]{0.6, 1, 1, 1, 1}, filas);
    }

    // ================================================================ Tablas

    /**
     * Tabla con encabezado y filas separadas por líneas; todas las filas
     * comparten las columnas. Cada celda es un texto o un componente (botones,
     * chips). pesos reparte el ancho sobrante entre las columnas.
     */
    public static JPanel tabla(String[] columnas, int[] alineaciones, double[] pesos, List<Object[]> filas) {
        JPanel tabla = new JPanel(new GridBagLayout());
        tabla.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        int y = 0;
        for (int i = 0; i < columnas.length; i++) {
            JLabel encabezado = Componentes.etiqueta(columnas[i], LEYENDA, TEXTO_SECUNDARIO);
            encabezado.setHorizontalAlignment(alineaciones[i]);
            agregarCelda(tabla, c, encabezado, i, y, pesos[i], alineaciones[i]);
        }
        agregarDivisor(tabla, c, ++y, columnas.length);
        for (int f = 0; f < filas.size(); f++) {
            y++;
            Object[] fila = filas.get(f);
            for (int i = 0; i < columnas.length; i++) {
                Component celda = fila[i] instanceof Component componente ? componente
                        : celdaTexto(String.valueOf(fila[i]), alineaciones[i]);
                agregarCelda(tabla, c, celda, i, y, pesos[i], alineaciones[i]);
            }
            if (f < filas.size() - 1) {
                agregarDivisor(tabla, c, ++y, columnas.length);
            }
        }
        return tabla;
    }

    private static JLabel celdaTexto(String texto, int alineacion) {
        JLabel etiqueta = Componentes.etiqueta(texto, CUERPO, TEXTO);
        etiqueta.setHorizontalAlignment(alineacion);
        return etiqueta;
    }

    private static void agregarCelda(JPanel tabla, GridBagConstraints c, Component celda, int x, int y, double peso,
            int alineacion) {
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = 1;
        c.weightx = peso;
        c.insets = new Insets(XS + 2, XS, XS + 2, XS);
        // Los componentes (botones, chips) conservan su tamaño y se alinean como el texto de su columna
        boolean esTexto = celda instanceof JLabel;
        c.fill = esTexto ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        c.anchor = alineacion == SwingConstants.RIGHT ? GridBagConstraints.LINE_END
                : alineacion == SwingConstants.CENTER ? GridBagConstraints.CENTER : GridBagConstraints.LINE_START;
        tabla.add(celda, c);
    }

    private static void agregarDivisor(JPanel tabla, GridBagConstraints c, int y, int columnas) {
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = columnas;
        c.weightx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 0);
        tabla.add(Componentes.lineaDivisoria(), c);
    }

    // ================================================================ Impresión

    /**
     * Imprime un componente (la factura) en una página, reducido si no cabe.
     * Devuelve false si el empleado cancela el diálogo de impresión.
     */
    public static boolean imprimir(JComponent componente, String nombreTrabajo) throws PrinterException {
        PrinterJob trabajo = PrinterJob.getPrinterJob();
        trabajo.setJobName(nombreTrabajo);
        trabajo.setPrintable((graphics, formato, pagina) -> {
            if (pagina > 0) {
                return Printable.NO_SUCH_PAGE;
            }
            Graphics2D g = (Graphics2D) graphics.create();
            g.translate(formato.getImageableX(), formato.getImageableY());
            double escala = Math.min(1.0, Math.min(formato.getImageableWidth() / componente.getWidth(),
                    formato.getImageableHeight() / componente.getHeight()));
            g.scale(escala, escala);
            componente.printAll(g);
            g.dispose();
            return Printable.PAGE_EXISTS;
        }, trabajo.defaultPage(new PageFormat()));
        if (!trabajo.printDialog()) {
            return false;
        }
        trabajo.print();
        return true;
    }
}

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
import ec.edu.monster.vista.Componentes.IconoEnCaja;
import ec.edu.monster.vista.Componentes.Pila;
import ec.edu.monster.vista.Componentes.Texto;
import ec.edu.monster.vista.Componentes.TipoAlerta;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Búsqueda de países por nombre en la API (CU-02). La consulta se hace solo al
 * pulsar "Buscar" (no mientras se escribe) porque cada consulta consume la
 * cuota mensual de la clave. Si la API falla, se muestra lo guardado en MySQL
 * y se avisa claramente el origen.
 *
 * @author carlo
 */
public class BusquedaVista extends JPanel {

    private final PaisControlador controlador;
    private final CampoTexto nombre = new CampoTexto("Nombre del país", "Ej. ecuador", null);
    private final Boton buscar = new Boton("Buscar", EstiloBoton.PRIMARIO, Iconos.Nombre.BUSCAR);
    private final Alerta alerta = new Alerta(true);
    private final Pila resultados = new Pila();

    public BusquedaVista(PaisControlador controlador) {
        super(new BorderLayout(0, M));
        this.controlador = controlador;
        setBackground(SUPERFICIE);
        setBorder(new EmptyBorder(L, L, M, L));

        buscar.addActionListener(e -> buscar());
        nombre.alConfirmar(this::buscar);

        add(new Pila()
                .agregar(Componentes.encabezadoPagina("Actividad 1", "Buscar en la API",
                        new Texto("Consulta REST Countries por nombre común, oficial o nativo y guarda los países que "
                                + "necesites.", CUERPO, TEXTO_SECUNDARIO)))
                .agregar(tarjetaBusqueda(), M)
                .agregar(alerta, XS), BorderLayout.NORTH);
        add(Componentes.desplazable(Componentes.centrado(resultados, Integer.MAX_VALUE, 0, false)),
                BorderLayout.CENTER);
        mostrarInicial();
    }

    public void alMostrar() {
        nombre.enfocar();
    }

    private JComponent tarjetaBusqueda() {
        JPanel fila = new JPanel(new GridBagLayout());
        fila.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.anchor = GridBagConstraints.SOUTH;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        fila.add(nombre, c);
        c.weightx = 0;
        c.insets = new Insets(0, S, 0, 0);
        fila.add(buscar, c);

        Caja tarjeta = Componentes.tarjeta();
        tarjeta.agregar(fila);
        tarjeta.agregar(new Texto("Escribe el nombre en inglés o en su idioma oficial, por ejemplo «ecuador», "
                + "«germany» o «españa». Cada búsqueda usa una de las solicitudes mensuales de tu clave.",
                LEYENDA, TEXTO_SECUNDARIO), XS);
        return tarjeta;
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
        Tarea.de(() -> controlador.buscarEnApi(texto))
                .siempre(() -> buscar.setCargando(false))
                .alTerminar(resultado -> mostrar(texto, resultado))
                .alFallar(mensaje -> alerta.mostrar(mensaje, TipoAlerta.ERROR))
                .iniciar();
    }

    private void mostrar(String texto, ResultadoBusqueda resultado) {
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
            String origen = desdeApi ? " de la API" : " guardados localmente";
            resultados.agregar(Componentes.sobretitulo(cantidad + (cantidad == 1 ? " resultado" : " resultados") + origen,
                    TEXTO_SECUNDARIO), XS);
            for (Pais pais : resultado.paises()) {
                resultados.agregar(new FilaResultado(pais, desdeApi), XS);
            }
        }
        resultados.revalidate();
        resultados.repaint();
    }

    private void mostrarInicial() {
        resultados.removeAll();
        resultados.agregar(Componentes.estadoVacio(Iconos.Nombre.BUSCAR, "Busca un país en la API",
                "Los resultados aparecerán aquí. Podrás guardar en la base de datos los que necesites.", null), L);
    }

    /** Un país encontrado: datos principales y la acción de guardarlo en MySQL. */
    private final class FilaResultado extends Caja {

        private final JPanel zonaAccion = new JPanel(new BorderLayout());

        FilaResultado(Pais pais, boolean desdeApi) {
            super(BLANCO, DIVISOR, RADIO_TARJETA, 2, Componentes.MARGEN_SOMBRA, M);
            JPanel contenido = new JPanel(new BorderLayout(M, 0));
            contenido.setOpaque(false);
            contenido.add(Componentes.enCentro(new IconoEnCaja(Iconos.Nombre.PUBLICO, AZUL_SUAVE, AZUL_ACCION, 44)),
                    BorderLayout.WEST);

            String detalle = "Capital: " + Formato.texto(pais.capital()) + "  ·  " + pais.region().getNombre()
                    + "  ·  " + Formato.entero(pais.poblacion()) + " habitantes  ·  Moneda: " + Formato.texto(pais.moneda());
            contenido.add(new Pila()
                    .agregar(Componentes.fila(XS, Componentes.etiqueta(pais.nombre(), CUERPO_GRANDE_SEMINEGRITA, TEXTO),
                            Componentes.chip(pais.codigo(), FONDO_SECCION, TEXTO_SECUNDARIO)))
                    .agregar(new Texto(detalle, CUERPO, TEXTO_SECUNDARIO), XXS), BorderLayout.CENTER);

            zonaAccion.setOpaque(false);
            if (desdeApi) {
                Boton guardar = new Boton("Guardar", EstiloBoton.SECUNDARIO, Iconos.Nombre.GUARDAR);
                guardar.getAccessibleContext().setAccessibleName("Guardar " + pais.nombre() + " en la base de datos");
                guardar.addActionListener(e -> guardar(pais, guardar));
                zonaAccion.add(Componentes.enCentro(guardar), BorderLayout.CENTER);
            } else {
                zonaAccion.add(Componentes.enCentro(Componentes.chipEstado("En la base local", TipoAlerta.INFO)),
                        BorderLayout.CENTER);
            }
            contenido.add(zonaAccion, BorderLayout.EAST);
            agregar(contenido);
            getAccessibleContext().setAccessibleName(pais.nombre() + ". " + detalle);
        }

        private void guardar(Pais pais, Boton boton) {
            alerta.ocultar();
            boton.setCargando(true, "Guardando…");
            Tarea.de(() -> controlador.guardarResultado(pais))
                    .siempre(() -> boton.setCargando(false))
                    .alTerminar(resultado -> {
                        zonaAccion.removeAll();
                        zonaAccion.add(Componentes.enCentro(Componentes.chipEstado("Guardado", TipoAlerta.EXITO)),
                                BorderLayout.CENTER);
                        zonaAccion.revalidate();
                        zonaAccion.repaint();
                        alerta.mostrar(resultado.nuevos() > 0
                                ? "«" + pais.nombre() + "» se agregó a la base de datos."
                                : "«" + pais.nombre() + "» ya estaba guardado: se actualizaron sus datos.",
                                TipoAlerta.EXITO);
                    })
                    .alFallar(mensaje -> alerta.mostrar(mensaje, TipoAlerta.ERROR))
                    .iniciar();
        }
    }
}

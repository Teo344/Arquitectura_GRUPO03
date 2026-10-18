package ec.edu.monster.controlador;

import ec.edu.monster.modelo.*;
import ec.edu.monster.servicio.ApiRest;
import ec.edu.monster.vista.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;

/** Coordina carga del catálogo y conversión de unidades. */
public final class ConversionController {
    private final ConversionView view; private final ApiRest api; private final Sesion sesion; private boolean cargando=true;
    public ConversionController(ConversionView view,ApiRest api,Sesion sesion) {
        this.view=view; this.api=api; this.sesion=sesion;
        view.categoria().addActionListener(e -> { if(!cargando) cargarUnidades(); });
        view.convertir().addActionListener(e -> convertir());
        view.limpiar().addActionListener(e -> { view.valor().setText("0"); view.estado("Listo para convertir.",false); });
        view.salir().addActionListener(e -> { MenuView m=new MenuView(sesion); new MenuController(m,api); view.dispose(); m.setVisible(true); });
        cargarCategorias();
    }
    private void cargarCategorias() {
        view.procesando(true);
        new SwingWorker<java.util.List<Categoria>,Void>() {
            @Override protected java.util.List<Categoria> doInBackground() throws Exception { return api.categorias(sesion.token()); }
            @Override protected void done() { try { view.categorias(get()); view.estado("Catálogo cargado.",false); }
                catch(Exception e) { error(e); } finally { cargando=false; view.procesando(false); cargarUnidades(); } }
        }.execute();
    }
    private void cargarUnidades() {
        Categoria c=(Categoria)view.categoria().getSelectedItem(); if(c==null)return;
        view.origen().removeAllItems(); view.destino().removeAllItems();
        new SwingWorker<java.util.List<Unidad>,Void>() {
            @Override protected java.util.List<Unidad> doInBackground() throws Exception { return api.unidades(sesion.token(),c.codigo()); }
            @Override protected void done() { try { if(view.categoria().getSelectedItem()==c) view.unidades(get()); }
                catch(Exception e) { error(e); } }
        }.execute();
    }
    private void convertir() {
        Categoria c=(Categoria)view.categoria().getSelectedItem(); Unidad o=(Unidad)view.origen().getSelectedItem(), d=(Unidad)view.destino().getSelectedItem();
        if(c==null||o==null||d==null) { view.estado("Selecciona categoría y unidades.",true); return; }
        final double n; try { n=Double.parseDouble(view.valor().getText().trim().replace(',','.')); if(!Double.isFinite(n)) throw new NumberFormatException(); }
        catch(NumberFormatException e) { view.estado("Ingresa un valor numérico válido.",true); view.valor().requestFocusInWindow(); return; }
        view.procesando(true); view.estado("Enviando conversión al servicio…",false);
        new SwingWorker<Double,Void>() {
            @Override protected Double doInBackground() throws Exception { return api.convertir(sesion.token(),c.codigo(),n,o.codigo(),d.codigo()); }
            @Override protected void done() { view.procesando(false); try { double r=get(); String fmt=format(r);
                String stamp=java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
                view.historial("<span style='color:#4A5A78;font-size:11px'>"+stamp+"</span><br>"+format(n)+" "+o.simbolo()+" → "+fmt+" "+d.simbolo());
                view.estado(format(n)+" "+o.simbolo()+" = "+fmt+" "+d.simbolo(),false); }
                catch(Exception e) { error(e); } }
        }.execute();
    }
    private String format(double n) { return new DecimalFormat("0.####",DecimalFormatSymbols.getInstance(Locale.US)).format(n); }
    private void error(Exception e) { Throwable t=e instanceof ExecutionException&&e.getCause()!=null?e.getCause():e; view.estado(t.getMessage()==null?"Error al comunicarse con el servicio.":t.getMessage(),true); }
}

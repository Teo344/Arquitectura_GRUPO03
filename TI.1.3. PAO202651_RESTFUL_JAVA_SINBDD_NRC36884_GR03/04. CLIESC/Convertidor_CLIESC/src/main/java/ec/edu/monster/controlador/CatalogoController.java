package ec.edu.monster.controlador;

import ec.edu.monster.modelo.*;
import ec.edu.monster.servicio.ApiRest;
import ec.edu.monster.vista.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;

/** Consulta categorías y unidades mediante los endpoints REST protegidos. */
public final class CatalogoController {
    private final CatalogoView view;private final ApiRest api;private java.util.List<Categoria> categorias=java.util.List.of();
    public CatalogoController(CatalogoView view,ApiRest api){this.view=view;this.api=api;view.volver().addActionListener(e->back());load();}
    private void load(){view.estado("Consultando el catálogo del servidor…",false);new SwingWorker<java.util.List<Categoria>,Void>(){
        protected java.util.List<Categoria> doInBackground()throws Exception{return api.categorias(view.sesion().token());}
        protected void done(){try{categorias=get();view.mostrarCategorias(categorias,CatalogoController.this::select);view.estado("Se cargaron "+categorias.size()+" categorías desde el servidor.",false);}catch(Exception e){error(e);}}
    }.execute();}
    private void select(java.awt.event.ActionEvent e){Categoria c=categorias.stream().filter(x->x.codigo().equals(e.getActionCommand())).findFirst().orElse(null);if(c==null)return;view.seleccionar(c);view.estado("Consultando unidades de "+c.nombre()+"…",false);
        new SwingWorker<java.util.List<Unidad>,Void>(){protected java.util.List<Unidad> doInBackground()throws Exception{return api.unidades(view.sesion().token(),c.codigo());}
            protected void done(){try{view.mostrarUnidades(get());view.estado("Unidades consultadas: "+c.nombre()+".",false);}catch(Exception ex){error(ex);}}}.execute();}
    private void back(){MenuView m=new MenuView(view.sesion());new MenuController(m,api);view.dispose();m.setVisible(true);}
    private void error(Exception e){Throwable t=e instanceof ExecutionException&&e.getCause()!=null?e.getCause():e;view.estado(t.getMessage()==null?"No se pudo consultar el catálogo REST.":t.getMessage(),true);}
}

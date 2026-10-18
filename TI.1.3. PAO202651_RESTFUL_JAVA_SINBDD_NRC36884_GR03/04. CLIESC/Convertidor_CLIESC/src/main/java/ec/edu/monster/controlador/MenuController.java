package ec.edu.monster.controlador;

import ec.edu.monster.servicio.ApiRest;
import ec.edu.monster.vista.*;

/** Navegación principal posterior a la autenticación REST. */
public final class MenuController {
    public MenuController(MenuView view,ApiRest api){
        view.convertir().addActionListener(e->{ConversionView v=new ConversionView();new ConversionController(v,api,view.sesion());view.dispose();v.setVisible(true);});
        view.catalogo().addActionListener(e->{CatalogoView v=new CatalogoView(view.sesion());new CatalogoController(v,api);view.dispose();v.setVisible(true);});
        view.salir().addActionListener(e->{view.dispose();ec.edu.monster.App.main(new String[0]);});
    }
}

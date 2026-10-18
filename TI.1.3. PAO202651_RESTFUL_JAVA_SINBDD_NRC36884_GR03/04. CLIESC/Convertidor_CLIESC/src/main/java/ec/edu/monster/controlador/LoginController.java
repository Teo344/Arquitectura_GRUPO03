package ec.edu.monster.controlador;

import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.servicio.ApiRest;
import ec.edu.monster.vista.LoginView;
import java.util.Arrays;
import java.util.function.Consumer;
import javax.swing.SwingWorker;

/** Coordina la vista de acceso con el servicio REST. */
public final class LoginController {
    public LoginController(LoginView view, ApiRest api, Consumer<Sesion> onLogin) {
        Runnable login = () -> {
            String user = view.usuario().getText().trim(); char[] chars = view.clave().getPassword();
            if(user.isBlank() || chars.length==0) { view.estado("Ingresa usuario y contraseña.",true); Arrays.fill(chars,'\0'); return; }
            String password = new String(chars); Arrays.fill(chars,'\0'); view.ingresar().setEnabled(false); view.estado("Conectando con el servidor…",false);
            new SwingWorker<Sesion,Void>() {
                @Override protected Sesion doInBackground() throws Exception { return api.login(user,password); }
                @Override protected void done() {
                    view.ingresar().setEnabled(true);
                    try { Sesion sesion=get(); onLogin.accept(sesion); view.dispose(); }
                    catch(Exception e) { Throwable cause=e.getCause()==null?e:e.getCause(); view.estado(mensaje(cause),true); view.clave().setText(""); }
                }
            }.execute();
        };
        view.ingresar().addActionListener(e -> login.run()); view.clave().addActionListener(e -> login.run());
    }
    private String mensaje(Throwable e) {
        String m=e.getMessage();
        if(e instanceof java.net.ConnectException || (m!=null && (m.contains("Connection refused")||m.contains("ConnectException")))) return "No se pudo conectar. Inicia el servidor y revisa la URL base.";
        return m==null||m.isBlank()?"No se pudo iniciar sesión.":m;
    }
}

package ec.edu.monster.vista;

import ec.edu.monster.modelo.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;

/** Catálogo RESTful presentado en dos paneles. */
public final class CatalogoView extends JFrame {
    private final Sesion sesion; private final JPanel categoriasPanel=new JPanel(new GridLayout(0,2,14,14));
    private final JPanel unidadesPanel=new JPanel(new GridLayout(0,1,0,9)); private final JLabel tituloDetalle=new JLabel("Selecciona una categoría");
    private final JLabel estado=new JLabel("El catálogo se consultará al servidor."); private final JButton volver=new JButton("Volver al inicio");
    private final Map<String,JButton> botones=new LinkedHashMap<>();
    public CatalogoView(Sesion sesion){super("Conversión de Unidades Monster | Catálogo");this.sesion=sesion;setDefaultCloseOperation(EXIT_ON_CLOSE);setSize(1180,760);setMinimumSize(new Dimension(940,620));setLocationRelativeTo(null);setExtendedState(JFrame.MAXIMIZED_BOTH);
        JPanel root=new JPanel(new BorderLayout(0,22));root.setBackground(new Color(0xF5F8FC));root.add(nav(),BorderLayout.NORTH);
        JPanel body=new JPanel(new GridLayout(1,2,24,0));body.setOpaque(false);body.setBorder(BorderFactory.createEmptyBorder(28,32,24,32));body.add(categoryPanel());body.add(detailPanel());root.add(body,BorderLayout.CENTER);
        estado.setFont(Estilo.font(13,false));estado.setForeground(Estilo.SECUNDARIO);estado.setBorder(BorderFactory.createEmptyBorder(0,36,12,36));root.add(estado,BorderLayout.SOUTH);setContentPane(root);}
    private JPanel nav(){JPanel p=new JPanel(new BorderLayout());p.setBackground(Estilo.OSCURO);p.setBorder(BorderFactory.createEmptyBorder(13,28,13,28));JPanel ts=new JPanel();ts.setOpaque(false);ts.setLayout(new BoxLayout(ts,BoxLayout.Y_AXIS));
        JLabel title=new JLabel("MONSTER · Catálogo de unidades");title.setForeground(Color.WHITE);title.setFont(Estilo.font(17,true));JLabel sub=new JLabel("Consulta categorías y unidades disponibles");sub.setForeground(new Color(0xB8D2FF));sub.setFont(Estilo.font(12,false));ts.add(title);ts.add(sub);p.add(ts,BorderLayout.WEST);
        Estilo.button(volver,false);p.add(volver,BorderLayout.EAST);return p;}
    private JPanel categoryPanel(){JPanel p=white();p.setLayout(new BorderLayout(0,14));JLabel h=new JLabel("Categorías");h.setForeground(Estilo.SECUNDARIO);h.setFont(Estilo.font(14,true));p.add(h,BorderLayout.NORTH);categoriasPanel.setOpaque(false);JScrollPane s=new JScrollPane(categoriasPanel);s.setBorder(BorderFactory.createEmptyBorder());s.getViewport().setBackground(Color.WHITE);p.add(s,BorderLayout.CENTER);return p;}
    private JPanel detailPanel(){JPanel p=white();p.setLayout(new BorderLayout(0,14));JPanel head=new JPanel(new BorderLayout(0,6));head.setOpaque(false);tituloDetalle.setForeground(Estilo.OSCURO);tituloDetalle.setFont(Estilo.font(22,true));JLabel sub=new JLabel("Unidades registradas para esta categoría");sub.setForeground(Estilo.SECUNDARIO);sub.setFont(Estilo.font(13,false));head.add(tituloDetalle,BorderLayout.NORTH);head.add(sub,BorderLayout.SOUTH);p.add(head,BorderLayout.NORTH);
        unidadesPanel.setOpaque(false);JScrollPane s=new JScrollPane(unidadesPanel);s.setBorder(BorderFactory.createEmptyBorder());s.getViewport().setBackground(Color.WHITE);p.add(s,BorderLayout.CENTER);message("Elige una tarjeta para consultar sus unidades.");return p;}
    private JPanel white(){JPanel p=new JPanel();p.setBackground(Color.WHITE);p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Estilo.DIVISOR),BorderFactory.createEmptyBorder(22,22,22,22)));return p;}
    public void mostrarCategorias(java.util.List<Categoria> list,ActionListener action){categoriasPanel.removeAll();botones.clear();int i=1;for(Categoria c:list){String kind=c.convertible()?"CONVERSIÓN DISPONIBLE":"SOLO CONSULTA";
        JButton b=new JButton("<html><div style='width:180px'><span style='color:#1D56CC;font-size:11px;font-weight:bold'>"+String.format("%02d",i++)+" · "+kind+"</span><br><br><span style='font-size:19px;font-weight:bold;color:#0B1D44'>"+escape(c.nombre())+"</span><br><br><span style='font-size:13px;color:#4A5A78'>"+description(c.codigo())+"</span></div></html>");
        b.setHorizontalAlignment(SwingConstants.LEFT);b.setVerticalAlignment(SwingConstants.TOP);b.setBackground(Color.WHITE);b.setFocusPainted(false);b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Estilo.DIVISOR),BorderFactory.createEmptyBorder(16,15,12,12)));b.setActionCommand(c.codigo());b.addActionListener(action);categoriasPanel.add(b);botones.put(c.codigo(),b);}
        categoriasPanel.revalidate();categoriasPanel.repaint();}
    public void seleccionar(Categoria c){tituloDetalle.setText(c.nombre());for(var e:botones.entrySet()){boolean selected=e.getKey().equals(c.codigo());e.getValue().setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(selected?Estilo.AZUL:Estilo.DIVISOR,selected?2:1),BorderFactory.createEmptyBorder(15,14,11,11)));}}
    public void mostrarUnidades(java.util.List<Unidad> list){unidadesPanel.removeAll();if(list.isEmpty())message("No hay unidades registradas para esta categoría.");else for(Unidad u:list){JPanel row=new JPanel(new BorderLayout(12,0));row.setBackground(new Color(0xF5F8FC));row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Estilo.DIVISOR),BorderFactory.createEmptyBorder(12,14,12,14)));
        JPanel data=new JPanel();data.setOpaque(false);data.setLayout(new BoxLayout(data,BoxLayout.Y_AXIS));JLabel name=new JLabel(u.nombre());name.setForeground(Estilo.TEXTO);name.setFont(Estilo.font(15,true));JLabel code=new JLabel("Código: "+u.codigo());code.setForeground(Estilo.SECUNDARIO);code.setFont(Estilo.font(12,false));data.add(name);data.add(code);JLabel symbol=new JLabel(u.simbolo(),SwingConstants.CENTER);symbol.setForeground(new Color(0x0B6B83));symbol.setFont(Estilo.font(18,true));symbol.setPreferredSize(new Dimension(66,38));row.add(data,BorderLayout.CENTER);row.add(symbol,BorderLayout.EAST);unidadesPanel.add(row);}
        unidadesPanel.revalidate();unidadesPanel.repaint();}
    private void message(String text){unidadesPanel.removeAll();JLabel m=new JLabel("<html><div style='width:300px;text-align:center'>"+text+"</div></html>",SwingConstants.CENTER);m.setForeground(Estilo.SECUNDARIO);m.setFont(Estilo.font(14,false));unidadesPanel.add(m);}
    private String description(String code){return switch(code){case "LONGITUD"->"Distancias y medidas lineales.";case "MASA"->"Medidas de masa y peso.";case "VOLUMEN"->"Capacidad y espacio ocupado.";case "TIEMPO"->"Unidades para medir duración.";case "TEMPERATURA"->"Escalas de temperatura.";case "AREA"->"Superficie y extensión.";default->"Categoría del catálogo de unidades.";};}
    private String escape(String s){return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");}
    public Sesion sesion(){return sesion;} public JButton volver(){return volver;} public JLabel estado(){return estado;}
    public void estado(String s,boolean error){estado.setText(s);estado.setForeground(error?Estilo.ERROR:Estilo.SECUNDARIO);}
}

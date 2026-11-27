package com.aarondevs.filered.app.ui;

import com.aarondevs.filered.app.Configuracion;
import com.aarondevs.filered.app.Motor;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Ventana {
    private static final int ANCHO = 380;
    private static final int ALTO = 640;

    private final Configuracion configuracion;
    private final Motor motor;
    private final JFrame marco = new JFrame("FileRed");
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);
    private final PantallaVincular vincular;
    private final PantallaEstado estado;
    private final JLabel pildora = new JLabel();
    private TrayIcon icono;
    private MenuItem itemPausa;
    private Motor.Estado anterior;

    public Ventana(Configuracion configuracion, Motor motor) {
        this.configuracion = configuracion;
        this.motor = motor;
        UIManager.put("ToolTip.background", Tema.TARJETA_ALTA);
        UIManager.put("ToolTip.foreground", Tema.TEXTO);
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(Tema.LINEA_FUERTE));
        UIManager.put("PopupMenu.background", Tema.TARJETA_ALTA);
        UIManager.put("Separator.foreground", Tema.LINEA_FUERTE);
        UIManager.put("Separator.background", Tema.TARJETA_ALTA);
        vincular = new PantallaVincular(configuracion, motor);
        estado = new PantallaEstado(motor, this::salir);
        construir();
        bandeja();
        motor.escuchar(m -> SwingUtilities.invokeLater(this::refrescar));
    }

    private void construir() {
        marco.setUndecorated(true);
        marco.setSize(ANCHO, ALTO);
        marco.setResizable(false);
        marco.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        marco.setIconImages(List.of(Tema.isotipo(16), Tema.isotipo(32), Tema.isotipo(64), Tema.isotipo(128)));

        JPanel raiz = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Tema.suave(g);
                g2.setColor(Tema.FONDO);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setPaint(new RadialGradientPaint(new Point2D.Double(getWidth(), 0), getWidth() * 0.9f, new float[]{0f, 1f},
                        new Color[]{Tema.alfa(Tema.ACENTO, 46), Tema.alfa(Tema.ACENTO, 0)}));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setPaint(new RadialGradientPaint(new Point2D.Double(0, getHeight()), getWidth() * 0.8f, new float[]{0f, 1f},
                        new Color[]{Tema.alfa(Tema.CIAN, 16), Tema.alfa(Tema.CIAN, 0)}));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Tema.LINEA_FUERTE);
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
            }
        };
        raiz.add(barraTitulo(), BorderLayout.NORTH);
        contenido.setOpaque(false);
        contenido.add(vincular, "vincular");
        contenido.add(estado, "estado");
        raiz.add(contenido, BorderLayout.CENTER);
        marco.setContentPane(raiz);
        marco.setLocationRelativeTo(null);
        Rectangle area = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        marco.setLocation(area.x + area.width - ANCHO - 24, area.y + area.height - ALTO - 24);
        marco.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentMoved(java.awt.event.ComponentEvent e) {
                marco.getRootPane().repaint();
            }

            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                marco.getRootPane().repaint();
            }
        });
        marco.addWindowStateListener(e -> marco.getRootPane().repaint());
        marco.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrar();
            }
        });
    }

    private JPanel barraTitulo() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.setBorder(BorderFactory.createEmptyBorder(12, 16, 6, 10));
        barra.setPreferredSize(new Dimension(ANCHO, 50));

        JPanel marca = new JPanel(new BorderLayout(8, 0));
        marca.setOpaque(false);
        marca.add(new JLabel(new javax.swing.ImageIcon(Tema.isotipo(22))), BorderLayout.WEST);
        JLabel nombre = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Tema.suave(g);
                g2.setFont(Tema.fuente(15.5f, Font.BOLD));
                int y = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
                g2.setPaint(new GradientPaint(0, 0, Color.WHITE, 0, getHeight(), Tema.PLATA));
                g2.drawString("File", 0, y);
                int ancho = g2.getFontMetrics().stringWidth("File");
                g2.setColor(Tema.ACENTO);
                g2.drawString("Red", ancho, y);
                int total = ancho + g2.getFontMetrics().stringWidth("Red");
                g2.setFont(Tema.fuente(10.5f, Font.PLAIN));
                g2.setColor(Tema.APAGADO);
                g2.drawString("v" + Configuracion.VERSION, total + 7, y);
                g2.dispose();
            }
        };
        nombre.setPreferredSize(new Dimension(130, 24));
        marca.add(nombre, BorderLayout.CENTER);
        barra.add(marca, BorderLayout.WEST);

        JPanel botones = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 2, 0));
        botones.setOpaque(false);
        pildora.setFont(Tema.fuente(10.5f, Font.BOLD));
        botones.add(pildora);
        Componentes.Boton minimizar = botonVentana(Iconos.Tipo.MINIMIZAR, "Minimizar");
        minimizar.addActionListener(e -> {
            if (icono != null) marco.setVisible(false);
            else marco.setState(JFrame.ICONIFIED);
        });
        Componentes.Boton cerrar = botonVentana(Iconos.Tipo.CERRAR, "Cerrar");
        cerrar.addActionListener(e -> cerrar());
        botones.add(minimizar);
        botones.add(cerrar);
        barra.add(botones, BorderLayout.EAST);

        MouseAdapter arrastre = new MouseAdapter() {
            private Point inicio;

            @Override
            public void mousePressed(MouseEvent e) {
                inicio = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (inicio == null) return;
                Point p = e.getLocationOnScreen();
                marco.setLocation(p.x - inicio.x, p.y - inicio.y);
            }
        };
        barra.addMouseListener(arrastre);
        barra.addMouseMotionListener(arrastre);
        return barra;
    }

    private Componentes.Boton botonVentana(Iconos.Tipo tipo, String texto) {
        Componentes.Boton b = new Componentes.Boton("", Componentes.Variante.FANTASMA);
        b.setIcon(new Iconos(tipo, 15, Tema.TEXTO_2));
        b.setToolTipText(texto);
        b.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        b.setPreferredSize(new Dimension(32, 30));
        return b;
    }

    private void bandeja() {
        if (!SystemTray.isSupported()) return;
        try {
            SystemTray sistema = SystemTray.getSystemTray();
            Dimension lado = sistema.getTrayIconSize();
            Image imagen = Tema.isotipo(Math.max(16, Math.min(lado.width, lado.height)));
            PopupMenu menu = new PopupMenu();
            MenuItem abrir = new MenuItem("Abrir FileRed");
            abrir.addActionListener(e -> SwingUtilities.invokeLater(() -> mostrar(true)));
            itemPausa = new MenuItem("Pausar");
            itemPausa.addActionListener(e -> new Thread(() -> {
                if (motor.estado() == Motor.Estado.PAUSADO) motor.reanudar();
                else motor.pausar();
            }, "FileRed-pausa").start());
            MenuItem salir = new MenuItem("Salir");
            salir.addActionListener(e -> SwingUtilities.invokeLater(this::salir));
            menu.add(abrir);
            menu.add(itemPausa);
            menu.addSeparator();
            menu.add(salir);
            icono = new TrayIcon(imagen, "FileRed", menu);
            icono.setImageAutoSize(true);
            icono.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) SwingUtilities.invokeLater(() -> mostrar(!marco.isVisible()));
                }
            });
            sistema.add(icono);
        } catch (AWTException | UnsupportedOperationException | SecurityException error) {
            icono = null;
        }
    }

    public void mostrar(boolean visible) {
        refrescar();
        if (!visible) {
            if (icono == null) {
                marco.setVisible(true);
                marco.setState(JFrame.ICONIFIED);
            }
            if (configuracion.vinculado()) motor.iniciar();
            return;
        }
        marco.setVisible(true);
        marco.setState(JFrame.NORMAL);
        marco.toFront();
        marco.requestFocus();
        if (configuracion.vinculado()) motor.iniciar();
        else vincular.enfocar();
    }

    private void refrescar() {
        Motor.Estado e = motor.estado();
        boolean enlazado = configuracion.vinculado() && e != Motor.Estado.DESVINCULADO;
        tarjetas.show(contenido, enlazado ? "estado" : "vincular");
        if (enlazado) estado.actualizar();
        else if (anterior != null && anterior != Motor.Estado.DESVINCULADO && e == Motor.Estado.DESVINCULADO) vincular.mensaje(motor.mensaje());
        pintarPildora(e, enlazado);
        if (icono != null) {
            icono.setToolTip("FileRed · " + switch (e) {
                case CONECTADO -> "Conectado";
                case CONECTANDO -> "Conectando";
                case SIN_CONEXION -> "Sin conexión";
                case PAUSADO -> "En pausa";
                default -> "Sin vincular";
            });
            if (itemPausa != null) {
                itemPausa.setLabel(e == Motor.Estado.PAUSADO ? "Reanudar" : "Pausar");
                itemPausa.setEnabled(enlazado);
            }
            if (anterior == Motor.Estado.CONECTADO && e == Motor.Estado.DESVINCULADO) {
                icono.displayMessage("FileRed", "Este equipo fue desvinculado de FileRed.", TrayIcon.MessageType.WARNING);
            }
        }
        anterior = e;
    }

    private void pintarPildora(Motor.Estado e, boolean enlazado) {
        if (!enlazado) {
            pildora.setText("");
            pildora.setIcon(null);
            return;
        }
        Color color = switch (e) {
            case CONECTADO -> Tema.EXITO;
            case SIN_CONEXION -> Tema.AVISO;
            case CONECTANDO -> Tema.CIAN;
            default -> Tema.APAGADO;
        };
        pildora.setIcon(new javax.swing.Icon() {
            @Override
            public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
                Graphics2D g2 = Tema.suave(g);
                g2.setColor(Tema.alfa(color, 60));
                g2.fillOval(x, y, 10, 10);
                g2.setColor(color);
                g2.fillOval(x + 2, y + 2, 6, 6);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 10;
            }

            @Override
            public int getIconHeight() {
                return 10;
            }
        });
        pildora.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
    }

    private void cerrar() {
        if (icono != null) {
            marco.setVisible(false);
            return;
        }
        int r = JOptionPane.showConfirmDialog(marco, "Si cierras FileRed, este equipo dejará de recibir y entregar archivos.\n¿Quieres salir?",
                "Salir de FileRed", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r == JOptionPane.OK_OPTION) salir();
    }

    private void salir() {
        motor.detener();
        if (icono != null) SystemTray.getSystemTray().remove(icono);
        marco.dispose();
        System.exit(0);
    }
}

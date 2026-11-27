package com.aarondevs.filered.app.ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RadialGradientPaint;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

public final class Componentes {
    private Componentes() {
    }

    public static JLabel etiqueta(String texto, float tamano, int estilo, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.fuente(tamano, estilo));
        l.setForeground(color);
        return l;
    }

    public static class Tarjeta extends JPanel {
        private final int radio;
        private Color fondo = Tema.TARJETA;
        private Color borde = Tema.LINEA;

        public Tarjeta(LayoutManager layout, int radio) {
            super(layout);
            this.radio = radio;
            setOpaque(false);
        }

        public Tarjeta colores(Color fondo, Color borde) {
            this.fondo = fondo;
            this.borde = borde;
            repaint();
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suave(g);
            RoundRectangle2D forma = new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, radio, radio);
            g2.setColor(fondo);
            g2.fill(forma);
            if (borde != null) {
                g2.setColor(borde);
                g2.draw(forma);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public enum Variante { PRIMARIO, SECUNDARIO, FANTASMA, PELIGRO }

    public static class Boton extends JButton {
        private final Variante variante;
        private boolean encima;
        private boolean presionado;
        private boolean ocupado;
        private double giro;
        private final Timer animacion;

        public Boton(String texto, Variante variante) {
            super(texto);
            this.variante = variante;
            setFont(Tema.fuente(13.5f, java.awt.Font.BOLD));
            setForeground(variante == Variante.PRIMARIO ? Color.WHITE : variante == Variante.PELIGRO ? Tema.PELIGRO : Tema.TEXTO);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
            setIconTextGap(8);
            animacion = new Timer(16, e -> {
                giro = (giro + 9) % 360;
                repaint();
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    encima = false;
                    presionado = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    presionado = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    presionado = false;
                    repaint();
                }
            });
        }

        public void ocupado(boolean valor) {
            ocupado = valor;
            setEnabled(!valor);
            if (valor) animacion.start();
            else animacion.stop();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suave(g);
            int w = getWidth();
            int h = getHeight();
            RoundRectangle2D forma = new RoundRectangle2D.Double(0, 0, w, h, 12, 12);
            float opacidad = isEnabled() || ocupado ? 1f : 0.45f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidad));
            switch (variante) {
                case PRIMARIO -> {
                    Color a = presionado ? Tema.ACENTO.darker() : encima ? Tema.ACENTO_FUERTE : Tema.ACENTO;
                    g2.setPaint(new GradientPaint(0, 0, a, 0, h, Tema.alfa(a.darker(), 255)));
                    g2.fill(forma);
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 12, 12));
                }
                case SECUNDARIO -> {
                    g2.setColor(presionado ? Tema.TARJETA : encima ? Tema.alfa(Color.WHITE, 22) : Tema.TARJETA_ALTA);
                    g2.fill(forma);
                    g2.setColor(encima ? Tema.LINEA_FUERTE : Tema.LINEA);
                    g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 12, 12));
                }
                case PELIGRO -> {
                    g2.setColor(encima ? Tema.alfa(Tema.PELIGRO, 40) : Tema.alfa(Tema.PELIGRO, 22));
                    g2.fill(forma);
                }
                case FANTASMA -> {
                    if (encima) {
                        g2.setColor(Tema.alfa(Color.WHITE, presionado ? 30 : 18));
                        g2.fill(forma);
                    }
                }
                default -> {
                }
            }
            if (ocupado) {
                int d = 16;
                int x = (w - d) / 2;
                int y = (h - d) / 2;
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(Tema.alfa(getForeground(), 60));
                g2.drawOval(x, y, d, d);
                g2.setColor(getForeground());
                g2.draw(new Arc2D.Double(x, y, d, d, -giro, 100, Arc2D.OPEN));
                g2.dispose();
                return;
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class Campo extends JTextField {
        private final String marcador;
        private boolean enfocado;
        private boolean error;

        public Campo(String marcador) {
            this.marcador = marcador;
            setOpaque(false);
            setFont(Tema.fuente(13.5f, java.awt.Font.PLAIN));
            setForeground(Tema.TEXTO);
            setCaretColor(Tema.ACENTO_FUERTE);
            setSelectionColor(Tema.alfa(Tema.ACENTO, 120));
            setSelectedTextColor(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(11, 13, 11, 13));
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    enfocado = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    enfocado = false;
                    repaint();
                }
            });
        }

        public void error(boolean valor) {
            error = valor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suave(g);
            RoundRectangle2D forma = new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.setColor(Tema.FONDO_ALTO);
            g2.fill(forma);
            Color borde = error ? Tema.PELIGRO : enfocado ? Tema.alfa(Tema.ACENTO, 200) : Tema.LINEA_FUERTE;
            if (enfocado || error) {
                g2.setColor(Tema.alfa(borde, 45));
                g2.setStroke(new BasicStroke(4f));
                g2.draw(forma);
            }
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(borde);
            g2.draw(forma);
            g2.dispose();
            super.paintComponent(g);
            if (getText().isEmpty() && marcador != null) {
                Graphics2D g3 = Tema.suave(g);
                g3.setFont(getFont());
                g3.setColor(Tema.APAGADO);
                Insets m = getInsets();
                FontMetrics fm = g3.getFontMetrics();
                g3.drawString(marcador, m.left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g3.dispose();
            }
        }
    }

    public static class Anillo extends JComponent {
        private Color color = Tema.APAGADO;
        private boolean girando;
        private boolean latiendo;
        private double fase;
        private final Timer reloj;
        private java.awt.Image imagen;
        private int ladoImagen;

        public Anillo(int lado) {
            setPreferredSize(new Dimension(lado, lado));
            setMinimumSize(new Dimension(lado, lado));
            setMaximumSize(new Dimension(lado, lado));
            reloj = new Timer(33, e -> {
                fase += 0.033;
                repaint();
            });
            reloj.start();
        }

        public void estado(Color color, boolean girando, boolean latiendo) {
            this.color = color;
            this.girando = girando;
            this.latiendo = latiendo;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suave(g);
            int lado = Math.min(getWidth(), getHeight());
            double cx = getWidth() / 2.0;
            double cy = getHeight() / 2.0;
            double radio = lado / 2.0 - 10;
            float pulso = latiendo ? (float) ((Math.sin(fase * 2.4) + 1) / 2) : 0.4f;
            g2.setPaint(new RadialGradientPaint(new Point2D.Double(cx, cy), (float) (radio + 10),
                    new float[]{0f, 0.55f, 1f},
                    new Color[]{Tema.alfa(color, (int) (40 + 40 * pulso)), Tema.alfa(color, (int) (14 + 14 * pulso)), Tema.alfa(color, 0)}));
            g2.fill(new java.awt.geom.Ellipse2D.Double(cx - radio - 10, cy - radio - 10, (radio + 10) * 2, (radio + 10) * 2));
            g2.setColor(Tema.TARJETA);
            g2.fill(new java.awt.geom.Ellipse2D.Double(cx - radio + 9, cy - radio + 9, (radio - 9) * 2, (radio - 9) * 2));
            g2.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(Tema.alfa(Color.WHITE, 16));
            g2.draw(new java.awt.geom.Ellipse2D.Double(cx - radio, cy - radio, radio * 2, radio * 2));
            g2.setColor(color);
            if (girando) {
                double inicio = -(fase * 260) % 360;
                g2.draw(new Arc2D.Double(cx - radio, cy - radio, radio * 2, radio * 2, inicio, 110, Arc2D.OPEN));
            } else {
                g2.draw(new java.awt.geom.Ellipse2D.Double(cx - radio, cy - radio, radio * 2, radio * 2));
            }
            int logo = (int) (radio * 1.3);
            if (imagen == null || ladoImagen != logo) {
                imagen = Tema.isotipo(logo);
                ladoImagen = logo;
            }
            g2.drawImage(imagen, (int) (cx - logo / 2.0), (int) (cy - logo / 2.0), null);
            g2.dispose();
        }
    }

    public static class Barra extends JComponent {
        private double valor;
        private Color color = Tema.ACENTO;

        public Barra() {
            setPreferredSize(new Dimension(100, 7));
            setMinimumSize(new Dimension(20, 7));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 7));
        }

        public void valor(double valor) {
            this.valor = Math.max(0, Math.min(1, valor));
            color = this.valor > 0.9 ? Tema.PELIGRO : this.valor > 0.75 ? Tema.AVISO : Tema.ACENTO;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suave(g);
            int h = getHeight();
            g2.setColor(Tema.alfa(Color.WHITE, 18));
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), h, h, h));
            int w = (int) Math.round(getWidth() * valor);
            if (w > 0) {
                g2.setPaint(new GradientPaint(0, 0, color.darker(), w, 0, color));
                g2.fill(new RoundRectangle2D.Double(0, 0, Math.max(w, h), h, h, h));
            }
            g2.dispose();
        }
    }

    public static class Punto extends JComponent {
        private Color color;

        public Punto(Color color) {
            this.color = color;
            setPreferredSize(new Dimension(8, 8));
            setMinimumSize(new Dimension(8, 8));
            setMaximumSize(new Dimension(8, 8));
        }

        public void color(Color color) {
            this.color = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suave(g);
            g2.setColor(color);
            g2.fillOval(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    public static class Chip extends JLabel {
        private Color fondo;

        public Chip(String texto, Color color) {
            super(texto);
            setFont(Tema.fuente(11.5f, java.awt.Font.BOLD));
            colores(color);
            setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            setOpaque(false);
        }

        public void colores(Color color) {
            setForeground(color);
            fondo = Tema.alfa(color, 34);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Tema.suave(g);
            g2.setColor(fondo);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}

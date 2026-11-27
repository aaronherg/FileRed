package com.aarondevs.filered.app.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Icon;

public final class Iconos implements Icon {
    public enum Tipo { CARPETA, NUBE, SERVIDOR, PAUSA, PLAY, CERRAR, MINIMIZAR, MAS, ENLACE, DESCARGA, SUBIDA, CHECK, ALERTA, ESTRELLA, DISCO, SALIR }

    private final Tipo tipo;
    private final int lado;
    private final Color color;

    public Iconos(Tipo tipo, int lado, Color color) {
        this.tipo = tipo;
        this.lado = lado;
        this.color = color;
    }

    @Override
    public int getIconWidth() {
        return lado;
    }

    @Override
    public int getIconHeight() {
        return lado;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = Tema.suave(g);
        g2.translate(x, y);
        double escala = lado / 24.0;
        g2.scale(escala, escala);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        dibujar(g2, tipo);
        g2.dispose();
    }

    public static void dibujar(Graphics2D g, Tipo tipo) {
        switch (tipo) {
            case CARPETA -> {
                Path2D p = new Path2D.Double();
                p.moveTo(3, 6.5);
                p.quadTo(3, 4.5, 5, 4.5);
                p.lineTo(9.2, 4.5);
                p.lineTo(11.2, 7);
                p.lineTo(19, 7);
                p.quadTo(21, 7, 21, 9);
                p.lineTo(21, 17.5);
                p.quadTo(21, 19.5, 19, 19.5);
                p.lineTo(5, 19.5);
                p.quadTo(3, 19.5, 3, 17.5);
                p.closePath();
                g.draw(p);
            }
            case NUBE -> {
                Path2D p = new Path2D.Double();
                p.moveTo(7, 18.5);
                p.curveTo(3.5, 18.5, 2.5, 14.5, 4.8, 12.6);
                p.curveTo(4.6, 9, 8.4, 7.2, 11, 8.6);
                p.curveTo(12.6, 5.2, 18.6, 5.6, 18.8, 10.2);
                p.curveTo(22.4, 10.8, 22.2, 18.5, 17.8, 18.5);
                p.closePath();
                g.draw(p);
            }
            case SERVIDOR -> {
                g.draw(new RoundRectangle2D.Double(3, 3.5, 18, 7, 3, 3));
                g.draw(new RoundRectangle2D.Double(3, 13.5, 18, 7, 3, 3));
                g.drawLine(7, 7, 7, 7);
                g.drawLine(7, 17, 7, 17);
                g.drawLine(11, 7, 16, 7);
                g.drawLine(11, 17, 16, 17);
            }
            case PAUSA -> {
                g.drawLine(9, 6, 9, 18);
                g.drawLine(15, 6, 15, 18);
            }
            case PLAY -> {
                Path2D p = new Path2D.Double();
                p.moveTo(8, 5.5);
                p.lineTo(18.5, 12);
                p.lineTo(8, 18.5);
                p.closePath();
                g.draw(p);
            }
            case CERRAR -> {
                g.drawLine(6, 6, 18, 18);
                g.drawLine(18, 6, 6, 18);
            }
            case MINIMIZAR -> g.drawLine(6, 12, 18, 12);
            case MAS -> {
                g.fillOval(4, 10, 4, 4);
                g.fillOval(10, 10, 4, 4);
                g.fillOval(16, 10, 4, 4);
            }
            case ENLACE -> {
                Path2D p = new Path2D.Double();
                p.moveTo(10, 13.5);
                p.curveTo(11.5, 15.5, 14.5, 15.7, 16.3, 14);
                p.lineTo(19.3, 11);
                p.curveTo(21, 9, 20.8, 6, 18.8, 4.6);
                p.curveTo(17, 3.2, 14.6, 3.5, 13, 5);
                p.lineTo(11.5, 6.5);
                p.moveTo(14, 10.5);
                p.curveTo(12.5, 8.5, 9.5, 8.3, 7.7, 10);
                p.lineTo(4.7, 13);
                p.curveTo(3, 15, 3.2, 18, 5.2, 19.4);
                p.curveTo(7, 20.8, 9.4, 20.5, 11, 19);
                p.lineTo(12.5, 17.5);
                g.draw(p);
            }
            case DESCARGA -> {
                g.drawLine(12, 4, 12, 15);
                g.drawLine(7, 10, 12, 15);
                g.drawLine(17, 10, 12, 15);
                g.drawLine(4, 20, 20, 20);
            }
            case SUBIDA -> {
                g.drawLine(12, 15, 12, 4);
                g.drawLine(7, 9, 12, 4);
                g.drawLine(17, 9, 12, 4);
                g.drawLine(4, 20, 20, 20);
            }
            case CHECK -> {
                g.drawLine(5, 12, 10, 17);
                g.drawLine(10, 17, 19, 7);
            }
            case ALERTA -> {
                Path2D p = new Path2D.Double();
                p.moveTo(12, 3.5);
                p.lineTo(21.5, 20);
                p.lineTo(2.5, 20);
                p.closePath();
                g.draw(p);
                g.drawLine(12, 10, 12, 14);
                g.drawLine(12, 17, 12, 17);
            }
            case ESTRELLA -> {
                Path2D p = new Path2D.Double();
                double[][] puntos = {{12, 2.5}, {14.9, 8.6}, {21.5, 9.4}, {16.6, 13.9}, {17.9, 20.5}, {12, 17.2}, {6.1, 20.5}, {7.4, 13.9}, {2.5, 9.4}, {9.1, 8.6}};
                p.moveTo(puntos[0][0], puntos[0][1]);
                for (int i = 1; i < puntos.length; i++) p.lineTo(puntos[i][0], puntos[i][1]);
                p.closePath();
                g.fill(p);
            }
            case DISCO -> {
                g.draw(new RoundRectangle2D.Double(3, 6, 18, 12, 4, 4));
                g.drawLine(3, 13, 21, 13);
                g.draw(new java.awt.geom.Line2D.Double(7, 15.5, 7, 15.5));
                g.draw(new java.awt.geom.Line2D.Double(10.5, 15.5, 10.5, 15.5));
            }
            case SALIR -> {
                g.drawLine(9, 20, 5, 20);
                g.drawLine(5, 20, 5, 4);
                g.drawLine(5, 4, 9, 4);
                g.drawLine(15, 16, 20, 12);
                g.drawLine(20, 12, 15, 8);
                g.drawLine(20, 12, 10, 12);
            }
            default -> {
            }
        }
    }
}

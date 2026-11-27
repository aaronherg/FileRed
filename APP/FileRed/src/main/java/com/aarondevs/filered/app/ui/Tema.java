package com.aarondevs.filered.app.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;

public final class Tema {
    public static final Color FONDO = new Color(0x08, 0x08, 0x0B);
    public static final Color FONDO_ALTO = new Color(0x0D, 0x0D, 0x12);
    public static final Color TARJETA = new Color(0x12, 0x12, 0x18);
    public static final Color TARJETA_ALTA = new Color(0x19, 0x19, 0x21);
    public static final Color LINEA = new Color(255, 255, 255, 22);
    public static final Color LINEA_FUERTE = new Color(255, 255, 255, 40);
    public static final Color TEXTO = new Color(0xF4, 0xF4, 0xF7);
    public static final Color TEXTO_2 = new Color(0xB8, 0xBA, 0xC4);
    public static final Color APAGADO = new Color(0x7A, 0x7C, 0x88);
    public static final Color ACENTO = new Color(0xFF, 0x1F, 0x3D);
    public static final Color ACENTO_FUERTE = new Color(0xFF, 0x3B, 0x55);
    public static final Color ACENTO_OSCURO = new Color(0x6D, 0x0A, 0x16);
    public static final Color PLATA = new Color(0xC9, 0xCC, 0xD3);
    public static final Color EXITO = new Color(0x34, 0xE3, 0x9A);
    public static final Color AVISO = new Color(0xFF, 0xC8, 0x61);
    public static final Color PELIGRO = new Color(0xFF, 0x54, 0x68);
    public static final Color CIAN = new Color(0x5E, 0xE7, 0xFF);

    private static final String FAMILIA;
    private static final String MONO;
    private static BufferedImage isotipo;

    static {
        Set<String> disponibles = new HashSet<>(Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        FAMILIA = primera(disponibles, "Segoe UI", "SF Pro Text", "Helvetica Neue", "Inter", "Ubuntu", "Noto Sans", "Cantarell", "DejaVu Sans", Font.SANS_SERIF);
        MONO = primera(disponibles, "Cascadia Mono", "JetBrains Mono", "SF Mono", "Menlo", "Consolas", "Ubuntu Mono", "DejaVu Sans Mono", Font.MONOSPACED);
    }

    private Tema() {
    }

    private static String primera(Set<String> disponibles, String... opciones) {
        for (String o : opciones) if (disponibles.contains(o)) return o;
        return opciones[opciones.length - 1];
    }

    public static Font fuente(float tamano, int estilo) {
        return new Font(FAMILIA, estilo, Math.round(tamano)).deriveFont(estilo, tamano);
    }

    public static Font mono(float tamano, int estilo) {
        return new Font(MONO, estilo, Math.round(tamano)).deriveFont(estilo, tamano);
    }

    public static Graphics2D suave(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        return g2;
    }

    public static synchronized BufferedImage isotipo() {
        if (isotipo != null) return isotipo;
        try (InputStream entrada = Tema.class.getResourceAsStream("/filered/isotipo.png")) {
            isotipo = entrada == null ? null : ImageIO.read(entrada);
        } catch (IOException error) {
            isotipo = null;
        }
        if (isotipo == null) {
            isotipo = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = suave(isotipo.getGraphics());
            g.setColor(ACENTO);
            g.fillRoundRect(4, 4, 56, 56, 18, 18);
            g.dispose();
        }
        return isotipo;
    }

    public static Image isotipo(int lado) {
        BufferedImage base = isotipo();
        BufferedImage salida = new BufferedImage(lado, lado, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = suave(salida.getGraphics());
        g.drawImage(base, 0, 0, lado, lado, null);
        g.dispose();
        return salida;
    }

    public static Color alfa(Color color, int alfa) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, alfa)));
    }
}

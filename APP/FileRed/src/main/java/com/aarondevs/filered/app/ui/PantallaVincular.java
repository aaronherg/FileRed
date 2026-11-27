package com.aarondevs.filered.app.ui;

import com.aarondevs.filered.app.ClienteFileRed;
import com.aarondevs.filered.app.Configuracion;
import com.aarondevs.filered.app.Json;
import com.aarondevs.filered.app.Motor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public final class PantallaVincular extends JPanel {
    private final Configuracion configuracion;
    private final Motor motor;
    private final Componentes.Campo servidor = new Componentes.Campo("https://filered.tudominio.com");
    private final Componentes.Campo codigo = new Componentes.Campo("XXXX-XXXX-XXXX");
    private final JLabel carpetaTexto = Componentes.etiqueta("", 12.5f, Font.PLAIN, Tema.TEXTO_2);
    private final JLabel aviso = Componentes.etiqueta(" ", 12f, Font.PLAIN, Tema.PELIGRO);
    private final Componentes.Boton vincular = new Componentes.Boton("Vincular equipo", Componentes.Variante.PRIMARIO);
    private Path carpeta;

    public PantallaVincular(Configuracion configuracion, Motor motor) {
        this.configuracion = configuracion;
        this.motor = motor;
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 26, 22, 26));

        JComponent logo = new JComponent() {
            private final Image imagen = Tema.isotipo(64);

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Tema.suave(g);
                double cx = getWidth() / 2.0;
                double cy = getHeight() / 2.0;
                g2.setPaint(new RadialGradientPaint(new Point2D.Double(cx, cy), 60f, new float[]{0f, 1f},
                        new Color[]{Tema.alfa(Tema.ACENTO, 70), Tema.alfa(Tema.ACENTO, 0)}));
                g2.fillOval((int) cx - 60, (int) cy - 60, 120, 120);
                g2.drawImage(imagen, (int) cx - 32, (int) cy - 32, null);
                g2.dispose();
            }
        };
        logo.setPreferredSize(new Dimension(120, 104));
        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 104));
        add(logo);

        JLabel titulo = Componentes.etiqueta("Vincula este equipo", 21f, Font.BOLD, Tema.TEXTO);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(titulo);
        add(Box.createVerticalStrut(6));
        JLabel subtitulo = Componentes.etiqueta("<html><div style='text-align:center'>Guarda en este equipo los archivos de tu cuenta.</div></html>",
                12.5f, Font.PLAIN, Tema.TEXTO_2);
        subtitulo.setHorizontalAlignment(SwingConstants.CENTER);
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(subtitulo);
        add(Box.createVerticalStrut(22));

        servidor.setText(configuracion.servidor() == null ? "" : configuracion.servidor());
        codigo.setFont(Tema.mono(15f, Font.BOLD));
        ((AbstractDocument) codigo.getDocument()).setDocumentFilter(new FiltroCodigo());

        add(campo("Servidor de FileRed", servidor));
        add(Box.createVerticalStrut(14));
        add(campo("Código de vinculación", codigo));
        add(Box.createVerticalStrut(14));

        carpeta = configuracion.carpeta() == null ? Configuracion.carpetaPredeterminada() : configuracion.carpeta();
        carpetaTexto.setText(carpeta.toString());
        Componentes.Tarjeta caja = new Componentes.Tarjeta(new BorderLayout(10, 0), 12).colores(Tema.FONDO_ALTO, Tema.LINEA_FUERTE);
        caja.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 12, 6, 6));
        JLabel icono = new JLabel(new Iconos(Iconos.Tipo.CARPETA, 18, Tema.APAGADO));
        caja.add(icono, BorderLayout.WEST);
        caja.add(carpetaTexto, BorderLayout.CENTER);
        Componentes.Boton elegir = new Componentes.Boton("Elegir", Componentes.Variante.SECUNDARIO);
        elegir.setFont(Tema.fuente(12.5f, Font.BOLD));
        elegir.setBorder(javax.swing.BorderFactory.createEmptyBorder(7, 12, 7, 12));
        elegir.addActionListener(e -> elegirCarpeta());
        caja.add(elegir, BorderLayout.EAST);
        add(campo("Carpeta donde se guardarán los archivos", caja));

        add(Box.createVerticalStrut(8));
        aviso.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel filaAviso = fila(aviso);
        add(filaAviso);
        add(Box.createVerticalStrut(10));

        vincular.setAlignmentX(Component.LEFT_ALIGNMENT);
        vincular.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        vincular.setPreferredSize(new Dimension(300, 44));
        vincular.addActionListener(e -> vincular());
        add(fila(vincular));
        codigo.addActionListener(e -> vincular());
        servidor.addActionListener(e -> codigo.requestFocusInWindow());

        add(Box.createVerticalGlue());
        JLabel pie = Componentes.etiqueta("<html><div style='text-align:center'>Genera el código en FileRed › Almacenamiento › Nodos</div></html>",
                11.5f, Font.PLAIN, Tema.APAGADO);
        pie.setHorizontalAlignment(SwingConstants.CENTER);
        pie.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(pie);
    }

    private JPanel fila(JComponent componente) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(componente, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, componente.getPreferredSize().height));
        return p;
    }

    private JPanel campo(String texto, JComponent control) {
        JPanel p = new JPanel(new BorderLayout(0, 7));
        p.setOpaque(false);
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(Componentes.etiqueta(texto, 12f, Font.BOLD, Tema.TEXTO_2), BorderLayout.NORTH);
        p.add(control, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    public void enfocar() {
        if (servidor.getText().isBlank()) servidor.requestFocusInWindow();
        else codigo.requestFocusInWindow();
    }

    public void mensaje(String texto) {
        aviso.setForeground(Tema.AVISO);
        aviso.setText(texto == null || texto.isBlank() ? " " : texto);
    }

    private void elegirCarpeta() {
        JFileChooser selector = new JFileChooser(carpeta.toFile().exists() ? carpeta.toFile() : carpeta.getParent().toFile());
        selector.setDialogTitle("Carpeta para los archivos de FileRed");
        selector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        selector.setAcceptAllFileFilterUsed(false);
        if (selector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            carpeta = selector.getSelectedFile().toPath().toAbsolutePath().normalize();
            carpetaTexto.setText(carpeta.toString());
        }
    }

    private void error(String texto, Componentes.Campo campo) {
        aviso.setForeground(Tema.PELIGRO);
        aviso.setText("<html>" + texto + "</html>");
        servidor.error(campo == servidor);
        codigo.error(campo == codigo);
        if (campo != null) campo.requestFocusInWindow();
    }

    private void vincular() {
        if (!vincular.isEnabled()) return;
        String url = ClienteFileRed.normalizar(servidor.getText());
        String clave = codigo.getText().trim();
        if (url.isBlank()) {
            error("Escribe la dirección de tu servidor de FileRed.", servidor);
            return;
        }
        if (clave.replace("-", "").length() != 12) {
            error("El código de vinculación tiene 12 caracteres.", codigo);
            return;
        }
        error(" ", null);
        aviso.setText(" ");
        vincular.ocupado(true);
        Path destino = carpeta;
        new SwingWorker<Map<String, Object>, Void>() {
            private String falla;

            @Override
            protected Map<String, Object> doInBackground() {
                try {
                    Files.createDirectories(destino);
                    if (!Files.isWritable(destino)) {
                        falla = "No se puede escribir en la carpeta elegida.";
                        return null;
                    }
                    Map<String, Object> datos = Motor.datosEquipo(destino);
                    datos.put("codigo", clave);
                    return new ClienteFileRed(url, null).vincular(datos);
                } catch (IOException error) {
                    falla = "No se pudo crear la carpeta elegida.";
                } catch (ClienteFileRed.FalloException error) {
                    falla = error.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                vincular.ocupado(false);
                Map<String, Object> r;
                try {
                    r = get();
                } catch (Exception error) {
                    r = null;
                    falla = "No se pudo vincular el equipo.";
                }
                if (r == null || Json.texto(r, "token") == null) {
                    error(falla == null ? "No se pudo vincular el equipo." : falla, falla != null && falla.toLowerCase().contains("código") ? codigo : null);
                    return;
                }
                configuracion.servidor(url);
                configuracion.token(Json.texto(r, "token"));
                configuracion.carpeta(destino);
                configuracion.nodo(Json.texto(r, "nodo"));
                configuracion.cliente(Json.texto(r, "cliente"));
                configuracion.pausado(false);
                configuracion.guardar();
                codigo.setText("");
                motor.vinculado(Json.texto(r, "nodo"), Json.texto(r, "cliente"));
            }
        }.execute();
    }

    private static final class FiltroCodigo extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String texto, AttributeSet attr) throws BadLocationException {
            replace(fb, offset, 0, texto, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int largo, String texto, AttributeSet attrs) throws BadLocationException {
            String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
            String nuevo = actual.substring(0, offset) + (texto == null ? "" : texto) + actual.substring(offset + largo);
            String limpio = nuevo.toUpperCase().replaceAll("[^A-Z0-9]", "");
            if (limpio.length() > 12) limpio = limpio.substring(0, 12);
            StringBuilder formato = new StringBuilder();
            for (int i = 0; i < limpio.length(); i++) {
                if (i > 0 && i % 4 == 0) formato.append('-');
                formato.append(limpio.charAt(i));
            }
            fb.replace(0, fb.getDocument().getLength(), formato.toString(), attrs);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int largo) throws BadLocationException {
            replace(fb, offset, largo, "", null);
        }
    }
}

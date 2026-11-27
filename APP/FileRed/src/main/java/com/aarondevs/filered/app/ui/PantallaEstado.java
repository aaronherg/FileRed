package com.aarondevs.filered.app.ui;

import com.aarondevs.filered.app.Arranque;
import com.aarondevs.filered.app.Motor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

public final class PantallaEstado extends JPanel {
    private final Motor motor;
    private final Runnable salir;
    private final Componentes.Anillo anillo = new Componentes.Anillo(112);
    private final JLabel estado = Componentes.etiqueta("Conectando…", 15f, Font.BOLD, Tema.TEXTO);
    private final JLabel detalle = Componentes.etiqueta(" ", 12f, Font.PLAIN, Tema.TEXTO_2);
    private final JLabel nodo = Componentes.etiqueta(" ", 18f, Font.BOLD, Tema.TEXTO);
    private final JLabel cliente = Componentes.etiqueta(" ", 12f, Font.PLAIN, Tema.APAGADO);
    private final Componentes.Chip defecto = new Componentes.Chip("POR DEFECTO", Tema.ACENTO_FUERTE);
    private final JLabel recibidos = numero();
    private final JLabel enviados = numero();
    private final JLabel pendientes = numero();
    private final JLabel carpeta = Componentes.etiqueta(" ", 12f, Font.PLAIN, Tema.TEXTO_2);
    private final JLabel disco = Componentes.etiqueta(" ", 11.5f, Font.PLAIN, Tema.APAGADO);
    private final Componentes.Barra barra = new Componentes.Barra();
    private final JPanel lista = new JPanel();
    private final Componentes.Boton pausa = new Componentes.Boton("Pausar", Componentes.Variante.SECUNDARIO);

    public PantallaEstado(Motor motor, Runnable salir) {
        this.motor = motor;
        this.salir = salir;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(2, 18, 16, 18));

        JPanel arriba = new JPanel();
        arriba.setOpaque(false);
        arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
        anillo.setAlignmentX(Component.CENTER_ALIGNMENT);
        arriba.add(anillo);
        arriba.add(centrado(estado));
        arriba.add(Box.createVerticalStrut(2));
        arriba.add(centrado(detalle));
        arriba.add(Box.createVerticalStrut(12));

        Componentes.Tarjeta identidad = new Componentes.Tarjeta(new BorderLayout(10, 0), 16);
        identidad.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        JLabel servidor = new JLabel(new Iconos(Iconos.Tipo.SERVIDOR, 20, Tema.ACENTO_FUERTE));
        identidad.add(servidor, BorderLayout.WEST);
        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 1));
        textos.setOpaque(false);
        textos.add(nodo);
        textos.add(cliente);
        identidad.add(textos, BorderLayout.CENTER);
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        chip.setOpaque(false);
        defecto.setVisible(false);
        chip.add(defecto);
        JPanel chipCentro = new JPanel(new java.awt.GridBagLayout());
        chipCentro.setOpaque(false);
        chipCentro.add(chip);
        identidad.add(chipCentro, BorderLayout.EAST);
        arriba.add(limitar(identidad));
        arriba.add(Box.createVerticalStrut(10));

        JPanel numeros = new JPanel(new GridLayout(1, 3, 8, 0));
        numeros.setOpaque(false);
        numeros.add(dato(recibidos, "Guardados", Iconos.Tipo.DESCARGA));
        numeros.add(dato(enviados, "Enviados", Iconos.Tipo.SUBIDA));
        numeros.add(dato(pendientes, "En cola", Iconos.Tipo.ALERTA));
        arriba.add(limitar(numeros));
        arriba.add(Box.createVerticalStrut(10));

        Componentes.Tarjeta almacen = new Componentes.Tarjeta(new BorderLayout(0, 8), 16);
        almacen.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 12));
        JPanel filaCarpeta = new JPanel(new BorderLayout(10, 0));
        filaCarpeta.setOpaque(false);
        filaCarpeta.add(new JLabel(new Iconos(Iconos.Tipo.CARPETA, 18, Tema.APAGADO)), BorderLayout.WEST);
        filaCarpeta.add(carpeta, BorderLayout.CENTER);
        Componentes.Boton abrir = new Componentes.Boton("Abrir", Componentes.Variante.SECUNDARIO);
        abrir.setFont(Tema.fuente(12f, Font.BOLD));
        abrir.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        abrir.addActionListener(e -> abrirCarpeta());
        filaCarpeta.add(abrir, BorderLayout.EAST);
        almacen.add(filaCarpeta, BorderLayout.NORTH);
        JPanel filaDisco = new JPanel(new BorderLayout(0, 6));
        filaDisco.setOpaque(false);
        filaDisco.add(barra, BorderLayout.CENTER);
        filaDisco.add(disco, BorderLayout.SOUTH);
        almacen.add(filaDisco, BorderLayout.CENTER);
        arriba.add(limitar(almacen));
        arriba.add(Box.createVerticalStrut(12));

        JLabel tituloActividad = Componentes.etiqueta("ACTIVIDAD", 10.5f, Font.BOLD, Tema.APAGADO);
        JPanel filaTitulo = new JPanel(new BorderLayout());
        filaTitulo.setOpaque(false);
        filaTitulo.add(tituloActividad, BorderLayout.WEST);
        filaTitulo.setBorder(BorderFactory.createEmptyBorder(0, 4, 6, 0));
        arriba.add(limitar(filaTitulo));
        add(arriba, BorderLayout.NORTH);

        lista.setOpaque(false);
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        JPanel envoltura = new JPanel(new BorderLayout());
        envoltura.setOpaque(false);
        envoltura.add(lista, BorderLayout.NORTH);
        add(envoltura, BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout(8, 0));
        abajo.setOpaque(false);
        abajo.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        pausa.addActionListener(e -> alternarPausa());
        abajo.add(pausa, BorderLayout.CENTER);
        Componentes.Boton mas = new Componentes.Boton("", Componentes.Variante.SECUNDARIO);
        mas.setIcon(new Iconos(Iconos.Tipo.MAS, 18, Tema.TEXTO_2));
        mas.setToolTipText("Más opciones");
        mas.setPreferredSize(new Dimension(46, 42));
        mas.addActionListener(e -> menu(mas));
        abajo.add(mas, BorderLayout.EAST);
        add(abajo, BorderLayout.SOUTH);
    }

    private static JLabel numero() {
        JLabel l = Componentes.etiqueta("0", 18f, Font.BOLD, Tema.TEXTO);
        l.setHorizontalAlignment(SwingConstants.LEFT);
        return l;
    }

    private JPanel centrado(JLabel etiqueta) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        p.setOpaque(false);
        p.add(etiqueta);
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, etiqueta.getPreferredSize().height + 2));
        return p;
    }

    private JPanel limitar(JPanel p) {
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    private JPanel dato(JLabel valor, String titulo, Iconos.Tipo icono) {
        Componentes.Tarjeta t = new Componentes.Tarjeta(new BorderLayout(0, 2), 14);
        t.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 10));
        JLabel nombre = Componentes.etiqueta(titulo, 11f, Font.PLAIN, Tema.APAGADO);
        nombre.setIcon(new Iconos(icono, 12, Tema.APAGADO));
        nombre.setIconTextGap(5);
        t.add(nombre, BorderLayout.NORTH);
        t.add(valor, BorderLayout.CENTER);
        return t;
    }

    public void actualizar() {
        Motor.Estado e = motor.estado();
        switch (e) {
            case CONECTADO -> {
                anillo.estado(Tema.EXITO, false, true);
                estado.setText("Conectado");
                estado.setForeground(Tema.EXITO);
            }
            case CONECTANDO -> {
                anillo.estado(Tema.CIAN, true, false);
                estado.setText("Conectando…");
                estado.setForeground(Tema.CIAN);
            }
            case SIN_CONEXION -> {
                anillo.estado(Tema.AVISO, true, false);
                estado.setText("Sin conexión");
                estado.setForeground(Tema.AVISO);
            }
            case PAUSADO -> {
                anillo.estado(Tema.APAGADO, false, false);
                estado.setText("En pausa");
                estado.setForeground(Tema.TEXTO_2);
            }
            default -> {
                anillo.estado(Tema.PELIGRO, false, false);
                estado.setText("Desvinculado");
                estado.setForeground(Tema.PELIGRO);
            }
        }
        String m = motor.mensaje();
        detalle.setText(m == null || m.isBlank() ? " " : recortar(m, 58));
        nodo.setText(motor.nodo() == null ? "Este equipo" : recortar(motor.nodo(), 26));
        cliente.setText(motor.cliente() == null ? "FileRed" : recortar(motor.cliente(), 36));
        defecto.setText(motor.respaldo() ? "RESPALDO" : "POR DEFECTO");
        defecto.colores(motor.respaldo() ? Tema.CIAN : Tema.ACENTO_FUERTE);
        defecto.setVisible(motor.respaldo() || motor.predeterminado());
        recibidos.setText(String.valueOf(motor.recibidos()));
        enviados.setText(String.valueOf(motor.entregados()));
        pendientes.setText(String.valueOf(motor.pendientes()));
        Path raiz = motor.carpeta();
        if (raiz != null) {
            carpeta.setText(raiz.toString());
            carpeta.setToolTipText(raiz.toString());
            long total = raiz.toFile().getTotalSpace();
            long libre = raiz.toFile().getUsableSpace();
            if (total > 0) {
                barra.valor((double) (total - libre) / total);
                disco.setText(bytes(libre) + " libres de " + bytes(total));
            }
        }
        pausa.setText(e == Motor.Estado.PAUSADO ? "Reanudar" : "Pausar");
        pausa.setIcon(new Iconos(e == Motor.Estado.PAUSADO ? Iconos.Tipo.PLAY : Iconos.Tipo.PAUSA, 15, Tema.TEXTO));
        pintarActividad(motor.actividad());
        revalidate();
        repaint();
    }

    private void pintarActividad(List<Motor.Actividad> eventos) {
        lista.removeAll();
        if (eventos.isEmpty()) {
            JLabel vacio = Componentes.etiqueta("Aquí verás los archivos que reciba y envíe este equipo.", 12f, Font.PLAIN, Tema.APAGADO);
            vacio.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 0));
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            p.add(vacio, BorderLayout.WEST);
            lista.add(p);
            return;
        }
        int limite = Math.min(3, eventos.size());
        for (int i = 0; i < limite; i++) {
            Motor.Actividad a = eventos.get(i);
            JPanel fila = new JPanel(new BorderLayout(9, 0));
            fila.setOpaque(false);
            fila.setBorder(BorderFactory.createEmptyBorder(5, 4, 5, 4));
            Color color = switch (a.tipo()) {
                case "ok" -> Tema.EXITO;
                case "error" -> Tema.PELIGRO;
                case "envio" -> Tema.CIAN;
                default -> Tema.APAGADO;
            };
            JPanel punto = new JPanel(new java.awt.GridBagLayout());
            punto.setOpaque(false);
            punto.add(new Componentes.Punto(color));
            fila.add(punto, BorderLayout.WEST);
            JLabel texto = Componentes.etiqueta(recortar(a.texto(), 44), 12f, Font.PLAIN, Tema.TEXTO_2);
            texto.setToolTipText(a.texto());
            fila.add(texto, BorderLayout.CENTER);
            fila.add(Componentes.etiqueta(a.hora(), 11f, Font.PLAIN, Tema.APAGADO), BorderLayout.EAST);
            fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, fila.getPreferredSize().height));
            lista.add(fila);
        }
    }

    private void alternarPausa() {
        if (motor.estado() == Motor.Estado.PAUSADO) motor.reanudar();
        else motor.pausar();
    }

    private void abrirCarpeta() {
        Path raiz = motor.carpeta();
        if (raiz == null) return;
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(raiz.toFile());
                return;
            }
            String so = System.getProperty("os.name", "").toLowerCase();
            new ProcessBuilder(so.contains("mac") ? "open" : "xdg-open", raiz.toString()).start();
        } catch (IOException | UnsupportedOperationException error) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir la carpeta:\n" + raiz, "FileRed", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void menu(Component origen) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Tema.LINEA_FUERTE), BorderFactory.createEmptyBorder(4, 0, 4, 0)));
        menu.setBackground(Tema.TARJETA_ALTA);
        JCheckBoxMenuItem inicio = new JCheckBoxMenuItem("Iniciar con el equipo", Arranque.activo());
        estilo(inicio);
        inicio.addActionListener(e -> {
            try {
                Arranque.activar(inicio.isSelected());
            } catch (IOException error) {
                JOptionPane.showMessageDialog(this, error.getMessage(), "FileRed", JOptionPane.WARNING_MESSAGE);
            }
        });
        menu.add(inicio);
        menu.addSeparator();
        JMenuItem desvincular = new JMenuItem("Desvincular este equipo", new Iconos(Iconos.Tipo.ENLACE, 14, Tema.PELIGRO));
        estilo(desvincular);
        desvincular.setForeground(Tema.PELIGRO);
        desvincular.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this,
                    "Este equipo dejará de recibir archivos de FileRed.\nLos archivos que ya están en la carpeta se conservan.",
                    "Desvincular equipo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (r == JOptionPane.OK_OPTION) new Thread(motor::desvincular, "FileRed-desvincular").start();
        });
        menu.add(desvincular);
        JMenuItem cerrar = new JMenuItem("Salir de FileRed", new Iconos(Iconos.Tipo.SALIR, 14, Tema.TEXTO_2));
        estilo(cerrar);
        cerrar.addActionListener(e -> salir.run());
        menu.add(cerrar);
        menu.show(origen, origen.getWidth() - menu.getPreferredSize().width, -menu.getPreferredSize().height - 6);
    }

    private void estilo(JMenuItem item) {
        item.setFont(Tema.fuente(13f, Font.PLAIN));
        item.setForeground(Tema.TEXTO);
        item.setBackground(Tema.TARJETA_ALTA);
        item.setOpaque(true);
        item.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 16));
    }

    private static String recortar(String texto, int maximo) {
        return texto.length() > maximo ? texto.substring(0, maximo - 1) + "…" : texto;
    }

    public static String bytes(long valor) {
        String[] unidades = {"B", "KB", "MB", "GB", "TB"};
        double v = valor;
        int i = 0;
        while (v >= 1024 && i < unidades.length - 1) {
            v /= 1024;
            i++;
        }
        return (i == 0 ? String.valueOf((long) v) : String.format(java.util.Locale.US, "%.1f", v)) + " " + unidades[i];
    }
}

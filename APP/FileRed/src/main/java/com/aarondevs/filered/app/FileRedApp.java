package com.aarondevs.filered.app;

import com.aarondevs.filered.app.ui.Ventana;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;

public final class FileRedApp {
    private static boolean consola;
    private static FileLock candado;

    private FileRedApp() {
    }

    public static boolean consola() {
        return consola;
    }

    public static void main(String[] argumentos) {
        System.setProperty("sun.java2d.d3d", "false");
        System.setProperty("sun.java2d.noddraw", "true");
        Map<String, String> opciones = opciones(argumentos);
        if (opciones.containsKey("datos") && !opciones.get("datos").isBlank()) System.setProperty("filered.datos", opciones.get("datos"));
        consola = opciones.containsKey("consola") || opciones.containsKey("servidor") || opciones.containsKey("desvincular") || GraphicsEnvironment.isHeadless();
        if (opciones.containsKey("ayuda") || opciones.containsKey("help")) {
            ayuda();
            return;
        }
        if (!unicaInstancia()) {
            if (consola) System.err.println("FileRed ya se está ejecutando en este equipo.");
            else javax.swing.JOptionPane.showMessageDialog(null, "FileRed ya se está ejecutando en este equipo.", "FileRed",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Configuracion configuracion = new Configuracion();
        Motor motor = new Motor(configuracion);
        if (consola) {
            modoConsola(configuracion, motor, opciones);
            return;
        }
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        boolean oculto = opciones.containsKey("oculto");
        SwingUtilities.invokeLater(() -> new Ventana(configuracion, motor).mostrar(!oculto || !configuracion.vinculado()));
    }

    private static Map<String, String> opciones(String[] argumentos) {
        Map<String, String> opciones = new HashMap<>();
        for (int i = 0; i < argumentos.length; i++) {
            String a = argumentos[i];
            if (!a.startsWith("--")) continue;
            String clave = a.substring(2);
            String valor = i + 1 < argumentos.length && !argumentos[i + 1].startsWith("--") ? argumentos[++i] : "";
            opciones.put(clave, valor);
        }
        return opciones;
    }

    private static boolean unicaInstancia() {
        try {
            Path archivo = Configuracion.carpetaDatos().resolve("filered.lock");
            FileChannel canal = new RandomAccessFile(archivo.toFile(), "rw").getChannel();
            candado = canal.tryLock();
            return candado != null;
        } catch (IOException | RuntimeException error) {
            return true;
        }
    }

    private static void ayuda() {
        System.out.println("""
                FileRed %s

                Uso con ventana:
                  java -jar FileRed.jar

                Uso en servidores sin pantalla:
                  java -jar FileRed.jar --servidor https://tu-filered.com --codigo XXXX-XXXX-XXXX --carpeta /ruta/archivos
                  java -jar FileRed.jar --consola
                  java -jar FileRed.jar --desvincular

                Varias instancias en el mismo equipo:
                  java -jar FileRed.jar --datos /ruta/config-2 --consola
                """.formatted(Configuracion.VERSION));
    }

    private static void modoConsola(Configuracion configuracion, Motor motor, Map<String, String> opciones) {
        System.out.println("FileRed " + Configuracion.VERSION);
        if (opciones.containsKey("desvincular")) {
            motor.desvincular();
            System.out.println("Este equipo se desvinculó de FileRed.");
            return;
        }
        if (opciones.containsKey("servidor") || opciones.containsKey("codigo")) {
            String servidor = ClienteFileRed.normalizar(opciones.get("servidor"));
            String codigo = opciones.getOrDefault("codigo", "");
            Path carpeta = opciones.containsKey("carpeta") && !opciones.get("carpeta").isBlank()
                    ? Paths.get(opciones.get("carpeta")) : Configuracion.carpetaPredeterminada();
            try {
                Files.createDirectories(carpeta);
                Map<String, Object> datos = Motor.datosEquipo(carpeta);
                datos.put("codigo", codigo);
                Map<String, Object> r = new ClienteFileRed(servidor, null).vincular(datos);
                configuracion.servidor(servidor);
                configuracion.token(Json.texto(r, "token"));
                configuracion.carpeta(carpeta);
                configuracion.nodo(Json.texto(r, "nodo"));
                configuracion.cliente(Json.texto(r, "cliente"));
                configuracion.pausado(false);
                configuracion.guardar();
                System.out.println("Vinculado como " + Json.texto(r, "nodo") + " (" + Json.texto(r, "cliente") + ").");
                System.out.println("Carpeta: " + carpeta.toAbsolutePath());
            } catch (IOException | ClienteFileRed.FalloException error) {
                System.err.println("No se pudo vincular: " + error.getMessage());
                System.exit(1);
            }
        }
        if (!configuracion.vinculado()) {
            System.err.println("Este equipo no está vinculado. Usa --servidor, --codigo y --carpeta.");
            System.exit(1);
        }
        configuracion.pausado(false);
        motor.escuchar(m -> {
            if (m.estado() == Motor.Estado.DESVINCULADO) {
                System.err.println(m.mensaje());
                System.exit(2);
            }
        });
        motor.iniciar();
        System.out.println("Guardando archivos en " + configuracion.carpeta().toAbsolutePath());
        try {
            Thread.currentThread().join();
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
        }
    }
}

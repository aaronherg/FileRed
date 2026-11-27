package com.aarondevs.filered.app;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Arranque {
    private Arranque() {
    }

    private static String sistema() {
        return System.getProperty("os.name", "").toLowerCase();
    }

    private static Path archivo() {
        Path casa = Paths.get(System.getProperty("user.home"));
        String so = sistema();
        if (so.contains("win")) {
            String datos = System.getenv("APPDATA");
            Path base = datos == null ? casa.resolve("AppData").resolve("Roaming") : Paths.get(datos);
            return base.resolve("Microsoft").resolve("Windows").resolve("Start Menu").resolve("Programs").resolve("Startup").resolve("FileRed.vbs");
        }
        if (so.contains("mac")) return casa.resolve("Library").resolve("LaunchAgents").resolve("com.aarondevs.filered.plist");
        return casa.resolve(".config").resolve("autostart").resolve("filered.desktop");
    }

    public static boolean activo() {
        return Files.isRegularFile(archivo());
    }

    public static Path jar() {
        try {
            Path ruta = Paths.get(Arranque.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return Files.isRegularFile(ruta) ? ruta.toAbsolutePath() : null;
        } catch (URISyntaxException | SecurityException error) {
            return null;
        }
    }

    private static String java() {
        String comando = ProcessHandle.current().info().command().orElse(null);
        if (comando == null) {
            Path base = Paths.get(System.getProperty("java.home"), "bin");
            comando = base.resolve(sistema().contains("win") ? "javaw.exe" : "java").toString();
        }
        if (sistema().contains("win")) {
            Path ruta = Paths.get(comando);
            Path javaw = ruta.resolveSibling("javaw.exe");
            if (Files.isRegularFile(javaw)) comando = javaw.toString();
        }
        return comando;
    }

    public static void activar(boolean activar) throws IOException {
        Path destino = archivo();
        if (!activar) {
            Files.deleteIfExists(destino);
            return;
        }
        Path jar = jar();
        if (jar == null) throw new IOException("Ejecuta FileRed desde su archivo FileRed.jar para iniciar con el equipo");
        String java = java();
        String so = sistema();
        String contenido;
        if (so.contains("win")) {
            contenido = "CreateObject(\"WScript.Shell\").Run \"\"\"" + java + "\"\" -jar \"\"" + jar + "\"\" --oculto\", 0, False\r\n";
        } else if (so.contains("mac")) {
            contenido = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
                    <plist version="1.0">
                    <dict>
                        <key>Label</key><string>com.aarondevs.filered</string>
                        <key>ProgramArguments</key>
                        <array><string>%s</string><string>-jar</string><string>%s</string><string>--oculto</string></array>
                        <key>RunAtLoad</key><true/>
                    </dict>
                    </plist>
                    """.formatted(escaparXml(java), escaparXml(jar.toString()));
        } else {
            contenido = """
                    [Desktop Entry]
                    Type=Application
                    Name=FileRed
                    Exec="%s" -jar "%s" --oculto
                    X-GNOME-Autostart-enabled=true
                    NoDisplay=false
                    """.formatted(java, jar);
        }
        Files.createDirectories(destino.getParent());
        Files.writeString(destino, contenido, StandardCharsets.UTF_8);
    }

    private static String escaparXml(String valor) {
        return valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

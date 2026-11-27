package com.aarondevs.filered.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Properties;

public final class Configuracion {
    public static final String VERSION = "1.0.0";

    private final Path archivo;
    private final Properties valores = new Properties();

    public Configuracion() {
        this.archivo = carpetaDatos().resolve("filered.properties");
        cargar();
    }

    public static Path carpetaDatos() {
        String propia = System.getProperty("filered.datos");
        if (propia != null && !propia.isBlank()) {
            Path carpeta = Paths.get(propia).toAbsolutePath().normalize();
            try {
                Files.createDirectories(carpeta);
            } catch (IOException ignorado) {
                return carpeta;
            }
            return carpeta;
        }
        String sistema = System.getProperty("os.name", "").toLowerCase();
        Path casa = Paths.get(System.getProperty("user.home"));
        Path carpeta;
        if (sistema.contains("win")) {
            String datos = System.getenv("APPDATA");
            carpeta = (datos == null ? casa : Paths.get(datos)).resolve("FileRed");
        } else if (sistema.contains("mac")) {
            carpeta = casa.resolve("Library").resolve("Application Support").resolve("FileRed");
        } else {
            carpeta = casa.resolve(".config").resolve("filered");
        }
        try {
            Files.createDirectories(carpeta);
        } catch (IOException ignorado) {
            return casa;
        }
        return carpeta;
    }

    public static Path carpetaPredeterminada() {
        return Paths.get(System.getProperty("user.home")).resolve("FileRed");
    }

    private void cargar() {
        if (!Files.isRegularFile(archivo)) return;
        try (InputStream entrada = Files.newInputStream(archivo)) {
            valores.load(entrada);
        } catch (IOException ignorado) {
            valores.clear();
        }
    }

    public synchronized void guardar() {
        Path temporal = archivo.resolveSibling("filered.properties.tmp");
        try (OutputStream salida = Files.newOutputStream(temporal)) {
            valores.store(salida, "FileRed");
        } catch (IOException error) {
            return;
        }
        try {
            Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
            try {
                Files.setPosixFilePermissions(archivo, PosixFilePermissions.fromString("rw-------"));
            } catch (UnsupportedOperationException | IOException ignorado) {
                return;
            }
        } catch (IOException ignorado) {
            return;
        }
    }

    private String valor(String clave) {
        String v = valores.getProperty(clave);
        return v == null || v.isBlank() ? null : v;
    }

    private void poner(String clave, String valor) {
        if (valor == null) valores.remove(clave);
        else valores.setProperty(clave, valor);
    }

    public String servidor() {
        return valor("servidor");
    }

    public void servidor(String valor) {
        poner("servidor", valor);
    }

    public String token() {
        return valor("token");
    }

    public void token(String valor) {
        poner("token", valor);
    }

    public Path carpeta() {
        String v = valor("carpeta");
        return v == null ? null : Paths.get(v);
    }

    public void carpeta(Path valor) {
        poner("carpeta", valor == null ? null : valor.toAbsolutePath().normalize().toString());
    }

    public String nodo() {
        return valor("nodo");
    }

    public void nodo(String valor) {
        poner("nodo", valor);
    }

    public String cliente() {
        return valor("cliente");
    }

    public void cliente(String valor) {
        poner("cliente", valor);
    }

    public boolean pausado() {
        return Boolean.parseBoolean(valores.getProperty("pausado", "false"));
    }

    public void pausado(boolean valor) {
        poner("pausado", String.valueOf(valor));
    }

    public boolean vinculado() {
        return servidor() != null && token() != null && carpeta() != null;
    }

    public void desvincular() {
        poner("token", null);
        poner("nodo", null);
        poner("cliente", null);
        poner("pausado", null);
        guardar();
    }
}

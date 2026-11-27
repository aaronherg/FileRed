package com.aarondevs.filered.app;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class Motor {
    public enum Estado { DETENIDO, CONECTANDO, CONECTADO, SIN_CONEXION, PAUSADO, DESVINCULADO }

    public record Actividad(String hora, String texto, String tipo) {
    }

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Configuracion configuracion;
    private final List<Consumer<Motor>> oyentes = new CopyOnWriteArrayList<>();
    private final Deque<Actividad> actividad = new ArrayDeque<>();
    private final LinkedBlockingQueue<Map<String, Object>> cola = new LinkedBlockingQueue<>();
    private final Set<String> enCola = ConcurrentHashMap.newKeySet();
    private final AtomicLong recibidos = new AtomicLong();
    private final AtomicLong entregados = new AtomicLong();
    private volatile Estado estado = Estado.DETENIDO;
    private volatile String mensaje = "";
    private volatile String nodo;
    private volatile String cliente;
    private volatile long pendientesServidor;
    private volatile boolean predeterminado;
    private volatile String rol;
    private volatile boolean activo;
    private volatile Thread escucha;
    private volatile Thread trabajador;
    private volatile Thread latidos;
    private ExecutorService entregas;
    private volatile ClienteFileRed api;

    public Motor(Configuracion configuracion) {
        this.configuracion = configuracion;
        this.nodo = configuracion.nodo();
        this.cliente = configuracion.cliente();
    }

    public void escuchar(Consumer<Motor> oyente) {
        oyentes.add(oyente);
    }

    private void avisar() {
        for (Consumer<Motor> oyente : oyentes) {
            try {
                oyente.accept(this);
            } catch (RuntimeException ignorado) {
                continue;
            }
        }
    }

    private void registrar(String texto, String tipo) {
        synchronized (actividad) {
            actividad.addFirst(new Actividad(LocalDateTime.now().format(HORA), texto, tipo));
            while (actividad.size() > 40) actividad.removeLast();
        }
        if (FileRedApp.consola()) System.out.println("[" + LocalDateTime.now().format(HORA) + "] " + texto);
        avisar();
    }

    public List<Actividad> actividad() {
        synchronized (actividad) {
            return new ArrayList<>(actividad);
        }
    }

    public Estado estado() {
        return estado;
    }

    public String mensaje() {
        return mensaje;
    }

    public String nodo() {
        return nodo;
    }

    public String cliente() {
        return cliente;
    }

    public long recibidos() {
        return recibidos.get();
    }

    public long entregados() {
        return entregados.get();
    }

    public long pendientes() {
        return Math.max(pendientesServidor, cola.size());
    }

    public boolean predeterminado() {
        return predeterminado;
    }

    public boolean respaldo() {
        return "Respaldo".equals(rol);
    }

    public Path carpeta() {
        return configuracion.carpeta();
    }

    private void estado(Estado nuevo, String texto) {
        boolean cambio = estado != nuevo || !texto.equals(mensaje);
        estado = nuevo;
        mensaje = texto;
        if (cambio) avisar();
    }

    public static Map<String, Object> datosEquipo(Path carpeta) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("equipo", nombreEquipo());
        datos.put("plataforma", System.getProperty("os.name", "") + " " + System.getProperty("os.version", "") + " · " + System.getProperty("os.arch", ""));
        datos.put("version", Configuracion.VERSION);
        datos.put("carpeta", carpeta.toAbsolutePath().normalize().toString());
        datos.put("espacioTotal", carpeta.toFile().getTotalSpace());
        datos.put("espacioLibre", carpeta.toFile().getUsableSpace());
        return datos;
    }

    private static String nombreEquipo() {
        String nombre = System.getenv("COMPUTERNAME");
        if (nombre == null || nombre.isBlank()) nombre = System.getenv("HOSTNAME");
        if (nombre == null || nombre.isBlank()) {
            try {
                nombre = InetAddress.getLocalHost().getHostName();
            } catch (IOException error) {
                nombre = "Equipo";
            }
        }
        return nombre;
    }

    public synchronized void iniciar() {
        if (activo) return;
        if (!configuracion.vinculado()) {
            estado(Estado.DESVINCULADO, "Vincula este equipo para empezar");
            return;
        }
        if (configuracion.pausado()) {
            estado(Estado.PAUSADO, "Sincronización en pausa");
            return;
        }
        activo = true;
        api = new ClienteFileRed(configuracion.servidor(), configuracion.token());
        try {
            Files.createDirectories(configuracion.carpeta());
        } catch (IOException error) {
            activo = false;
            estado(Estado.SIN_CONEXION, "No se puede usar la carpeta " + configuracion.carpeta());
            return;
        }
        estado(Estado.CONECTANDO, "Conectando con FileRed…");
        entregas = Executors.newFixedThreadPool(4, r -> hilo(r, "FileRed-entregas"));
        trabajador = hilo(this::trabajar, "FileRed-trabajo");
        escucha = hilo(this::escuchar, "FileRed-escucha");
        latidos = hilo(this::latir, "FileRed-latido");
        trabajador.start();
        escucha.start();
        latidos.start();
    }

    private static Thread hilo(Runnable tarea, String nombre) {
        Thread hilo = new Thread(tarea, nombre);
        hilo.setDaemon(true);
        return hilo;
    }

    public synchronized void detener() {
        activo = false;
        for (Thread h : new Thread[]{escucha, trabajador, latidos}) if (h != null) h.interrupt();
        if (entregas != null) entregas.shutdownNow();
        cola.clear();
        enCola.clear();
    }

    public void pausar() {
        detener();
        configuracion.pausado(true);
        configuracion.guardar();
        estado(Estado.PAUSADO, "Sincronización en pausa");
        registrar("Sincronización pausada", "info");
    }

    public void reanudar() {
        configuracion.pausado(false);
        configuracion.guardar();
        registrar("Sincronización reanudada", "info");
        iniciar();
    }

    public void vinculado(String nuevoNodo, String nuevoCliente) {
        nodo = nuevoNodo;
        cliente = nuevoCliente;
        recibidos.set(0);
        entregados.set(0);
        synchronized (actividad) {
            actividad.clear();
        }
        registrar("Equipo vinculado a " + (nuevoCliente == null ? "FileRed" : nuevoCliente), "ok");
        iniciar();
    }

    public void desvincular() {
        ClienteFileRed actual = api;
        detener();
        if (actual != null) {
            Thread h = hilo(() -> {
                try {
                    actual.desvincular();
                } catch (ClienteFileRed.FalloException ignorado) {
                    return;
                }
            }, "FileRed-desvincular");
            h.start();
            try {
                h.join(4000);
            } catch (InterruptedException ignorado) {
                Thread.currentThread().interrupt();
            }
        }
        configuracion.desvincular();
        nodo = null;
        cliente = null;
        estado(Estado.DESVINCULADO, "Este equipo se desvinculó de FileRed");
    }

    private void perdioVinculo(String texto) {
        detener();
        configuracion.desvincular();
        nodo = null;
        cliente = null;
        estado(Estado.DESVINCULADO, texto == null ? "Este equipo ya no está vinculado" : texto);
        registrar("El equipo fue desvinculado desde FileRed", "error");
    }

    private void latir() {
        while (activo && !Thread.currentThread().isInterrupted()) {
            try {
                Map<String, Object> r = api.estado(datosEquipo(configuracion.carpeta()));
                String n = Json.texto(r, "nodo");
                String c = Json.texto(r, "cliente");
                if (n != null && !n.equals(nodo)) {
                    nodo = n;
                    configuracion.nodo(n);
                    configuracion.guardar();
                }
                if (c != null && !c.equals(cliente)) {
                    cliente = c;
                    configuracion.cliente(c);
                    configuracion.guardar();
                }
                pendientesServidor = Json.numero(r, "pendientes");
                predeterminado = Boolean.TRUE.equals(r.get("predeterminado"));
                rol = Json.texto(r, "rol");
                avisar();
            } catch (ClienteFileRed.FalloException error) {
                if (error.desvinculado()) {
                    perdioVinculo(error.getMessage());
                    return;
                }
            }
            try {
                Thread.sleep(30000);
            } catch (InterruptedException error) {
                return;
            }
        }
    }

    private void escuchar() {
        long espera = 2000;
        while (activo && !Thread.currentThread().isInterrupted()) {
            try {
                List<Object> ordenes = api.ordenes();
                if (estado != Estado.CONECTADO) {
                    estado(Estado.CONECTADO, "Conectado y listo para recibir archivos");
                    registrar("Conectado a " + api.servidor(), "ok");
                }
                espera = 2000;
                for (Object o : ordenes) {
                    if (!(o instanceof Map<?, ?> mapa)) continue;
                    @SuppressWarnings("unchecked")
                    Map<String, Object> orden = (Map<String, Object>) mapa;
                    String tipo = Json.texto(orden, "tipo");
                    String id = Json.texto(orden, "id");
                    if (id == null || tipo == null) continue;
                    if ("ENTREGAR".equals(tipo)) entregas.submit(() -> entregar(orden));
                    else if (enCola.add(id)) cola.add(orden);
                }
            } catch (ClienteFileRed.FalloException error) {
                if (error.desvinculado()) {
                    perdioVinculo(error.getMessage());
                    return;
                }
                if (!activo) return;
                if (estado != Estado.SIN_CONEXION) registrar("Sin conexión: " + error.getMessage(), "error");
                estado(Estado.SIN_CONEXION, error.getMessage() + ". Reintentando…");
                try {
                    Thread.sleep(espera);
                } catch (InterruptedException interrupcion) {
                    return;
                }
                espera = Math.min(espera * 2, 30000);
            } catch (RuntimeException error) {
                try {
                    Thread.sleep(espera);
                } catch (InterruptedException interrupcion) {
                    return;
                }
            }
        }
    }

    private void trabajar() {
        while (activo && !Thread.currentThread().isInterrupted()) {
            Map<String, Object> orden;
            try {
                orden = cola.poll(1, TimeUnit.SECONDS);
            } catch (InterruptedException error) {
                return;
            }
            if (orden == null) continue;
            String id = Json.texto(orden, "id");
            try {
                procesar(orden);
            } finally {
                enCola.remove(id);
                avisar();
            }
        }
    }

    private void procesar(Map<String, Object> orden) {
        String id = Json.texto(orden, "id");
        String tipo = Json.texto(orden, "tipo");
        String ruta = Json.texto(orden, "ruta");
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("ruta", ruta);
        try {
            switch (tipo) {
                case "GUARDAR", "COPIAR" -> {
                    String hash = guardar(id, ruta, Json.texto(orden, "hash"));
                    respuesta.put("hash", hash);
                    recibidos.incrementAndGet();
                    registrar(("COPIAR".equals(tipo) ? "Respaldado " : "Recibido ") + nombre(ruta), "ok");
                }
                case "CREAR" -> Files.createDirectories(resolver(ruta));
                case "MOVER" -> {
                    mover(resolver(ruta), resolver(Json.texto(orden, "destino")));
                    registrar("Renombrado " + ruta + " → " + Json.texto(orden, "destino"), "info");
                }
                case "ELIMINAR" -> {
                    eliminar(resolver(ruta));
                    registrar("Eliminado " + nombre(ruta), "info");
                }
                default -> throw new IOException("Orden desconocida: " + tipo);
            }
            respuesta.put("ok", true);
        } catch (IOException | RuntimeException error) {
            respuesta.put("ok", false);
            respuesta.put("error", error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage());
            registrar("Error con " + nombre(ruta) + ": " + respuesta.get("error"), "error");
        }
        for (int intento = 0; intento < 3 && activo; intento++) {
            try {
                api.confirmar(id, respuesta);
                return;
            } catch (ClienteFileRed.FalloException error) {
                if (error.desvinculado()) {
                    perdioVinculo(error.getMessage());
                    return;
                }
                try {
                    Thread.sleep(2000L * (intento + 1));
                } catch (InterruptedException interrupcion) {
                    return;
                }
            }
        }
    }

    private String guardar(String id, String ruta, String esperado) throws IOException {
        Path destino = resolver(ruta);
        Path temporales = configuracion.carpeta().resolve(".filered-tmp");
        Files.createDirectories(temporales);
        Path parcial = temporales.resolve(id + ".part");
        try {
            api.descargar(id, parcial);
        } catch (ClienteFileRed.FalloException error) {
            Files.deleteIfExists(parcial);
            if (error.desvinculado()) throw new IOException("Equipo desvinculado");
            throw new IOException(error.getMessage());
        }
        String hash = hash(parcial);
        if (esperado != null && !esperado.equalsIgnoreCase(hash)) {
            Files.deleteIfExists(parcial);
            throw new IOException("El archivo recibido llegó incompleto");
        }
        Files.createDirectories(destino.getParent());
        try {
            Files.move(parcial, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException error) {
            Files.move(parcial, destino, StandardCopyOption.REPLACE_EXISTING);
        }
        return hash;
    }

    private void entregar(Map<String, Object> orden) {
        String id = Json.texto(orden, "id");
        String ruta = Json.texto(orden, "ruta");
        try {
            Path archivo;
            try {
                archivo = resolver(ruta);
            } catch (IOException error) {
                api.entregarError(id, error.getMessage());
                return;
            }
            if (!Files.isRegularFile(archivo)) {
                api.entregarError(id, "El archivo no existe en el equipo");
                registrar("No se encontró " + nombre(ruta), "error");
                return;
            }
            api.entregar(id, archivo);
            entregados.incrementAndGet();
            registrar("Enviado " + nombre(ruta), "envio");
        } catch (ClienteFileRed.FalloException error) {
            if (error.desvinculado()) perdioVinculo(error.getMessage());
        }
    }

    private Path resolver(String ruta) throws IOException {
        if (ruta == null || ruta.isBlank()) throw new IOException("Ruta vacía");
        Path raiz = configuracion.carpeta().toAbsolutePath().normalize();
        for (String parte : ruta.split("/")) {
            if (parte.isBlank() || parte.equals(".") || parte.equals("..") || parte.contains("\\") || parte.contains(":")) {
                throw new IOException("Ruta no permitida");
            }
        }
        Path destino = raiz.resolve(ruta).normalize();
        if (!destino.startsWith(raiz) || destino.equals(raiz)) throw new IOException("Ruta no permitida");
        return destino;
    }

    private void mover(Path origen, Path destino) throws IOException {
        if (!Files.exists(origen)) return;
        if (!Files.exists(destino)) {
            Files.createDirectories(destino.getParent());
            Files.move(origen, destino);
            return;
        }
        if (Files.isDirectory(origen) && Files.isDirectory(destino)) {
            try (DirectoryStream<Path> hijos = Files.newDirectoryStream(origen)) {
                for (Path hijo : hijos) mover(hijo, destino.resolve(hijo.getFileName().toString()));
            }
            Files.deleteIfExists(origen);
        } else {
            Files.move(origen, destino, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void eliminar(Path ruta) throws IOException {
        if (!Files.exists(ruta)) return;
        try (Stream<Path> recorrido = Files.walk(ruta)) {
            for (Path elemento : recorrido.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(elemento);
        }
    }

    private static String nombre(String ruta) {
        if (ruta == null) return "";
        int i = ruta.lastIndexOf('/');
        return i >= 0 ? ruta.substring(i + 1) : ruta;
    }

    public static String hash(Path archivo) throws IOException {
        try (InputStream entrada = Files.newInputStream(archivo)) {
            MessageDigest resumen = MessageDigest.getInstance("SHA-256");
            byte[] bloque = new byte[65536];
            int leidos;
            while ((leidos = entrada.read(bloque)) > 0) resumen.update(bloque, 0, leidos);
            return HexFormat.of().formatHex(resumen.digest());
        } catch (NoSuchAlgorithmException error) {
            throw new IOException("SHA-256 no disponible");
        }
    }
}

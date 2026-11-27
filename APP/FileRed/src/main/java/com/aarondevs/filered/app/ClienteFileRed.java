package com.aarondevs.filered.app;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public final class ClienteFileRed {
    private static final String ENCABEZADO = "X-Nodo-Token";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private final String servidor;
    private final String token;

    public ClienteFileRed(String servidor, String token) {
        this.servidor = normalizar(servidor);
        this.token = token;
    }

    public static String normalizar(String servidor) {
        String limpio = servidor == null ? "" : servidor.trim();
        while (limpio.endsWith("/")) limpio = limpio.substring(0, limpio.length() - 1);
        if (!limpio.isEmpty() && !limpio.matches("(?i)^https?://.*")) limpio = "https://" + limpio;
        return limpio;
    }

    public String servidor() {
        return servidor;
    }

    private HttpRequest.Builder peticion(String ruta, Duration espera) {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(servidor + ruta)).timeout(espera)
                .header("Accept", "application/json")
                .header("User-Agent", "FileRed/" + Configuracion.VERSION);
        if (token != null) b.header(ENCABEZADO, token);
        return b;
    }

    public Map<String, Object> vincular(Map<String, Object> datos) throws FalloException {
        return Json.objeto(enviarJson("/api/nodo/vincular", datos, Duration.ofSeconds(30)));
    }

    public Map<String, Object> estado(Map<String, Object> datos) throws FalloException {
        return Json.objeto(enviarJson("/api/nodo/estado", datos, Duration.ofSeconds(30)));
    }

    public List<Object> ordenes() throws FalloException {
        HttpResponse<String> r = ejecutar(peticion("/api/nodo/ordenes", Duration.ofSeconds(50)).GET().build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        revisar(r.statusCode(), r.body());
        return Json.lista(r.body());
    }

    public void descargar(String tareaId, Path destino) throws FalloException {
        HttpResponse<Path> r = ejecutar(peticion("/api/nodo/contenido/" + tareaId, Duration.ofMinutes(30)).GET().build(),
                HttpResponse.BodyHandlers.ofFile(destino));
        if (r.statusCode() >= 300) {
            String cuerpo;
            try {
                cuerpo = java.nio.file.Files.readString(destino);
            } catch (IOException error) {
                cuerpo = "";
            }
            revisar(r.statusCode(), cuerpo);
        }
    }

    public void confirmar(String tareaId, Map<String, Object> datos) throws FalloException {
        enviarJson("/api/nodo/confirmar/" + tareaId, datos, Duration.ofSeconds(30));
    }

    public void entregar(String solicitud, Path archivo) throws FalloException {
        HttpRequest peticion;
        try {
            peticion = peticion("/api/nodo/entregar/" + solicitud, Duration.ofMinutes(30))
                    .header("Content-Type", "application/octet-stream")
                    .POST(HttpRequest.BodyPublishers.ofFile(archivo)).build();
        } catch (java.io.FileNotFoundException error) {
            entregarError(solicitud, "El archivo no existe en el equipo");
            return;
        }
        HttpResponse<String> r = ejecutar(peticion, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        revisar(r.statusCode(), r.body());
    }

    public void entregarError(String solicitud, String mensaje) throws FalloException {
        HttpRequest peticion = peticion("/api/nodo/entregar/" + solicitud, Duration.ofSeconds(30))
                .header("X-Nodo-Error", URLEncoder.encode(mensaje, StandardCharsets.UTF_8))
                .POST(HttpRequest.BodyPublishers.noBody()).build();
        HttpResponse<String> r = ejecutar(peticion, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        revisar(r.statusCode(), r.body());
    }

    public void desvincular() throws FalloException {
        enviarJson("/api/nodo/desvincular", Map.of(), Duration.ofSeconds(15));
    }

    private String enviarJson(String ruta, Map<String, Object> datos, Duration espera) throws FalloException {
        HttpRequest peticion = peticion(ruta, espera).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(Json.escribir(datos), StandardCharsets.UTF_8)).build();
        HttpResponse<String> r = ejecutar(peticion, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        revisar(r.statusCode(), r.body());
        return r.body();
    }

    private <T> HttpResponse<T> ejecutar(HttpRequest peticion, HttpResponse.BodyHandler<T> manejador) throws FalloException {
        try {
            return http.send(peticion, manejador);
        } catch (HttpConnectTimeoutException | ConnectException error) {
            throw new FalloException(0, "No se pudo conectar con " + servidor, false);
        } catch (HttpTimeoutException error) {
            throw new FalloException(0, "El servidor tardó demasiado en responder", false);
        } catch (IOException error) {
            throw new FalloException(0, "Sin conexión con el servidor", false);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new FalloException(0, "Operación interrumpida", false);
        } catch (IllegalArgumentException error) {
            throw new FalloException(0, "La dirección del servidor no es válida", false);
        }
    }

    private void revisar(int estado, String cuerpo) throws FalloException {
        if (estado >= 200 && estado < 300) return;
        String mensaje = Json.texto(Json.objeto(cuerpo), "mensage");
        if (mensaje == null) mensaje = estado == 404 ? "El servidor no tiene FileRed o la dirección no es correcta" : "El servidor respondió " + estado;
        throw new FalloException(estado, mensaje, estado == 401);
    }

    public static final class FalloException extends Exception {
        private final int estado;
        private final boolean desvinculado;

        public FalloException(int estado, String mensaje, boolean desvinculado) {
            super(mensaje);
            this.estado = estado;
            this.desvinculado = desvinculado;
        }

        public int estado() {
            return estado;
        }

        public boolean desvinculado() {
            return desvinculado;
        }
    }
}

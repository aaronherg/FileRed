package com.aarondevs.filered.Controllers.Archivos;

import com.aarondevs.filered.Models.Archivos.Nodo;
import com.aarondevs.filered.Services.Archivos.NodoService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequestMapping("/api/nodo")
public class NodoProtocoloController {
    public static final String ENCABEZADO = "X-Nodo-Token";

    private final NodoService service;

    public NodoProtocoloController(NodoService service) {
        this.service = service;
    }

    private Nodo nodo(HttpServletRequest request) {
        return service.autenticar(request.getHeader(ENCABEZADO));
    }

    @PostMapping("/vincular")
    public Map<String, Object> vincular(@RequestBody(required = false) Map<String, Object> datos) {
        return service.vincular(datos == null ? Map.of() : datos);
    }

    @PostMapping("/estado")
    public Map<String, Object> estado(@RequestBody(required = false) Map<String, Object> datos, HttpServletRequest request) {
        return service.latido(nodo(request), datos == null ? Map.of() : datos);
    }

    @GetMapping("/ordenes")
    public DeferredResult<List<Map<String, Object>>> ordenes(HttpServletRequest request) {
        return service.ordenes(nodo(request));
    }

    @GetMapping("/contenido/{tareaId}")
    public ResponseEntity<Resource> contenido(@PathVariable Long tareaId, HttpServletRequest request) throws IOException {
        NodoService.Entrega entrega = service.contenido(nodo(request), tareaId);
        Path ruta = entrega.ruta();
        if (!entrega.temporal()) return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(new FileSystemResource(ruta));
        long tamano = java.nio.file.Files.size(ruta);
        java.io.InputStream entrada = new java.io.FilterInputStream(java.nio.file.Files.newInputStream(ruta)) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    java.nio.file.Files.deleteIfExists(ruta);
                }
            }
        };
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).contentLength(tamano)
                .body(new org.springframework.core.io.InputStreamResource(entrada));
    }

    @PostMapping("/confirmar/{tareaId}")
    public Map<String, Object> confirmar(@PathVariable Long tareaId, @RequestBody(required = false) Map<String, Object> datos,
                                         HttpServletRequest request) {
        return service.confirmar(nodo(request), tareaId, datos == null ? Map.of() : datos);
    }

    @PostMapping("/entregar/{solicitud}")
    public Map<String, Object> entregar(@PathVariable String solicitud, HttpServletRequest request) throws IOException {
        Nodo nodo = nodo(request);
        String error = request.getHeader("X-Nodo-Error");
        service.recibirEntrega(nodo, solicitud, request.getInputStream(), error == null ? null : URLDecoder.decode(error, StandardCharsets.UTF_8));
        return Map.of("ok", true);
    }

    @PostMapping("/desvincular")
    public Map<String, Object> desvincular(HttpServletRequest request) {
        service.desvincularEquipo(nodo(request));
        return Map.of("ok", true);
    }

    @ExceptionHandler(NodoService.NodoNoAutorizadoException.class)
    public ResponseEntity<Map<String, Object>> noAutorizado(NodoService.NodoNoAutorizadoException error) {
        return ResponseEntity.status(401).body(Map.of("mensage", error.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> invalido(IllegalArgumentException error) {
        return ResponseEntity.badRequest().body(Map.of("mensage", error.getMessage() == null ? "Solicitud no válida" : error.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> retirado(IllegalStateException error) {
        return ResponseEntity.status(410).body(Map.of("mensage", error.getMessage() == null ? "No disponible" : error.getMessage()));
    }
}

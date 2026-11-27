package com.aarondevs.filered.Services.Archivos;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

@Component
public class NodoCanal {
    private final Map<Long, DeferredResult<List<Map<String, Object>>>> esperando = new ConcurrentHashMap<>();
    private final Map<Long, Queue<Map<String, Object>>> entregas = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<Path>> solicitudes = new ConcurrentHashMap<>();

    public void esperar(Long nodoId, DeferredResult<List<Map<String, Object>>> resultado) {
        DeferredResult<List<Map<String, Object>>> anterior = esperando.put(nodoId, resultado);
        if (anterior != null && anterior != resultado) anterior.setResult(List.of());
        resultado.onCompletion(() -> esperando.remove(nodoId, resultado));
        resultado.onTimeout(() -> esperando.remove(nodoId, resultado));
        resultado.onError(error -> esperando.remove(nodoId, resultado));
    }

    public DeferredResult<List<Map<String, Object>>> tomar(Long nodoId) {
        return esperando.remove(nodoId);
    }

    public void encolarEntrega(Long nodoId, Map<String, Object> orden) {
        entregas.computeIfAbsent(nodoId, id -> new ConcurrentLinkedQueue<>()).add(orden);
    }

    public boolean hayEntregas(Long nodoId) {
        Queue<Map<String, Object>> cola = entregas.get(nodoId);
        return cola != null && !cola.isEmpty();
    }

    public List<Map<String, Object>> sacarEntregas(Long nodoId) {
        Queue<Map<String, Object>> cola = entregas.get(nodoId);
        List<Map<String, Object>> lista = new ArrayList<>();
        if (cola == null) return lista;
        Map<String, Object> orden;
        while ((orden = cola.poll()) != null) lista.add(orden);
        return lista;
    }

    public CompletableFuture<Path> nuevaSolicitud(String solicitud) {
        CompletableFuture<Path> futuro = new CompletableFuture<>();
        solicitudes.put(solicitud, futuro);
        return futuro;
    }

    public CompletableFuture<Path> solicitud(String solicitud) {
        return solicitudes.get(solicitud);
    }

    public void cerrarSolicitud(String solicitud) {
        solicitudes.remove(solicitud);
    }

    public void olvidar(Long nodoId) {
        entregas.remove(nodoId);
        DeferredResult<List<Map<String, Object>>> pendiente = esperando.remove(nodoId);
        if (pendiente != null) pendiente.setResult(List.of());
    }
}

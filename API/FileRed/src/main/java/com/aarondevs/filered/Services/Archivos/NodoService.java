package com.aarondevs.filered.Services.Archivos;

import com.aarondevs.filered.Models.Archivos.Nodo;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.springframework.web.context.request.async.DeferredResult;

public interface NodoService {
    List<Map<String, Object>> listar(Long clienteId);

    Map<String, Object> crear(Long clienteId, String nombre, String rol, List<Long> origenes, String actor);

    Map<String, Object> origenes(Long clienteId, Long nodoId, List<Long> origenes);

    Map<String, Object> renombrar(Long clienteId, Long nodoId, String nombre, String actor);

    Map<String, Object> generarCodigo(Long clienteId, Long nodoId, String actor);

    Map<String, Object> eliminar(Long clienteId, Long nodoId);

    Map<String, Object> predeterminar(Long clienteId, Long nodoId);

    Nodo delCliente(Long clienteId, Long nodoId);

    Long predeterminado(Long clienteId);

    Map<String, Object> vincular(Map<String, Object> datos);

    Nodo autenticar(String token);

    Map<String, Object> latido(Nodo nodo, Map<String, Object> datos);

    DeferredResult<List<Map<String, Object>>> ordenes(Nodo nodo);

    Entrega contenido(Nodo nodo, Long tareaId);

    Map<String, Object> confirmar(Nodo nodo, Long tareaId, Map<String, Object> datos);

    void recibirEntrega(Nodo nodo, String solicitud, InputStream contenido, String error);

    void desvincularEquipo(Nodo nodo);

    Entrega publico(String clienteUuid, String almacen, String categoria, String archivo);

    void avisar(Long nodoId);

    Path temporal(Nodo nodo, String identificador, String extension);

    Path pedirAlEquipo(Nodo nodo, String ruta, String hash);

    record Entrega(Path ruta, boolean temporal) {
    }

    class NodoNoDisponibleException extends RuntimeException {
        public NodoNoDisponibleException(String mensaje) {
            super(mensaje);
        }
    }

    class NodoNoAutorizadoException extends RuntimeException {
        public NodoNoAutorizadoException(String mensaje) {
            super(mensaje);
        }
    }
}

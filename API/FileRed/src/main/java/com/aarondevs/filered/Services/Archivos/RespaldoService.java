package com.aarondevs.filered.Services.Archivos;

import com.aarondevs.filered.Models.Archivos.Archivo;
import com.aarondevs.filered.Models.Archivos.Nodo;
import com.aarondevs.filered.Models.Archivos.NodoTarea;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface RespaldoService {
    void alSubir(Long clienteId, Long origenId, Long archivoId);

    void alReemplazar(Long clienteId, Long origenId, Archivo anterior, String extensionNueva);

    void alEliminarArchivo(Long clienteId, Long origenId, Archivo archivo);

    void alCrearDirectorio(Long clienteId, Long origenId, String ruta);

    void alMoverDirectorio(Long clienteId, Long origenId, String desde, String hasta);

    void alEliminarDirectorio(Long clienteId, Long origenId, String ruta);

    void alRenombrarOrigen(Long clienteId, Long nodoId, String nombreAnterior, String nombreNuevo);

    void alEliminarOrigen(Long clienteId, Nodo origen);

    void configurar(Long clienteId, Nodo respaldo, Collection<Long> origenes);

    Map<String, Object> orden(Nodo respaldo, NodoTarea tarea);

    NodoService.Entrega contenido(Nodo respaldo, NodoTarea tarea);

    void confirmar(Nodo respaldo, NodoTarea tarea, boolean ok, String hash, String error, String ruta);

    Path desdeRespaldo(Archivo archivo);

    Map<String, Object> restaurar(Long clienteId, Long nodoId);

    Map<String, Object> restauracion(Long nodoId);

    Map<Long, List<Long>> origenesCliente(Long clienteId);

    Map<Long, Map<String, Object>> progreso(Long clienteId);

    Map<Long, Long> respaldadosPorOrigen(Long clienteId);

    boolean permiteNodos(Long clienteId);

    boolean permiteNube(Long clienteId);

    boolean permiteRespaldos(Long clienteId);
}

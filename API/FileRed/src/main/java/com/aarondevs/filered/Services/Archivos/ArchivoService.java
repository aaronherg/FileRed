package com.aarondevs.filered.Services.Archivos;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public interface ArchivoService {
    Map<String, Object> crearAlmacen(Long clienteId, String almacen);

    Map<String, Object> renombrarAlmacen(Long clienteId, String almacen, String nombre, String actor);

    Map<String, Object> eliminarAlmacen(Long clienteId, String almacen);

    Map<String, Object> renombrarCarpeta(Long clienteId, String almacen, String categoria, String nombre, String actor);

    Map<String, Object> renombrarArchivo(Long clienteId, String almacen, String categoria, String identificador, String nombre, String actor);

    Map<String, Object> paginas(Long clienteId, String almacen, String categoria, String buscar, Integer page, Integer size);

    Map<String, Object> crearCarpeta(Long clienteId, String almacen, String categoria);

    Map<String, Object> eliminarCarpeta(Long clienteId, String almacen, String categoria);

    List<Map<String, Object>> almacenes(Long clienteId);

    List<Map<String, Object>> carpetas(Long clienteId, String almacen);

    Map<String, Object> subir(Long clienteId, String almacen, String categoria, MultipartFile archivo, Long apiKeyId, String actor,
                              boolean incluirBase64);

    Map<String, Object> reemplazar(Long clienteId, String almacen, String categoria, String identificador, String extensionActual,
                                   MultipartFile archivo, String actor, boolean incluirBase64);

    Map<String, Object> eliminar(Long clienteId, String almacen, String categoria, String identificador, String extension);

    List<Map<String, Object>> archivos(Long clienteId, String almacen, String categoria);

    Map<String, Object> archivo(Long clienteId, String almacen, String categoria, String identificador, String extension);

    void registrarSolicitudApi(Long clienteId);

    Map<String, Object> archivoPorId(Long clienteId, String almacen, String categoria, String identificador);

    Path publico(String clienteUuid, String almacen, String categoria, String archivo);

    String mime(String extension);
}

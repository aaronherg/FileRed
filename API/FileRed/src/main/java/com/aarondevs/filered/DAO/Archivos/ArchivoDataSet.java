package com.aarondevs.filered.DAO.Archivos;

import com.aarondevs.filered.Models.Archivos.Archivo;
import com.aarondevs.filered.Parameters.Response.PaginationResponse;
import java.util.List;
import java.util.Map;

public interface ArchivoDataSet {
    Archivo findByRuta(Long clienteId, String almacen, String categoria, String identificador);

    List<Archivo> findAll(Long clienteId, String almacen, String categoria);

    void create(Long clienteId, String almacen, String categoria, String identificador, String extension, String nombreOriginal,
                long tamano, Long apiKeyId, String actor);

    void update(Long archivoId, String extension, String nombreOriginal, long tamano, String actor);

    int delete(Long clienteId, String almacen, String categoria, String identificador);

    int deleteCategoria(Long clienteId, String almacen, String categoria);

    String clienteUuid(Long clienteId);

    Long clienteActivoPorUuid(String clienteUuid);

    void sumarMetrica(Long clienteId, String campo, long cantidad);

    PaginationResponse<Archivo> findAllPages(Long clienteId, String almacen, String categoria, String buscar, Integer page, Integer size);

    List<Map<String, Object>> resumenAlmacenes(Long clienteId);

    List<Map<String, Object>> resumenCarpetas(Long clienteId, String almacen);

    int renombrarAlmacen(Long clienteId, String almacen, String nombre, String actor);

    int renombrarCategoria(Long clienteId, String almacen, String categoria, String nombre, String actor);

    int deleteAlmacen(Long clienteId, String almacen);

    int updateNombre(Long clienteId, String almacen, String categoria, String identificador, String nombreOriginal, String actor);
}

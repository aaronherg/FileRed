package com.aarondevs.filered.DAO.Archivos;

import com.aarondevs.filered.Models.Archivos.Archivo;
import com.aarondevs.filered.Models.Archivos.Nodo;
import com.aarondevs.filered.Models.Archivos.NodoTarea;
import com.aarondevs.filered.Parameters.Response.PaginationResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface NodoDataSet {
    List<Nodo> findByCliente(Long clienteId);

    Nodo findById(Long nodoId);

    Nodo findByToken(String tokenHash);

    Nodo findByCodigo(String codigoHash);

    boolean existeNombre(Long clienteId, String nombre, Long excluirNodoId);

    Long create(Long clienteId, String nombre, String rol, String codigoHash, LocalDateTime expira, String actor);

    void updateNombre(Long nodoId, String nombre, String actor);

    void asignarCodigo(Long nodoId, String codigoHash, LocalDateTime expira, String actor);

    void vincular(Long nodoId, String tokenHash, String equipo, String plataforma, String version, String carpeta, Long total, Long libre);

    void latido(Long nodoId, String version, String carpeta, Long total, Long libre);

    void tocar(Long nodoId);

    boolean conectado(Long nodoId);

    List<Long> conectados(Long clienteId);

    List<Long> codigosVigentes(Long clienteId);

    void desvincular(Long nodoId);

    void delete(Long nodoId);

    void marcarSinPlan(Long nodoId);

    void marcarNubeSinPlan(Long clienteId);

    void limpiarNubeSinPlan(Long clienteId);

    java.sql.Timestamp nubeSinPlanDesde(Long clienteId);

    List<Long> clientesConNube();

    List<Long> clientesNubeVencida(int dias);

    void limpiarSinPlan(Long nodoId);

    List<Long> clientesConNodos();

    List<Nodo> vencidosSinPlan(int dias);

    String nombreCliente(Long clienteId);

    Long predeterminado(Long clienteId);

    void predeterminar(Long clienteId, Long nodoId);

    List<Map<String, Object>> resumenNodos(Long clienteId);

    Map<String, Object> resumenNube(Long clienteId);

    long tareasPendientes(Long nodoId);

    boolean existeDirectorio(Long nodoId, String almacen, String categoria);

    void crearDirectorio(Long nodoId, String almacen, String categoria);

    List<String> almacenes(Long nodoId);

    List<Map<String, Object>> conteoCarpetas(Long nodoId);

    List<String> carpetas(Long nodoId, String almacen);

    void renombrarAlmacen(Long nodoId, String almacen, String nombre, String actor);

    void renombrarCarpeta(Long nodoId, String almacen, String categoria, String nombre, String actor);

    void deleteAlmacen(Long nodoId, String almacen);

    void deleteCarpeta(Long nodoId, String almacen, String categoria);

    List<Map<String, Object>> resumenAlmacenes(Long nodoId);

    List<Map<String, Object>> resumenCarpetas(Long nodoId, String almacen);

    PaginationResponse<Archivo> findAllPages(Long nodoId, String almacen, String categoria, String buscar, Integer page, Integer size);

    List<Archivo> findArchivos(Long nodoId, String almacen, String categoria);

    List<Archivo> findArchivosAlmacen(Long nodoId, String almacen);

    List<Archivo> findArchivosNodo(Long nodoId);

    Archivo findArchivo(Long nodoId, String almacen, String categoria, String identificador);

    Archivo findArchivoById(Long archivoId);

    Archivo findArchivoPublico(Long clienteId, String almacen, String categoria, String identificador);

    Long createArchivo(Long clienteId, Long nodoId, String almacen, String categoria, String identificador, String extension,
                       String nombreOriginal, long tamano, String hash, Long apiKeyId, String actor);

    void updateArchivo(Long archivoId, String extension, String nombreOriginal, long tamano, String hash, String actor);

    int marcarListo(Long archivoId, String hash);

    int updateNombre(Long nodoId, String almacen, String categoria, String identificador, String nombreOriginal, String actor);

    void deleteArchivo(Long archivoId);

    Long nodoDeArchivo(Long clienteId, String almacen, String categoria, String identificador);

    void crearTarea(Long nodoId, String tipo, Long archivoId, String ruta, String destino);

    List<NodoTarea> tareasParaEnviar(Long nodoId, int limite);

    void marcarEnviadas(List<Long> tareas);

    NodoTarea findTarea(Long nodoId, Long nodoTareaId);

    void terminarTarea(Long nodoTareaId, String estado, String error);

    void fallarTarea(Long nodoTareaId, String error);
}

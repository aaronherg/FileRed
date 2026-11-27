package com.aarondevs.filered.Services.Archivos;

import com.aarondevs.filered.DAO.Archivos.ArchivoDataSet;
import com.aarondevs.filered.DAO.Archivos.NodoDataSet;
import com.aarondevs.filered.Models.Archivos.Archivo;
import com.aarondevs.filered.Models.Archivos.Nodo;
import com.aarondevs.filered.Parameters.Response.PaginationResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public class NodoArchivoServicio implements ArchivoService {
    private static final long MARGEN_LIBRE = 50L * 1024 * 1024;

    private final Nodo nodo;
    private final NodoDataSet datos;
    private final ArchivoDataSet archivosDatos;
    private final ArchivoServiceImpl nube;
    private final NodoService nodos;
    private final RespaldoService respaldos;

    public NodoArchivoServicio(Nodo nodo, NodoDataSet datos, ArchivoDataSet archivosDatos, ArchivoServiceImpl nube, NodoService nodos,
                               RespaldoService respaldos) {
        this.respaldos = respaldos;
        this.nodo = nodo;
        this.datos = datos;
        this.archivosDatos = archivosDatos;
        this.nube = nube;
        this.nodos = nodos;
    }

    private Long id() {
        return nodo.getNodoId();
    }

    private void tarea(String tipo, Long archivoId, String ruta, String destino) {
        datos.crearTarea(id(), tipo, archivoId, ruta, destino);
    }

    private void avisar() {
        nodos.avisar(id());
    }

    private void exigirPlan(Long clienteId) {
        if (!respaldos.permiteNodos(clienteId)) {
            throw new IllegalArgumentException("Tu plan no incluye nodos propios: " + nodo.getNombre() + " está en solo lectura. Usa la Nube o mejora tu plan");
        }
    }

    private void exigirEquipoConectado(Long clienteId) {
        if (!respaldos.permiteNube(clienteId) && !datos.conectado(id())) {
            throw new IllegalArgumentException("El equipo " + nodo.getNombre() + " está desconectado y tu plan no incluye la Nube: el archivo no se guardó. "
                    + "Conecta el equipo e inténtalo de nuevo");
        }
    }

    private void exigirAlmacen(String almacen) {
        if (!datos.existeDirectorio(id(), almacen, "")) throw new IllegalArgumentException("El almacén no existe");
    }

    private void exigirCarpeta(String almacen, String categoria) {
        if (!datos.existeDirectorio(id(), almacen, categoria)) throw new IllegalArgumentException("La carpeta no existe");
    }

    @Override
    public Map<String, Object> crearAlmacen(Long clienteId, String almacen) {
        nube.validarNombres(almacen);
        nube.vigente(clienteId);
        exigirPlan(clienteId);
        if (datos.existeDirectorio(id(), almacen, "")) throw new IllegalArgumentException("Ya existe un almacén con ese nombre");
        datos.crearDirectorio(id(), almacen, "");
        tarea("CREAR", null, almacen, null);
        avisar();
        respaldos.alCrearDirectorio(clienteId, id(), almacen);
        return datos("mensaje", "Almacén creado correctamente", "almacen", almacen);
    }

    @Override
    public Map<String, Object> renombrarAlmacen(Long clienteId, String almacen, String nombre, String actor) {
        nube.validarNombres(almacen, nombre);
        exigirPlan(clienteId);
        exigirAlmacen(almacen);
        if (almacen.equals(nombre)) return datos("mensaje", "Almacén renombrado correctamente", "almacen", nombre);
        if (datos.existeDirectorio(id(), nombre, "")) throw new IllegalArgumentException("Ya existe un almacén con ese nombre");
        datos.renombrarAlmacen(id(), almacen, nombre, actor);
        tarea("MOVER", null, almacen, nombre);
        avisar();
        respaldos.alMoverDirectorio(clienteId, id(), almacen, nombre);
        return datos("mensaje", "Almacén renombrado correctamente", "almacen", nombre);
    }

    @Override
    public Map<String, Object> eliminarAlmacen(Long clienteId, String almacen) {
        nube.validarNombres(almacen);
        exigirPlan(clienteId);
        exigirAlmacen(almacen);
        List<Archivo> lista = datos.findArchivosAlmacen(id(), almacen);
        lista.forEach(this::borrarTemporal);
        datos.deleteAlmacen(id(), almacen);
        tarea("ELIMINAR", null, almacen, null);
        avisar();
        respaldos.alEliminarDirectorio(clienteId, id(), almacen);
        if (!lista.isEmpty()) archivosDatos.sumarMetrica(clienteId, "archivosEliminados", lista.size());
        return datos("mensaje", "Almacén eliminado correctamente");
    }

    @Override
    public Map<String, Object> renombrarCarpeta(Long clienteId, String almacen, String categoria, String nombre, String actor) {
        nube.validarNombres(almacen, categoria, nombre);
        exigirPlan(clienteId);
        exigirCarpeta(almacen, categoria);
        if (categoria.equals(nombre)) return datos("mensaje", "Carpeta renombrada correctamente", "almacen", almacen, "categoria", nombre);
        if (datos.existeDirectorio(id(), almacen, nombre)) throw new IllegalArgumentException("Ya existe una carpeta con ese nombre");
        datos.renombrarCarpeta(id(), almacen, categoria, nombre, actor);
        tarea("MOVER", null, almacen + "/" + categoria, almacen + "/" + nombre);
        avisar();
        respaldos.alMoverDirectorio(clienteId, id(), almacen + "/" + categoria, almacen + "/" + nombre);
        return datos("mensaje", "Carpeta renombrada correctamente", "almacen", almacen, "categoria", nombre);
    }

    @Override
    public Map<String, Object> renombrarArchivo(Long clienteId, String almacen, String categoria, String identificador, String nombre, String actor) {
        nube.validarNombres(almacen, categoria, identificador);
        String limpio = nombre == null ? "" : nombre.trim();
        exigirPlan(clienteId);
        if (limpio.isEmpty()) throw new IllegalArgumentException("El nombre del archivo es obligatorio");
        if (limpio.length() > 200) throw new IllegalArgumentException("El nombre admite hasta 200 caracteres");
        if (limpio.contains("/") || limpio.contains("\\")) throw new IllegalArgumentException("El nombre no puede contener barras");
        if (datos.updateNombre(id(), almacen, categoria, identificador, limpio, actor) == 0) throw new IllegalArgumentException("Archivo no encontrado");
        return datos("mensaje", "Archivo renombrado correctamente", "nombreOriginal", limpio);
    }

    @Override
    public Map<String, Object> paginas(Long clienteId, String almacen, String categoria, String buscar, Integer page, Integer size) {
        nube.validarNombres(almacen, categoria);
        exigirCarpeta(almacen, categoria);
        PaginationResponse<Archivo> respuesta = datos.findAllPages(id(), almacen, categoria, buscar, page, size);
        List<Map<String, Object>> registros = new ArrayList<>();
        for (Archivo a : respuesta.getRecords()) registros.add(fila(clienteId, a));
        Map<String, Object> resultado = datos("page", respuesta.getPage(), "size", respuesta.getSize(), "totalRecords", respuesta.getTotalRecords(),
                "totalPages", respuesta.getTotalPages(), "hasPrevious", respuesta.getHasPrevious(), "hasNext", respuesta.getHasNext());
        resultado.put("records", registros);
        return resultado;
    }

    @Override
    public Map<String, Object> crearCarpeta(Long clienteId, String almacen, String categoria) {
        nube.validarNombres(almacen, categoria);
        nube.vigente(clienteId);
        exigirPlan(clienteId);
        if (datos.existeDirectorio(id(), almacen, categoria)) throw new IllegalArgumentException("La carpeta ya existe");
        if (!datos.existeDirectorio(id(), almacen, "")) datos.crearDirectorio(id(), almacen, "");
        datos.crearDirectorio(id(), almacen, categoria);
        tarea("CREAR", null, almacen + "/" + categoria, null);
        avisar();
        respaldos.alCrearDirectorio(clienteId, id(), almacen + "/" + categoria);
        return datos("mensaje", "Carpeta creada correctamente", "almacen", almacen, "categoria", categoria);
    }

    @Override
    public Map<String, Object> eliminarCarpeta(Long clienteId, String almacen, String categoria) {
        nube.validarNombres(almacen, categoria);
        exigirPlan(clienteId);
        exigirCarpeta(almacen, categoria);
        List<Archivo> lista = datos.findArchivos(id(), almacen, categoria);
        lista.forEach(this::borrarTemporal);
        datos.deleteCarpeta(id(), almacen, categoria);
        tarea("ELIMINAR", null, almacen + "/" + categoria, null);
        avisar();
        respaldos.alEliminarDirectorio(clienteId, id(), almacen + "/" + categoria);
        if (!lista.isEmpty()) archivosDatos.sumarMetrica(clienteId, "archivosEliminados", lista.size());
        return datos("mensaje", "Carpeta y archivos eliminados correctamente");
    }

    @Override
    public List<Map<String, Object>> almacenes(Long clienteId) {
        Map<String, Map<String, Object>> resumen = new LinkedHashMap<>();
        for (Map<String, Object> fila : datos.resumenAlmacenes(id())) resumen.put(String.valueOf(fila.get("almacen")), fila);
        Map<String, Long> carpetas = new LinkedHashMap<>();
        for (Map<String, Object> fila : datos.conteoCarpetas(id())) carpetas.put(String.valueOf(fila.get("almacen")), numero(fila.get("carpetas")));
        List<Map<String, Object>> lista = new ArrayList<>();
        for (String nombre : datos.almacenes(id())) {
            Map<String, Object> fila = resumen.getOrDefault(nombre, Map.of());
            lista.add(datos("almacen", nombre, "carpetas", carpetas.getOrDefault(nombre, 0L), "archivos", numero(fila.get("archivos")),
                    "bytes", numero(fila.get("bytes")), "ultimaModificacion", fila.get("ultimaModificacion")));
        }
        return lista;
    }

    @Override
    public List<Map<String, Object>> carpetas(Long clienteId, String almacen) {
        nube.validarNombres(almacen);
        exigirAlmacen(almacen);
        Map<String, Map<String, Object>> resumen = new LinkedHashMap<>();
        for (Map<String, Object> fila : datos.resumenCarpetas(id(), almacen)) resumen.put(String.valueOf(fila.get("categoria")), fila);
        List<Map<String, Object>> lista = new ArrayList<>();
        for (String nombre : datos.carpetas(id(), almacen)) {
            Map<String, Object> fila = resumen.getOrDefault(nombre, Map.of());
            lista.add(datos("categoria", nombre, "almacen", almacen, "archivos", numero(fila.get("archivos")),
                    "bytes", numero(fila.get("bytes")), "ultimaModificacion", fila.get("ultimaModificacion")));
        }
        return lista;
    }

    @Override
    public Map<String, Object> subir(Long clienteId, String almacen, String categoria, MultipartFile archivo, Long apiKeyId, String actor,
                                     boolean incluirBase64) {
        nube.validarNombres(almacen, categoria);
        String extension = nube.extension(archivo == null ? null : archivo.getOriginalFilename());
        nube.validarArchivoNodo(clienteId, archivo, extension);
        exigirPlan(clienteId);
        if (!datos.existeDirectorio(id(), almacen, categoria)) throw new IllegalArgumentException("La carpeta no existe, créela primero con addDirectory");
        exigirEquipoConectado(clienteId);
        exigirEspacio(archivo.getSize());
        String identificador = nube.nuevoIdentificador();
        Path destino = nodos.temporal(nodo, identificador, extension);
        guardar(archivo, destino);
        String hash = NodoServiceImpl.hashArchivo(destino);
        Long archivoId = datos.createArchivo(clienteId, id(), almacen, categoria, identificador, extension, nube.nombreOriginal(archivo),
                archivo.getSize(), hash, apiKeyId, actor);
        tarea("GUARDAR", archivoId, null, null);
        archivosDatos.sumarMetrica(clienteId, "archivosSubidos", 1);
        archivosDatos.sumarMetrica(clienteId, "bytesSubidos", archivo.getSize());
        Map<String, Object> resultado = datos("identificador", identificador, "extension", extension, "size", archivo.getSize(),
                "url", nube.url(clienteId, almacen, categoria, identificador, extension));
        if (incluirBase64) resultado.put("base64", nube.base64SiEsImagen(destino, extension));
        avisar();
        respaldos.alSubir(clienteId, id(), archivoId);
        return resultado;
    }

    @Override
    public Map<String, Object> reemplazar(Long clienteId, String almacen, String categoria, String identificador, String extensionActual,
                                          MultipartFile archivo, String actor, boolean incluirBase64) {
        String actual = nube.extensionLimpia(extensionActual);
        nube.validarNombres(almacen, categoria, identificador, actual);
        exigirCarpeta(almacen, categoria);
        Archivo registro = datos.findArchivo(id(), almacen, categoria, identificador);
        if (registro == null || !actual.equals(registro.getExtension())) throw new IllegalArgumentException("Archivo original no encontrado");
        String nueva = nube.extension(archivo == null ? null : archivo.getOriginalFilename());
        nube.validarArchivoNodo(clienteId, archivo, nueva);
        exigirPlan(clienteId);
        exigirEquipoConectado(clienteId);
        exigirEspacio(archivo.getSize());
        Path rutaNueva = nodos.temporal(nodo, identificador, nueva);
        guardar(archivo, rutaNueva);
        String hash = NodoServiceImpl.hashArchivo(rutaNueva);
        if (!actual.equals(nueva)) {
            borrarTemporal(registro);
            tarea("ELIMINAR", null, NodoServiceImpl.ruta(registro), null);
        }
        datos.updateArchivo(registro.getArchivoId(), nueva, nube.nombreOriginal(archivo), archivo.getSize(), hash, actor);
        tarea("GUARDAR", registro.getArchivoId(), null, null);
        archivosDatos.sumarMetrica(clienteId, "bytesSubidos", archivo.getSize());
        Map<String, Object> resultado = datos("mensaje", "Archivo actualizado correctamente", "identificador", identificador,
                "extension", nueva, "size", archivo.getSize(), "url", nube.url(clienteId, almacen, categoria, identificador, nueva));
        if (incluirBase64) resultado.put("base64", nube.base64SiEsImagen(rutaNueva, nueva));
        avisar();
        respaldos.alReemplazar(clienteId, id(), registro, nueva);
        return resultado;
    }

    @Override
    public Map<String, Object> eliminar(Long clienteId, String almacen, String categoria, String identificador, String extension) {
        String ext = nube.extensionLimpia(extension);
        nube.validarNombres(almacen, categoria, identificador, ext);
        exigirPlan(clienteId);
        Archivo registro = datos.findArchivo(id(), almacen, categoria, identificador);
        if (registro == null || !ext.equals(registro.getExtension())) throw new IllegalArgumentException("Archivo no encontrado");
        borrarTemporal(registro);
        datos.deleteArchivo(registro.getArchivoId());
        tarea("ELIMINAR", null, NodoServiceImpl.ruta(registro), null);
        archivosDatos.sumarMetrica(clienteId, "archivosEliminados", 1);
        avisar();
        respaldos.alEliminarArchivo(clienteId, id(), registro);
        return datos("mensaje", "Archivo eliminado correctamente");
    }

    @Override
    public List<Map<String, Object>> archivos(Long clienteId, String almacen, String categoria) {
        nube.validarNombres(almacen, categoria);
        exigirCarpeta(almacen, categoria);
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Archivo a : datos.findArchivos(id(), almacen, categoria)) lista.add(fila(clienteId, a));
        return lista;
    }

    @Override
    public Map<String, Object> archivo(Long clienteId, String almacen, String categoria, String identificador, String extension) {
        String ext = nube.extensionLimpia(extension);
        nube.validarNombres(almacen, categoria, identificador, ext);
        Archivo registro = datos.findArchivo(id(), almacen, categoria, identificador);
        if (registro == null || !ext.equals(registro.getExtension())) throw new IllegalArgumentException("Archivo no encontrado");
        return datos("identificador", identificador, "extension", ext, "size", registro.getTamano(),
                "url", nube.url(clienteId, almacen, categoria, identificador, ext));
    }

    @Override
    public void registrarSolicitudApi(Long clienteId) {
        nube.registrarSolicitudApi(clienteId);
    }

    @Override
    public Map<String, Object> archivoPorId(Long clienteId, String almacen, String categoria, String identificador) {
        nube.validarNombres(almacen, categoria, identificador);
        Archivo registro = datos.findArchivo(id(), almacen, categoria, identificador);
        if (registro == null) throw new IllegalArgumentException("Archivo no encontrado");
        Map<String, Object> resultado = archivo(clienteId, almacen, categoria, identificador, registro.getExtension());
        resultado.put("almacen", almacen);
        resultado.put("categoria", categoria);
        resultado.put("nombreOriginal", registro.getNombreOriginal());
        resultado.put("mime", mime(registro.getExtension()));
        resultado.put("fechaCreacion", registro.getFechaCreacion());
        resultado.put("fechaModificacion", registro.getFechaModificacion());
        resultado.put("estadoNodo", registro.getEstadoNodo());
        return resultado;
    }

    @Override
    public Path publico(String clienteUuid, String almacen, String categoria, String archivo) {
        return null;
    }

    @Override
    public String mime(String extension) {
        return nube.mime(extension);
    }

    private Map<String, Object> fila(Long clienteId, Archivo a) {
        Map<String, Object> fila = datos("identificador", a.getIdentificador(), "extension", a.getExtension(), "size", a.getTamano(),
                "url", nube.url(clienteId, a.getAlmacen(), a.getCategoria(), a.getIdentificador(), a.getExtension()));
        fila.put("nombreOriginal", a.getNombreOriginal());
        fila.put("mime", mime(a.getExtension()));
        fila.put("fechaCreacion", a.getFechaCreacion());
        fila.put("fechaModificacion", a.getFechaModificacion());
        fila.put("estadoNodo", a.getEstadoNodo());
        return fila;
    }

    private void exigirEspacio(long tamano) {
        Long libre = nodo.getEspacioLibre();
        if (libre != null && tamano + MARGEN_LIBRE > libre) {
            throw new IllegalArgumentException("El equipo " + nodo.getNombre() + " no tiene espacio suficiente para este archivo");
        }
    }

    private void guardar(MultipartFile archivo, Path destino) {
        try (InputStream entrada = archivo.getInputStream()) {
            Files.createDirectories(destino.getParent());
            Files.copy(entrada, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException error) {
            throw new IllegalStateException("No se pudo recibir el archivo");
        }
    }

    private void borrarTemporal(Archivo archivo) {
        try {
            Files.deleteIfExists(nodos.temporal(nodo, archivo.getIdentificador(), archivo.getExtension()));
        } catch (IOException ignorado) {
            return;
        }
    }

    private long numero(Object valor) {
        return valor instanceof Number n ? n.longValue() : 0;
    }

    private static Map<String, Object> datos(Object... pares) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        for (int i = 0; i + 1 < pares.length; i += 2) mapa.put(String.valueOf(pares[i]), pares[i + 1]);
        return mapa;
    }
}

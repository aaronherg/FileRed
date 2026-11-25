package com.aarondevs.filered.Services.Archivos;

import com.aarondevs.filered.DAO.Archivos.ArchivoDataSet;
import com.aarondevs.filered.Models.Archivos.Archivo;
import com.aarondevs.filered.Models.Suscripciones.DetalleSuscripcion;
import com.aarondevs.filered.Parameters.Response.PaginationResponse;
import com.aarondevs.filered.Services.Suscripciones.SuscripcionService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

@Service
public class ArchivoServiceImpl implements ArchivoService {
    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "ico", "tiff",
            "mp4", "avi", "mov", "wmv", "flv", "mkv", "webm", "m4v", "3gp",
            "mp3", "wav", "ogg", "aac", "flac", "m4a",
            "pdf", "txt", "rtf", "odt", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "ods", "odp",
            "zip", "rar", "7z", "tar", "gz",
            "json", "xml", "csv", "md", "log");

    private static final Map<String, String> MIME = Map.ofEntries(
            Map.entry("jpg", "image/jpeg"), Map.entry("jpeg", "image/jpeg"), Map.entry("png", "image/png"),
            Map.entry("gif", "image/gif"), Map.entry("webp", "image/webp"), Map.entry("bmp", "image/bmp"),
            Map.entry("svg", "image/svg+xml"), Map.entry("ico", "image/x-icon"), Map.entry("tiff", "image/tiff"),
            Map.entry("mp4", "video/mp4"), Map.entry("avi", "video/x-msvideo"), Map.entry("mov", "video/quicktime"),
            Map.entry("wmv", "video/x-ms-wmv"), Map.entry("flv", "video/x-flv"), Map.entry("mkv", "video/x-matroska"),
            Map.entry("webm", "video/webm"), Map.entry("m4v", "video/x-m4v"), Map.entry("3gp", "video/3gpp"),
            Map.entry("mp3", "audio/mpeg"), Map.entry("wav", "audio/wav"), Map.entry("ogg", "audio/ogg"),
            Map.entry("aac", "audio/aac"), Map.entry("flac", "audio/flac"), Map.entry("m4a", "audio/mp4"),
            Map.entry("pdf", "application/pdf"), Map.entry("txt", "text/plain"), Map.entry("rtf", "application/rtf"),
            Map.entry("odt", "application/vnd.oasis.opendocument.text"), Map.entry("doc", "application/msword"),
            Map.entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            Map.entry("xls", "application/vnd.ms-excel"),
            Map.entry("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
            Map.entry("ppt", "application/vnd.ms-powerpoint"),
            Map.entry("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
            Map.entry("ods", "application/vnd.oasis.opendocument.spreadsheet"),
            Map.entry("odp", "application/vnd.oasis.opendocument.presentation"),
            Map.entry("zip", "application/zip"), Map.entry("rar", "application/vnd.rar"),
            Map.entry("7z", "application/x-7z-compressed"), Map.entry("tar", "application/x-tar"),
            Map.entry("gz", "application/gzip"), Map.entry("json", "application/json"),
            Map.entry("xml", "application/xml"), Map.entry("csv", "text/csv"), Map.entry("md", "text/markdown"),
            Map.entry("log", "text/plain"));

    private static final Pattern NOMBRE_VALIDO = Pattern.compile("^[\\p{L}\\p{N}][\\p{L}\\p{N} ._-]{0,127}$");
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final long BYTES_POR_MB = 1024L * 1024;

    private final ArchivoDataSet datos;
    private final SuscripcionService suscripciones;
    private final Path base;
    private final long tamanoMaximo;
    private final String urlBase;

    public ArchivoServiceImpl(ArchivoDataSet datos, SuscripcionService suscripciones,
                              @Value("${app.filered.storage-path}") String rutaAlmacenamiento,
                              @Value("${app.filered.max-file-size}") long tamanoMaximo,
                              @Value("${app.filered.url-base}") String urlBase) throws IOException {
        this.datos = datos;
        this.suscripciones = suscripciones;
        this.base = Paths.get(rutaAlmacenamiento).toAbsolutePath().normalize();
        this.tamanoMaximo = tamanoMaximo;
        this.urlBase = urlBase == null || urlBase.isBlank() ? "/" : (urlBase.endsWith("/") ? urlBase : urlBase + "/");
        Files.createDirectories(this.base);
    }

    @Override
    public Map<String, Object> crearAlmacen(Long clienteId, String almacen) {
        validarNombres(almacen);
        vigente(clienteId);
        Path ruta = raiz(clienteId).resolve(almacen);
        if (Files.exists(ruta)) throw new IllegalArgumentException("Ya existe un almacén con ese nombre");
        try {
            Files.createDirectories(ruta);
        } catch (IOException error) {
            throw new IllegalStateException("No se pudo crear el almacén");
        }
        return datos("mensaje", "Almacén creado correctamente", "almacen", almacen);
    }

    @Override
    public Map<String, Object> renombrarAlmacen(Long clienteId, String almacen, String nombre, String actor) {
        validarNombres(almacen, nombre);
        Path origen = raiz(clienteId).resolve(almacen);
        Path destino = raiz(clienteId).resolve(nombre);
        renombrarDirectorio(origen, destino, "El almacén no existe", "Ya existe un almacén con ese nombre");
        datos.renombrarAlmacen(clienteId, almacen, nombre, actor);
        return datos("mensaje", "Almacén renombrado correctamente", "almacen", nombre);
    }

    @Override
    public Map<String, Object> eliminarAlmacen(Long clienteId, String almacen) {
        validarNombres(almacen);
        Path ruta = raiz(clienteId).resolve(almacen);
        if (!Files.exists(ruta)) throw new IllegalArgumentException("El almacén no existe");
        long eliminados;
        try (Stream<Path> recorrido = Files.walk(ruta)) {
            List<Path> elementos = recorrido.sorted(Comparator.reverseOrder()).toList();
            eliminados = elementos.stream().filter(Files::isRegularFile).count();
            for (Path elemento : elementos) Files.deleteIfExists(elemento);
        } catch (IOException error) {
            throw new IllegalStateException("No se pudo eliminar el almacén");
        }
        datos.deleteAlmacen(clienteId, almacen);
        if (eliminados > 0) datos.sumarMetrica(clienteId, "archivosEliminados", eliminados);
        return datos("mensaje", "Almacén eliminado correctamente");
    }

    @Override
    public Map<String, Object> renombrarCarpeta(Long clienteId, String almacen, String categoria, String nombre, String actor) {
        validarNombres(almacen, categoria, nombre);
        renombrarDirectorio(carpeta(clienteId, almacen, categoria), carpeta(clienteId, almacen, nombre),
                "La carpeta no existe", "Ya existe una carpeta con ese nombre");
        datos.renombrarCategoria(clienteId, almacen, categoria, nombre, actor);
        return datos("mensaje", "Carpeta renombrada correctamente", "almacen", almacen, "categoria", nombre);
    }

    @Override
    public Map<String, Object> renombrarArchivo(Long clienteId, String almacen, String categoria, String identificador, String nombre, String actor) {
        validarNombres(almacen, categoria, identificador);
        String limpio = nombre == null ? "" : nombre.trim();
        if (limpio.isEmpty()) throw new IllegalArgumentException("El nombre del archivo es obligatorio");
        if (limpio.length() > 200) throw new IllegalArgumentException("El nombre admite hasta 200 caracteres");
        if (limpio.contains("/") || limpio.contains("\\")) throw new IllegalArgumentException("El nombre no puede contener barras");
        if (datos.updateNombre(clienteId, almacen, categoria, identificador, limpio, actor) == 0) {
            throw new IllegalArgumentException("Archivo no encontrado");
        }
        return datos("mensaje", "Archivo renombrado correctamente", "nombreOriginal", limpio);
    }

    @Override
    public Map<String, Object> paginas(Long clienteId, String almacen, String categoria, String buscar, Integer page, Integer size) {
        validarNombres(almacen, categoria);
        if (!Files.exists(carpeta(clienteId, almacen, categoria))) throw new IllegalArgumentException("La carpeta no existe");
        PaginationResponse<Archivo> respuesta = datos.findAllPages(clienteId, almacen, categoria, buscar, page, size);
        List<Map<String, Object>> registros = new ArrayList<>();
        for (Archivo a : respuesta.getRecords()) {
            Map<String, Object> fila = datos("identificador", a.getIdentificador(), "extension", a.getExtension(), "size", a.getTamano(),
                    "url", url(clienteId, almacen, categoria, a.getIdentificador(), a.getExtension()));
            fila.put("nombreOriginal", a.getNombreOriginal());
            fila.put("mime", mime(a.getExtension()));
            fila.put("fechaCreacion", a.getFechaCreacion());
            fila.put("fechaModificacion", a.getFechaModificacion());
            registros.add(fila);
        }
        Map<String, Object> resultado = datos("page", respuesta.getPage(), "size", respuesta.getSize(), "totalRecords", respuesta.getTotalRecords(),
                "totalPages", respuesta.getTotalPages(), "hasPrevious", respuesta.getHasPrevious(), "hasNext", respuesta.getHasNext());
        resultado.put("records", registros);
        return resultado;
    }

    @Override
    public Map<String, Object> crearCarpeta(Long clienteId, String almacen, String categoria) {
        validarNombres(almacen, categoria);
        vigente(clienteId);
        Path ruta = carpeta(clienteId, almacen, categoria);
        if (Files.exists(ruta)) throw new IllegalArgumentException("La carpeta ya existe");
        try {
            Files.createDirectories(ruta);
        } catch (IOException error) {
            throw new IllegalStateException("No se pudo crear la carpeta");
        }
        return datos("mensaje", "Carpeta creada correctamente", "almacen", almacen, "categoria", categoria);
    }

    @Override
    public Map<String, Object> eliminarCarpeta(Long clienteId, String almacen, String categoria) {
        validarNombres(almacen, categoria);
        Path ruta = carpeta(clienteId, almacen, categoria);
        if (!Files.exists(ruta)) throw new IllegalArgumentException("La carpeta no existe");
        long eliminados;
        try (Stream<Path> contenido = Files.list(ruta)) {
            List<Path> archivos = contenido.filter(Files::isRegularFile).toList();
            for (Path archivo : archivos) Files.deleteIfExists(archivo);
            Files.delete(ruta);
            eliminados = archivos.size();
        } catch (IOException error) {
            throw new IllegalStateException("No se pudo eliminar la carpeta");
        }
        datos.deleteCategoria(clienteId, almacen, categoria);
        if (eliminados > 0) datos.sumarMetrica(clienteId, "archivosEliminados", eliminados);
        return datos("mensaje", "Carpeta y archivos eliminados correctamente");
    }

    @Override
    public List<Map<String, Object>> almacenes(Long clienteId) {
        Path raiz = raiz(clienteId);
        if (!Files.exists(raiz)) return List.of();
        try (Stream<Path> contenido = Files.list(raiz)) {
            Map<String, Map<String, Object>> resumen = new LinkedHashMap<>();
            for (Map<String, Object> fila : datos.resumenAlmacenes(clienteId)) resumen.put(String.valueOf(fila.get("almacen")), fila);
            List<Map<String, Object>> almacenes = new ArrayList<>();
            for (Path almacen : contenido.filter(Files::isDirectory).sorted().toList()) {
                String nombre = almacen.getFileName().toString();
                Map<String, Object> fila = resumen.getOrDefault(nombre, Map.of());
                almacenes.add(datos("almacen", nombre, "carpetas", contarCarpetas(almacen), "archivos", numero(fila.get("archivos")),
                        "bytes", numero(fila.get("bytes")), "ultimaModificacion", fila.get("ultimaModificacion")));
            }
            return almacenes;
        } catch (IOException error) {
            throw new IllegalStateException("No se pudieron consultar los almacenes");
        }
    }

    @Override
    public List<Map<String, Object>> carpetas(Long clienteId, String almacen) {
        validarNombres(almacen);
        Path ruta = raiz(clienteId).resolve(almacen);
        if (!Files.exists(ruta)) throw new IllegalArgumentException("El almacén no existe");
        try (Stream<Path> contenido = Files.list(ruta)) {
            Map<String, Map<String, Object>> resumen = new LinkedHashMap<>();
            for (Map<String, Object> fila : datos.resumenCarpetas(clienteId, almacen)) resumen.put(String.valueOf(fila.get("categoria")), fila);
            List<Map<String, Object>> carpetas = new ArrayList<>();
            for (Path carpeta : contenido.filter(Files::isDirectory).sorted().toList()) {
                String nombre = carpeta.getFileName().toString();
                Map<String, Object> fila = resumen.getOrDefault(nombre, Map.of());
                carpetas.add(datos("categoria", nombre, "almacen", almacen, "archivos", numero(fila.get("archivos")),
                        "bytes", numero(fila.get("bytes")), "ultimaModificacion", fila.get("ultimaModificacion")));
            }
            return carpetas;
        } catch (IOException error) {
            throw new IllegalStateException("No se pudieron consultar las carpetas");
        }
    }

    @Override
    public Map<String, Object> subir(Long clienteId, String almacen, String categoria, MultipartFile archivo, Long apiKeyId, String actor,
                                     boolean incluirBase64) {
        validarNombres(almacen, categoria);
        String extension = extension(archivo == null ? null : archivo.getOriginalFilename());
        validarArchivo(clienteId, archivo, extension, 0);
        Path ruta = carpeta(clienteId, almacen, categoria);
        if (!Files.exists(ruta)) throw new IllegalArgumentException("La carpeta no existe, créela primero con addDirectory");
        String identificador = nuevoIdentificador();
        Path destino = ruta.resolve(identificador + "." + extension);
        guardar(archivo, destino);
        datos.create(clienteId, almacen, categoria, identificador, extension, nombreOriginal(archivo), archivo.getSize(), apiKeyId, actor);
        datos.sumarMetrica(clienteId, "archivosSubidos", 1);
        datos.sumarMetrica(clienteId, "bytesSubidos", archivo.getSize());
        Map<String, Object> resultado = datos("identificador", identificador, "extension", extension, "size", archivo.getSize(),
                "url", url(clienteId, almacen, categoria, identificador, extension));
        if (incluirBase64) resultado.put("base64", base64SiEsImagen(destino, extension));
        return resultado;
    }

    @Override
    public Map<String, Object> reemplazar(Long clienteId, String almacen, String categoria, String identificador, String extensionActual,
                                          MultipartFile archivo, String actor, boolean incluirBase64) {
        String actual = extensionLimpia(extensionActual);
        validarNombres(almacen, categoria, identificador, actual);
        Path ruta = carpeta(clienteId, almacen, categoria);
        Path rutaActual = ruta.resolve(identificador + "." + actual);
        if (!Files.exists(ruta)) throw new IllegalArgumentException("La carpeta no existe");
        if (!Files.exists(rutaActual)) throw new IllegalArgumentException("Archivo original no encontrado");
        String nueva = extension(archivo == null ? null : archivo.getOriginalFilename());
        validarArchivo(clienteId, archivo, nueva, tamano(rutaActual));
        Path rutaNueva = ruta.resolve(identificador + "." + nueva);
        guardar(archivo, rutaNueva);
        if (!rutaActual.equals(rutaNueva)) {
            try {
                Files.deleteIfExists(rutaActual);
            } catch (IOException error) {
                try {
                    Files.deleteIfExists(rutaNueva);
                } catch (IOException ignorado) {
                    throw new IllegalStateException("No se pudo retirar la versión anterior del archivo");
                }
                throw new IllegalStateException("No se pudo retirar la versión anterior del archivo");
            }
        }
        Archivo registro = datos.findByRuta(clienteId, almacen, categoria, identificador);
        if (registro == null) {
            datos.create(clienteId, almacen, categoria, identificador, nueva, nombreOriginal(archivo), archivo.getSize(), null, actor);
        } else {
            datos.update(registro.getArchivoId(), nueva, nombreOriginal(archivo), archivo.getSize(), actor);
        }
        datos.sumarMetrica(clienteId, "bytesSubidos", archivo.getSize());
        Map<String, Object> resultado = datos("mensaje", "Archivo actualizado correctamente", "identificador", identificador,
                "extension", nueva, "size", archivo.getSize(), "url", url(clienteId, almacen, categoria, identificador, nueva));
        if (incluirBase64) resultado.put("base64", base64SiEsImagen(rutaNueva, nueva));
        return resultado;
    }

    @Override
    public Map<String, Object> eliminar(Long clienteId, String almacen, String categoria, String identificador, String extension) {
        String ext = extensionLimpia(extension);
        validarNombres(almacen, categoria, identificador, ext);
        Path ruta = carpeta(clienteId, almacen, categoria).resolve(identificador + "." + ext);
        if (!Files.exists(ruta)) throw new IllegalArgumentException("Archivo no encontrado");
        try {
            Files.delete(ruta);
        } catch (IOException error) {
            throw new IllegalStateException("No se pudo eliminar el archivo");
        }
        datos.delete(clienteId, almacen, categoria, identificador);
        datos.sumarMetrica(clienteId, "archivosEliminados", 1);
        return datos("mensaje", "Archivo eliminado correctamente");
    }

    @Override
    public List<Map<String, Object>> archivos(Long clienteId, String almacen, String categoria) {
        validarNombres(almacen, categoria);
        Path ruta = carpeta(clienteId, almacen, categoria);
        if (!Files.exists(ruta)) throw new IllegalArgumentException("La carpeta no existe");
        Map<String, Archivo> registros = new LinkedHashMap<>();
        for (Archivo archivo : datos.findAll(clienteId, almacen, categoria)) registros.put(archivo.getIdentificador(), archivo);
        try (Stream<Path> contenido = Files.list(ruta)) {
            List<Map<String, Object>> archivos = new ArrayList<>();
            for (Path archivo : contenido.filter(Files::isRegularFile).sorted().toList()) {
                String nombre = archivo.getFileName().toString();
                String extension = extension(nombre);
                String identificador = StringUtils.stripFilenameExtension(nombre);
                Archivo registro = registros.get(identificador);
                Map<String, Object> fila = datos("identificador", identificador, "extension", extension, "size", tamano(archivo),
                        "url", url(clienteId, almacen, categoria, identificador, extension));
                fila.put("nombreOriginal", registro == null ? null : registro.getNombreOriginal());
                fila.put("fechaCreacion", registro == null ? null : registro.getFechaCreacion());
                archivos.add(fila);
            }
            return archivos;
        } catch (IOException error) {
            throw new IllegalStateException("No se pudieron consultar los archivos");
        }
    }

    @Override
    public Map<String, Object> archivo(Long clienteId, String almacen, String categoria, String identificador, String extension) {
        String ext = extensionLimpia(extension);
        validarNombres(almacen, categoria, identificador, ext);
        Path ruta = carpeta(clienteId, almacen, categoria).resolve(identificador + "." + ext);
        if (!Files.exists(ruta)) throw new IllegalArgumentException("Archivo no encontrado");
        return datos("identificador", identificador, "extension", ext, "size", tamano(ruta),
                "url", url(clienteId, almacen, categoria, identificador, ext));
    }

    @Override
    public Map<String, Object> archivoPorId(Long clienteId, String almacen, String categoria, String identificador) {
        validarNombres(almacen, categoria, identificador);
        Archivo registro = datos.findByRuta(clienteId, almacen, categoria, identificador);
        if (registro == null) throw new IllegalArgumentException("Archivo no encontrado");
        Map<String, Object> resultado = archivo(clienteId, almacen, categoria, identificador, registro.getExtension());
        resultado.put("almacen", almacen);
        resultado.put("categoria", categoria);
        resultado.put("nombreOriginal", registro.getNombreOriginal());
        resultado.put("mime", mime(registro.getExtension()));
        resultado.put("fechaCreacion", registro.getFechaCreacion());
        resultado.put("fechaModificacion", registro.getFechaModificacion());
        return resultado;
    }

    @Override
    public void registrarSolicitudApi(Long clienteId) {
        datos.sumarMetrica(clienteId, "solicitudesApi", 1);
    }

    @Override
    public Path publico(String clienteUuid, String almacen, String categoria, String archivo) {
        if (clienteUuid == null || !clienteUuid.matches("[0-9a-fA-F-]{36}")) return null;
        if (!nombresValidos(almacen, categoria, archivo)) return null;
        Long clienteId = datos.clienteActivoPorUuid(clienteUuid);
        if (clienteId == null) return null;
        Path ruta = base.resolve(clienteUuid).resolve(almacen).resolve(categoria).resolve(archivo).normalize();
        if (!ruta.startsWith(base) || !Files.isRegularFile(ruta)) return null;
        datos.sumarMetrica(clienteId, "descargas", 1);
        datos.sumarMetrica(clienteId, "bytesDescargados", tamano(ruta));
        return ruta;
    }

    @Override
    public String mime(String extension) {
        return MIME.getOrDefault(extensionLimpia(extension), "application/octet-stream");
    }

    private DetalleSuscripcion vigente(Long clienteId) {
        DetalleSuscripcion vigente = suscripciones.vigente(clienteId);
        if (vigente == null) {
            throw new IllegalArgumentException("No tiene un plan vigente. Contrate o renueve un plan para usar el almacenamiento");
        }
        return vigente;
    }

    private void validarArchivo(Long clienteId, MultipartFile archivo, String extension, long tamanoLiberado) {
        if (archivo == null || archivo.isEmpty() || archivo.getSize() == 0) {
            throw new IllegalArgumentException("El archivo está vacío o no fue enviado correctamente");
        }
        if (extension.isEmpty() || !EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new IllegalArgumentException("Extensión no permitida: " + extension);
        }
        DetalleSuscripcion plan = vigente(clienteId);
        long maximoPlan = plan.getTamanoMaximoArchivoMb() == null ? tamanoMaximo : plan.getTamanoMaximoArchivoMb() * BYTES_POR_MB;
        long maximo = Math.min(maximoPlan, tamanoMaximo);
        if (archivo.getSize() > maximo) {
            throw new IllegalArgumentException("El archivo supera el tamaño máximo de " + (maximo / BYTES_POR_MB) + "MB "
                    + (plan.getTamanoMaximoArchivoMb() == null ? "permitido por archivo" : "de su plan"));
        }
        if (plan.getEspacioGb() != null && !Boolean.TRUE.equals(plan.getPermitirAdicionales())) {
            long usado = suscripciones.espacioUsado(clienteId) - tamanoLiberado;
            if (usado + archivo.getSize() > SuscripcionService.limiteBytes(plan.getEspacioGb())) {
                throw new IllegalArgumentException("Espacio insuficiente: su plan " + plan.getPlanNombre() + " incluye "
                        + plan.getEspacioGb().stripTrailingZeros().toPlainString() + " GB");
            }
        }
    }

    private void guardar(MultipartFile archivo, Path destino) {
        try (InputStream entrada = archivo.getInputStream()) {
            Files.copy(entrada, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException error) {
            throw new IllegalStateException("No se pudo guardar el archivo");
        }
    }

    private void validarNombres(String... nombres) {
        for (String nombre : nombres) {
            if (!nombresValidos(nombre)) throw new IllegalArgumentException("Nombre no válido: " + (nombre == null ? "" : nombre));
        }
    }

    private boolean nombresValidos(String... nombres) {
        for (String nombre : nombres) {
            if (nombre == null || nombre.contains("..") || !NOMBRE_VALIDO.matcher(nombre).matches()) return false;
        }
        return true;
    }

    private Path raiz(Long clienteId) {
        String uuid = datos.clienteUuid(clienteId);
        if (uuid == null) throw new IllegalArgumentException("Cliente no encontrado");
        return base.resolve(uuid);
    }

    private Path carpeta(Long clienteId, String almacen, String categoria) {
        return raiz(clienteId).resolve(almacen).resolve(categoria);
    }

    private void renombrarDirectorio(Path origen, Path destino, String noExiste, String yaExiste) {
        if (!Files.isDirectory(origen)) throw new IllegalArgumentException(noExiste);
        if (origen.equals(destino)) return;
        if (Files.exists(destino)) throw new IllegalArgumentException(yaExiste);
        try {
            Files.move(origen, destino);
        } catch (IOException error) {
            throw new IllegalStateException("No se pudo renombrar");
        }
    }

    private long contarCarpetas(Path almacen) {
        try (Stream<Path> contenido = Files.list(almacen)) {
            return contenido.filter(Files::isDirectory).count();
        } catch (IOException error) {
            return 0;
        }
    }

    private long numero(Object valor) {
        return valor instanceof Number n ? n.longValue() : 0;
    }

    private void eliminarSiVacio(Path ruta) {
        try (Stream<Path> contenido = Files.list(ruta)) {
            if (contenido.findAny().isEmpty()) Files.delete(ruta);
        } catch (IOException ignorado) {
            return;
        }
    }

    private String url(Long clienteId, String almacen, String categoria, String identificador, String extension) {
        return urlBase + "f/" + segmento(datos.clienteUuid(clienteId)) + "/" + segmento(almacen) + "/" + segmento(categoria) + "/"
                + segmento(identificador + "." + extension);
    }

    private String segmento(String valor) {
        return UriUtils.encodePathSegment(valor, StandardCharsets.UTF_8);
    }

    private String nombreOriginal(MultipartFile archivo) {
        String nombre = StringUtils.getFilename(archivo.getOriginalFilename());
        if (nombre == null || nombre.isBlank()) return null;
        return nombre.length() > 200 ? nombre.substring(0, 200) : nombre;
    }

    private String extension(String nombre) {
        String extension = StringUtils.getFilenameExtension(nombre == null ? "" : nombre);
        return extension == null ? "" : extension.toLowerCase(Locale.ROOT);
    }

    private String extensionLimpia(String extension) {
        if (extension == null) return "";
        String limpia = extension.trim();
        while (limpia.startsWith(".")) limpia = limpia.substring(1);
        return limpia.toLowerCase(Locale.ROOT);
    }

    private String nuevoIdentificador() {
        byte[] bytes = new byte[16];
        ALEATORIO.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private long tamano(Path ruta) {
        try {
            return Files.size(ruta);
        } catch (IOException error) {
            return 0;
        }
    }

    private String base64SiEsImagen(Path ruta, String extension) {
        String tipo = mime(extension);
        if (!tipo.startsWith("image/")) return null;
        try {
            return "data:" + tipo + ";base64," + Base64.getEncoder().encodeToString(Files.readAllBytes(ruta));
        } catch (IOException error) {
            return null;
        }
    }

    private static Map<String, Object> datos(Object... pares) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        for (int i = 0; i + 1 < pares.length; i += 2) mapa.put(String.valueOf(pares[i]), pares[i + 1]);
        return mapa;
    }
}

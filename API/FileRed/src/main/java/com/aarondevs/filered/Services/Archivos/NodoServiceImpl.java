package com.aarondevs.filered.Services.Archivos;

import com.aarondevs.filered.DAO.Archivos.ArchivoDataSet;
import com.aarondevs.filered.DAO.Archivos.NodoDataSet;
import com.aarondevs.filered.Models.Archivos.Archivo;
import com.aarondevs.filered.Models.Archivos.Nodo;
import com.aarondevs.filered.Models.Archivos.NodoTarea;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.async.DeferredResult;

@Service
public class NodoServiceImpl implements NodoService {
    public static final String NUBE = "Nube";
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final long ESPERA_ORDENES_MS = 25000;
    private static final long ESPERA_ENTREGA_SEGUNDOS = 300;

    private final NodoDataSet datos;
    private final ArchivoDataSet archivosDatos;
    private final ArchivoServiceImpl archivos;
    private final NodoCanal canal;
    private final RespaldoService respaldos;
    private final String servidor;
    private final int diasSinPlan;
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(NodoServiceImpl.class);

    public NodoServiceImpl(NodoDataSet datos, ArchivoDataSet archivosDatos, ArchivoServiceImpl archivos, NodoCanal canal, RespaldoService respaldos,
                           @Value("${app.filered.url-base}") String urlBase, @Value("${app.filered.nodos.dias-sin-plan:30}") int diasSinPlan) {
        this.diasSinPlan = Math.max(1, diasSinPlan);
        this.respaldos = respaldos;
        this.datos = datos;
        this.archivosDatos = archivosDatos;
        this.archivos = archivos;
        this.canal = canal;
        String base = urlBase == null ? "" : urlBase.trim();
        this.servidor = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    @Override
    public List<Map<String, Object>> listar(Long clienteId) {
        revisarPlan(clienteId);
        boolean planNodos = respaldos.permiteNodos(clienteId);
        boolean planNube = respaldos.permiteNube(clienteId);
        Long predeterminado = planNodos ? datos.predeterminado(clienteId) : null;
        java.sql.Timestamp nubeSinPlan = datos.nubeSinPlanDesde(clienteId);
        Map<String, Object> resumenNube = datos.resumenNube(clienteId);
        Map<Long, Map<String, Object>> resumen = new HashMap<>();
        for (Map<String, Object> fila : datos.resumenNodos(clienteId)) resumen.put(numero(fila.get("nodoId")), fila);
        Set<Long> conectados = new HashSet<>(datos.conectados(clienteId));
        Set<Long> codigos = new HashSet<>(datos.codigosVigentes(clienteId));
        Map<Long, List<Long>> origenes = respaldos.origenesCliente(clienteId);
        Map<Long, Map<String, Object>> progreso = respaldos.progreso(clienteId);
        Map<Long, Long> respaldados = respaldos.respaldadosPorOrigen(clienteId);
        Map<Long, Integer> cobertura = new HashMap<>();
        for (List<Long> lista : origenes.values()) for (Long o : lista) cobertura.merge(o, 1, Integer::sum);
        List<Map<String, Object>> lista = new ArrayList<>();
        Map<String, Object> nube = new LinkedHashMap<>();
        nube.put("nodoId", 0);
        nube.put("nombre", NUBE);
        nube.put("tipo", NUBE);
        nube.put("predeterminado", predeterminado == null && planNube);
        nube.put("planNube", planNube);
        nube.put("soloLectura", nubeSinPlan != null);
        nube.put("eliminacion", nubeSinPlan == null ? null : java.sql.Timestamp.valueOf(nubeSinPlan.toLocalDateTime().plusDays(diasSinPlan)));
        nube.put("vinculado", true);
        nube.put("conectado", true);
        nube.put("archivos", numero(resumenNube.get("archivos")));
        nube.put("bytes", numero(resumenNube.get("bytes")));
        nube.put("pendientes", 0);
        nube.put("rol", RespaldoServiceImpl.ALMACENAMIENTO);
        nube.put("respaldos", cobertura.getOrDefault(0L, 0));
        nube.put("respaldados", respaldados.getOrDefault(0L, 0L));
        nube.put("planNodos", planNodos);
        nube.put("planRespaldos", respaldos.permiteRespaldos(clienteId));
        nube.put("diasSinPlan", diasSinPlan);
        lista.add(nube);
        for (Nodo nodo : datos.findByCliente(clienteId)) {
            Map<String, Object> fila = resumen.getOrDefault(nodo.getNodoId(), Map.of());
            Map<String, Object> vista = vista(nodo, predeterminado, conectados.contains(nodo.getNodoId()), codigos.contains(nodo.getNodoId()), fila);
            if (RespaldoServiceImpl.RESPALDO.equals(nodo.getRol())) {
                Map<String, Object> p = progreso.getOrDefault(nodo.getNodoId(), Map.of());
                vista.put("origenes", origenes.getOrDefault(nodo.getNodoId(), List.of()));
                vista.put("totalRespaldo", numero(p.get("total")));
                vista.put("bytesRespaldo", numero(p.get("bytes")));
                vista.put("listosRespaldo", numero(p.get("listos")));
                vista.put("ultimaCopia", p.get("ultimaCopia"));
                vista.put("pendientes", datos.tareasPendientes(nodo.getNodoId()));
            } else {
                vista.put("respaldos", cobertura.getOrDefault(nodo.getNodoId(), 0));
                vista.put("respaldados", respaldados.getOrDefault(nodo.getNodoId(), 0L));
                vista.put("restauracion", respaldos.restauracion(nodo.getNodoId()));
            }
            lista.add(vista);
        }
        return lista;
    }

    private Map<String, Object> vista(Nodo nodo, Long predeterminado, boolean conectado, boolean codigoVigente, Map<String, Object> resumen) {
        Map<String, Object> vista = new LinkedHashMap<>();
        vista.put("nodoId", nodo.getNodoId());
        vista.put("nombre", nodo.getNombre());
        vista.put("tipo", "Equipo");
        vista.put("rol", nodo.getRol());
        vista.put("soloLectura", nodo.getSinPlanDesde() != null);
        vista.put("sinPlanDesde", nodo.getSinPlanDesde());
        vista.put("eliminacion", nodo.getSinPlanDesde() == null ? null
                : java.sql.Timestamp.valueOf(nodo.getSinPlanDesde().toLocalDateTime().plusDays(diasSinPlan)));
        vista.put("predeterminado", nodo.getNodoId().equals(predeterminado));
        vista.put("vinculado", nodo.getTokenHash() != null);
        vista.put("conectado", nodo.getTokenHash() != null && conectado);
        vista.put("codigoVigente", nodo.getTokenHash() == null && codigoVigente);
        vista.put("codigoExpira", nodo.getCodigoExpira());
        vista.put("equipo", nodo.getEquipo());
        vista.put("plataforma", nodo.getPlataforma());
        vista.put("versionPrograma", nodo.getVersionPrograma());
        vista.put("carpeta", nodo.getCarpeta());
        vista.put("espacioTotal", nodo.getEspacioTotal());
        vista.put("espacioLibre", nodo.getEspacioLibre());
        vista.put("ultimaConexion", nodo.getUltimaConexion());
        vista.put("fechaVinculacion", nodo.getFechaVinculacion());
        vista.put("fechaCreacion", nodo.getFechaCreacion());
        vista.put("archivos", numero(resumen.get("archivos")));
        vista.put("bytes", numero(resumen.get("bytes")));
        vista.put("pendientes", numero(resumen.get("pendientes")));
        return vista;
    }

    @Override
    public Map<String, Object> crear(Long clienteId, String nombre, String rol, List<Long> origenes, String actor) {
        String tipo = RespaldoServiceImpl.RESPALDO.equalsIgnoreCase(rol == null ? "" : rol.trim()) ? RespaldoServiceImpl.RESPALDO : RespaldoServiceImpl.ALMACENAMIENTO;
        String limpio = nombreValido(clienteId, nombre, null);
        archivos.vigente(clienteId);
        exigirPlan(clienteId, tipo);
        String codigo = nuevoCodigo();
        LocalDateTime expira = LocalDateTime.now().plusHours(24);
        Long nodoId = datos.create(clienteId, limpio, tipo, hash(normalizarCodigo(codigo)), expira, actor);
        if (RespaldoServiceImpl.RESPALDO.equals(tipo)) respaldos.configurar(clienteId, datos.findById(nodoId), origenes);
        return respuestaCodigo(clienteId, nodoId, codigo, expira);
    }

    private void exigirPlan(Long clienteId, String rol) {
        if (RespaldoServiceImpl.RESPALDO.equals(rol)) {
            if (!respaldos.permiteRespaldos(clienteId)) throw new IllegalArgumentException("Tu plan no incluye servidores de respaldo");
        } else if (!respaldos.permiteNodos(clienteId)) {
            throw new IllegalArgumentException("Tu plan no incluye nodos propios");
        }
    }

    @Override
    public Map<String, Object> origenes(Long clienteId, Long nodoId, List<Long> origenes) {
        Nodo nodo = delCliente(clienteId, nodoId);
        if (!RespaldoServiceImpl.RESPALDO.equals(nodo.getRol())) throw new IllegalArgumentException("Solo los respaldos eligen qué nodos respaldan");
        exigirPlan(clienteId, RespaldoServiceImpl.RESPALDO);
        respaldos.configurar(clienteId, nodo, origenes);
        return Map.of("mensaje", "Nodos respaldados actualizados");
    }

    @Override
    public Map<String, Object> renombrar(Long clienteId, Long nodoId, String nombre, String actor) {
        Nodo nodo = delCliente(clienteId, nodoId);
        exigirPlan(clienteId, nodo.getRol());
        String limpio = nombreValido(clienteId, nombre, nodo.getNodoId());
        datos.updateNombre(nodo.getNodoId(), limpio, actor);
        if (!RespaldoServiceImpl.RESPALDO.equals(nodo.getRol())) respaldos.alRenombrarOrigen(clienteId, nodo.getNodoId(), nodo.getNombre(), limpio);
        return Map.of("mensaje", "Nodo actualizado correctamente", "nombre", limpio);
    }

    @Override
    public Map<String, Object> generarCodigo(Long clienteId, Long nodoId, String actor) {
        Nodo nodo = delCliente(clienteId, nodoId);
        String codigo = nuevoCodigo();
        LocalDateTime expira = LocalDateTime.now().plusHours(24);
        datos.asignarCodigo(nodo.getNodoId(), hash(normalizarCodigo(codigo)), expira, actor);
        canal.olvidar(nodo.getNodoId());
        return respuestaCodigo(clienteId, nodo.getNodoId(), codigo, expira);
    }

    private Map<String, Object> respuestaCodigo(Long clienteId, Long nodoId, String codigo, LocalDateTime expira) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("nodoId", nodoId);
        respuesta.put("codigo", codigo);
        respuesta.put("servidor", servidor);
        respuesta.put("expira", expira);
        Nodo nodo = datos.findById(nodoId);
        respuesta.put("nombre", nodo == null ? null : nodo.getNombre());
        return respuesta;
    }

    @Override
    public Map<String, Object> eliminar(Long clienteId, Long nodoId) {
        Nodo nodo = delCliente(clienteId, nodoId);
        long total = datos.findArchivosNodo(nodo.getNodoId()).size();
        if (!RespaldoServiceImpl.RESPALDO.equals(nodo.getRol())) respaldos.alEliminarOrigen(clienteId, nodo);
        datos.delete(nodo.getNodoId());
        canal.olvidar(nodo.getNodoId());
        borrarDirectorio(carpetaTemporal(nodo));
        if (total > 0) archivosDatos.sumarMetrica(clienteId, "archivosEliminados", total);
        return Map.of("mensaje", "Nodo eliminado correctamente");
    }

    @Override
    public Map<String, Object> predeterminar(Long clienteId, Long nodoId) {
        if (nodoId == null || nodoId == 0) {
            if (!respaldos.permiteNube(clienteId)) throw new IllegalArgumentException("Tu plan no incluye la Nube: el nodo por defecto debe ser uno de tus equipos");
            datos.predeterminar(clienteId, null);
            return Map.of("mensaje", "La Nube es ahora el nodo por defecto", "nodoId", 0);
        }
        Nodo nodo = delCliente(clienteId, nodoId);
        if (RespaldoServiceImpl.RESPALDO.equals(nodo.getRol())) throw new IllegalArgumentException("Un servidor de respaldo no puede ser el nodo por defecto");
        exigirPlan(clienteId, RespaldoServiceImpl.ALMACENAMIENTO);
        if (nodo.getTokenHash() == null) throw new IllegalArgumentException("Vincula el equipo antes de marcarlo como nodo por defecto");
        datos.predeterminar(clienteId, nodo.getNodoId());
        return Map.of("mensaje", nodo.getNombre() + " es ahora el nodo por defecto", "nodoId", nodo.getNodoId());
    }

    @Override
    public Nodo delCliente(Long clienteId, Long nodoId) {
        if (nodoId == null || nodoId <= 0) throw new IllegalArgumentException("La Nube no se puede modificar");
        Nodo nodo = datos.findById(nodoId);
        if (nodo == null || !nodo.getClienteId().equals(clienteId)) throw new IllegalArgumentException("El nodo no existe");
        return nodo;
    }

    @Override
    public Long predeterminado(Long clienteId) {
        return datos.predeterminado(clienteId);
    }

    @Override
    public Map<String, Object> vincular(Map<String, Object> entrada) {
        String codigo = normalizarCodigo(texto(entrada.get("codigo")));
        if (codigo.length() != 12) throw new IllegalArgumentException("Escribe el código de vinculación completo");
        Nodo nodo = datos.findByCodigo(hash(codigo));
        if (nodo == null) throw new IllegalArgumentException("El código no es válido o ya venció. Genera uno nuevo en FileRed");
        String token = nuevoToken();
        datos.vincular(nodo.getNodoId(), hash(token), corto(entrada.get("equipo"), 120), corto(entrada.get("plataforma"), 120),
                corto(entrada.get("version"), 20), corto(entrada.get("carpeta"), 1000), largo(entrada.get("espacioTotal")), largo(entrada.get("espacioLibre")));
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("token", token);
        respuesta.put("nodo", nodo.getNombre());
        respuesta.put("cliente", datos.nombreCliente(nodo.getClienteId()));
        respuesta.put("rol", nodo.getRol());
        return respuesta;
    }

    @Override
    public Nodo autenticar(String token) {
        if (token == null || token.isBlank()) throw new NodoNoAutorizadoException("Este equipo no está vinculado");
        Nodo nodo = datos.findByToken(hash(token.trim()));
        if (nodo == null) throw new NodoNoAutorizadoException("Este equipo ya no está vinculado a FileRed");
        return nodo;
    }

    @Override
    public Map<String, Object> latido(Nodo nodo, Map<String, Object> entrada) {
        datos.latido(nodo.getNodoId(), corto(entrada.get("version"), 20), corto(entrada.get("carpeta"), 1000),
                largo(entrada.get("espacioTotal")), largo(entrada.get("espacioLibre")));
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("nodo", nodo.getNombre());
        respuesta.put("cliente", datos.nombreCliente(nodo.getClienteId()));
        respuesta.put("pendientes", datos.tareasPendientes(nodo.getNodoId()));
        respuesta.put("predeterminado", nodo.getNodoId().equals(datos.predeterminado(nodo.getClienteId())));
        respuesta.put("rol", nodo.getRol());
        return respuesta;
    }

    @Override
    public DeferredResult<List<Map<String, Object>>> ordenes(Nodo nodo) {
        datos.tocar(nodo.getNodoId());
        DeferredResult<List<Map<String, Object>>> resultado = new DeferredResult<>(ESPERA_ORDENES_MS, List.of());
        List<Map<String, Object>> listas = recolectar(nodo);
        if (!listas.isEmpty()) {
            resultado.setResult(listas);
            return resultado;
        }
        canal.esperar(nodo.getNodoId(), resultado);
        if (canal.hayEntregas(nodo.getNodoId()) || !datos.tareasParaEnviar(nodo.getNodoId(), 1).isEmpty()) avisar(nodo.getNodoId());
        return resultado;
    }

    private List<Map<String, Object>> recolectar(Nodo nodo) {
        List<Map<String, Object>> ordenes = new ArrayList<>(canal.sacarEntregas(nodo.getNodoId()));
        List<Long> enviadas = new ArrayList<>();
        boolean respaldo = RespaldoServiceImpl.RESPALDO.equals(nodo.getRol());
        if (respaldo && !respaldos.permiteRespaldos(nodo.getClienteId())) return ordenes;
        for (NodoTarea tarea : datos.tareasParaEnviar(nodo.getNodoId(), 25)) {
            if ("COPIAR".equals(tarea.getTipo())) {
                Map<String, Object> copia = respaldos.orden(nodo, tarea);
                if (copia != null) {
                    ordenes.add(copia);
                    enviadas.add(tarea.getNodoTareaId());
                }
                continue;
            }
            Map<String, Object> orden = new LinkedHashMap<>();
            orden.put("id", String.valueOf(tarea.getNodoTareaId()));
            orden.put("tipo", tarea.getTipo());
            if ("GUARDAR".equals(tarea.getTipo())) {
                Archivo archivo = tarea.getArchivoId() == null ? null : datos.findArchivoById(tarea.getArchivoId());
                if (archivo == null || !nodo.getNodoId().equals(archivo.getNodoId())) {
                    datos.terminarTarea(tarea.getNodoTareaId(), "Cancelada", null);
                    continue;
                }
                if ("Listo".equals(archivo.getEstadoNodo())) {
                    datos.terminarTarea(tarea.getNodoTareaId(), "Hecha", null);
                    continue;
                }
                if (!Files.isRegularFile(temporal(nodo, archivo.getIdentificador(), archivo.getExtension()))) {
                    datos.terminarTarea(tarea.getNodoTareaId(), "Cancelada", "Sin copia temporal");
                    continue;
                }
                orden.put("ruta", ruta(archivo));
                orden.put("hash", archivo.getHashSha256());
                orden.put("tamano", archivo.getTamano());
            } else {
                orden.put("ruta", tarea.getRuta());
                orden.put("destino", tarea.getDestino());
            }
            ordenes.add(orden);
            enviadas.add(tarea.getNodoTareaId());
        }
        if (!enviadas.isEmpty()) datos.marcarEnviadas(enviadas);
        return ordenes;
    }

    @Override
    public Entrega contenido(Nodo nodo, Long tareaId) {
        NodoTarea tarea = datos.findTarea(nodo.getNodoId(), tareaId);
        if (tarea != null && "COPIAR".equals(tarea.getTipo())) return respaldos.contenido(nodo, tarea);
        if (tarea == null || !"GUARDAR".equals(tarea.getTipo())) throw new IllegalArgumentException("La tarea no existe");
        Archivo archivo = tarea.getArchivoId() == null ? null : datos.findArchivoById(tarea.getArchivoId());
        if (archivo == null) throw new IllegalStateException("El archivo ya no existe");
        Path ruta = temporal(nodo, archivo.getIdentificador(), archivo.getExtension());
        if (!Files.isRegularFile(ruta)) throw new IllegalStateException("El archivo ya fue entregado");
        return new Entrega(ruta, false);
    }

    @Override
    public Map<String, Object> confirmar(Nodo nodo, Long tareaId, Map<String, Object> entrada) {
        NodoTarea tarea = datos.findTarea(nodo.getNodoId(), tareaId);
        if (tarea == null || "Hecha".equals(tarea.getEstado()) || "Cancelada".equals(tarea.getEstado())) return Map.of("ok", true);
        boolean ok = Boolean.TRUE.equals(entrada.get("ok"));
        String error = corto(entrada.get("error"), 1000);
        if ("COPIAR".equals(tarea.getTipo())) {
            respaldos.confirmar(nodo, tarea, ok, corto(entrada.get("hash"), 64), error, corto(entrada.get("ruta"), 1000));
            return Map.of("ok", true);
        }
        if (!"GUARDAR".equals(tarea.getTipo())) {
            if (ok) datos.terminarTarea(tarea.getNodoTareaId(), "Hecha", null);
            else datos.fallarTarea(tarea.getNodoTareaId(), error);
            return Map.of("ok", true);
        }
        Archivo archivo = tarea.getArchivoId() == null ? null : datos.findArchivoById(tarea.getArchivoId());
        if (archivo == null) {
            datos.terminarTarea(tarea.getNodoTareaId(), "Cancelada", null);
            String ruta = corto(entrada.get("ruta"), 1000);
            if (ok && ruta != null) {
                datos.crearTarea(nodo.getNodoId(), "ELIMINAR", null, ruta, null);
                avisar(nodo.getNodoId());
            }
            return Map.of("ok", true);
        }
        if (!ok) {
            if ("Listo".equals(archivo.getEstadoNodo())) datos.terminarTarea(tarea.getNodoTareaId(), "Hecha", null);
            else datos.fallarTarea(tarea.getNodoTareaId(), error);
            return Map.of("ok", true);
        }
        String recibido = texto(entrada.get("hash")).toLowerCase(Locale.ROOT);
        if (recibido.equals(archivo.getHashSha256()) && datos.marcarListo(archivo.getArchivoId(), recibido) > 0) {
            try {
                Files.deleteIfExists(temporal(nodo, archivo.getIdentificador(), archivo.getExtension()));
            } catch (IOException ignorado) {
                datos.terminarTarea(tarea.getNodoTareaId(), "Hecha", "No se pudo retirar la copia temporal");
                return Map.of("ok", true);
            }
        }
        datos.terminarTarea(tarea.getNodoTareaId(), "Hecha", null);
        return Map.of("ok", true);
    }

    @Override
    public void recibirEntrega(Nodo nodo, String solicitud, InputStream contenido, String error) {
        if (solicitud == null || !solicitud.startsWith(nodo.getNodoId() + "-")) throw new IllegalArgumentException("La solicitud no existe");
        CompletableFuture<Path> futuro = canal.solicitud(solicitud);
        if (futuro == null || futuro.isDone()) throw new IllegalArgumentException("La solicitud ya venció");
        if (error != null && !error.isBlank()) {
            futuro.completeExceptionally(new NodoNoDisponibleException(error));
            return;
        }
        Path destino = carpetaEntregas().resolve(solicitud + ".part");
        try {
            Files.createDirectories(destino.getParent());
            Files.copy(contenido, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException fallo) {
            futuro.completeExceptionally(new NodoNoDisponibleException("No se pudo recibir el archivo del equipo"));
            return;
        }
        if (!futuro.complete(destino)) {
            try {
                Files.deleteIfExists(destino);
            } catch (IOException ignorado) {
                return;
            }
        }
    }

    @Override
    public void desvincularEquipo(Nodo nodo) {
        datos.desvincular(nodo.getNodoId());
        canal.olvidar(nodo.getNodoId());
    }

    @Override
    public Entrega publico(String clienteUuid, String almacen, String categoria, String archivo) {
        if (clienteUuid == null || !clienteUuid.matches("[0-9a-fA-F-]{36}")) return null;
        if (!archivos.nombresValidos(almacen, categoria, archivo)) return null;
        Long clienteId = archivosDatos.clienteActivoPorUuid(clienteUuid);
        if (clienteId == null) return null;
        String identificador = StringUtils.stripFilenameExtension(archivo);
        String extension = archivos.extension(archivo);
        Archivo registro = datos.findArchivoPublico(clienteId, almacen, categoria, identificador);
        if (registro == null || !extension.equals(registro.getExtension())) return null;
        Nodo nodo = datos.findById(registro.getNodoId());
        if (nodo == null) return null;
        Path copia = temporal(nodo, identificador, extension);
        if (Files.isRegularFile(copia)) {
            contarDescarga(clienteId, registro.getTamano());
            return new Entrega(copia, false);
        }
        Path recibido;
        if (datos.conectado(nodo.getNodoId())) {
            try {
                recibido = pedirAlEquipo(nodo, ruta(registro), registro.getHashSha256());
            } catch (NodoNoDisponibleException error) {
                recibido = respaldos.desdeRespaldo(registro);
                if (recibido == null) throw error;
            }
        } else {
            recibido = respaldos.desdeRespaldo(registro);
            if (recibido == null) throw new NodoNoDisponibleException("El equipo donde está guardado este archivo no está conectado en este momento");
        }
        contarDescarga(clienteId, registro.getTamano());
        return new Entrega(recibido, true);
    }

    @Override
    public Path pedirAlEquipo(Nodo nodo, String rutaArchivo, String hashEsperado) {
        if (!datos.conectado(nodo.getNodoId())) throw new NodoNoDisponibleException("El equipo " + nodo.getNombre() + " no está conectado");
        String solicitud = nodo.getNodoId() + "-" + UUID.randomUUID();
        CompletableFuture<Path> futuro = canal.nuevaSolicitud(solicitud);
        Map<String, Object> orden = new LinkedHashMap<>();
        orden.put("id", solicitud);
        orden.put("tipo", "ENTREGAR");
        orden.put("ruta", rutaArchivo);
        canal.encolarEntrega(nodo.getNodoId(), orden);
        avisar(nodo.getNodoId());
        Path recibido;
        try {
            recibido = futuro.get(ESPERA_ENTREGA_SEGUNDOS, TimeUnit.SECONDS);
        } catch (InterruptedException interrupcion) {
            Thread.currentThread().interrupt();
            throw new NodoNoDisponibleException("La descarga fue interrumpida");
        } catch (ExecutionException fallo) {
            throw new NodoNoDisponibleException("El equipo no pudo entregar el archivo: " + fallo.getCause().getMessage());
        } catch (TimeoutException espera) {
            throw new NodoNoDisponibleException("El equipo tardó demasiado en entregar el archivo");
        } finally {
            canal.cerrarSolicitud(solicitud);
        }
        if (hashEsperado != null && !hashEsperado.equals(hashArchivo(recibido))) {
            try {
                Files.deleteIfExists(recibido);
            } catch (IOException ignorado) {
                throw new NodoNoDisponibleException("El archivo entregado por el equipo no coincide con el original");
            }
            throw new NodoNoDisponibleException("El archivo entregado por el equipo no coincide con el original");
        }
        return recibido;
    }

    private boolean cubierto(Long clienteId, String rol) {
        return RespaldoServiceImpl.RESPALDO.equals(rol) ? respaldos.permiteRespaldos(clienteId) : respaldos.permiteNodos(clienteId);
    }

    private void revisarPlan(Long clienteId) {
        if (respaldos.permiteNube(clienteId) || numero(datos.resumenNube(clienteId).get("archivos")) == 0) datos.limpiarNubeSinPlan(clienteId);
        else datos.marcarNubeSinPlan(clienteId);
        boolean nodosPlan = respaldos.permiteNodos(clienteId);
        boolean respaldosPlan = respaldos.permiteRespaldos(clienteId);
        for (Nodo nodo : datos.findByCliente(clienteId)) {
            boolean ok = RespaldoServiceImpl.RESPALDO.equals(nodo.getRol()) ? respaldosPlan : nodosPlan;
            if (ok) datos.limpiarSinPlan(nodo.getNodoId());
            else datos.marcarSinPlan(nodo.getNodoId());
        }
    }

    @Scheduled(cron = "${app.filered.nodos.cron-limpieza:0 15 * * * *}", zone = "America/Santo_Domingo")
    public void eliminarNodosSinPlan() {
        Set<Long> clientes = new java.util.LinkedHashSet<>(datos.clientesConNodos());
        clientes.addAll(datos.clientesConNube());
        for (Long clienteId : clientes) {
            try {
                revisarPlan(clienteId);
            } catch (RuntimeException error) {
                LOG.warn("No se pudo revisar el plan de los nodos del cliente {}: {}", clienteId, error.getMessage());
            }
        }
        for (Long clienteId : datos.clientesNubeVencida(diasSinPlan)) {
            if (respaldos.permiteNube(clienteId)) {
                datos.limpiarNubeSinPlan(clienteId);
                continue;
            }
            try {
                int almacenes = archivos.eliminarNubeCompleta(clienteId);
                datos.limpiarNubeSinPlan(clienteId);
                LOG.info("Nube del cliente {} eliminada ({} almacenes) tras {} días sin plan que la incluya", clienteId, almacenes, diasSinPlan);
            } catch (RuntimeException error) {
                LOG.warn("No se pudo eliminar la Nube del cliente {}: {}", clienteId, error.getMessage());
            }
        }
        for (Nodo nodo : datos.vencidosSinPlan(diasSinPlan)) {
            if (cubierto(nodo.getClienteId(), nodo.getRol())) {
                datos.limpiarSinPlan(nodo.getNodoId());
                continue;
            }
            try {
                eliminar(nodo.getClienteId(), nodo.getNodoId());
                LOG.info("Nodo {} ({}) del cliente {} eliminado tras {} días sin plan", nodo.getNodoId(), nodo.getNombre(), nodo.getClienteId(), diasSinPlan);
            } catch (RuntimeException error) {
                LOG.warn("No se pudo eliminar el nodo {} sin plan: {}", nodo.getNodoId(), error.getMessage());
            }
        }
    }

    @Scheduled(fixedDelay = 1800000, initialDelay = 60000)
    public void limpiarEntregas() {
        Path carpeta = carpetaEntregas();
        if (!Files.isDirectory(carpeta)) return;
        long limite = System.currentTimeMillis() - 3600000;
        try (Stream<Path> archivosTemporales = Files.list(carpeta)) {
            for (Path temporal : archivosTemporales.toList()) {
                if (Files.getLastModifiedTime(temporal).toMillis() < limite) Files.deleteIfExists(temporal);
            }
        } catch (IOException ignorado) {
            return;
        }
    }

    @Override
    public void avisar(Long nodoId) {
        DeferredResult<List<Map<String, Object>>> pendiente = canal.tomar(nodoId);
        if (pendiente == null) return;
        Nodo nodo = datos.findById(nodoId);
        pendiente.setResult(nodo == null ? List.of() : recolectar(nodo));
    }

    @Override
    public Path temporal(Nodo nodo, String identificador, String extension) {
        return carpetaTemporal(nodo).resolve(identificador + "." + extension);
    }

    public static String ruta(Archivo archivo) {
        return archivo.getAlmacen() + "/" + archivo.getCategoria() + "/" + archivo.getIdentificador() + "." + archivo.getExtension();
    }

    public static String hashArchivo(Path ruta) {
        try (InputStream entrada = Files.newInputStream(ruta)) {
            MessageDigest resumen = MessageDigest.getInstance("SHA-256");
            byte[] bloque = new byte[65536];
            int leidos;
            while ((leidos = entrada.read(bloque)) > 0) resumen.update(bloque, 0, leidos);
            return HexFormat.of().formatHex(resumen.digest());
        } catch (IOException | NoSuchAlgorithmException error) {
            return "";
        }
    }

    private void contarDescarga(Long clienteId, Long tamano) {
        archivosDatos.sumarMetrica(clienteId, "descargas", 1);
        archivosDatos.sumarMetrica(clienteId, "bytesDescargados", tamano == null ? 0 : tamano);
    }

    private Path carpetaTemporal(Nodo nodo) {
        return archivos.base().resolve("_nodos").resolve(nodo.getNodoUuid());
    }

    private Path carpetaEntregas() {
        return archivos.base().resolve("_nodos").resolve("_entregas");
    }

    private void borrarDirectorio(Path ruta) {
        if (!Files.exists(ruta)) return;
        try (Stream<Path> recorrido = Files.walk(ruta)) {
            for (Path elemento : recorrido.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(elemento);
        } catch (IOException ignorado) {
            return;
        }
    }

    private String nombreValido(Long clienteId, String nombre, Long excluir) {
        String limpio = nombre == null ? "" : nombre.trim().replaceAll("\\s+", " ");
        if (limpio.isEmpty()) throw new IllegalArgumentException("El nombre del nodo es obligatorio");
        if (limpio.length() > 80) throw new IllegalArgumentException("El nombre admite hasta 80 caracteres");
        if (limpio.equalsIgnoreCase(NUBE) || limpio.equalsIgnoreCase("Cloud")) throw new IllegalArgumentException("Ese nombre está reservado para la Nube");
        if (datos.existeNombre(clienteId, limpio, excluir)) throw new IllegalArgumentException("Ya tienes un nodo con ese nombre");
        return limpio;
    }

    private String nuevoCodigo() {
        StringBuilder codigo = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            if (i > 0 && i % 4 == 0) codigo.append('-');
            codigo.append(ALFABETO.charAt(ALEATORIO.nextInt(ALFABETO.length())));
        }
        return codigo.toString();
    }

    private static String normalizarCodigo(String codigo) {
        return codigo == null ? "" : codigo.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }

    private String nuevoToken() {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String valor) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 no disponible");
        }
    }

    private static String texto(Object valor) {
        return valor == null ? "" : valor.toString().trim();
    }

    private static String corto(Object valor, int maximo) {
        String limpio = texto(valor);
        if (limpio.isEmpty()) return null;
        return limpio.length() > maximo ? limpio.substring(0, maximo) : limpio;
    }

    private static Long largo(Object valor) {
        if (valor instanceof Number numero) return numero.longValue();
        try {
            return valor == null ? null : Long.parseLong(valor.toString().trim());
        } catch (NumberFormatException error) {
            return null;
        }
    }

    private static long numero(Object valor) {
        return valor instanceof Number n ? n.longValue() : 0;
    }
}

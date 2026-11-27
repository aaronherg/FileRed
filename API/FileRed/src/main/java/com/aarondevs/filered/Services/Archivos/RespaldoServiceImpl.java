package com.aarondevs.filered.Services.Archivos;

import com.aarondevs.filered.DAO.Archivos.NodoDataSet;
import com.aarondevs.filered.DAO.Archivos.RespaldoDataSet;
import com.aarondevs.filered.Models.Archivos.Archivo;
import com.aarondevs.filered.Models.Archivos.Nodo;
import com.aarondevs.filered.Models.Archivos.NodoTarea;
import com.aarondevs.filered.Models.Suscripciones.DetalleSuscripcion;
import com.aarondevs.filered.Services.Suscripciones.SuscripcionService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class RespaldoServiceImpl implements RespaldoService {
    public static final String RESPALDO = "Respaldo";
    public static final String ALMACENAMIENTO = "Almacenamiento";

    private final RespaldoDataSet datos;
    private final NodoDataSet nodosDatos;
    private final SuscripcionService suscripciones;
    private final NodoService nodos;
    private final ArchivoServiceImpl nube;
    private final Map<Long, Map<String, Object>> restauraciones = new ConcurrentHashMap<>();

    public RespaldoServiceImpl(RespaldoDataSet datos, NodoDataSet nodosDatos, SuscripcionService suscripciones, @Lazy NodoService nodos,
                               @Lazy ArchivoServiceImpl nube) {
        this.datos = datos;
        this.nodosDatos = nodosDatos;
        this.suscripciones = suscripciones;
        this.nodos = nodos;
        this.nube = nube;
    }

    @Override
    public boolean permiteNodos(Long clienteId) {
        DetalleSuscripcion plan = clienteId == null ? null : suscripciones.vigente(clienteId);
        return plan != null && Boolean.TRUE.equals(plan.getPermitirNodos());
    }

    @Override
    public boolean permiteNube(Long clienteId) {
        DetalleSuscripcion plan = clienteId == null ? null : suscripciones.vigente(clienteId);
        return plan == null || !Boolean.FALSE.equals(plan.getPermitirNube());
    }

    @Override
    public boolean permiteRespaldos(Long clienteId) {
        DetalleSuscripcion plan = clienteId == null ? null : suscripciones.vigente(clienteId);
        return plan != null && Boolean.TRUE.equals(plan.getPermitirRespaldos());
    }

    private static long origen(Long origenId) {
        return origenId == null ? 0 : origenId;
    }

    public static String carpeta(long origenId, String nombre) {
        if (origenId == 0) return "Nube";
        String limpio = nombre == null ? "" : nombre.replaceAll("[^\\p{L}\\p{N} ._-]", "_").replaceAll("\\s+", " ").trim();
        while (limpio.endsWith(".")) limpio = limpio.substring(0, limpio.length() - 1).trim();
        return (limpio.isEmpty() ? "Nodo" : limpio) + " (" + origenId + ")";
    }

    private String prefijo(long origenId) {
        if (origenId == 0) return "Nube";
        Nodo nodo = nodosDatos.findById(origenId);
        return carpeta(origenId, nodo == null ? null : nodo.getNombre());
    }

    private void encolar(Long clienteId, Long origenId, String tipo, Long archivoId, String ruta, String destino) {
        long o = origen(origenId);
        List<Long> respaldos = datos.respaldosDeOrigen(clienteId, o);
        if (respaldos.isEmpty()) return;
        String base = prefijo(o);
        for (Long respaldoId : respaldos) {
            nodosDatos.crearTarea(respaldoId, tipo, archivoId, ruta == null ? null : base + "/" + ruta, destino == null ? null : base + "/" + destino);
            nodos.avisar(respaldoId);
        }
    }

    @Override
    public void alSubir(Long clienteId, Long origenId, Long archivoId) {
        if (archivoId != null) encolar(clienteId, origenId, "COPIAR", archivoId, null, null);
    }

    @Override
    public void alReemplazar(Long clienteId, Long origenId, Archivo anterior, String extensionNueva) {
        if (anterior == null) return;
        if (!anterior.getExtension().equals(extensionNueva)) encolar(clienteId, origenId, "ELIMINAR", null, NodoServiceImpl.ruta(anterior), null);
        encolar(clienteId, origenId, "COPIAR", anterior.getArchivoId(), null, null);
    }

    @Override
    public void alEliminarArchivo(Long clienteId, Long origenId, Archivo archivo) {
        if (archivo != null) encolar(clienteId, origenId, "ELIMINAR", null, NodoServiceImpl.ruta(archivo), null);
    }

    @Override
    public void alCrearDirectorio(Long clienteId, Long origenId, String ruta) {
        encolar(clienteId, origenId, "CREAR", null, ruta, null);
    }

    @Override
    public void alMoverDirectorio(Long clienteId, Long origenId, String desde, String hasta) {
        encolar(clienteId, origenId, "MOVER", null, desde, hasta);
    }

    @Override
    public void alEliminarDirectorio(Long clienteId, Long origenId, String ruta) {
        encolar(clienteId, origenId, "ELIMINAR", null, ruta, null);
    }

    @Override
    public void alRenombrarOrigen(Long clienteId, Long nodoId, String nombreAnterior, String nombreNuevo) {
        String desde = carpeta(nodoId, nombreAnterior);
        String hasta = carpeta(nodoId, nombreNuevo);
        if (desde.equals(hasta)) return;
        for (Long respaldoId : datos.respaldosDeOrigen(clienteId, nodoId)) {
            nodosDatos.crearTarea(respaldoId, "MOVER", null, desde, hasta);
            nodos.avisar(respaldoId);
        }
    }

    @Override
    public void alEliminarOrigen(Long clienteId, Nodo origen) {
        String carpeta = carpeta(origen.getNodoId(), origen.getNombre());
        for (Long respaldoId : datos.respaldosDeOrigen(clienteId, origen.getNodoId())) {
            datos.olvidarCopiasDeOrigen(respaldoId, clienteId, origen.getNodoId());
            datos.quitarOrigen(respaldoId, origen.getNodoId());
            nodosDatos.crearTarea(respaldoId, "ELIMINAR", null, carpeta, null);
            nodos.avisar(respaldoId);
        }
    }

    @Override
    public void configurar(Long clienteId, Nodo respaldo, Collection<Long> origenes) {
        Set<Long> nuevos = new LinkedHashSet<>();
        for (Long o : origenes == null ? List.<Long>of() : origenes) {
            long id = origen(o);
            if (id != 0) {
                Nodo n = nodosDatos.findById(id);
                if (n == null || !n.getClienteId().equals(clienteId)) throw new IllegalArgumentException("Uno de los nodos elegidos no existe");
                if (RESPALDO.equals(n.getRol())) throw new IllegalArgumentException("Un respaldo no puede respaldar a otro respaldo");
            }
            nuevos.add(id);
        }
        Set<Long> actuales = new LinkedHashSet<>(datos.origenes(respaldo.getNodoId()));
        if (nuevos.contains(0L) && !actuales.contains(0L) && !permiteNube(clienteId)) {
            throw new IllegalArgumentException("Tu plan no incluye la Nube, así que no se puede respaldar");
        }
        for (Long quitado : actuales) {
            if (nuevos.contains(quitado)) continue;
            datos.olvidarCopiasDeOrigen(respaldo.getNodoId(), clienteId, quitado);
            datos.quitarOrigen(respaldo.getNodoId(), quitado);
            nodosDatos.crearTarea(respaldo.getNodoId(), "ELIMINAR", null, prefijo(quitado), null);
        }
        for (Long agregado : nuevos) {
            if (actuales.contains(agregado)) continue;
            datos.agregarOrigen(respaldo.getNodoId(), agregado);
            nodosDatos.crearTarea(respaldo.getNodoId(), "CREAR", null, prefijo(agregado), null);
            datos.sembrar(respaldo.getNodoId(), clienteId, agregado);
        }
        nodos.avisar(respaldo.getNodoId());
    }

    private Path rutaNube(Archivo archivo) {
        return nube.base().resolve(archivo.getClienteUuid()).resolve(archivo.getAlmacen()).resolve(archivo.getCategoria())
                .resolve(archivo.getIdentificador() + "." + archivo.getExtension());
    }

    @Override
    public Map<String, Object> orden(Nodo respaldo, NodoTarea tarea) {
        Archivo archivo = tarea.getArchivoId() == null ? null : nodosDatos.findArchivoById(tarea.getArchivoId());
        if (archivo == null || !archivo.getClienteId().equals(respaldo.getClienteId())) {
            nodosDatos.terminarTarea(tarea.getNodoTareaId(), "Cancelada", null);
            return null;
        }
        long o = origen(archivo.getNodoId());
        if (!datos.origenes(respaldo.getNodoId()).contains(o)) {
            nodosDatos.terminarTarea(tarea.getNodoTareaId(), "Cancelada", null);
            return null;
        }
        if (archivo.getHashSha256() == null && o == 0) {
            Path ruta = rutaNube(archivo);
            if (!Files.isRegularFile(ruta)) {
                nodosDatos.terminarTarea(tarea.getNodoTareaId(), "Cancelada", "El archivo no está en la Nube");
                return null;
            }
            String hash = NodoServiceImpl.hashArchivo(ruta);
            datos.actualizarHash(archivo.getArchivoId(), hash);
            archivo.setHashSha256(hash);
        }
        if (archivo.getHashSha256() != null && datos.tieneCopia(archivo.getArchivoId(), respaldo.getNodoId(), archivo.getHashSha256())) {
            nodosDatos.terminarTarea(tarea.getNodoTareaId(), "Hecha", null);
            return null;
        }
        Map<String, Object> orden = new LinkedHashMap<>();
        orden.put("id", String.valueOf(tarea.getNodoTareaId()));
        orden.put("tipo", "COPIAR");
        orden.put("ruta", prefijo(o) + "/" + NodoServiceImpl.ruta(archivo));
        orden.put("hash", archivo.getHashSha256());
        orden.put("tamano", archivo.getTamano());
        return orden;
    }

    @Override
    public NodoService.Entrega contenido(Nodo respaldo, NodoTarea tarea) {
        Archivo archivo = tarea.getArchivoId() == null ? null : nodosDatos.findArchivoById(tarea.getArchivoId());
        if (archivo == null || !archivo.getClienteId().equals(respaldo.getClienteId())) throw new IllegalStateException("El archivo ya no existe");
        if (archivo.getNodoId() == null) {
            Path ruta = rutaNube(archivo);
            if (!Files.isRegularFile(ruta)) throw new IllegalStateException("El archivo ya no está en la Nube");
            return new NodoService.Entrega(ruta, false);
        }
        Nodo origen = nodosDatos.findById(archivo.getNodoId());
        if (origen == null) throw new IllegalStateException("El equipo de origen ya no existe");
        Path copia = nodos.temporal(origen, archivo.getIdentificador(), archivo.getExtension());
        if (Files.isRegularFile(copia)) return new NodoService.Entrega(copia, false);
        if (!nodosDatos.conectado(origen.getNodoId())) throw new IllegalStateException("El equipo " + origen.getNombre() + " está desconectado");
        try {
            return new NodoService.Entrega(nodos.pedirAlEquipo(origen, NodoServiceImpl.ruta(archivo), archivo.getHashSha256()), true);
        } catch (NodoService.NodoNoDisponibleException error) {
            throw new IllegalStateException(error.getMessage());
        }
    }

    @Override
    public void confirmar(Nodo respaldo, NodoTarea tarea, boolean ok, String hash, String error, String ruta) {
        Archivo archivo = tarea.getArchivoId() == null ? null : nodosDatos.findArchivoById(tarea.getArchivoId());
        if (archivo == null) {
            nodosDatos.terminarTarea(tarea.getNodoTareaId(), "Cancelada", null);
            if (ok && ruta != null) {
                nodosDatos.crearTarea(respaldo.getNodoId(), "ELIMINAR", null, ruta, null);
                nodos.avisar(respaldo.getNodoId());
            }
            return;
        }
        if (!ok) {
            nodosDatos.fallarTarea(tarea.getNodoTareaId(), error);
            return;
        }
        if (hash != null && hash.equalsIgnoreCase(archivo.getHashSha256())) datos.marcarCopia(archivo.getArchivoId(), respaldo.getNodoId(), archivo.getHashSha256());
        nodosDatos.terminarTarea(tarea.getNodoTareaId(), "Hecha", null);
    }

    @Override
    public Path desdeRespaldo(Archivo archivo) {
        if (archivo.getHashSha256() == null) return null;
        Long respaldoId = datos.respaldoConectadoConCopia(archivo.getArchivoId(), archivo.getHashSha256());
        if (respaldoId == null) return null;
        Nodo respaldo = nodosDatos.findById(respaldoId);
        if (respaldo == null) return null;
        try {
            return nodos.pedirAlEquipo(respaldo, prefijo(origen(archivo.getNodoId())) + "/" + NodoServiceImpl.ruta(archivo), archivo.getHashSha256());
        } catch (NodoService.NodoNoDisponibleException error) {
            return null;
        }
    }

    @Override
    public Map<String, Object> restaurar(Long clienteId, Long nodoId) {
        Nodo nodo = nodos.delCliente(clienteId, nodoId);
        if (RESPALDO.equals(nodo.getRol())) throw new IllegalArgumentException("Solo se restauran nodos de almacenamiento");
        if (!permiteNube(clienteId)) throw new IllegalArgumentException("Tu plan no incluye la Nube, así que no se puede restaurar en ella");
        Map<String, Object> actual = restauraciones.get(nodo.getNodoId());
        if (actual != null && Boolean.TRUE.equals(actual.get("activa"))) throw new IllegalArgumentException("La restauración ya está en curso");
        List<Archivo> lista = nodosDatos.findArchivosNodo(nodo.getNodoId());
        if (lista.isEmpty()) throw new IllegalArgumentException("Este nodo no tiene archivos para restaurar");
        Map<String, Object> estado = new ConcurrentHashMap<>();
        estado.put("activa", true);
        estado.put("total", lista.size());
        estado.put("hechos", 0);
        estado.put("errores", 0);
        restauraciones.put(nodo.getNodoId(), estado);
        Thread hilo = new Thread(() -> ejecutarRestauracion(clienteId, nodo, lista, estado), "FileRed-restaurar-" + nodo.getNodoId());
        hilo.setDaemon(true);
        hilo.start();
        return Map.of("mensaje", "Restauración iniciada", "total", lista.size());
    }

    private void ejecutarRestauracion(Long clienteId, Nodo nodo, List<Archivo> lista, Map<String, Object> estado) {
        String carpetaOrigen = carpeta(nodo.getNodoId(), nodo.getNombre());
        try {
            for (Archivo archivo : lista) {
                if (archivo.getNodoId() == null || archivo.getHashSha256() == null) {
                    sumar(estado, "errores");
                    continue;
                }
                Path recibido = null;
                Path temporal = nodos.temporal(nodo, archivo.getIdentificador(), archivo.getExtension());
                boolean desdeTemporal = Files.isRegularFile(temporal);
                if (!desdeTemporal) {
                    Long respaldoId = datos.respaldoConectadoConCopia(archivo.getArchivoId(), archivo.getHashSha256());
                    Nodo respaldo = respaldoId == null ? null : nodosDatos.findById(respaldoId);
                    if (respaldo == null) {
                        sumar(estado, "errores");
                        continue;
                    }
                    try {
                        recibido = nodos.pedirAlEquipo(respaldo, carpetaOrigen + "/" + NodoServiceImpl.ruta(archivo), archivo.getHashSha256());
                    } catch (NodoService.NodoNoDisponibleException error) {
                        sumar(estado, "errores");
                        continue;
                    }
                }
                Path destino = rutaNube(archivo);
                try {
                    Files.createDirectories(destino.getParent());
                    Files.move(desdeTemporal ? temporal : recibido, destino, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException error) {
                    sumar(estado, "errores");
                    continue;
                }
                datos.moverANube(archivo.getArchivoId());
                datos.olvidarCopias(archivo.getArchivoId());
                String ruta = NodoServiceImpl.ruta(archivo);
                for (Long respaldoId : datos.respaldosDeOrigen(clienteId, nodo.getNodoId())) {
                    nodosDatos.crearTarea(respaldoId, "ELIMINAR", null, carpetaOrigen + "/" + ruta, null);
                }
                nodosDatos.crearTarea(nodo.getNodoId(), "ELIMINAR", null, ruta, null);
                alSubir(clienteId, 0L, archivo.getArchivoId());
                sumar(estado, "hechos");
            }
            for (Long respaldoId : datos.respaldosDeOrigen(clienteId, nodo.getNodoId())) nodos.avisar(respaldoId);
            nodos.avisar(nodo.getNodoId());
        } finally {
            estado.put("activa", false);
        }
    }

    private static void sumar(Map<String, Object> estado, String clave) {
        estado.merge(clave, 1, (a, b) -> ((Integer) a) + ((Integer) b));
    }

    @Override
    public Map<String, Object> restauracion(Long nodoId) {
        Map<String, Object> estado = restauraciones.get(nodoId);
        return estado == null ? null : new HashMap<>(estado);
    }

    @Override
    public Map<Long, List<Long>> origenesCliente(Long clienteId) {
        Map<Long, List<Long>> mapa = new HashMap<>();
        for (Map<String, Object> fila : datos.origenesCliente(clienteId)) {
            mapa.computeIfAbsent(numero(fila.get("respaldoId")), k -> new ArrayList<>()).add(numero(fila.get("origenNodoId")));
        }
        return mapa;
    }

    @Override
    public Map<Long, Map<String, Object>> progreso(Long clienteId) {
        Map<Long, Map<String, Object>> mapa = new HashMap<>();
        for (Map<String, Object> fila : datos.progreso(clienteId)) mapa.put(numero(fila.get("respaldoId")), fila);
        return mapa;
    }

    @Override
    public Map<Long, Long> respaldadosPorOrigen(Long clienteId) {
        Map<Long, Long> mapa = new HashMap<>();
        for (Map<String, Object> fila : datos.respaldadosPorOrigen(clienteId)) mapa.put(numero(fila.get("origenNodoId")), numero(fila.get("respaldados")));
        return mapa;
    }

    private static long numero(Object valor) {
        return valor instanceof Number n ? n.longValue() : 0;
    }
}

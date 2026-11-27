package com.aarondevs.filered.DAO.Archivos;

import com.aarondevs.filered.Models.Archivos.Archivo;
import com.aarondevs.filered.Models.Archivos.Nodo;
import com.aarondevs.filered.Models.Archivos.NodoTarea;
import com.aarondevs.filered.Parameters.Response.PaginationResponse;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class NodoDAO implements NodoDataSet {
    private static final BeanPropertyRowMapper<Nodo> NODO = new BeanPropertyRowMapper<>(Nodo.class);
    private static final BeanPropertyRowMapper<Archivo> ARCHIVO = new BeanPropertyRowMapper<>(Archivo.class);
    private static final BeanPropertyRowMapper<NodoTarea> TAREA = new BeanPropertyRowMapper<>(NodoTarea.class);

    private final JdbcTemplate jdbc;

    public NodoDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Nodo> findByCliente(Long clienteId) {
        return jdbc.query("SELECT * FROM Nodos WHERE clienteId=? AND estado='Activo' ORDER BY fechaCreacion, nodoId", NODO, clienteId);
    }

    @Override
    public Nodo findById(Long nodoId) {
        return jdbc.query("SELECT * FROM Nodos WHERE nodoId=? AND estado='Activo'", NODO, nodoId).stream().findFirst().orElse(null);
    }

    @Override
    public Nodo findByToken(String tokenHash) {
        return jdbc.query("SELECT n.* FROM Nodos n JOIN Clientes c ON c.clienteId=n.clienteId WHERE n.tokenHash=? AND n.estado='Activo' "
                + "AND c.estado='Activo'", NODO, tokenHash).stream().findFirst().orElse(null);
    }

    @Override
    public Nodo findByCodigo(String codigoHash) {
        return jdbc.query("SELECT n.* FROM Nodos n JOIN Clientes c ON c.clienteId=n.clienteId WHERE n.codigoHash=? AND n.estado='Activo' "
                + "AND c.estado='Activo' AND n.codigoExpira>NOW(3)", NODO, codigoHash).stream().findFirst().orElse(null);
    }

    @Override
    public boolean existeNombre(Long clienteId, String nombre, Long excluirNodoId) {
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM Nodos WHERE clienteId=? AND LOWER(nombre)=LOWER(?) AND estado='Activo' "
                + "AND nodoId<>?", Long.class, clienteId, nombre, excluirNodoId == null ? 0L : excluirNodoId);
        return total != null && total > 0;
    }

    @Override
    public Long create(Long clienteId, String nombre, String rol, String codigoHash, LocalDateTime expira, String actor) {
        GeneratedKeyHolder llave = new GeneratedKeyHolder();
        jdbc.update(conexion -> {
            PreparedStatement sentencia = conexion.prepareStatement("INSERT INTO Nodos (nodoUuid, clienteId, nombre, codigoHash, codigoExpira, "
                    + "usuarioCreacionUuid, rol) VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            sentencia.setString(1, UUID.randomUUID().toString());
            sentencia.setLong(2, clienteId);
            sentencia.setString(3, nombre);
            sentencia.setString(4, codigoHash);
            sentencia.setTimestamp(5, Timestamp.valueOf(expira));
            sentencia.setString(6, actor);
            sentencia.setString(7, rol);
            return sentencia;
        }, llave);
        return llave.getKey() == null ? null : llave.getKey().longValue();
    }

    @Override
    public void updateNombre(Long nodoId, String nombre, String actor) {
        jdbc.update("UPDATE Nodos SET nombre=?, fechaModificacion=NOW(3), usuarioModificacionUuid=? WHERE nodoId=?", nombre, actor, nodoId);
    }

    @Override
    public void asignarCodigo(Long nodoId, String codigoHash, LocalDateTime expira, String actor) {
        jdbc.update("UPDATE Nodos SET codigoHash=?, codigoExpira=?, tokenHash=NULL, fechaModificacion=NOW(3), usuarioModificacionUuid=? "
                + "WHERE nodoId=?", codigoHash, Timestamp.valueOf(expira), actor, nodoId);
    }

    @Override
    public void vincular(Long nodoId, String tokenHash, String equipo, String plataforma, String version, String carpeta, Long total, Long libre) {
        jdbc.update("UPDATE Nodos SET tokenHash=?, codigoHash=NULL, codigoExpira=NULL, equipo=?, plataforma=?, versionPrograma=?, carpeta=?, "
                + "espacioTotal=?, espacioLibre=?, ultimaConexion=NOW(3), fechaVinculacion=NOW(3) WHERE nodoId=?",
                tokenHash, equipo, plataforma, version, carpeta, total, libre, nodoId);
    }

    @Override
    public void latido(Long nodoId, String version, String carpeta, Long total, Long libre) {
        jdbc.update("UPDATE Nodos SET versionPrograma=COALESCE(?, versionPrograma), carpeta=COALESCE(?, carpeta), espacioTotal=COALESCE(?, espacioTotal), "
                + "espacioLibre=COALESCE(?, espacioLibre), ultimaConexion=NOW(3) WHERE nodoId=?", version, carpeta, total, libre, nodoId);
    }

    @Override
    public void tocar(Long nodoId) {
        jdbc.update("UPDATE Nodos SET ultimaConexion=NOW(3) WHERE nodoId=?", nodoId);
    }

    @Override
    public boolean conectado(Long nodoId) {
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM Nodos WHERE nodoId=? AND tokenHash IS NOT NULL AND ultimaConexion>NOW(3)-INTERVAL 75 SECOND",
                Long.class, nodoId);
        return total != null && total > 0;
    }

    @Override
    public List<Long> conectados(Long clienteId) {
        return jdbc.queryForList("SELECT nodoId FROM Nodos WHERE clienteId=? AND tokenHash IS NOT NULL AND ultimaConexion>NOW(3)-INTERVAL 75 SECOND",
                Long.class, clienteId);
    }

    @Override
    public List<Long> codigosVigentes(Long clienteId) {
        return jdbc.queryForList("SELECT nodoId FROM Nodos WHERE clienteId=? AND codigoHash IS NOT NULL AND codigoExpira>NOW(3)", Long.class, clienteId);
    }

    @Override
    public void desvincular(Long nodoId) {
        jdbc.update("UPDATE Nodos SET tokenHash=NULL, ultimaConexion=NULL WHERE nodoId=?", nodoId);
    }

    @Override
    public void delete(Long nodoId) {
        jdbc.update("UPDATE Clientes SET nodoPredeterminadoId=NULL WHERE nodoPredeterminadoId=?", nodoId);
        jdbc.update("DELETE FROM Archivos WHERE nodoId=?", nodoId);
        jdbc.update("DELETE FROM Nodos WHERE nodoId=?", nodoId);
    }

    @Override
    public void marcarSinPlan(Long nodoId) {
        jdbc.update("UPDATE Nodos SET sinPlanDesde=COALESCE(sinPlanDesde, NOW(3)) WHERE nodoId=?", nodoId);
    }

    @Override
    public void marcarNubeSinPlan(Long clienteId) {
        jdbc.update("UPDATE Clientes SET nubeSinPlanDesde=COALESCE(nubeSinPlanDesde, NOW(3)) WHERE clienteId=?", clienteId);
    }

    @Override
    public void limpiarNubeSinPlan(Long clienteId) {
        jdbc.update("UPDATE Clientes SET nubeSinPlanDesde=NULL WHERE clienteId=? AND nubeSinPlanDesde IS NOT NULL", clienteId);
    }

    @Override
    public java.sql.Timestamp nubeSinPlanDesde(Long clienteId) {
        List<java.sql.Timestamp> filas = jdbc.query("SELECT nubeSinPlanDesde FROM Clientes WHERE clienteId=?", (rs, i) -> rs.getTimestamp(1), clienteId);
        return filas.isEmpty() ? null : filas.get(0);
    }

    @Override
    public List<Long> clientesConNube() {
        return jdbc.queryForList("SELECT DISTINCT clienteId FROM Archivos WHERE nodoId IS NULL AND estado='Activo' "
                + "UNION SELECT clienteId FROM Clientes WHERE nubeSinPlanDesde IS NOT NULL", Long.class);
    }

    @Override
    public List<Long> clientesNubeVencida(int dias) {
        return jdbc.queryForList("SELECT clienteId FROM Clientes WHERE nubeSinPlanDesde IS NOT NULL AND nubeSinPlanDesde<NOW(3)-INTERVAL ? DAY",
                Long.class, dias);
    }

    @Override
    public void limpiarSinPlan(Long nodoId) {
        jdbc.update("UPDATE Nodos SET sinPlanDesde=NULL WHERE nodoId=? AND sinPlanDesde IS NOT NULL", nodoId);
    }

    @Override
    public List<Long> clientesConNodos() {
        return jdbc.queryForList("SELECT DISTINCT clienteId FROM Nodos WHERE estado='Activo'", Long.class);
    }

    @Override
    public List<Nodo> vencidosSinPlan(int dias) {
        return jdbc.query("SELECT * FROM Nodos WHERE estado='Activo' AND sinPlanDesde IS NOT NULL AND sinPlanDesde<NOW(3)-INTERVAL ? DAY", NODO, dias);
    }

    @Override
    public String nombreCliente(Long clienteId) {
        return jdbc.query("SELECT nombreComercial FROM Clientes WHERE clienteId=?", (rs, i) -> rs.getString(1), clienteId)
                .stream().findFirst().orElse(null);
    }

    @Override
    public Long predeterminado(Long clienteId) {
        return jdbc.query("SELECT c.nodoPredeterminadoId FROM Clientes c JOIN Nodos n ON n.nodoId=c.nodoPredeterminadoId AND n.estado='Activo' AND n.rol='Almacenamiento' "
                + "WHERE c.clienteId=?", (rs, i) -> rs.getLong(1), clienteId).stream().findFirst().orElse(null);
    }

    @Override
    public void predeterminar(Long clienteId, Long nodoId) {
        jdbc.update("UPDATE Clientes SET nodoPredeterminadoId=? WHERE clienteId=?", nodoId, clienteId);
    }

    @Override
    public List<Map<String, Object>> resumenNodos(Long clienteId) {
        return jdbc.queryForList("SELECT nodoId, COUNT(*) AS archivos, COALESCE(SUM(tamano),0) AS bytes, "
                + "SUM(CASE WHEN estadoNodo='Pendiente' THEN 1 ELSE 0 END) AS pendientes FROM Archivos WHERE clienteId=? AND nodoId IS NOT NULL "
                + "AND estado='Activo' GROUP BY nodoId", clienteId);
    }

    @Override
    public Map<String, Object> resumenNube(Long clienteId) {
        return jdbc.queryForMap("SELECT COUNT(*) AS archivos, COALESCE(SUM(tamano),0) AS bytes FROM Archivos WHERE clienteId=? AND nodoId IS NULL "
                + "AND estado='Activo'", clienteId);
    }

    @Override
    public long tareasPendientes(Long nodoId) {
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM NodosTareas WHERE nodoId=? AND estado IN ('Pendiente','Enviada')", Long.class, nodoId);
        return total == null ? 0 : total;
    }

    @Override
    public boolean existeDirectorio(Long nodoId, String almacen, String categoria) {
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM NodosDirectorios WHERE nodoId=? AND almacen=? AND categoria=?", Long.class,
                nodoId, almacen, categoria);
        return total != null && total > 0;
    }

    @Override
    public void crearDirectorio(Long nodoId, String almacen, String categoria) {
        jdbc.update("INSERT IGNORE INTO NodosDirectorios (nodoId, almacen, categoria) VALUES (?, ?, ?)", nodoId, almacen, categoria);
    }

    @Override
    public List<String> almacenes(Long nodoId) {
        return jdbc.queryForList("SELECT almacen FROM NodosDirectorios WHERE nodoId=? AND categoria='' ORDER BY almacen", String.class, nodoId);
    }

    @Override
    public List<Map<String, Object>> conteoCarpetas(Long nodoId) {
        return jdbc.queryForList("SELECT almacen, COUNT(*) AS carpetas FROM NodosDirectorios WHERE nodoId=? AND categoria<>'' GROUP BY almacen", nodoId);
    }

    @Override
    public List<String> carpetas(Long nodoId, String almacen) {
        return jdbc.queryForList("SELECT categoria FROM NodosDirectorios WHERE nodoId=? AND almacen=? AND categoria<>'' ORDER BY categoria",
                String.class, nodoId, almacen);
    }

    @Override
    public void renombrarAlmacen(Long nodoId, String almacen, String nombre, String actor) {
        jdbc.update("UPDATE NodosDirectorios SET almacen=? WHERE nodoId=? AND almacen=?", nombre, nodoId, almacen);
        jdbc.update("UPDATE Archivos SET almacen=?, fechaModificacion=NOW(3), fechaVersion=NOW(3), version=version+1, usuarioModificacionUuid=? "
                + "WHERE nodoId=? AND almacen=?", nombre, actor, nodoId, almacen);
    }

    @Override
    public void renombrarCarpeta(Long nodoId, String almacen, String categoria, String nombre, String actor) {
        jdbc.update("UPDATE NodosDirectorios SET categoria=? WHERE nodoId=? AND almacen=? AND categoria=?", nombre, nodoId, almacen, categoria);
        jdbc.update("UPDATE Archivos SET categoria=?, fechaModificacion=NOW(3), fechaVersion=NOW(3), version=version+1, usuarioModificacionUuid=? "
                + "WHERE nodoId=? AND almacen=? AND categoria=?", nombre, actor, nodoId, almacen, categoria);
    }

    @Override
    public void deleteAlmacen(Long nodoId, String almacen) {
        jdbc.update("DELETE FROM Archivos WHERE nodoId=? AND almacen=?", nodoId, almacen);
        jdbc.update("DELETE FROM NodosDirectorios WHERE nodoId=? AND almacen=?", nodoId, almacen);
    }

    @Override
    public void deleteCarpeta(Long nodoId, String almacen, String categoria) {
        jdbc.update("DELETE FROM Archivos WHERE nodoId=? AND almacen=? AND categoria=?", nodoId, almacen, categoria);
        jdbc.update("DELETE FROM NodosDirectorios WHERE nodoId=? AND almacen=? AND categoria=?", nodoId, almacen, categoria);
    }

    @Override
    public List<Map<String, Object>> resumenAlmacenes(Long nodoId) {
        return jdbc.queryForList("SELECT almacen, COUNT(*) AS archivos, COALESCE(SUM(tamano),0) AS bytes, "
                + "MAX(COALESCE(fechaModificacion, fechaCreacion)) AS ultimaModificacion FROM Archivos WHERE nodoId=? AND estado='Activo' "
                + "GROUP BY almacen", nodoId);
    }

    @Override
    public List<Map<String, Object>> resumenCarpetas(Long nodoId, String almacen) {
        return jdbc.queryForList("SELECT categoria, COUNT(*) AS archivos, COALESCE(SUM(tamano),0) AS bytes, "
                + "MAX(COALESCE(fechaModificacion, fechaCreacion)) AS ultimaModificacion FROM Archivos WHERE nodoId=? AND almacen=? "
                + "AND estado='Activo' GROUP BY categoria", nodoId, almacen);
    }

    @Override
    public PaginationResponse<Archivo> findAllPages(Long nodoId, String almacen, String categoria, String buscar, Integer page, Integer size) {
        int pagina = page == null || page < 1 ? 1 : page;
        int tamano = size == null || size < 1 ? 25 : Math.min(size, 200);
        String where = "WHERE nodoId=? AND almacen=? AND categoria=? AND estado='Activo' ";
        List<Object> argumentos = new ArrayList<>(List.of(nodoId, almacen, categoria));
        if (buscar != null && !buscar.isBlank()) {
            where += "AND (nombreOriginal LIKE ? OR identificador LIKE ? OR extension LIKE ?) ";
            String patron = "%" + buscar.trim() + "%";
            argumentos.add(patron);
            argumentos.add(patron);
            argumentos.add(patron);
        }
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM Archivos " + where, Long.class, argumentos.toArray());
        List<Object> argumentosPagina = new ArrayList<>(argumentos);
        argumentosPagina.add(tamano);
        argumentosPagina.add((pagina - 1) * tamano);
        List<Archivo> registros = jdbc.query("SELECT * FROM Archivos " + where + "ORDER BY fechaCreacion DESC, archivoId DESC LIMIT ? OFFSET ?",
                ARCHIVO, argumentosPagina.toArray());
        long totalRegistros = total == null ? 0 : total;
        int totalPaginas = (int) Math.ceil((double) totalRegistros / tamano);
        return new PaginationResponse<>(pagina, tamano, totalRegistros, totalPaginas, pagina > 1, pagina < totalPaginas, registros);
    }

    @Override
    public List<Archivo> findArchivos(Long nodoId, String almacen, String categoria) {
        return jdbc.query("SELECT * FROM Archivos WHERE nodoId=? AND almacen=? AND categoria=? AND estado='Activo' ORDER BY fechaCreacion DESC",
                ARCHIVO, nodoId, almacen, categoria);
    }

    @Override
    public List<Archivo> findArchivosAlmacen(Long nodoId, String almacen) {
        return jdbc.query("SELECT * FROM Archivos WHERE nodoId=? AND almacen=?", ARCHIVO, nodoId, almacen);
    }

    @Override
    public List<Archivo> findArchivosNodo(Long nodoId) {
        return jdbc.query("SELECT * FROM Archivos WHERE nodoId=?", ARCHIVO, nodoId);
    }

    @Override
    public Archivo findArchivo(Long nodoId, String almacen, String categoria, String identificador) {
        return jdbc.query("SELECT * FROM Archivos WHERE nodoId=? AND almacen=? AND categoria=? AND identificador=? AND estado='Activo'",
                ARCHIVO, nodoId, almacen, categoria, identificador).stream().findFirst().orElse(null);
    }

    @Override
    public Archivo findArchivoById(Long archivoId) {
        return jdbc.query("SELECT * FROM Archivos WHERE archivoId=? AND estado='Activo'", ARCHIVO, archivoId).stream().findFirst().orElse(null);
    }

    @Override
    public Archivo findArchivoPublico(Long clienteId, String almacen, String categoria, String identificador) {
        return jdbc.query("SELECT * FROM Archivos WHERE clienteId=? AND nodoId IS NOT NULL AND almacen=? AND categoria=? AND identificador=? "
                + "AND estado='Activo'", ARCHIVO, clienteId, almacen, categoria, identificador).stream().findFirst().orElse(null);
    }

    @Override
    public Long createArchivo(Long clienteId, Long nodoId, String almacen, String categoria, String identificador, String extension,
                              String nombreOriginal, long tamano, String hash, Long apiKeyId, String actor) {
        jdbc.update("INSERT INTO Archivos (archivoUuid, clienteId, clienteUuid, almacen, categoria, identificador, extension, nombreOriginal, "
                        + "tamano, apiKeyId, apiKeyUuid, usuarioCreacionUuid, nodoId, estadoNodo, hashSha256) SELECT ?, c.clienteId, c.clienteUuid, ?, ?, ?, ?, ?, ?, ?, "
                        + "(SELECT k.apiKeyUuid FROM ApiKeys k WHERE k.apiKeyId=?), ?, ?, 'Pendiente', ? FROM Clientes c WHERE c.clienteId=?",
                UUID.randomUUID().toString(), almacen, categoria, identificador, extension, nombreOriginal, tamano, apiKeyId, apiKeyId, actor,
                nodoId, hash, clienteId);
        return jdbc.queryForObject("SELECT archivoId FROM Archivos WHERE nodoId=? AND identificador=?", Long.class, nodoId, identificador);
    }

    @Override
    public void updateArchivo(Long archivoId, String extension, String nombreOriginal, long tamano, String hash, String actor) {
        jdbc.update("UPDATE Archivos SET extension=?, nombreOriginal=?, tamano=?, hashSha256=?, estadoNodo='Pendiente', fechaModificacion=NOW(3), "
                + "fechaVersion=NOW(3), version=version+1, usuarioModificacionUuid=? WHERE archivoId=?", extension, nombreOriginal, tamano, hash, actor,
                archivoId);
    }

    @Override
    public int marcarListo(Long archivoId, String hash) {
        return jdbc.update("UPDATE Archivos SET estadoNodo='Listo' WHERE archivoId=? AND hashSha256=?", archivoId, hash);
    }

    @Override
    public int updateNombre(Long nodoId, String almacen, String categoria, String identificador, String nombreOriginal, String actor) {
        return jdbc.update("UPDATE Archivos SET nombreOriginal=?, fechaModificacion=NOW(3), fechaVersion=NOW(3), version=version+1, "
                + "usuarioModificacionUuid=? WHERE nodoId=? AND almacen=? AND categoria=? AND identificador=? AND estado='Activo'",
                nombreOriginal, actor, nodoId, almacen, categoria, identificador);
    }

    @Override
    public void deleteArchivo(Long archivoId) {
        jdbc.update("DELETE FROM Archivos WHERE archivoId=?", archivoId);
    }

    @Override
    public Long nodoDeArchivo(Long clienteId, String almacen, String categoria, String identificador) {
        return jdbc.query("SELECT nodoId FROM Archivos WHERE clienteId=? AND almacen=? AND categoria=? AND identificador=? AND estado='Activo'",
                (rs, i) -> {
                    long valor = rs.getLong(1);
                    return rs.wasNull() ? 0L : valor;
                }, clienteId, almacen, categoria, identificador).stream().findFirst().orElse(null);
    }

    @Override
    public void crearTarea(Long nodoId, String tipo, Long archivoId, String ruta, String destino) {
        jdbc.update("INSERT INTO NodosTareas (nodoId, tipo, archivoId, ruta, destino) VALUES (?, ?, ?, ?, ?)", nodoId, tipo, archivoId, ruta, destino);
    }

    @Override
    public List<NodoTarea> tareasParaEnviar(Long nodoId, int limite) {
        return jdbc.query("SELECT * FROM NodosTareas WHERE nodoId=? AND (estado='Pendiente' OR (estado='Enviada' AND fechaEnvio<NOW(3)-INTERVAL 120 SECOND)) "
                + "ORDER BY nodoTareaId LIMIT ?", TAREA, nodoId, limite);
    }

    @Override
    public void marcarEnviadas(List<Long> tareas) {
        for (Long id : tareas) jdbc.update("UPDATE NodosTareas SET estado='Enviada', fechaEnvio=NOW(3) WHERE nodoTareaId=?", id);
    }

    @Override
    public NodoTarea findTarea(Long nodoId, Long nodoTareaId) {
        return jdbc.query("SELECT * FROM NodosTareas WHERE nodoId=? AND nodoTareaId=?", TAREA, nodoId, nodoTareaId).stream().findFirst().orElse(null);
    }

    @Override
    public void terminarTarea(Long nodoTareaId, String estado, String error) {
        jdbc.update("UPDATE NodosTareas SET estado=?, error=?, fechaFin=NOW(3) WHERE nodoTareaId=?", estado, error, nodoTareaId);
    }

    @Override
    public void fallarTarea(Long nodoTareaId, String error) {
        jdbc.update("UPDATE NodosTareas SET estado='Enviada', intentos=intentos+1, error=?, fechaEnvio=NOW(3) WHERE nodoTareaId=?",
                error == null ? null : error.substring(0, Math.min(error.length(), 1000)), nodoTareaId);
    }
}

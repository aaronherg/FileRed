package com.aarondevs.filered.DAO.Archivos;

import com.aarondevs.filered.Models.Archivos.Archivo;
import com.aarondevs.filered.Parameters.Response.PaginationResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ArchivoDAO implements ArchivoDataSet {
    private static final Set<String> METRICAS = Set.of("solicitudesApi", "archivosSubidos", "bytesSubidos", "archivosEliminados",
            "descargas", "bytesDescargados");

    private final JdbcTemplate jdbc;

    public ArchivoDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Archivo findByRuta(Long clienteId, String almacen, String categoria, String identificador) {
        return jdbc.query("SELECT * FROM Archivos WHERE clienteId=? AND almacen=? AND categoria=? AND identificador=? AND estado='Activo'",
                new BeanPropertyRowMapper<>(Archivo.class), clienteId, almacen, categoria, identificador).stream().findFirst().orElse(null);
    }

    @Override
    public List<Archivo> findAll(Long clienteId, String almacen, String categoria) {
        return jdbc.query("SELECT * FROM Archivos WHERE clienteId=? AND almacen=? AND categoria=? AND estado='Activo' ORDER BY fechaCreacion DESC",
                new BeanPropertyRowMapper<>(Archivo.class), clienteId, almacen, categoria);
    }

    @Override
    public void create(Long clienteId, String almacen, String categoria, String identificador, String extension, String nombreOriginal,
                       long tamano, Long apiKeyId, String actor) {
        jdbc.update("INSERT INTO Archivos (archivoUuid, clienteId, clienteUuid, almacen, categoria, identificador, extension, nombreOriginal, "
                        + "tamano, apiKeyId, apiKeyUuid, usuarioCreacionUuid) SELECT ?, c.clienteId, c.clienteUuid, ?, ?, ?, ?, ?, ?, ?, "
                        + "(SELECT k.apiKeyUuid FROM ApiKeys k WHERE k.apiKeyId=?), ? FROM Clientes c WHERE c.clienteId=?",
                UUID.randomUUID().toString(), almacen, categoria, identificador, extension, nombreOriginal, tamano, apiKeyId, apiKeyId, actor,
                clienteId);
    }

    @Override
    public void update(Long archivoId, String extension, String nombreOriginal, long tamano, String actor) {
        jdbc.update("UPDATE Archivos SET extension=?, nombreOriginal=?, tamano=?, fechaModificacion=NOW(3), fechaVersion=NOW(3), "
                + "version=version+1, usuarioModificacionUuid=? WHERE archivoId=?", extension, nombreOriginal, tamano, actor, archivoId);
    }

    @Override
    public int delete(Long clienteId, String almacen, String categoria, String identificador) {
        return jdbc.update("DELETE FROM Archivos WHERE clienteId=? AND almacen=? AND categoria=? AND identificador=?",
                clienteId, almacen, categoria, identificador);
    }

    @Override
    public int deleteCategoria(Long clienteId, String almacen, String categoria) {
        return jdbc.update("DELETE FROM Archivos WHERE clienteId=? AND almacen=? AND categoria=?", clienteId, almacen, categoria);
    }

    @Override
    public String clienteUuid(Long clienteId) {
        return jdbc.query("SELECT clienteUuid FROM Clientes WHERE clienteId=?", (rs, i) -> rs.getString(1), clienteId)
                .stream().findFirst().orElse(null);
    }

    @Override
    public Long clienteActivoPorUuid(String clienteUuid) {
        return jdbc.query("SELECT clienteId FROM Clientes WHERE clienteUuid=? AND estado='Activo'", (rs, i) -> rs.getLong(1), clienteUuid)
                .stream().findFirst().orElse(null);
    }

    @Override
    public void sumarMetrica(Long clienteId, String campo, long cantidad) {
        if (!METRICAS.contains(campo)) throw new IllegalArgumentException("Métrica no válida");
        jdbc.update("INSERT INTO MetricasClientesDiarias (metricaClienteDiariaUuid, clienteId, clienteUuid, fecha, " + campo + ") "
                        + "SELECT ?, c.clienteId, c.clienteUuid, CURRENT_DATE, ? FROM Clientes c WHERE c.clienteId=? "
                        + "ON DUPLICATE KEY UPDATE " + campo + "=" + campo + "+VALUES(" + campo + ")",
                UUID.randomUUID().toString(), cantidad, clienteId);
    }

    @Override
    public PaginationResponse<Archivo> findAllPages(Long clienteId, String almacen, String categoria, String buscar, Integer page, Integer size) {
        int pagina = page == null || page < 1 ? 1 : page;
        int tamano = size == null || size < 1 ? 25 : Math.min(size, 200);
        String where = "WHERE clienteId=? AND almacen=? AND categoria=? AND estado='Activo' ";
        List<Object> argumentos = new ArrayList<>(List.of(clienteId, almacen, categoria));
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
                new BeanPropertyRowMapper<>(Archivo.class), argumentosPagina.toArray());
        long totalRegistros = total == null ? 0 : total;
        int totalPaginas = (int) Math.ceil((double) totalRegistros / tamano);
        return new PaginationResponse<>(pagina, tamano, totalRegistros, totalPaginas, pagina > 1, pagina < totalPaginas, registros);
    }

    @Override
    public List<Map<String, Object>> resumenAlmacenes(Long clienteId) {
        return jdbc.queryForList("SELECT almacen, COUNT(DISTINCT categoria) AS carpetas, COUNT(*) AS archivos, COALESCE(SUM(tamano),0) AS bytes, "
                + "MAX(COALESCE(fechaModificacion, fechaCreacion)) AS ultimaModificacion FROM Archivos WHERE clienteId=? AND estado='Activo' "
                + "GROUP BY almacen", clienteId);
    }

    @Override
    public List<Map<String, Object>> resumenCarpetas(Long clienteId, String almacen) {
        return jdbc.queryForList("SELECT categoria, COUNT(*) AS archivos, COALESCE(SUM(tamano),0) AS bytes, "
                + "MAX(COALESCE(fechaModificacion, fechaCreacion)) AS ultimaModificacion FROM Archivos WHERE clienteId=? AND almacen=? "
                + "AND estado='Activo' GROUP BY categoria", clienteId, almacen);
    }

    @Override
    public int renombrarAlmacen(Long clienteId, String almacen, String nombre, String actor) {
        return jdbc.update("UPDATE Archivos SET almacen=?, fechaModificacion=NOW(3), fechaVersion=NOW(3), version=version+1, "
                + "usuarioModificacionUuid=? WHERE clienteId=? AND almacen=?", nombre, actor, clienteId, almacen);
    }

    @Override
    public int renombrarCategoria(Long clienteId, String almacen, String categoria, String nombre, String actor) {
        return jdbc.update("UPDATE Archivos SET categoria=?, fechaModificacion=NOW(3), fechaVersion=NOW(3), version=version+1, "
                + "usuarioModificacionUuid=? WHERE clienteId=? AND almacen=? AND categoria=?", nombre, actor, clienteId, almacen, categoria);
    }

    @Override
    public int deleteAlmacen(Long clienteId, String almacen) {
        return jdbc.update("DELETE FROM Archivos WHERE clienteId=? AND almacen=?", clienteId, almacen);
    }

    @Override
    public int updateNombre(Long clienteId, String almacen, String categoria, String identificador, String nombreOriginal, String actor) {
        return jdbc.update("UPDATE Archivos SET nombreOriginal=?, fechaModificacion=NOW(3), fechaVersion=NOW(3), version=version+1, "
                + "usuarioModificacionUuid=? WHERE clienteId=? AND almacen=? AND categoria=? AND identificador=? AND estado='Activo'",
                nombreOriginal, actor, clienteId, almacen, categoria, identificador);
    }
}

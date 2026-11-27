package com.aarondevs.filered.DAO.Archivos;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RespaldoDAO implements RespaldoDataSet {
    private static final String ORIGEN = "((?=0 AND a.nodoId IS NULL) OR a.nodoId=?)";

    private final JdbcTemplate jdbc;

    public RespaldoDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Long> respaldosDeOrigen(Long clienteId, long origenId) {
        return jdbc.queryForList("SELECT o.respaldoId FROM RespaldosOrigenes o JOIN Nodos n ON n.nodoId=o.respaldoId AND n.estado='Activo' "
                + "AND n.rol='Respaldo' WHERE n.clienteId=? AND o.origenNodoId=? ORDER BY o.respaldoId", Long.class, clienteId, origenId);
    }

    @Override
    public List<Long> origenes(Long respaldoId) {
        return jdbc.queryForList("SELECT origenNodoId FROM RespaldosOrigenes WHERE respaldoId=? ORDER BY origenNodoId", Long.class, respaldoId);
    }

    @Override
    public List<Map<String, Object>> origenesCliente(Long clienteId) {
        return jdbc.queryForList("SELECT o.respaldoId, o.origenNodoId FROM RespaldosOrigenes o JOIN Nodos n ON n.nodoId=o.respaldoId "
                + "WHERE n.clienteId=? AND n.estado='Activo' ORDER BY o.origenNodoId", clienteId);
    }

    @Override
    public void agregarOrigen(Long respaldoId, long origenId) {
        jdbc.update("INSERT IGNORE INTO RespaldosOrigenes (respaldoId, origenNodoId) VALUES (?, ?)", respaldoId, origenId);
    }

    @Override
    public void quitarOrigen(Long respaldoId, long origenId) {
        jdbc.update("DELETE FROM RespaldosOrigenes WHERE respaldoId=? AND origenNodoId=?", respaldoId, origenId);
    }

    @Override
    public int sembrar(Long respaldoId, Long clienteId, long origenId) {
        return jdbc.update("INSERT INTO NodosTareas (nodoId, tipo, archivoId) SELECT ?, 'COPIAR', a.archivoId FROM Archivos a "
                + "WHERE a.clienteId=? AND a.estado='Activo' AND " + ORIGEN + " ORDER BY a.archivoId", respaldoId, clienteId, origenId, origenId);
    }

    @Override
    public void olvidarCopiasDeOrigen(Long respaldoId, Long clienteId, long origenId) {
        jdbc.update("DELETE r FROM ArchivosRespaldos r JOIN Archivos a ON a.archivoId=r.archivoId WHERE r.respaldoId=? AND a.clienteId=? AND "
                + ORIGEN, respaldoId, clienteId, origenId, origenId);
        jdbc.update("UPDATE NodosTareas t JOIN Archivos a ON a.archivoId=t.archivoId SET t.estado='Cancelada', t.fechaFin=NOW(3) "
                + "WHERE t.nodoId=? AND t.tipo='COPIAR' AND t.estado IN ('Pendiente','Enviada') AND a.clienteId=? AND " + ORIGEN,
                respaldoId, clienteId, origenId, origenId);
    }

    @Override
    public boolean tieneCopia(Long archivoId, Long respaldoId, String hash) {
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM ArchivosRespaldos WHERE archivoId=? AND respaldoId=? AND hashSha256=?", Long.class,
                archivoId, respaldoId, hash);
        return total != null && total > 0;
    }

    @Override
    public void marcarCopia(Long archivoId, Long respaldoId, String hash) {
        jdbc.update("INSERT INTO ArchivosRespaldos (archivoId, respaldoId, hashSha256) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE hashSha256=VALUES(hashSha256), fechaCopia=NOW(3)", archivoId, respaldoId, hash);
    }

    @Override
    public void olvidarCopias(Long archivoId) {
        jdbc.update("DELETE FROM ArchivosRespaldos WHERE archivoId=?", archivoId);
    }

    @Override
    public List<Map<String, Object>> progreso(Long clienteId) {
        return jdbc.queryForList("SELECT o.respaldoId, COUNT(a.archivoId) AS total, COALESCE(SUM(a.tamano),0) AS bytes, "
                + "SUM(CASE WHEN r.hashSha256 IS NOT NULL AND r.hashSha256=a.hashSha256 THEN 1 ELSE 0 END) AS listos, MAX(r.fechaCopia) AS ultimaCopia "
                + "FROM RespaldosOrigenes o JOIN Nodos n ON n.nodoId=o.respaldoId AND n.clienteId=? AND n.estado='Activo' "
                + "JOIN Archivos a ON a.clienteId=n.clienteId AND a.estado='Activo' AND ((o.origenNodoId=0 AND a.nodoId IS NULL) OR a.nodoId=o.origenNodoId) "
                + "LEFT JOIN ArchivosRespaldos r ON r.archivoId=a.archivoId AND r.respaldoId=o.respaldoId GROUP BY o.respaldoId", clienteId);
    }

    @Override
    public List<Map<String, Object>> respaldadosPorOrigen(Long clienteId) {
        return jdbc.queryForList("SELECT COALESCE(a.nodoId,0) AS origenNodoId, COUNT(DISTINCT a.archivoId) AS respaldados FROM Archivos a "
                + "JOIN ArchivosRespaldos r ON r.archivoId=a.archivoId AND r.hashSha256=a.hashSha256 WHERE a.clienteId=? AND a.estado='Activo' "
                + "GROUP BY COALESCE(a.nodoId,0)", clienteId);
    }

    @Override
    public Long respaldoConectadoConCopia(Long archivoId, String hash) {
        return jdbc.query("SELECT r.respaldoId FROM ArchivosRespaldos r JOIN Nodos n ON n.nodoId=r.respaldoId WHERE r.archivoId=? AND r.hashSha256=? "
                + "AND n.estado='Activo' AND n.tokenHash IS NOT NULL AND n.ultimaConexion>NOW(3)-INTERVAL 75 SECOND ORDER BY n.ultimaConexion DESC LIMIT 1",
                (rs, i) -> rs.getLong(1), archivoId, hash).stream().findFirst().orElse(null);
    }

    @Override
    public void actualizarHash(Long archivoId, String hash) {
        jdbc.update("UPDATE Archivos SET hashSha256=? WHERE archivoId=?", hash, archivoId);
    }

    @Override
    public void moverANube(Long archivoId) {
        jdbc.update("UPDATE Archivos SET nodoId=NULL, estadoNodo=NULL WHERE archivoId=?", archivoId);
    }
}

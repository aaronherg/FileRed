package com.aarondevs.filered.DAO.General;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReporteDAO implements ReporteDataSet {
    private static final String PAGOS = "FROM PagosSuscripcionesDetalles pd JOIN PagosSuscripciones g ON g.pagoSuscripcionId=pd.pagoSuscripcionId "
            + "JOIN DetallesSuscripciones d ON d.detalleSuscripcionId=g.detalleSuscripcionId JOIN Suscripciones s ON s.suscripcionId=d.suscripcionId "
            + "JOIN Clientes c ON c.clienteId=s.clienteId JOIN Planes p ON p.planId=d.planId JOIN Monedas m ON m.monedaId=g.monedaId "
            + "WHERE pd.estado='Activo' AND pd.fechaPago>=? AND pd.fechaPago<? ";
    private static final String PLAN_VIGENTE = "SELECT p.%s FROM Suscripciones s2 JOIN DetallesSuscripciones d2 ON d2.suscripcionId=s2.suscripcionId "
            + "AND d2.estado='Activo' AND d2.fechaInicio<=NOW(3) AND d2.fechaFin>NOW(3) JOIN Planes p ON p.planId=d2.planId "
            + "WHERE s2.clienteId=c.clienteId ORDER BY d2.fechaInicio DESC LIMIT 1";

    private final JdbcTemplate jdbc;

    public ReporteDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private Object[] rango(LocalDate desde, LocalDate hasta) {
        return new Object[] {Date.valueOf(desde), Date.valueOf(hasta.plusDays(1))};
    }

    @Override
    public List<Map<String, Object>> ingresosDetalle(LocalDate desde, LocalDate hasta) {
        return jdbc.queryForList("SELECT pd.fechaPago, pd.monto, pd.metodo, pd.referencia, c.nombreComercial AS clienteNombre, "
                + "p.nombre AS planNombre, m.simbolo AS monedaSimbolo " + PAGOS + "ORDER BY pd.fechaPago DESC LIMIT 1000", rango(desde, hasta));
    }

    @Override
    public List<Map<String, Object>> ingresosPorDia(LocalDate desde, LocalDate hasta) {
        return jdbc.queryForList("SELECT DATE(pd.fechaPago) AS fecha, SUM(pd.monto) AS monto, COUNT(*) AS pagos " + PAGOS
                + "GROUP BY DATE(pd.fechaPago) ORDER BY fecha", rango(desde, hasta));
    }

    @Override
    public List<Map<String, Object>> ingresosPorMetodo(LocalDate desde, LocalDate hasta) {
        return jdbc.queryForList("SELECT pd.metodo, SUM(pd.monto) AS monto, COUNT(*) AS pagos " + PAGOS
                + "GROUP BY pd.metodo ORDER BY monto DESC", rango(desde, hasta));
    }

    @Override
    public List<Map<String, Object>> cuentasPorCobrar() {
        return jdbc.queryForList("SELECT g.pagoSuscripcionId, c.clienteId, c.nombreComercial AS clienteNombre, p.nombre AS planNombre, "
                + "d.fechaInicio, d.fechaFin, g.montoTotal, g.montoPagado, (g.montoTotal-g.montoPagado) AS montoPendiente, g.fechaVencimiento, "
                + "DATEDIFF(NOW(), g.fechaVencimiento) AS diasVencido, m.simbolo AS monedaSimbolo FROM PagosSuscripciones g "
                + "JOIN DetallesSuscripciones d ON d.detalleSuscripcionId=g.detalleSuscripcionId JOIN Suscripciones s ON s.suscripcionId=d.suscripcionId "
                + "JOIN Clientes c ON c.clienteId=s.clienteId JOIN Planes p ON p.planId=d.planId JOIN Monedas m ON m.monedaId=g.monedaId "
                + "WHERE g.estado='Pendiente' ORDER BY g.fechaVencimiento");
    }

    @Override
    public List<Map<String, Object>> consumoClientes(String buscar) {
        String sql = "SELECT c.clienteId, c.nombreComercial AS clienteNombre, c.estado, "
                + "(" + PLAN_VIGENTE.formatted("nombre") + ") AS planNombre, "
                + "(" + PLAN_VIGENTE.formatted("espacioGb") + ") AS espacioGb, "
                + "(" + PLAN_VIGENTE.formatted("tamanoMaximoArchivoMb") + ") AS tamanoMaximoArchivoMb, "
                + "(SELECT COALESCE(SUM(a.tamano),0) FROM Archivos a WHERE a.clienteId=c.clienteId AND a.estado='Activo') AS bytes, "
                + "(SELECT COUNT(*) FROM Archivos a WHERE a.clienteId=c.clienteId AND a.estado='Activo') AS archivos, "
                + "(SELECT COUNT(DISTINCT a.almacen) FROM Archivos a WHERE a.clienteId=c.clienteId AND a.estado='Activo') AS almacenes, "
                + "(SELECT COALESCE(SUM(x.solicitudesApi),0) FROM MetricasClientesDiarias x WHERE x.clienteId=c.clienteId "
                + "AND x.fecha>=DATE_SUB(CURRENT_DATE, INTERVAL 29 DAY)) AS solicitudes30 "
                + "FROM Clientes c WHERE c.estado<>'Eliminado' ";
        List<Object> argumentos = new ArrayList<>();
        if (buscar != null && !buscar.isBlank()) {
            sql += "AND (c.nombreComercial LIKE ? OR c.documento LIKE ?) ";
            argumentos.add("%" + buscar.trim() + "%");
            argumentos.add("%" + buscar.trim() + "%");
        }
        return jdbc.queryForList(sql + "ORDER BY bytes DESC, c.nombreComercial LIMIT 500", argumentos.toArray());
    }

    @Override
    public List<Map<String, Object>> usoApiPorCliente(LocalDate desde, LocalDate hasta) {
        return jdbc.queryForList("SELECT c.clienteId, c.nombreComercial AS clienteNombre, SUM(x.solicitudesApi) AS solicitudesApi, "
                + "SUM(x.archivosSubidos) AS archivosSubidos, SUM(x.bytesSubidos) AS bytesSubidos, SUM(x.archivosEliminados) AS archivosEliminados, "
                + "SUM(x.descargas) AS descargas, SUM(x.bytesDescargados) AS bytesDescargados FROM MetricasClientesDiarias x "
                + "JOIN Clientes c ON c.clienteId=x.clienteId WHERE x.fecha>=? AND x.fecha<=? GROUP BY c.clienteId, c.nombreComercial "
                + "ORDER BY solicitudesApi DESC", Date.valueOf(desde), Date.valueOf(hasta));
    }

    @Override
    public List<Map<String, Object>> usoApiPorDia(Long clienteId, LocalDate desde, LocalDate hasta) {
        String filtro = clienteId == null ? "" : "AND clienteId=? ";
        List<Object> argumentos = new ArrayList<>(List.of(Date.valueOf(desde), Date.valueOf(hasta)));
        if (clienteId != null) argumentos.add(clienteId);
        return jdbc.queryForList("SELECT fecha, SUM(solicitudesApi) AS solicitudesApi, SUM(archivosSubidos) AS archivosSubidos, "
                + "SUM(bytesSubidos) AS bytesSubidos, SUM(archivosEliminados) AS archivosEliminados, SUM(descargas) AS descargas, "
                + "SUM(bytesDescargados) AS bytesDescargados FROM MetricasClientesDiarias WHERE fecha>=? AND fecha<=? " + filtro
                + "GROUP BY fecha ORDER BY fecha", argumentos.toArray());
    }

    @Override
    public List<Map<String, Object>> consumoPorCarpeta(Long clienteId) {
        return jdbc.queryForList("SELECT almacen, categoria, COUNT(*) AS archivos, COALESCE(SUM(tamano),0) AS bytes, "
                + "MAX(COALESCE(fechaModificacion, fechaCreacion)) AS ultimaModificacion FROM Archivos WHERE clienteId=? AND estado='Activo' "
                + "GROUP BY almacen, categoria ORDER BY bytes DESC", clienteId);
    }

    @Override
    public List<Map<String, Object>> consumoPorTipo(Long clienteId) {
        return jdbc.queryForList("SELECT extension, COUNT(*) AS archivos, COALESCE(SUM(tamano),0) AS bytes FROM Archivos "
                + "WHERE clienteId=? AND estado='Activo' GROUP BY extension ORDER BY bytes DESC", clienteId);
    }

    @Override
    public List<Map<String, Object>> archivosMasGrandes(Long clienteId, int limite) {
        return jdbc.queryForList("SELECT almacen, categoria, identificador, extension, nombreOriginal, tamano, fechaCreacion FROM Archivos "
                + "WHERE clienteId=? AND estado='Activo' ORDER BY tamano DESC LIMIT ?", clienteId, limite);
    }

    @Override
    public List<Map<String, Object>> usoApiKeys(Long clienteId) {
        return jdbc.queryForList("SELECT k.nombre, k.prefijo, k.estado, k.fechaUltimoUso, (SELECT COUNT(*) FROM Archivos a WHERE a.apiKeyId=k.apiKeyId "
                + "AND a.estado='Activo') AS archivos FROM ApiKeys k WHERE k.clienteId=? AND k.estado<>'Eliminado' ORDER BY k.fechaUltimoUso DESC",
                clienteId);
    }
}

package com.aarondevs.filered.Services.General;

import com.aarondevs.filered.DAO.General.ReporteDataSet;
import com.aarondevs.filered.Parameters.General.ReporteParameters;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ReporteServiceImpl implements ReporteService {
    private static final int MAXIMO_DIAS = 366;

    private final ReporteDataSet datos;

    public ReporteServiceImpl(ReporteDataSet datos) {
        this.datos = datos;
    }

    @Override
    public Map<String, Object> ingresos(ReporteParameters parametro) {
        LocalDate[] rango = rango(parametro);
        List<Map<String, Object>> detalle = datos.ingresosDetalle(rango[0], rango[1]);
        List<Map<String, Object>> porDia = datos.ingresosPorDia(rango[0], rango[1]);
        Map<String, Object> resultado = encabezado(rango);
        resultado.put("total", sumar(porDia, "monto"));
        resultado.put("pagos", porDia.stream().mapToLong(f -> numero(f.get("pagos")).longValue()).sum());
        resultado.put("porDia", porDia);
        resultado.put("porMetodo", datos.ingresosPorMetodo(rango[0], rango[1]));
        resultado.put("detalle", detalle);
        return resultado;
    }

    @Override
    public Map<String, Object> cuentasPorCobrar() {
        List<Map<String, Object>> cargos = datos.cuentasPorCobrar();
        Map<String, BigDecimal> tramos = new LinkedHashMap<>();
        for (String tramo : List.of("Por vencer", "1 a 30 días", "31 a 60 días", "61 a 90 días", "Más de 90 días")) tramos.put(tramo, BigDecimal.ZERO);
        for (Map<String, Object> cargo : cargos) {
            long dias = numero(cargo.get("diasVencido")).longValue();
            String tramo = dias <= 0 ? "Por vencer" : dias <= 30 ? "1 a 30 días" : dias <= 60 ? "31 a 60 días" : dias <= 90 ? "61 a 90 días" : "Más de 90 días";
            cargo.put("tramo", tramo);
            tramos.merge(tramo, decimal(cargo.get("montoPendiente")), BigDecimal::add);
        }
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("total", sumar(cargos, "montoPendiente"));
        resultado.put("cargos", cargos.size());
        resultado.put("vencido", cargos.stream().filter(c -> numero(c.get("diasVencido")).longValue() > 0)
                .map(c -> decimal(c.get("montoPendiente"))).reduce(BigDecimal.ZERO, BigDecimal::add));
        resultado.put("tramos", tramos.entrySet().stream().map(e -> Map.of("tramo", (Object) e.getKey(), "monto", e.getValue())).toList());
        resultado.put("detalle", cargos);
        return resultado;
    }

    @Override
    public Map<String, Object> consumoClientes(ReporteParameters parametro) {
        List<Map<String, Object>> clientes = datos.consumoClientes(parametro == null ? null : parametro.getBuscar());
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("bytes", clientes.stream().mapToLong(f -> numero(f.get("bytes")).longValue()).sum());
        resultado.put("archivos", clientes.stream().mapToLong(f -> numero(f.get("archivos")).longValue()).sum());
        resultado.put("clientes", clientes.size());
        resultado.put("detalle", clientes);
        return resultado;
    }

    @Override
    public Map<String, Object> usoApi(ReporteParameters parametro) {
        LocalDate[] rango = rango(parametro);
        Map<String, Object> resultado = encabezado(rango);
        List<Map<String, Object>> porDia = datos.usoApiPorDia(null, rango[0], rango[1]);
        resultado.put("totales", totales(porDia));
        resultado.put("porDia", porDia);
        resultado.put("detalle", datos.usoApiPorCliente(rango[0], rango[1]));
        return resultado;
    }

    @Override
    public Map<String, Object> consumoCliente(Long clienteId) {
        List<Map<String, Object>> carpetas = datos.consumoPorCarpeta(clienteId);
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("bytes", carpetas.stream().mapToLong(f -> numero(f.get("bytes")).longValue()).sum());
        resultado.put("archivos", carpetas.stream().mapToLong(f -> numero(f.get("archivos")).longValue()).sum());
        resultado.put("porCarpeta", carpetas);
        resultado.put("porTipo", datos.consumoPorTipo(clienteId));
        resultado.put("masGrandes", datos.archivosMasGrandes(clienteId, 10));
        return resultado;
    }

    @Override
    public Map<String, Object> actividadCliente(Long clienteId, ReporteParameters parametro) {
        LocalDate[] rango = rango(parametro);
        List<Map<String, Object>> porDia = datos.usoApiPorDia(clienteId, rango[0], rango[1]);
        Map<String, Object> resultado = encabezado(rango);
        resultado.put("totales", totales(porDia));
        resultado.put("porDia", porDia);
        resultado.put("apiKeys", datos.usoApiKeys(clienteId));
        return resultado;
    }

    private LocalDate[] rango(ReporteParameters parametro) {
        LocalDate hasta = parametro == null || parametro.getHasta() == null ? LocalDate.now() : parametro.getHasta();
        LocalDate desde = parametro == null || parametro.getDesde() == null ? hasta.minusDays(29) : parametro.getDesde();
        if (desde.isAfter(hasta)) throw new IllegalArgumentException("La fecha desde no puede ser mayor que la fecha hasta");
        if (ChronoUnit.DAYS.between(desde, hasta) > MAXIMO_DIAS) throw new IllegalArgumentException("El rango máximo es de un año");
        return new LocalDate[] {desde, hasta};
    }

    private Map<String, Object> encabezado(LocalDate[] rango) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("desde", rango[0]);
        resultado.put("hasta", rango[1]);
        return resultado;
    }

    private Map<String, Object> totales(List<Map<String, Object>> filas) {
        Map<String, Object> totales = new LinkedHashMap<>();
        for (String campo : List.of("solicitudesApi", "archivosSubidos", "bytesSubidos", "archivosEliminados", "descargas", "bytesDescargados")) {
            totales.put(campo, filas.stream().mapToLong(f -> numero(f.get(campo)).longValue()).sum());
        }
        return totales;
    }

    private BigDecimal sumar(List<Map<String, Object>> filas, String campo) {
        return filas.stream().map(f -> decimal(f.get(campo))).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal decimal(Object valor) {
        if (valor instanceof BigDecimal d) return d;
        return valor instanceof Number n ? BigDecimal.valueOf(n.doubleValue()) : BigDecimal.ZERO;
    }

    private Number numero(Object valor) {
        return valor instanceof Number n ? n : 0;
    }
}

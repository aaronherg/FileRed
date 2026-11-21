package com.aarondevs.filered.Services.General;

import com.aarondevs.filered.Parameters.General.ReporteParameters;
import java.util.Map;

public interface ReporteService {
    Map<String, Object> ingresos(ReporteParameters parametro);

    Map<String, Object> cuentasPorCobrar();

    Map<String, Object> consumoClientes(ReporteParameters parametro);

    Map<String, Object> usoApi(ReporteParameters parametro);

    Map<String, Object> consumoCliente(Long clienteId);

    Map<String, Object> actividadCliente(Long clienteId, ReporteParameters parametro);
}

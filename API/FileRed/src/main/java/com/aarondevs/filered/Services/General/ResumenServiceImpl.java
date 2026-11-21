package com.aarondevs.filered.Services.General;

import com.aarondevs.filered.DAO.General.ResumenDataSet;
import com.aarondevs.filered.Services.Suscripciones.SuscripcionService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ResumenServiceImpl implements ResumenService {
    private final ResumenDataSet datos;
    private final SuscripcionService suscripciones;

    public ResumenServiceImpl(ResumenDataSet datos, SuscripcionService suscripciones) {
        this.datos = datos;
        this.suscripciones = suscripciones;
    }

    @Override
    public Map<String, Object> administracion() {
        Map<String, Object> resultado = new LinkedHashMap<>(datos.totalesAdministracion());
        resultado.put("planes", datos.distribucionPlanes());
        resultado.put("ultimosPagos", datos.ultimosPagos(8));
        resultado.put("ingresosMensuales", datos.ingresosMensuales(6));
        return resultado;
    }

    @Override
    public Map<String, Object> cliente(Long clienteId) {
        Map<String, Object> resultado = new LinkedHashMap<>(datos.totalesCliente(clienteId));
        resultado.putAll(suscripciones.consumo(clienteId));
        resultado.put("metricas", datos.metricasCliente(clienteId, 30));
        return resultado;
    }
}

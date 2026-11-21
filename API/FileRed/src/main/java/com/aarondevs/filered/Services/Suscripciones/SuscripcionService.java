package com.aarondevs.filered.Services.Suscripciones;

import com.aarondevs.filered.Models.Suscripciones.DetalleSuscripcion;
import com.aarondevs.filered.Models.Suscripciones.Suscripcion;
import com.aarondevs.filered.Parameters.Suscripciones.SuscripcionAsignarParameters;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SuscripcionService {
    String PREPAGO = "PREPAGO";
    String POSTPAGO = "POSTPAGO";
    BigDecimal BYTES_POR_GB = BigDecimal.valueOf(1024L * 1024 * 1024);

    static long limiteBytes(BigDecimal espacioGb) {
        return espacioGb == null ? 0 : espacioGb.multiply(BYTES_POR_GB).longValue();
    }

    Suscripcion asignar(SuscripcionAsignarParameters parametro, String actor, Long usuarioAdministracionId);

    int cancelar(Long clienteId, String actor);

    Suscripcion cliente(Long clienteId);

    Object pages(Integer page, Integer size, String buscar);

    DetalleSuscripcion vigente(Long clienteId);

    Object historial(Long clienteId);

    long espacioUsado(Long clienteId);

    Map<String, Object> consumo(Long clienteId);

    void renovarCadena(DetalleSuscripcion vencido, LocalDateTime limite);

    List<DetalleSuscripcion> pendientesRenovacion(LocalDateTime limite);
}

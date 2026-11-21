package com.aarondevs.filered.DAO.Suscripciones;

import com.aarondevs.filered.Models.Suscripciones.DetalleSuscripcion;
import com.aarondevs.filered.Models.Suscripciones.Suscripcion;
import com.aarondevs.filered.Parameters.Response.PaginationResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface SuscripcionDataSet {
    Suscripcion findByClienteId(Long clienteId);

    PaginationResponse<Suscripcion> findAllPages(Integer page, Integer size, String buscar);

    DetalleSuscripcion findVigente(Long clienteId);

    DetalleSuscripcion findDetalleById(Long detalleSuscripcionId);

    List<DetalleSuscripcion> findDetalles(Long clienteId);

    int cerrarVigente(Long clienteId, String estado, String actor);

    Long createDetalle(Long suscripcionId, Long planId, LocalDateTime inicio, LocalDateTime fin, String tipoCobro,
                       boolean permitirAdicionales, boolean renovacionAutomatica, Long renovacionOrigenDetalleId, String actor);

    Long createPago(Long detalleSuscripcionId, BigDecimal montoPlan, LocalDateTime fechaVencimiento, String actor);

    List<DetalleSuscripcion> findPendientesRenovacion(LocalDateTime limite);

    boolean existsRenovacion(Long detalleSuscripcionId);

    long espacioUsadoBytes(Long clienteId);

    void registrarAdicionales(Long detalleSuscripcionId, BigDecimal gbAdicionales, BigDecimal montoAdicionales);
}

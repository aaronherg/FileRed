package com.aarondevs.filered.DAO.Suscripciones;

import com.aarondevs.filered.Models.Suscripciones.PagoSuscripcion;
import com.aarondevs.filered.Models.Suscripciones.PagoSuscripcionDetalle;
import com.aarondevs.filered.Parameters.Response.PaginationResponse;
import java.math.BigDecimal;
import java.util.List;

public interface PagoSuscripcionDataSet {
    PaginationResponse<PagoSuscripcion> findAllPages(Long clienteId, String estado, String buscar, Integer page, Integer size);

    PagoSuscripcion findById(Long pagoSuscripcionId);

    List<PagoSuscripcionDetalle> findDetalles(Long pagoSuscripcionId);

    Long createDetalle(Long pagoSuscripcionId, BigDecimal monto, String metodo, String referencia, String observacion,
                       Long usuarioAdministracionId, String actor);

    void aplicarMonto(Long pagoSuscripcionId, BigDecimal monto, String actor);

    Long clienteDeSuscripcion(Long suscripcionId);
}

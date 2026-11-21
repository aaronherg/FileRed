package com.aarondevs.filered.Services.Suscripciones;

import com.aarondevs.filered.Models.Suscripciones.SuscripcionSolicitud;
import com.aarondevs.filered.Parameters.Suscripciones.SuscripcionSolicitudAgregarParameters;
import com.aarondevs.filered.Parameters.Suscripciones.SuscripcionSolicitudResponderParameters;

public interface SuscripcionSolicitudService {
    SuscripcionSolicitud crear(SuscripcionSolicitudAgregarParameters parametro, Long clienteId, String actor);

    SuscripcionSolicitud responder(SuscripcionSolicitudResponderParameters parametro, String actor, Long usuarioAdministracionId);

    boolean cancelar(Long suscripcionSolicitudId, String actor);

    SuscripcionSolicitud id(Long suscripcionSolicitudId);

    Object pages(Long clienteId, String estado, Integer page, Integer size);
}

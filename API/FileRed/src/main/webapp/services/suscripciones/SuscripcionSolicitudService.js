window.SuscripcionSolicitudService = {
    paginar: (filtro = {}) => FileRedApi.post("/api/suscripciones/solicitudes/getAllPages", filtro),
    obtener: suscripcionSolicitudId => FileRedApi.post("/api/suscripciones/solicitudes/getById", { suscripcionSolicitudId }),
    solicitar: datos => FileRedApi.post("/api/suscripciones/solicitudes/add", datos),
    responder: datos => FileRedApi.put("/api/suscripciones/solicitudes/update", datos),
    cancelar: suscripcionSolicitudId => FileRedApi.delete("/api/suscripciones/solicitudes/delete", { suscripcionSolicitudId })
};

window.PagoSuscripcionService = {
    paginar: (filtro = {}) => FileRedApi.post("/api/suscripciones/pagos/getAllPages", filtro),
    obtener: pagoSuscripcionId => FileRedApi.post("/api/suscripciones/pagos/getById", { pagoSuscripcionId }),
    registrar: pago => FileRedApi.post("/api/suscripciones/pagos/add", pago)
};

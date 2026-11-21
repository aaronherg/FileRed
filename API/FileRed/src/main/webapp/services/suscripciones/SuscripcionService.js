window.SuscripcionService = {
    paginar: (filtro = {}) => FileRedApi.post("/api/suscripciones/getAllPages", filtro),
    obtener: (clienteId = null) => FileRedApi.post("/api/suscripciones/getById", { clienteId }),
    asignar: datos => FileRedApi.post("/api/suscripciones/add", datos),
    cancelar: clienteId => FileRedApi.put("/api/suscripciones/cancelar", { clienteId })
};

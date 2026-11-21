window.MonedaService = {
    listar: () => FileRedApi.get("/api/administracion/monedas/getAll"),
    obtener: monedaId => FileRedApi.post("/api/administracion/monedas/getById", { monedaId }),
    agregar: moneda => FileRedApi.post("/api/administracion/monedas/add", moneda),
    actualizar: moneda => FileRedApi.put("/api/administracion/monedas/update", moneda),
    eliminar: monedaId => FileRedApi.delete("/api/administracion/monedas/delete", { monedaId })
};

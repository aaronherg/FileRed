window.PlanService = {
    listar: () => FileRedApi.get("/api/administracion/planes/getAll"),
    publicos: () => FileRedApi.get("/api/administracion/planes/getAllPublicos", { silent: true }),
    obtener: planId => FileRedApi.post("/api/administracion/planes/getById", { planId }),
    agregar: plan => FileRedApi.post("/api/administracion/planes/add", plan),
    actualizar: plan => FileRedApi.put("/api/administracion/planes/update", plan),
    eliminar: planId => FileRedApi.delete("/api/administracion/planes/delete", { planId })
};

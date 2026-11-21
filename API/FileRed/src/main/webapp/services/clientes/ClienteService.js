window.ClienteService = {
    paginar: (filtro = {}) => FileRedApi.post("/api/clientes/getAllPages", filtro),
    obtener: clienteId => FileRedApi.post("/api/clientes/getById", { clienteId }),
    actual: () => FileRedApi.post("/api/clientes/getActual", {}),
    agregar: cliente => FileRedApi.post("/api/clientes/add", cliente),
    actualizar: cliente => FileRedApi.put("/api/clientes/update", cliente),
    actualizarActual: cliente => FileRedApi.put("/api/clientes/updateActual", cliente),
    eliminar: clienteId => FileRedApi.delete("/api/clientes/delete", { clienteId })
};

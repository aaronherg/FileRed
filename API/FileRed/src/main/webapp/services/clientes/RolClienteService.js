window.RolClienteService = {
    listar: (clienteId = null) => FileRedApi.post("/api/clientes/roles/getAll", { clienteId }),
    obtener: rolClienteId => FileRedApi.post("/api/clientes/roles/getById", { rolClienteId }),
    agregar: rol => FileRedApi.post("/api/clientes/roles/add", rol),
    actualizar: rol => FileRedApi.put("/api/clientes/roles/update", rol),
    eliminar: rolClienteId => FileRedApi.delete("/api/clientes/roles/delete", { rolClienteId })
};

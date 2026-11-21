window.ApiKeyService = {
    listar: (clienteId = null) => FileRedApi.post("/api/archivos/apikeys/getAll", { clienteId }),
    permisos: () => FileRedApi.get("/api/archivos/apikeys/getPermisos", { silent: true }),
    agregar: datos => FileRedApi.post("/api/archivos/apikeys/add", datos),
    actualizar: datos => FileRedApi.put("/api/archivos/apikeys/update", datos),
    revocar: apiKeyId => FileRedApi.delete("/api/archivos/apikeys/delete", { apiKeyId })
};

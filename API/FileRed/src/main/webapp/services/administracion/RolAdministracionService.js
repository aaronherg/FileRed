window.RolAdministracionService = {
    listar: () => FileRedApi.get("/api/administracion/roles/getAll"),
    obtener: rolAdministracionId => FileRedApi.post("/api/administracion/roles/getById", { rolAdministracionId }),
    agregar: rol => FileRedApi.post("/api/administracion/roles/add", rol),
    actualizar: rol => FileRedApi.put("/api/administracion/roles/update", rol),
    eliminar: rolAdministracionId => FileRedApi.delete("/api/administracion/roles/delete", { rolAdministracionId })
};

window.ApiKeyAdministracionService = {
    listar: () => FileRedApi.post("/api/administracion/apikeys/getAll", {}),
    crear: nombre => FileRedApi.post("/api/administracion/apikeys/add", { nombre }),
    renombrar: (uuid, nombre) => FileRedApi.put("/api/administracion/apikeys/rename", { uuid, nombre }),
    revocar: uuid => FileRedApi.put("/api/administracion/apikeys/revoke", { uuid })
};

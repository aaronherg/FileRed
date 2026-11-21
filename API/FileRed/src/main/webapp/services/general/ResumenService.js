window.ResumenService = {
    administracion: () => FileRedApi.get("/api/general/resumen/getAdministracion"),
    cliente: () => FileRedApi.get("/api/general/resumen/getCliente")
};

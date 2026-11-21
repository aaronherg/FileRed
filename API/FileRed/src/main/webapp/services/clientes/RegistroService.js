window.RegistroService = {
    registrar: datos => FileRedApi.post("/api/clientes/registro/add", datos)
};

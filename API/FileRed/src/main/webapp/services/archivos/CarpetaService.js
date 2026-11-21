window.CarpetaService = {
    listar: (almacen, clienteId = null) => FileRedApi.post("/api/archivos/carpetas/getAll", { almacen, clienteId }),
    agregar: (almacen, categoria) => FileRedApi.post("/api/archivos/carpetas/add", { almacen, categoria }),
    renombrar: (almacen, categoria, nombre) => FileRedApi.put("/api/archivos/carpetas/update", { almacen, categoria, nombre }),
    eliminar: (almacen, categoria) => FileRedApi.delete("/api/archivos/carpetas/delete", { almacen, categoria })
};

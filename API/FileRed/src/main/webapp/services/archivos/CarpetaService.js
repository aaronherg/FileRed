window.CarpetaService = {
    listar: (almacen, clienteId = null, nodoId = 0) => FileRedApi.post("/api/archivos/carpetas/getAll", { almacen, clienteId, nodoId }),
    agregar: (almacen, categoria, nodoId = 0) => FileRedApi.post("/api/archivos/carpetas/add", { almacen, categoria, nodoId }),
    renombrar: (almacen, categoria, nombre, nodoId = 0) => FileRedApi.put("/api/archivos/carpetas/update", { almacen, categoria, nombre, nodoId }),
    eliminar: (almacen, categoria, nodoId = 0) => FileRedApi.delete("/api/archivos/carpetas/delete", { almacen, categoria, nodoId })
};

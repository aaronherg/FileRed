window.AlmacenService = {
    listar: (clienteId = null, nodoId = 0) => FileRedApi.post("/api/archivos/almacenes/getAll", { clienteId, nodoId }),
    agregar: (almacen, nodoId = 0) => FileRedApi.post("/api/archivos/almacenes/add", { almacen, nodoId }),
    renombrar: (almacen, nombre, nodoId = 0) => FileRedApi.put("/api/archivos/almacenes/update", { almacen, nombre, nodoId }),
    eliminar: (almacen, nodoId = 0) => FileRedApi.delete("/api/archivos/almacenes/delete", { almacen, nodoId })
};

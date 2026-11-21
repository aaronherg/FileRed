window.AlmacenService = {
    listar: (clienteId = null) => FileRedApi.post("/api/archivos/almacenes/getAll", { clienteId }),
    agregar: almacen => FileRedApi.post("/api/archivos/almacenes/add", { almacen }),
    renombrar: (almacen, nombre) => FileRedApi.put("/api/archivos/almacenes/update", { almacen, nombre }),
    eliminar: almacen => FileRedApi.delete("/api/archivos/almacenes/delete", { almacen })
};

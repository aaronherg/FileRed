window.NodoService = {
    listar: (silencioso = false) => FileRedApi.post("/api/archivos/nodos/getAll", {}, { silent: silencioso }),
    agregar: (nombre, rol = "Almacenamiento", origenes = []) => FileRedApi.post("/api/archivos/nodos/add", { nombre, rol, origenes }),
    origenes: (nodoId, origenes) => FileRedApi.put("/api/archivos/nodos/updateOrigenes", { nodoId, origenes }),
    restaurar: nodoId => FileRedApi.put("/api/archivos/nodos/updateRestaurar", { nodoId }),
    renombrar: (nodoId, nombre) => FileRedApi.put("/api/archivos/nodos/update", { nodoId, nombre }),
    generarCodigo: nodoId => FileRedApi.put("/api/archivos/nodos/updateCodigo", { nodoId }),
    predeterminar: nodoId => FileRedApi.put("/api/archivos/nodos/updatePredeterminado", { nodoId }),
    eliminar: nodoId => FileRedApi.delete("/api/archivos/nodos/delete", { nodoId })
};

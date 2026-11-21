window.UsuarioAdministracionService = {
    listar: () => FileRedApi.get("/api/administracion/usuarios/getAll"),
    obtener: usuarioAdministracionId => FileRedApi.post("/api/administracion/usuarios/getById", { usuarioAdministracionId }),
    agregar: usuario => FileRedApi.post("/api/administracion/usuarios/add", usuario),
    actualizar: usuario => FileRedApi.put("/api/administracion/usuarios/update", usuario),
    restablecerContrasena: (usuarioAdministracionId, contrasena) =>
        FileRedApi.put("/api/administracion/usuarios/restablecerContrasena", { usuarioAdministracionId, contrasena }),
    eliminar: usuarioAdministracionId => FileRedApi.delete("/api/administracion/usuarios/delete", { usuarioAdministracionId })
};

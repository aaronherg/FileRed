window.UsuarioClienteService = {
    listar: (clienteId = null) => FileRedApi.post("/api/clientes/usuarios/getAll", { clienteId }),
    obtener: usuarioClienteId => FileRedApi.post("/api/clientes/usuarios/getById", { usuarioClienteId }),
    agregar: usuario => FileRedApi.post("/api/clientes/usuarios/add", usuario),
    actualizar: usuario => FileRedApi.put("/api/clientes/usuarios/update", usuario),
    restablecerContrasena: (usuarioClienteId, contrasena) =>
        FileRedApi.put("/api/clientes/usuarios/restablecerContrasena", { usuarioClienteId, contrasena }),
    eliminar: usuarioClienteId => FileRedApi.delete("/api/clientes/usuarios/delete", { usuarioClienteId })
};

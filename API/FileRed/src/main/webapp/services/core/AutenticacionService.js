window.AutenticacionService = {
    async login(usuario, contrasena) {
        const resultado = await FileRedApi.post("/api/seguridad/autenticacion/login", {
            usuario,
            contrasena,
            agente: navigator.userAgent
        });
        if (resultado?.requiereDosFactores) return resultado;
        if (!resultado?.token || !resultado?.tipo) throw new Error("El servidor respondió sin los datos completos de la sesión.");
        FileRedStorage.guardarSesion(resultado);
        return resultado;
    },

    async ingresoDosFactores(usuario, contrasena, codigo) {
        const session = await FileRedApi.post("/api/seguridad/autenticacion/dosfactores/ingreso", {
            usuario,
            contrasena,
            codigo,
            agente: navigator.userAgent
        });
        if (!session?.token || !session?.tipo) throw new Error("El servidor respondió sin los datos completos de la sesión.");
        FileRedStorage.guardarSesion(session);
        return session;
    },

    sesionActual() {
        return FileRedStorage.sesion();
    },

    destino(session = FileRedStorage.sesion()) {
        const contexto = window.FileRedConfig?.contextPath || "";
        return `${contexto}${session?.tipo === "Administracion" ? "/pages/center/" : "/pages/customer/"}`;
    },

    async validarSesion(tipoEsperado) {
        const session = FileRedStorage.sesion();
        if (!session?.token || (tipoEsperado && session.tipo !== tipoEsperado)) return null;
        const actual = await FileRedApi.post("/api/seguridad/autenticacion/getSesion", {}, { silent: true });
        FileRedStorage.actualizarSesion(actual || {});
        return FileRedStorage.sesion();
    },

    cambiarContrasena: (contrasenaActual, contrasenaNueva) =>
        FileRedApi.put("/api/seguridad/autenticacion/cambiarContrasena", { contrasenaActual, contrasenaNueva }),

    async logout() {
        try {
            if (FileRedStorage.token()) await FileRedApi.put("/api/seguridad/autenticacion/logout", {}, { silent: true });
        } catch (_) {
            FileRedStorage.limpiar();
        }
        FileRedStorage.limpiar();
    },

    puede(accion) {
        return (FileRedStorage.sesion()?.acciones || []).includes(accion);
    }
};

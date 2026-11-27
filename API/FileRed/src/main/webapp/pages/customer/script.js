(() => {
    const version = "20261127-6";
    FileRedPanel.iniciar({
        tipo: "Cliente",
        version,
        pantallas: {
            inicio: "./pantallas/inicio/script.js",
            archivos: "./pantallas/archivos/script.js",
            nodos: "./pantallas/nodos/script.js",
            apikeys: "./pantallas/apikeys/script.js",
            suscripcion: "./pantallas/suscripcion/script.js",
            pagos: "./pantallas/pagos/script.js",
            usuarios: "./pantallas/usuarios/script.js",
            roles: "./pantallas/roles/script.js",
            __perfil: "./pantallas/perfilusuario/script.js",
            __seguridad: "./pantallas/seguridad/script.js",
            reporte_consumo: "./pantallas/reporte-consumo/script.js",
            reporte_actividad: "./pantallas/reporte-actividad/script.js"
        }
    });
})();

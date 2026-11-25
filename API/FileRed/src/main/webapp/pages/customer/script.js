(() => {
    const version = "20261120-8";
    FileRedPanel.iniciar({
        tipo: "Cliente",
        version,
        pantallas: {
            inicio: "./pantallas/inicio/script.js",
            archivos: "./pantallas/archivos/script.js",
            apikeys: "./pantallas/apikeys/script.js",
            suscripcion: "./pantallas/suscripcion/script.js",
            pagos: "./pantallas/pagos/script.js",
            usuarios: "./pantallas/usuarios/script.js",
            roles: "./pantallas/roles/script.js",
            perfil: "./pantallas/perfil/script.js",
            __perfil: "./pantallas/perfilusuario/script.js",
            reporte_consumo: "./pantallas/reporte-consumo/script.js",
            reporte_actividad: "./pantallas/reporte-actividad/script.js"
        }
    });
})();

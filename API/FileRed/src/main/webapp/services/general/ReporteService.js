window.ReporteService = {
    ingresos: filtro => FileRedApi.post("/api/general/reportes/getIngresos", filtro),
    cuentasPorCobrar: () => FileRedApi.post("/api/general/reportes/getCuentasCobrar", {}),
    consumoClientes: filtro => FileRedApi.post("/api/general/reportes/getConsumoClientes", filtro),
    usoApi: filtro => FileRedApi.post("/api/general/reportes/getUsoApi", filtro),
    consumo: () => FileRedApi.post("/api/general/reportes/getConsumo", {}),
    actividad: filtro => FileRedApi.post("/api/general/reportes/getActividad", filtro)
};

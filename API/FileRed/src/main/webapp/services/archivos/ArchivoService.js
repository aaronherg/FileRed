window.ArchivoService = {
    paginar: (filtro = {}) => FileRedApi.post("/api/archivos/getAllPages", filtro),
    subir(almacen, categoria, archivo) {
        const datos = new FormData();
        datos.append("almacen", almacen);
        datos.append("categoria", categoria);
        datos.append("file", archivo);
        return FileRedApi.post("/api/archivos/add", datos, { silent: true });
    },
    subirConProgreso(almacen, categoria, archivo, alProgresar) {
        return new Promise((resolve, reject) => {
            const datos = new FormData();
            datos.append("almacen", almacen);
            datos.append("categoria", categoria);
            datos.append("file", archivo);
            const xhr = new XMLHttpRequest();
            xhr.open("POST", `${window.FileRedConfig?.apiBaseUrl || ""}/api/archivos/add`);
            xhr.setRequestHeader("Accept", "application/json");
            const token = FileRedStorage.token();
            if (token) xhr.setRequestHeader("Authorization", `Bearer ${token}`);
            xhr.upload.addEventListener("progress", evento => {
                if (evento.lengthComputable) alProgresar?.(Math.round((evento.loaded / evento.total) * 100));
            });
            xhr.addEventListener("load", () => {
                let respuesta = null;
                try {
                    respuesta = JSON.parse(xhr.responseText || "null");
                } catch (_) {
                    respuesta = { mensage: xhr.responseText };
                }
                if (xhr.status >= 200 && xhr.status < 300) resolve(respuesta?.data ?? respuesta);
                else reject(new Error(respuesta?.mensage || `La API respondió con estado ${xhr.status}.`));
            });
            xhr.addEventListener("error", () => reject(new Error("No fue posible conectar con FileRed.")));
            xhr.addEventListener("abort", () => reject(new Error("Subida cancelada.")));
            xhr.send(datos);
        });
    },
    reemplazar(almacen, categoria, identificador, extension, archivo) {
        const datos = new FormData();
        datos.append("almacen", almacen);
        datos.append("categoria", categoria);
        datos.append("identificador", identificador);
        datos.append("extension", extension);
        datos.append("file", archivo);
        return FileRedApi.put("/api/archivos/update", datos);
    },
    renombrar: (almacen, categoria, identificador, nombre) =>
        FileRedApi.put("/api/archivos/updateNombre", { almacen, categoria, identificador, nombre }),
    eliminar: archivo => FileRedApi.delete("/api/archivos/delete", archivo)
};

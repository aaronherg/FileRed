package com.aarondevs.filered.Services.Archivos;

import com.aarondevs.filered.Models.Archivos.ApiKey;
import com.aarondevs.filered.Parameters.Archivos.ApiKeyActualizarParameters;
import com.aarondevs.filered.Parameters.Archivos.ApiKeyAgregarParameters;
import java.util.List;

public interface ApiKeyService {
    List<String> PERMISOS = List.of("consultar_cuenta",
            "obtener_almacenes", "agregar_almacen", "actualizar_almacen", "eliminar_almacen",
            "obtener_carpetas", "agregar_carpeta", "actualizar_carpeta", "eliminar_carpeta",
            "obtener_archivos", "obtener_archivo", "agregar_archivo", "actualizar_archivo", "renombrar_archivo", "eliminar_archivo");

    ApiKey crear(ApiKeyAgregarParameters parametro, Long clienteId, String actor);

    ApiKey actualizar(ApiKeyActualizarParameters parametro, String actor);

    boolean revocar(Long apiKeyId, String actor);

    ApiKey id(Long apiKeyId);

    List<ApiKey> all(Long clienteId);

    ApiKey autenticar(String clave, String permiso);
}

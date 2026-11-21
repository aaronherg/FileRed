package com.aarondevs.filered.Controllers.FileRed;

import com.aarondevs.filered.Models.Archivos.ApiKey;
import com.aarondevs.filered.Parameters.Response.ApiResponse;
import com.aarondevs.filered.Services.Archivos.ApiKeyService;
import com.aarondevs.filered.Services.Archivos.ArchivoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class FileRedApiSupport {
    public static final String ENCABEZADO = "X-Api-Key";

    private final ApiKeyService apiKeys;
    private final ArchivoService archivos;

    public FileRedApiSupport(ApiKeyService apiKeys, ArchivoService archivos) {
        this.apiKeys = apiKeys;
        this.archivos = archivos;
    }

    public ApiKey autenticar(HttpServletRequest request, String permiso) {
        String clave = request.getHeader(ENCABEZADO);
        if (clave == null || clave.isBlank()) throw new ApiKeyRequeridaException("Envíe su API key en el encabezado " + ENCABEZADO);
        ApiKey llave;
        try {
            llave = apiKeys.autenticar(clave.trim(), permiso);
        } catch (SecurityException error) {
            if ("APIKEY NO VALIDA".equals(error.getMessage())) throw new ApiKeyRequeridaException("La API key no es válida o está suspendida");
            throw new SecurityException("La API key no tiene el permiso " + permiso);
        }
        archivos.registrarSolicitudApi(llave.getClienteId());
        return llave;
    }

    public String actor(ApiKey llave) {
        return "APIKEY:" + llave.getApiKeyUuid();
    }

    public String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    public ResponseEntity<ApiResponse<?>> error(Exception error) {
        if (error instanceof ApiKeyRequeridaException) return ResponseEntity.status(401).body(ApiResponse.error(401, error.getMessage()));
        if (error instanceof SecurityException) return ResponseEntity.status(403).body(ApiResponse.error(403, error.getMessage()));
        if (error instanceof IllegalStateException) return ResponseEntity.status(500).body(ApiResponse.error(500, error.getMessage()));
        String mensaje = error.getMessage() == null ? "Solicitud no válida" : error.getMessage();
        int estado = mensaje.contains("no existe") || mensaje.contains("no encontrado") ? 404 : 400;
        return ResponseEntity.status(estado).body(ApiResponse.error(estado, mensaje));
    }

    public static class ApiKeyRequeridaException extends RuntimeException {
        public ApiKeyRequeridaException(String mensaje) {
            super(mensaje);
        }
    }
}

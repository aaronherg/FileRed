package com.aarondevs.filered.Config;

import com.aarondevs.filered.Parameters.Response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice
public class FileRedExcepcionesConfig {
    private final String limiteSolicitud;

    public FileRedExcepcionesConfig(@Value("${spring.servlet.multipart.max-request-size}") String limiteSolicitud) {
        this.limiteSolicitud = limiteSolicitud;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<?>> tamanoExcedido(MaxUploadSizeExceededException error) {
        return ResponseEntity.status(413).body(ApiResponse.error(413, "La petición supera el límite permitido (" + limiteSolicitud + ")"));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<?>> multipartInvalido(MultipartException error) {
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "No se pudo leer la petición multipart"));
    }
}

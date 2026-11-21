package com.aarondevs.filered.Parameters.Archivos;

import java.util.List;
import lombok.Data;

@Data
public class ApiKeyActualizarParameters {
    private Long apiKeyId;
    private String nombre;
    private List<String> permisos;
    private String estado;
}

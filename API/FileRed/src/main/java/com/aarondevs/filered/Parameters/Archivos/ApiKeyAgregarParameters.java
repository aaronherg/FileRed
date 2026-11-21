package com.aarondevs.filered.Parameters.Archivos;

import java.util.List;
import lombok.Data;

@Data
public class ApiKeyAgregarParameters {
    private String nombre;
    private List<String> permisos;
}

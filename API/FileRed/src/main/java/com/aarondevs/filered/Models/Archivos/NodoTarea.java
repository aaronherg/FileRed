package com.aarondevs.filered.Models.Archivos;

import java.sql.Timestamp;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NodoTarea {
    private Long nodoTareaId;
    private Long nodoId;
    private String tipo;
    private Long archivoId;
    private String ruta;
    private String destino;
    private String estado;
    private Integer intentos;
    private Timestamp fechaEnvio;
    private String error;
}

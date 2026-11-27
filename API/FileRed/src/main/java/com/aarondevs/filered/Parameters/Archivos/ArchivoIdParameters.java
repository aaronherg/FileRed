package com.aarondevs.filered.Parameters.Archivos;

import lombok.Data;

@Data
public class ArchivoIdParameters {
    private String almacen;
    private String categoria;
    private String identificador;
    private String extension;
    private Long nodoId;
}

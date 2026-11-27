package com.aarondevs.filered.Parameters.Archivos;

import lombok.Data;

@Data
public class NodoAgregarParameters {
    private String nombre;
    private String rol;
    private java.util.List<Long> origenes;
}

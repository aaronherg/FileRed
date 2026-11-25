package com.aarondevs.filered.Parameters.Seguridad;

import lombok.Data;

@Data
public class AutenticacionDosFactoresParameters {
    private String codigo;
    private String contrasena;
}

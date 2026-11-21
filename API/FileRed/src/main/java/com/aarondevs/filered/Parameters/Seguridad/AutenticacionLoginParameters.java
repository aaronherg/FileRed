package com.aarondevs.filered.Parameters.Seguridad;

import lombok.Data;

@Data
public class AutenticacionLoginParameters {
    private String usuario;
    private String contrasena;
    private String ipPublica;
    private String agente;
}

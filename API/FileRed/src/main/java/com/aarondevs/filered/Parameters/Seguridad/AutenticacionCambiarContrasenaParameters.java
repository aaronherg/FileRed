package com.aarondevs.filered.Parameters.Seguridad;

import lombok.Data;

@Data
public class AutenticacionCambiarContrasenaParameters {
    private String contrasenaActual;
    private String contrasenaNueva;
}

package com.aarondevs.filered.Parameters.Administracion;

import lombok.Data;

@Data
public class UsuarioAdministracionRestablecerContrasenaParameters {
    private Long usuarioAdministracionId;
    private String contrasena;
}

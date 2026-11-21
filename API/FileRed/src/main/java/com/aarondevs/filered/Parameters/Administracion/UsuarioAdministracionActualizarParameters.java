package com.aarondevs.filered.Parameters.Administracion;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UsuarioAdministracionActualizarParameters extends UsuarioAdministracionAgregarParameters {
    private String usuarioAdministracionUuid;
    private String estado;
}

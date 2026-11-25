package com.aarondevs.filered.Services.Administracion;

import com.aarondevs.filered.Models.Administracion.UsuarioAdministracion;
import com.aarondevs.filered.Parameters.Administracion.UsuarioAdministracionActualizarParameters;
import com.aarondevs.filered.Parameters.Administracion.UsuarioAdministracionAgregarParameters;
import com.aarondevs.filered.Parameters.Administracion.UsuarioAdministracionPerfilParameters;

public interface UsuarioAdministracionService {
    UsuarioAdministracion crear(UsuarioAdministracionAgregarParameters parametro, String actor);

    UsuarioAdministracion actualizar(UsuarioAdministracionActualizarParameters parametro, String actor);

    UsuarioAdministracion actualizarPerfil(Long usuarioAdministracionId, UsuarioAdministracionPerfilParameters parametro, String actor);

    UsuarioAdministracion id(Long id);

    UsuarioAdministracion uuid(String uuid);

    Object all();

    void restablecerContrasena(Long id, String contrasena, String actor);

    boolean eliminar(Long id, String actor, String usuarioUuidActual);
}

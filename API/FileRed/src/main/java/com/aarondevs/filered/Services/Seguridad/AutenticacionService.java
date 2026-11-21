package com.aarondevs.filered.Services.Seguridad;

import com.aarondevs.filered.Parameters.Seguridad.AutenticacionCambiarContrasenaParameters;
import com.aarondevs.filered.Parameters.Seguridad.AutenticacionLoginParameters;
import com.aarondevs.filered.Security.ContextoAutenticacion;
import java.util.Map;

public interface AutenticacionService {
    Map<String, Object> autenticar(AutenticacionLoginParameters parametro);

    Map<String, Object> sesionActual(ContextoAutenticacion contexto);

    boolean cerrar(ContextoAutenticacion contexto);

    void cambiarContrasena(ContextoAutenticacion contexto, AutenticacionCambiarContrasenaParameters parametro);
}

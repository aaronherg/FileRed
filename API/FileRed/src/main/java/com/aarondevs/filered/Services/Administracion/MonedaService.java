package com.aarondevs.filered.Services.Administracion;

import com.aarondevs.filered.Parameters.Administracion.MonedaActualizarParameters;
import com.aarondevs.filered.Parameters.Administracion.MonedaAgregarParameters;

public interface MonedaService {
    Object crear(MonedaAgregarParameters parametro, String actor);

    Object actualizar(MonedaActualizarParameters parametro, String actor);

    Object id(Long id);

    Object all();

    boolean eliminar(Long id, String actor);
}

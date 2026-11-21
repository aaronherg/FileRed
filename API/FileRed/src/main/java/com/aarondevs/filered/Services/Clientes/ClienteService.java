package com.aarondevs.filered.Services.Clientes;

import com.aarondevs.filered.Models.Clientes.Cliente;
import com.aarondevs.filered.Parameters.Clientes.ClienteActualizarParameters;
import com.aarondevs.filered.Parameters.Clientes.ClienteAgregarParameters;

public interface ClienteService {
    Cliente crear(ClienteAgregarParameters parametro, String actor);

    Cliente actualizar(ClienteActualizarParameters parametro, String actor);

    Cliente id(Long id);

    Cliente uuid(String uuid);

    Object all();

    Object pages(Integer page, Integer size, String buscar, String estado);

    boolean eliminar(Long id, String actor);
}

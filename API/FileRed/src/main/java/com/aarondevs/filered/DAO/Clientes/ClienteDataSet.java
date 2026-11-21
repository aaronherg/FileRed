package com.aarondevs.filered.DAO.Clientes;

import com.aarondevs.filered.Models.Clientes.Cliente;
import com.aarondevs.filered.Parameters.Clientes.ClienteActualizarParameters;
import com.aarondevs.filered.Parameters.Clientes.ClienteDatosParameters;
import com.aarondevs.filered.Parameters.Response.PaginationResponse;
import java.util.List;

public interface ClienteDataSet {
    Cliente create(ClienteDatosParameters parametro, String actor);

    Cliente update(ClienteActualizarParameters parametro, String actor);

    Cliente findById(Long id);

    Cliente findByUuid(String uuid);

    List<Cliente> findAll();

    PaginationResponse<Cliente> findAllPages(Integer page, Integer size, String buscar, String estado);

    boolean existsDocumento(String documento, String excluirUuid);

    Long crearSuscripcion(Long clienteId, String actor);

    Long crearRolPropietario(Long clienteId, String nombre, String actor);

    boolean delete(Long id, String actor);
}

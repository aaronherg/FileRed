package com.aarondevs.filered.Parameters.Clientes;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RolClienteActualizarParameters extends RolClienteAgregarParameters {
    private String rolClienteUuid;
    private String estado;
}

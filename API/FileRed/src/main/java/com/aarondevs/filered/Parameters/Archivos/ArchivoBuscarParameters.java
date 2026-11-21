package com.aarondevs.filered.Parameters.Archivos;

import lombok.Data;

@Data
public class ArchivoBuscarParameters {
    private Long clienteId;
    private String almacen;
    private String categoria;
    private String buscar;
    private Integer page;
    private Integer size;
}

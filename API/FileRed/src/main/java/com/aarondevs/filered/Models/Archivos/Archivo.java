package com.aarondevs.filered.Models.Archivos;

import com.aarondevs.filered.Models.BaseVersionado;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Archivo extends BaseVersionado {
    private Long archivoId;
    private String archivoUuid;
    private Long clienteId;
    private String clienteUuid;
    private String almacen;
    private String categoria;
    private String identificador;
    private String extension;
    private String nombreOriginal;
    private Long tamano;
    private Long apiKeyId;
    private String apiKeyUuid;
    private Long nodoId;
    private String estadoNodo;
    private String hashSha256;
}

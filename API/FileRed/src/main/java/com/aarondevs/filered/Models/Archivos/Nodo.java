package com.aarondevs.filered.Models.Archivos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.sql.Timestamp;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Nodo {
    private Long nodoId;
    private String nodoUuid;
    private Long clienteId;
    private String nombre;
    private String rol;
    @JsonIgnore
    private String codigoHash;
    private Timestamp codigoExpira;
    @JsonIgnore
    private String tokenHash;
    private String equipo;
    private String plataforma;
    private String versionPrograma;
    private String carpeta;
    private Long espacioTotal;
    private Long espacioLibre;
    private Timestamp ultimaConexion;
    private Timestamp fechaVinculacion;
    private Timestamp sinPlanDesde;
    private Timestamp fechaCreacion;
    private String estado;
}

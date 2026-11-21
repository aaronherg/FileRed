package com.aarondevs.filered.Models.Administracion;

import com.aarondevs.filered.Models.BaseVersionado;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Plan extends BaseVersionado {
    private Long planId;
    private String planUuid;
    private Long monedaId;
    private String monedaUuid;
    private String nombre;
    private String descripcion;
    private String modalidad;
    private BigDecimal espacioGb;
    private Integer tamanoMaximoArchivoMb;
    private BigDecimal precio;
    private BigDecimal precioGbAdicional;
    private String caracteristicas;
    private Boolean destacado;
    private Boolean publico;
    private Integer orden;
    private String monedaSimbolo;
    private String monedaCodigo;
}

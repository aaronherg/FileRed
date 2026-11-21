package com.aarondevs.filered.Parameters.Administracion;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PlanAgregarParameters {
    private Long monedaId;
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
}

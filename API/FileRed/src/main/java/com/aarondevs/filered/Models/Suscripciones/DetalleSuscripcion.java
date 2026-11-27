package com.aarondevs.filered.Models.Suscripciones;

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
public class DetalleSuscripcion extends BaseVersionado {
    private Long detalleSuscripcionId;
    private String detalleSuscripcionUuid;
    private Long suscripcionId;
    private String suscripcionUuid;
    private Long planId;
    private String planUuid;
    private java.sql.Timestamp fechaInicio;
    private java.sql.Timestamp fechaFin;
    private String tipoCobro;
    private Boolean permitirAdicionales;
    private Boolean renovacionAutomatica;
    private Long renovacionOrigenDetalleId;
    private String renovacionOrigenDetalleUuid;
    private String planNombre;
    private BigDecimal espacioGb;
    private Integer tamanoMaximoArchivoMb;
    private BigDecimal precio;
    private BigDecimal precioGbAdicional;
    private Boolean permitirNube;
    private Boolean permitirNodos;
    private Boolean permitirRespaldos;
    private String monedaSimbolo;
}

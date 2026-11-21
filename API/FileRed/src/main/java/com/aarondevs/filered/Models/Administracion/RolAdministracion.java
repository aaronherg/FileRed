package com.aarondevs.filered.Models.Administracion;

import com.aarondevs.filered.Models.BaseVersionado;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RolAdministracion extends BaseVersionado {
    private Long rolAdministracionId;
    private String rolAdministracionUuid;
    private String nombre;
    private String descripcion;
    private String color;
    private Boolean protegido;
    private java.util.List<Long> accionesIds;
}

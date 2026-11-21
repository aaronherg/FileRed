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
public class RolAdministracionAccion extends BaseVersionado {
    private Long rolAdministracionAccionId;
    private String rolAdministracionAccionUuid;
    private Long rolAdministracionId;
    private String rolAdministracionUuid;
    private Long moduloAdministracionAccionId;
    private String moduloAdministracionAccionUuid;
}

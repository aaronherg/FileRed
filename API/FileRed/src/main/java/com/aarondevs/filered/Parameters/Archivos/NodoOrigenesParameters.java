package com.aarondevs.filered.Parameters.Archivos;

import java.util.List;
import lombok.Data;

@Data
public class NodoOrigenesParameters {
    private Long nodoId;
    private List<Long> origenes;
}

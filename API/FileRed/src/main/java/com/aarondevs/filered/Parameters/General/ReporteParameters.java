package com.aarondevs.filered.Parameters.General;

import java.time.LocalDate;
import lombok.Data;

@Data
public class ReporteParameters {
    private LocalDate desde;
    private LocalDate hasta;
    private String buscar;
}

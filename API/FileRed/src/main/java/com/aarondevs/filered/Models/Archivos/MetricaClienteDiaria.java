package com.aarondevs.filered.Models.Archivos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricaClienteDiaria {
    private Long metricaClienteDiariaId;
    private String metricaClienteDiariaUuid;
    private Long clienteId;
    private String clienteUuid;
    private java.sql.Date fecha;
    private Long solicitudesApi;
    private Long archivosSubidos;
    private Long bytesSubidos;
    private Long archivosEliminados;
    private Long descargas;
    private Long bytesDescargados;
}

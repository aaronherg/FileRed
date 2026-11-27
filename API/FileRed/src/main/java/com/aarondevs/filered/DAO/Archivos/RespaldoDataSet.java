package com.aarondevs.filered.DAO.Archivos;

import java.util.List;
import java.util.Map;

public interface RespaldoDataSet {
    List<Long> respaldosDeOrigen(Long clienteId, long origenId);

    List<Long> origenes(Long respaldoId);

    List<Map<String, Object>> origenesCliente(Long clienteId);

    void agregarOrigen(Long respaldoId, long origenId);

    void quitarOrigen(Long respaldoId, long origenId);

    int sembrar(Long respaldoId, Long clienteId, long origenId);

    void olvidarCopiasDeOrigen(Long respaldoId, Long clienteId, long origenId);

    boolean tieneCopia(Long archivoId, Long respaldoId, String hash);

    void marcarCopia(Long archivoId, Long respaldoId, String hash);

    void olvidarCopias(Long archivoId);

    List<Map<String, Object>> progreso(Long clienteId);

    List<Map<String, Object>> respaldadosPorOrigen(Long clienteId);

    Long respaldoConectadoConCopia(Long archivoId, String hash);

    void actualizarHash(Long archivoId, String hash);

    void moverANube(Long archivoId);
}

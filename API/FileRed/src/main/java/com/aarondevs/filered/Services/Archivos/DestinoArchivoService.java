package com.aarondevs.filered.Services.Archivos;

import com.aarondevs.filered.DAO.Archivos.ArchivoDataSet;
import com.aarondevs.filered.DAO.Archivos.NodoDataSet;
import com.aarondevs.filered.Models.Archivos.Nodo;
import org.springframework.stereotype.Service;

@Service
public class DestinoArchivoService {
    private final ArchivoServiceImpl nube;
    private final NodoDataSet nodosDatos;
    private final ArchivoDataSet archivosDatos;
    private final NodoService nodos;
    private final RespaldoService respaldos;

    public DestinoArchivoService(ArchivoServiceImpl nube, NodoDataSet nodosDatos, ArchivoDataSet archivosDatos, NodoService nodos,
                                 RespaldoService respaldos) {
        this.respaldos = respaldos;
        this.nube = nube;
        this.nodosDatos = nodosDatos;
        this.archivosDatos = archivosDatos;
        this.nodos = nodos;
    }

    public ArchivoService nube() {
        return nube;
    }

    public ArchivoService seleccionado(Long clienteId, Long nodoId) {
        if (clienteId == null || nodoId == null || nodoId <= 0) return nube;
        Nodo nodo = nodos.delCliente(clienteId, nodoId);
        if (RespaldoServiceImpl.RESPALDO.equals(nodo.getRol())) throw new IllegalArgumentException("Los servidores de respaldo no se exploran");
        return nodo(nodo);
    }

    public ArchivoService predeterminado(Long clienteId) {
        Long nodoId = nodosDatos.predeterminado(clienteId);
        Nodo nodo = nodoId == null || !respaldos.permiteNodos(clienteId) ? null : nodosDatos.findById(nodoId);
        if (nodo != null) return nodo(nodo);
        if (!respaldos.permiteNube(clienteId)) {
            throw new IllegalArgumentException("Tu plan no incluye la Nube: vincula un equipo y márcalo como Por defecto para recibir archivos");
        }
        return nube;
    }

    public ArchivoService deArchivo(Long clienteId, String almacen, String categoria, String identificador) {
        Long nodoId = nodosDatos.nodoDeArchivo(clienteId, almacen, categoria, identificador);
        if (nodoId == null) return predeterminado(clienteId);
        if (nodoId == 0) return nube;
        Nodo nodo = nodosDatos.findById(nodoId);
        return nodo == null ? nube : nodo(nodo);
    }

    private ArchivoService nodo(Nodo nodo) {
        return new NodoArchivoServicio(nodo, nodosDatos, archivosDatos, nube, nodos, respaldos);
    }
}

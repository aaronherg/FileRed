package com.aarondevs.filered.Controllers.Archivos;

import com.aarondevs.filered.Parameters.Archivos.NodoActualizarParameters;
import com.aarondevs.filered.Parameters.Archivos.NodoAgregarParameters;
import com.aarondevs.filered.Parameters.Archivos.NodoIdParameters;
import com.aarondevs.filered.Parameters.Archivos.NodoOrigenesParameters;
import com.aarondevs.filered.Services.Archivos.RespaldoService;
import com.aarondevs.filered.Parameters.Response.ApiResponse;
import com.aarondevs.filered.Security.ContextoAutenticacion;
import com.aarondevs.filered.Services.Archivos.NodoService;
import com.aarondevs.filered.Services.Seguridad.ContextoAutenticacionService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/archivos/nodos")
public class NodoController {
    private final NodoService service;
    private final RespaldoService respaldos;
    private final ContextoAutenticacionService contextoService;

    public NodoController(NodoService service, RespaldoService respaldos, ContextoAutenticacionService contextoService) {
        this.service = service;
        this.respaldos = respaldos;
        this.contextoService = contextoService;
    }

    @PostMapping("/getAll")
    public ResponseEntity<ApiResponse<?>> getAll(HttpServletRequest request) {
        try {
            ContextoAutenticacion contexto = contextoService.resolver(request);
            contextoService.exigirCliente(contexto);
            Set<String> acciones = contextoService.acciones(contexto);
            if (!acciones.contains("nodos.listar") && !acciones.contains("archivos.listar")) {
                throw new SecurityException("No tienes permiso para consultar los nodos");
            }
            return ResponseEntity.ok(ApiResponse.success(200, service.listar(contexto.clienteId()), "Nodos obtenidos correctamente"));
        } catch (Exception error) {
            return error(error);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<?>> add(@RequestBody(required = false) NodoAgregarParameters parametro, HttpServletRequest request) {
        try {
            ContextoAutenticacion contexto = contextoService.resolver(request);
            contextoService.exigirAccionCliente(contexto, "nodos", "crear");
            if (parametro == null) throw new IllegalArgumentException("El nombre del nodo es obligatorio");
            return ResponseEntity.status(201).body(ApiResponse.success(201, service.crear(contexto.clienteId(), parametro.getNombre(),
                    parametro.getRol(), parametro.getOrigenes(), contexto.usuarioUuid()), "Nodo creado correctamente"));
        } catch (Exception error) {
            return error(error);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<?>> update(@RequestBody(required = false) NodoActualizarParameters parametro, HttpServletRequest request) {
        try {
            ContextoAutenticacion contexto = contextoService.resolver(request);
            contextoService.exigirAccionCliente(contexto, "nodos", "modificar");
            if (parametro == null) throw new IllegalArgumentException("Indique el nodo");
            return ResponseEntity.ok(ApiResponse.success(200, service.renombrar(contexto.clienteId(), parametro.getNodoId(), parametro.getNombre(),
                    contexto.usuarioUuid()), "Nodo actualizado correctamente"));
        } catch (Exception error) {
            return error(error);
        }
    }

    @PutMapping("/updateCodigo")
    public ResponseEntity<ApiResponse<?>> updateCodigo(@RequestBody(required = false) NodoIdParameters parametro, HttpServletRequest request) {
        try {
            ContextoAutenticacion contexto = contextoService.resolver(request);
            contextoService.exigirAccionCliente(contexto, "nodos", "modificar");
            if (parametro == null) throw new IllegalArgumentException("Indique el nodo");
            return ResponseEntity.ok(ApiResponse.success(200, service.generarCodigo(contexto.clienteId(), parametro.getNodoId(),
                    contexto.usuarioUuid()), "Código de vinculación generado"));
        } catch (Exception error) {
            return error(error);
        }
    }

    @PutMapping("/updateOrigenes")
    public ResponseEntity<ApiResponse<?>> updateOrigenes(@RequestBody(required = false) NodoOrigenesParameters parametro, HttpServletRequest request) {
        try {
            ContextoAutenticacion contexto = contextoService.resolver(request);
            contextoService.exigirAccionCliente(contexto, "nodos", "modificar");
            if (parametro == null) throw new IllegalArgumentException("Indique el respaldo");
            return ResponseEntity.ok(ApiResponse.success(200, service.origenes(contexto.clienteId(), parametro.getNodoId(), parametro.getOrigenes()),
                    "Nodos respaldados actualizados"));
        } catch (Exception error) {
            return error(error);
        }
    }

    @PutMapping("/updateRestaurar")
    public ResponseEntity<ApiResponse<?>> updateRestaurar(@RequestBody(required = false) NodoIdParameters parametro, HttpServletRequest request) {
        try {
            ContextoAutenticacion contexto = contextoService.resolver(request);
            contextoService.exigirAccionCliente(contexto, "nodos", "restaurar");
            if (parametro == null) throw new IllegalArgumentException("Indique el nodo");
            return ResponseEntity.ok(ApiResponse.success(200, respaldos.restaurar(contexto.clienteId(), parametro.getNodoId()), "Restauración iniciada"));
        } catch (Exception error) {
            return error(error);
        }
    }

    @PutMapping("/updatePredeterminado")
    public ResponseEntity<ApiResponse<?>> updatePredeterminado(@RequestBody(required = false) NodoIdParameters parametro, HttpServletRequest request) {
        try {
            ContextoAutenticacion contexto = contextoService.resolver(request);
            contextoService.exigirAccionCliente(contexto, "nodos", "predeterminar");
            return ResponseEntity.ok(ApiResponse.success(200, service.predeterminar(contexto.clienteId(), parametro == null ? null : parametro.getNodoId()),
                    "Nodo por defecto actualizado"));
        } catch (Exception error) {
            return error(error);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<?>> delete(@RequestBody(required = false) NodoIdParameters parametro, HttpServletRequest request) {
        try {
            ContextoAutenticacion contexto = contextoService.resolver(request);
            contextoService.exigirAccionCliente(contexto, "nodos", "eliminar");
            if (parametro == null) throw new IllegalArgumentException("Indique el nodo");
            return ResponseEntity.ok(ApiResponse.success(200, service.eliminar(contexto.clienteId(), parametro.getNodoId()), "Nodo eliminado correctamente"));
        } catch (Exception error) {
            return error(error);
        }
    }

    private ResponseEntity<ApiResponse<?>> error(Exception error) {
        return error instanceof SecurityException
                ? ResponseEntity.status(403).body(ApiResponse.error(403, error.getMessage()))
                : ResponseEntity.badRequest().body(ApiResponse.error(400, error.getMessage()));
    }
}

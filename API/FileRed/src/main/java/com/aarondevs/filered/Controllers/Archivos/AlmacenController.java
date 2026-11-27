package com.aarondevs.filered.Controllers.Archivos;

import com.aarondevs.filered.Parameters.Archivos.AlmacenActualizarParameters;
import com.aarondevs.filered.Parameters.Archivos.AlmacenAgregarParameters;
import com.aarondevs.filered.Parameters.Archivos.AlmacenBuscarParameters;
import com.aarondevs.filered.Parameters.Archivos.AlmacenIdParameters;
import com.aarondevs.filered.Parameters.Response.ApiResponse;
import com.aarondevs.filered.Services.Archivos.ArchivoService;
import com.aarondevs.filered.Services.Archivos.DestinoArchivoService;
import com.aarondevs.filered.Services.Seguridad.ContextoAutenticacionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/archivos/almacenes")
public class AlmacenController {
    private final DestinoArchivoService destinos;
    private final ContextoAutenticacionService contextoService;

    public AlmacenController(DestinoArchivoService destinos, ContextoAutenticacionService contextoService) {
        this.destinos = destinos;
        this.contextoService = contextoService;
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<?>> add(@RequestBody AlmacenAgregarParameters parametro, HttpServletRequest request) {
        try {
            var contexto = contextoService.resolver(request);
            contextoService.exigirAccionCliente(contexto, "archivos", "gestionar_almacenes");
            if (parametro == null) throw new IllegalArgumentException("El nombre del almacén es requerido");
            ArchivoService service = destinos.seleccionado(contexto.clienteId(), parametro == null ? null : parametro.getNodoId());
            return ResponseEntity.status(201).body(ApiResponse.success(201, service.crearAlmacen(contexto.clienteId(), texto(parametro.getAlmacen())),
                    "Almacén creado correctamente"));
        } catch (Exception error) {
            return error(error);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<?>> update(@RequestBody AlmacenActualizarParameters parametro, HttpServletRequest request) {
        try {
            var contexto = contextoService.resolver(request);
            contextoService.exigirAccionCliente(contexto, "archivos", "gestionar_almacenes");
            if (parametro == null) throw new IllegalArgumentException("almacen y nombre son requeridos");
            ArchivoService service = destinos.seleccionado(contexto.clienteId(), parametro == null ? null : parametro.getNodoId());
            return ResponseEntity.ok(ApiResponse.success(200, service.renombrarAlmacen(contexto.clienteId(), texto(parametro.getAlmacen()),
                    texto(parametro.getNombre()), contexto.usuarioUuid()), "Almacén renombrado correctamente"));
        } catch (Exception error) {
            return error(error);
        }
    }

    @PostMapping("/getAll")
    public ResponseEntity<ApiResponse<?>> getAll(@RequestBody(required = false) AlmacenBuscarParameters parametro, HttpServletRequest request) {
        try {
            var contexto = contextoService.resolver(request);
            contextoService.exigirAccion(contexto, "clientes", "listar", "archivos", "listar");
            Long clienteId = contextoService.clienteDeContexto(contexto, parametro == null ? null : parametro.getClienteId());
            if (clienteId == null) throw new IllegalArgumentException("clienteId es requerido");
            ArchivoService service = destinos.seleccionado(clienteId, parametro == null ? null : parametro.getNodoId());
            return ResponseEntity.ok(ApiResponse.success(200, service.almacenes(clienteId), "Almacenes obtenidos correctamente"));
        } catch (Exception error) {
            return error(error);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<?>> delete(@RequestBody AlmacenIdParameters parametro, HttpServletRequest request) {
        try {
            var contexto = contextoService.resolver(request);
            contextoService.exigirAccionCliente(contexto, "archivos", "gestionar_almacenes");
            if (parametro == null) throw new IllegalArgumentException("almacen es requerido");
            ArchivoService service = destinos.seleccionado(contexto.clienteId(), parametro == null ? null : parametro.getNodoId());
            return ResponseEntity.ok(ApiResponse.success(200, service.eliminarAlmacen(contexto.clienteId(), texto(parametro.getAlmacen())),
                    "Almacén eliminado correctamente"));
        } catch (Exception error) {
            return error(error);
        }
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private ResponseEntity<ApiResponse<?>> error(Exception error) {
        return error instanceof SecurityException
                ? ResponseEntity.status(403).body(ApiResponse.error(403, error.getMessage()))
                : ResponseEntity.badRequest().body(ApiResponse.error(400, error.getMessage()));
    }
}

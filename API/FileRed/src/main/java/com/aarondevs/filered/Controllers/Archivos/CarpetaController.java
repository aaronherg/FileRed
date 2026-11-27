package com.aarondevs.filered.Controllers.Archivos;

import com.aarondevs.filered.Parameters.Archivos.CarpetaActualizarParameters;
import com.aarondevs.filered.Parameters.Archivos.CarpetaAgregarParameters;
import com.aarondevs.filered.Parameters.Archivos.CarpetaBuscarParameters;
import com.aarondevs.filered.Parameters.Archivos.CarpetaIdParameters;
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
@RequestMapping("/api/archivos/carpetas")
public class CarpetaController {
    private final DestinoArchivoService destinos;
    private final ContextoAutenticacionService contextoService;

    public CarpetaController(DestinoArchivoService destinos, ContextoAutenticacionService contextoService) {
        this.destinos = destinos;
        this.contextoService = contextoService;
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<?>> add(@RequestBody CarpetaAgregarParameters parametro, HttpServletRequest request) {
        try {
            var contexto = contextoService.resolver(request);
            contextoService.exigirAccionCliente(contexto, "archivos", "gestionar_carpetas");
            if (parametro == null) throw new IllegalArgumentException("almacen y categoria son requeridos");
            ArchivoService service = destinos.seleccionado(contexto.clienteId(), parametro == null ? null : parametro.getNodoId());
            return ResponseEntity.status(201).body(ApiResponse.success(201, service.crearCarpeta(contexto.clienteId(),
                    texto(parametro.getAlmacen()), texto(parametro.getCategoria())), "Carpeta creada correctamente"));
        } catch (Exception error) {
            return error(error);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<?>> update(@RequestBody CarpetaActualizarParameters parametro, HttpServletRequest request) {
        try {
            var contexto = contextoService.resolver(request);
            contextoService.exigirAccionCliente(contexto, "archivos", "gestionar_carpetas");
            if (parametro == null) throw new IllegalArgumentException("almacen, categoria y nombre son requeridos");
            ArchivoService service = destinos.seleccionado(contexto.clienteId(), parametro == null ? null : parametro.getNodoId());
            return ResponseEntity.ok(ApiResponse.success(200, service.renombrarCarpeta(contexto.clienteId(), texto(parametro.getAlmacen()),
                    texto(parametro.getCategoria()), texto(parametro.getNombre()), contexto.usuarioUuid()), "Carpeta renombrada correctamente"));
        } catch (Exception error) {
            return error(error);
        }
    }

    @PostMapping("/getAll")
    public ResponseEntity<ApiResponse<?>> getAll(@RequestBody CarpetaBuscarParameters parametro, HttpServletRequest request) {
        try {
            var contexto = contextoService.resolver(request);
            contextoService.exigirAccion(contexto, "clientes", "listar", "archivos", "listar");
            if (parametro == null) throw new IllegalArgumentException("almacen es requerido");
            Long clienteId = contextoService.clienteDeContexto(contexto, parametro.getClienteId());
            if (clienteId == null) throw new IllegalArgumentException("clienteId es requerido");
            ArchivoService service = destinos.seleccionado(clienteId, parametro == null ? null : parametro.getNodoId());
            return ResponseEntity.ok(ApiResponse.success(200, service.carpetas(clienteId, texto(parametro.getAlmacen())),
                    "Carpetas obtenidas correctamente"));
        } catch (Exception error) {
            return error(error);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<?>> delete(@RequestBody CarpetaIdParameters parametro, HttpServletRequest request) {
        try {
            var contexto = contextoService.resolver(request);
            contextoService.exigirAccionCliente(contexto, "archivos", "gestionar_carpetas");
            if (parametro == null) throw new IllegalArgumentException("almacen y categoria son requeridos");
            ArchivoService service = destinos.seleccionado(contexto.clienteId(), parametro == null ? null : parametro.getNodoId());
            return ResponseEntity.ok(ApiResponse.success(200, service.eliminarCarpeta(contexto.clienteId(), texto(parametro.getAlmacen()),
                    texto(parametro.getCategoria())), "Carpeta eliminada correctamente"));
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

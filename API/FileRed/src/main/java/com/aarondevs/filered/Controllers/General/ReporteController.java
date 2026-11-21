package com.aarondevs.filered.Controllers.General;

import com.aarondevs.filered.Parameters.General.ReporteParameters;
import com.aarondevs.filered.Parameters.Response.ApiResponse;
import com.aarondevs.filered.Services.General.ReporteService;
import com.aarondevs.filered.Services.Seguridad.ContextoAutenticacionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/general/reportes")
public class ReporteController {
    private final ReporteService service;
    private final ContextoAutenticacionService contextoService;

    public ReporteController(ReporteService service, ContextoAutenticacionService contextoService) {
        this.service = service;
        this.contextoService = contextoService;
    }

    @PostMapping("/getIngresos")
    public ResponseEntity<ApiResponse<?>> getIngresos(@RequestBody(required = false) ReporteParameters parametro, HttpServletRequest request) {
        try {
            var contexto = contextoService.resolver(request);
            contextoService.exigirAccionAdministracion(contexto, "reporte_ingresos", "consultar");
            return ResponseEntity.ok(ApiResponse.success(200, service.ingresos(parametro), "Reporte generado correctamente"));
        } catch (Exception error) {
            return error(error);
        }
    }

    @PostMapping("/getCuentasCobrar")
    public ResponseEntity<ApiResponse<?>> getCuentasCobrar(@RequestBody(required = false) ReporteParameters parametro, HttpServletRequest request) {
        try {
            var contexto = contextoService.resolver(request);
            contextoService.exigirAccionAdministracion(contexto, "reporte_cobros", "consultar");
            return ResponseEntity.ok(ApiResponse.success(200, service.cuentasPorCobrar(), "Reporte generado correctamente"));
        } catch (Exception error) {
            return error(error);
        }
    }

    @PostMapping("/getConsumoClientes")
    public ResponseEntity<ApiResponse<?>> getConsumoClientes(@RequestBody(required = false) ReporteParameters parametro, HttpServletRequest request) {
        try {
            var contexto = contextoService.resolver(request);
            contextoService.exigirAccionAdministracion(contexto, "reporte_consumo", "consultar");
            return ResponseEntity.ok(ApiResponse.success(200, service.consumoClientes(parametro), "Reporte generado correctamente"));
        } catch (Exception error) {
            return error(error);
        }
    }

    @PostMapping("/getUsoApi")
    public ResponseEntity<ApiResponse<?>> getUsoApi(@RequestBody(required = false) ReporteParameters parametro, HttpServletRequest request) {
        try {
            var contexto = contextoService.resolver(request);
            contextoService.exigirAccionAdministracion(contexto, "reporte_api", "consultar");
            return ResponseEntity.ok(ApiResponse.success(200, service.usoApi(parametro), "Reporte generado correctamente"));
        } catch (Exception error) {
            return error(error);
        }
    }

    @PostMapping("/getConsumo")
    public ResponseEntity<ApiResponse<?>> getConsumo(@RequestBody(required = false) ReporteParameters parametro, HttpServletRequest request) {
        try {
            var contexto = contextoService.resolver(request);
            contextoService.exigirAccionCliente(contexto, "reporte_consumo", "consultar");
            return ResponseEntity.ok(ApiResponse.success(200, service.consumoCliente(contexto.clienteId()), "Reporte generado correctamente"));
        } catch (Exception error) {
            return error(error);
        }
    }

    @PostMapping("/getActividad")
    public ResponseEntity<ApiResponse<?>> getActividad(@RequestBody(required = false) ReporteParameters parametro, HttpServletRequest request) {
        try {
            var contexto = contextoService.resolver(request);
            contextoService.exigirAccionCliente(contexto, "reporte_actividad", "consultar");
            return ResponseEntity.ok(ApiResponse.success(200, service.actividadCliente(contexto.clienteId(), parametro), "Reporte generado correctamente"));
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

package com.aarondevs.filered.Controllers.FileRed;

import com.aarondevs.filered.Parameters.Response.ApiResponse;
import com.aarondevs.filered.Services.Suscripciones.SuscripcionService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fileRed/cuenta")
public class FileRedCuentaController {
    private final SuscripcionService suscripciones;
    private final FileRedApiSupport support;

    public FileRedCuentaController(SuscripcionService suscripciones, FileRedApiSupport support) {
        this.suscripciones = suscripciones;
        this.support = support;
    }

    @GetMapping("/getById")
    public ResponseEntity<ApiResponse<?>> getById(HttpServletRequest request) {
        try {
            var llave = support.autenticar(request, "consultar_cuenta");
            Map<String, Object> consumo = suscripciones.consumo(llave.getClienteId());
            Map<String, Object> resultado = new LinkedHashMap<>();
            resultado.put("plan", consumo.get("vigente"));
            resultado.put("espacioUsadoBytes", consumo.get("espacioUsadoBytes"));
            resultado.put("espacioLimiteBytes", consumo.get("espacioLimiteBytes"));
            resultado.put("espacioIlimitado", consumo.get("espacioIlimitado"));
            resultado.put("permisos", llave.getPermisos().split(","));
            return ResponseEntity.ok(ApiResponse.success(200, resultado, "Cuenta obtenida correctamente"));
        } catch (Exception error) {
            return support.error(error);
        }
    }
}

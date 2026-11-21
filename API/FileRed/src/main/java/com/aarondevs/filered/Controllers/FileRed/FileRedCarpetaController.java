package com.aarondevs.filered.Controllers.FileRed;

import com.aarondevs.filered.Parameters.Archivos.CarpetaActualizarParameters;
import com.aarondevs.filered.Parameters.Archivos.CarpetaAgregarParameters;
import com.aarondevs.filered.Parameters.Archivos.CarpetaBuscarParameters;
import com.aarondevs.filered.Parameters.Archivos.CarpetaIdParameters;
import com.aarondevs.filered.Parameters.Response.ApiResponse;
import com.aarondevs.filered.Services.Archivos.ArchivoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fileRed/carpetas")
public class FileRedCarpetaController {
    private final ArchivoService service;
    private final FileRedApiSupport support;

    public FileRedCarpetaController(ArchivoService service, FileRedApiSupport support) {
        this.service = service;
        this.support = support;
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<?>> add(@RequestBody(required = false) CarpetaAgregarParameters parametro, HttpServletRequest request) {
        try {
            var llave = support.autenticar(request, "agregar_carpeta");
            if (parametro == null) throw new IllegalArgumentException("almacen y categoria son requeridos");
            return ResponseEntity.status(201).body(ApiResponse.success(201, service.crearCarpeta(llave.getClienteId(),
                    support.texto(parametro.getAlmacen()), support.texto(parametro.getCategoria())), "Carpeta creada correctamente"));
        } catch (Exception error) {
            return support.error(error);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<?>> update(@RequestBody(required = false) CarpetaActualizarParameters parametro, HttpServletRequest request) {
        try {
            var llave = support.autenticar(request, "actualizar_carpeta");
            if (parametro == null) throw new IllegalArgumentException("almacen, categoria y nombre son requeridos");
            return ResponseEntity.ok(ApiResponse.success(200, service.renombrarCarpeta(llave.getClienteId(), support.texto(parametro.getAlmacen()),
                    support.texto(parametro.getCategoria()), support.texto(parametro.getNombre()), support.actor(llave)),
                    "Carpeta renombrada correctamente"));
        } catch (Exception error) {
            return support.error(error);
        }
    }

    @PostMapping("/getAll")
    public ResponseEntity<ApiResponse<?>> getAll(@RequestBody(required = false) CarpetaBuscarParameters parametro, HttpServletRequest request) {
        try {
            var llave = support.autenticar(request, "obtener_carpetas");
            if (parametro == null) throw new IllegalArgumentException("almacen es requerido");
            return ResponseEntity.ok(ApiResponse.success(200, service.carpetas(llave.getClienteId(), support.texto(parametro.getAlmacen())),
                    "Carpetas obtenidas correctamente"));
        } catch (Exception error) {
            return support.error(error);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<?>> delete(@RequestBody(required = false) CarpetaIdParameters parametro, HttpServletRequest request) {
        try {
            var llave = support.autenticar(request, "eliminar_carpeta");
            if (parametro == null) throw new IllegalArgumentException("almacen y categoria son requeridos");
            return ResponseEntity.ok(ApiResponse.success(200, service.eliminarCarpeta(llave.getClienteId(), support.texto(parametro.getAlmacen()),
                    support.texto(parametro.getCategoria())), "Carpeta eliminada correctamente"));
        } catch (Exception error) {
            return support.error(error);
        }
    }
}

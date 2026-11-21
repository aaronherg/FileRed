package com.aarondevs.filered.Controllers.FileRed;

import com.aarondevs.filered.Parameters.Archivos.AlmacenActualizarParameters;
import com.aarondevs.filered.Parameters.Archivos.AlmacenAgregarParameters;
import com.aarondevs.filered.Parameters.Archivos.AlmacenIdParameters;
import com.aarondevs.filered.Parameters.Response.ApiResponse;
import com.aarondevs.filered.Services.Archivos.ArchivoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fileRed/almacenes")
public class FileRedAlmacenController {
    private final ArchivoService service;
    private final FileRedApiSupport support;

    public FileRedAlmacenController(ArchivoService service, FileRedApiSupport support) {
        this.service = service;
        this.support = support;
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<?>> add(@RequestBody(required = false) AlmacenAgregarParameters parametro, HttpServletRequest request) {
        try {
            var llave = support.autenticar(request, "agregar_almacen");
            if (parametro == null) throw new IllegalArgumentException("almacen es requerido");
            return ResponseEntity.status(201).body(ApiResponse.success(201,
                    service.crearAlmacen(llave.getClienteId(), support.texto(parametro.getAlmacen())), "Almacén creado correctamente"));
        } catch (Exception error) {
            return support.error(error);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<?>> update(@RequestBody(required = false) AlmacenActualizarParameters parametro, HttpServletRequest request) {
        try {
            var llave = support.autenticar(request, "actualizar_almacen");
            if (parametro == null) throw new IllegalArgumentException("almacen y nombre son requeridos");
            return ResponseEntity.ok(ApiResponse.success(200, service.renombrarAlmacen(llave.getClienteId(), support.texto(parametro.getAlmacen()),
                    support.texto(parametro.getNombre()), support.actor(llave)), "Almacén renombrado correctamente"));
        } catch (Exception error) {
            return support.error(error);
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse<?>> getAll(HttpServletRequest request) {
        try {
            var llave = support.autenticar(request, "obtener_almacenes");
            return ResponseEntity.ok(ApiResponse.success(200, service.almacenes(llave.getClienteId()), "Almacenes obtenidos correctamente"));
        } catch (Exception error) {
            return support.error(error);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<?>> delete(@RequestBody(required = false) AlmacenIdParameters parametro, HttpServletRequest request) {
        try {
            var llave = support.autenticar(request, "eliminar_almacen");
            if (parametro == null) throw new IllegalArgumentException("almacen es requerido");
            return ResponseEntity.ok(ApiResponse.success(200, service.eliminarAlmacen(llave.getClienteId(), support.texto(parametro.getAlmacen())),
                    "Almacén eliminado correctamente"));
        } catch (Exception error) {
            return support.error(error);
        }
    }
}

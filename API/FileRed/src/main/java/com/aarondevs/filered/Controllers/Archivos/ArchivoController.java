package com.aarondevs.filered.Controllers.Archivos;

import com.aarondevs.filered.Parameters.Archivos.ArchivoAgregarParameters;
import com.aarondevs.filered.Parameters.Archivos.ArchivoBuscarParameters;
import com.aarondevs.filered.Parameters.Archivos.ArchivoIdParameters;
import com.aarondevs.filered.Parameters.Archivos.ArchivoNombreParameters;
import com.aarondevs.filered.Parameters.Response.ApiResponse;
import com.aarondevs.filered.Services.Archivos.ArchivoService;
import com.aarondevs.filered.Services.Seguridad.ContextoAutenticacionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/archivos")
public class ArchivoController {
    private final ArchivoService service;
    private final ContextoAutenticacionService contextoService;

    public ArchivoController(ArchivoService service, ContextoAutenticacionService contextoService) {
        this.service = service;
        this.contextoService = contextoService;
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<?>> add(@ModelAttribute ArchivoAgregarParameters parametro,
                                              @RequestParam(value = "file", required = false) MultipartFile file,
                                              HttpServletRequest request) {
        try {
            var contexto = contextoService.resolver(request);
            contextoService.exigirAccionCliente(contexto, "archivos", "subir");
            return ResponseEntity.status(201).body(ApiResponse.success(201, service.subir(contexto.clienteId(), texto(parametro.getAlmacen()),
                    texto(parametro.getCategoria()), file, null, contexto.usuarioUuid(), false), "Archivo subido correctamente"));
        } catch (Exception error) {
            return error(error);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<?>> update(@ModelAttribute ArchivoIdParameters parametro,
                                                 @RequestParam(value = "file", required = false) MultipartFile file,
                                                 HttpServletRequest request) {
        try {
            var contexto = contextoService.resolver(request);
            contextoService.exigirAccionCliente(contexto, "archivos", "subir");
            return ResponseEntity.ok(ApiResponse.success(200, service.reemplazar(contexto.clienteId(), texto(parametro.getAlmacen()),
                    texto(parametro.getCategoria()), texto(parametro.getIdentificador()), texto(parametro.getExtension()), file,
                    contexto.usuarioUuid(), false), "Archivo reemplazado correctamente"));
        } catch (Exception error) {
            return error(error);
        }
    }

    @PostMapping("/getAllPages")
    public ResponseEntity<ApiResponse<?>> getAllPages(@RequestBody ArchivoBuscarParameters parametro, HttpServletRequest request) {
        try {
            var contexto = contextoService.resolver(request);
            contextoService.exigirAccion(contexto, "clientes", "listar", "archivos", "listar");
            if (parametro == null) throw new IllegalArgumentException("almacen y categoria son requeridos");
            Long clienteId = contextoService.clienteDeContexto(contexto, parametro.getClienteId());
            if (clienteId == null) throw new IllegalArgumentException("clienteId es requerido");
            return ResponseEntity.ok(ApiResponse.success(200, service.paginas(clienteId, texto(parametro.getAlmacen()), texto(parametro.getCategoria()),
                    parametro.getBuscar(), parametro.getPage(), parametro.getSize()), "Archivos obtenidos correctamente"));
        } catch (Exception error) {
            return error(error);
        }
    }

    @PutMapping("/updateNombre")
    public ResponseEntity<ApiResponse<?>> updateNombre(@RequestBody ArchivoNombreParameters parametro, HttpServletRequest request) {
        try {
            var contexto = contextoService.resolver(request);
            contextoService.exigirAccionCliente(contexto, "archivos", "subir");
            if (parametro == null) throw new IllegalArgumentException("Indique el archivo a renombrar");
            return ResponseEntity.ok(ApiResponse.success(200, service.renombrarArchivo(contexto.clienteId(), texto(parametro.getAlmacen()),
                    texto(parametro.getCategoria()), texto(parametro.getIdentificador()), parametro.getNombre(), contexto.usuarioUuid()),
                    "Archivo renombrado correctamente"));
        } catch (Exception error) {
            return error(error);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<?>> delete(@RequestBody ArchivoIdParameters parametro, HttpServletRequest request) {
        try {
            var contexto = contextoService.resolver(request);
            contextoService.exigirAccionCliente(contexto, "archivos", "eliminar");
            if (parametro == null) throw new IllegalArgumentException("Indique el archivo a eliminar");
            return ResponseEntity.ok(ApiResponse.success(200, service.eliminar(contexto.clienteId(), texto(parametro.getAlmacen()),
                    texto(parametro.getCategoria()), texto(parametro.getIdentificador()), texto(parametro.getExtension())),
                    "Archivo eliminado correctamente"));
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

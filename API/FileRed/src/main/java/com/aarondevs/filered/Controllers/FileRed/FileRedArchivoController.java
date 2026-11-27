package com.aarondevs.filered.Controllers.FileRed;

import com.aarondevs.filered.Parameters.Archivos.ArchivoAgregarParameters;
import com.aarondevs.filered.Parameters.Archivos.ArchivoBuscarParameters;
import com.aarondevs.filered.Parameters.Archivos.ArchivoNombreParameters;
import com.aarondevs.filered.Parameters.Archivos.FileRedArchivoIdParameters;
import com.aarondevs.filered.Parameters.Response.ApiResponse;
import com.aarondevs.filered.Services.Archivos.ArchivoService;
import com.aarondevs.filered.Services.Archivos.DestinoArchivoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import java.util.Map;
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
@RequestMapping("/api/fileRed/archivos")
public class FileRedArchivoController {
    private final DestinoArchivoService destinos;
    private final FileRedApiSupport support;

    public FileRedArchivoController(DestinoArchivoService destinos, FileRedApiSupport support) {
        this.destinos = destinos;
        this.support = support;
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<?>> add(@ModelAttribute ArchivoAgregarParameters parametro,
                                              @RequestParam(value = "file", required = false) MultipartFile file,
                                              @RequestParam(value = "base64", required = false) Boolean base64,
                                              HttpServletRequest request) {
        try {
            var llave = support.autenticar(request, "agregar_archivo");
            if (file == null) throw new IllegalArgumentException("Envíe el archivo en el campo file");
            ArchivoService service = destinos.predeterminado(llave.getClienteId());
            return ResponseEntity.status(201).body(ApiResponse.success(201, service.subir(llave.getClienteId(), support.texto(parametro.getAlmacen()),
                    support.texto(parametro.getCategoria()), file, llave.getApiKeyId(), support.actor(llave), Boolean.TRUE.equals(base64)),
                    "Archivo subido correctamente"));
        } catch (Exception error) {
            return support.error(error);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<?>> update(@ModelAttribute FileRedArchivoIdParameters parametro,
                                                 @RequestParam(value = "file", required = false) MultipartFile file,
                                                 HttpServletRequest request) {
        try {
            var llave = support.autenticar(request, "actualizar_archivo");
            if (file == null) throw new IllegalArgumentException("Envíe el archivo nuevo en el campo file");
            ArchivoService service = destinos.deArchivo(llave.getClienteId(), support.texto(parametro.getAlmacen()), support.texto(parametro.getCategoria()), support.texto(parametro.getIdentificador()));
            String almacen = support.texto(parametro.getAlmacen());
            String categoria = support.texto(parametro.getCategoria());
            String identificador = support.texto(parametro.getIdentificador());
            Map<String, Object> actual = service.archivoPorId(llave.getClienteId(), almacen, categoria, identificador);
            return ResponseEntity.ok(ApiResponse.success(200, service.reemplazar(llave.getClienteId(), almacen, categoria, identificador,
                    String.valueOf(actual.get("extension")), file, support.actor(llave), false), "Archivo reemplazado correctamente"));
        } catch (Exception error) {
            return support.error(error);
        }
    }

    @PutMapping("/updateNombre")
    public ResponseEntity<ApiResponse<?>> updateNombre(@RequestBody(required = false) ArchivoNombreParameters parametro, HttpServletRequest request) {
        try {
            var llave = support.autenticar(request, "renombrar_archivo");
            if (parametro == null) throw new IllegalArgumentException("almacen, categoria, identificador y nombre son requeridos");
            ArchivoService service = destinos.deArchivo(llave.getClienteId(), support.texto(parametro.getAlmacen()), support.texto(parametro.getCategoria()), support.texto(parametro.getIdentificador()));
            return ResponseEntity.ok(ApiResponse.success(200, service.renombrarArchivo(llave.getClienteId(), support.texto(parametro.getAlmacen()),
                    support.texto(parametro.getCategoria()), support.texto(parametro.getIdentificador()), parametro.getNombre(), support.actor(llave)),
                    "Archivo renombrado correctamente"));
        } catch (Exception error) {
            return support.error(error);
        }
    }

    @PostMapping("/getAllPages")
    public ResponseEntity<ApiResponse<?>> getAllPages(@RequestBody(required = false) ArchivoBuscarParameters parametro, HttpServletRequest request) {
        try {
            var llave = support.autenticar(request, "obtener_archivos");
            if (parametro == null) throw new IllegalArgumentException("almacen y categoria son requeridos");
            ArchivoService service = destinos.predeterminado(llave.getClienteId());
            return ResponseEntity.ok(ApiResponse.success(200, service.paginas(llave.getClienteId(), support.texto(parametro.getAlmacen()),
                    support.texto(parametro.getCategoria()), parametro.getBuscar(), parametro.getPage(), parametro.getSize()),
                    "Archivos obtenidos correctamente"));
        } catch (Exception error) {
            return support.error(error);
        }
    }

    @PostMapping("/getById")
    public ResponseEntity<ApiResponse<?>> getById(@RequestBody(required = false) FileRedArchivoIdParameters parametro, HttpServletRequest request) {
        try {
            var llave = support.autenticar(request, "obtener_archivo");
            if (parametro == null) throw new IllegalArgumentException("almacen, categoria e identificador son requeridos");
            ArchivoService service = destinos.deArchivo(llave.getClienteId(), support.texto(parametro.getAlmacen()), support.texto(parametro.getCategoria()), support.texto(parametro.getIdentificador()));
            return ResponseEntity.ok(ApiResponse.success(200, service.archivoPorId(llave.getClienteId(), support.texto(parametro.getAlmacen()),
                    support.texto(parametro.getCategoria()), support.texto(parametro.getIdentificador())), "Archivo obtenido correctamente"));
        } catch (Exception error) {
            return support.error(error);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<?>> delete(@RequestBody(required = false) FileRedArchivoIdParameters parametro, HttpServletRequest request) {
        try {
            var llave = support.autenticar(request, "eliminar_archivo");
            if (parametro == null) throw new IllegalArgumentException("almacen, categoria e identificador son requeridos");
            ArchivoService service = destinos.deArchivo(llave.getClienteId(), support.texto(parametro.getAlmacen()), support.texto(parametro.getCategoria()), support.texto(parametro.getIdentificador()));
            String almacen = support.texto(parametro.getAlmacen());
            String categoria = support.texto(parametro.getCategoria());
            String identificador = support.texto(parametro.getIdentificador());
            Map<String, Object> actual = service.archivoPorId(llave.getClienteId(), almacen, categoria, identificador);
            return ResponseEntity.ok(ApiResponse.success(200, service.eliminar(llave.getClienteId(), almacen, categoria, identificador,
                    String.valueOf(actual.get("extension"))), "Archivo eliminado correctamente"));
        } catch (Exception error) {
            return support.error(error);
        }
    }
}

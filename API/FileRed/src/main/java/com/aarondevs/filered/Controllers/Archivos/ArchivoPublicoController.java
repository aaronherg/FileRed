package com.aarondevs.filered.Controllers.Archivos;

import com.aarondevs.filered.Services.Archivos.ArchivoService;
import java.nio.file.Path;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/f")
public class ArchivoPublicoController {
    private final ArchivoService archivos;

    public ArchivoPublicoController(ArchivoService archivos) {
        this.archivos = archivos;
    }

    @GetMapping("/{clienteUuid}/{almacen}/{categoria}/{archivo:.+}")
    public ResponseEntity<Resource> getById(@PathVariable String clienteUuid, @PathVariable String almacen,
                                            @PathVariable String categoria, @PathVariable String archivo) {
        Path ruta = archivos.publico(clienteUuid, almacen, categoria, archivo);
        if (ruta == null) return ResponseEntity.notFound().build();
        String extension = StringUtils.getFilenameExtension(archivo);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivos.mime(extension == null ? "" : extension)))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(archivo).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.noCache())
                .body(new FileSystemResource(ruta));
    }
}

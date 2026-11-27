package com.aarondevs.filered.Controllers.Archivos;

import com.aarondevs.filered.Services.Archivos.ArchivoService;
import com.aarondevs.filered.Services.Archivos.NodoService;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
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
    private final NodoService nodos;

    public ArchivoPublicoController(ArchivoService archivos, NodoService nodos) {
        this.archivos = archivos;
        this.nodos = nodos;
    }

    @GetMapping("/{clienteUuid}/{almacen}/{categoria}/{archivo:.+}")
    public ResponseEntity<?> getById(@PathVariable String clienteUuid, @PathVariable String almacen,
                                     @PathVariable String categoria, @PathVariable String archivo) {
        Path ruta = archivos.publico(clienteUuid, almacen, categoria, archivo);
        if (ruta != null) return respuesta(archivo).body((Resource) new FileSystemResource(ruta));
        NodoService.Entrega entrega;
        try {
            entrega = nodos.publico(clienteUuid, almacen, categoria, archivo);
        } catch (NodoService.NodoNoDisponibleException error) {
            return ResponseEntity.status(503).contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                    .cacheControl(CacheControl.noStore()).body(error.getMessage());
        }
        if (entrega == null) return ResponseEntity.notFound().build();
        if (!entrega.temporal()) return respuesta(archivo).body((Resource) new FileSystemResource(entrega.ruta()));
        Path temporal = entrega.ruta();
        long tamano;
        try {
            tamano = Files.size(temporal);
        } catch (IOException error) {
            return ResponseEntity.status(503).body("No se pudo leer el archivo entregado por el equipo");
        }
        InputStream entrada;
        try {
            entrada = new FilterInputStream(Files.newInputStream(temporal)) {
                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    } finally {
                        Files.deleteIfExists(temporal);
                    }
                }
            };
        } catch (IOException error) {
            return ResponseEntity.status(503).body("No se pudo leer el archivo entregado por el equipo");
        }
        return respuesta(archivo).contentLength(tamano).body((Resource) new InputStreamResource(entrada));
    }

    private ResponseEntity.BodyBuilder respuesta(String archivo) {
        String extension = StringUtils.getFilenameExtension(archivo);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivos.mime(extension == null ? "" : extension)))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(archivo).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.noCache());
    }
}

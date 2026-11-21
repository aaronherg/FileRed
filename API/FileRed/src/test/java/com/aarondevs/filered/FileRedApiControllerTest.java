package com.aarondevs.filered;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aarondevs.filered.Controllers.Archivos.ArchivoPublicoController;
import com.aarondevs.filered.Controllers.FileRed.FileRedAlmacenController;
import com.aarondevs.filered.Controllers.FileRed.FileRedApiSupport;
import com.aarondevs.filered.Controllers.FileRed.FileRedArchivoController;
import com.aarondevs.filered.Controllers.FileRed.FileRedCarpetaController;
import com.aarondevs.filered.DAO.Archivos.ApiKeyDataSet;
import com.aarondevs.filered.DAO.Archivos.ArchivoDataSet;
import com.aarondevs.filered.Models.Archivos.ApiKey;
import com.aarondevs.filered.Models.Archivos.Archivo;
import com.aarondevs.filered.Models.Suscripciones.DetalleSuscripcion;
import com.aarondevs.filered.Models.Suscripciones.Suscripcion;
import com.aarondevs.filered.Parameters.Archivos.ApiKeyAgregarParameters;
import com.aarondevs.filered.Parameters.Response.PaginationResponse;
import com.aarondevs.filered.Parameters.Suscripciones.SuscripcionAsignarParameters;
import com.aarondevs.filered.Services.Archivos.ApiKeyService;
import com.aarondevs.filered.Services.Archivos.ApiKeyServiceImpl;
import com.aarondevs.filered.Services.Archivos.ArchivoServiceImpl;
import com.aarondevs.filered.Services.Suscripciones.SuscripcionService;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class FileRedApiControllerTest {
    private static final String CLIENTE_UUID = "11111111-2222-3333-4444-555555555555";

    @TempDir
    Path almacenamiento;

    private MockMvc mvc;
    private String clave;
    private String claveLectura;

    @BeforeEach
    void preparar() throws Exception {
        ApiKeyService apiKeys = new ApiKeyServiceImpl(new ApiKeysEnMemoria());
        clave = apiKeys.crear(agregar(ApiKeyService.PERMISOS), 1L, "prueba").getClave();
        claveLectura = apiKeys.crear(agregar(List.of("obtener_almacenes")), 1L, "prueba").getClave();
        ArchivoServiceImpl archivos = new ArchivoServiceImpl(new ArchivosEnMemoria(), new SuscripcionDePrueba(),
                almacenamiento.toString(), 100L * 1024 * 1024, "/");
        FileRedApiSupport support = new FileRedApiSupport(apiKeys, archivos);
        mvc = MockMvcBuilders.standaloneSetup(new FileRedAlmacenController(archivos, support), new FileRedCarpetaController(archivos, support),
                new FileRedArchivoController(archivos, support), new ArchivoPublicoController(archivos)).build();
    }

    private static ApiKeyAgregarParameters agregar(List<String> permisos) {
        ApiKeyAgregarParameters parametro = new ApiKeyAgregarParameters();
        parametro.setNombre("Prueba");
        parametro.setPermisos(permisos);
        return parametro;
    }

    @Test
    void exigeApiKeyValidaYPermiso() throws Exception {
        mvc.perform(get("/api/fileRed/almacenes/getAll"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensage").value("Envíe su API key en el encabezado X-Api-Key"));
        mvc.perform(get("/api/fileRed/almacenes/getAll").header("X-Api-Key", "fr_otra"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/fileRed/almacenes/getAll").header("X-Api-Key", claveLectura))
                .andExpect(status().isOk());
        mvc.perform(post("/api/fileRed/almacenes/add").header("X-Api-Key", claveLectura)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"almacen\":\"MiApp\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensage").value("La API key no tiene el permiso agregar_almacen"));
    }

    @Test
    void cicloCompletoDeAlmacenesCarpetasYArchivos() throws Exception {
        mvc.perform(post("/api/fileRed/almacenes/add").header("X-Api-Key", clave)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"almacen\":\"MiApp\"}"))
                .andExpect(status().isCreated());
        mvc.perform(put("/api/fileRed/almacenes/update").header("X-Api-Key", clave)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"almacen\":\"MiApp\",\"nombre\":\"EduversoRD\"}"))
                .andExpect(jsonPath("$.data.almacen").value("EduversoRD"));
        mvc.perform(get("/api/fileRed/almacenes/getAll").header("X-Api-Key", clave))
                .andExpect(jsonPath("$.data[0].almacen").value("EduversoRD"));
        mvc.perform(post("/api/fileRed/carpetas/add").header("X-Api-Key", clave)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"almacen\":\"EduversoRD\",\"categoria\":\"Fotos\"}"))
                .andExpect(status().isCreated());
        mvc.perform(put("/api/fileRed/carpetas/update").header("X-Api-Key", clave)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"almacen\":\"EduversoRD\",\"categoria\":\"Fotos\",\"nombre\":\"Estudiantes\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/fileRed/carpetas/getAll").header("X-Api-Key", clave)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"almacen\":\"EduversoRD\"}"))
                .andExpect(jsonPath("$.data[0].categoria").value("Estudiantes"));

        MockMultipartFile imagen = new MockMultipartFile("file", "foto.PNG", "image/png", new byte[]{1, 2, 3, 4});
        String respuesta = mvc.perform(multipart("/api/fileRed/archivos/add").file(imagen).header("X-Api-Key", clave)
                        .param("almacen", "EduversoRD").param("categoria", "Estudiantes").param("base64", "true"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.extension").value("png"))
                .andExpect(jsonPath("$.data.base64").value("data:image/png;base64,AQIDBA=="))
                .andReturn().getResponse().getContentAsString();
        String identificador = respuesta.replaceAll(".*\"identificador\":\"([0-9a-f]{32})\".*", "$1");
        String publica = "/f/" + CLIENTE_UUID + "/EduversoRD/Estudiantes/" + identificador;
        String id = "{\"almacen\":\"EduversoRD\",\"categoria\":\"Estudiantes\",\"identificador\":\"" + identificador + "\"}";

        mvc.perform(get(publica + ".png")).andExpect(status().isOk()).andExpect(content().bytes(new byte[]{1, 2, 3, 4}));
        mvc.perform(post("/api/fileRed/archivos/getById").header("X-Api-Key", clave).contentType(MediaType.APPLICATION_JSON).content(id))
                .andExpect(jsonPath("$.data.url").value(publica + ".png"))
                .andExpect(jsonPath("$.data.nombreOriginal").value("foto.PNG"));
        mvc.perform(post("/api/fileRed/archivos/getAllPages").header("X-Api-Key", clave).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"almacen\":\"EduversoRD\",\"categoria\":\"Estudiantes\",\"page\":1,\"size\":10}"))
                .andExpect(jsonPath("$.data.records[0].identificador").value(identificador));

        MockMultipartFile pdf = new MockMultipartFile("file", "documento.pdf", "application/pdf", new byte[]{9, 9});
        mvc.perform(multipart("/api/fileRed/archivos/update").file(pdf).header("X-Api-Key", clave)
                        .param("almacen", "EduversoRD").param("categoria", "Estudiantes").param("identificador", identificador)
                        .with(r -> {
                            r.setMethod("PUT");
                            return r;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.extension").value("pdf"));
        mvc.perform(get(publica + ".png")).andExpect(status().isNotFound());

        mvc.perform(delete("/api/fileRed/archivos/delete").header("X-Api-Key", clave).contentType(MediaType.APPLICATION_JSON).content(id))
                .andExpect(status().isOk());
        mvc.perform(post("/api/fileRed/archivos/getById").header("X-Api-Key", clave).contentType(MediaType.APPLICATION_JSON).content(id))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/fileRed/carpetas/delete").header("X-Api-Key", clave).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"almacen\":\"EduversoRD\",\"categoria\":\"Estudiantes\"}"))
                .andExpect(status().isOk());
        mvc.perform(delete("/api/fileRed/almacenes/delete").header("X-Api-Key", clave).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"almacen\":\"EduversoRD\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/fileRed/almacenes/getAll").header("X-Api-Key", clave)).andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void rechazaExtensionesRutasYLimitesDelPlan() throws Exception {
        mvc.perform(post("/api/fileRed/carpetas/add").header("X-Api-Key", clave)
                .contentType(MediaType.APPLICATION_JSON).content("{\"almacen\":\"A\",\"categoria\":\"B\"}"));
        MockMultipartFile exe = new MockMultipartFile("file", "virus.exe", "application/octet-stream", new byte[]{1});
        mvc.perform(multipart("/api/fileRed/archivos/add").file(exe).header("X-Api-Key", clave).param("almacen", "A").param("categoria", "B"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensage").value("Extensión no permitida: exe"));
        MockMultipartFile grande = new MockMultipartFile("file", "grande.pdf", "application/pdf", new byte[2 * 1024 * 1024]);
        mvc.perform(multipart("/api/fileRed/archivos/add").file(grande).header("X-Api-Key", clave).param("almacen", "A").param("categoria", "B"))
                .andExpect(jsonPath("$.mensage").value("El archivo supera el tamaño máximo de 1MB de su plan"));
        mvc.perform(post("/api/fileRed/carpetas/getAll").header("X-Api-Key", clave)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"almacen\":\"..\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/f/" + CLIENTE_UUID + "/A/../secreto.txt")).andExpect(status().isNotFound());
    }

    private static class ApiKeysEnMemoria implements ApiKeyDataSet {
        private final List<ApiKey> llaves = new ArrayList<>();

        @Override
        public List<ApiKey> findAll(Long clienteId) {
            return llaves;
        }

        @Override
        public ApiKey findById(Long apiKeyId) {
            return llaves.stream().filter(k -> k.getApiKeyId().equals(apiKeyId)).findFirst().orElse(null);
        }

        @Override
        public ApiKey findActivaByHash(String hash) {
            return llaves.stream().filter(k -> k.getHash().equals(hash)).findFirst().orElse(null);
        }

        @Override
        public Long create(Long clienteId, String nombre, String prefijo, String hash, String permisos, String actor) {
            ApiKey llave = new ApiKey();
            llave.setApiKeyId((long) llaves.size() + 1);
            llave.setApiKeyUuid("llave-" + llave.getApiKeyId());
            llave.setClienteId(clienteId);
            llave.setNombre(nombre);
            llave.setPrefijo(prefijo);
            llave.setHash(hash);
            llave.setPermisos(permisos);
            llaves.add(llave);
            return llave.getApiKeyId();
        }

        @Override
        public int update(Long apiKeyId, String nombre, String permisos, String estado, String actor) {
            return 0;
        }

        @Override
        public int revocar(Long apiKeyId, String actor) {
            return 0;
        }

        @Override
        public void registrarUso(Long apiKeyId) {
        }

        @Override
        public long countActivas(Long clienteId) {
            return llaves.size();
        }
    }

    private static class ArchivosEnMemoria implements ArchivoDataSet {
        private final List<Archivo> archivos = new ArrayList<>();

        @Override
        public Archivo findByRuta(Long clienteId, String almacen, String categoria, String identificador) {
            return archivos.stream().filter(a -> a.getIdentificador().equals(identificador)).findFirst().orElse(null);
        }

        @Override
        public List<Archivo> findAll(Long clienteId, String almacen, String categoria) {
            return archivos;
        }

        @Override
        public void create(Long clienteId, String almacen, String categoria, String identificador, String extension, String nombreOriginal,
                           long tamano, Long apiKeyId, String actor) {
            Archivo archivo = new Archivo();
            archivo.setArchivoId((long) archivos.size() + 1);
            archivo.setAlmacen(almacen);
            archivo.setCategoria(categoria);
            archivo.setIdentificador(identificador);
            archivo.setExtension(extension);
            archivo.setNombreOriginal(nombreOriginal);
            archivo.setTamano(tamano);
            archivos.add(archivo);
        }

        @Override
        public void update(Long archivoId, String extension, String nombreOriginal, long tamano, String actor) {
            archivos.stream().filter(a -> a.getArchivoId().equals(archivoId)).forEach(a -> {
                a.setExtension(extension);
                a.setNombreOriginal(nombreOriginal);
                a.setTamano(tamano);
            });
        }

        @Override
        public int delete(Long clienteId, String almacen, String categoria, String identificador) {
            return archivos.removeIf(a -> a.getIdentificador().equals(identificador)) ? 1 : 0;
        }

        @Override
        public int deleteCategoria(Long clienteId, String almacen, String categoria) {
            int total = archivos.size();
            archivos.removeIf(a -> a.getCategoria().equals(categoria));
            return total - archivos.size();
        }

        @Override
        public String clienteUuid(Long clienteId) {
            return CLIENTE_UUID;
        }

        @Override
        public Long clienteActivoPorUuid(String clienteUuid) {
            return CLIENTE_UUID.equals(clienteUuid) ? 1L : null;
        }

        @Override
        public void sumarMetrica(Long clienteId, String campo, long cantidad) {
        }

        @Override
        public PaginationResponse<Archivo> findAllPages(Long clienteId, String almacen, String categoria, String buscar, Integer page, Integer size) {
            List<Archivo> filas = archivos.stream().filter(a -> a.getAlmacen().equals(almacen) && a.getCategoria().equals(categoria)).toList();
            return new PaginationResponse<>(1, 25, (long) filas.size(), 1, false, false, filas);
        }

        @Override
        public List<Map<String, Object>> resumenAlmacenes(Long clienteId) {
            return List.of();
        }

        @Override
        public List<Map<String, Object>> resumenCarpetas(Long clienteId, String almacen) {
            return List.of();
        }

        @Override
        public int renombrarAlmacen(Long clienteId, String almacen, String nombre, String actor) {
            archivos.stream().filter(a -> a.getAlmacen().equals(almacen)).forEach(a -> a.setAlmacen(nombre));
            return 1;
        }

        @Override
        public int renombrarCategoria(Long clienteId, String almacen, String categoria, String nombre, String actor) {
            archivos.stream().filter(a -> a.getCategoria().equals(categoria)).forEach(a -> a.setCategoria(nombre));
            return 1;
        }

        @Override
        public int deleteAlmacen(Long clienteId, String almacen) {
            int total = archivos.size();
            archivos.removeIf(a -> a.getAlmacen().equals(almacen));
            return total - archivos.size();
        }

        @Override
        public int updateNombre(Long clienteId, String almacen, String categoria, String identificador, String nombreOriginal, String actor) {
            return 1;
        }
    }

    private static class SuscripcionDePrueba implements SuscripcionService {
        @Override
        public Suscripcion asignar(SuscripcionAsignarParameters parametro, String actor, Long usuarioAdministracionId) {
            return null;
        }

        @Override
        public int cancelar(Long clienteId, String actor) {
            return 0;
        }

        @Override
        public Suscripcion cliente(Long clienteId) {
            return null;
        }

        @Override
        public Object pages(Integer page, Integer size, String buscar) {
            return null;
        }

        @Override
        public DetalleSuscripcion vigente(Long clienteId) {
            DetalleSuscripcion plan = new DetalleSuscripcion();
            plan.setPlanNombre("Prueba");
            plan.setEspacioGb(BigDecimal.ONE);
            plan.setTamanoMaximoArchivoMb(1);
            plan.setPermitirAdicionales(false);
            return plan;
        }

        @Override
        public Object historial(Long clienteId) {
            return List.of();
        }

        @Override
        public long espacioUsado(Long clienteId) {
            return 0;
        }

        @Override
        public Map<String, Object> consumo(Long clienteId) {
            return Map.of();
        }

        @Override
        public void renovarCadena(DetalleSuscripcion vencido, LocalDateTime limite) {
        }

        @Override
        public List<DetalleSuscripcion> pendientesRenovacion(LocalDateTime limite) {
            return List.of();
        }
    }
}

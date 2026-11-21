<div align="center" width="50">

  <img src="https://github.com/aaronherg/aaronherg/blob/main/recursos/fileredLogo.png?raw=true" 
       alt="FileRed Logo" width="50%"/><br> 

  <p><strong>FileRed</strong></p>
  <p>Servicio de alquiler de almacenamiento de archivos con API, área de administración y área de cliente.</p>
  <p><code>v1.0.1</code></p>

</div>

<hr>

## Plataforma

FileRed es un servicio de alquiler de almacenamiento de archivos con API:

| Ruta | Área |
|---|---|
| `/` → `/pages/` | Portal público: producto, documentación de la API, planes publicados y preguntas frecuentes. |
| `/pages/login/` | Inicio de sesión para ambas áreas. |
| `/pages/registro/` | Registro de clientes (crea cliente, suscripción, rol Propietario y usuario propietario). |
| `/pages/center/` | Administración. |
| `/pages/customer/` | Área de cliente. |

Menú por categorías, igual que Coopiteco (buscador de opciones con `Ctrl+K`):

| Center | Customer |
|---|---|
| **Dashboard** | **Dashboard** |
| **Gestión:** Clientes, Suscripciones (y solicitudes), Pagos | **Almacenamiento:** Explorador de archivos |
| **Reportes:** Ingresos, Cuentas por cobrar, Consumo por cliente, Uso de la API | **Facturación:** Mi plan, Pagos |
| **Configuración:** Planes, Monedas, Usuarios, Roles | **Reportes:** Consumo de almacenamiento, Actividad de la API |
| | **Configuración:** API keys, Usuarios, Roles, Perfil |

- **Explorador de archivos** como un servidor de archivos: Almacenes → Carpetas → Archivos paginados. Subida con arrastrar y soltar y progreso, vista previa, renombrar, reemplazar, descargar, copiar URL y eliminar (también en lote).
- Cada cliente tiene su equipo de usuarios y roles, y varias API keys con un permiso por acción.
- **Planes:** espacio en GB y tamaño máximo por archivo (ambos pueden ser ilimitados), visibles u ocultos en el portal. Prepago o pospago; el pospago se renueva cada medianoche (America/Santo_Domingo) y, si permite adicionales, cobra los GB extra.
- Pagos con abonos parciales; el cargo pasa a `Pagado` al completarse.
- Reportes con filtro de fechas, gráficos y exportación CSV.
- Interfaz oscura futurista (con tema claro), en español e inglés (`pages/files/languages`).

## API

El módulo Java está en `API/FileRed` (Spring Boot, Java 17, Maven Wrapper). Estructura por módulo (`Seguridad`, `Administracion`, `Clientes`, `Suscripciones`, `Archivos`, `General`) en `Controllers`, `Services` (interfaz + `Impl`), `DAO` (`DataSet` + `DAO`), `Models` y `Parameters`. Rutas `/api/<modulo>/<recurso>/add|update|getAll|getAllPages|getById|delete`, respuestas `ApiResponse`.

```powershell
cd API\FileRed
.\mvnw.cmd spring-boot:run
```

- Puerto: `8080`, definido directamente en el `.env` (`server.port=8080`).
- Configuración: `API/FileRed/opt/filered/.env` (no se versiona; contiene credenciales).
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Catálogo de permisos: `resources/permisos/administracion.json` y `clientes.json` (`categorias` → `opciones` → `acciones`), sincronizados al iniciar.
- Migraciones Flyway en `resources/db/migration` (`V1` a `V4`), se aplican solas al iniciar.
- Pruebas: `.\mvnw.cmd test` (MockMvc de la API FileRed).

### Variables propias de FileRed (`.env`)

| Variable | Uso |
|---|---|
| `FILERED_STORAGE_PATH` | Carpeta física de los archivos; cada cliente usa `{clienteUuid}/{almacen}/{categoria}`. |
| `FILERED_MAX_FILE_SIZE` | Tope global por archivo en bytes (el plan puede ser menor). |
| `FILERED_URL_BASE` | Prefijo de la URL pública devuelta (`/` en local; en servidor la URL completa, por ejemplo `https://dominio/filered/`). |
| `FILERED_ADMIN_*` | Usuario administrador que se crea al iniciar si no existe. |
| `FILERED_*_PERMISSIONS_RESOURCE` | JSON de categorías, opciones y acciones de cada área. |
| `JWT_PUBLIC_PATHS` | Rutas sin token (páginas, `/f/**`, login, registro, planes públicos y `/api/fileRed/**`, que usa API key). |

## API FileRed

Cada cliente crea sus API keys en `/pages/customer/` y elige qué permisos tiene cada una. La key se envía en el encabezado `X-Api-Key`; las respuestas usan `ApiResponse` (`validation`, `status`, `data`, `mensage`). Requiere plan vigente y respeta el espacio y el tamaño máximo del plan.

| Método | Ruta | Permiso | Cuerpo |
|---|---|---|---|
| GET | `/api/fileRed/cuenta/getById` | `consultar_cuenta` | — |
| GET | `/api/fileRed/almacenes/getAll` | `obtener_almacenes` | — |
| POST | `/api/fileRed/almacenes/add` | `agregar_almacen` | `{ almacen }` |
| PUT | `/api/fileRed/almacenes/update` | `actualizar_almacen` | `{ almacen, nombre }` |
| DELETE | `/api/fileRed/almacenes/delete` | `eliminar_almacen` | `{ almacen }` |
| POST | `/api/fileRed/carpetas/getAll` | `obtener_carpetas` | `{ almacen }` |
| POST | `/api/fileRed/carpetas/add` | `agregar_carpeta` | `{ almacen, categoria }` |
| PUT | `/api/fileRed/carpetas/update` | `actualizar_carpeta` | `{ almacen, categoria, nombre }` |
| DELETE | `/api/fileRed/carpetas/delete` | `eliminar_carpeta` | `{ almacen, categoria }` |
| POST | `/api/fileRed/archivos/getAllPages` | `obtener_archivos` | `{ almacen, categoria, buscar, page, size }` |
| POST | `/api/fileRed/archivos/getById` | `obtener_archivo` | `{ almacen, categoria, identificador }` |
| POST | `/api/fileRed/archivos/add` | `agregar_archivo` | multipart: `almacen`, `categoria`, `file`, `base64` opcional |
| PUT | `/api/fileRed/archivos/update` | `actualizar_archivo` | multipart: `almacen`, `categoria`, `identificador`, `file` |
| PUT | `/api/fileRed/archivos/updateNombre` | `renombrar_archivo` | `{ almacen, categoria, identificador, nombre }` |
| DELETE | `/api/fileRed/archivos/delete` | `eliminar_archivo` | `{ almacen, categoria, identificador }` |

```json
{ "validation": "Success", "status": 201, "data": { "identificador": "…", "extension": "png", "size": 1024, "url": "/f/{clienteUuid}/Almacen/Categoria/….png" }, "mensage": "Archivo subido correctamente" }
```

Errores: `401` sin key o key inválida, `403` sin el permiso, `404` recurso inexistente, `400` validación, `413` petición demasiado grande.

Los archivos se sirven en `GET /f/{clienteUuid}/{almacen}/{categoria}/{identificador}.{extension}` (solo clientes activos).

## Despliegue

1. MariaDB con una base vacía (`utf8mb4`) y la carpeta de `FILERED_STORAGE_PATH` con permisos de escritura.
2. `.env` en `/opt/filered/.env` (la app lo busca en `./opt/filered/.env` relativo a la carpeta desde donde arranca Tomcat).
3. `.\mvnw.cmd clean package` y copiar `target/filered-<versión>.war` como `filered.war` a `webapps` de **Tomcat 11** (Java 17+).
4. Entrar con el usuario administrador del `.env` y cambiar su contraseña.

<hr>

<div align="center">
  <p><strong>👑 CONTRIBUCIÓN</strong></p>
  ¡Agradecemos cualquier contribución para mejorar!<br>
  Haz un fork, realiza tus cambios y envía un pull request.
</div>

<hr>

<div align="center">
  <p><strong>📝 LICENCIA</strong></p>
  Este proyecto está bajo la licencia AaronDevs.
</div>

let ui;
let raiz;
let almacen = "";
let categoria = "";
let almacenes = [];
let carpetas = [];
let archivos = [];
let respuestaArchivos = null;
let pagina = 1;
let tamanoPagina = 25;
let buscarArchivo = "";
let filtroLocal = "";
let vista = "lista";
let seleccion = new Set();
let temporizador = 0;

const VERSION = "20261120-5";
const NOMBRE_VALIDO = /^[\p{L}\p{N}][\p{L}\p{N} ._-]{0,127}$/u;
const IMAGENES = new Set(["jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "ico"]);
const VIDEOS = new Set(["mp4", "webm", "m4v", "mov"]);
const AUDIOS = new Set(["mp3", "wav", "ogg", "aac", "flac", "m4a"]);

export function loadStyles() {
    if (document.querySelector("link[data-estilo-archivos]")) return;
    const enlace = document.createElement("link");
    enlace.rel = "stylesheet";
    enlace.dataset.estiloArchivos = "true";
    enlace.href = new URL(`./style.css?v=${VERSION}`, import.meta.url).href;
    document.head.appendChild(enlace);
}

export function render() {
    return `
        <section class="fs-session" data-sesion></section>
        <div class="fr-toolbar fs-toolbar" data-toolbar></div>
        <div class="fs-selection" data-seleccion hidden></div>
        <section class="fr-panel fs-panel" data-zona>
            <div class="fs-drop" data-drop hidden>${FileRedPanel.icono("upload")}<p>Suelta tus archivos para subirlos a <strong data-drop-destino data-i18n-skip></strong></p></div>
            <div data-contenido></div>
            <div data-paginador></div>
        </section>
        <input type="file" multiple hidden data-input-archivos>
        <ul class="upload-queue" data-cola hidden></ul>`;
}

function url(relativa) {
    if (/^https?:\/\//i.test(relativa || "")) return relativa;
    return `${window.FileRedConfig?.apiBaseUrl || ""}${relativa}`;
}

function nombreArchivo(a) {
    return a.nombreOriginal || `${a.identificador}.${a.extension}`;
}

function nivel() {
    return categoria ? "archivos" : almacen ? "carpetas" : "almacenes";
}

function nombreValido(valor) {
    return NOMBRE_VALIDO.test(valor) && !valor.includes("..");
}

function vacio(icono, mensaje, accion = "") {
    return `<div class="fr-empty">${ui.icono(icono)}<p>${ui.escapar(mensaje)}</p>${accion}</div>`;
}

function pintarSesion() {
    const n = nivel();
    const migas = [`<button type="button" data-ir="raiz">${ui.icono("hard")}<span>Almacenes</span></button>`];
    if (almacen) migas.push(`<button type="button" data-ir="almacen" data-i18n-skip>${ui.icono("folder-open")}<span>${ui.escapar(almacen)}</span></button>`);
    if (categoria) migas.push(`<span class="fs-crumb-actual" data-i18n-skip>${ui.icono("folder")}<span>${ui.escapar(categoria)}</span></span>`);
    const titulos = {
        almacenes: ["Almacenes", "Cada almacén agrupa las carpetas de un proyecto o aplicación."],
        carpetas: [almacen, "Carpetas del almacén. Entra a una carpeta para gestionar sus archivos."],
        archivos: [categoria, `Archivos de ${almacen} / ${categoria}`]
    };
    const [titulo, descripcion] = titulos[n];
    raiz.querySelector("[data-sesion]").innerHTML = `
        <nav class="fs-crumbs" aria-label="Ubicación">${migas.join(ui.icono("chevron-right", "fs-sep"))}</nav>
        <div class="fs-session-head">
            ${n !== "almacenes" ? `<button class="fr-icon-btn" type="button" data-atras title="Volver" aria-label="Volver">${ui.icono("chevron-right", "fs-back")}</button>` : ""}
            <span class="fs-session-icon">${ui.icono(n === "almacenes" ? "hard" : n === "carpetas" ? "folder-open" : "folder")}</span>
            <div>
                <h2 ${n === "almacenes" ? "" : "data-i18n-skip"}>${ui.escapar(titulo)}</h2>
                <p>${ui.escapar(descripcion)}</p>
            </div>
            <div class="fs-session-stats" data-estadisticas></div>
        </div>`;
}

function pintarToolbar() {
    const n = nivel();
    let html = "";
    if (n === "almacenes") {
        html = `
            <label class="fr-search">${ui.icono("search")}<input class="fr-input" type="search" placeholder="Filtrar almacenes" value="${ui.escapar(filtroLocal)}" data-filtro-local aria-label="Filtrar almacenes"></label>
            <span class="fr-toolbar-spacer"></span>
            <button class="fr-btn" type="button" data-recargar>${ui.icono("refresh")}<span>Actualizar</span></button>
            ${ui.puede("archivos", "gestionar_almacenes") ? `<button class="fr-btn fr-btn-primary" type="button" data-nuevo-almacen>${ui.icono("plus")}<span>Nuevo almacén</span></button>` : ""}`;
    } else if (n === "carpetas") {
        html = `
            <label class="fr-search">${ui.icono("search")}<input class="fr-input" type="search" placeholder="Filtrar carpetas" value="${ui.escapar(filtroLocal)}" data-filtro-local aria-label="Filtrar carpetas"></label>
            <span class="fr-toolbar-spacer"></span>
            <button class="fr-btn" type="button" data-recargar>${ui.icono("refresh")}<span>Actualizar</span></button>
            ${ui.puede("archivos", "gestionar_carpetas") ? `<button class="fr-btn fr-btn-primary" type="button" data-nueva-carpeta>${ui.icono("plus")}<span>Nueva carpeta</span></button>` : ""}`;
    } else {
        html = `
            <label class="fr-search">${ui.icono("search")}<input class="fr-input" type="search" placeholder="Buscar por nombre o extensión" value="${ui.escapar(buscarArchivo)}" data-buscar-archivo aria-label="Buscar archivos"></label>
            <select class="fr-select fs-size" data-tamano aria-label="Archivos por página">
                ${[25, 50, 100].map(t => `<option value="${t}" ${t === tamanoPagina ? "selected" : ""}>${t} por página</option>`).join("")}
            </select>
            <div class="fs-view-toggle" role="group" aria-label="Vista">
                <button type="button" data-vista="lista" class="${vista === "lista" ? "is-active" : ""}" title="Lista" aria-label="Lista">${ui.icono("menu")}</button>
                <button type="button" data-vista="cuadricula" class="${vista === "cuadricula" ? "is-active" : ""}" title="Cuadrícula" aria-label="Cuadrícula">${ui.icono("layout-dashboard")}</button>
            </div>
            <span class="fr-toolbar-spacer"></span>
            <button class="fr-btn" type="button" data-recargar>${ui.icono("refresh")}<span>Actualizar</span></button>
            ${ui.puede("archivos", "subir") ? `<button class="fr-btn fr-btn-primary" type="button" data-subir>${ui.icono("upload")}<span>Subir archivos</span></button>` : ""}`;
    }
    raiz.querySelector("[data-toolbar]").innerHTML = html;
}

function estadisticas(items) {
    const caja = raiz.querySelector("[data-estadisticas]");
    if (!caja) return;
    caja.innerHTML = items.map(([valor, etiqueta]) => `<div><strong>${valor}</strong><span>${ui.escapar(etiqueta)}</span></div>`).join("");
}

function tarjeta({ tipo, nombre, detalle, fecha, acciones }) {
    return `
        <article class="fs-card" data-abrir="${ui.escapar(nombre)}" data-tipo="${tipo}" tabindex="0" role="button" aria-label="${ui.escapar(nombre)}">
            <span class="fs-card-icon is-${tipo}">${ui.icono(tipo === "almacen" ? "hard" : "folder")}</span>
            <div class="fs-card-body">
                <strong data-i18n-skip>${ui.escapar(nombre)}</strong>
                <small>${detalle}</small>
                ${fecha ? `<small class="fs-card-date">Modificado ${ui.formato.fechaHora(fecha)}</small>` : ""}
            </div>
            ${acciones ? `<div class="fs-card-actions">${acciones}</div>` : ""}
        </article>`;
}

function accionesTarjeta(permiso) {
    if (!ui.puede("archivos", permiso)) return "";
    return ui.botonAccion("renombrar", "pencil", "Renombrar") + ui.botonAccion("eliminar", "trash", "Eliminar", "is-danger");
}

function pintarAlmacenes() {
    const texto = filtroLocal.toLowerCase();
    const filas = almacenes.filter(a => a.almacen.toLowerCase().includes(texto));
    const totalBytes = almacenes.reduce((t, a) => t + Number(a.bytes || 0), 0);
    const totalArchivos = almacenes.reduce((t, a) => t + Number(a.archivos || 0), 0);
    estadisticas([
        [ui.formato.numero(almacenes.length), "almacenes"],
        [ui.formato.numero(totalArchivos), "archivos"],
        [ui.formato.bytes(totalBytes), "usados"]
    ]);
    const crear = ui.puede("archivos", "gestionar_almacenes")
        ? `<button class="fr-btn fr-btn-primary" type="button" data-nuevo-almacen>${ui.icono("plus")}<span>Crear mi primer almacén</span></button>`
        : "";
    raiz.querySelector("[data-contenido]").innerHTML = filas.length
        ? `<div class="fs-grid">${filas
              .map(a =>
                  tarjeta({
                      tipo: "almacen",
                      nombre: a.almacen,
                      detalle: `${ui.formato.numero(a.carpetas)} carpetas · ${ui.formato.numero(a.archivos)} archivos · ${ui.formato.bytes(a.bytes)}`,
                      fecha: a.ultimaModificacion,
                      acciones: accionesTarjeta("gestionar_almacenes")
                  })
              )
              .join("")}</div>`
        : almacenes.length
          ? vacio("search", "Ningún almacén coincide con el filtro.")
          : vacio("hard", "Aún no tienes almacenes. Crea uno para tu proyecto o aplicación y organiza dentro tus carpetas.", crear);
    raiz.querySelector("[data-paginador]").innerHTML = "";
}

function pintarCarpetas() {
    const texto = filtroLocal.toLowerCase();
    const filas = carpetas.filter(c => c.categoria.toLowerCase().includes(texto));
    estadisticas([
        [ui.formato.numero(carpetas.length), "carpetas"],
        [ui.formato.numero(carpetas.reduce((t, c) => t + Number(c.archivos || 0), 0)), "archivos"],
        [ui.formato.bytes(carpetas.reduce((t, c) => t + Number(c.bytes || 0), 0)), "usados"]
    ]);
    const crear = ui.puede("archivos", "gestionar_carpetas")
        ? `<button class="fr-btn fr-btn-primary" type="button" data-nueva-carpeta>${ui.icono("plus")}<span>Crear carpeta</span></button>`
        : "";
    raiz.querySelector("[data-contenido]").innerHTML = filas.length
        ? `<div class="fs-grid">${filas
              .map(c =>
                  tarjeta({
                      tipo: "carpeta",
                      nombre: c.categoria,
                      detalle: `${ui.formato.numero(c.archivos)} archivos · ${ui.formato.bytes(c.bytes)}`,
                      fecha: c.ultimaModificacion,
                      acciones: accionesTarjeta("gestionar_carpetas")
                  })
              )
              .join("")}</div>`
        : carpetas.length
          ? vacio("search", "Ninguna carpeta coincide con el filtro.")
          : vacio("folder", "Este almacén no tiene carpetas todavía.", crear);
    raiz.querySelector("[data-paginador]").innerHTML = "";
}

function miniatura(a, clase = "") {
    if (IMAGENES.has(a.extension)) return `<img class="fs-thumb ${clase}" src="${ui.escapar(url(a.url))}" alt="" loading="lazy">`;
    return `<span class="fs-thumb fs-ext ${clase}">${ui.escapar(a.extension)}</span>`;
}

function accionesArchivo() {
    return [
        ui.botonAccion("ver", "eye", "Ver detalle"),
        ui.botonAccion("copiar", "link", "Copiar URL pública"),
        ui.botonAccion("descargar", "download", "Descargar"),
        ui.puede("archivos", "subir") ? ui.botonAccion("renombrar", "pencil", "Renombrar") : "",
        ui.puede("archivos", "subir") ? ui.botonAccion("reemplazar", "refresh", "Reemplazar contenido") : "",
        ui.puede("archivos", "eliminar") ? ui.botonAccion("eliminar", "trash", "Eliminar", "is-danger") : ""
    ].join("");
}

function pintarArchivos() {
    const total = respuestaArchivos?.totalRecords || 0;
    const actual = carpetas.find(c => c.categoria === categoria);
    estadisticas([
        [ui.formato.numero(total), "archivos"],
        [ui.formato.bytes(actual?.bytes || archivos.reduce((t, a) => t + Number(a.size || 0), 0)), "usados"]
    ]);
    const puedeSubir = ui.puede("archivos", "subir");
    const contenido = raiz.querySelector("[data-contenido]");
    if (!archivos.length) {
        contenido.innerHTML = buscarArchivo
            ? vacio("search", "Ningún archivo coincide con la búsqueda.")
            : puedeSubir
              ? `<button type="button" class="fs-dropzone" data-subir>${ui.icono("upload")}<strong>Arrastra tus archivos aquí</strong><span>o haz clic para seleccionarlos desde tu equipo</span></button>`
              : vacio("file", "Esta carpeta está vacía.");
    } else if (vista === "cuadricula") {
        contenido.innerHTML = `<div class="fs-files-grid">${archivos
            .map(
                a => `
                <article class="fs-file-card ${seleccion.has(a.identificador) ? "is-selected" : ""}" data-row="${ui.escapar(a.identificador)}">
                    <label class="fs-check"><input type="checkbox" data-seleccionar ${seleccion.has(a.identificador) ? "checked" : ""} aria-label="Seleccionar"></label>
                    <button type="button" class="fs-file-preview" data-action="ver">${miniatura(a, "is-large")}</button>
                    <div class="fs-file-meta"><strong data-i18n-skip title="${ui.escapar(nombreArchivo(a))}">${ui.escapar(nombreArchivo(a))}</strong><small>${ui.formato.bytes(a.size)} · ${ui.formato.fecha(a.fechaCreacion)}</small></div>
                    <div class="fs-file-actions">${accionesArchivo()}</div>
                </article>`
            )
            .join("")}</div>`;
    } else {
        const todos = archivos.length > 0 && archivos.every(a => seleccion.has(a.identificador));
        contenido.innerHTML = `
            <div class="fr-table-wrap">
                <table class="fr-table fs-table" data-i18n>
                    <thead><tr>
                        <th class="fs-col-check"><label class="fs-check"><input type="checkbox" data-seleccionar-todo ${todos ? "checked" : ""} aria-label="Seleccionar todos"></label></th>
                        <th>Archivo</th><th>Tamaño</th><th>Tipo</th><th>Subido</th><th class="is-actions">Acciones</th>
                    </tr></thead>
                    <tbody>${archivos
                        .map(
                            a => `
                        <tr data-row="${ui.escapar(a.identificador)}" class="${seleccion.has(a.identificador) ? "is-selected" : ""}">
                            <td class="fs-col-check"><label class="fs-check"><input type="checkbox" data-seleccionar ${seleccion.has(a.identificador) ? "checked" : ""} aria-label="Seleccionar"></label></td>
                            <td data-label="Archivo"><button type="button" class="fs-file" data-action="ver">${miniatura(a)}<span><span class="fr-cell-main" data-i18n-skip>${ui.escapar(nombreArchivo(a))}</span><span class="fr-cell-sub fr-mono" data-i18n-skip>${ui.escapar(a.identificador)}.${ui.escapar(a.extension)}</span></span></button></td>
                            <td data-label="Tamaño">${ui.formato.bytes(a.size)}</td>
                            <td data-label="Tipo"><span class="fr-badge is-plain" data-i18n-skip>${ui.escapar(a.extension.toUpperCase())}</span></td>
                            <td data-label="Subido">${ui.formato.fechaHora(a.fechaCreacion)}</td>
                            <td class="is-actions">${accionesArchivo()}</td>
                        </tr>`
                        )
                        .join("")}</tbody>
                </table>
            </div>`;
    }
    raiz.querySelector("[data-paginador]").innerHTML = ui.paginador(respuestaArchivos);
    pintarSeleccion();
}

function pintarSeleccion() {
    const barra = raiz.querySelector("[data-seleccion]");
    if (nivel() !== "archivos" || !seleccion.size) {
        barra.hidden = true;
        return;
    }
    barra.hidden = false;
    barra.innerHTML = `
        <strong>${ui.formato.numero(seleccion.size)} seleccionado(s)</strong>
        <button class="fr-btn fr-btn-sm" type="button" data-copiar-seleccion>${ui.icono("link")}<span>Copiar URLs</span></button>
        ${ui.puede("archivos", "eliminar") ? `<button class="fr-btn fr-btn-sm fr-btn-danger" type="button" data-eliminar-seleccion>${ui.icono("trash")}<span>Eliminar</span></button>` : ""}
        <button class="fr-btn fr-btn-sm fr-btn-ghost" type="button" data-limpiar-seleccion>Quitar selección</button>`;
}

async function cargar() {
    pintarSesion();
    pintarToolbar();
    try {
        if (nivel() === "almacenes") {
            almacenes = (await AlmacenService.listar()) || [];
            pintarAlmacenes();
        } else if (nivel() === "carpetas") {
            carpetas = (await CarpetaService.listar(almacen)) || [];
            pintarCarpetas();
        } else {
            const [respuesta, listaCarpetas] = await Promise.all([
                ArchivoService.paginar({ almacen, categoria, page: pagina, size: tamanoPagina, buscar: buscarArchivo || null }),
                CarpetaService.listar(almacen, null)
            ]);
            respuestaArchivos = respuesta;
            archivos = respuesta?.records || [];
            carpetas = listaCarpetas || [];
            const visibles = new Set(archivos.map(a => a.identificador));
            seleccion = new Set([...seleccion].filter(id => visibles.has(id)));
            pintarArchivos();
        }
    } catch (error) {
        raiz.querySelector("[data-contenido]").innerHTML = vacio("ban", error.message);
        raiz.querySelector("[data-paginador]").innerHTML = "";
        ui.notificarError(error);
    }
}

function ir(nuevoAlmacen, nuevaCategoria) {
    almacen = nuevoAlmacen;
    categoria = nuevaCategoria;
    pagina = 1;
    buscarArchivo = "";
    filtroLocal = "";
    seleccion = new Set();
    cargar();
}

function formularioNombre({ titulo, subtitulo, etiqueta, valor = "", textoGuardar, ayuda, alGuardar }) {
    ui.formulario({
        titulo,
        subtitulo,
        ancho: "sm",
        textoGuardar,
        campos: ui.campo({ nombre: "nombre", etiqueta, valor, requerido: true, max: 128, clase: "is-full", ayuda }),
        alAbrir(form) {
            const campo = form.elements.nombre;
            window.setTimeout(() => campo.select(), 80);
        },
        async alGuardar(form) {
            ui.exigir(form, [["nombre", "El nombre no es válido: usa letras, números, espacio, punto, guion o guion bajo.", v => nombreValido(v)]]);
            await alGuardar(form.elements.nombre.value.trim());
        }
    });
}

const AYUDA_NOMBRE = "Letras, números, espacio, punto, guion y guion bajo.";

function nuevoAlmacen() {
    formularioNombre({
        titulo: "Nuevo almacén",
        subtitulo: "Por ejemplo, el nombre de tu aplicación o proyecto.",
        etiqueta: "Nombre del almacén",
        textoGuardar: "Crear almacén",
        ayuda: AYUDA_NOMBRE,
        async alGuardar(nombre) {
            await AlmacenService.agregar(nombre);
            AppNotifications.success("Almacén creado correctamente");
            ir(nombre, "");
        }
    });
}

function nuevaCarpeta() {
    formularioNombre({
        titulo: "Nueva carpeta",
        subtitulo: `En el almacén ${almacen}`,
        etiqueta: "Nombre de la carpeta",
        textoGuardar: "Crear carpeta",
        ayuda: AYUDA_NOMBRE,
        async alGuardar(nombre) {
            await CarpetaService.agregar(almacen, nombre);
            AppNotifications.success("Carpeta creada correctamente");
            await cargar();
        }
    });
}

function renombrarAlmacen(nombre) {
    formularioNombre({
        titulo: "Renombrar almacén",
        subtitulo: "Las URLs públicas de sus archivos cambiarán.",
        etiqueta: "Nuevo nombre",
        valor: nombre,
        textoGuardar: "Renombrar",
        ayuda: AYUDA_NOMBRE,
        async alGuardar(nuevo) {
            await AlmacenService.renombrar(nombre, nuevo);
            AppNotifications.success("Almacén renombrado correctamente");
            await cargar();
        }
    });
}

function renombrarCarpeta(nombre) {
    formularioNombre({
        titulo: "Renombrar carpeta",
        subtitulo: "Las URLs públicas de sus archivos cambiarán.",
        etiqueta: "Nuevo nombre",
        valor: nombre,
        textoGuardar: "Renombrar",
        ayuda: AYUDA_NOMBRE,
        async alGuardar(nuevo) {
            await CarpetaService.renombrar(almacen, nombre, nuevo);
            AppNotifications.success("Carpeta renombrada correctamente");
            await cargar();
        }
    });
}

async function eliminarAlmacen(nombre) {
    const ok = await ui.confirmar({
        titulo: "Eliminar almacén",
        mensaje: `Se eliminará el almacén ${nombre} con todas sus carpetas y archivos. Las URLs públicas dejarán de funcionar. Esta acción no se puede deshacer.`,
        textoConfirmar: "Eliminar almacén",
        peligro: true
    });
    if (!ok) return;
    try {
        await AlmacenService.eliminar(nombre);
        AppNotifications.success("Almacén eliminado correctamente");
        await cargar();
    } catch (error) {
        ui.notificarError(error);
    }
}

async function eliminarCarpeta(nombre) {
    const ok = await ui.confirmar({
        titulo: "Eliminar carpeta",
        mensaje: `Se eliminará la carpeta ${nombre} con todos sus archivos. Las URLs públicas dejarán de funcionar.`,
        textoConfirmar: "Eliminar carpeta",
        peligro: true
    });
    if (!ok) return;
    try {
        await CarpetaService.eliminar(almacen, nombre);
        AppNotifications.success("Carpeta eliminada correctamente");
        await cargar();
    } catch (error) {
        ui.notificarError(error);
    }
}

async function subirArchivos(lista) {
    if (!lista.length || nivel() !== "archivos") return;
    const cola = raiz.querySelector("[data-cola]");
    cola.hidden = false;
    const destinoAlmacen = almacen;
    const destinoCategoria = categoria;
    let exitosos = 0;
    for (const archivo of lista) {
        const item = document.createElement("li");
        item.innerHTML = `
            <span class="upload-name" data-i18n-skip>${ui.escapar(archivo.name)}</span><span class="upload-size">${ui.formato.bytes(archivo.size)}</span>
            <span class="upload-bar"><span></span></span>
            <span class="upload-state is-pending">Subiendo…</span>`;
        cola.prepend(item);
        const barra = item.querySelector(".upload-bar span");
        const estado = item.querySelector(".upload-state");
        try {
            await ArchivoService.subirConProgreso(destinoAlmacen, destinoCategoria, archivo, porcentaje => {
                barra.style.width = `${porcentaje}%`;
            });
            barra.style.width = "100%";
            estado.textContent = "Listo";
            estado.className = "upload-state is-done";
            exitosos++;
        } catch (error) {
            estado.textContent = error.message;
            estado.className = "upload-state is-error";
            item.classList.add("is-error");
        }
    }
    if (exitosos) AppNotifications.success(`${exitosos} archivo(s) subido(s) correctamente`);
    if (almacen === destinoAlmacen && categoria === destinoCategoria) {
        pagina = 1;
        await cargar();
    }
    window.setTimeout(() => {
        cola.querySelectorAll(".is-done").forEach(e => e.closest("li").remove());
        if (!cola.children.length) cola.hidden = true;
    }, 6000);
}

function renombrarArchivo(archivo) {
    ui.formulario({
        titulo: "Renombrar archivo",
        subtitulo: "Cambia el nombre visible. La URL pública no cambia.",
        ancho: "sm",
        textoGuardar: "Renombrar",
        campos: ui.campo({ nombre: "nombre", etiqueta: "Nombre", valor: nombreArchivo(archivo), requerido: true, max: 200, clase: "is-full" }),
        alAbrir(form) {
            const campo = form.elements.nombre;
            window.setTimeout(() => {
                const punto = campo.value.lastIndexOf(".");
                campo.setSelectionRange(0, punto > 0 ? punto : campo.value.length);
                campo.focus();
            }, 80);
        },
        async alGuardar(form) {
            ui.exigir(form, [
                ["nombre", "El nombre es obligatorio."],
                ["nombre", "El nombre no puede contener barras.", v => !/[\\/]/.test(v)]
            ]);
            await ArchivoService.renombrar(almacen, categoria, archivo.identificador, form.elements.nombre.value.trim());
            AppNotifications.success("Archivo renombrado correctamente");
            await cargar();
        }
    });
}

function reemplazarArchivo(archivo) {
    const selector = document.createElement("input");
    selector.type = "file";
    selector.addEventListener("change", async () => {
        const nuevo = selector.files?.[0];
        if (!nuevo) return;
        const ok = await ui.confirmar({
            titulo: "Reemplazar contenido",
            mensaje: `El contenido de ${nombreArchivo(archivo)} se reemplazará por ${nuevo.name}. Si cambia la extensión, cambiará también su URL.`,
            textoConfirmar: "Reemplazar"
        });
        if (!ok) return;
        try {
            await ArchivoService.reemplazar(almacen, categoria, archivo.identificador, archivo.extension, nuevo);
            AppNotifications.success("Archivo reemplazado correctamente");
            await cargar();
        } catch (error) {
            ui.notificarError(error);
        }
    });
    selector.click();
}

async function eliminarArchivos(lista) {
    if (!lista.length) return;
    const ok = await ui.confirmar({
        titulo: lista.length === 1 ? "Eliminar archivo" : "Eliminar archivos",
        mensaje:
            lista.length === 1
                ? `Se eliminará ${nombreArchivo(lista[0])}. Su URL pública dejará de funcionar.`
                : `Se eliminarán ${lista.length} archivos. Sus URLs públicas dejarán de funcionar.`,
        textoConfirmar: "Eliminar",
        peligro: true
    });
    if (!ok) return;
    let eliminados = 0;
    for (const archivo of lista) {
        try {
            await ArchivoService.eliminar({ almacen, categoria, identificador: archivo.identificador, extension: archivo.extension });
            seleccion.delete(archivo.identificador);
            eliminados++;
        } catch (error) {
            ui.notificarError(error);
        }
    }
    if (eliminados) AppNotifications.success(eliminados === 1 ? "Archivo eliminado correctamente" : `${eliminados} archivos eliminados correctamente`);
    if (eliminados === archivos.length && pagina > 1) pagina--;
    await cargar();
}

function descargar(archivo) {
    const enlace = document.createElement("a");
    enlace.href = url(archivo.url);
    enlace.download = nombreArchivo(archivo);
    enlace.target = "_blank";
    enlace.rel = "noopener";
    document.body.appendChild(enlace);
    enlace.click();
    enlace.remove();
}

function vistaPrevia(a) {
    const enlace = ui.escapar(url(a.url));
    if (IMAGENES.has(a.extension)) return `<img src="${enlace}" alt="">`;
    if (VIDEOS.has(a.extension)) return `<video src="${enlace}" controls preload="metadata"></video>`;
    if (AUDIOS.has(a.extension)) return `<audio src="${enlace}" controls preload="metadata"></audio>`;
    if (a.extension === "pdf") return `<iframe src="${enlace}" title="Vista previa"></iframe>`;
    return `<div class="fs-preview-none">${miniatura(a, "is-large")}<p>Sin vista previa para este tipo de archivo.</p></div>`;
}

function verArchivo(archivo) {
    const puedeSubir = ui.puede("archivos", "subir");
    const puedeEliminar = ui.puede("archivos", "eliminar");
    const { raiz: modal, cerrar } = ui.modal({
        titulo: nombreArchivo(archivo),
        subtitulo: `${almacen} / ${categoria}`,
        ancho: "lg",
        cuerpo: `
            <div class="fs-detail">
                <div class="fs-preview">${vistaPrevia(archivo)}</div>
                <dl class="fr-kv fs-detail-kv">
                    <div><dt>Identificador</dt><dd class="fr-mono" data-i18n-skip>${ui.escapar(archivo.identificador)}</dd></div>
                    <div><dt>Tipo</dt><dd data-i18n-skip>${ui.escapar(archivo.extension.toUpperCase())} · ${ui.escapar(archivo.mime || "")}</dd></div>
                    <div><dt>Tamaño</dt><dd>${ui.formato.bytes(archivo.size)}</dd></div>
                    <div><dt>Subido</dt><dd>${ui.formato.fechaHora(archivo.fechaCreacion)}</dd></div>
                    <div class="fs-kv-full"><dt>URL pública</dt><dd><div class="fr-secret" data-i18n-skip><code>${ui.escapar(url(archivo.url))}</code><button class="fr-btn fr-btn-sm" type="button" data-detalle="copiar">${ui.icono("copy")}<span>Copiar</span></button></div></dd></div>
                </dl>
            </div>`,
        pie: `
            ${puedeEliminar ? `<button class="fr-btn fr-btn-danger" type="button" data-detalle="eliminar">${ui.icono("trash")}<span>Eliminar</span></button>` : ""}
            <span class="fr-toolbar-spacer"></span>
            ${puedeSubir ? `<button class="fr-btn" type="button" data-detalle="renombrar">${ui.icono("pencil")}<span>Renombrar</span></button>` : ""}
            ${puedeSubir ? `<button class="fr-btn" type="button" data-detalle="reemplazar">${ui.icono("refresh")}<span>Reemplazar</span></button>` : ""}
            <button class="fr-btn fr-btn-primary" type="button" data-detalle="descargar">${ui.icono("download")}<span>Descargar</span></button>`
    });
    modal.addEventListener("click", event => {
        const boton = event.target.closest("[data-detalle]");
        if (!boton) return;
        const accion = boton.dataset.detalle;
        if (accion === "copiar") return ui.copiar(url(archivo.url));
        if (accion === "descargar") return descargar(archivo);
        cerrar();
        if (accion === "renombrar") renombrarArchivo(archivo);
        if (accion === "reemplazar") reemplazarArchivo(archivo);
        if (accion === "eliminar") eliminarArchivos([archivo]);
    });
}

function enlazarEventos() {
    const input = raiz.querySelector("[data-input-archivos]");
    input.addEventListener("change", () => {
        subirArchivos([...input.files]);
        input.value = "";
    });

    raiz.querySelector("[data-sesion]").addEventListener("click", event => {
        if (event.target.closest("[data-atras]")) return categoria ? ir(almacen, "") : ir("", "");
        const miga = event.target.closest("[data-ir]");
        if (!miga) return;
        if (miga.dataset.ir === "raiz") ir("", "");
        else ir(almacen, "");
    });

    const toolbar = raiz.querySelector("[data-toolbar]");
    toolbar.addEventListener("click", event => {
        if (event.target.closest("[data-recargar]")) return cargar();
        if (event.target.closest("[data-nuevo-almacen]")) return nuevoAlmacen();
        if (event.target.closest("[data-nueva-carpeta]")) return nuevaCarpeta();
        if (event.target.closest("[data-subir]")) return input.click();
        const botonVista = event.target.closest("[data-vista]");
        if (botonVista) {
            vista = botonVista.dataset.vista;
            pintarToolbar();
            pintarArchivos();
        }
    });
    toolbar.addEventListener("input", event => {
        if (event.target.matches("[data-filtro-local]")) {
            filtroLocal = event.target.value.trim();
            if (nivel() === "almacenes") pintarAlmacenes();
            else pintarCarpetas();
        }
        if (event.target.matches("[data-buscar-archivo]")) {
            window.clearTimeout(temporizador);
            temporizador = window.setTimeout(() => {
                buscarArchivo = event.target.value.trim();
                pagina = 1;
                cargarArchivosConservandoFoco();
            }, 350);
        }
    });
    toolbar.addEventListener("change", event => {
        if (!event.target.matches("[data-tamano]")) return;
        tamanoPagina = Number(event.target.value);
        pagina = 1;
        cargar();
    });

    raiz.querySelector("[data-seleccion]").addEventListener("click", event => {
        if (event.target.closest("[data-limpiar-seleccion]")) {
            seleccion = new Set();
            return pintarArchivos();
        }
        const elegidos = archivos.filter(a => seleccion.has(a.identificador));
        if (event.target.closest("[data-copiar-seleccion]")) return ui.copiar(elegidos.map(a => url(a.url)).join("\n"));
        if (event.target.closest("[data-eliminar-seleccion]")) eliminarArchivos(elegidos);
    });

    const contenido = raiz.querySelector("[data-contenido]");
    contenido.addEventListener("change", event => {
        if (event.target.matches("[data-seleccionar-todo]")) {
            seleccion = event.target.checked ? new Set(archivos.map(a => a.identificador)) : new Set();
            return pintarArchivos();
        }
        if (event.target.matches("[data-seleccionar]")) {
            const id = event.target.closest("[data-row]")?.dataset.row;
            if (!id) return;
            if (event.target.checked) seleccion.add(id);
            else seleccion.delete(id);
            pintarArchivos();
        }
    });
    contenido.addEventListener("click", event => {
        if (event.target.closest(".fs-check")) return;
        if (event.target.closest("[data-subir]")) return input.click();
        if (event.target.closest("[data-nuevo-almacen]")) return nuevoAlmacen();
        if (event.target.closest("[data-nueva-carpeta]")) return nuevaCarpeta();
        const tarjetaCarpeta = event.target.closest("[data-abrir]");
        const boton = event.target.closest("[data-action]");
        if (tarjetaCarpeta) {
            const nombre = tarjetaCarpeta.dataset.abrir;
            const esAlmacen = tarjetaCarpeta.dataset.tipo === "almacen";
            if (boton?.dataset.action === "renombrar") return esAlmacen ? renombrarAlmacen(nombre) : renombrarCarpeta(nombre);
            if (boton?.dataset.action === "eliminar") return esAlmacen ? eliminarAlmacen(nombre) : eliminarCarpeta(nombre);
            return esAlmacen ? ir(nombre, "") : ir(almacen, nombre);
        }
        if (!boton) return;
        const archivo = archivos.find(a => a.identificador === boton.closest("[data-row]")?.dataset.row);
        if (!archivo) return;
        const acciones = {
            ver: () => verArchivo(archivo),
            copiar: () => ui.copiar(url(archivo.url)),
            descargar: () => descargar(archivo),
            renombrar: () => renombrarArchivo(archivo),
            reemplazar: () => reemplazarArchivo(archivo),
            eliminar: () => eliminarArchivos([archivo])
        };
        acciones[boton.dataset.action]?.();
    });
    contenido.addEventListener("keydown", event => {
        const tarjetaCarpeta = event.target.closest("[data-abrir]");
        if (!tarjetaCarpeta || event.target !== tarjetaCarpeta || (event.key !== "Enter" && event.key !== " ")) return;
        event.preventDefault();
        tarjetaCarpeta.click();
    });

    raiz.querySelector("[data-paginador]").addEventListener("click", event => {
        const boton = event.target.closest("[data-page]");
        if (!boton || boton.disabled) return;
        pagina = Number(boton.dataset.page);
        seleccion = new Set();
        cargar();
    });

    const zona = raiz.querySelector("[data-zona]");
    const drop = raiz.querySelector("[data-drop]");
    zona.addEventListener("dragover", event => {
        if (nivel() !== "archivos" || !ui.puede("archivos", "subir")) return;
        event.preventDefault();
        raiz.querySelector("[data-drop-destino]").textContent = `${almacen} / ${categoria}`;
        drop.hidden = false;
    });
    zona.addEventListener("dragleave", event => {
        if (!zona.contains(event.relatedTarget)) drop.hidden = true;
    });
    zona.addEventListener("drop", event => {
        if (nivel() !== "archivos") return;
        event.preventDefault();
        drop.hidden = true;
        subirArchivos([...(event.dataTransfer?.files || [])]);
    });
}

async function cargarArchivosConservandoFoco() {
    await cargar();
    const campo = raiz.querySelector("[data-buscar-archivo]");
    if (campo) {
        campo.focus();
        campo.setSelectionRange(campo.value.length, campo.value.length);
    }
}

export async function bind(stage, api) {
    ui = api;
    raiz = stage;
    enlazarEventos();
    await cargar();
}

export function reset() {
    window.clearTimeout(temporizador);
    seleccion = new Set();
}

let ui;
let raiz;
let nodos = [];
let reloj = 0;
let vigilancia = 0;

const VERSION = "20261127-4";
const DESCARGA = "../files/descargas/FileRed.jar";

export function loadStyles() {
    if (document.querySelector("link[data-estilo-nodos]")) return;
    const enlace = document.createElement("link");
    enlace.rel = "stylesheet";
    enlace.dataset.estiloNodos = "true";
    enlace.href = new URL(`./style.css?v=${VERSION}`, import.meta.url).href;
    document.head.appendChild(enlace);
}

export function render() {
    return `
        <section class="nd-hero">
            <div class="nd-hero-icon">${FileRedPanel.icono("server")}</div>
            <div class="nd-hero-copy">
                <h2>Tus nodos de almacenamiento</h2>
                <p>Un nodo es un lugar donde FileRed guarda tus archivos: la Nube de FileRed o cualquier equipo tuyo con el programa FileRed instalado (Windows, macOS o Linux). Todo lo que llega por la API se guarda en el nodo marcado como <strong>Por defecto</strong>. Los servidores de respaldo guardan una copia exacta de los nodos que elijas.</p>
            </div>
            <div class="nd-hero-actions">
                <a class="fr-btn" href="${DESCARGA}" download="FileRed.jar">${FileRedPanel.icono("download")}<span>Descargar FileRed</span></a>
                <button class="fr-btn fr-btn-primary" type="button" data-nuevo hidden>${FileRedPanel.icono("plus")}<span>Nuevo nodo</span></button>
            </div>
        </section>
        <div data-aviso-plan></div>
        <header class="nd-section"><span>${FileRedPanel.icono("hard")}</span><div><h3>Almacenamiento</h3><p>Dónde viven tus archivos.</p></div></header>
        <div class="nd-grid" data-lista></div>
        <header class="nd-section"><span class="is-backup">${FileRedPanel.icono("layers")}</span><div><h3>Servidores de respaldo</h3><p>Copias exactas de los nodos que elijas. Si un equipo se apaga, tus archivos se siguen entregando desde el respaldo.</p></div></header>
        <div class="nd-grid" data-respaldos></div>`;
}

function esNube(n) {
    return Number(n.nodoId) === 0;
}

function estado(n) {
    if (esNube(n)) return '<span class="nd-status is-on"><i></i>En línea</span>';
    if (!n.vinculado) return n.codigoVigente ? '<span class="nd-status is-wait"><i></i>Esperando vinculación</span>' : '<span class="nd-status is-wait"><i></i>Sin vincular</span>';
    return n.conectado ? '<span class="nd-status is-on"><i></i>Conectado</span>' : '<span class="nd-status is-off"><i></i>Desconectado</span>';
}

function espacio(n) {
    const total = Number(n.espacioTotal || 0);
    if (!total) return "";
    const libre = Number(n.espacioLibre || 0);
    const usado = Math.max(0, total - libre);
    const porcentaje = Math.min(100, (usado / total) * 100);
    const tono = porcentaje > 90 ? "is-danger" : porcentaje > 75 ? "is-warning" : "";
    return `<div class="nd-disk">
        <div class="nd-disk-head"><span>Disco del equipo</span><strong>${ui.formato.bytes(libre)} libres de ${ui.formato.bytes(total)}</strong></div>
        <div class="nd-bar ${tono}"><span style="width:${porcentaje.toFixed(1)}%"></span></div>
    </div>`;
}

function dato(icono, etiqueta, valor, skip = true) {
    if (!valor) return "";
    return `<div class="nd-kv">${ui.icono(icono)}<span>${ui.escapar(etiqueta)}</span><strong ${skip ? "data-i18n-skip" : ""} title="${ui.escapar(valor)}">${ui.escapar(valor)}</strong></div>`;
}

function botonDefecto(n) {
    if (n.predeterminado) return `<span class="nd-default">${ui.icono("star")}<span>Por defecto</span></span>`;
    if (!ui.puede("nodos", "predeterminar") || n.soloLectura || (!esNube(n) && !nube().planNodos) || (esNube(n) && n.planNube === false)) return "";
    const habilitado = esNube(n) || n.vinculado;
    return `<button class="fr-btn fr-btn-sm nd-make-default" type="button" data-accion="defecto" ${habilitado ? "" : 'disabled title="Vincula el equipo primero"'}>${ui.icono("star")}<span>Usar por defecto</span></button>`;
}

function esRespaldo(n) {
    return n.rol === "Respaldo";
}

function nube() {
    return nodos.find(esNube) || {};
}

function chipLectura(n) {
    if (!n.soloLectura) return "";
    return `<span class="nd-readonly" title="Tu plan no cubre este nodo">${ui.icono("lock")}Solo lectura${n.eliminacion ? ` · se elimina el ${ui.formato.fecha(n.eliminacion)}` : ""}</span>`;
}

function chipRespaldo(n) {
    const total = Number(n.respaldos || 0);
    if (!total) return "";
    return `<span class="nd-backup-chip">${ui.icono("layers")}${total === 1 ? "1 respaldo" : `${total} respaldos`}</span>`;
}

function nombreOrigen(id) {
    const n = nodos.find(x => Number(x.nodoId) === Number(id));
    return n ? (esNube(n) ? "Nube" : n.nombre) : "Nodo eliminado";
}

function tarjetaRespaldo(n) {
    const editar = ui.puede("nodos", "modificar");
    const cambiar = editar && !n.soloLectura;
    const eliminar = ui.puede("nodos", "eliminar");
    const total = Number(n.totalRespaldo || 0);
    const listos = Number(n.listosRespaldo || 0);
    const porcentaje = total ? Math.min(100, (listos / total) * 100) : 100;
    const origenes = (n.origenes || []).map(id => `<span class="nd-origin">${ui.icono(Number(id) === 0 ? "cloud" : "server")}<span data-i18n-skip>${ui.escapar(nombreOrigen(id))}</span></span>`).join("");
    const acciones = [
        cambiar ? `<button class="fr-row-btn" type="button" data-accion="origenes" title="Elegir nodos a respaldar" aria-label="Elegir nodos a respaldar">${ui.icono("layers")}</button>` : "",
        editar ? `<button class="fr-row-btn" type="button" data-accion="codigo" title="${n.vinculado ? "Vincular otro equipo" : "Generar código de vinculación"}" aria-label="Código de vinculación">${ui.icono("plug")}</button>` : "",
        cambiar ? `<button class="fr-row-btn" type="button" data-accion="renombrar" title="Renombrar" aria-label="Renombrar">${ui.icono("pencil")}</button>` : "",
        eliminar ? `<button class="fr-row-btn is-danger" type="button" data-accion="eliminar" title="Eliminar" aria-label="Eliminar">${ui.icono("trash")}</button>` : ""
    ].join("");
    const cuerpo = n.vinculado
        ? `${dato("monitor", "Equipo", [n.equipo, n.plataforma].filter(Boolean).join(" · "))}
           ${dato("folder", "Carpeta", n.carpeta)}
           ${dato("clock", "Última copia", n.ultimaCopia ? ui.formato.fechaHora(n.ultimaCopia) : "", false)}
           ${espacio(n)}`
        : `<div class="nd-pair">
                <span>${ui.icono("plug")}</span>
                <p>${n.codigoVigente ? `Instala FileRed en el servidor de respaldo y escribe el código que generaste. Vence: ${ui.formato.fechaHora(n.codigoExpira)}` : "Genera un código de vinculación e instala FileRed en el servidor de respaldo."}</p>
                ${editar ? `<button class="fr-btn fr-btn-sm fr-btn-primary" type="button" data-accion="codigo">${ui.icono("plug")}<span>${n.codigoVigente ? "Nuevo código" : "Generar código"}</span></button>` : ""}
           </div>`;
    return `
        <article class="nd-card is-backup ${n.vinculado && !n.conectado ? "is-offline" : ""}" data-nodo="${ui.escapar(n.nodoId)}">
            <header class="nd-card-head">
                <span class="nd-icon is-backup">${ui.icono("layers")}</span>
                <div class="nd-title">
                    <strong data-i18n-skip title="${ui.escapar(n.nombre)}">${ui.escapar(n.nombre)}</strong>
                    <small>${n.versionPrograma ? `Respaldo · FileRed ${ui.escapar(n.versionPrograma)}` : "Servidor de respaldo"}</small>
                </div>
            </header>
            <div class="nd-tags">${estado(n)}${Number(n.pendientes || 0) ? `<span class="nd-pending">${ui.icono("upload")}${ui.formato.numero(n.pendientes)} en cola</span>` : ""}${chipLectura(n)}</div>
            <div class="nd-sync">
                <div class="nd-disk-head"><span>Sincronizado</span><strong>${ui.formato.numero(listos)} de ${ui.formato.numero(total)} archivos · ${porcentaje.toFixed(0)}%</strong></div>
                <div class="nd-bar is-backup"><span style="width:${porcentaje.toFixed(1)}%"></span></div>
            </div>
            <div class="nd-origins"><small>Respalda</small><div>${origenes || '<span class="nd-origin is-empty">Ningún nodo elegido</span>'}</div></div>
            <div class="nd-body">${cuerpo}</div>
            ${acciones ? `<footer class="nd-actions">${acciones}</footer>` : ""}
        </article>`;
}

function tarjetaNube(n) {
    return `
        <article class="nd-card is-cloud ${n.predeterminado ? "is-default" : ""}" data-nodo="0">
            <div class="nd-card-glow" aria-hidden="true"></div>
            <header class="nd-card-head">
                <span class="nd-icon is-cloud">${ui.icono("cloud")}</span>
                <div class="nd-title">
                    <strong>Nube</strong>
                    <small>Servidor de FileRed</small>
                </div>
                ${botonDefecto(n)}
            </header>
            <div class="nd-tags">${estado(n)}<span class="nd-lock">${ui.icono("lock")}Integrado</span>${chipRespaldo(n)}${chipLectura(n)}</div>
            <div class="nd-numbers">
                <div><strong>${ui.formato.numero(n.archivos)}</strong><span>archivos</span></div>
                <div><strong>${ui.formato.bytes(n.bytes)}</strong><span>usados</span></div>
            </div>
            <p class="nd-foot">${n.planNube === false ? "Tu plan no incluye almacenamiento en la Nube: lo que llega por la API va directo a tu equipo Por defecto y, si está desconectado, no se guarda." : "Siempre disponible. Es el almacenamiento propio del servidor y no se puede editar ni eliminar."}</p>
        </article>`;
}

function tarjetaEquipo(n) {
    const editar = ui.puede("nodos", "modificar");
    const eliminar = ui.puede("nodos", "eliminar");
    const acciones = [
        ui.puede("nodos", "restaurar") && nube().planNube !== false && Number(n.respaldos || 0) && Number(n.archivos || 0) && !n.restauracion?.activa
            ? `<button class="fr-row-btn" type="button" data-accion="restaurar" title="Restaurar en la Nube desde el respaldo" aria-label="Restaurar en la Nube">${ui.icono("cloud")}</button>`
            : "",
        editar ? `<button class="fr-row-btn" type="button" data-accion="codigo" title="${n.vinculado ? "Vincular otro equipo" : "Generar código de vinculación"}" aria-label="Código de vinculación">${ui.icono("plug")}</button>` : "",
        editar && !n.soloLectura ? `<button class="fr-row-btn" type="button" data-accion="renombrar" title="Renombrar" aria-label="Renombrar">${ui.icono("pencil")}</button>` : "",
        eliminar ? `<button class="fr-row-btn is-danger" type="button" data-accion="eliminar" title="Eliminar" aria-label="Eliminar">${ui.icono("trash")}</button>` : ""
    ].join("");
    const pendientes = Number(n.pendientes || 0);
    const r = n.restauracion;
    const restaurando = r?.activa
        ? `<span class="nd-pending is-restore">${ui.icono("cloud")}Restaurando ${ui.formato.numero(Number(r.hechos || 0) + Number(r.errores || 0))} de ${ui.formato.numero(r.total)}</span>`
        : r && !r.activa
          ? `<span class="nd-backup-chip">${ui.icono("check")}Restaurados ${ui.formato.numero(r.hechos)}${Number(r.errores || 0) ? ` · ${ui.formato.numero(r.errores)} sin copia disponible` : ""}</span>`
          : "";
    const cuerpo = n.vinculado
        ? `${dato("monitor", "Equipo", [n.equipo, n.plataforma].filter(Boolean).join(" · "))}
           ${dato("folder", "Carpeta", n.carpeta)}
           ${dato("clock", "Última conexión", n.ultimaConexion ? ui.formato.fechaHora(n.ultimaConexion) : "", false)}
           ${espacio(n)}`
        : `<div class="nd-pair">
                <span>${ui.icono("plug")}</span>
                <p>${n.codigoVigente ? `Instala FileRed en el equipo y escribe el código que generaste. Vence: ${ui.formato.fechaHora(n.codigoExpira)}` : "Genera un código de vinculación e instala FileRed en el equipo donde quieres guardar los archivos."}</p>
                ${editar ? `<button class="fr-btn fr-btn-sm fr-btn-primary" type="button" data-accion="codigo">${ui.icono("plug")}<span>${n.codigoVigente ? "Nuevo código" : "Generar código"}</span></button>` : ""}
           </div>`;
    return `
        <article class="nd-card ${n.predeterminado ? "is-default" : ""} ${n.vinculado && !n.conectado ? "is-offline" : ""}" data-nodo="${ui.escapar(n.nodoId)}">
            <header class="nd-card-head">
                <span class="nd-icon">${ui.icono("server")}</span>
                <div class="nd-title">
                    <strong data-i18n-skip title="${ui.escapar(n.nombre)}">${ui.escapar(n.nombre)}</strong>
                    <small>${n.versionPrograma ? `FileRed ${ui.escapar(n.versionPrograma)}` : "Equipo propio"}</small>
                </div>
                ${botonDefecto(n)}
            </header>
            <div class="nd-tags">${estado(n)}${pendientes ? `<span class="nd-pending">${ui.icono("upload")}${ui.formato.numero(pendientes)} enviándose</span>` : ""}${chipRespaldo(n)}${restaurando}${chipLectura(n)}</div>
            <div class="nd-numbers">
                <div><strong>${ui.formato.numero(n.archivos)}</strong><span>archivos</span></div>
                <div><strong>${ui.formato.bytes(n.bytes)}</strong><span>guardados</span></div>
            </div>
            <div class="nd-body">${cuerpo}</div>
            ${acciones ? `<footer class="nd-actions">${acciones}</footer>` : ""}
        </article>`;
}

function tarjetaNueva(rol) {
    if (!ui.puede("nodos", "crear")) return "";
    const respaldo = rol === "Respaldo";
    const permitido = respaldo ? nube().planRespaldos : nube().planNodos;
    if (!permitido) {
        return `<div class="nd-card nd-new is-locked">
            <span class="nd-new-icon">${ui.icono("lock")}</span>
            <strong>${respaldo ? "Respaldos no incluidos" : "Nodos propios no incluidos"}</strong>
            <small>${respaldo ? "Tu plan actual no incluye servidores de respaldo." : "Tu plan actual solo usa la Nube."} Mejora tu plan para activarlo.</small>
        </div>`;
    }
    return `<button class="nd-card nd-new ${respaldo ? "is-backup" : ""}" type="button" data-nuevo="${respaldo ? "Respaldo" : "Almacenamiento"}">
        <span class="nd-new-icon">${ui.icono(respaldo ? "layers" : "plus")}</span>
        <strong>${respaldo ? "Agregar servidor de respaldo" : "Agregar nodo"}</strong>
        <small>${respaldo ? "Un servidor tuyo que guarda una copia exacta de los nodos que elijas." : "Guarda tus archivos en un equipo de tu casa, oficina o servidor propio."}</small>
    </button>`;
}

function pintar() {
    const base = nube();
    const almacenamiento = nodos.filter(n => !esRespaldo(n));
    const copias = nodos.filter(esRespaldo);
    const avisos = [];
    if (base.planNube === false) avisos.push("almacenamiento en la Nube");
    if (!base.planNodos) avisos.push("nodos propios");
    if (!base.planRespaldos) avisos.push("servidores de respaldo");
    const afectados = nodos.some(n => n.soloLectura);
    raiz.querySelector("[data-aviso-plan]").innerHTML = avisos.length
        ? `<p class="fr-note nd-plan-note">${ui.icono("lock")}<span>Tu plan no incluye ${avisos.join(" ni ")}. ${afectados
              ? `Los nodos afectados quedan en <b>solo lectura</b>: puedes ver y descargar tus archivos, volver a conectar el equipo, restaurarlos en la Nube o eliminarlos. Si en ${ui.formato.numero(base.diasSinPlan || 30)} días tu plan no los cubre, se eliminarán automáticamente.`
              : "Mejora tu plan para activarlos."}</span></p>`
        : "";
    if (base.planNube === false && !almacenamiento.some(n => n.predeterminado)) {
        raiz.querySelector("[data-aviso-plan]").innerHTML += `<p class="fr-note nd-plan-note">${ui.icono("star")}<span>Marca uno de tus equipos como <b>Por defecto</b>: tu plan no incluye la Nube y, sin un equipo por defecto, la API no podrá recibir archivos.</span></p>`;
    }
    raiz.querySelector("[data-lista]").innerHTML = almacenamiento.map(n => (esNube(n) ? tarjetaNube(n) : tarjetaEquipo(n))).join("") + tarjetaNueva("Almacenamiento");
    raiz.querySelector("[data-respaldos]").innerHTML = copias.map(tarjetaRespaldo).join("") + tarjetaNueva("Respaldo");
    const nuevoArriba = raiz.querySelector(".nd-hero [data-nuevo]");
    if (nuevoArriba) nuevoArriba.hidden = !ui.puede("nodos", "crear") || (!base.planNodos && !base.planRespaldos);
}

async function cargar(silencioso = false) {
    try {
        nodos = (await NodoService.listar(silencioso)) || [];
        pintar();
    } catch (error) {
        if (!silencioso) {
            raiz.querySelector("[data-lista]").innerHTML = `<div class="fr-empty">${ui.icono("ban")}<p>${ui.escapar(error.message)}</p></div>`;
            raiz.querySelector("[data-respaldos]").innerHTML = "";
            ui.notificarError(error);
        }
    }
}

function nombreFormulario({ titulo, valor = "", textoGuardar, alGuardar }) {
    ui.formulario({
        titulo,
        subtitulo: "Por ejemplo: Servidor de la oficina, PC de la casa, NAS.",
        ancho: "sm",
        textoGuardar,
        campos: ui.campo({ nombre: "nombre", etiqueta: "Nombre del nodo", valor, requerido: true, max: 80, clase: "is-full" }),
        alAbrir(form) {
            window.setTimeout(() => form.elements.nombre.select(), 80);
        },
        async alGuardar(form) {
            ui.exigir(form, [
                ["nombre", "El nombre del nodo es obligatorio."],
                ["nombre", "Ese nombre está reservado para la Nube.", v => !["nube", "cloud"].includes(v.toLowerCase())]
            ]);
            await alGuardar(form.elements.nombre.value.trim());
        }
    });
}

function casillasOrigenes(seleccionados) {
    const activos = new Set((seleccionados || []).map(Number));
    const opciones = nodos.filter(n => !esRespaldo(n) && !(esNube(n) && n.planNube === false && !activos.has(0)));
    return `<fieldset class="nd-origin-pick"><legend>Nodos que respaldará</legend>${opciones
        .map(
            n => `<label class="nd-origin-option">
                <input type="checkbox" name="origenes" data-multiple value="${ui.escapar(n.nodoId)}" ${activos.has(Number(n.nodoId)) ? "checked" : ""}>
                <span class="nd-origin-box">${ui.icono(esNube(n) ? "cloud" : "server")}<span><strong data-i18n-skip>${ui.escapar(esNube(n) ? "Nube" : n.nombre)}</strong><small>${ui.formato.numero(n.archivos)} archivos · ${ui.formato.bytes(n.bytes)}</small></span></span>
            </label>`
        )
        .join("")}</fieldset>`;
}

function formularioRespaldo() {
    ui.formulario({
        titulo: "Nuevo servidor de respaldo",
        subtitulo: "Guarda una copia exacta de los nodos que elijas, incluyendo lo que ya tienen.",
        ancho: "md",
        textoGuardar: "Crear respaldo",
        campos: `${ui.campo({ nombre: "nombre", etiqueta: "Nombre del respaldo", requerido: true, max: 80, clase: "is-full", ayuda: "Por ejemplo: Respaldo oficina, Servidor NAS." })}
            ${casillasOrigenes(nube().planNube === false ? [] : [0])}`,
        alAbrir(form) {
            window.setTimeout(() => form.elements.nombre.focus(), 80);
        },
        async alGuardar(form) {
            ui.exigir(form, [
                ["nombre", "El nombre del respaldo es obligatorio."],
                ["nombre", "Ese nombre está reservado para la Nube.", v => !["nube", "cloud"].includes(v.toLowerCase())]
            ]);
            const datos = ui.valores(form);
            if (!datos.origenes?.length) throw new Error("Elige al menos un nodo para respaldar.");
            const r = await NodoService.agregar(datos.nombre, "Respaldo", datos.origenes);
            await cargar(true);
            window.setTimeout(() => mostrarCodigo(r), 200);
        }
    });
}

function elegirOrigenes(n) {
    ui.formulario({
        titulo: "Nodos que respalda",
        subtitulo: n.nombre,
        ancho: "md",
        textoGuardar: "Guardar",
        campos: `${casillasOrigenes(n.origenes)}<p class="fr-note nd-form-note">Al agregar un nodo se copia todo lo que ya tiene. Al quitarlo, su copia se elimina de este respaldo.</p>`,
        async alGuardar(form) {
            const datos = ui.valores(form);
            if (!datos.origenes?.length) throw new Error("Elige al menos un nodo para respaldar.");
            await NodoService.origenes(n.nodoId, datos.origenes);
            AppNotifications.success("Nodos respaldados actualizados");
            await cargar(true);
        }
    });
}

async function restaurar(n, boton) {
    const ok = await ui.confirmar({
        titulo: "Restaurar en la Nube",
        mensaje: `Los ${ui.formato.numero(n.archivos)} archivos de ${n.nombre} se copiarán desde sus respaldos a la Nube y desde ese momento vivirán en la Nube. Sus URLs no cambian. Si el equipo vuelve a conectarse, se borrarán de él.`,
        textoConfirmar: "Restaurar"
    });
    if (!ok) return;
    try {
        await ui.ocupado(boton, () => NodoService.restaurar(n.nodoId));
        AppNotifications.success("Restauración iniciada");
        await cargar(true);
    } catch (error) {
        ui.notificarError(error);
    }
}

function mostrarCodigo(r) {
    window.clearInterval(vigilancia);
    const { raiz: modal, cerrar } = ui.modal({
        titulo: "Vincula tu equipo",
        subtitulo: r.nombre || "",
        ancho: "md",
        cuerpo: `
            <div class="nd-link" data-vinculo>
                <ol class="nd-steps">
                    <li><span>1</span><div><strong>Descarga FileRed</strong><p>Funciona en Windows, macOS y Linux con Java 17 o superior.</p><a class="fr-btn fr-btn-sm" href="${DESCARGA}" download="FileRed.jar">${ui.icono("download")}<span>Descargar FileRed</span></a></div></li>
                    <li><span>2</span><div><strong>Ábrelo en el equipo</strong><p>Escribe el servidor y el código de vinculación, y elige la carpeta donde se guardarán los archivos.</p></div></li>
                </ol>
                <div class="nd-secret-grid">
                    <div><small>Servidor</small><div class="fr-secret" data-i18n-skip><code>${ui.escapar(r.servidor || window.location.origin)}</code><button class="fr-btn fr-btn-sm" type="button" data-copiar="servidor">${ui.icono("copy")}<span>Copiar</span></button></div></div>
                    <div><small>Código de vinculación</small><div class="fr-secret nd-code" data-i18n-skip><code>${ui.escapar(r.codigo)}</code><button class="fr-btn fr-btn-sm" type="button" data-copiar="codigo">${ui.icono("copy")}<span>Copiar</span></button></div></div>
                </div>
                <p class="nd-wait"><span class="nd-pulse"></span>Esperando a que el equipo se vincule… Este código solo se muestra una vez. Vence: ${ui.formato.fechaHora(r.expira)}</p>
            </div>`,
        pie: '<button class="fr-btn fr-btn-primary" type="button" data-modal-close>Listo</button>',
        alCerrar: () => {
            window.clearInterval(vigilancia);
            cargar(true);
        }
    });
    modal.addEventListener("click", event => {
        const boton = event.target.closest("[data-copiar]");
        if (!boton) return;
        ui.copiar(boton.dataset.copiar === "codigo" ? r.codigo : r.servidor || window.location.origin);
    });
    vigilancia = window.setInterval(async () => {
        if (!modal.isConnected) return window.clearInterval(vigilancia);
        try {
            const lista = (await NodoService.listar(true)) || [];
            const actual = lista.find(n => String(n.nodoId) === String(r.nodoId));
            if (!actual?.vinculado) return;
            window.clearInterval(vigilancia);
            nodos = lista;
            pintar();
            modal.querySelector("[data-vinculo]").innerHTML = `
                <div class="nd-linked">
                    <span>${ui.icono("check")}</span>
                    <strong>¡Equipo vinculado!</strong>
                    <p data-i18n-skip>${ui.escapar(actual.nombre)}${actual.equipo ? ` · ${ui.escapar(actual.equipo)}` : ""}</p>
                    <small>Márcalo como <b>Por defecto</b> para que los archivos que lleguen por la API se guarden en este equipo.</small>
                </div>`;
            window.setTimeout(() => {
                if (modal.isConnected) cerrar();
            }, 4500);
        } catch (_) {
            return;
        }
    }, 4000);
}

function nuevo() {
    nombreFormulario({
        titulo: "Nuevo nodo",
        textoGuardar: "Crear nodo",
        async alGuardar(nombre) {
            const r = await NodoService.agregar(nombre);
            await cargar(true);
            window.setTimeout(() => mostrarCodigo(r), 200);
        }
    });
}

function renombrar(n) {
    nombreFormulario({
        titulo: "Renombrar nodo",
        valor: n.nombre,
        textoGuardar: "Guardar",
        async alGuardar(nombre) {
            await NodoService.renombrar(n.nodoId, nombre);
            AppNotifications.success("Nodo actualizado correctamente");
            await cargar(true);
        }
    });
}

async function codigo(n, boton) {
    if (n.vinculado) {
        const ok = await ui.confirmar({
            titulo: "Vincular otro equipo",
            mensaje: `El equipo vinculado a ${n.nombre} se desconectará y dejará de recibir archivos hasta que se vincule de nuevo con el código nuevo. Los archivos ya guardados se conservan.`,
            textoConfirmar: "Generar código"
        });
        if (!ok) return;
    }
    try {
        const r = await ui.ocupado(boton, () => NodoService.generarCodigo(n.nodoId));
        await cargar(true);
        mostrarCodigo(r);
    } catch (error) {
        ui.notificarError(error);
    }
}

async function predeterminar(n, boton) {
    try {
        const r = await ui.ocupado(boton, () => NodoService.predeterminar(Number(n.nodoId)));
        AppNotifications.success(r?.mensaje || "Nodo por defecto actualizado");
        await cargar(true);
    } catch (error) {
        ui.notificarError(error);
    }
}

async function eliminar(n) {
    const archivos = Number(n.archivos || 0);
    const ok = await ui.confirmar({
        titulo: "Eliminar nodo",
        mensaje: archivos
            ? `Se eliminará ${n.nombre} y FileRed olvidará sus ${archivos} archivos: sus URLs públicas dejarán de funcionar. Los archivos que ya están en el equipo se quedan en su carpeta. Esta acción no se puede deshacer.`
            : `Se eliminará ${n.nombre} y el equipo quedará desvinculado. Esta acción no se puede deshacer.`,
        textoConfirmar: "Eliminar nodo",
        peligro: true
    });
    if (!ok) return;
    try {
        await NodoService.eliminar(n.nodoId);
        AppNotifications.success("Nodo eliminado correctamente");
        await cargar(true);
    } catch (error) {
        ui.notificarError(error);
    }
}

export async function bind(stage, api) {
    ui = api;
    raiz = stage;
    const nuevoArriba = raiz.querySelector("[data-nuevo]");
    nuevoArriba.hidden = !ui.puede("nodos", "crear");
    nuevoArriba.addEventListener("click", () => (nube().planNodos ? nuevo() : formularioRespaldo()));
    const alClic = event => {
        const crear = event.target.closest("[data-nuevo]");
        if (crear) return crear.dataset.nuevo === "Respaldo" ? formularioRespaldo() : nuevo();
        const boton = event.target.closest("[data-accion]");
        if (!boton || boton.disabled) return;
        const tarjeta = boton.closest("[data-nodo]");
        const n = nodos.find(x => String(x.nodoId) === tarjeta?.dataset.nodo);
        if (!n) return;
        const acciones = {
            defecto: () => predeterminar(n, boton),
            codigo: () => codigo(n, boton),
            renombrar: () => renombrar(n),
            eliminar: () => eliminar(n),
            origenes: () => elegirOrigenes(n),
            restaurar: () => restaurar(n, boton)
        };
        acciones[boton.dataset.accion]?.();
    };
    raiz.querySelector("[data-lista]").addEventListener("click", alClic);
    raiz.querySelector("[data-respaldos]").addEventListener("click", alClic);
    await cargar();
    window.clearInterval(reloj);
    reloj = window.setInterval(() => {
        if (!raiz.isConnected) return window.clearInterval(reloj);
        if (!document.hidden) cargar(true);
    }, 8000);
}

export function reset() {
    window.clearInterval(reloj);
    window.clearInterval(vigilancia);
    nodos = [];
}

export function skeleton() {
    const b = css => `<div class="fr-skeleton" style="${css}"></div>`;
    const card = `<div style="border:1px solid var(--line);border-radius:18px;padding:18px;display:grid;gap:14px;background:var(--surface)">
        <div style="display:flex;gap:12px;align-items:center">${b("height:46px;width:46px;border-radius:14px;flex:none")}<div style="flex:1;display:grid;gap:7px">${b("height:14px;width:55%")}${b("height:10px;width:35%")}</div>${b("height:28px;width:96px;border-radius:999px;flex:none")}</div>
        <div style="display:flex;gap:8px">${b("height:22px;width:90px;border-radius:999px")}${b("height:22px;width:80px;border-radius:999px")}</div>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px">${b("height:54px;border-radius:12px")}${b("height:54px;border-radius:12px")}</div>
        ${b("height:10px;width:80%")}${b("height:10px;width:60%")}</div>`;
    return `<section class="fr-panel" style="padding:20px;display:flex;gap:16px;align-items:center;flex-wrap:wrap">${b("height:54px;width:54px;border-radius:16px;flex:none")}<div style="flex:1;min-width:220px;display:grid;gap:9px">${b("height:16px;width:40%")}${b("height:10px;width:85%")}${b("height:10px;width:70%")}</div><div style="display:flex;gap:10px">${b("height:40px;width:160px;border-radius:12px")}${b("height:40px;width:130px;border-radius:12px")}</div></section>
        <div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(300px,1fr));gap:16px;margin-top:16px">${card}${card}${card}</div>`;
}

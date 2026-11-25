import { barras, dona, exportarCsv } from "../../../comunes/graficos.js?v=20261120-7";

let ui;
let raiz;
let datos = null;

const COLUMNAS = [
    { titulo: "Almacén", valor: c => c.almacen },
    { titulo: "Carpeta", valor: c => c.categoria },
    { titulo: "Archivos", valor: c => c.archivos },
    { titulo: "Bytes", valor: c => c.bytes },
    { titulo: "Última modificación", valor: c => c.ultimaModificacion }
];

export function render() {
    return `
        <div class="fr-toolbar">
            <p class="fr-toolbar-text">Cómo se distribuye el espacio de tu cuenta entre almacenes, carpetas y tipos de archivo.</p>
            <span class="fr-toolbar-spacer"></span>
            <button class="fr-btn" type="button" data-recargar>${FileRedPanel.icono("refresh")}<span>Actualizar</span></button>
            <button class="fr-btn" type="button" data-exportar>${FileRedPanel.icono("download")}<span>Exportar CSV</span></button>
        </div>
        <div class="fr-stats" data-stats></div>
        <div class="fr-bento">
            <section class="fr-panel span-7">
                <header class="fr-panel-head"><div><h2>Por almacén</h2><p>Espacio ocupado</p></div></header>
                <div class="fr-panel-body" data-almacenes></div>
            </section>
            <section class="fr-panel span-5">
                <header class="fr-panel-head"><div><h2>Por tipo de archivo</h2><p>Top 6 extensiones</p></div></header>
                <div class="fr-panel-body" data-tipos></div>
            </section>
        </div>
        <div class="fr-grid-2">
            <section class="fr-panel">
                <header class="fr-panel-head"><div><h2>Por carpeta</h2></div></header>
                <div data-carpetas></div>
            </section>
            <section class="fr-panel">
                <header class="fr-panel-head"><div><h2>Archivos más grandes</h2></div></header>
                <div data-grandes></div>
            </section>
        </div>`;
}

async function cargar() {
    try {
        datos = await ReporteService.consumo();
        const carpetas = datos.porCarpeta || [];
        const porAlmacen = new Map();
        carpetas.forEach(c => porAlmacen.set(c.almacen, (porAlmacen.get(c.almacen) || 0) + Number(c.bytes || 0)));
        raiz.querySelector("[data-stats]").innerHTML = [
            ui.tarjetaDato({ titulo: "Espacio usado", valor: ui.formato.bytes(datos.bytes), iconoNombre: "hard", tono: "is-accent" }),
            ui.tarjetaDato({ titulo: "Archivos", valor: ui.formato.numero(datos.archivos), iconoNombre: "file" }),
            ui.tarjetaDato({ titulo: "Almacenes con archivos", valor: ui.formato.numero(porAlmacen.size), iconoNombre: "folder-open" }),
            ui.tarjetaDato({ titulo: "Carpetas con archivos", valor: ui.formato.numero(carpetas.length), iconoNombre: "folder" })
        ].join("");
        raiz.querySelector("[data-almacenes]").innerHTML = porAlmacen.size
            ? barras({ items: [...porAlmacen.entries()].sort((a, b) => b[1] - a[1]).map(([etiqueta, valor]) => ({ etiqueta, valor })), formato: v => ui.formato.bytes(v) })
            : `<div class="fr-empty">${ui.icono("hard")}<p>Aún no tienes archivos.</p></div>`;
        const tipos = (datos.porTipo || []).slice(0, 6);
        raiz.querySelector("[data-tipos]").innerHTML = tipos.length
            ? dona({ items: tipos.map(t => ({ etiqueta: String(t.extension).toUpperCase(), valor: Number(t.bytes || 0) })), formato: v => ui.formato.bytes(v), centro: String(tipos.length) })
            : `<div class="fr-empty">${ui.icono("pie-chart")}<p>Sin datos.</p></div>`;
        raiz.querySelector("[data-carpetas]").innerHTML = ui.tabla({
            filas: carpetas,
            vacio: "Sin archivos.",
            columnas: [
                { titulo: "Carpeta", valor: c => `<span class="fr-cell-main" data-i18n-skip>${ui.escapar(c.categoria)}</span><span class="fr-cell-sub" data-i18n-skip>${ui.escapar(c.almacen)}</span>` },
                { titulo: "Archivos", clase: "is-right", valor: c => ui.formato.numero(c.archivos) },
                { titulo: "Espacio", clase: "is-right", valor: c => `<strong>${ui.formato.bytes(c.bytes)}</strong>` }
            ]
        });
        raiz.querySelector("[data-grandes]").innerHTML = ui.tabla({
            filas: datos.masGrandes || [],
            vacio: "Sin archivos.",
            columnas: [
                {
                    titulo: "Archivo",
                    valor: a => `<span class="fr-cell-main" data-i18n-skip>${ui.escapar(a.nombreOriginal || `${a.identificador}.${a.extension}`)}</span><span class="fr-cell-sub" data-i18n-skip>${ui.escapar(a.almacen)} / ${ui.escapar(a.categoria)}</span>`
                },
                { titulo: "Tamaño", clase: "is-right", valor: a => `<strong>${ui.formato.bytes(a.tamano)}</strong>` }
            ]
        });
    } catch (error) {
        ui.notificarError(error);
    }
}

export async function bind(stage, api) {
    ui = api;
    raiz = stage;
    raiz.querySelector("[data-recargar]").addEventListener("click", cargar);
    raiz.querySelector("[data-exportar]").addEventListener("click", () => {
        if (datos) exportarCsv("consumo_almacenamiento", COLUMNAS, datos.porCarpeta || []);
    });
    await cargar();
}

export function reset() {
    datos = null;
}

export function skeleton() {
    return window.FileRedSk.tablero({ tarjetas: 4, columnas: 5, filtros: true });
}

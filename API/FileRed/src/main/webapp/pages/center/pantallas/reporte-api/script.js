import { area, activarTooltips, barras, diasEntre, enlazarFiltros, exportarCsv, fechaCorta, filtrosFecha, hoyIso } from "../../../comunes/graficos.js?v=20261120-7";

let ui;
let raiz;
let datos = null;

const COLUMNAS = [
    { titulo: "Cliente", valor: c => c.clienteNombre },
    { titulo: "Solicitudes", valor: c => c.solicitudesApi },
    { titulo: "Archivos subidos", valor: c => c.archivosSubidos },
    { titulo: "Bytes subidos", valor: c => c.bytesSubidos },
    { titulo: "Archivos eliminados", valor: c => c.archivosEliminados },
    { titulo: "Descargas", valor: c => c.descargas },
    { titulo: "Bytes descargados", valor: c => c.bytesDescargados }
];

export function render() {
    return `
        ${filtrosFecha({ desde: hoyIso(29), hasta: hoyIso(0) })}
        <div class="fr-stats" data-stats></div>
        <div class="fr-bento">
            <section class="fr-panel span-8">
                <header class="fr-panel-head"><div><h2>Tráfico diario</h2><p>Solicitudes a la API y descargas públicas</p></div></header>
                <div class="fr-panel-body" data-serie></div>
            </section>
            <section class="fr-panel span-4">
                <header class="fr-panel-head"><div><h2>Clientes más activos</h2><p>Por solicitudes</p></div></header>
                <div class="fr-panel-body" data-top></div>
            </section>
        </div>
        <section class="fr-panel">
            <header class="fr-panel-head"><div><h2>Uso por cliente</h2></div></header>
            <div data-detalle></div>
        </section>`;
}

async function generar(filtro) {
    try {
        datos = await ReporteService.usoApi(filtro);
        const t = datos.totales || {};
        raiz.querySelector("[data-stats]").innerHTML = [
            ui.tarjetaDato({ titulo: "Solicitudes API", valor: ui.formato.numero(t.solicitudesApi), iconoNombre: "activity", tono: "is-accent" }),
            ui.tarjetaDato({ titulo: "Archivos subidos", valor: ui.formato.numero(t.archivosSubidos), detalle: ui.formato.bytes(t.bytesSubidos), iconoNombre: "upload" }),
            ui.tarjetaDato({ titulo: "Descargas", valor: ui.formato.numero(t.descargas), detalle: ui.formato.bytes(t.bytesDescargados), iconoNombre: "download" }),
            ui.tarjetaDato({ titulo: "Archivos eliminados", valor: ui.formato.numero(t.archivosEliminados), iconoNombre: "trash" })
        ].join("");
        const porDia = new Map((datos.porDia || []).map(d => [String(d.fecha).slice(0, 10), d]));
        raiz.querySelector("[data-serie]").innerHTML = `${area({
            puntos: diasEntre(datos.desde, datos.hasta).map(d => ({
                etiqueta: d,
                valor: Number(porDia.get(d)?.solicitudesApi || 0),
                secundario: Number(porDia.get(d)?.descargas || 0)
            })),
            etiqueta: p => fechaCorta(p.etiqueta),
            formato: v => `${ui.formato.numero(v)} solicitudes`,
            secundaria: { nombre: "Descargas", formato: v => ui.formato.numero(v) }
        })}<div class="fr-chart-legend"><span><i style="background:var(--accent-strong)"></i>Solicitudes API</span><span><i style="background:var(--cyan)"></i>Descargas</span></div>`;
        const top = (datos.detalle || []).slice(0, 8);
        raiz.querySelector("[data-top]").innerHTML = top.length
            ? barras({ items: top.map(c => ({ etiqueta: c.clienteNombre, valor: Number(c.solicitudesApi || 0) })), formato: v => ui.formato.numero(v) })
            : `<div class="fr-empty">${ui.icono("activity")}<p>Sin actividad en el periodo.</p></div>`;
        raiz.querySelector("[data-detalle]").innerHTML = ui.tabla({
            filas: datos.detalle || [],
            vacio: "No hay actividad en el periodo seleccionado.",
            columnas: [
                { titulo: "Cliente", valor: c => `<span class="fr-cell-main" data-i18n-skip>${ui.escapar(c.clienteNombre)}</span>` },
                { titulo: "Solicitudes", clase: "is-right", valor: c => ui.formato.numero(c.solicitudesApi) },
                { titulo: "Subidos", clase: "is-right", valor: c => `${ui.formato.numero(c.archivosSubidos)}<span class="fr-cell-sub">${ui.formato.bytes(c.bytesSubidos)}</span>` },
                { titulo: "Descargas", clase: "is-right", valor: c => `${ui.formato.numero(c.descargas)}<span class="fr-cell-sub">${ui.formato.bytes(c.bytesDescargados)}</span>` },
                { titulo: "Eliminados", clase: "is-right", valor: c => ui.formato.numero(c.archivosEliminados) }
            ]
        });
        activarTooltips(raiz);
    } catch (error) {
        ui.notificarError(error);
    }
}

export async function bind(stage, api) {
    ui = api;
    raiz = stage;
    enlazarFiltros(raiz, generar, () => {
        if (datos) exportarCsv(`uso_api_${datos.desde}_${datos.hasta}`, COLUMNAS, datos.detalle || []);
    });
    await generar({ desde: hoyIso(29), hasta: hoyIso(0) });
}

export function reset() {
    datos = null;
}

export function skeleton() {
    return window.FileRedSk.tablero({ tarjetas: 4, columnas: 5, filtros: true });
}

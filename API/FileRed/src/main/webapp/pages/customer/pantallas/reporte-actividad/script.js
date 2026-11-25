import { area, activarTooltips, diasEntre, enlazarFiltros, exportarCsv, fechaCorta, filtrosFecha, hoyIso } from "../../../comunes/graficos.js?v=20261120-7";

let ui;
let raiz;
let datos = null;
let filas = [];

const COLUMNAS = [
    { titulo: "Fecha", valor: d => d.fecha },
    { titulo: "Solicitudes API", valor: d => d.solicitudesApi },
    { titulo: "Archivos subidos", valor: d => d.archivosSubidos },
    { titulo: "Bytes subidos", valor: d => d.bytesSubidos },
    { titulo: "Archivos eliminados", valor: d => d.archivosEliminados },
    { titulo: "Descargas", valor: d => d.descargas },
    { titulo: "Bytes descargados", valor: d => d.bytesDescargados }
];

export function render() {
    return `
        ${filtrosFecha({ desde: hoyIso(29), hasta: hoyIso(0) })}
        <div class="fr-stats" data-stats></div>
        <div class="fr-bento">
            <section class="fr-panel span-8">
                <header class="fr-panel-head"><div><h2>Actividad diaria</h2><p>Solicitudes a la API y descargas públicas</p></div></header>
                <div class="fr-panel-body" data-serie></div>
            </section>
            <section class="fr-panel span-4">
                <header class="fr-panel-head"><div><h2>API keys</h2><p>Último uso y archivos subidos</p></div></header>
                <div data-llaves></div>
            </section>
        </div>
        <section class="fr-panel">
            <header class="fr-panel-head"><div><h2>Detalle por día</h2></div></header>
            <div data-detalle></div>
        </section>`;
}

async function generar(filtro) {
    try {
        datos = await ReporteService.actividad(filtro);
        const t = datos.totales || {};
        raiz.querySelector("[data-stats]").innerHTML = [
            ui.tarjetaDato({ titulo: "Solicitudes API", valor: ui.formato.numero(t.solicitudesApi), iconoNombre: "activity", tono: "is-accent" }),
            ui.tarjetaDato({ titulo: "Archivos subidos", valor: ui.formato.numero(t.archivosSubidos), detalle: ui.formato.bytes(t.bytesSubidos), iconoNombre: "upload" }),
            ui.tarjetaDato({ titulo: "Descargas", valor: ui.formato.numero(t.descargas), detalle: ui.formato.bytes(t.bytesDescargados), iconoNombre: "download" }),
            ui.tarjetaDato({ titulo: "Archivos eliminados", valor: ui.formato.numero(t.archivosEliminados), iconoNombre: "trash" })
        ].join("");
        const porDia = new Map((datos.porDia || []).map(d => [String(d.fecha).slice(0, 10), d]));
        const dias = diasEntre(datos.desde, datos.hasta);
        filas = dias.map(d => ({ fecha: d, ...(porDia.get(d) || {}) }));
        raiz.querySelector("[data-serie]").innerHTML = `${area({
            puntos: filas.map(d => ({ etiqueta: d.fecha, valor: Number(d.solicitudesApi || 0), secundario: Number(d.descargas || 0) })),
            etiqueta: p => fechaCorta(p.etiqueta),
            formato: v => `${ui.formato.numero(v)} solicitudes`,
            secundaria: { nombre: "Descargas", formato: v => ui.formato.numero(v) }
        })}<div class="fr-chart-legend"><span><i style="background:var(--accent-strong)"></i>Solicitudes API</span><span><i style="background:var(--cyan)"></i>Descargas</span></div>`;
        raiz.querySelector("[data-llaves]").innerHTML = ui.tabla({
            filas: datos.apiKeys || [],
            vacio: "No tienes API keys.",
            columnas: [
                { titulo: "API key", valor: k => `<span class="fr-cell-main" data-i18n-skip>${ui.escapar(k.nombre)}</span><span class="fr-cell-sub">${ui.formato.fechaHora(k.fechaUltimoUso)}</span>` },
                { titulo: "Archivos", clase: "is-right", valor: k => ui.formato.numero(k.archivos) }
            ]
        });
        raiz.querySelector("[data-detalle]").innerHTML = ui.tabla({
            filas: [...filas].reverse().filter(d => Number(d.solicitudesApi || 0) + Number(d.descargas || 0) + Number(d.archivosSubidos || 0) > 0),
            vacio: "Sin actividad en el periodo seleccionado.",
            columnas: [
                { titulo: "Fecha", valor: d => ui.formato.fecha(`${d.fecha}T00:00:00`) },
                { titulo: "Solicitudes", clase: "is-right", valor: d => ui.formato.numero(d.solicitudesApi) },
                { titulo: "Subidos", clase: "is-right", valor: d => `${ui.formato.numero(d.archivosSubidos)}<span class="fr-cell-sub">${ui.formato.bytes(d.bytesSubidos)}</span>` },
                { titulo: "Descargas", clase: "is-right", valor: d => `${ui.formato.numero(d.descargas)}<span class="fr-cell-sub">${ui.formato.bytes(d.bytesDescargados)}</span>` },
                { titulo: "Eliminados", clase: "is-right", valor: d => ui.formato.numero(d.archivosEliminados) }
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
        if (datos) exportarCsv(`actividad_${datos.desde}_${datos.hasta}`, COLUMNAS, filas);
    });
    await generar({ desde: hoyIso(29), hasta: hoyIso(0) });
}

export function reset() {
    datos = null;
    filas = [];
}

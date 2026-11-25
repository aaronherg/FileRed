import { barras, exportarCsv } from "../../../comunes/graficos.js?v=20261120-7";

let ui;
let raiz;
let datos = null;
let temporizador = 0;

const COLUMNAS = [
    { titulo: "Cliente", valor: c => c.clienteNombre },
    { titulo: "Estado", valor: c => c.estado },
    { titulo: "Plan", valor: c => c.planNombre || "" },
    { titulo: "Espacio (GB)", valor: c => (c.planNombre ? (c.espacioGb == null ? "Ilimitado" : c.espacioGb) : "") },
    { titulo: "Usado (bytes)", valor: c => c.bytes },
    { titulo: "Archivos", valor: c => c.archivos },
    { titulo: "Almacenes", valor: c => c.almacenes },
    { titulo: "Solicitudes API 30 días", valor: c => c.solicitudes30 }
];

export function render() {
    return `
        <div class="fr-toolbar">
            <label class="fr-search">${FileRedPanel.icono("search")}<input class="fr-input" type="search" placeholder="Buscar cliente" data-buscar aria-label="Buscar cliente"></label>
            <span class="fr-toolbar-spacer"></span>
            <button class="fr-btn" type="button" data-exportar>${FileRedPanel.icono("download")}<span>Exportar CSV</span></button>
        </div>
        <div class="fr-stats" data-stats></div>
        <section class="fr-panel" style="margin-bottom:16px">
            <header class="fr-panel-head"><div><h2>Clientes con más almacenamiento</h2><p>Top 10 por espacio usado</p></div></header>
            <div class="fr-panel-body" data-top></div>
        </section>
        <section class="fr-panel">
            <header class="fr-panel-head"><div><h2>Consumo por cliente</h2></div></header>
            <div data-detalle></div>
        </section>`;
}

function uso(c) {
    if (!c.planNombre) return '<span class="fr-badge is-warning">Sin plan</span>';
    if (c.espacioGb == null) return `<span class="fr-cell-main">${ui.formato.bytes(c.bytes)}</span><span class="fr-cell-sub">Ilimitado</span>`;
    const limite = Number(c.espacioGb) * 1024 ** 3;
    const porcentaje = Math.min(100, (Number(c.bytes || 0) / Math.max(1, limite)) * 100);
    return `<div style="min-width:160px"><span class="fr-cell-sub">${ui.formato.bytes(c.bytes)} de ${ui.formato.numero(c.espacioGb, 0)} GB · ${ui.formato.numero(porcentaje, 1)}%</span><div class="fr-meter ${porcentaje < 85 ? "is-ok" : "is-alert"}" style="margin-top:6px"><span style="width:${porcentaje}%"></span></div></div>`;
}

async function cargar(buscar = null) {
    try {
        datos = await ReporteService.consumoClientes({ buscar });
        const filas = datos.detalle || [];
        raiz.querySelector("[data-stats]").innerHTML = [
            ui.tarjetaDato({ titulo: "Almacenamiento total", valor: ui.formato.bytes(datos.bytes), iconoNombre: "hard", tono: "is-accent" }),
            ui.tarjetaDato({ titulo: "Archivos", valor: ui.formato.numero(datos.archivos), iconoNombre: "file" }),
            ui.tarjetaDato({ titulo: "Clientes", valor: ui.formato.numero(datos.clientes), iconoNombre: "building-2" })
        ].join("");
        const top = filas.filter(c => Number(c.bytes) > 0).slice(0, 10);
        raiz.querySelector("[data-top]").innerHTML = top.length
            ? barras({ items: top.map(c => ({ etiqueta: c.clienteNombre, valor: Number(c.bytes) })), formato: v => ui.formato.bytes(v) })
            : `<div class="fr-empty">${ui.icono("hard")}<p>Aún no hay archivos almacenados.</p></div>`;
        raiz.querySelector("[data-detalle]").innerHTML = ui.tabla({
            filas,
            vacio: "No hay clientes para mostrar.",
            columnas: [
                { titulo: "Cliente", valor: c => `<span class="fr-cell-main" data-i18n-skip>${ui.escapar(c.clienteNombre)}</span>${ui.estado(c.estado)}` },
                { titulo: "Plan", valor: c => (c.planNombre ? `<span data-i18n-skip>${ui.escapar(c.planNombre)}</span>` : "—") },
                { titulo: "Uso", valor: uso },
                { titulo: "Archivos", clase: "is-right", valor: c => ui.formato.numero(c.archivos) },
                { titulo: "Almacenes", clase: "is-right", valor: c => ui.formato.numero(c.almacenes) },
                { titulo: "API 30 días", clase: "is-right", valor: c => ui.formato.numero(c.solicitudes30) }
            ]
        });
    } catch (error) {
        ui.notificarError(error);
    }
}

export async function bind(stage, api) {
    ui = api;
    raiz = stage;
    raiz.querySelector("[data-buscar]").addEventListener("input", event => {
        window.clearTimeout(temporizador);
        temporizador = window.setTimeout(() => cargar(event.target.value.trim() || null), 350);
    });
    raiz.querySelector("[data-exportar]").addEventListener("click", () => {
        if (datos) exportarCsv("consumo_por_cliente", COLUMNAS, datos.detalle || []);
    });
    await cargar();
}

export function reset() {
    window.clearTimeout(temporizador);
    datos = null;
}

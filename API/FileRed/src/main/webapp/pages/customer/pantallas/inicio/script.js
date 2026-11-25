import { area, activarTooltips, fechaCorta } from "../../../comunes/graficos.js?v=20261120-7";

let ui;

export function render() {
    return `
        <section class="fr-panel consumo-hero" data-consumo></section>
        <div class="fr-stats" data-stats></div>
        <div class="fr-bento">
            <section class="fr-panel span-8">
                <header class="fr-panel-head"><div><h2>Actividad de la API</h2><p>Solicitudes por día en los últimos 30 días</p></div></header>
                <div class="fr-panel-body" data-actividad></div>
            </section>
            <section class="fr-panel span-4">
                <header class="fr-panel-head"><div><h2>Primeros pasos</h2><p>Configura tu cuenta en minutos</p></div></header>
                <div class="fr-panel-body"><ol class="primeros-pasos" data-pasos></ol></div>
            </section>
        </div>`;
}

function dias(cantidad) {
    const lista = [];
    const hoy = new Date();
    for (let i = cantidad - 1; i >= 0; i--) {
        const fecha = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate() - i);
        lista.push(`${fecha.getFullYear()}-${String(fecha.getMonth() + 1).padStart(2, "0")}-${String(fecha.getDate()).padStart(2, "0")}`);
    }
    return lista;
}

export async function bind(stage, api) {
    ui = api;
    try {
        const r = await ResumenService.cliente();
        const vigente = r.vigente;
        const usado = Number(r.espacioUsadoBytes || 0);
        const limite = Number(r.espacioLimiteBytes || 0);
        const porcentaje = limite ? Math.min(100, (usado / limite) * 100) : 0;
        stage.querySelector("[data-consumo]").innerHTML = vigente
            ? `
                <div class="consumo-hero-copy">
                    <p class="consumo-eyebrow">Plan ${ui.escapar(vigente.planNombre)} · ${ui.escapar(vigente.tipoCobro)}</p>
                    <h2>${ui.formato.bytes(usado)} <span>${r.espacioIlimitado ? "usados · espacio ilimitado" : `de ${ui.formato.bytes(limite)}`}</span></h2>
                    ${r.espacioIlimitado ? "" : `<div class="fr-meter ${porcentaje < 85 ? "" : "is-alert"}"><span style="width:${porcentaje}%"></span></div>`}
                    <p class="consumo-meta">${r.espacioIlimitado ? "Sin límite de espacio" : `${ui.formato.numero(porcentaje, 1)}% usado`} · ${vigente.tamanoMaximoArchivoMb == null ? "Sin límite por archivo" : `Archivos de hasta ${ui.formato.numero(vigente.tamanoMaximoArchivoMb)} MB`} · Vence ${ui.formato.fecha(vigente.fechaFin)}${vigente.permitirAdicionales ? " · GB adicionales activos" : ""}</p>
                </div>
                ${r.espacioIlimitado ? '<div class="consumo-hero-ring" style="--porcentaje:100"><strong>∞</strong><span>ilimitado</span></div>' : `<div class="consumo-hero-ring" style="--porcentaje:${porcentaje}"><strong>${ui.formato.numero(porcentaje, 0)}%</strong><span>usado</span></div>`}`
            : `
                <div class="consumo-hero-copy">
                    <p class="consumo-eyebrow">Sin plan vigente</p>
                    <h2>Elige tu plan para empezar</h2>
                    <p class="consumo-meta">Necesitas un plan vigente para crear carpetas y subir archivos.</p>
                    ${ui.puede("suscripcion", "solicitar") ? '<a class="fr-btn fr-btn-primary" href="#/suscripcion">Ver planes</a>' : ""}
                </div>`;

        stage.querySelector("[data-stats]").innerHTML = [
            ui.tarjetaDato({ titulo: "Archivos", valor: ui.formato.numero(r.archivos), iconoNombre: "file", tono: "is-accent" }),
            ui.tarjetaDato({ titulo: "API keys activas", valor: ui.formato.numero(r.apiKeysActivas), iconoNombre: "key-round" }),
            ui.tarjetaDato({ titulo: "Solicitudes API (30 días)", valor: ui.formato.numero(r.solicitudesMes), iconoNombre: "activity" }),
            ui.tarjetaDato({ titulo: "Descargas (30 días)", valor: ui.formato.numero(r.descargasMes), iconoNombre: "download" }),
            ui.tarjetaDato({
                titulo: "Saldo pendiente",
                valor: ui.formato.moneda(r.montoPendiente, vigente?.monedaSimbolo || "US$"),
                iconoNombre: "receipt"
            })
        ].join("");

        const metricas = new Map((r.metricas || []).map(m => [String(m.fecha).slice(0, 10), m]));
        const lista = dias(30);
        stage.querySelector("[data-actividad]").innerHTML = `${area({
            puntos: lista.map(d => ({ etiqueta: d, valor: Number(metricas.get(d)?.solicitudesApi || 0), secundario: Number(metricas.get(d)?.descargas || 0) })),
            etiqueta: p => fechaCorta(p.etiqueta),
            formato: v => `${ui.formato.numero(v)} solicitudes`,
            secundaria: { nombre: "Descargas", formato: v => ui.formato.numero(v) }
        })}<div class="fr-chart-legend"><span><i style="background:var(--accent-strong)"></i>Solicitudes API</span><span><i style="background:var(--cyan)"></i>Descargas</span></div>`;
        activarTooltips(stage);

        const pasos = [
            { hecho: Boolean(vigente), texto: "Ten un plan vigente", enlace: "#/suscripcion", modulo: "suscripcion" },
            { hecho: Number(r.apiKeysActivas) > 0, texto: "Crea tu primera API key", enlace: "#/apikeys", modulo: "apikeys" },
            { hecho: Number(r.archivos) > 0, texto: "Sube tu primer archivo", enlace: "#/archivos", modulo: "archivos" },
            { hecho: Number(r.usuarios) > 1, texto: "Invita a tu equipo", enlace: "#/usuarios", modulo: "usuarios" }
        ];
        stage.querySelector("[data-pasos]").innerHTML = pasos
            .map(
                p =>
                    `<li class="${p.hecho ? "is-done" : ""}"><span class="paso-check">${ui.icono(p.hecho ? "check" : "chevron-right")}</span><a href="${p.enlace}">${ui.escapar(p.texto)}</a></li>`
            )
            .join("");
    } catch (error) {
        ui.notificarError(error);
    }
}

export function reset() {
    ui = null;
}

export function skeleton() {
    return window.FileRedSk.tablero({ tarjetas: 4, columnas: 4 });
}

import { area, activarTooltips, dona } from "../../../comunes/graficos.js?v=20261120-7";

let ui;

export function render() {
    return `
        <div class="fr-stats" data-stats></div>
        <div class="fr-bento">
            <section class="fr-panel span-8">
                <header class="fr-panel-head"><div><h2>Ingresos</h2><p>Pagos y abonos de los últimos 6 meses</p></div><a class="fr-btn fr-btn-sm" href="#/reporte_ingresos" data-link-reporte hidden>Ver reporte</a></header>
                <div class="fr-panel-body" data-ingresos></div>
            </section>
            <section class="fr-panel span-4">
                <header class="fr-panel-head"><div><h2>Planes vigentes</h2><p>Clientes por plan</p></div></header>
                <div class="fr-panel-body" data-planes></div>
            </section>
        </div>
        <section class="fr-panel">
            <header class="fr-panel-head"><div><h2>Últimos pagos</h2><p>Abonos registrados recientemente</p></div><a class="fr-btn fr-btn-sm" href="#/pagos" data-link-pagos hidden>Ir a pagos</a></header>
            <div data-pagos></div>
        </section>`;
}

function ultimosMeses(cantidad) {
    const meses = [];
    const hoy = new Date();
    for (let i = cantidad - 1; i >= 0; i--) {
        const fecha = new Date(hoy.getFullYear(), hoy.getMonth() - i, 1);
        meses.push(`${fecha.getFullYear()}-${String(fecha.getMonth() + 1).padStart(2, "0")}`);
    }
    return meses;
}

function mesCorto(clave) {
    const [anio, mes] = String(clave).split("-").map(Number);
    return new Date(anio, mes - 1, 1).toLocaleDateString("es-DO", { month: "short", year: "2-digit" });
}

export async function bind(stage, api) {
    ui = api;
    stage.querySelector("[data-link-reporte]").hidden = !ui.puede("reporte_ingresos", "consultar");
    stage.querySelector("[data-link-pagos]").hidden = !ui.puede("pagos", "listar");
    try {
        const r = await ResumenService.administracion();
        stage.querySelector("[data-stats]").innerHTML = [
            ui.tarjetaDato({ titulo: "Clientes activos", valor: ui.formato.numero(r.clientesActivos), detalle: `${ui.formato.numero(r.clientesSuspendidos)} suspendidos`, iconoNombre: "building-2", tono: "is-accent" }),
            ui.tarjetaDato({ titulo: "Suscripciones vigentes", valor: ui.formato.numero(r.suscripcionesVigentes), detalle: `${ui.formato.numero(r.solicitudesPendientes)} solicitudes pendientes`, iconoNombre: "calendar-check" }),
            ui.tarjetaDato({ titulo: "Por cobrar", valor: ui.formato.moneda(r.montoPendiente), detalle: `${ui.formato.numero(r.cargosPendientes)} cargos pendientes`, iconoNombre: "wallet" }),
            ui.tarjetaDato({ titulo: "Ingresos del mes", valor: ui.formato.moneda(r.ingresosMes), iconoNombre: "dollar" }),
            ui.tarjetaDato({ titulo: "Almacenamiento usado", valor: ui.formato.bytes(r.almacenamientoBytes), detalle: `${ui.formato.numero(r.archivos)} archivos`, iconoNombre: "hard" }),
            ui.tarjetaDato({ titulo: "Solicitudes API hoy", valor: ui.formato.numero(r.solicitudesHoy), iconoNombre: "activity" })
        ].join("");

        const ingresos = new Map((r.ingresosMensuales || []).map(i => [i.mes, Number(i.monto || 0)]));
        stage.querySelector("[data-ingresos]").innerHTML = area({
            puntos: ultimosMeses(6).map(m => ({ etiqueta: m, valor: ingresos.get(m) || 0 })),
            etiqueta: p => mesCorto(p.etiqueta),
            formato: v => ui.formato.moneda(v)
        });

        const planes = (r.planes || []).filter(p => Number(p.clientes) > 0);
        stage.querySelector("[data-planes]").innerHTML = planes.length
            ? dona({
                  items: planes.map(p => ({ etiqueta: p.nombre, valor: Number(p.clientes) })),
                  formato: v => ui.formato.numero(v),
                  centro: ui.formato.numero(planes.reduce((t, p) => t + Number(p.clientes), 0))
              })
            : `<div class="fr-empty">${ui.icono("layers")}<p>Aún no hay clientes con plan vigente.</p></div>`;

        stage.querySelector("[data-pagos]").innerHTML = ui.tabla({
            filas: r.ultimosPagos || [],
            vacio: "Aún no hay pagos registrados.",
            columnas: [
                { titulo: "Cliente", valor: p => `<span class="fr-cell-main" data-i18n-skip>${ui.escapar(p.clienteNombre)}</span>` },
                { titulo: "Método", valor: p => ui.escapar(p.metodo) },
                { titulo: "Fecha", valor: p => ui.formato.fechaHora(p.fechaPago) },
                { titulo: "Monto", clase: "is-right", valor: p => `<strong>${ui.formato.moneda(p.monto, p.monedaSimbolo)}</strong>` }
            ]
        });
        activarTooltips(stage);
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

let ui;
let raiz;
let planes = [];
let solicitudes = [];

export function render() {
    return `
        <section class="fr-panel" data-vigente></section>
        <section class="fr-panel">
            <header class="fr-panel-head"><div><h2>Planes disponibles</h2><p>Solicita el plan que necesitas. Nuestro equipo lo activará y generará el cargo.</p></div></header>
            <div class="fr-panel-body"><div class="plan-choices" data-planes></div></div>
        </section>
        <div class="fr-grid-2">
            <section class="fr-panel">
                <header class="fr-panel-head"><div><h2>Mis solicitudes</h2></div></header>
                <div data-solicitudes></div>
            </section>
            <section class="fr-panel">
                <header class="fr-panel-head"><div><h2>Historial de planes</h2></div></header>
                <div data-historial></div>
            </section>
        </div>`;
}

function pintarVigente(datos) {
    const vigente = datos?.vigente;
    const usado = Number(datos?.espacioUsadoBytes || 0);
    const limite = Number(datos?.espacioLimiteBytes || 0);
    const porcentaje = limite ? Math.min(100, (usado / limite) * 100) : 0;
    raiz.querySelector("[data-vigente]").innerHTML = vigente
        ? `
            <header class="fr-panel-head"><div><h2>Plan ${ui.escapar(vigente.planNombre)}</h2><p>${vigente.tipoCobro === "POSTPAGO" ? "Pospago con renovación automática" : "Prepago"}${vigente.permitirAdicionales ? " · GB adicionales activos" : ""}</p></div>${ui.estado(vigente.estado)}</header>
            <div class="fr-panel-body">
                <dl class="fr-kv">
                    <div><dt>Espacio</dt><dd>${datos.espacioIlimitado ? `${ui.formato.bytes(usado)} · Ilimitado` : `${ui.formato.bytes(usado)} de ${ui.formato.bytes(limite)}`}</dd></div>
                    <div><dt>Tamaño máximo por archivo</dt><dd>${vigente.tamanoMaximoArchivoMb == null ? "Sin límite" : `${ui.formato.numero(vigente.tamanoMaximoArchivoMb)} MB`}</dd></div>
                    <div><dt>Periodo</dt><dd>${ui.formato.fecha(vigente.fechaInicio)} – ${ui.formato.fecha(vigente.fechaFin)}</dd></div>
                    <div><dt>Precio</dt><dd>${ui.formato.moneda(vigente.precio, vigente.monedaSimbolo)}${Number(vigente.precioGbAdicional) > 0 ? ` · GB adicional ${ui.formato.moneda(vigente.precioGbAdicional, vigente.monedaSimbolo)}` : ""}</dd></div>
                </dl>
                ${datos.espacioIlimitado ? "" : `<div class="fr-meter ${porcentaje < 85 ? "" : "is-alert"}" style="margin-top:18px"><span style="width:${porcentaje}%"></span></div>`}
            </div>`
        : `<div class="fr-empty">${ui.icono("gauge")}<p>No tienes un plan vigente. Elige uno de los planes disponibles para empezar a almacenar archivos.</p></div>`;
}

function pintarPlanes(vigente) {
    const pendiente = solicitudes.some(s => s.estado === "Pendiente");
    const puedeSolicitar = ui.puede("suscripcion", "solicitar");
    let sugerido = null;
    try {
        sugerido = sessionStorage.getItem("filered.planElegido");
    } catch (_) {
        sugerido = null;
    }
    raiz.querySelector("[data-planes]").innerHTML = planes.length
        ? planes
              .map(
                  p => `
            <article class="plan-choice ${String(p.planId) === sugerido ? "is-suggested" : ""} ${p.destacado ? "is-featured" : ""}">
                <header><h3>${ui.escapar(p.nombre)}</h3>${vigente?.planId === p.planId ? '<span class="fr-badge is-success">Actual</span>' : ""}</header>
                <p class="plan-choice-price">${ui.formato.moneda(p.precio, p.monedaSimbolo)} <span>/ ${ui.escapar(p.modalidad)}</span></p>
                <ul>
                    <li>${p.espacioGb == null ? "Espacio ilimitado" : `${ui.formato.numero(p.espacioGb, 0)} GB de espacio`}</li>
                    <li>${p.tamanoMaximoArchivoMb == null ? "Sin límite por archivo" : `Archivos de hasta ${ui.formato.numero(p.tamanoMaximoArchivoMb)} MB`}</li>
                    ${p.espacioGb != null && Number(p.precioGbAdicional) > 0 ? `<li>GB adicional ${ui.formato.moneda(p.precioGbAdicional, p.monedaSimbolo)}</li>` : ""}
                </ul>
                ${puedeSolicitar ? `<button class="fr-btn ${p.destacado ? "fr-btn-primary" : ""}" type="button" data-solicitar="${p.planId}" ${pendiente ? "disabled" : ""}>Solicitar</button>` : ""}
            </article>`
              )
              .join("")
        : `<div class="fr-empty">${ui.icono("layers")}<p>No hay planes publicados por el momento.</p></div>`;
    if (pendiente && puedeSolicitar) {
        raiz.querySelector("[data-planes]").insertAdjacentHTML("beforebegin", '<p class="fr-note" data-aviso-pendiente>Tienes una solicitud pendiente. Podrás solicitar otro plan cuando sea respondida o si la cancelas.</p>');
    }
}

function pintarSolicitudes() {
    raiz.querySelector("[data-solicitudes]").innerHTML = ui.tabla({
        filas: solicitudes,
        clave: "suscripcionSolicitudId",
        vacio: "No has enviado solicitudes.",
        columnas: [
            { titulo: "Plan", valor: s => `<span class="fr-cell-main">${ui.escapar(s.planNombre)}</span><span class="fr-cell-sub">${ui.escapar(s.tipoCobro)} · ${ui.formato.fecha(s.fechaCreacion)}</span>` },
            { titulo: "Estado", valor: s => `${ui.estado(s.estado)}${s.respuesta ? `<span class="fr-cell-sub">${ui.escapar(s.respuesta)}</span>` : ""}` },
            {
                titulo: "Acciones",
                clase: "is-actions",
                valor: s => (s.estado === "Pendiente" && ui.puede("suscripcion", "solicitar") ? ui.botonAccion("cancelar", "x", "Cancelar solicitud", "is-danger") : "")
            }
        ]
    });
}

async function cargar() {
    try {
        raiz.querySelector("[data-aviso-pendiente]")?.remove();
        const [datos, publicos, respuesta] = await Promise.all([
            SuscripcionService.obtener(),
            PlanService.publicos(),
            SuscripcionSolicitudService.paginar({ page: 1, size: 10 })
        ]);
        planes = publicos || [];
        solicitudes = respuesta?.records || [];
        pintarVigente(datos);
        pintarPlanes(datos?.vigente);
        pintarSolicitudes();
        raiz.querySelector("[data-historial]").innerHTML = ui.tabla({
            filas: datos?.historial || [],
            vacio: "Sin historial.",
            columnas: [
                { titulo: "Plan", valor: d => `<span class="fr-cell-main">${ui.escapar(d.planNombre)}</span><span class="fr-cell-sub">${ui.escapar(d.tipoCobro)}</span>` },
                { titulo: "Periodo", valor: d => `${ui.formato.fecha(d.fechaInicio)} – ${ui.formato.fecha(d.fechaFin)}` },
                { titulo: "Estado", valor: d => ui.estado(d.estado) }
            ]
        });
    } catch (error) {
        ui.notificarError(error);
    }
}

function solicitar(plan) {
    ui.formulario({
        titulo: `Solicitar plan ${plan.nombre}`,
        subtitulo: `${ui.formato.moneda(plan.precio, plan.monedaSimbolo)} / ${plan.modalidad}`,
        ancho: "sm",
        textoGuardar: "Enviar solicitud",
        campos: `
            ${ui.campo({
                nombre: "tipoCobro",
                etiqueta: "Modalidad de pago",
                tipo: "select",
                valor: "PREPAGO",
                clase: "is-full",
                opciones: [
                    { valor: "PREPAGO", texto: "Prepago: pago el periodo por adelantado" },
                    { valor: "POSTPAGO", texto: "Pospago: se renueva automáticamente" }
                ]
            })}
            ${ui.campo({ nombre: "comentario", etiqueta: "Comentario (opcional)", tipo: "textarea", max: 500, clase: "is-full" })}`,
        async alGuardar(form) {
            const datos = ui.valores(form);
            await SuscripcionSolicitudService.solicitar({ planId: plan.planId, tipoCobro: datos.tipoCobro, comentario: datos.comentario });
            try {
                sessionStorage.removeItem("filered.planElegido");
            } catch (_) {
                datos.limpio = true;
            }
            AppNotifications.success("Solicitud enviada correctamente");
            await cargar();
        }
    });
}

async function cancelar(solicitud) {
    const ok = await ui.confirmar({
        titulo: "Cancelar solicitud",
        mensaje: `Se cancelará tu solicitud del plan ${solicitud.planNombre}.`,
        textoConfirmar: "Cancelar solicitud",
        peligro: true
    });
    if (!ok) return;
    try {
        await SuscripcionSolicitudService.cancelar(solicitud.suscripcionSolicitudId);
        AppNotifications.success("Solicitud cancelada correctamente");
        await cargar();
    } catch (error) {
        ui.notificarError(error);
    }
}

export async function bind(stage, api) {
    ui = api;
    raiz = stage;
    raiz.querySelector("[data-planes]").addEventListener("click", event => {
        const boton = event.target.closest("[data-solicitar]");
        if (!boton) return;
        const plan = planes.find(p => String(p.planId) === boton.dataset.solicitar);
        if (plan) solicitar(plan);
    });
    raiz.querySelector("[data-solicitudes]").addEventListener("click", event => {
        const boton = event.target.closest("[data-action='cancelar']");
        if (!boton) return;
        const solicitud = solicitudes.find(s => String(s.suscripcionSolicitudId) === boton.closest("[data-row]")?.dataset.row);
        if (solicitud) cancelar(solicitud);
    });
    await cargar();
}

export function reset() {
    planes = [];
    solicitudes = [];
}

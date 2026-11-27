let ui;
let raiz;
let planes = [];
let monedas = [];

const MODALIDADES = ["Mensual", "Bimestral", "Trimestral", "Semestral", "Anual"].map(m => ({ valor: m, texto: m }));

export function render() {
    return `
        <div class="fr-toolbar">
            <p class="fr-toolbar-text">Los planes públicos se muestran en el portal y los clientes pueden solicitarlos.</p>
            <span class="fr-toolbar-spacer"></span>
            <button class="fr-btn fr-btn-primary" type="button" data-nuevo hidden>${FileRedPanel.icono("plus")}<span>Nuevo plan</span></button>
        </div>
        <section class="fr-panel"><div data-lista></div></section>`;
}

async function cargar() {
    try {
        [planes, monedas] = await Promise.all([PlanService.listar(), MonedaService.listar()]);
        const modificar = ui.puede("planes", "modificar");
        const eliminar = ui.puede("planes", "cambiar_estado");
        raiz.querySelector("[data-lista]").innerHTML = ui.tabla({
            filas: planes || [],
            clave: "planId",
            vacio: "No hay planes registrados.",
            columnas: [
                {
                    titulo: "Plan",
                    valor: p => `<span class="fr-cell-main">${ui.escapar(p.nombre)}${p.destacado ? ' <span class="fr-badge is-danger is-plain">Destacado</span>' : ""}</span><span class="fr-cell-sub">${ui.escapar(p.modalidad)}</span>`
                },
                { titulo: "Espacio", valor: p => (p.espacioGb == null ? '<span class="fr-badge is-info is-plain">Ilimitado</span>' : `${ui.formato.numero(p.espacioGb, 0)} GB`) },
                { titulo: "Por archivo", valor: p => (p.tamanoMaximoArchivoMb == null ? '<span class="fr-badge is-info is-plain">Sin límite</span>' : `${ui.formato.numero(p.tamanoMaximoArchivoMb)} MB`) },
                { titulo: "Precio", clase: "is-right", valor: p => `<strong>${ui.formato.moneda(p.precio, p.monedaSimbolo)}</strong>` },
                { titulo: "GB adicional", clase: "is-right", valor: p => (p.espacioGb == null ? "—" : ui.formato.moneda(p.precioGbAdicional, p.monedaSimbolo)) },
                {
                    titulo: "Funciones",
                    valor: p => [p.permitirNodos ? '<span class="fr-badge is-info is-plain">Nodos</span>' : "", p.permitirNube !== false ? '<span class="fr-badge is-info is-plain">Nube</span>' : "", p.permitirRespaldos ? '<span class="fr-badge is-info is-plain">Respaldos</span>' : ""].filter(Boolean).join(" ")
                },
                { titulo: "Portal", valor: p => (p.publico ? '<span class="fr-badge is-success">Visible</span>' : '<span class="fr-badge">Oculto</span>') },
                { titulo: "Estado", valor: p => ui.estado(p.estado) },
                {
                    titulo: "Acciones",
                    clase: "is-actions",
                    valor: () => (modificar ? ui.botonAccion("editar", "pencil", "Editar") : "") + (eliminar ? ui.botonAccion("eliminar", "trash", "Eliminar", "is-danger") : "")
                }
            ]
        });
    } catch (error) {
        ui.notificarError(error);
    }
}

function formulario(plan) {
    const nuevo = !plan;
    const opcionesMoneda = (monedas || []).map(m => ({ valor: m.monedaId, texto: `${m.codigo} · ${m.nombre}` }));
    ui.formulario({
        titulo: nuevo ? "Nuevo plan" : "Editar plan",
        subtitulo: nuevo ? "" : plan.nombre,
        ancho: "lg",
        campos: `
            ${ui.campo({ nombre: "nombre", etiqueta: "Nombre", valor: plan?.nombre, requerido: true, max: 80 })}
            ${ui.campo({ nombre: "modalidad", etiqueta: "Modalidad", tipo: "select", valor: plan?.modalidad || "Mensual", opciones: MODALIDADES })}
            ${ui.campo({ nombre: "descripcion", etiqueta: "Descripción", tipo: "textarea", valor: plan?.descripcion, max: 300, clase: "is-full" })}
            ${ui.campo({ nombre: "monedaId", etiqueta: "Moneda", tipo: "select", valor: plan?.monedaId, opciones: opcionesMoneda })}
            ${ui.campo({ nombre: "precio", etiqueta: "Precio por periodo", tipo: "number", valor: plan?.precio ?? "", requerido: true, atributos: 'min="0" step="0.01"' })}
            <div class="fr-field">
                ${ui.campo({ nombre: "espacioGb", etiqueta: "Espacio (GB)", tipo: "number", valor: plan?.espacioGb ?? "", atributos: 'min="0.01" step="0.01"' })}
                ${ui.campo({ nombre: "espacioIlimitado", etiqueta: "Espacio ilimitado", tipo: "checkbox", valor: !nuevo && plan.espacioGb == null })}
            </div>
            <div class="fr-field">
                ${ui.campo({ nombre: "tamanoMaximoArchivoMb", etiqueta: "Tamaño máximo por archivo (MB)", tipo: "number", valor: plan?.tamanoMaximoArchivoMb ?? "", atributos: 'min="1" step="1"' })}
                ${ui.campo({ nombre: "archivoIlimitado", etiqueta: "Sin límite por archivo", tipo: "checkbox", valor: !nuevo && plan.tamanoMaximoArchivoMb == null, ayuda: "" })}
                <small>Siempre aplica el tope global del servidor.</small>
            </div>
            ${ui.campo({ nombre: "precioGbAdicional", etiqueta: "Precio por GB adicional", tipo: "number", valor: plan?.precioGbAdicional ?? 0, ayuda: "Solo aplica a pospago con adicionales.", atributos: 'min="0" step="0.01"' })}
            ${ui.campo({ nombre: "orden", etiqueta: "Orden en el portal", tipo: "number", valor: plan?.orden ?? 0, atributos: 'min="0" step="1"' })}
            ${ui.campo({ nombre: "caracteristicas", etiqueta: "Características", tipo: "textarea", valor: (plan?.caracteristicas || "").split("|").join("\n"), clase: "is-full", ayuda: "Una característica por línea." })}
            <div class="fr-field is-full fr-checks-inline">
                ${ui.campo({ nombre: "publico", etiqueta: "Visible en el portal", tipo: "checkbox", valor: nuevo ? true : plan.publico })}
                ${ui.campo({ nombre: "destacado", etiqueta: "Destacado", tipo: "checkbox", valor: plan?.destacado })}
            </div>
            <div class="fr-field is-full">
                <span>Funciones incluidas</span>
                <div class="fr-checks-inline">
                    ${ui.campo({ nombre: "permitirNodos", etiqueta: "Nodos: guardar archivos en equipos propios", tipo: "checkbox", valor: plan?.permitirNodos })}
                    ${ui.campo({ nombre: "permitirNube", etiqueta: "Almacenamiento en la Nube", tipo: "checkbox", valor: nuevo ? true : plan.permitirNube !== false })}
                    ${ui.campo({ nombre: "permitirRespaldos", etiqueta: "Servidores de respaldo", tipo: "checkbox", valor: plan?.permitirRespaldos })}
                </div>
                <small data-ayuda-funciones>El plan siempre debe tener dónde guardar: si no incluye la Nube, incluye Nodos obligatoriamente.</small>
            </div>
            ${nuevo ? "" : ui.campo({ nombre: "estado", etiqueta: "Estado", tipo: "select", valor: plan.estado, opciones: [{ valor: "Activo", texto: "Activo" }, { valor: "Inactivo", texto: "Inactivo" }] })}`,
        alAbrir(form) {
            const enlazar = (casilla, campo, extra) => {
                const aplicar = () => {
                    const ilimitado = form.elements[casilla].checked;
                    form.elements[campo].disabled = ilimitado;
                    if (ilimitado) form.elements[campo].value = "";
                    if (extra) {
                        form.elements[extra].disabled = ilimitado;
                        if (ilimitado) form.elements[extra].value = "0";
                    }
                };
                form.elements[casilla].addEventListener("change", aplicar);
                aplicar();
            };
            enlazar("espacioIlimitado", "espacioGb", "precioGbAdicional");
            const nubeCasilla = form.elements.permitirNube;
            const nodosCasilla = form.elements.permitirNodos;
            const exigirDestino = () => {
                if (!nubeCasilla.checked) nodosCasilla.checked = true;
                nodosCasilla.disabled = !nubeCasilla.checked;
                nodosCasilla.closest(".fr-check")?.classList.toggle("is-locked", !nubeCasilla.checked);
            };
            nubeCasilla.addEventListener("change", exigirDestino);
            exigirDestino();
            enlazar("archivoIlimitado", "tamanoMaximoArchivoMb");
        },
        async alGuardar(form) {
            const espacioIlimitado = form.elements.espacioIlimitado.checked;
            const archivoIlimitado = form.elements.archivoIlimitado.checked;
            ui.exigir(form, [
                ["nombre", "El nombre es obligatorio."],
                ["monedaId", "Selecciona una moneda."],
                ["precio", "El precio debe ser mayor o igual a cero.", valor => valor !== "" && Number(valor) >= 0],
                ["espacioGb", "El espacio debe ser mayor que cero.", valor => espacioIlimitado || Number(valor) > 0],
                ["tamanoMaximoArchivoMb", "El tamaño máximo debe ser un entero mayor que cero.", valor => archivoIlimitado || (/^\d+$/.test(valor) && Number(valor) > 0)],
                ["precioGbAdicional", "El precio por GB adicional no puede ser negativo.", valor => !valor || Number(valor) >= 0]
            ]);
            const datos = ui.valores(form);
            datos.permitirNube = form.elements.permitirNube.checked;
            datos.permitirNodos = form.elements.permitirNodos.checked || !datos.permitirNube;
            datos.monedaId = Number(datos.monedaId);
            datos.espacioGb = espacioIlimitado ? null : datos.espacioGb;
            datos.tamanoMaximoArchivoMb = archivoIlimitado ? null : datos.tamanoMaximoArchivoMb;
            datos.precioGbAdicional = espacioIlimitado ? 0 : datos.precioGbAdicional ?? 0;
            delete datos.espacioIlimitado;
            delete datos.archivoIlimitado;
            datos.caracteristicas = String(datos.caracteristicas || "")
                .split(/\r?\n/)
                .map(c => c.trim().replace(/\|/g, "/"))
                .filter(Boolean)
                .join("|");
            if (nuevo) await PlanService.agregar(datos);
            else await PlanService.actualizar({ ...datos, planUuid: plan.planUuid });
            AppNotifications.success(nuevo ? "Plan creado correctamente" : "Plan actualizado correctamente");
            await cargar();
        }
    });
}

async function eliminar(plan) {
    const ok = await ui.confirmar({
        titulo: "Eliminar plan",
        mensaje: `El plan ${plan.nombre} dejará de estar disponible. Las suscripciones vigentes no se modifican.`,
        textoConfirmar: "Eliminar",
        peligro: true
    });
    if (!ok) return;
    try {
        await PlanService.eliminar(plan.planId);
        AppNotifications.success("Plan eliminado correctamente");
        await cargar();
    } catch (error) {
        ui.notificarError(error);
    }
}

export async function bind(stage, api) {
    ui = api;
    raiz = stage;
    const nuevo = raiz.querySelector("[data-nuevo]");
    nuevo.hidden = !ui.puede("planes", "crear");
    nuevo.addEventListener("click", () => formulario(null));
    raiz.querySelector("[data-lista]").addEventListener("click", event => {
        const boton = event.target.closest("[data-action]");
        if (!boton) return;
        const plan = planes.find(p => String(p.planId) === boton.closest("[data-row]")?.dataset.row);
        if (!plan) return;
        if (boton.dataset.action === "editar") formulario(plan);
        if (boton.dataset.action === "eliminar") eliminar(plan);
    });
    await cargar();
}

export function reset() {
    planes = [];
}

export function skeleton() {
    return window.FileRedSk.lista({ filtros: 1, columnas: 5 });
}

(() => {
    const ICONOS = {
        "layout-dashboard": '<rect x="3" y="3" width="7" height="9" rx="1.5"/><rect x="14" y="3" width="7" height="5" rx="1.5"/><rect x="14" y="12" width="7" height="9" rx="1.5"/><rect x="3" y="16" width="7" height="5" rx="1.5"/>',
        "building-2": '<path d="M6 22V4a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v18Z"/><path d="M6 12H4a2 2 0 0 0-2 2v8h4M18 9h2a2 2 0 0 1 2 2v11h-4M10 6h4M10 10h4M10 14h4M10 18h4"/>',
        "calendar-check": '<rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18m-12 6 2 2 4-4"/>',
        wallet: '<path d="M19 7V4a1 1 0 0 0-1-1H5a2 2 0 0 0 0 4h15a1 1 0 0 1 1 1v4h-3a2 2 0 0 0 0 4h3a1 1 0 0 0 1-1v-2a1 1 0 0 0-1-1"/><path d="M3 5v14a2 2 0 0 0 2 2h15a1 1 0 0 0 1-1v-4"/>',
        layers: '<path d="m12 2 10 5-10 5L2 7Z"/><path d="m2 17 10 5 10-5M2 12l10 5 10-5"/>',
        coins: '<circle cx="8" cy="8" r="6"/><path d="M18.1 10.4A6 6 0 1 1 10.3 18M7 6h1v4m8.7 3.9.7.7-2.8 2.8"/>',
        users: '<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.9M16 3.1a4 4 0 0 1 0 7.8"/>',
        "shield-check": '<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10Z"/><path d="m9 12 2 2 4-4"/>',
        "folder-open": '<path d="m6 14 1.5-2.9A2 2 0 0 1 9.2 10H20a2 2 0 0 1 1.9 2.5l-1.5 6A2 2 0 0 1 18.5 20H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h3.9a2 2 0 0 1 1.7.9l.8 1.2a2 2 0 0 0 1.7.9H18a2 2 0 0 1 2 2v2"/>',
        "key-round": '<path d="M2.6 18.2A2 2 0 0 0 2 19.6V21a1 1 0 0 0 1 1h3a1 1 0 0 0 1-1v-1a1 1 0 0 1 1-1h1a1 1 0 0 0 1-1v-1a1 1 0 0 1 1-1h.2a2 2 0 0 0 1.4-.6l.8-.8a6.5 6.5 0 1 0-4-4Z"/><circle cx="16.5" cy="7.5" r=".5" fill="currentColor"/>',
        gauge: '<path d="m12 14 4-4M3.3 19a10 10 0 1 1 17.4 0"/>',
        receipt: '<path d="M4 2v20l2-1 2 1 2-1 2 1 2-1 2 1 2-1 2 1V2l-2 1-2-1-2 1-2-1-2 1-2-1-2 1Z"/><path d="M16 8h-6a2 2 0 1 0 0 4h4a2 2 0 1 1 0 4H8M12 17.5v-11"/>',
        "id-card": '<path d="M16 10h2M16 14h2"/><path d="M6.2 15a3 3 0 0 1 5.6 0"/><circle cx="9" cy="11" r="2"/><rect x="2" y="5" width="20" height="14" rx="2"/>',
        plus: '<path d="M12 5v14M5 12h14"/>',
        pencil: '<path d="M21.2 6.8a2.8 2.8 0 0 0-4-4L3.8 16.2a2 2 0 0 0-.5.8l-1.3 4.4a.5.5 0 0 0 .6.6l4.4-1.3a2 2 0 0 0 .8-.5Z"/>',
        trash: '<path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2M10 11v6M14 11v6"/>',
        refresh: '<path d="M3 12a9 9 0 0 1 15-6.7L21 8M21 3v5h-5M21 12a9 9 0 0 1-15 6.7L3 16M8 16H3v5"/>',
        logout: '<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9"/>',
        menu: '<path d="M4 7h16M4 12h16M4 17h16"/>',
        x: '<path d="M18 6 6 18M6 6l12 12"/>',
        upload: '<path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12"/>',
        folder: '<path d="M20 20a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-7.9a2 2 0 0 1-1.7-.9l-.8-1.2A2 2 0 0 0 7.9 3H4a2 2 0 0 0-2 2v13a2 2 0 0 0 2 2Z"/>',
        file: '<path d="M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z"/><path d="M14 2v4a2 2 0 0 0 2 2h4"/>',
        search: '<circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/>',
        eye: '<path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/>',
        copy: '<rect x="8" y="8" width="14" height="14" rx="2"/><path d="M4 16a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2"/>',
        check: '<path d="m5 12 5 5L20 7"/>',
        lock: '<rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>',
        link: '<path d="M10 13a5 5 0 0 0 7.5.5l3-3a5 5 0 0 0-7-7l-1.7 1.7"/><path d="M14 11a5 5 0 0 0-7.5-.5l-3 3a5 5 0 0 0 7 7l1.7-1.7"/>',
        ban: '<circle cx="12" cy="12" r="10"/><path d="m4.9 4.9 14.2 14.2"/>',
        play: '<circle cx="12" cy="12" r="10"/><path d="m10 8 6 4-6 4Z"/>',
        dollar: '<path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>',
        "chevron-right": '<path d="m9 18 6-6-6-6"/>',
        home: '<path d="m3 10 9-7 9 7v10a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2Z"/><path d="M9 22V12h6v10"/>',
        inbox: '<path d="M22 12h-6l-2 3h-4l-2-3H2"/><path d="M5.5 5.1 2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.5-6.9A2 2 0 0 0 16.8 4H7.2a2 2 0 0 0-1.7 1.1Z"/>',
        hard: '<path d="M22 12H2M5.5 5.1 2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.5-6.9A2 2 0 0 0 16.8 4H7.2a2 2 0 0 0-1.7 1.1ZM6 16h.01M10 16h.01"/>',
        activity: '<path d="M22 12h-4l-3 9L9 3l-3 9H2"/>',
        download: '<path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3"/>',
        settings: '<path d="M12.2 2h-.4a2 2 0 0 0-2 2v.2a2 2 0 0 1-1 1.7l-.4.3a2 2 0 0 1-2 0l-.2-.1a2 2 0 0 0-2.7.7l-.2.4a2 2 0 0 0 .7 2.7l.2.1a2 2 0 0 1 1 1.7v.5a2 2 0 0 1-1 1.7l-.2.1a2 2 0 0 0-.7 2.7l.2.4a2 2 0 0 0 2.7.7l.2-.1a2 2 0 0 1 2 0l.4.3a2 2 0 0 1 1 1.7v.2a2 2 0 0 0 2 2h.4a2 2 0 0 0 2-2v-.2a2 2 0 0 1 1-1.7l.4-.3a2 2 0 0 1 2 0l.2.1a2 2 0 0 0 2.7-.7l.2-.4a2 2 0 0 0-.7-2.7l-.2-.1a2 2 0 0 1-1-1.7v-.5a2 2 0 0 1 1-1.7l.2-.1a2 2 0 0 0 .7-2.7l-.2-.4a2 2 0 0 0-2.7-.7l-.2.1a2 2 0 0 1-2 0l-.4-.3a2 2 0 0 1-1-1.7V4a2 2 0 0 0-2-2Z"/><circle cx="12" cy="12" r="3"/>',
        "bar-chart": '<path d="M3 3v18h18"/><path d="M7 16v-5M12 16V8M17 16v-9"/>',
        "pie-chart": '<path d="M21.2 15.9A10 10 0 1 1 8 2.8"/><path d="M22 12A10 10 0 0 0 12 2v10Z"/>',
        calendar: '<rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/>',
        trend: '<path d="m22 7-8.5 8.5-5-5L2 17"/><path d="M16 7h6v6"/>'
    };

    function icono(nombre, clase = "") {
        const trazo = ICONOS[nombre] || ICONOS.file;
        return `<svg class="fr-svg ${clase}" viewBox="0 0 24 24" aria-hidden="true">${trazo}</svg>`;
    }

    const escapar = valor =>
        String(valor ?? "").replace(/[&<>"']/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[c]);

    const formato = {
        numero: (valor, decimales = 0) =>
            Number(valor || 0).toLocaleString("es-DO", { minimumFractionDigits: decimales, maximumFractionDigits: decimales }),
        moneda: (valor, simbolo = "US$") =>
            `${simbolo} ${Number(valor || 0).toLocaleString("es-DO", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`,
        bytes(valor) {
            let numero = Number(valor || 0);
            const unidades = ["B", "KB", "MB", "GB", "TB"];
            let indice = 0;
            while (numero >= 1024 && indice < unidades.length - 1) {
                numero /= 1024;
                indice++;
            }
            return `${numero.toLocaleString("es-DO", { maximumFractionDigits: indice ? 2 : 0 })} ${unidades[indice]}`;
        },
        fecha(valor) {
            if (!valor) return "—";
            const fecha = new Date(valor);
            return Number.isNaN(fecha.getTime()) ? "—" : fecha.toLocaleDateString("es-DO", { day: "2-digit", month: "short", year: "numeric" });
        },
        fechaHora(valor) {
            if (!valor) return "—";
            const fecha = new Date(valor);
            return Number.isNaN(fecha.getTime())
                ? "—"
                : fecha.toLocaleString("es-DO", { day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" });
        }
    };

    const ESTADOS = {
        Activo: "is-success",
        Activa: "is-success",
        Pagado: "is-success",
        Aprobada: "is-success",
        Pendiente: "is-warning",
        Suspendido: "is-danger",
        Inactivo: "is-danger",
        Rechazada: "is-danger",
        Cancelado: "is-danger",
        Cancelada: "is-danger",
        Reemplazado: "is-info",
        Eliminado: "is-danger"
    };

    function estado(valor) {
        return `<span class="fr-badge ${ESTADOS[valor] || ""}">${escapar(valor || "—")}</span>`;
    }

    function tabla({ columnas, filas, vacio = "No hay registros para mostrar.", clave }) {
        if (!filas?.length) {
            return `<div class="fr-empty">${icono("inbox")}<p>${escapar(vacio)}</p></div>`;
        }
        return `
            <div class="fr-table-wrap">
                <table class="fr-table" data-i18n>
                    <thead><tr>${columnas.map(c => `<th class="${c.clase || ""}">${escapar(c.titulo)}</th>`).join("")}</tr></thead>
                    <tbody>
                        ${filas
                            .map(
                                (fila, indice) =>
                                    `<tr ${clave ? `data-row="${escapar(fila[clave])}"` : `data-row="${indice}"`}>${columnas
                                        .map(c => `<td class="${c.clase || ""}" data-label="${escapar(c.titulo)}">${c.valor(fila)}</td>`)
                                        .join("")}</tr>`
                            )
                            .join("")}
                    </tbody>
                </table>
            </div>`;
    }

    function paginador(respuesta) {
        if (!respuesta || respuesta.totalPages <= 1) {
            return respuesta ? `<div class="fr-pager"><span>${formato.numero(respuesta.totalRecords)} registros</span></div>` : "";
        }
        return `
            <div class="fr-pager">
                <span>${formato.numero(respuesta.totalRecords)} registros · Página ${respuesta.page} de ${respuesta.totalPages}</span>
                <div>
                    <button class="fr-btn fr-btn-sm" type="button" data-page="${respuesta.page - 1}" ${respuesta.hasPrevious ? "" : "disabled"}>Anterior</button>
                    <button class="fr-btn fr-btn-sm" type="button" data-page="${respuesta.page + 1}" ${respuesta.hasNext ? "" : "disabled"}>Siguiente</button>
                </div>
            </div>`;
    }

    function botonAccion(accion, iconoNombre, titulo, extra = "") {
        return `<button class="fr-row-btn ${extra}" type="button" data-action="${accion}" title="${escapar(titulo)}" aria-label="${escapar(titulo)}">${icono(iconoNombre)}</button>`;
    }

    async function ocupado(boton, tarea) {
        if (boton?.disabled) return undefined;
        if (boton) {
            boton.disabled = true;
            boton.classList.add("is-busy");
        }
        try {
            return await tarea();
        } finally {
            if (boton) {
                boton.disabled = false;
                boton.classList.remove("is-busy");
            }
        }
    }

    function notificarError(error) {
        AppNotifications.error(error?.message || String(error));
    }

    let modalAbierto = null;

    function cerrarModal() {
        if (!modalAbierto) return;
        const actual = modalAbierto;
        modalAbierto = null;
        actual.classList.add("is-leaving");
        window.setTimeout(() => actual.remove(), 160);
        document.body.classList.remove("has-modal");
    }

    function modal({ titulo, subtitulo = "", cuerpo, pie = "", ancho = "md", alCerrar }) {
        cerrarModal();
        const capa = document.createElement("div");
        capa.className = "fr-modal-layer";
        capa.innerHTML = `
            <div class="fr-modal is-${ancho}" role="dialog" aria-modal="true" aria-labelledby="frModalTitle" data-stage>
                <header class="fr-modal-head">
                    <div>
                        <h2 id="frModalTitle">${escapar(titulo)}</h2>
                        ${subtitulo ? `<p>${escapar(subtitulo)}</p>` : ""}
                    </div>
                    <button class="fr-icon-btn" type="button" data-modal-close aria-label="Cerrar">${icono("x")}</button>
                </header>
                <div class="fr-modal-body" data-stage-body>${cuerpo}</div>
                ${pie ? `<footer class="fr-modal-foot">${pie}</footer>` : ""}
            </div>`;
        document.body.appendChild(capa);
        document.body.classList.add("has-modal");
        modalAbierto = capa;
        const cerrar = () => {
            cerrarModal();
            alCerrar?.();
        };
        capa.addEventListener("mousedown", event => {
            if (event.target === capa) cerrar();
        });
        capa.querySelectorAll("[data-modal-close]").forEach(boton => boton.addEventListener("click", cerrar));
        capa.addEventListener("keydown", event => {
            if (event.key === "Escape") cerrar();
        });
        window.setTimeout(() => capa.querySelector("input:not([type=hidden]),select,textarea,button:not([data-modal-close])")?.focus(), 60);
        return { raiz: capa.querySelector(".fr-modal"), cerrar };
    }

    function formulario({ titulo, subtitulo, campos, textoGuardar = "Guardar", ancho = "md", alAbrir, alGuardar }) {
        const { raiz, cerrar } = modal({
            titulo,
            subtitulo,
            ancho,
            cuerpo: `<form class="fr-form" novalidate data-modal-form>${campos}<button type="submit" hidden></button></form>`,
            pie: `<button class="fr-btn" type="button" data-modal-close>Cancelar</button><button class="fr-btn fr-btn-primary" type="button" data-modal-save>${escapar(textoGuardar)}</button>`
        });
        const form = raiz.querySelector("[data-modal-form]");
        const guardar = raiz.querySelector("[data-modal-save]");
        raiz.querySelector("[data-modal-close]")?.addEventListener("click", cerrar);
        alAbrir?.(form, raiz);
        const enviar = () =>
            ocupado(guardar, async () => {
                try {
                    const resultado = await alGuardar(form, raiz);
                    if (resultado !== false) cerrar();
                } catch (error) {
                    notificarError(error);
                }
            });
        guardar.addEventListener("click", enviar);
        form.addEventListener("submit", event => {
            event.preventDefault();
            enviar();
        });
        return { form, raiz, cerrar };
    }

    function confirmar({ titulo = "Confirmar acción", mensaje, textoConfirmar = "Confirmar", peligro = false }) {
        return new Promise(resolve => {
            let resuelto = false;
            const terminar = valor => {
                if (resuelto) return;
                resuelto = true;
                resolve(valor);
            };
            const { raiz, cerrar } = modal({
                titulo,
                ancho: "sm",
                cuerpo: `<p class="fr-confirm-text">${escapar(mensaje)}</p>`,
                pie: `<button class="fr-btn" type="button" data-modal-close>Cancelar</button><button class="fr-btn ${peligro ? "fr-btn-danger" : "fr-btn-primary"}" type="button" data-confirm>${escapar(textoConfirmar)}</button>`,
                alCerrar: () => terminar(false)
            });
            raiz.querySelector("[data-confirm]").addEventListener("click", () => {
                terminar(true);
                cerrar();
            });
        });
    }

    function valores(form) {
        const datos = {};
        for (const elemento of form.elements) {
            if (!elemento.name || elemento.disabled) continue;
            if (elemento.type === "checkbox") {
                if (elemento.dataset.multiple !== undefined) {
                    datos[elemento.name] ||= [];
                    if (elemento.checked) datos[elemento.name].push(Number.isNaN(Number(elemento.value)) ? elemento.value : Number(elemento.value));
                } else datos[elemento.name] = elemento.checked;
            } else if (elemento.type === "radio") {
                if (elemento.checked) datos[elemento.name] = elemento.value;
            } else {
                const texto = elemento.value.trim();
                datos[elemento.name] = texto === "" ? null : elemento.type === "number" ? Number(texto) : texto;
            }
        }
        return datos;
    }

    function exigir(form, reglas) {
        form.querySelectorAll(".is-invalid").forEach(campo => campo.classList.remove("is-invalid"));
        for (const [nombre, mensaje, prueba] of reglas) {
            const campo = form.elements[nombre];
            const valor = campo?.value?.trim?.() ?? "";
            const valido = prueba ? prueba(valor, campo) : valor !== "";
            if (!valido) {
                campo?.classList?.add("is-invalid");
                campo?.focus?.();
                throw new Error(mensaje);
            }
        }
    }

    function campo({ nombre, etiqueta, tipo = "text", valor = "", requerido = false, max, ayuda = "", clase = "", atributos = "", opciones }) {
        const valorSeguro = escapar(valor ?? "");
        let control;
        if (tipo === "select") {
            control = `<select class="fr-select" name="${nombre}" ${requerido ? "required" : ""} ${atributos}>${(opciones || [])
                .map(o => `<option value="${escapar(o.valor)}" ${String(o.valor) === String(valor ?? "") ? "selected" : ""}>${escapar(o.texto)}</option>`)
                .join("")}</select>`;
        } else if (tipo === "textarea") {
            control = `<textarea class="fr-textarea" name="${nombre}" ${max ? `maxlength="${max}"` : ""} ${requerido ? "required" : ""} ${atributos}>${valorSeguro}</textarea>`;
        } else if (tipo === "checkbox") {
            return `<label class="fr-check ${clase}"><input type="checkbox" name="${nombre}" ${valor ? "checked" : ""} ${atributos}><span>${escapar(etiqueta)}</span></label>`;
        } else {
            control = `<input class="fr-input" type="${tipo}" name="${nombre}" value="${valorSeguro}" ${max ? `maxlength="${max}"` : ""} ${requerido ? "required" : ""} ${atributos}>`;
        }
        return `<label class="fr-field ${clase}"><span>${escapar(etiqueta)}${requerido ? ' <b class="fr-required">*</b>' : ""}</span>${control}${ayuda ? `<small>${escapar(ayuda)}</small>` : ""}</label>`;
    }

    function tarjetaDato({ titulo, valor, detalle = "", iconoNombre = "activity", tono = "" }) {
        return `
            <article class="fr-stat ${tono}">
                <span class="fr-stat-icon">${icono(iconoNombre)}</span>
                <div>
                    <p>${escapar(titulo)}</p>
                    <strong>${valor}</strong>
                    ${detalle ? `<small>${detalle}</small>` : ""}
                </div>
            </article>`;
    }

    async function copiar(texto) {
        try {
            await navigator.clipboard.writeText(texto);
            AppNotifications.success("Copiado al portapapeles");
        } catch (_) {
            AppNotifications.warning("No se pudo copiar automáticamente");
        }
    }

    const contrasenaValida = valor =>
        valor.length >= 8 && /[A-Z]/.test(valor) && /[a-z]/.test(valor) && /\d/.test(valor) && /[^A-Za-z0-9]/.test(valor);
    const correoValido = valor => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(valor);
    const usuarioValido = valor => /^[A-Za-z0-9._@-]{3,60}$/.test(valor);
    const MENSAJE_CONTRASENA = "La contraseña debe tener al menos 8 caracteres, mayúscula, minúscula, número y carácter especial";

    function cambiarContrasena(obligatorio = false) {
        formulario({
            titulo: obligatorio ? "Define una nueva contraseña" : "Cambiar contraseña",
            subtitulo: obligatorio ? "Por seguridad debes cambiar tu contraseña antes de continuar." : "",
            ancho: "sm",
            textoGuardar: "Cambiar contraseña",
            campos: `
                ${campo({ nombre: "contrasenaActual", etiqueta: "Contraseña actual", tipo: "password", requerido: true, atributos: 'autocomplete="current-password"' })}
                ${campo({ nombre: "contrasenaNueva", etiqueta: "Nueva contraseña", tipo: "password", requerido: true, ayuda: MENSAJE_CONTRASENA, atributos: 'autocomplete="new-password"' })}
                ${campo({ nombre: "confirmacion", etiqueta: "Confirmar nueva contraseña", tipo: "password", requerido: true, atributos: 'autocomplete="new-password"' })}`,
            async alGuardar(form) {
                exigir(form, [
                    ["contrasenaActual", "Ingresa tu contraseña actual."],
                    ["contrasenaNueva", MENSAJE_CONTRASENA, valor => contrasenaValida(valor)],
                    ["confirmacion", "Las contraseñas no coinciden.", valor => valor === form.elements.contrasenaNueva.value]
                ]);
                await AutenticacionService.cambiarContrasena(form.elements.contrasenaActual.value, form.elements.contrasenaNueva.value);
                FileRedStorage.actualizarSesion({ debeCambiarContrasena: false });
                AppNotifications.success("Contraseña actualizada correctamente");
            }
        });
    }

    async function iniciar({ tipo, pantallas, version }) {
        const contexto = window.FileRedConfig?.contextPath || "";
        let sesion;
        try {
            sesion = await AutenticacionService.validarSesion(tipo);
        } catch (_) {
            sesion = null;
        }
        if (!sesion) {
            FileRedStorage.limpiar();
            window.location.replace(`${contexto}/pages/login/`);
            return;
        }

        const acciones = new Set(sesion.acciones || []);
        const puede = (modulo, accion) => acciones.has(`${modulo}.${accion}`);
        const modulos = (sesion.modulos || []).filter(m => pantallas[m.clave]);
        const navegacion = document.querySelector("[data-panel-nav]");
        const escenario = document.querySelector("[data-panel-stage]");
        const tituloPagina = document.querySelector("[data-panel-title]");
        const descripcionPagina = document.querySelector("[data-panel-description]");
        const perfil = sesion.perfil || {};
        const nombre = [perfil.nombres, perfil.apellidos].filter(Boolean).join(" ") || sesion.usuario;

        document.querySelectorAll("[data-user-name]").forEach(e => (e.textContent = nombre));
        document.querySelectorAll("[data-user-detail]").forEach(e => (e.textContent = perfil.nombreComercial || perfil.correo || sesion.usuario));
        document.querySelectorAll("[data-user-initials]").forEach(e => {
            e.textContent = nombre
                .split(/\s+/)
                .slice(0, 2)
                .map(p => p[0] || "")
                .join("")
                .toUpperCase();
        });

        const grupos = [];
        modulos.forEach(m => {
            const clave = m.categoria || m.clave;
            let grupo = grupos.find(g => g.clave === clave);
            if (!grupo) {
                grupo = { clave, nombre: m.categoriaNombre, icono: m.categoriaIcono || m.icono, opciones: [] };
                grupos.push(grupo);
            }
            grupo.opciones.push(m);
        });
        const claveColapsados = `filered.menu.${tipo}`;
        let colapsados = new Set();
        try {
            colapsados = new Set(JSON.parse(localStorage.getItem(claveColapsados) || "[]"));
        } catch (_) {
            colapsados = new Set();
        }
        const enlace = (m, hijo) =>
            `<a class="panel-nav-item${hijo ? " is-child" : ""}" href="#/${m.clave}" data-nav="${m.clave}">${hijo ? "" : icono(m.icono)}<span>${escapar(m.nombre)}</span></a>`;
        navegacion.innerHTML = grupos
            .map(g =>
                !g.nombre
                    ? g.opciones.map(m => enlace(m, false)).join("")
                    : `<div class="panel-group${colapsados.has(g.clave) ? "" : " is-open"}" data-grupo="${escapar(g.clave)}">
                        <button class="panel-group-head" type="button" aria-expanded="${!colapsados.has(g.clave)}">${icono(g.icono)}<span>${escapar(g.nombre)}</span><svg class="fr-svg panel-chevron" viewBox="0 0 24 24" aria-hidden="true"><path d="m6 9 6 6 6-6"/></svg></button>
                        <div class="panel-group-items"><div>${g.opciones.map(m => enlace(m, true)).join("")}</div></div>
                    </div>`
            )
            .join("");
        navegacion.addEventListener("click", event => {
            const item = event.target.closest("[data-nav]");
            if (item) {
                event.preventDefault();
                document.body.classList.remove("nav-open");
                abrir(item.dataset.nav);
                return;
            }
            const cabecera = event.target.closest(".panel-group-head");
            if (!cabecera) return;
            const grupo = cabecera.closest("[data-grupo]");
            const abierto = grupo.classList.toggle("is-open");
            cabecera.setAttribute("aria-expanded", String(abierto));
            if (abierto) colapsados.delete(grupo.dataset.grupo);
            else colapsados.add(grupo.dataset.grupo);
            try {
                localStorage.setItem(claveColapsados, JSON.stringify([...colapsados]));
            } catch (_) {
                colapsados = new Set(colapsados);
            }
        });
        document.querySelector("[data-nav-skeleton]")?.remove();
        const migas = document.querySelector("[data-panel-crumb]");

        let pantallaActual = null;
        let carga = 0;
        const RUTA_KEY = "filered.ruta";
        let rutaActual = null;

        async function abrir(clave) {
            const modulo = modulos.find(m => m.clave === clave) || modulos[0];
            if (!modulo) {
                escenario.innerHTML = `<div class="fr-empty">${icono("lock")}<p>Tu usuario no tiene módulos asignados. Contacta al administrador.</p></div>`;
                return;
            }
            rutaActual = modulo.clave;
            try { localStorage.setItem(RUTA_KEY, modulo.clave); } catch (e) { }
            navegacion.querySelectorAll("[data-nav]").forEach(e => e.classList.toggle("is-active", e.dataset.nav === modulo.clave));
            const grupoActivo = navegacion.querySelector(`[data-nav="${modulo.clave}"]`)?.closest("[data-grupo]");
            navegacion.querySelectorAll("[data-grupo]").forEach(g => g.classList.toggle("has-active", g === grupoActivo));
            if (grupoActivo && !grupoActivo.classList.contains("is-open")) {
                grupoActivo.classList.add("is-open");
                grupoActivo.querySelector(".panel-group-head")?.setAttribute("aria-expanded", "true");
            }
            if (migas) migas.textContent = modulo.categoriaNombre ? `${modulo.categoriaNombre} / ${modulo.nombre}` : modulo.nombre;
            document.body.classList.remove("nav-open");
            tituloPagina.textContent = modulo.nombre;
            descripcionPagina.textContent = modulo.descripcion || "";
            document.title = `${modulo.nombre} | FileRed`;
            const turno = ++carga;
            try {
                pantallaActual?.reset?.();
                const pantalla = await import(new URL(`${pantallas[modulo.clave]}?v=${version}`, document.baseURI).href);
                if (turno !== carga) return;
                pantalla.loadStyles?.();
                escenario.innerHTML = pantalla.render();
                pantallaActual = pantalla;
                await pantalla.bind(escenario, api);
            } catch (error) {
                if (turno !== carga) return;
                escenario.innerHTML = `<div class="fr-empty">${icono("ban")}<p>${escapar(error.message)}</p></div>`;
            }
        }

        async function abrirPerfil() {
            const ruta = pantallas.__perfil;
            if (!ruta) return;
            rutaActual = "__perfil";
            navegacion.querySelectorAll("[data-nav]").forEach(e => e.classList.remove("is-active"));
            navegacion.querySelectorAll("[data-grupo]").forEach(g => g.classList.remove("has-active"));
            document.body.classList.remove("nav-open");
            if (migas) migas.textContent = "Mi perfil";
            tituloPagina.textContent = "Mi perfil";
            descripcionPagina.textContent = "Administra tu información personal y de contacto";
            document.title = "Mi perfil | FileRed";
            const turno = ++carga;
            try {
                pantallaActual?.reset?.();
                const pantalla = await import(new URL(`${ruta}?v=${version}`, document.baseURI).href);
                if (turno !== carga) return;
                pantalla.loadStyles?.();
                escenario.innerHTML = pantalla.render();
                pantallaActual = pantalla;
                await pantalla.bind(escenario, api);
            } catch (error) {
                if (turno !== carga) return;
                escenario.innerHTML = `<div class="fr-empty">${icono("ban")}<p>${escapar(error.message)}</p></div>`;
            }
        }

        async function abrirSeguridad() {
            const ruta = pantallas.__seguridad;
            if (!ruta) return;
            rutaActual = "__seguridad";
            navegacion.querySelectorAll("[data-nav]").forEach(e => e.classList.remove("is-active"));
            navegacion.querySelectorAll("[data-grupo]").forEach(g => g.classList.remove("has-active"));
            document.body.classList.remove("nav-open");
            if (migas) migas.textContent = "Seguridad";
            tituloPagina.textContent = "Seguridad";
            descripcionPagina.textContent = "Contraseña y verificación en dos pasos";
            document.title = "Seguridad | FileRed";
            const turno = ++carga;
            try {
                pantallaActual?.reset?.();
                const pantalla = await import(new URL(`${ruta}?v=${version}`, document.baseURI).href);
                if (turno !== carga) return;
                pantalla.loadStyles?.();
                escenario.innerHTML = pantalla.render();
                pantallaActual = pantalla;
                await pantalla.bind(escenario, api);
            } catch (error) {
                if (turno !== carga) return;
                escenario.innerHTML = `<div class="fr-empty">${icono("ban")}<p>${escapar(error.message)}</p></div>`;
            }
        }

        const api = {
            sesion,
            puede,
            icono,
            escapar,
            formato,
            estado,
            tabla,
            paginador,
            botonAccion,
            ocupado,
            notificarError,
            modal,
            formulario,
            confirmar,
            valores,
            exigir,
            campo,
            tarjetaDato,
            copiar,
            contrasenaValida,
            correoValido,
            usuarioValido,
            MENSAJE_CONTRASENA,
            recargar: () => (rutaActual === "__perfil" ? abrirPerfil() : rutaActual === "__seguridad" ? abrirSeguridad() : abrir(rutaActual)),
            abrir,
            abrirPerfil,
            abrirSeguridad
        };
        window.FileRedPanelApi = api;

        document.querySelector("[data-nav-toggle]")?.addEventListener("click", () => document.body.classList.toggle("nav-open"));
        document.querySelector("[data-nav-scrim]")?.addEventListener("click", () => document.body.classList.remove("nav-open"));
        document.querySelector("[data-change-password]")?.addEventListener("click", () => abrirSeguridad());
        document.querySelector("[data-panel-user]")?.addEventListener("click", event => {
            if (event.target.closest("[data-change-password],[data-logout]")) return;
            abrirPerfil();
        });
        document.querySelector("[data-logout]")?.addEventListener("click", async event => {
            await ocupado(event.currentTarget, () => AutenticacionService.logout());
            window.location.replace(`${contexto}/pages/login/`);
        });
        function abrirPaleta() {
            const { raiz, cerrar } = modal({
                titulo: "Ir a…",
                ancho: "sm",
                cuerpo: `<div class="palette"><label class="fr-search">${icono("search")}<input class="fr-input" type="search" placeholder="Escribe el nombre de una opción" data-palette-input autocomplete="off"></label>
                    <ul class="palette-list" data-palette-list>${modulos
                        .map(
                            m => `<li><button type="button" data-palette-ir="${m.clave}">${icono(m.icono)}<span><strong>${escapar(m.nombre)}</strong><small>${escapar(m.categoriaNombre || "Inicio")}</small></span><kbd>↵</kbd></button></li>`
                        )
                        .join("")}</ul></div>`
            });
            const entrada = raiz.querySelector("[data-palette-input]");
            const filtrar = () => {
                const texto = entrada.value.trim().toLowerCase();
                raiz.querySelectorAll("[data-palette-ir]").forEach(b => {
                    b.parentElement.hidden = Boolean(texto) && !b.textContent.toLowerCase().includes(texto);
                });
            };
            entrada.addEventListener("input", filtrar);
            entrada.addEventListener("keydown", event => {
                if (event.key !== "Enter") return;
                const primero = [...raiz.querySelectorAll("[data-palette-ir]")].find(b => !b.parentElement.hidden);
                primero?.click();
            });
            raiz.addEventListener("click", event => {
                const destino = event.target.closest("[data-palette-ir]");
                if (!destino) return;
                cerrar();
                abrir(destino.dataset.paletteIr);
            });
            window.setTimeout(() => entrada.focus(), 50);
        }
        document.querySelector("[data-palette-open]")?.addEventListener("click", abrirPaleta);
        document.addEventListener("keydown", event => {
            if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === "k") {
                event.preventDefault();
                abrirPaleta();
            }
        });
        const reloj = document.querySelector("[data-panel-clock]");
        const marcarHora = () => {
            if (reloj) reloj.textContent = new Date().toLocaleTimeString("es-DO", { hour: "2-digit", minute: "2-digit" });
        };
        marcarHora();
        window.setInterval(marcarHora, 30000);
        document.body.classList.remove("is-booting");
        let rutaInicial = "";
        try { rutaInicial = localStorage.getItem(RUTA_KEY) || ""; } catch (e) { }
        if (location.hash) { try { history.replaceState(null, "", location.pathname + location.search); } catch (e) { } }
        await abrir(rutaInicial);
        if (sesion.debeCambiarContrasena) cambiarContrasena(true);
    }

    window.FileRedPanel = Object.freeze({ iniciar, icono, escapar, formato });
})();

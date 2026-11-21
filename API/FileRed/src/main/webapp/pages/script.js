(() => {
    const menuButton = document.querySelector("[data-menu-toggle]");
    const nav = document.querySelector("[data-main-nav]");
    const header = document.querySelector("[data-site-header]");

    menuButton?.addEventListener("click", () => {
        const open = menuButton.getAttribute("aria-expanded") !== "true";
        menuButton.setAttribute("aria-expanded", String(open));
        nav?.classList.toggle("is-open", open);
    });
    nav?.querySelectorAll("a").forEach(link =>
        link.addEventListener("click", () => {
            menuButton?.setAttribute("aria-expanded", "false");
            nav.classList.remove("is-open");
        })
    );

    const onScroll = () => header?.classList.toggle("is-scrolled", window.scrollY > 12);
    window.addEventListener("scroll", onScroll, { passive: true });
    onScroll();

    const year = document.querySelector("[data-year]");
    if (year) year.textContent = String(new Date().getFullYear());

    const reveal = new IntersectionObserver(
        entries =>
            entries.forEach(entry => {
                if (!entry.isIntersecting) return;
                entry.target.classList.add("is-visible");
                reveal.unobserve(entry.target);
            }),
        { threshold: 0.12 }
    );
    function observar(root = document) {
        root.querySelectorAll("[data-reveal]:not(.is-visible)").forEach(element => reveal.observe(element));
    }
    observar();

    const tabs = [...document.querySelectorAll("[data-code-tab]")];
    tabs.forEach(tab =>
        tab.addEventListener("click", () => {
            tabs.forEach(item => {
                const active = item === tab;
                item.classList.toggle("is-active", active);
                item.setAttribute("aria-selected", String(active));
            });
            document.querySelectorAll("[data-code-panel]").forEach(panel => {
                panel.hidden = panel.dataset.codePanel !== tab.dataset.codeTab;
            });
        })
    );

    document.querySelector("[data-code-copy]")?.addEventListener("click", async event => {
        const visible = document.querySelector("[data-code-panel]:not([hidden])");
        try {
            await navigator.clipboard.writeText(visible?.textContent || "");
            event.currentTarget.textContent = "Copiado";
        } catch (_) {
            event.currentTarget.textContent = "No se pudo copiar";
        }
        const button = event.currentTarget;
        window.setTimeout(() => (button.textContent = "Copiar"), 1600);
    });

    const escapar = valor =>
        String(valor ?? "").replace(/[&<>"']/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[c]);
    const numero = valor => Number(valor || 0).toLocaleString("es-DO", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    const gb = valor => Number(valor || 0).toLocaleString("es-DO", { maximumFractionDigits: 2 });
    const periodos = { Mensual: "mes", Bimestral: "2 meses", Trimestral: "trimestre", Semestral: "semestre", Anual: "año" };
    const check =
        '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="m5 12 5 5L20 7"/></svg>';

    function tarjeta(plan) {
        const caracteristicas = String(plan.caracteristicas || "")
            .split("|")
            .map(item => item.trim())
            .filter(Boolean);
        const adicional = plan.espacioGb != null && Number(plan.precioGbAdicional || 0) > 0
            ? `<li>${check}<span>GB adicional: ${escapar(plan.monedaSimbolo)} ${numero(plan.precioGbAdicional)}</span></li>`
            : "";
        return `
            <article class="plan-card${plan.destacado ? " is-featured" : ""}" data-reveal>
                ${plan.destacado ? '<span class="plan-flag">Más popular</span>' : ""}
                <h3>${escapar(plan.nombre)}</h3>
                <p class="plan-description">${escapar(plan.descripcion || "")}</p>
                <p class="plan-price"><span class="plan-currency">${escapar(plan.monedaSimbolo)}</span><strong>${numero(plan.precio)}</strong><span>/ ${escapar(periodos[plan.modalidad] || plan.modalidad)}</span></p>
                <div class="plan-specs">
                    <div><strong>${plan.espacioGb == null ? "Ilimitado" : `${gb(plan.espacioGb)} GB`}</strong><span>Espacio</span></div>
                    <div><strong>${plan.tamanoMaximoArchivoMb == null ? "Sin límite" : `${escapar(plan.tamanoMaximoArchivoMb)} MB`}</strong><span>Por archivo</span></div>
                </div>
                <ul class="plan-features">
                    ${caracteristicas.map(item => `<li>${check}<span>${escapar(item)}</span></li>`).join("")}
                    ${adicional}
                </ul>
                <a class="fr-btn ${plan.destacado ? "fr-btn-primary" : ""} plan-cta" href="registro/?plan=${encodeURIComponent(plan.planId)}">Comenzar con ${escapar(plan.nombre)}</a>
            </article>`;
    }

    async function cargarPlanes() {
        const contenedor = document.querySelector("[data-plans]");
        const vacio = document.querySelector("[data-plans-empty]");
        if (!contenedor || !window.PlanService) return;
        try {
            const planes = (await PlanService.publicos()) || [];
            contenedor.innerHTML = planes.map(tarjeta).join("");
            if (vacio) vacio.hidden = planes.length > 0;
            contenedor.hidden = planes.length === 0;
            observar(contenedor);
        } catch (_) {
            contenedor.hidden = true;
            if (vacio) vacio.hidden = false;
        }
    }

    cargarPlanes();
})();

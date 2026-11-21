(() => {
    const root = document.documentElement;
    const storageKey = "filered.theme";
    let stored = null;
    try {
        stored = localStorage.getItem(storageKey);
    } catch (_) {
        stored = null;
    }
    const inicial = stored === "light" || stored === "dark" ? stored : "dark";

    function applyTheme(value) {
        root.dataset.theme = value;
        try {
            localStorage.setItem(storageKey, value);
        } catch (_) {
            root.dataset.themeVolatile = "true";
        }
        const next = value === "light" ? "oscuro" : "claro";
        document.querySelectorAll("[data-theme-toggle]").forEach(toggle => {
            toggle.setAttribute("aria-label", `Cambiar a tema ${next}`);
            toggle.title = `Cambiar a tema ${next}`;
        });
        document.querySelector('meta[name="theme-color"]')?.setAttribute("content", value === "light" ? "#f3f4f6" : "#060607");
    }

    applyTheme(inicial);
    document.addEventListener("click", event => {
        if (!event.target.closest("[data-theme-toggle]")) return;
        applyTheme(root.dataset.theme === "dark" ? "light" : "dark");
    });
})();

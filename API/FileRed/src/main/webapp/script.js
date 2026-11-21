(() => {
    const appRoot = document.getElementById("app-root");
    const minimumLoadingTime = 1400;
    const startedAt = performance.now();

    async function showWelcome() {
        const elapsed = performance.now() - startedAt;
        await new Promise(resolve => window.setTimeout(resolve, Math.max(0, minimumLoadingTime - elapsed)));
        appRoot.querySelector(".boot-screen")?.classList.add("is-leaving");
        window.setTimeout(() => window.location.replace("pages/"), 380);
    }

    showWelcome();
})();

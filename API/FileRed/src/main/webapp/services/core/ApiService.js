window.FileRedApi = (() => {
    const baseUrl = window.FileRedConfig?.apiBaseUrl || window.location.origin;
    const areasProtegidas = ["/pages/center", "/pages/customer"];

    function createError(status, payload) {
        const error = new Error(payload?.mensage || payload?.message || payload?.error || `La API respondió con estado ${status}.`);
        error.status = status;
        error.response = payload;
        return error;
    }

    function isDataQuery(path, method) {
        if (method === "GET" || method === "HEAD") return true;
        return method === "POST" && /(?:^|\/)get[A-Za-z]*(?:\/|\?|$)/.test(path);
    }

    function redirigirAlLogin() {
        const currentPath = window.location.pathname || "";
        if (!areasProtegidas.some(area => currentPath.includes(area))) return;
        window.location.replace(`${window.FileRedConfig?.contextPath || ""}/pages/login/`);
    }

    async function request(path, options = {}) {
        const headers = new Headers(options.headers || {});
        headers.set("Accept", "application/json");
        const token = window.FileRedStorage?.token();
        if (token) headers.set("Authorization", `Bearer ${token}`);

        let body = options.body;
        if (body !== undefined && !(body instanceof FormData)) {
            headers.set("Content-Type", "application/json");
            body = JSON.stringify(body);
        }

        const method = options.method || "GET";
        const target = window.FileRedLoader?.resolveTarget?.();
        const useSkeleton = isDataQuery(path, method) && target?.hasAttribute?.("data-stage");
        const loadingInstance = options.silent
            ? null
            : useSkeleton
              ? window.FileRedSkeleton?.show(target)
              : window.FileRedLoader?.show(target);
        try {
            const response = await fetch(`${baseUrl}${path}`, { method, headers, body, credentials: "same-origin" });
            const text = await response.text();
            let payload = null;
            if (text) {
                try {
                    payload = JSON.parse(text);
                } catch (_) {
                    payload = { mensage: text };
                }
            }
            if (!response.ok) {
                if (response.status === 401 && token) {
                    window.FileRedStorage?.limpiar();
                    redirigirAlLogin();
                }
                throw createError(response.status, payload);
            }
            return payload && Object.prototype.hasOwnProperty.call(payload, "data") ? payload.data : payload;
        } catch (cause) {
            if (cause.status) throw cause;
            const error = new Error("No fue posible conectar con FileRed.");
            error.cause = cause;
            throw error;
        } finally {
            if (loadingInstance) {
                if (useSkeleton) window.FileRedSkeleton?.hide(loadingInstance);
                else window.FileRedLoader?.hide(loadingInstance);
            }
        }
    }

    return Object.freeze({
        request,
        get: (path, options = {}) => request(path, options),
        post: (path, body, options = {}) => request(path, { ...options, method: "POST", body }),
        put: (path, body, options = {}) => request(path, { ...options, method: "PUT", body }),
        delete: (path, body, options = {}) => request(path, { ...options, method: "DELETE", body })
    });
})();

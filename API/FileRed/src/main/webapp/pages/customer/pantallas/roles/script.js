import { crearPantallaRoles } from "../../../comunes/roles.js?v=20261120-7";

const pantalla = crearPantallaRoles({
    servicio: RolClienteService,
    modulos: () => ModuloClienteService.listar(),
    id: "rolClienteId",
    uuid: "rolClienteUuid",
    accionId: "moduloClienteAccionId"
});

export const render = pantalla.render;
export const skeleton = pantalla.skeleton;
export const bind = pantalla.bind;
export const reset = pantalla.reset;

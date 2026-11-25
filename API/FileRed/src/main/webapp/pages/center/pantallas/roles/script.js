import { crearPantallaRoles } from "../../../comunes/roles.js?v=20261120-7";

const pantalla = crearPantallaRoles({
    servicio: RolAdministracionService,
    modulos: () => ModuloAdministracionService.listar(),
    id: "rolAdministracionId",
    uuid: "rolAdministracionUuid",
    accionId: "moduloAdministracionAccionId"
});

export const render = pantalla.render;
export const skeleton = pantalla.skeleton;
export const bind = pantalla.bind;
export const reset = pantalla.reset;

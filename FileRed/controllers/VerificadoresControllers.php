<?php

require_once(__DIR__ . "/../config/config.php");

// === Verificar acceso ===
function verificarAcceso($apikey, $permiso)
{
    global $API_KEY_VALIDA, $PERMISOS;

    if ($apikey !== $API_KEY_VALIDA) {
        echo json_encode([
            'Validacion' => 'Error',
            'Respuesta' => [
                'mensaje' => 'APIKEY INCORRECTA',
                'recibida' => $apikey,
                'esperada' => $API_KEY_VALIDA
            ]
        ]);
        exit;
    }

    if (!in_array($permiso, $PERMISOS)) {
        echo json_encode([
            'Validacion' => 'Error',
            'Respuesta' => [
                'mensaje' => 'PERMISO NO EXISTE',
                'recibido' => $permiso
            ]
        ]);
        exit;
    }

    return true;
}
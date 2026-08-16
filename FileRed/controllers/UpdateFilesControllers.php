<?php

require_once(__DIR__ . "/../config/config.php");
require_once(__DIR__ . "/FilesControllers.php");

// UpdateFile(almacen, categoria, identificador, extensionActual, archivo)
// Reemplaza el contenido físico conservando el mismo identificador.
function updateFile($almacen, $categoria, $identificador, $extensionActual, $file)
{
    global $EXTENSIONES_PERMITIDAS;

    if ($file['error'] !== UPLOAD_ERR_OK) {
        echo json_encode([
            'Validacion' => 'Error',
            'Respuesta' => ['mensaje' => 'Error al recibir el archivo: ' . $file['error']]
        ]);
        return;
    }

    if (empty($file['tmp_name']) || $file['size'] === 0) {
        echo json_encode([
            'Validacion' => 'Error',
            'Respuesta' => ['mensaje' => 'El archivo está vacío o no fue enviado correctamente']
        ]);
        return;
    }

    if ($file['size'] > MAX_SIZE) {
        echo json_encode([
            'Validacion' => 'Error',
            'Respuesta' => ['mensaje' => 'El archivo supera el tamaño máximo de 100MB']
        ]);
        return;
    }

    $extensionActual = strtolower(ltrim(trim($extensionActual), '.'));
    $extensionNueva = strtolower(pathinfo($file['name'], PATHINFO_EXTENSION));

    if (!$extensionNueva || !in_array($extensionNueva, $EXTENSIONES_PERMITIDAS)) {
        echo json_encode([
            'Validacion' => 'Error',
            'Respuesta' => ['mensaje' => 'Extensión no permitida: ' . $extensionNueva]
        ]);
        return;
    }

    $ruta = getRutaFisica($almacen, $categoria);
    $pathActual = $ruta . $identificador . '.' . $extensionActual;
    $pathNuevo = $ruta . $identificador . '.' . $extensionNueva;

    if (!file_exists($ruta)) {
        echo json_encode([
            'Validacion' => 'Error',
            'Respuesta' => ['mensaje' => 'La carpeta no existe']
        ]);
        return;
    }

    if (!file_exists($pathActual)) {
        echo json_encode([
            'Validacion' => 'Error',
            'Respuesta' => ['mensaje' => 'Archivo original no encontrado']
        ]);
        return;
    }

    // Si cambia la extensión se crea primero el nuevo archivo y solo después
    // se elimina el anterior. Si la extensión es la misma, PHP reemplaza
    // directamente el archivo manteniendo exactamente la misma ruta.
    if (!move_uploaded_file($file['tmp_name'], $pathNuevo)) {
        echo json_encode([
            'Validacion' => 'Error',
            'Respuesta' => ['mensaje' => 'No se pudo actualizar el archivo']
        ]);
        return;
    }

    if ($pathActual !== $pathNuevo && file_exists($pathActual)) {
        if (!unlink($pathActual)) {
            // Evitar dejar dos versiones físicas si no se pudo retirar la anterior.
            @unlink($pathNuevo);
            echo json_encode([
                'Validacion' => 'Error',
                'Respuesta' => ['mensaje' => 'No se pudo retirar la versión anterior del archivo']
            ]);
            return;
        }
    }

    $url = getUrlArchivo($almacen, $categoria, $identificador, $extensionNueva);

    $base64 = null;
    $mime = getMime($extensionNueva);
    if (strpos($mime, 'image/') === 0) {
        $contenido = file_get_contents($pathNuevo);
        $base64 = 'data:' . $mime . ';base64,' . base64_encode($contenido);
    }

    echo json_encode([
        'Validacion' => 'Exitoso',
        'Respuesta' => [
            'mensaje' => 'Archivo actualizado correctamente',
            'identificador' => $identificador,
            'extension' => $extensionNueva,
            'size' => filesize($pathNuevo),
            'url' => $url,
            'base64' => $base64,
        ]
    ]);
}

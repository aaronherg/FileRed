<?php

require_once(__DIR__ . "/../config/config.php");

// ============================================================
// HELPERS
// ============================================================

function getMime($ext)
{
    $map = [
        'jpg' => 'image/jpeg',
        'jpeg' => 'image/jpeg',
        'png' => 'image/png',
        'gif' => 'image/gif',
        'webp' => 'image/webp',
        'bmp' => 'image/bmp',
        'svg' => 'image/svg+xml',
        'ico' => 'image/x-icon',
        'tiff' => 'image/tiff',
        'mp4' => 'video/mp4',
        'avi' => 'video/x-msvideo',
        'mov' => 'video/quicktime',
        'wmv' => 'video/x-ms-wmv',
        'flv' => 'video/x-flv',
        'mkv' => 'video/x-matroska',
        'webm' => 'video/webm',
        'm4v' => 'video/x-m4v',
        '3gp' => 'video/3gpp',
        'mp3' => 'audio/mpeg',
        'wav' => 'audio/wav',
        'ogg' => 'audio/ogg',
        'aac' => 'audio/aac',
        'flac' => 'audio/flac',
        'm4a' => 'audio/mp4',
        'pdf' => 'application/pdf',
        'txt' => 'text/plain',
        'rtf' => 'application/rtf',
        'odt' => 'application/vnd.oasis.opendocument.text',
        'doc' => 'application/msword',
        'docx' => 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'xls' => 'application/vnd.ms-excel',
        'xlsx' => 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        'ppt' => 'application/vnd.ms-powerpoint',
        'pptx' => 'application/vnd.openxmlformats-officedocument.presentationml.presentation',
        'ods' => 'application/vnd.oasis.opendocument.spreadsheet',
        'odp' => 'application/vnd.oasis.opendocument.presentation',
        'zip' => 'application/zip',
        'rar' => 'application/vnd.rar',
        '7z' => 'application/x-7z-compressed',
        'tar' => 'application/x-tar',
        'gz' => 'application/gzip',
        'json' => 'application/json',
        'xml' => 'application/xml',
        'csv' => 'text/csv',
        'md' => 'text/markdown',
        'log' => 'text/plain',
    ];

    return $map[$ext] ?? 'application/octet-stream';
}

// Construye la ruta física de la carpeta
function getRutaFisica($almacen, $categoria)
{
    return BASE_PATH . $almacen . '/' . $categoria . '/';
}

// Construye la URL pública del archivo
// Resultado: /eduversord/almacen/categoria/uuid.ext
function getUrlArchivo($almacen, $categoria, $identificador, $extension)
{
    return BASE_URL . $almacen . '/' . $categoria . '/' . $identificador . '.' . $extension;
}

// ============================================================
// MANEJADOR DE CARPETAS
// ============================================================

// AddDirectory(almacen, categoria)
function addDirectory($almacen, $categoria)
{
    $ruta = getRutaFisica($almacen, $categoria);

    if (file_exists($ruta)) {
        echo json_encode([
            'Validacion' => 'Error',
            'Respuesta' => ['mensaje' => 'La carpeta ya existe']
        ]);
        return;
    }

    if (mkdir($ruta, 0777, false)) {
        echo json_encode([
            'Validacion' => 'Exitoso',
            'Respuesta' => [
                'mensaje' => 'Carpeta creada correctamente',
                'almacen' => $almacen,
                'categoria' => $categoria,
            ]
        ]);
    } else {
        echo json_encode([
            'Validacion' => 'Error',
            'Respuesta' => ['mensaje' => 'No se pudo crear la carpeta']
        ]);
    }
}

// DeleteDirectory(almacen, categoria)
// Elimina la carpeta y todos los archivos que están dentro
function deleteDirectory($almacen, $categoria)
{
    $ruta = getRutaFisica($almacen, $categoria);

    if (!file_exists($ruta)) {
        echo json_encode([
            'Validacion' => 'Error',
            'Respuesta' => ['mensaje' => 'La carpeta no existe']
        ]);
        return;
    }

    // Elimina todos los archivos dentro antes de eliminar la carpeta
    $archivos = glob($ruta . '*');
    foreach ($archivos as $archivo) {
        if (is_file($archivo)) {
            unlink($archivo);
        }
    }

    if (rmdir($ruta)) {
        echo json_encode([
            'Validacion' => 'Exitoso',
            'Respuesta' => ['mensaje' => 'Carpeta y archivos eliminados correctamente']
        ]);
    } else {
        echo json_encode([
            'Validacion' => 'Error',
            'Respuesta' => ['mensaje' => 'No se pudo eliminar la carpeta']
        ]);
    }
}

// GetAllDirectory(almacen)
// Devuelve todas las categorias (subcarpetas) dentro de un almacen
function getAllDirectory($almacen)
{
    $ruta = BASE_PATH . $almacen . '/';

    if (!file_exists($ruta)) {
        echo json_encode([
            'Validacion' => 'Error',
            'Respuesta' => ['mensaje' => 'El almacén no existe']
        ]);
        return;
    }

    $carpetas = array_values(array_filter(
        glob($ruta . '*'),
        'is_dir'
    ));

    $resultado = array_map(function ($c) use ($almacen) {
        return [
            'categoria' => basename($c),
            'almacen' => $almacen,
        ];
    }, $carpetas);

    echo json_encode([
        'Validacion' => 'Exitoso',
        'Respuesta' => ['carpetas' => $resultado]
    ]);
}

// ============================================================
// MANEJADOR DE ARCHIVOS
// ============================================================

// AddFile(almacen, categoria, archivo)
// Devuelve la URL del archivo guardado
function addFile($almacen, $categoria, $file)
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

    $ext = strtolower(pathinfo($file['name'], PATHINFO_EXTENSION));

    if (!in_array($ext, $EXTENSIONES_PERMITIDAS)) {
        echo json_encode([
            'Validacion' => 'Error',
            'Respuesta' => ['mensaje' => 'Extensión no permitida: ' . $ext]
        ]);
        return;
    }

    $ruta = getRutaFisica($almacen, $categoria);

    // Si la carpeta no existe la crea automáticamente
    // Crea el almacén si no existe, pero la categoría debe existir (se crea con addDirectory)
    $rutaAlmacen = BASE_PATH . $almacen . '/';
    if (!file_exists($rutaAlmacen)) {
        mkdir($rutaAlmacen, 0777, true);
    }

    if (!file_exists($ruta)) {
        echo json_encode([
            'Validacion' => 'Error',
            'Respuesta' => ['mensaje' => 'La carpeta no existe, créela primero con addDirectory']
        ]);
        return;
    }

    $identificador = bin2hex(random_bytes(16));
    $path = $ruta . $identificador . '.' . $ext;

    if (!move_uploaded_file($file['tmp_name'], $path)) {
        echo json_encode([
            'Validacion' => 'Error',
            'Respuesta' => ['mensaje' => 'No se pudo guardar el archivo']
        ]);
        return;
    }

    $url = getUrlArchivo($almacen, $categoria, $identificador, $ext);

    // Convertir a base64 solo si es imagen
    $base64 = null;
    $mime = getMime($ext);
    if (strpos($mime, 'image/') === 0) {
        $contenido = file_get_contents($path);
        $base64 = 'data:' . $mime . ';base64,' . base64_encode($contenido);
    }

    echo json_encode([
        'Validacion' => 'Exitoso',
        'Respuesta' => [
            'identificador' => $identificador,
            'extension' => $ext,
            'size' => $file['size'],
            'url' => $url,
            'base64' => $base64,
        ]
    ]);
}

// DeleteFileById(almacen, categoria, identificador, extension)
function deleteFileById($almacen, $categoria, $identificador, $extension)
{
    $ruta = getRutaFisica($almacen, $categoria) . $identificador . '.' . $extension;

    if (!file_exists($ruta)) {
        echo json_encode([
            'Validacion' => 'Error',
            'Respuesta' => ['mensaje' => 'Archivo no encontrado']
        ]);
        return;
    }

    if (unlink($ruta)) {
        echo json_encode([
            'Validacion' => 'Exitoso',
            'Respuesta' => ['mensaje' => 'Archivo eliminado correctamente']
        ]);
    } else {
        echo json_encode([
            'Validacion' => 'Error',
            'Respuesta' => ['mensaje' => 'No se pudo eliminar el archivo']
        ]);
    }
}

// GetAllFiles(almacen, categoria)
// Devuelve todos los archivos de una carpeta con su URL
function getAllFiles($almacen, $categoria)
{
    $ruta = getRutaFisica($almacen, $categoria);

    if (!file_exists($ruta)) {
        echo json_encode([
            'Validacion' => 'Error',
            'Respuesta' => ['mensaje' => 'La carpeta no existe']
        ]);
        return;
    }

    $archivos = array_values(array_filter(
        glob($ruta . '*'),
        'is_file'
    ));

    $resultado = array_map(function ($archivo) use ($almacen, $categoria) {
        $nombre = pathinfo($archivo, PATHINFO_FILENAME);
        $ext = strtolower(pathinfo($archivo, PATHINFO_EXTENSION));
        return [
            'identificador' => $nombre,
            'extension' => $ext,
            'size' => filesize($archivo),
            'url' => getUrlArchivo($almacen, $categoria, $nombre, $ext),
        ];
    }, $archivos);

    echo json_encode([
        'Validacion' => 'Exitoso',
        'Respuesta' => ['archivos' => $resultado]
    ]);
}

// GetFileById(almacen, categoria, identificador, extension)
// Devuelve la URL del archivo
function getFileById($almacen, $categoria, $identificador, $extension)
{
    $ruta = getRutaFisica($almacen, $categoria) . $identificador . '.' . $extension;

    if (!file_exists($ruta)) {
        echo json_encode([
            'Validacion' => 'Error',
            'Respuesta' => ['mensaje' => 'Archivo no encontrado']
        ]);
        return;
    }

    $url = getUrlArchivo($almacen, $categoria, $identificador, $extension);

    echo json_encode([
        'Validacion' => 'Exitoso',
        'Respuesta' => [
            'identificador' => $identificador,
            'extension' => $extension,
            'size' => filesize($ruta),
            'url' => $url,
        ]
    ]);
}
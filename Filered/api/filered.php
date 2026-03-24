<?php

// ================= CONFIG =================
define('BASE_PATH', dirname(__DIR__) . '/files/');
define('SECRET_KEY', 'A9fK3xQ7LmP2vZ8tR5cW1sY4');
define('MAX_SIZE', 100 * 1024 * 1024); // 100MB

$allowed = [
    'jpg','jpeg','png','gif','webp','bmp','svg','ico','tiff',
    'mp4','avi','mov','wmv','flv','mkv','webm','m4v','3gp',
    'mp3','wav','ogg','aac','flac','m4a',
    'pdf','txt','rtf','odt',
    'doc','docx','xls','xlsx','ppt','pptx',
    'ods','odp',
    'zip','rar','7z','tar','gz',
    'json','xml','csv','md','log'
];

// ================= RESPONSE =================
function jsonResponse($validacion, $mensaje = "", $data = null) {
    header('Content-Type: application/json');
    echo json_encode([
        "validacion" => $validacion,
        "mensaje" => $mensaje,
        "data" => $data
    ]);
    exit;
}

// ================= TOKEN =================
function generateToken($id) {
    return hash_hmac('sha256', $id, SECRET_KEY);
}

function validateToken($id, $token) {
    return hash_equals(generateToken($id), $token);
}

// ================= MIME =================
function getMime($ext) {
    $map = [

        // ===== IMÁGENES =====
        'jpg'=>'image/jpeg','jpeg'=>'image/jpeg','png'=>'image/png',
        'gif'=>'image/gif','webp'=>'image/webp','bmp'=>'image/bmp',
        'svg'=>'image/svg+xml','ico'=>'image/x-icon','tiff'=>'image/tiff',

        // ===== VIDEO =====
        'mp4'=>'video/mp4','avi'=>'video/x-msvideo','mov'=>'video/quicktime',
        'wmv'=>'video/x-ms-wmv','flv'=>'video/x-flv','mkv'=>'video/x-matroska',
        'webm'=>'video/webm','m4v'=>'video/x-m4v','3gp'=>'video/3gpp',

        // ===== AUDIO =====
        'mp3'=>'audio/mpeg','wav'=>'audio/wav','ogg'=>'audio/ogg',
        'aac'=>'audio/aac','flac'=>'audio/flac','m4a'=>'audio/mp4',

        // ===== DOCUMENTOS =====
        'pdf'=>'application/pdf',
        'txt'=>'text/plain',
        'rtf'=>'application/rtf',
        'odt'=>'application/vnd.oasis.opendocument.text',

        // ===== OFFICE =====
        'doc'=>'application/msword',
        'docx'=>'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'xls'=>'application/vnd.ms-excel',
        'xlsx'=>'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        'ppt'=>'application/vnd.ms-powerpoint',
        'pptx'=>'application/vnd.openxmlformats-officedocument.presentationml.presentation',

        // ===== OPENDOCUMENT =====
        'ods'=>'application/vnd.oasis.opendocument.spreadsheet',
        'odp'=>'application/vnd.oasis.opendocument.presentation',

        // ===== COMPRESIÓN =====
        'zip'=>'application/zip',
        'rar'=>'application/vnd.rar',
        '7z'=>'application/x-7z-compressed',
        'tar'=>'application/x-tar',
        'gz'=>'application/gzip',

        // ===== DATOS / TEXTO =====
        'json'=>'application/json',
        'xml'=>'application/xml',
        'csv'=>'text/csv',
        'md'=>'text/markdown',
        'log'=>'text/plain'
    ];

    return $map[$ext] ?? 'application/octet-stream';
}

// ================= HELPER VALIDATION =================
function requireParams($params, $required) {
    $missing = [];

    foreach ($required as $key) {
        if (!isset($params[$key]) || $params[$key] === '') {
            $missing[] = $key;
        }
    }

    if (!empty($missing)) {
        jsonResponse("Error", "Asegurese de especificar el parametro: " . implode(", ", $missing));
    }
}

// ================= CAPTURA GLOBAL =================
$method = $_SERVER['REQUEST_METHOD'];
$request = $_POST;

if (!$method) {
    jsonResponse("Error", "Metodo HTTP no detectado");
}

requireParams($request, ['action']);

$action = $request['action'];

switch ($action) {

    case 'addFile':

        if ($method !== 'POST') {
            jsonResponse("Error", "Metodo no permitido");
        }

        if (!isset($_FILES['file'])) {
            jsonResponse("Error", "No se envio archivo");
        }

        $file = $_FILES['file'];

        if (
            $file['error'] === UPLOAD_ERR_NO_FILE ||
            empty($file['tmp_name']) ||
            $file['size'] === 0
        ) {
            jsonResponse("Error", "El archivo esta vacio o no fue enviado correctamente");
        }

        handleUpload($file);
        break;

    case 'getFile':

        if ($method !== 'POST') {
            jsonResponse("Error", "Metodo no permitido");
        }

        requireParams($request, ['id', 'extension', 'token']);

        handleGetFile(
            $request['id'],
            $request['extension'],
            $request['token']
        );
        break;

    default:
        jsonResponse("Error", "Accion no valida");
}

// ================= UPLOAD =================
function handleUpload($file) {
    global $allowed;

    try {

        if ($file['error'] !== 0) {
            throw new Exception("Error en upload: " . $file['error']);
        }

        if ($file['size'] > MAX_SIZE) {
            throw new Exception("Archivo demasiado grande");
        }

        $ext = strtolower(pathinfo($file['name'], PATHINFO_EXTENSION));

        if (!in_array($ext, $allowed)) {
            throw new Exception("Extension no permitida: $ext");
        }

        $dir = BASE_PATH . $ext . "/";
        if (!file_exists($dir)) {
            mkdir($dir, 0777, true);
        }

        $id = bin2hex(random_bytes(16));
        $path = $dir . $id . "." . $ext;

        if (!move_uploaded_file($file['tmp_name'], $path)) {
            throw new Exception("Error al guardar archivo");
        }

        $token = generateToken($id);

        jsonResponse("Exitoso", "", [
            "id"=>$id,
            "extension"=>$ext,
            "size"=>$file['size'],
            "token"=>$token
        ]);

    } catch (Exception $e) {
        jsonResponse("Error", $e->getMessage());
    }
}

// ================= GET FILE =================
function handleGetFile($id, $ext, $token) {

    if (!validateToken($id, $token)) {
        http_response_code(403);
        echo "Token invalido";
        exit;
    }

    $path = BASE_PATH . $ext . "/" . $id . "." . $ext;

    if (!file_exists($path)) {
        http_response_code(404);
        echo "Archivo no encontrado";
        exit;
    }

    $mime = getMime($ext);

    header("Content-Type: $mime");
    header("Content-Length: " . filesize($path));
    header("Content-Disposition: inline; filename=\"$id.$ext\"");

    readfile($path);
    exit;
}
<?php

// Ruta física donde se guardan los archivos en el servidor
define('BASE_PATH', dirname(__DIR__) . '/files/');

// Tamaño máximo permitido por archivo
define('MAX_SIZE', 100 * 1024 * 1024); // 100MB

// Dominio base para armar la URL pública del archivo
define('BASE_URL', '/');

$EXTENSIONES_PERMITIDAS = [
    'jpg',
    'jpeg',
    'png',
    'gif',
    'webp',
    'bmp',
    'svg',
    'ico',
    'tiff',
    'mp4',
    'avi',
    'mov',
    'wmv',
    'flv',
    'mkv',
    'webm',
    'm4v',
    '3gp',
    'mp3',
    'wav',
    'ogg',
    'aac',
    'flac',
    'm4a',
    'pdf',
    'txt',
    'rtf',
    'odt',
    'doc',
    'docx',
    'xls',
    'xlsx',
    'ppt',
    'pptx',
    'ods',
    'odp',
    'zip',
    'rar',
    '7z',
    'tar',
    'gz',
    'json',
    'xml',
    'csv',
    'md',
    'log'
];

$API_KEY_VALIDA = "12345678";

$PERMISOS = [
    // Archivos
    "agregar_archivo",
    "actualizar_archivo",
    "eliminar_archivo",
    "obtener_archivos",
    "obtener_archivo",

    // Carpetas
    "agregar_carpeta",
    "eliminar_carpeta",
    "obtener_carpetas",
];
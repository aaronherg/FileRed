<?php

require_once(__DIR__ . "/../../controllers/FilesControllers.php");
require_once(__DIR__ . "/../../controllers/UpdateFilesControllers.php");
require_once(__DIR__ . "/../../controllers/VerificadoresControllers.php");

header('Content-Type: application/json; charset=utf-8');

function ejecutador()
{
    $apikey = $_POST['apikey'] ?? null;
    $permiso = $_POST['permiso'] ?? null;
    $metodo = $_POST['metodo'] ?? null;

    if (!$apikey || !$permiso || !$metodo) {
        echo json_encode([
            'Validacion' => 'Error',
            'Respuesta' => ['mensaje' => 'SE REQUIERE: apikey, permiso, metodo']
        ]);
        return;
    }

    if (!verificarAcceso($apikey, $permiso)) {
        return;
    }

    // ====================================
    // MANEJADOR DE CARPETAS
    // ====================================

    if ($metodo === 'addDirectory' && $permiso === 'agregar_carpeta') {
        $almacen = trim($_POST['almacen'] ?? '');
        $categoria = trim($_POST['categoria'] ?? '');

        if ($almacen && $categoria) {
            addDirectory($almacen, $categoria);
        } else {
            echo json_encode([
                'Validacion' => 'Error',
                'Respuesta' => ['mensaje' => 'SE REQUIERE: almacen, categoria']
            ]);
        }
    }

    else if ($metodo === 'deleteDirectory' && $permiso === 'eliminar_carpeta') {
        $almacen = trim($_POST['almacen'] ?? '');
        $categoria = trim($_POST['categoria'] ?? '');

        if ($almacen && $categoria) {
            deleteDirectory($almacen, $categoria);
        } else {
            echo json_encode([
                'Validacion' => 'Error',
                'Respuesta' => ['mensaje' => 'SE REQUIERE: almacen, categoria']
            ]);
        }
    }

    else if ($metodo === 'getAllDirectory' && $permiso === 'obtener_carpetas') {
        $almacen = trim($_POST['almacen'] ?? '');

        if ($almacen) {
            getAllDirectory($almacen);
        } else {
            echo json_encode([
                'Validacion' => 'Error',
                'Respuesta' => ['mensaje' => 'SE REQUIERE: almacen']
            ]);
        }
    }

    // ====================================
    // MANEJADOR DE ARCHIVOS
    // ====================================

    else if ($metodo === 'addFile' && $permiso === 'agregar_archivo') {
        $almacen = trim($_POST['almacen'] ?? '');
        $categoria = trim($_POST['categoria'] ?? '');

        if (!isset($_FILES['file'])) {
            echo json_encode([
                'Validacion' => 'Error',
                'Respuesta' => ['mensaje' => 'SE REQUIERE: file']
            ]);
            return;
        }

        if ($almacen && $categoria) {
            addFile($almacen, $categoria, $_FILES['file']);
        } else {
            echo json_encode([
                'Validacion' => 'Error',
                'Respuesta' => ['mensaje' => 'SE REQUIERE: almacen, categoria, file']
            ]);
        }
    }

    else if ($metodo === 'updateFile' && $permiso === 'actualizar_archivo') {
        $almacen = trim($_POST['almacen'] ?? '');
        $categoria = trim($_POST['categoria'] ?? '');
        $identificador = trim($_POST['identificador'] ?? '');
        $extension = trim($_POST['extension'] ?? '');

        if (!isset($_FILES['file'])) {
            echo json_encode([
                'Validacion' => 'Error',
                'Respuesta' => ['mensaje' => 'SE REQUIERE: file']
            ]);
            return;
        }

        if ($almacen && $categoria && $identificador && $extension) {
            updateFile(
                $almacen,
                $categoria,
                $identificador,
                $extension,
                $_FILES['file']
            );
        } else {
            echo json_encode([
                'Validacion' => 'Error',
                'Respuesta' => [
                    'mensaje' => 'SE REQUIERE: almacen, categoria, identificador, extension, file'
                ]
            ]);
        }
    }

    else if ($metodo === 'deleteFileById' && $permiso === 'eliminar_archivo') {
        $almacen = trim($_POST['almacen'] ?? '');
        $categoria = trim($_POST['categoria'] ?? '');
        $identificador = trim($_POST['identificador'] ?? '');
        $extension = trim($_POST['extension'] ?? '');

        if ($almacen && $categoria && $identificador && $extension) {
            deleteFileById($almacen, $categoria, $identificador, $extension);
        } else {
            echo json_encode([
                'Validacion' => 'Error',
                'Respuesta' => ['mensaje' => 'SE REQUIERE: almacen, categoria, identificador, extension']
            ]);
        }
    }

    else if ($metodo === 'getAllFiles' && $permiso === 'obtener_archivos') {
        $almacen = trim($_POST['almacen'] ?? '');
        $categoria = trim($_POST['categoria'] ?? '');

        if ($almacen && $categoria) {
            getAllFiles($almacen, $categoria);
        } else {
            echo json_encode([
                'Validacion' => 'Error',
                'Respuesta' => ['mensaje' => 'SE REQUIERE: almacen, categoria']
            ]);
        }
    }

    else if ($metodo === 'getFileById' && $permiso === 'obtener_archivo') {
        $almacen = trim($_POST['almacen'] ?? '');
        $categoria = trim($_POST['categoria'] ?? '');
        $identificador = trim($_POST['identificador'] ?? '');
        $extension = trim($_POST['extension'] ?? '');

        if ($almacen && $categoria && $identificador && $extension) {
            getFileById($almacen, $categoria, $identificador, $extension);
        } else {
            echo json_encode([
                'Validacion' => 'Error',
                'Respuesta' => ['mensaje' => 'SE REQUIERE: almacen, categoria, identificador, extension']
            ]);
        }
    }

    else {
        echo json_encode([
            'Validacion' => 'Error',
            'Respuesta' => ['mensaje' => 'SU METODO NO EXISTE O NO TIENES PERMISO PARA ESTA ACCION']
        ]);
    }
}

ejecutador();
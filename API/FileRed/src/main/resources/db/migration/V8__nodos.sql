CREATE TABLE Nodos (
    nodoId BIGINT NOT NULL AUTO_INCREMENT,
    nodoUuid VARCHAR(36) NOT NULL,
    clienteId BIGINT NOT NULL,
    nombre VARCHAR(80) NOT NULL,
    codigoHash VARCHAR(64) NULL,
    codigoExpira DATETIME(3) NULL,
    tokenHash VARCHAR(64) NULL,
    equipo VARCHAR(120) NULL,
    plataforma VARCHAR(120) NULL,
    versionPrograma VARCHAR(20) NULL,
    carpeta TEXT NULL,
    espacioTotal BIGINT NULL,
    espacioLibre BIGINT NULL,
    ultimaConexion DATETIME(3) NULL,
    fechaVinculacion DATETIME(3) NULL,
    usuarioCreacionUuid TEXT NULL,
    fechaCreacion DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    fechaModificacion DATETIME(3) NULL,
    usuarioModificacionUuid TEXT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'Activo',
    PRIMARY KEY (nodoId),
    UNIQUE KEY uk_nodos_uuid (nodoUuid),
    UNIQUE KEY uk_nodos_token (tokenHash),
    UNIQUE KEY uk_nodos_codigo (codigoHash),
    KEY idx_nodos_cliente (clienteId),
    CONSTRAINT fk_nodos_cliente FOREIGN KEY (clienteId) REFERENCES Clientes(clienteId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE NodosDirectorios (
    nodoDirectorioId BIGINT NOT NULL AUTO_INCREMENT,
    nodoId BIGINT NOT NULL,
    almacen VARCHAR(128) NOT NULL,
    categoria VARCHAR(128) NOT NULL DEFAULT '',
    fechaCreacion DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (nodoDirectorioId),
    UNIQUE KEY uk_nodos_directorios (nodoId, almacen, categoria),
    CONSTRAINT fk_nodos_directorios_nodo FOREIGN KEY (nodoId) REFERENCES Nodos(nodoId) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE NodosTareas (
    nodoTareaId BIGINT NOT NULL AUTO_INCREMENT,
    nodoId BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    archivoId BIGINT NULL,
    ruta TEXT NULL,
    destino TEXT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'Pendiente',
    intentos INT NOT NULL DEFAULT 0,
    fechaEnvio DATETIME(3) NULL,
    fechaFin DATETIME(3) NULL,
    error TEXT NULL,
    fechaCreacion DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (nodoTareaId),
    KEY idx_nodos_tareas_estado (nodoId, estado, nodoTareaId),
    CONSTRAINT fk_nodos_tareas_nodo FOREIGN KEY (nodoId) REFERENCES Nodos(nodoId) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE Clientes ADD COLUMN nodoPredeterminadoId BIGINT NULL;

ALTER TABLE Archivos
    ADD COLUMN nodoId BIGINT NULL,
    ADD COLUMN estadoNodo VARCHAR(20) NULL,
    ADD COLUMN hashSha256 VARCHAR(64) NULL,
    ADD KEY idx_archivos_nodo (nodoId, almacen(100), categoria(100));

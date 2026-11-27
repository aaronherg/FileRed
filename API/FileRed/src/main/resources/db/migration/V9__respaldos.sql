ALTER TABLE Nodos ADD COLUMN rol VARCHAR(20) NOT NULL DEFAULT 'Almacenamiento';

ALTER TABLE Planes
    ADD COLUMN permitirNodos BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN permitirRespaldos BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE RespaldosOrigenes (
    respaldoOrigenId BIGINT NOT NULL AUTO_INCREMENT,
    respaldoId BIGINT NOT NULL,
    origenNodoId BIGINT NOT NULL,
    fechaCreacion DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (respaldoOrigenId),
    UNIQUE KEY uk_respaldos_origenes (respaldoId, origenNodoId),
    KEY idx_respaldos_origen (origenNodoId),
    CONSTRAINT fk_respaldos_origenes_nodo FOREIGN KEY (respaldoId) REFERENCES Nodos(nodoId) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ArchivosRespaldos (
    archivoRespaldoId BIGINT NOT NULL AUTO_INCREMENT,
    archivoId BIGINT NOT NULL,
    respaldoId BIGINT NOT NULL,
    hashSha256 VARCHAR(64) NULL,
    fechaCopia DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (archivoRespaldoId),
    UNIQUE KEY uk_archivos_respaldos (archivoId, respaldoId),
    KEY idx_archivos_respaldos_respaldo (respaldoId),
    CONSTRAINT fk_archivos_respaldos_archivo FOREIGN KEY (archivoId) REFERENCES Archivos(archivoId) ON DELETE CASCADE,
    CONSTRAINT fk_archivos_respaldos_nodo FOREIGN KEY (respaldoId) REFERENCES Nodos(nodoId) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

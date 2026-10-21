-- EIF206 - Proyecto 02. Esquema inicial MySQL 8.4.
-- Correspondencia: database/modelo.dbml.
-- No elimina tablas ni datos existentes. IF NOT EXISTS no actualiza esquemas anteriores.

CREATE DATABASE IF NOT EXISTS sistema_reservas
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE sistema_reservas;

CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario VARCHAR(20) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    clave_hash VARCHAR(255) NOT NULL,
    rol ENUM('ADMIN', 'FUNCIONARIO') NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id_usuario)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS funcionarios (
    id_usuario VARCHAR(20) NOT NULL,
    telefono VARCHAR(25) NOT NULL,
    PRIMARY KEY (id_usuario),
    CONSTRAINT fk_funcionario_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios (id_usuario) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS categorias (
    id_categoria INT NOT NULL AUTO_INCREMENT,
    descripcion VARCHAR(150) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id_categoria)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS recursos (
    id_recurso VARCHAR(30) NOT NULL,
    id_categoria INT NOT NULL,
    descripcion VARCHAR(200) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id_recurso),
    INDEX idx_recurso_categoria (id_categoria),
    CONSTRAINT fk_recurso_categoria FOREIGN KEY (id_categoria)
        REFERENCES categorias (id_categoria) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reservas (
    id_reserva INT NOT NULL AUTO_INCREMENT,
    id_funcionario VARCHAR(20) NOT NULL,
    actividad VARCHAR(255) NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    estado ENUM('ACTIVA', 'CANCELADA') NOT NULL DEFAULT 'ACTIVA',
    PRIMARY KEY (id_reserva),
    INDEX idx_reserva_funcionario (id_funcionario),
    INDEX idx_reserva_fecha (fecha),
    CONSTRAINT chk_reserva_horario CHECK (hora_inicio < hora_fin),
    CONSTRAINT fk_reserva_funcionario FOREIGN KEY (id_funcionario)
        REFERENCES funcionarios (id_usuario) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reserva_recurso (
    id_reserva INT NOT NULL,
    id_recurso VARCHAR(30) NOT NULL,
    PRIMARY KEY (id_reserva, id_recurso),
    INDEX idx_asignacion_recurso (id_recurso),
    CONSTRAINT fk_asignacion_reserva FOREIGN KEY (id_reserva)
        REFERENCES reservas (id_reserva) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_asignacion_recurso FOREIGN KEY (id_recurso)
        REFERENCES recursos (id_recurso) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

-- Reglas de negocio a cargo del backend:
-- * Coherencia entre rol FUNCIONARIO y su perfil.
-- * Una o mas asignaciones por reserva, un recurso por categoria solicitada.
-- * Disponibilidad sin traslapes mediante transaccion y bloqueos.
-- * Fecha y horario futuros; cancelacion solo por el propietario y antes del inicio.
-- * Bajas logicas, gestionando previamente las reservas futuras afectadas.
-- * No cambiar la categoria de recursos con reservas historicas.

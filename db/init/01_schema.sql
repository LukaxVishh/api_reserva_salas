CREATE DATABASE IF NOT EXISTS salalivre
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE salalivre;

CREATE TABLE IF NOT EXISTS salas (
    id           INT PRIMARY KEY AUTO_INCREMENT,
    nome         VARCHAR(100) NOT NULL,
    bloco        VARCHAR(50)  NOT NULL,
    capacidade   INT          NOT NULL,
    tem_projetor BOOLEAN      NOT NULL,
    ativa        BOOLEAN      NOT NULL
);

CREATE TABLE IF NOT EXISTS reservas (
    id               INT PRIMARY KEY AUTO_INCREMENT,
    nome_responsavel VARCHAR(100) NOT NULL,
    descricao        VARCHAR(255) NOT NULL,
    data_reserva     DATE         NOT NULL,
    hora_inicio      TIME         NOT NULL,
    hora_fim         TIME         NOT NULL,
    status           VARCHAR(30)  NOT NULL,
    sala_id          INT          NOT NULL,
    CONSTRAINT fk_reserva_sala FOREIGN KEY (sala_id) REFERENCES salas(id)
);

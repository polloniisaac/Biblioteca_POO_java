-- Biblioteca Comunitária Vila Leitura — MySQL 8.0.16+
-- Opcional: o sistema cria o banco e as tabelas ao abrir (cada DAO faz CREATE TABLE IF NOT EXISTS).

CREATE DATABASE IF NOT EXISTS biblioteca_vila_leitura
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
SET NAMES utf8mb4;
USE biblioteca_vila_leitura;

-- ObraDAO ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS obra (
    codigo INT NOT NULL,
    titulo VARCHAR(150) NOT NULL,
    autor VARCHAR(150) NOT NULL,
    categoria TINYINT NOT NULL,            -- 1 Literatura .. 5 Outros
    ano_publicacao SMALLINT NOT NULL,
    CONSTRAINT pk_obra PRIMARY KEY (codigo),
    CONSTRAINT ck_obra_codigo CHECK (codigo BETWEEN 100 AND 9999),      -- RN01
    CONSTRAINT ck_obra_titulo CHECK (TRIM(titulo) <> ''),               -- RN02
    CONSTRAINT ck_obra_autor CHECK (TRIM(autor) <> ''),
    CONSTRAINT ck_obra_categoria CHECK (categoria BETWEEN 1 AND 5),     -- RN03
    CONSTRAINT ck_obra_ano CHECK (ano_publicacao >= 1450)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- CopiaDAO -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS copia (
    codigo INT NOT NULL AUTO_INCREMENT,
    obra_codigo INT NOT NULL,
    numero INT NOT NULL,                   -- 1, 2, 3... dentro da obra (etiqueta "101-2")
    situacao VARCHAR(20) NOT NULL DEFAULT 'DISPONIVEL',
    CONSTRAINT pk_copia PRIMARY KEY (codigo),
    CONSTRAINT fk_copia_obra FOREIGN KEY (obra_codigo) REFERENCES obra (codigo),
    CONSTRAINT uk_copia_obra_numero UNIQUE (obra_codigo, numero),
    CONSTRAINT ck_copia_numero CHECK (numero >= 1),
    CONSTRAINT ck_copia_situacao CHECK (situacao IN ('DISPONIVEL','EMPRESTADA','RESERVADA'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- LeitorDAO ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS leitor (
    codigo INT NOT NULL,
    nome VARCHAR(100) NOT NULL,
    tipo TINYINT NOT NULL,                 -- 1 Estudante, 2 Professor, 3 Comunidade
    CONSTRAINT pk_leitor PRIMARY KEY (codigo),
    CONSTRAINT ck_leitor_codigo CHECK (codigo BETWEEN 1 AND 9999),      -- RN05
    CONSTRAINT ck_leitor_nome CHECK (TRIM(nome) <> ''),
    CONSTRAINT ck_leitor_tipo CHECK (tipo BETWEEN 1 AND 3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- FuncionarioDAO -----------------------------------------------------
CREATE TABLE IF NOT EXISTS funcionario (
    codigo INT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    cargo TINYINT NOT NULL,                -- 1 Bibliotecário, 2 Voluntário
    login VARCHAR(30) NOT NULL,
    senha_hash CHAR(64) NOT NULL,          -- SHA-256 da senha
    CONSTRAINT pk_funcionario PRIMARY KEY (codigo),
    CONSTRAINT uk_funcionario_login UNIQUE (login),
    CONSTRAINT ck_funcionario_nome CHECK (TRIM(nome) <> ''),
    CONSTRAINT ck_funcionario_cargo CHECK (cargo IN (1, 2))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- EmprestimoDAO ------------------------------------------------------
CREATE TABLE IF NOT EXISTS emprestimo (
    codigo INT NOT NULL AUTO_INCREMENT,
    copia_codigo INT NOT NULL,
    leitor_codigo INT NOT NULL,
    funcionario_emprestimo INT NOT NULL,
    funcionario_devolucao INT NULL,
    data_emprestimo DATE NOT NULL,
    data_prevista DATE NOT NULL,           -- data_emprestimo + prazo do tipo do leitor (RN10)
    data_devolucao DATE NULL,              -- NULL = em aberto
    multa DECIMAL(8,2) NOT NULL DEFAULT 0.00,
    -- coluna calculada: só tem valor enquanto o empréstimo está aberto
    copia_aberta INT AS (IF(data_devolucao IS NULL, copia_codigo, NULL)) STORED,
    CONSTRAINT pk_emprestimo PRIMARY KEY (codigo),
    CONSTRAINT fk_emp_copia FOREIGN KEY (copia_codigo) REFERENCES copia (codigo),
    CONSTRAINT fk_emp_leitor FOREIGN KEY (leitor_codigo) REFERENCES leitor (codigo),
    CONSTRAINT fk_emp_func_e FOREIGN KEY (funcionario_emprestimo) REFERENCES funcionario (codigo),
    CONSTRAINT fk_emp_func_d FOREIGN KEY (funcionario_devolucao) REFERENCES funcionario (codigo),
    CONSTRAINT ck_emp_prevista CHECK (data_prevista > data_emprestimo),
    CONSTRAINT ck_emp_devolucao CHECK (data_devolucao IS NULL OR data_devolucao >= data_emprestimo), -- RN11
    CONSTRAINT ck_emp_multa CHECK (multa >= 0),
    CONSTRAINT uk_emp_copia_aberta UNIQUE (copia_aberta),   -- uma cópia nunca tem 2 empréstimos abertos
    INDEX idx_emp_leitor_aberto (leitor_codigo, data_devolucao)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ReservaDAO ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS reserva (
    codigo INT NOT NULL AUTO_INCREMENT,
    obra_codigo INT NOT NULL,
    leitor_codigo INT NOT NULL,
    funcionario_codigo INT NOT NULL,
    data_reserva DATE NOT NULL,
    copia_codigo INT NULL,                 -- cópia separada (situação AGUARDANDO)
    data_limite_retirada DATE NULL,        -- separação + 3 dias
    emprestimo_codigo INT NULL,            -- preenchido quando ATENDIDA
    situacao VARCHAR(20) NOT NULL DEFAULT 'ATIVA',
    viva_leitor INT AS (IF(situacao IN ('ATIVA','AGUARDANDO'), leitor_codigo, NULL)) STORED,
    viva_obra INT AS (IF(situacao IN ('ATIVA','AGUARDANDO'), obra_codigo, NULL)) STORED,
    CONSTRAINT pk_reserva PRIMARY KEY (codigo),
    CONSTRAINT fk_res_obra FOREIGN KEY (obra_codigo) REFERENCES obra (codigo),
    CONSTRAINT fk_res_leitor FOREIGN KEY (leitor_codigo) REFERENCES leitor (codigo),
    CONSTRAINT fk_res_func FOREIGN KEY (funcionario_codigo) REFERENCES funcionario (codigo),
    CONSTRAINT fk_res_copia FOREIGN KEY (copia_codigo) REFERENCES copia (codigo),
    CONSTRAINT fk_res_emp FOREIGN KEY (emprestimo_codigo) REFERENCES emprestimo (codigo),
    CONSTRAINT ck_res_situacao CHECK (situacao IN ('ATIVA','AGUARDANDO','ATENDIDA','CANCELADA','EXPIRADA')),
    CONSTRAINT uk_res_viva UNIQUE (viva_leitor, viva_obra), -- 1 reserva viva por leitor e obra
    INDEX idx_res_fila (obra_codigo, situacao, codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

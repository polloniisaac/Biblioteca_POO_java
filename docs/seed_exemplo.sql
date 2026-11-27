-- Dados de exemplo para a apresentação (rodar num banco vazio, depois de abrir o sistema uma vez).
-- Cria também o usuário "voluntario" (senha "1234").
SET NAMES utf8mb4;
USE biblioteca_vila_leitura;

INSERT IGNORE INTO funcionario (codigo, nome, cargo, login, senha_hash) VALUES
    (1, 'Administrador', 1, 'admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918');
INSERT IGNORE INTO funcionario (nome, cargo, login, senha_hash) VALUES
    ('Voluntário de Exemplo', 2, 'voluntario', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4');

INSERT INTO obra (codigo, titulo, autor, categoria, ano_publicacao) VALUES
    (101, 'Dom Casmurro', 'Machado de Assis', 1, 1899),
    (102, 'Memórias Póstumas de Brás Cubas', 'Machado de Assis', 1, 1881),
    (103, 'O Cortiço', 'Aluísio Azevedo', 1, 1890),
    (104, 'Vidas Secas', 'Graciliano Ramos', 1, 1938),
    (201, 'Matemática: Ciência e Aplicações', 'Gelson Iezzi', 2, 2016),
    (202, 'História Geral e do Brasil', 'Cláudio Vicentino', 2, 2014),
    (203, 'Geografia Geral e do Brasil', 'Eustáquio de Sene', 2, 2013),
    (205, 'Java: Como Programar', 'Deitel', 3, 2016),
    (301, 'Algoritmos: Teoria e Prática', 'Thomas H. Cormen', 3, 2012),
    (302, 'Use a Cabeça! Java', 'Kathy Sierra', 3, 2009),
    (310, 'O Pequeno Príncipe', 'Antoine de Saint-Exupéry', 4, 1943),
    (401, 'Harry Potter e a Pedra Filosofal', 'J. K. Rowling', 4, 2000),
    (402, 'O Menino Maluquinho', 'Ziraldo', 4, 1980),
    (501, 'Guia Prático de Horta Caseira', 'Associação Vila Leitura', 5, 2020),
    (502, 'Receitas de Família', 'Comunidade Vila Leitura', 5, 2018);

INSERT INTO copia (codigo, obra_codigo, numero, situacao) VALUES
    (1, 101, 1, 'DISPONIVEL'),
    (2, 101, 2, 'DISPONIVEL'),
    (3, 101, 3, 'DISPONIVEL'),
    (4, 102, 1, 'DISPONIVEL'),
    (5, 102, 2, 'DISPONIVEL'),
    (6, 103, 1, 'DISPONIVEL'),
    (7, 103, 2, 'DISPONIVEL'),
    (8, 104, 1, 'DISPONIVEL'),
    (9, 104, 2, 'DISPONIVEL'),
    (10, 201, 1, 'DISPONIVEL'),
    (11, 201, 2, 'DISPONIVEL'),
    (12, 201, 3, 'DISPONIVEL'),
    (13, 202, 1, 'DISPONIVEL'),
    (14, 202, 2, 'DISPONIVEL'),
    (15, 203, 1, 'DISPONIVEL'),
    (16, 205, 1, 'DISPONIVEL'),
    (17, 301, 1, 'DISPONIVEL'),
    (18, 302, 1, 'DISPONIVEL'),
    (19, 302, 2, 'DISPONIVEL'),
    (20, 310, 1, 'DISPONIVEL'),
    (21, 310, 2, 'DISPONIVEL'),
    (22, 401, 1, 'DISPONIVEL'),
    (23, 401, 2, 'DISPONIVEL'),
    (24, 402, 1, 'DISPONIVEL'),
    (25, 402, 2, 'DISPONIVEL'),
    (26, 501, 1, 'DISPONIVEL'),
    (27, 502, 1, 'DISPONIVEL');

INSERT INTO leitor (codigo, nome, tipo) VALUES
    (1, 'Lucas Pereira', 1),
    (2, 'Helena Costa', 2),
    (3, 'Antônio Ramos', 3),
    (4, 'Beatriz Lima', 1),
    (5, 'Carlos Souza', 1),
    (6, 'Fernanda Alves', 2),
    (7, 'João Batista', 3),
    (8, 'Mariana Rocha', 1);

INSERT INTO emprestimo (codigo, copia_codigo, leitor_codigo, funcionario_emprestimo, funcionario_devolucao,
                        data_emprestimo, data_prevista, data_devolucao, multa) VALUES
    (1, 1, 1, 1, 1, DATE_SUB(CURDATE(), INTERVAL 60 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 60 DAY), INTERVAL 7 DAY), DATE_SUB(CURDATE(), INTERVAL 55 DAY), 0.00),
    (2, 10, 2, 1, 1, DATE_SUB(CURDATE(), INTERVAL 58 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 58 DAY), INTERVAL 14 DAY), DATE_SUB(CURDATE(), INTERVAL 45 DAY), 0.00),
    (3, 20, 4, 1, 1, DATE_SUB(CURDATE(), INTERVAL 50 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 50 DAY), INTERVAL 7 DAY), DATE_SUB(CURDATE(), INTERVAL 43 DAY), 0.00),
    (4, 2, 1, 1, 1, DATE_SUB(CURDATE(), INTERVAL 40 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 40 DAY), INTERVAL 7 DAY), DATE_SUB(CURDATE(), INTERVAL 30 DAY), 1.50),
    (5, 16, 3, 1, 1, DATE_SUB(CURDATE(), INTERVAL 45 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 45 DAY), INTERVAL 7 DAY), DATE_SUB(CURDATE(), INTERVAL 30 DAY), 4.00),
    (6, 24, 5, 1, 1, DATE_SUB(CURDATE(), INTERVAL 35 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 35 DAY), INTERVAL 7 DAY), DATE_SUB(CURDATE(), INTERVAL 26 DAY), 1.00),
    (7, 1, 2, 1, NULL, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 14 DAY), NULL, 0.00),
    (8, 18, 6, 1, NULL, DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 14 DAY), NULL, 0.00),
    (9, 22, 8, 1, NULL, DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 7 DAY), NULL, 0.00),
    (10, 16, 3, 1, NULL, DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 20 DAY), INTERVAL 7 DAY), NULL, 0.00),
    (11, 20, 1, 1, NULL, DATE_SUB(CURDATE(), INTERVAL 12 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 12 DAY), INTERVAL 7 DAY), NULL, 0.00),
    (12, 8, 7, 1, NULL, DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 15 DAY), INTERVAL 7 DAY), NULL, 0.00);

-- cópias dos empréstimos em aberto ficam EMPRESTADA
UPDATE copia SET situacao = 'EMPRESTADA'
 WHERE codigo IN (SELECT copia_codigo FROM emprestimo WHERE data_devolucao IS NULL);

-- Helena (Professor) entra na fila do livro 205, que está emprestado e atrasado com Antônio
INSERT INTO reserva (obra_codigo, leitor_codigo, funcionario_codigo, data_reserva, situacao)
VALUES (205, 2, 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'ATIVA');

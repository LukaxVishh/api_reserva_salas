-- USE salalivre;

-- INSERT INTO salas (nome, bloco, capacidade, tem_projetor, ativa) VALUES
--   ('Sala 101',   'Bloco A', 30, TRUE,  TRUE),
--   ('Sala 102',   'Bloco A', 20, FALSE, TRUE),
--   ('Sala Lab 1', 'Bloco B', 25, TRUE,  FALSE);


USE salalivre;

INSERT INTO salas (
    nome,
    bloco,
    capacidade,
    tem_projetor,
    ativa,
    cep,
    logradouro,
    bairro,
    cidade,
    uf,
    numero,
    complemento
) VALUES
  (
    'Sala 101',
    'Bloco A',
    30,
    TRUE,
    TRUE,
    '01001000',
    'Praça da Sé',
    'Sé',
    'São Paulo',
    'SP',
    '100',
    '1º andar'
  ),
  (
    'Sala 102',
    'Bloco A',
    20,
    FALSE,
    TRUE,
    '01310930',
    'Avenida Paulista',
    'Bela Vista',
    'São Paulo',
    'SP',
    '1000',
    '2º andar'
  ),
  (
    'Sala Lab 1',
    'Bloco B',
    25,
    TRUE,
    FALSE,
    '20040002',
    'Rua da Quitanda',
    'Centro',
    'Rio de Janeiro',
    'RJ',
    '50',
    'Laboratório'
  );
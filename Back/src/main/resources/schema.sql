    -- ===================================================================
-- Esquema de Banco de Dados E-Commerce TDD (PostgreSQL)
-- ===================================================================

-- 1. Tabela de Usuários com Endereço Detalhado
CREATE TABLE IF NOT EXISTS usuarios (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    sobrenome VARCHAR(100) NOT NULL DEFAULT '',
    email VARCHAR(150) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    cpf VARCHAR(14) NOT NULL DEFAULT '',
    cep VARCHAR(10) NOT NULL DEFAULT '',
    endereco VARCHAR(200) NOT NULL DEFAULT '',
    numero VARCHAR(20) NOT NULL DEFAULT '',
    bairro VARCHAR(100) NOT NULL DEFAULT '',
    cidade VARCHAR(100) NOT NULL DEFAULT '',
    estado VARCHAR(50) NOT NULL DEFAULT '',
    pais VARCHAR(50) NOT NULL DEFAULT 'Brasil',
    tipo_usuario INT NOT NULL DEFAULT 1, -- 0: Vendedor, 1: Cliente, 2: Vendedor/Cliente
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Garantir adição das colunas detalhadas caso a tabela já exista:
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS sobrenome VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS cpf VARCHAR(14) NOT NULL DEFAULT '';
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS cep VARCHAR(10) NOT NULL DEFAULT '';
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS endereco VARCHAR(200) NOT NULL DEFAULT '';
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS numero VARCHAR(20) NOT NULL DEFAULT '';
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS bairro VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS cidade VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS estado VARCHAR(50) NOT NULL DEFAULT '';
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS pais VARCHAR(50) NOT NULL DEFAULT 'Brasil';
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS tipo_usuario INT NOT NULL DEFAULT 1;

-- 2. Tabela de Lojas com Endereço Detalhado
CREATE TABLE IF NOT EXISTS lojas (
    id SERIAL PRIMARY KEY,
    usuario_id INT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    nome_loja VARCHAR(150) NOT NULL,
    email_loja VARCHAR(150) NOT NULL,
    cnpj VARCHAR(18),
    cep_loja VARCHAR(10) NOT NULL DEFAULT '',
    endereco_loja VARCHAR(200) NOT NULL DEFAULT '',
    numero_loja VARCHAR(20) NOT NULL DEFAULT '',
    bairro_loja VARCHAR(100) NOT NULL DEFAULT '',
    cidade_loja VARCHAR(100) NOT NULL DEFAULT '',
    estado_loja VARCHAR(50) NOT NULL DEFAULT '',
    pais_loja VARCHAR(50) NOT NULL DEFAULT 'Brasil',
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Garantir adição das colunas detalhadas na tabela lojas caso já exista:
ALTER TABLE lojas ADD COLUMN IF NOT EXISTS cep_loja VARCHAR(10) NOT NULL DEFAULT '';
ALTER TABLE lojas ADD COLUMN IF NOT EXISTS endereco_loja VARCHAR(200) NOT NULL DEFAULT '';
ALTER TABLE lojas ADD COLUMN IF NOT EXISTS numero_loja VARCHAR(20) NOT NULL DEFAULT '';
ALTER TABLE lojas ADD COLUMN IF NOT EXISTS bairro_loja VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE lojas ADD COLUMN IF NOT EXISTS cidade_loja VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE lojas ADD COLUMN IF NOT EXISTS estado_loja VARCHAR(50) NOT NULL DEFAULT '';
ALTER TABLE lojas ADD COLUMN IF NOT EXISTS pais_loja VARCHAR(50) NOT NULL DEFAULT 'Brasil';

-- 3. Tabela de Produtos
CREATE TABLE IF NOT EXISTS produtos (
    id SERIAL PRIMARY KEY,
    loja_id INT REFERENCES lojas(id) ON DELETE SET NULL,
    usuario_id INT REFERENCES usuarios(id) ON DELETE CASCADE,
    nome VARCHAR(150) NOT NULL,
    preco NUMERIC(10, 2) NOT NULL,
    estoque INTEGER NOT NULL DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE produtos ADD COLUMN IF NOT EXISTS loja_id INT REFERENCES lojas(id) ON DELETE SET NULL;
ALTER TABLE produtos ADD COLUMN IF NOT EXISTS usuario_id INT REFERENCES usuarios(id) ON DELETE CASCADE;
ALTER TABLE produtos ADD COLUMN IF NOT EXISTS ativo BOOLEAN NOT NULL DEFAULT TRUE;

-- 4. Tabela de Pedidos
CREATE TABLE IF NOT EXISTS pedidos (
    id SERIAL PRIMARY KEY,
    usuario_id INT REFERENCES usuarios(id),
    total NUMERIC(10, 2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDENTE',
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Tabela de Itens do Pedido
CREATE TABLE IF NOT EXISTS itens_pedido (
    id SERIAL PRIMARY KEY,
    pedido_id INT REFERENCES pedidos(id) ON DELETE CASCADE,
    produto_id INT REFERENCES produtos(id),
    quantidade INTEGER NOT NULL,
    preco_unitario NUMERIC(10, 2) NOT NULL
);

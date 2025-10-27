CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS test (
    id bigserial PRIMARY KEY,
    embedding vector(3) -- Exemplo de coluna de vetor com 3 dimensões
);
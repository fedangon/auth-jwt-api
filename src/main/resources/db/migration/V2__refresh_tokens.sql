-- Adiciona tabela de refresh tokens para instalacoes que ja aplicaram a V1

create table if not exists refresh_tokens (
    id uuid primary key,
    user_id uuid not null references users(id) on delete cascade,
    token_hash char(64) not null unique,
    expires_at timestamptz not null,
    created_at timestamptz not null,
    revoked_at timestamptz
);

create index if not exists idx_refresh_tokens_user_id on refresh_tokens(user_id);


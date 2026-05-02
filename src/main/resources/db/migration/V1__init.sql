-- Criacao inicial das tabelas de autenticacao (usuarios e refresh tokens)

create table users (
    id uuid primary key,
    email varchar(320) not null unique,
    password_hash varchar(72) not null,
    full_name varchar(150),
    created_at timestamptz not null
);

create table user_roles (
    user_id uuid not null references users(id) on delete cascade,
    role varchar(50) not null,
    primary key (user_id, role)
);

create table refresh_tokens (
    id uuid primary key,
    user_id uuid not null references users(id) on delete cascade,
    token_hash char(64) not null unique,
    expires_at timestamptz not null,
    created_at timestamptz not null,
    revoked_at timestamptz
);

create index idx_refresh_tokens_user_id on refresh_tokens(user_id);


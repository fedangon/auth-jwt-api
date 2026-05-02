-- Ajusta o tipo do hash para varchar(64) (melhor compatibilidade com validacao do Hibernate)

alter table refresh_tokens
    alter column token_hash type varchar(64);


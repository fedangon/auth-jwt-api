# Auth JWT API

API de autenticação com JWT (access token) e refresh token, feita com Java 21, Spring Boot, Maven e PostgreSQL.

## Stack

- Java 21
- Spring Boot
- Maven
- PostgreSQL
- Spring Security (Resource Server JWT)
- Flyway (migrations)

## Endpoints

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/users/me` (requer `Authorization: Bearer <accessToken>`)

## Variaveis de ambiente

- `DB_URL` (default: `jdbc:postgresql://localhost:5432/auth_jwt_api`)
- `DB_USERNAME` (default: `postgres`)
- `DB_PASSWORD` (default: `postgres`)
- `JWT_SECRET_BASE64` (Base64 de pelo menos 32 bytes para HS256)
- `JWT_ISSUER` (default: `https://auth-jwt-api`)
- `JWT_ACCESS_TTL` (default: `PT15M`)
- `JWT_CLOCK_SKEW` (default: `PT30S`)
- `REFRESH_TOKEN_TTL` (default: `P7D`)

## Como executar

1. Suba o PostgreSQL (exemplo em `docker-compose.yml`).
2. Rode a aplicação:
   - `./mvnw spring-boot:run`

As migrations do Flyway rodam automaticamente na inicialização.

## Decisões de segurança

- Access token: JWT assinado com HS256, validado via Resource Server.
- Refresh token: valor aleatório (nao-JWT) armazenado no banco somente como hash SHA-256.
- Rotação de refresh token: ao chamar `/refresh`, o token anterior e revogado e um novo e emitido.

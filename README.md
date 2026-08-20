# Taverna RPG — Backend

API REST para gerenciamento de campanhas de RPG de mesa: campanhas, personagens, raças, classes, habilidades/magias, equipamentos, sessões e status de combate. Autenticação via JWT (RSA), autorização por role (`ADMIN`/`PLAYER`) e por posse de campanha.

Frontend correspondente: [`taverna-rpg-forge`](../taverna-rpg-forge).

## Stack

- Java 17, Spring Boot 3.5.11
- Spring Security (OAuth2 Resource Server / JWT assinado com par de chaves RSA)
- Spring Data JPA + H2 (perfil `local`, em memória)
- Maven, JUnit 5 + Mockito + MockMvc (193 testes)

## Rodando localmente

```bash
./mvnw spring-boot:run
```

Sobe em `http://localhost:8081` com o profile `local` (ativo por padrão). Ao iniciar, um usuário admin é criado automaticamente se não existir:

```
username: admin
password: admin123
```

O banco H2 é **em memória** — todos os dados são perdidos a cada restart, exceto o usuário admin (recriado automaticamente).

### Rodar os testes

```bash
./mvnw test
```

### Chaves JWT

`src/main/resources/private.key` / `public.key` (RSA, PKCS8/X.509) assinam e validam os tokens. Não são versionadas (`.gitignore`) — gere um novo par se não existirem:

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.key
openssl rsa -pubout -in private.key -out public.key
```

## Modelo de permissões

| Quem | O que pode fazer |
|---|---|
| **ADMIN** | Tudo, incluindo conteúdo global (raças/classes/habilidades/equipamentos sem `campaignId`) |
| **Mestre de uma campanha** (`campaign.masterId`) | Gerencia tudo *daquela campanha*: cria/edita/exclui raças, classes, habilidades, equipamentos e sessões com `campaignId` apontando pra ela; gerencia personagens dos jogadores na campanha |
| **PLAYER** (dono do personagem) | Cria/edita seus próprios personagens; concede a si mesmo itens/habilidades já existentes; vê apenas as campanhas em que está |

Importante: **"mestre" não é uma role de conta** — é só quem criou a campanha (`campaign.masterId`). Qualquer conta (sempre criada como `PLAYER`) pode criar uma campanha e virar mestre *dela*, continuando `PLAYER` em todo o resto do sistema. `ADMIN` não é auto-atribuível no cadastro.

## Endpoints principais

Todos exigem `Authorization: Bearer <token>` exceto onde indicado.

### Auth & usuários
| Método | Rota | Observação |
|---|---|---|
| POST | `/api/auth/authentication` | Login — `{login, password}` → JWT. Sem auth. |
| POST | `/users` | Cadastro — sempre cria `PLAYER`. Sem auth. |
| GET | `/users`, `/users/{id}` | — |
| PUT/DELETE | `/users/{id}` | — |

### Campanhas
| Método | Rota | Observação |
|---|---|---|
| GET | `/campaigns` | Lista as campanhas do usuário (mestre ou jogador); ADMIN vê todas |
| GET | `/campaigns/code/{inviteCode}` | Busca por código de convite |
| POST | `/campaigns/join?inviteCode=` | Entra numa campanha como jogador |
| POST | `/campaigns` ou `/campaigns/master/{id}` | Cria campanha (gera `inviteCode` único, `SecureRandom`) |
| PUT/DELETE | `/campaigns/{id}` | Mestre ou ADMIN |
| CRUD | `/campaigns/{id}/status-templates` | Templates de status (HP, mana etc.) por campanha |

### Conteúdo de jogo (raças, classes, habilidades/magias, equipamentos)
Cada um segue o mesmo padrão em `/races`, `/character-classes`, `/ability-spells`, `/equipments`:
- `GET` (lista, filtra por `?campaignId=`) e `GET /{id}` — qualquer autenticado.
- `POST` / `PUT /{id}` / `DELETE /{id}` — ADMIN (conteúdo global) ou mestre da campanha (`campaignId` no corpo/entidade).

### Personagens
| Método | Rota | Observação |
|---|---|---|
| GET | `/characters/user/{userId}` (+ `/page`) | Personagens de um usuário |
| GET | `/characters/campaign/{campaignId}` | Personagens de uma campanha (mestre/ADMIN) |
| POST | `/characters/user/{userId}` | Cria — exige `raceId`, `classId`, `role`, `gender`, `campaignId` |
| PUT/DELETE | `/characters/{id}` | Dono, mestre da campanha ou ADMIN |
| POST/DELETE | `/characters/{id}/equipments/{equipmentId}`, `/{id}/abilities/{abilityId}` | Concede/revoga item — é assim que o mestre monta o inventário/grimório de um jogador |
| GET | `/characters/{id}/validate/ability\|equipment/{id}` | Verifica se o personagem cumpre os requisitos (nível, atributos) |
| CRUD | `/characters/{characterId}/statuses` | Status de combate (HP atual etc.) do personagem |

### Sessões
| Método | Rota | Observação |
|---|---|---|
| GET | `/sessions` | **Somente ADMIN** — navegador global |
| GET | `/sessions/campaign/{campaignId}` | Sessões de uma campanha — mestre/ADMIN |
| POST/PUT/DELETE | `/sessions` | Mestre da campanha ou ADMIN |

## Estrutura

Um pacote por domínio (`Campaign`, `Character`, `Race`, ...), cada um com `Controller/`, `Service/`, `Repository/`, `DTO/`. Regras de autorização centralizadas em `security/AccessControlService.java`.

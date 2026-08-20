# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
./mvnw spring-boot:run                          # run the API on :8081 (profile "local", always active — see below)
./mvnw test                                     # run all 193 tests
./mvnw test -Dtest=RaceServiceTest               # run one test class
./mvnw test -Dtest=RaceServiceTest#testCreateRaceSuccess   # run one test method
./mvnw -q compile                               # compile only, quiet output
```

No lint/format command is configured. Java 17, Maven, Spring Boot 3.5.11.

## Architecture

### Package-per-domain, not layer-per-package

Each business concept (`Campaign`, `Character`, `CharacterClass`, `Race`, `AbilitySpell`, `Equipment`, `Session`, `StatusTemplate`, `User`, `CharacterStatus`) is its own top-level package under `com.eduardo.rpg`, each containing its own `Controller/`, `Service/`, `Repository/`, `DTO/`. Cross-cutting stuff lives in `security/`, `config/`, `exception/`, `enums/`, `common/`.

### Authorization model — this is the part that isn't obvious from any single file

Two independent axes, both enforced in `security/AccessControlService.java`, not in `@PreAuthorize` annotations (those were removed from create/update/delete on game-content controllers on purpose):

1. **Account role** (`Role`: `ADMIN` | `PLAYER`) — set at signup, always `PLAYER` (see `UserService.createUser`, hardcoded). `ADMIN` isn't self-assignable.
2. **Campaign mastery** — *not* an account role. Whoever created a `Campaign` is its `master` (a `User`). A `PLAYER` account can master any number of campaigns while being just a `PLAYER` everywhere else. `AccessControlService.isCampaignMaster(user, campaign)` is the check, used all over instead of a role check.

Game content (`Race`, `CharacterClass`, `AbilitySpell`, `Equipment`) can be **global** (`campaignId == null`, ADMIN-only) or **campaign-scoped** (`campaignId` set, writable by that campaign's master or ADMIN). The gate is `AccessControlService.requireGameContentWritePermission(user, campaignId)` — called from the *service* layer (after fetching the entity, for update/delete) with the entity's own `campaignId`, not from the controller. When adding a new campaign-scoped entity type, follow this exact pattern rather than reaching for `@PreAuthorize("hasRole('ADMIN')")`.

Sessions follow a different rule (`SessionService`, via `getMasterCampaign`/`ensureMasterOwnsCampaign`): only that campaign's master or ADMIN can create/read/update/delete a session — there's no global-session concept. `GET /sessions` (no path param) is the one ADMIN-only global listing; `GET /sessions/campaign/{id}` is the one everyone with campaign access should actually call.

### JWT: the claim value already includes the `ROLE_` prefix

`JwtService.generateToken` puts the *full* Spring authority strings (e.g. `ROLE_ADMIN`) into the `scope` claim, mirroring `UserAuth.getAuthorities()`. `SecurityConfig` registers a custom `JwtAuthenticationConverter` with an empty authority prefix specifically because Spring's default converter would re-prefix the claim with `SCOPE_`, producing `SCOPE_ROLE_ADMIN` — which never matches `hasRole("ADMIN")`. This bit backend admin auth for a while before the converter was added; don't remove it or switch back to `Customizer.withDefaults()` for the JWT config.

`@WithMockUser` in tests bypasses this entire pipeline (it injects the authority directly), so a test suite that's green does **not** prove the real JWT→authority path works — if you touch `SecurityConfig`'s JWT setup, verify against a real token (`POST /api/auth/authentication`), not just `./mvnw test`.

### Character equipment/abilities are grants, not a catalog subscription

`POST /characters/{id}/equipments/{equipmentId}` and `.../abilities/{abilityId}` attach one specific catalog item to one specific character — this is how a master hands out loot/spells to a player, and how the frontend's "my items" view (only items actually granted) differs from the campaign's full catalog view.

### Data model notes

- `campaignId` on `Race`/`CharacterClass`/`AbilitySpell`/`Equipment` is a plain `Long` column, not a JPA relation — no cascade, no join, just a filter key.
- `StatusTemplate` (per-campaign, e.g. "HP", "Sanity") + `CharacterStatus` (a character's current value against one template) is the generic resource-tracking mechanism — deliberately not hardcoded to D&D-style HP, to stay usable for other rule systems.
- `Role` enum is `ADMIN`/`PLAYER` only. There is no `MASTER` value — if you see a test or old doc referencing a `MASTER` role, it's stale; mastery is derived from `Campaign.master`, never stored as a role.

### Persistence is H2 in-memory (`local` profile)

`spring.profiles.active: local` in `application.yml` is unconditional — there's no separate prod config yet. The database resets on every restart; only the `admin`/`admin123` user survives (recreated by `DataInitializer`, itself gated to the `local` profile). Don't rely on data surviving a backend restart when testing manually.

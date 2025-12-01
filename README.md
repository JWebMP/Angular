# JWebMP Angular Plugin

Generate Angular 20 apps straight from JWebMP annotations and host the built SPA with Vert.x 5 + STOMP/WebSocket bridging. The module renders TypeScript/config from `@NgApp`/`@NgComponent`/`@NgRoutable` metadata, but it does not run the Angular build—point it at your dist output and it handles routing and assets.

## Getting started
- Prereqs: Java 25 LTS, Maven. Build with `mvn clean verify`.
- Turn on TypeScript generation: set `JWEBMP_PROCESS_ANGULAR_TS=true` (or `-Djwebmp.process.angular.ts=true`). Default is off.
- Host & messaging: WebSocket/STOMP at `/eventbus` (inbound `/toBus/incoming`, outbound `/toStomp.*`). Listeners must validate payloads and return `AjaxResponse`.
- Static hosting: serves the generated/built dist path (from `AppUtils` per `@NgApp`) with SPA fallback and `/assets` handling.

## Why use this plugin
- Documentation-first: architecture diagrams live in `docs/architecture/` and feed the rules in `rules/generative/frontend/jwebmp/angular/`.
- Forward-only: no legacy anchors; CRTP fluent APIs with Lombok `@Log4j2` logging.
- Vert.x 5 hosting out of the box: SPA fallback + STOMP/WebSocket bridge wired for `IGuicedWebSocket` listeners.
- Topic-first glossary: minimal duplication; link to topic glossaries in `rules/`.

## Rules & docs
- Rules submodule: `rules/` (see `.gitmodules`). Host docs stay outside the submodule.
- PACT: `PACT.md`
- Rules: `RULES.md`
- Glossary: `GLOSSARY.md`
- Guides: `GUIDES.md`
- Implementation plan: `IMPLEMENTATION.md`
- Prompt + diagrams index: `docs/PROMPT_REFERENCE.md`, `docs/architecture/README.md`

## Configuration & CI
- Sample env vars: `.env.example` (non-secret). Documented toggles include TS processing and CI secrets.
- GitHub Actions workflow: `.github/workflows/maven-package.yml` (shared GuicedEE workflow; requires `USERNAME`, `USER_TOKEN`, `SONA_USERNAME`, `SONA_PASSWORD`).

Contributing
- Contributions welcome under forward-only rules. Open issues/PRs with clear reproduction steps.
- Follow the topic rules in `rules/generative/frontend/jwebmp/angular/` and align with `RULES.md` section 4/5 + Document Modularity + Forward-Only.
- Use Log4j2 for logging; keep generated artifacts out of PRs.

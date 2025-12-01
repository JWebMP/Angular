Host rules for the JWebMP Angular plugin (forward-only, documentation-as-code). Treat prior docs as stale; this file supersedes them.

## Scope & stacks
- Java 25 LTS, Maven build; Lombok allowed; Log4j2 for logging.
- Angular 20 generated via JWebMP TypeScript plugin; base rules + v20 override.
- JWebMP Core + Client + TypeScript; GuicedEE Core/Client/Web/WebSocket; Vert.x 5 HTTP/STOMP hosting.
- Fluent API strategy: **CRTP** (no Lombok `@Builder` for fluent setters); use Lombok `@Log4j2` where logging is needed.
- Testing: JUnit Jupiter present; Jacoco to be aligned per rules; BrowserStack/Java Micro Harness noted in inputs (not yet wired).
- CI/CD: GitHub Actions (provider rules below).

## Rule sources (submodule)
- Rules repository submodule at `rules/` (see `.gitmodules`); do not place host docs inside the submodule.
- Language & framework anchors:
  - Java: `rules/generative/language/java/README.md`, version: `rules/generative/language/java/java-25.rules.md`
  - TypeScript: `rules/generative/language/typescript/README.md`
  - Angular base: `rules/generative/language/angular/angular.md`; Angular 20 override: `rules/generative/language/angular/angular-20.rules.md`
  - JWebMP: `rules/generative/frontend/jwebmp/README.md`, plus `rules/generative/frontend/jwebmp/core/README.md`, `rules/generative/frontend/jwebmp/client/README.md`, `rules/generative/frontend/jwebmp/typescript/README.md`
  - GuicedEE: `rules/generative/backend/guicedee/README.md`, `.../client/README.md`, `.../web/README.md`, `.../websockets/README.md`, `.../vertx/README.md`
  - Vert.x reactive: `rules/generative/backend/vertx/README.md`
  - Fluent API (CRTP): `rules/generative/backend/fluent-api/README.md`; Lombok alignment: `rules/generative/backend/lombok/README.md`
  - Logging: use Log4j2 (prefer Lombok `@Log4j2`); align with `rules/generative/backend/jspecify/README.md` for annotations as needed.
  - CI/CD: `rules/generative/platform/ci-cd/README.md`; provider: `rules/generative/platform/ci-cd/providers/github-actions.md`
  - Observability/Security as needed: `rules/generative/platform/observability/README.md`, `rules/generative/platform/security-auth/README.md`
  - Architecture practices: `rules/generative/architecture/README.md`, `rules/generative/architecture/tdd/README.md`

## Behavioral constraints
- Forward-Only Change Policy (see `rules/RULES.md` sections 4/5 & Forward-Only): replace outdated docs; do not preserve deprecated stubs.
- Documentation-first, stage-gated; blanket approval noted for this run, but gates are still recorded.
- Document modularity: keep PACT ↔ GLOSSARY ↔ RULES ↔ GUIDES ↔ IMPLEMENTATION linked; diagrams live under `docs/architecture/`.
- No project docs inside `rules/`; submodule content remains upstream-controlled.

## Architecture alignment
- Diagrams live in `docs/architecture/` (context/container/component/sequence/ERD) and are referenced from PACT, GUIDES, and IMPLEMENTATION.
- Vert.x hosting: configure STOMP/WebSocket at `/eventbus`, event-bus patterns `/toBus.*` ↔ `/toStomp.*`; respect heartbeats.
- TypeScript generation: gated by `JWEBMP_PROCESS_ANGULAR_TS`/`jwebmp.process.angular.ts`; generation uses `JWebMPTypeScriptCompiler` and `AngularTSPostStartup`.
- Routing: generated from `@NgRoutable` via `AngularRoutingModule`; SPA fallback enabled.

## Naming & glossary alignment
- Topic-first glossary precedence; host `GLOSSARY.md` indexes topic glossaries from `rules/`.
- Use Angular version-specific APIs (v20) only; do not mix 17/19 APIs.
- Use CRTP method chaining for fluent setters returning `(J)this`; avoid Lombok builders on fluent types.
- WebSocket/Web assets naming follows Angular routing and JWebMP component selectors.

## Cross-links
- PACT: `PACT.md`
- Glossary index: `GLOSSARY.md`
- Guides: `GUIDES.md`
- Implementation notes: `IMPLEMENTATION.md` (to be populated in Stage 3/4)
- Prompt reference & diagrams: `docs/PROMPT_REFERENCE.md`, `docs/architecture/README.md`

Guides for applying the selected rules in this repository. Link back to `RULES.md`, `GLOSSARY.md`, and diagrams under `docs/architecture/`.

## Angular TypeScript generation
- Follow `rules/generative/language/angular/angular.md` + `angular-20.rules.md`; TypeScript base in `rules/generative/language/typescript/README.md`.
- Source of truth: annotated classes (`@NgApp`, `@NgComponent`, `@NgRoutable`) under `src/main/java/com/jwebmp/core/base/angular/...`.
- Runtime flow: see `docs/architecture/sequence-typescript-generation.md`. TS generation is gated by `JWEBMP_PROCESS_ANGULAR_TS` / `jwebmp.process.angular.ts` and produces Angular source/config; npm build of dist occurs outside this module.
- Keep CRTP fluent setters; avoid Lombok builders on components/modules.
- Component import/config: `ConfigureImportReferences` (`IOnComponentConfigured`) maps JWebMP component trees to Angular imports/attributes (inputs/outputs) and prepares HTML/CSS/index content. Ensure new components expose correct annotations to participate in this step.
- Compiler path: `JWebMPTypeScriptCompiler` is present but marked for deprecation; plan new work against `TypeScriptCompiler` and keep SPI wiring compatible. Callers should prefer the newer compiler when introducing changes.
- Hosting and messaging: `AngularTSSiteBinder` serves generated dist assets (including `/assets`), applies SPA fallback, configures STOMP/WebSocket bridge at `/eventbus`, and dispatches `/toBus/incoming` messages to `IGuicedWebSocket` listeners; design new routes/components with this hosting model in mind.
- STOMP message flow: Clients connect via WebSocket to `/eventbus`, send STOMP frames to `/toBus/incoming`, and receive from `/toStomp.*`; listeners should validate actions/payloads and return `AjaxResponse` data to drive client updates.
- npm configuration: provide `NpmrcConfigurator` (SPI) when custom `.npmrc` entries are required for generated Angular builds.
- Inline structural helpers: use the provided components in `com.jwebmp.core.base.angular.components` (`NgIf`, `NgIfElse`, `NgFor`, `NgForEmpty`, `NgElse`, `NgLet`) for Angular structural directives (`@if`, `@else`, `@for`, `@let`).

## JWebMP + GuicedEE integration
- JWebMP rules: `rules/generative/frontend/jwebmp/README.md`, `core/`, `client/`, `typescript/`.
- GuicedEE lifecycle: `rules/generative/backend/guicedee/README.md`, `client/README.md`, `web/README.md`, `websockets/README.md`, `vertx/README.md`.
- Vert.x hosting and WebSocket bridge behavior: `docs/architecture/sequence-websocket-stomp.md`, component diagram `docs/architecture/c4-component-angular.md`.
- When adding message listeners, ensure they return `AjaxResponse` and validate payloads (trust boundary noted in `docs/architecture/overview.md`).

## Build, testing, and coverage
- Build with Maven (Java 25). Align with `rules/generative/language/java/build-tooling.md`.
- Tests: JUnit Jupiter present; extend with Jacoco per `rules/generative/platform/testing/GLOSSARY.md` and `rules/generative/architecture/tdd/README.md`.
- Browser/Angular integration tests: follow Angular rules for CLI/build outputs; consider BrowserStack per inputs (not yet wired).

## CI/CD and environments
- CI provider: GitHub Actions (`rules/generative/platform/ci-cd/providers/github-actions.md`). Default Maven workflow to be added under `.github/workflows/`.
- Environment variables/sample secrets should follow `rules/generative/platform/secrets-config/env-variables.md`; mirror in `.env.example`.
- Reference diagrams and PROMPT reference in CI/docs so future AI runs load the same context.

## Validation & acceptance cues
- TypeScript generation succeeds when `JWEBMP_PROCESS_ANGULAR_TS=true` and produces dist assets per `@NgApp`; SPA routes resolve per `AngularRoutingModule` tree.
- WebSocket/STOMP bridge relays `/toBus/incoming` messages to `IGucedWebSocket` listeners and returns `AjaxResponse` payloads; SPA fallback still serves `index.html`.
- CI: GitHub Actions build runs Maven verify and (future) Jacoco; secrets documented in `.env.example` and workflow notes.
- Docs closure: PACT ↔ GLOSSARY ↔ RULES ↔ GUIDES ↔ IMPLEMENTATION all link to `docs/architecture/` diagrams.
## Traceability
- Upstream rules: `rules/`.
- Glossary aggregation and precedence: `GLOSSARY.md`.
- Architecture diagrams: `docs/architecture/README.md`.
- Implementation details (modules, file layout, env/CI wiring) will be documented in `IMPLEMENTATION.md` (Stage 3/4).*** End Patch to=functions.apply_patch military少婦 Json## Test Output Reasoning

# Stage 2 — Rules/Guides design (doc-first, forward-only)

Scope: refresh the canonical rules for the JWebMP Angular plugin in the Rules Repository submodule (`rules/`), using the path `rules/generative/frontend/jwebmp/angular/` (not `angular-awesome`). Target relevant generative topics for this library (language/angular, frontend/jwebmp, backend/guicedee/vertx/websockets, platform/ci-cd/testing).

## Topic index updates (parent README)
- Add or update a parent README under `rules/generative/frontend/jwebmp/angular/` to index Angular 20 generation/hosting rules and link to base Angular language rules (`rules/generative/language/angular/README.md` + `angular-20.rules.md`).
- Add “How to consume” section pointing host projects to the rules submodule usage and to the library glossary.
- Ensure anchors for each modular rule remain intact; remove obsolete entries per forward-only policy.

## Modular rules skeleton (docs only)
- Angular hosting and generation core:
  - `overview.rules.md` — purpose, stack alignment, generation/hosting contract, CRTP fluent API guidance (no Lombok builders), Log4j2 logging policy.
  - `type-generation.rules.md` — @NgApp/@NgComponent/@NgRoutable inputs, ConfigureImportReferences mapping, AngularTsProcessingConfig flag, npmrc configurator.
  - `hosting-messaging.rules.md` — Vert.x router/STOMP bridge (`/eventbus`, `/toBus.*`, `/toStomp.*`), SPA fallback, static asset layout, heartbeats, security expectations.
  - `testing.rules.md` — Jacoco coverage expectations, Java Micro Harness hooks, BrowserStack workflow pointers (planned).
- Component/topic links:
  - Reuse existing JWebMP component rule files where applicable; ensure they reference Angular 20 where relevant and point back to the updated README index.
  - Add “See also” links to TypeScript/Angular base rules and JWebMP client/core/typescript indexes.
- Migration/breaking changes:
  - `release-notes.md` — forward-only notes for Angular 20 + Vert.x 5 uplift, TypeScriptCompiler migration path, glossary alignment changes.

## Guides and examples (design only)
- Host-side guides to add under library root (not in `rules/`):
  - `GUIDES.md` additions: applying AngularTsProcessingConfig, mapping messages to `IGuicedWebSocket`, wiring BrowserStack in CI.
  - Example stubs (not committed now): TypeScript generation runbook, WebSocket listener template returning `AjaxResponse`.
- Rules-side “how to apply” pointers inside each `.rules.md`: short steps that reference diagrams (`docs/architecture/`), PACT, and PROMPT_REFERENCE.

## API surface sketches (reference only)
- Highlight SPI and lifecycle entry points: `AngularPreStartup`, `AngularTSPostStartup`, `AngularTsProcessingConfig`, `JWebMPTypeScriptCompiler`/`TypeScriptCompiler`, `AngularTSSiteBinder`, `AngularRoutingModule`, `NpmrcConfigurator`, `IGuicedWebSocket`.
- Document CRTP fluent setter expectation for JWebMP component wrappers; avoid Lombok `@Builder` on fluent types.
- Log4j2 alignment using Lombok `@Log4j2` for logging examples.

## Testing & coverage strategy
- JVM: Jacoco minimum coverage gates for generation utilities; Java Micro Harness smoke for Vert.x hosting hooks; JUnit Jupiter baseline.
- Frontend: BrowserStack/Angular e2e strategy to be drafted; defer npm tooling specifics to Angular/TypeScript rules.
- CI: GitHub Actions workflow to run Maven verify + Jacoco; optional BrowserStack matrix plan.

## Migration / release notes outline
- Capture forward-only changes, removed anchors, and Angular 20/Vert.x 5 enforcement in `release-notes.md`.
- Note deprecation path from `JWebMPTypeScriptCompiler` to `TypeScriptCompiler`.
- Remind consumers to update glossary links and regenerate diagrams when consuming the new rules.

Implementation plan (docs-first). No source code changes performed yet; Stage 4 will execute the scoped items below.

## Current layout (evidence)
- Maven module root (`pom.xml`) with parent `com.jwebmp:parent:2.0.0-SNAPSHOT`; packaging `jar`.
- Java sources under `src/main/java/com/jwebmp/core/base/angular/...`:
  - Guice lifecycle: `AngularPreStartup`, `AngularTSPostStartup`, `AngularTSSiteBinder`, `AngularScanModuleInclusion`.
  - Vert.x hosting/STOMP bridge inside `AngularTSSiteBinder` (routes, StaticHandler, STOMP server, event bus consumer).
  - TypeScript generation: `JWebMPTypeScriptCompiler` (deprecated; transition to `TypeScriptCompiler`), `AngularRoutingModule`, `TypeScriptCodeGenerator/Validator`, env flag `AngularTsProcessingConfig`. Generation produces Angular source/config scaffolding; it does not execute the Angular build—dist assets must be produced externally.
  - Component import resolution: `ConfigureImportReferences` implements `IOnComponentConfigured` (registered via `META-INF/services/com.jwebmp.core.databind.IOnComponentConfigured`) to translate JWebMP component trees into Angular-ready tags/imports/attributes (inputs/outputs) during the JWebMP lifecycle, feeding generated HTML/CSS/index content.
  - Runtime hosting: `AngularTSSiteBinder` binds Guice modules, configures Vert.x router/HTTP options, serves generated dist assets (assets/, file fallback, SPA fallback), and wires STOMP/WebSocket bridge (`/eventbus`) plus event-bus consumer dispatching to `IGuicedWebSocket` listeners.
  - npm configuration SPI: `NpmrcConfigurator` (`uses` in `module-info.java`) allows pluggable `.npmrc` customization during TypeScript/Angular generation.
  - Inline Angular structural helpers (`@if`, `@else`, `@for`, `@let`) are provided as components under `com.jwebmp.core.base.angular.components` (e.g., `NgIf`, `NgIfElse`, `NgFor`, `NgForEmpty`, `NgElse`, `NgLet`).
  - Component annotations, modules, directives, and WebSocket receivers under `components`, `modules`, `modules/services`.
- Resources: `META-INF/services/*` registering Guice modules, lifecycle hooks, router configurators, WebSocket receivers.
- Tests: JUnit Jupiter examples in `src/test/java` (component rendering, app/service samples).
- Submodule: `rules/` from `https://github.com/GuicedEE/ai-rules.git` (already present).

## Module use-site SPI consumption
- `module-info.java` declares `uses` for: `AngularScanPackages`, `IWebSocketAuthDataProvider`, `RenderedAssets`, `NpmrcConfigurator`, `WebSocketGroupAdd`, `TypescriptIndexPageConfigurator`, `IPageConfigurator`. Implementations must be available on the module path for runtime discovery.

## Planned changes (Stage 4 execution)
- Update `README.md` to reflect Rules adoption, architecture docs, and stack selections.
- Add `.env.example` per `rules/generative/platform/secrets-config/env-variables.md` and document `JWEBMP_PROCESS_ANGULAR_TS` plus Vert.x/STOMP related toggles.
- Add GitHub Actions workflow (`.github/workflows/maven-package.yml`) referencing shared GuicedEE workflow per requirements; document secrets.
- Create AI workspace rule mirrors:
  - `.aiassistant/rules/` summary of RULES sections 4/5 + Document Modularity + Forward-Only.
  - `.github/copilot-instructions.md`.
  - `.cursor/rules.md`.
  - Ensure `rules/` submodule is referenced, not modified.
- Backlink PACT/RULES/GLOSSARY/GUIDES/IMPLEMENTATION to diagrams and PROMPT reference where missing.
- Consider MIGRATION.md entry if legacy docs (old README encoding) are superseded.

## Build/CI wiring plan
- Maven verify with Java 25; include flatten plugin defaults; future Jacoco config captured in TODOs.
- GitHub Actions workflow triggers on push + dispatch; uses GuicedEE shared workflow with required secrets (`USERNAME`, `USER_TOKEN`, `SONA_USERNAME`, `SONA_PASSWORD`).
- Document CI and env requirements in README and `.env.example`.

## Environment/config plan
- Surface toggles: `JWEBMP_PROCESS_ANGULAR_TS` (TS generation), potential Vert.x host/port if exposed by parent configs, heartbeat values (document defaults).
- Keep sample `.env.example` minimal and non-secret; instruct loading via system properties/env vars.

## Rollout & validation
- Validate docs link resolution.
- Run `mvn -q -DskipTests package`? (optional) after Stage 4 if feasible; note if skipped.
- Manual check: ensure `rules` submodule referenced in README and not polluted with host docs.
- When implementing new TS generation features, prefer `TypeScriptCompiler` and keep compatibility for existing `JWebMPTypeScriptCompiler` callers until full migration.

## Risks / unknowns
- WebSocket/STOMP security not defined; document expectation that listeners enforce auth/validation.
- Dist path generation uses `AppUtils` and user `webroot`; ensure no unintended directory exposure (documented in overview).
- Existing README text is encoded/garbled; replacing it could drop prior context (low risk, forward-only).*** End Patch to=functions.apply_patch sulky Json## Test Output Reasoning

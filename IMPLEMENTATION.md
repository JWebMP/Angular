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

- Rules Repository updates (restricted to `rules/generative/frontend/jwebmp/angular/` and necessary upstream indexes; no host docs inside the submodule):
  - Refresh the parent README in `rules/generative/frontend/jwebmp/angular/` and add cross-links to Angular 20, TypeScript, JWebMP core/client/typescript, GuicedEE, Vert.x, CI/testing rules.
  - Add modular rule pages outlined in `docs/rules-skeleton.md` (overview, type-generation, hosting-messaging, testing) and a release-notes doc; ensure “see also” links point back to the topic README and enterprise indexes.
  - Keep existing component `.rules.md` files under this topic (if present); align anchors to Angular 20 terminology and Log4j2/CRTP guidance.
- Host repository docs:
  - Update `README.md` with “How to use these rules” and glossary alignment note linking `GLOSSARY.md` and the topic glossaries under `rules/`.
  - Ensure `GLOSSARY.md` references topic-first precedence and points to rule glossaries; keep minimal local definitions.
  - Reference diagrams from PACT/RULES/GUIDES/IMPLEMENTATION and the refreshed architecture index.
- Release/change notes:
  - Add or update `RELEASE_NOTES.md` and bump changelog entry summarizing forward-only rule changes (Angular 20 enforcement, Vert.x 5 bridge, TypeScriptCompiler migration path).
  - Note forward-only behavior and removed legacy anchors if any.
- CI/docs validation:
  - Run link checks (manual review) across README/rules/guides; ensure new rule files are linked from indexes.
  - No code/test execution expected unless required to validate doc-generated paths.

## Build/CI wiring plan
- Keep Maven/Java 25 defaults; no new plugins for this docs-only change set.
- Verify `.github/workflows/maven-package.yml` stays aligned with GitHub Actions provider rules and references required secrets.
- Call out CI expectations in README/GUIDES (docs only).

## Environment/config plan
- Document `JWEBMP_PROCESS_ANGULAR_TS` / `jwebmp.process.angular.ts` gating in README/GUIDES; highlight STOMP heartbeat defaults and Vert.x host/port expectations if exposed.
- Keep `.env.example` minimal (no secrets); ensure env names match rules and guides.

## Rollout & validation
- Validate Markdown links and anchors across README/GLOSSARY/GUIDES/IMPLEMENTATION and new rule pages.
- Ensure `rules` submodule changes stay within the intended topic directories; no host docs inside submodule.
- Confirm cross-links to diagrams (`docs/architecture/`), PROMPT reference, and glossary precedence.

## Risks / unknowns
- WebSocket/STOMP security not defined; rules/guides must call out application-layer auth/validation.
- Dist path generation uses `AppUtils` and user `webroot`; document directory exposure safeguards in hosting rules.
- Component index drift risk in rules submodule; ensure forward-only removal of stale anchors is well-documented in release notes.

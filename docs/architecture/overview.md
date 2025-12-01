# Architecture overview (Stage 1 refresh)

- Purpose: Generate Angular 20 source/config from JWebMP annotations and host an externally built dist via Vert.x 5 with STOMP/WebSocket messaging. Angular bundling/build runs outside this module; this runtime serves the built assets and bridges messaging.
- Entry points: Guice lifecycle (`AngularPreStartup`, `AngularTSPostStartup`) and Vert.x router binding (`AngularTSSiteBinder`); processing is gated by `AngularTsProcessingConfig` (`JWEBMP_PROCESS_ANGULAR_TS` / `jwebmp.process.angular.ts`).
- Generation flow: `JWebMPTypeScriptCompiler` (migrating toward `TypeScriptCompiler`) scans `@NgApp`/`@NgComponent`/`@NgRoutable` metadata, applies `ConfigureImportReferences`, honors `NpmrcConfigurator` SPI, and writes Angular TS/config + assets into the dist/webroot tree.
- Hosting flow: `AngularTSSiteBinder` binds Guice modules, resolves dist paths via `AppUtils`, serves static assets (including `/assets`), enforces SPA fallback, and exposes a Vert.x STOMP bridge at `/eventbus` wired to event-bus consumers.
- Messaging flow: Clients connect over WebSocket/STOMP to `/eventbus`, send payloads to `/toBus/incoming`, which are mapped to `WebSocketMessageReceiver` and forwarded to `IGuicedWebSocket` listeners; replies stream back to `/toStomp.*` with `AjaxResponse` payloads and optional session/local storage updates.

## Dependencies & integrations (current evidence)

- Internal: JWebMP Core + TypeScript client annotations; GuicedEE scanning (core/client/web/websocket); Lombok `@Log4j2`; Apache Commons IO/Text; ClassGraph; Jackson; CRTP fluent API strategy.
- External runtime: Vert.x 5 (HTTP, event bus, STOMP bridge); Angular 20 SPA runtime built from generated TS; BrowserStack/Java Micro Harness noted for testing but not yet wired.
- Build: Maven (Java 25 LTS); flatten-maven-plugin; moditect disabled via property; JPMS module-info present.
- Tests: JUnit Jupiter exists; Angular generation and Vert.x runtime flows are not exercised in current tests.

## Trust boundaries / threat sketch

- WebSocket/STOMP endpoint `/eventbus` is internet-facing; listeners must validate `action` + payload and enforce authz at the application layer (no built-in auth observed).
- Static hosting reads from resolved dist paths; path resolution must avoid directory traversal and ensure only generated assets are served.
- Heartbeats: server → client (10s) enabled; client heartbeats disabled — monitor idle connections and DOS exposure.
- File writes: TypeScript generation writes under user home/dist; ensure permissions and cleanup policies are documented.

## Glossary composition plan (topic-first)

- Root `GLOSSARY.md` indexes topic glossaries from `rules/` (Java 25, Angular 20, TypeScript, JWebMP Core/Client/TypeScript, GuicedEE Core/Client/Web/WebSocket, Vert.x 5, Lombok CRTP, Log4j2, Jacoco, GitHub Actions).
- Prompt Language Alignment to mirror locally: CRTP fluent setter rules (no Lombok `@Builder` for fluent types), Log4j2 logging, Angular naming for routes/components/modules, Vert.x STOMP endpoint naming (`/toBus.*`, `/toStomp.*`).
- Host projects should link back to topic glossaries rather than duplicating definitions; only enforced mappings (e.g., CRTP terminology) are copied into the root glossary.

## Open questions / follow-ups

- Desired authn/z layer for WebSocket/STOMP endpoints (JWT, session cookies, or custom headers) is unspecified.
- Dist path expectations for multiple `@NgApp` deployments (current flow uses `AppUtils` base path).
- Desired npm/toolchain pinning for generated Angular projects (versions/scripts are not pinned in repo artifacts).

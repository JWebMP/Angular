Glossary index (topic-first). Topic glossaries from the `rules/` submodule take precedence for their scope; this file aggregates links and captures only enforced local mappings.

## Glossary precedence policy
- Topic glossaries override root definitions for their domain; defer to upstream sources for terminology.
- Host glossary is an index and captures mandatory Prompt Language Alignment mappings only.
- CRTP fluent strategy is enforced (no Lombok builders on fluent types); Log4j2 is the required logging API.

## Topic glossaries (links)
- Java: `rules/generative/language/java/GLOSSARY.md` (Java 25)
- TypeScript: `rules/generative/language/typescript/GLOSSARY.md`
- Angular: `rules/generative/language/angular/GLOSSARY.md` (+ version override references)
- JWebMP: `rules/generative/frontend/jwebmp/core/README.md` glossary hooks; `.../client/GLOSSARY.md`; `.../typescript/GLOSSARY.md`
- GuicedEE: `rules/generative/backend/guicedee/GLOSSARY.md`, `.../client/GLOSSARY.md`, `.../web/GLOSSARY.md`, `.../websockets/README.md` terminology, `.../vertx/README.md`
- Fluent API: `rules/generative/backend/fluent-api/GLOSSARY.md`
- Lombok: `rules/generative/backend/lombok/GLOSSARY.md`
- JSpecify: `rules/generative/backend/jspecify/GLOSSARY.md`
- Angular 20 specifics: `rules/generative/language/angular/angular-20.rules.md`
- CI/CD: `rules/generative/platform/ci-cd/README.md`

## Local enforced mappings
- **Fluent style:** Use CRTP chaining returning `(J)this`; avoid Lombok `@Builder` on fluent types; follow Lombok glossary for annotations (prefer `@Log4j2`).
- **Logging:** Log4j2 only (no SLF4J bridge unless rules require); annotate classes with Lombok `@Log4j2` when adding logs.
- **WebSocket/STOMP endpoints:** Event bus bridge at `/eventbus`; inbound `/toBus/incoming`; outbound `/toStomp.*`. Clients send actions mapped to `WebSocketMessageReceiver.action`.
- **Routing terms:** Routes derive from `@NgRoutable`; SPA fallback serves `index.html` for non-file paths; assets served from `dist/assets`.
- **TS processing toggle:** `JWEBMP_PROCESS_ANGULAR_TS` env var (or `jwebmp.process.angular.ts` system property) controls TypeScript generation.
- **Generated asset paths:** `AppUtils` resolves per-`@NgApp` dist path under user `webroot` directory; keep terminology consistent across docs and code.

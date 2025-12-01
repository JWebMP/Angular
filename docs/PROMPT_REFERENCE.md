Prompt reference for future AI interactions (forward-only). Load this file before prompting to ensure stack alignment, stage gates, and diagram reuse.

## Selected stacks
- Java 25 LTS; Maven build.
- Angular 20 generated via JWebMP TypeScript plugin; Angular routing produced from @NgRoutable.
- Vert.x 5 HTTP router + STOMP WebSocket bridge; GuicedEE lifecycle (pre/post startup).
- JWebMP Core + TypeScript client; CRTP fluent API strategy with Lombok `@Log4j2`.
- Logging: Log4j2.
- Testing: JUnit Jupiter (present), Jacoco selected; Java Micro Harness/BrowsersStack referenced in inputs (not yet wired).
- CI/CD provider: GitHub Actions (to be aligned).

## Diagram links
- Context: docs/architecture/c4-context.md
- Container: docs/architecture/c4-container.md
- Component: docs/architecture/c4-component-angular.md
- Dependency map: docs/architecture/dependency-map.md
- Interaction/data flows: docs/architecture/interaction-flows.md
- Sequences: docs/architecture/sequence-typescript-generation.md, docs/architecture/sequence-websocket-stomp.md
- ERD: docs/architecture/erd-angular-plugin.md

## Glossary precedence (topic-first)
- Host `GLOSSARY.md` indexes topic glossaries from the rules submodule; topic definitions override root terms in their scope.
- Prompt Language Alignment: use CRTP (no Lombok @Builder for fluent setters), Log4j2 for logging, Angular naming from `rules/generative/language/angular`.

## Traceability
- PACT ↔ GLOSSARY ↔ RULES ↔ GUIDES ↔ IMPLEMENTATION must reference these diagrams and the overview/trust notes.
- Do not place project docs inside `rules/`; keep host docs under repository root/docs.
- Mermaid MCP server is available for regenerating diagrams; keep sources in Markdown (no rendered images committed).

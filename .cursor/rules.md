Cursor workspace rules (pin these before generation):
- Apply Rules Repository behavioral/technical sections (4/5), Document Modularity, Forward-Only; no host docs inside `rules/` submodule.
- Docs-first, stage-gated; blanket approval noted but keep gate awareness. Use Mermaid diagrams under docs/architecture/.
- Stack: Java 25 + Maven; Angular 20 (base + angular-20 override); JWebMP Core/Client/TypeScript; GuicedEE Core/Client/Web/WebSocket; Vert.x 5; CRTP fluent API; Log4j2 logging.
- Reference host artifacts: PACT.md, RULES.md, GLOSSARY.md, GUIDES.md, IMPLEMENTATION.md, docs/PROMPT_REFERENCE.md.
- Align prompts/components to glossary terms; avoid fabricating modules beyond current codebase (TS compiler, AngularTSSiteBinder, Vert.x STOMP bridge).

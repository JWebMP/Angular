Workspace guardrails for Copilot:
- Follow `rules/RULES.md` sections 4/5 plus Document Modularity and Forward-Only policies.
- Documentation-first, stage-gated; stages auto-approved this run. Keep host docs outside `rules/` submodule.
- Stack: Java 25 + Maven; Angular 20 (base + angular-20.rules.md); JWebMP + GuicedEE + Vert.x 5; CRTP fluent APIs; Log4j2 logging.
- Link artifacts: PACT.md, RULES.md, GLOSSARY.md, GUIDES.md, IMPLEMENTATION.md, docs/architecture/ diagrams, docs/PROMPT_REFERENCE.md.
- Prefer Mermaid for diagrams; do not invent architecture beyond observed code (TypeScript compiler, Vert.x/STOMP bridge).

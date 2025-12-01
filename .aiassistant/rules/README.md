Pinned workspace rules (summarized for AI Assistant).

- Honor Rules Repository `rules/RULES.md` sections 4 (Behavioral) and 5 (Technical), Document Modularity Policy, and Forward-Only Change Policy.
- Documentation-first, stage-gated workflow applies; stages auto-approved for this run but record gates. No source edits before docs are in place.
- Do not place host docs inside `rules/` submodule; keep project artifacts at repo root/docs.
- Logging: Log4j2 with Lombok `@Log4j2`. Fluent API strategy: CRTP (no Lombok builders on fluent types).
- Use Java 25 + Maven; Angular 20 generation rules apply (base + angular-20 override). Vert.x 5 + GuicedEE lifecycle.
- Close loops: PACT ↔ GLOSSARY ↔ RULES ↔ GUIDES ↔ IMPLEMENTATION ↔ diagrams (`docs/architecture/`).

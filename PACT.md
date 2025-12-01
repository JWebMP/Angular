---
version: 2.0
date: 2025-12-01
title: The Human–AI Collaboration Pact
project: JWebMP / Angular
authors: [Project Maintainers, AI Assistants]
---

# 🤝 Pact.md (v2)
### The Human–AI Collaboration Pact
*(Human × AI Assistant — “The Pact” Developer Edition)*

## 1. Purpose

This pact aligns the JWebMP Angular plugin team and AI assistants on how we document, design, and evolve the TypeScript/Vert.x integration. We operate documentation-first, respect forward-only change policy, and keep Rules ↔ Guides ↔ Implementation tightly linked.

> We don’t *vibe code* — we *vibe engineer*.

## 2. Principles

### 🧭 Continuity
- Context is pinned in repository artifacts (PACT/RULES/GUIDES/IMPLEMENTATION, docs/architecture, docs/PROMPT_REFERENCE.md).
- Rules from `rules/RULES.md` (sections 4/5, Document Modularity, Forward-Only) are mirrored in local workspace files for Copilot/AIAssistant/Cursor.
- Traceability is explicit: PACT → GLOSSARY → RULES → GUIDES → IMPLEMENTATION.

### 🪶 Finesse
- Documentation leads; source changes follow staged gates (blanket-approved for this run, but still recorded).
- Tone stays clear and technical; diagrams live as Mermaid in Markdown for diffability.
- Naming follows glossary precedence (topic-first; see `GLOSSARY.md` once published).

### 🌿 Non-Transactional Flow
- Conversations are collaborative; assumptions are surfaced instead of invented architecture.
- Stage gates are honored even when auto-approved, with notes in deliverables.

### 🔁 Closing Loops
- Every doc links forward/back to its neighbors.
- Diagrams in docs/architecture are linked from RULES/GUIDES/IMPLEMENTATION.
- Risky removals are captured in MIGRATION.md if/when they occur.

## 3. Structure of Work

| Layer | Description | Artifact |
|-------|-------------|----------|
| **Pact** | Shared culture, tone, and collaboration model. | `PACT.md` |
| **Rules** | Constraints, conventions, selected stacks; links into `rules/` submodule. | `RULES.md` (host) |
| **Guides** | How to apply rules in this codebase (Angular TS generation, Vert.x hosting, WebSocket bridge). | `GUIDES.md` |
| **Implementation** | Current modules, layouts, CI/env wiring; back-links to Guides/Rules. | `IMPLEMENTATION.md` |

> Every artifact cross-links to its source and next layer for traceable flow.

## 4. Behavioral Agreements

- **Language:** Clear, narrative-technical; no filler.
- **Context:** Derived from current repository state; legacy docs treated as stale unless updated in this run.
- **Reflection:** Unknowns are documented as questions, not guesses.
- **Tone:** Friendly and direct; use Log4j2 with Lombok `@Log4j2` when logging is needed.
- **Iteration:** Small, reviewable deltas; forward-only.
- **Transparency:** Constraints and assumptions are declared.
- **Attribution:** Authorship is dual — human intent + AI articulation.

## 5. Developer Culture: *“Vibe Engineering”*

### 🎛️ Definition
**Vibe Engineering** aligns toolchains (Maven, Vert.x 5, Angular 20 generation) with documentation-first practices for JWebMP/GuicedEE stacks.

### 🧠 Practiced Through
- Tool literacy across Guice modules, Vert.x router/STOMP bridge, and JWebMP TypeScript compiler.
- Stage-gated docs with Mermaid diagrams for C4/sequence/ERD.
- Consistent glossary usage (CRTP fluent APIs, Lombok, Angular terminology).

### 💡 Motto
> “Engineering the *vibe* means making the *craft* visible.”

## 6. Technical Commitments

- **Format:** Markdown-first; diagrams as Mermaid fenced blocks.
- **Naming:** Stable component naming aligned with Angular decorators and JWebMP components.
- **Traceability:** RULES/GUIDES/IMPLEMENTATION reference PACT, GLOSSARY, docs/architecture, and rules/ submodule anchors.
- **Tools:** Java 25 LTS, Maven build, Vert.x 5 websockets/STOMP, CRTP fluent style with Lombok `@Log4j2`.
- **Transparency:** Configuration flags (e.g., `JWEBMP_PROCESS_ANGULAR_TS`) are documented in `.env.example` and IMPLEMENTATION.
- **Documentation:** docs/PROMPT_REFERENCE.md indexes selected stacks and diagrams for future prompts.

## 7. Shared Goals

1. Keep Angular TypeScript generation predictable and documented (scans @NgApp, renders TS, serves dist via Vert.x).
2. Maintain secure, observable WebSocket/STOMP bridging between Angular clients and Vert.x event bus.
3. Align CI/env scaffolding with forward-only rules (GitHub Actions, .env.example).
4. Ensure Rules submodule stays referenced; no host-specific docs live inside the submodule.
5. Close loops between diagrams and implementation for future contributors and AI tools.

## 8. Closing Note

> “We’ve moved beyond prompt engineering into *vibe engineering* — where the prompt is the tool, the conversation is the method, and the craft is the output.”

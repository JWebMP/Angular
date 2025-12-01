Architecture index for the JWebMP Angular plugin (forward-only, documentation-first). All diagrams are Mermaid sources; no rendered images are required.

- C4 Context: `docs/architecture/c4-context.md`
- C4 Container: `docs/architecture/c4-container.md`
- C4 Component (Angular hosting & messaging): `docs/architecture/c4-component-angular.md`
- Sequence — TypeScript generation on startup: `docs/architecture/sequence-typescript-generation.md`
- Sequence — WebSocket/STOMP bridge: `docs/architecture/sequence-websocket-stomp.md`
- ERD — Angular integration domain objects: `docs/architecture/erd-angular-plugin.md`
- Narrative overview, trust boundaries, and glossary plan: `docs/architecture/overview.md`

These files describe the current observed code: Vert.x 5 hosting, Guice lifecycle modules, TypeScript generation via `JWebMPTypeScriptCompiler`, and STOMP/WebSocket messaging through `AngularTSSiteBinder`. Update these diagrams whenever implementation changes.

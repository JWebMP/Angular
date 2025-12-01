## Forward-only migration notes
- Replaced legacy/encoded `README.md` with forward-only documentation aligned to Rules submodule and new host artifacts (PACT/RULES/GUIDES/IMPLEMENTATION/GLOSSARY).
- No runtime code changes performed in this change set; future migrations should extend this file only when behavior or APIs change.
- Planned deprecation: transition TypeScript generation from `JWebMPTypeScriptCompiler` to `TypeScriptCompiler`; keep existing SPI integrations working during the migration.

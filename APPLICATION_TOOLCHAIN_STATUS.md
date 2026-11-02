# Application-only toolchain: implementation status

Draft API: `@NgApp(applicationOnly = true)`.

The draft uses Angular 22, build 22.2.5, Architect 0.2202.5 (which itself exports
`architect`), TypeScript ~6.0.0, and Node ^22.22.3 / ^24.15.0 / >=26.
Only INgModule's framework scaffold declarations are omitted; consumer dependency
annotations remain. Production configuration and serve targets use the existing
workspace generation pipeline.

NOT READY FOR PUBLICATION: strict npm resolution fails in build 22.2.5's optional
WASM parser: @emnapi/core 2.0.0-alpha.3 conflicts with wasm-runtime 1.2.6's peer
^1.7.1 || 2.0.0-beta.2. Build 22.2.4 resolves but has Vite audit findings,
including high severity. Neither forced resolution nor advisory suppression is used.

Preflight accepts unchanged generator-owned manifests recorded after successful
generation. Unowned or edited manifests and actual test/lint configuration cause
failure before writes. Dependency/cache/build directories are excluded from test
scanning. The application-only path retains the npm lock, installs strictly (also
on Linux), and propagates npm command failures. Seven focused tests passed before
the final orchestration checks; production build validation remains blocked.

NE1 adoption after validation:
- Add applicationOnly=true to ShellAngularApplication's NgApp annotation.
- Remove FrontendBuildToolchain.java and its configure call in ShellApplication.
- Replace local rewrite tests with generator integration and preservation tests.
- ShellApplication: invoke node node_modules/@angular-devkit/architect/bin/cli.js
  JWebMP:build:production (or use the generated npm build-prod script).
- .github/workflows/ci.yml and tools/local-shell.ps1: use npm run build-prod;
  update the local dependency presence check to @angular-devkit/architect/bin/cli.js.
- Upgrade CI Node 22.14.0 to a supported release; validation used Node 24.19.0.
- Regenerate .github/frontend/package-lock.json from actual generated output with
  strict peer resolution; validate npm ci, production build and artifact checks.
- Retain the separate AngularAwesome library CLI and standalone browser test tooling.

NE1 source adoption is implemented locally and its focused annotation test passes.
The canonical lock remains unchanged because resolution is blocked; do not publish
the adoption until a valid regenerated lock and production build pass.
No reset, deployment, or publication has occurred.

## Actual consumer validation

- Updated TypeScript client installed from an isolated source copy because the
  original target JAR was locked by another process; no process was terminated.
- Angular install and 13 focused tests passed.
- NE1 FrontendBuildToolchainTest passed; LandingZone reactor install passed with
  tests skipped after that focused test.
- Actual LandingZone generate-only entry point passed in
  C:/Java/ne1-world/target/toolchain-audit/generated.
- Its emitted package has Architect 0.2202.5, build 22.2.5, TypeScript ~6.0.0,
  production/serve/watch scripts and no default CLI/test/lint/library scaffolding.
- Strict resolution of that actual manifest fails with the same WASM peer conflict.
- The 22.1.9 diagnostic resolves but reports one high and two critical affected
  dependency nodes; it is not adopted. The 22.2.4 diagnostic reports one low and
  one high affected node; it is not adopted either.
- No production build, valid replacement canonical lock, or remote publication.

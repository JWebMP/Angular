# JWebMP Angular Plugin

## Angular build budgets and options

Configure the generated `angular.json` build environments directly on `@NgApp`:

```java
import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.client.annotations.angularconfig.NgBudget;
import com.jwebmp.core.base.angular.client.annotations.angularconfig.NgBuildConfiguration;

import static com.jwebmp.core.base.angular.client.annotations.angularconfig.NgBudget.Type.ANY_COMPONENT_STYLE;
import static com.jwebmp.core.base.angular.client.annotations.angularconfig.NgBuildConfiguration.BooleanOption.*;

@NgApp(
    value = "my-app",
    bootComponent = AppComponent.class,
    production = @NgBuildConfiguration(
        budgets = @NgBudget(type = ANY_COMPONENT_STYLE, maximumWarning = "30kb", maximumError = "50kb"),
        optimization = TRUE,
        sourceMap = FALSE,
        extractLicenses = TRUE,
        outputHashing = NgBuildConfiguration.OutputHashing.ALL,
        optionsJson = """
            {"optimization": {"scripts": true, "styles": true, "fonts": false}}
            """),
    development = @NgBuildConfiguration(
        budgets = @NgBudget(type = ANY_COMPONENT_STYLE, maximumWarning = "50kb", maximumError = "100kb"),
        optimization = FALSE,
        sourceMap = TRUE))
public class MyApp extends NGApplication<MyApp> { }
```

These settings target `projects.JWebMP.architect.build.configurations.production`
and `.development` independently; they do not change the selected/default build
configuration or serve settings. Regenerate and rebuild the application to apply them.
The example accommodates a 22.39 kB component stylesheet without disabling budgets.

Unspecified values preserve the template defaults: production has an initial
budget of `2500kb` warning / `10mb` error, a component-style budget of `6kb` warning /
`10kb` error, and `outputHashing: "all"`. Development has no budgets,
`optimization: false`, `extractLicenses: false`, and `sourceMap: true`.
Boolean and hashing `DEFAULT` values leave the existing property untouched.

Typed budgets replace the **whole matching budget** by type and name, retaining
unrelated template budgets. Supply both warning and error limits when both are wanted.
All Angular budget types and threshold fields are supported, including named `BUNDLE`
budgets, `baseline`, minimum thresholds, and symmetric `warning`/`error` thresholds.
Set `inheritBudgets = false` to replace the entire budget list; with no supplied
budgets, this explicitly disables budgets for that environment.

`optionsJson` is a JSON **object of build options**, applied after typed values.
Nested objects merge recursively, arrays and scalars replace, and `null` removes
a property. For example, `{"budgets":[]}` clears budgets and
`{"sourceMap":{"scripts":true,"styles":false,"hidden":true}}` uses advanced source maps.
It is not a complete workspace document. Invalid/non-object JSON and duplicate typed
budgets fail generation; Angular CLI validates option names, values, and size syntax.
Inherited `@NgApp` declarations are supported.

## Locale formatting

Declare the locale on your application (or its boot component):

```java
import com.jwebmp.core.base.angular.client.annotations.angular.NgLocale;

@NgLocale("en-ZA")
@NgApp(value = "my-app", bootComponent = AppComponent.class)
public class MyApp extends NGApplication<MyApp> { }
```

The generator imports Angular locale data, calls `registerLocaleData` in
`app.config.ts` before bootstrap, and provides `LOCALE_ID` application-wide.
This replaces the manual locale imports, constructor registration, and provider
annotations. An application annotation takes precedence over its boot component;
inherited annotations are supported and unrelated apps are unaffected.

Use `@NgLocale(value = "fr-CA", dataLocale = "fr", extraData = true)` to select
a different Angular data file and include extra day-period data. By default the
file name matches the locale identifier (`en-US` uses Angular's `en` file).
The selected file must exist in your installed `@angular/common/locales` package.
This configures date, number, percentage, and currency formatting; message
translation is a separate concern. Regenerate
and rebuild the Angular application after changing the annotation.

### User-selected formatting locale

Register the additional locale data on your app:

```java
@NgLocale(value = "en-ZA", supportedLocales = {"de", "fr"})
```

Reference the root-scoped service from components that select or display the locale:

```java
import com.jwebmp.core.base.angular.client.services.LocaleService;

@NgComponentReference(LocaleService.class)
```

This supplies `public localeService: LocaleService` in the generated constructor.
Use `localeService.setLocale('de')` in a template event, or
`this.localeService.setLocale('de');` inside an `@NgMethod`. Bind the read-only
locale signal explicitly when formatting:

```html
{{ orderDate | date:'longDate':undefined:localeService.locale() }}
{{ amount | number:'1.2-2':localeService.locale() }}
{{ amount | currency:'ZAR':'symbol':'1.2-2':localeService.locale() }}
{{ ratio | percent:'1.0-2':localeService.locale() }}
```

The signal updates these bindings without a reload, including in OnPush components.
`resetLocale()` restores the application default. Missing locale data throws before
the preference changes; include the desired locale in `supportedLocales` first.
Angular's fallback to registered parent-language data still applies. `extraData`
also applies to all supported locales. Duplicate IDs are registered once.

`LOCALE_ID` stays at the app default: existing pipes without an explicit locale
argument and third-party controls do not automatically switch. Pass the selected
locale to those controls through their own APIs. Currency codes and timezones are
independent of locale. The service keeps state in the current Angular application
instance; save/restore the preference through your user-profile flow and call
`resetLocale()` when switching users or logging out. It does not write browser
storage, cookies, or backend records, and does not translate application text.

## Runtime translations

Enable Transloco generation on the app or boot component:

```java
@NgTranslations(defaultLanguage = "en", supportedLanguages = {"en", "de", "fr"}, namespaces = {"orders"})
@NgTranslationSource(namespace = "orders", resource = "META-INF/jwebmp/i18n/orders")
@NgTranslationSource(namespace = "orders", url = "/rest/translations/orders/{language}", priority = 200)
```

Libraries can package defaults in `META-INF/jwebmp/i18n/<namespace>/<language>.json`.
The generator discovers those files from ClassGraph, writes merged bundles to
`public/i18n/jwebmp`, and emits a language manifest. Bundled dictionaries have
priority `0`; application and URL sources default to `100`, and higher priorities
override lower ones. Equal-priority conflicts fail with both origins reported.

Empty or whitespace-only classpath dictionary files are accepted as empty objects
and logged as warnings; `{}` is the preferred explicit placeholder. They contribute
no keys, so existing lower-priority translations remain intact and missing translated
keys can fall back to the default language at runtime. Malformed JSON, non-object
dictionaries, and invalid translation values still fail generation.

The generated `TranslationService` loads the selected and fallback languages through
Angular `HttpClient`, so application interceptors and authenticated REST endpoints
apply. Optional URL sources fall back to bundled dictionaries. Requests are
deduplicated and stale responses are discarded after a newer language or context
selection.

Reference `TranslationService` with `@NgComponentReference(TranslationService.class)`.
It exposes `language()`, `loading()`, `errors()`, `setLanguage`,
`setLanguageAndLocale`, `reload`, `clearContext`, and
`applyTranslations(language, namespace, data)` for dictionaries returned by an
existing REST client. Use `{{ 'orders.save' | transloco }}` or the `transloco`
directive in generated templates. ICU plural/select messages are enabled by default;
set `messageFormat = false` when they are unnecessary. Translation language and
formatting locale remain independent. Call `clearContext()` on logout, tenant
changes, or user switches; persistence remains the consumer application's concern.

[![Build](https://github.com/JWebMP/Plugins/Angular/actions/workflows/maven-package.yml/badge.svg)](https://github.com/JWebMP/Plugins/actions/workflows/maven-package.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.jwebmp.plugins/angular)](https://central.sonatype.com/artifact/com.jwebmp.plugins/angular)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue)](https://www.apache.org/licenses/LICENSE-2.0)

![Java 25+](https://img.shields.io/badge/Java-25%2B-green)
![Angular 21](https://img.shields.io/badge/Angular-20-green)
![Vert.X 5](https://img.shields.io/badge/Vert.x-5%2B-green)
![Maven 4](https://img.shields.io/badge/Maven-4%2B-green)

Generates **Angular 21** TypeScript projects from JWebMP annotation metadata and serves the built SPA via **Vert.x 5** with STOMP/WebSocket bridging. Annotate your Java classes with `@NgApp`, `@NgComponent`, `@NgRoutable`, `@NgDataService`, etc. — the compiler produces `.ts` files, `angular.json`, `package.json`, `tsconfig.json`, routing modules, and environment config. At runtime, the plugin hosts the compiled dist directory with SPA-fallback routing, static-asset serving, and a full STOMP event-bus bridge.

## ✨ Features

- **TypeScript code generation** — `TypeScriptCompiler` orchestrates `TypeScriptCodeGenerator`, `AngularModuleProcessor`, `ComponentProcessor`, `AngularAppSetup`, `AssetManager`, `DependencyManager`, and `TypeScriptCodeValidator` to produce a complete Angular project from Java annotations
- **SPA hosting via Vert.x** — serves the `dist/` output with `StaticHandler`, explicit `/assets/*` mount, regex-based static file routing, and an `index.html` SPA fallback for Angular Router client-side routes
- **STOMP/WebSocket bridge** — `StompServer` at `/eventbus` with configurable heartbeats (10s server → client, 50s client tolerance), `v10/v11/v12.stomp` sub-protocols, inbound `/toBus.*` and outbound `/toStomp.*` address patterns
- **Reactive message processing** — inbound STOMP messages on `/toBus/incoming` are deserialised to `WebSocketMessageReceiver`, dispatched to registered `IWebSocketMessageReceiver` listeners, and responses are published back via the event bus (data returns, session/local storage)
- **Built-in WebSocket receivers** — `WebSocketAjaxCallReceiver` (action: `ajax`), `WebSocketDataRequestCallReceiver` (action: `data`), `WebSocketDataSendCallReceiver` (action: `dataSend`), `WSAddToGroupMessageReceiver` (action: `AddToWebSocketGroup`), `WSRemoveFromWebsocketGroupMessageReceiver` (action: `RemoveFromWebSocketGroup`)
- **Angular control-flow components** — `NgIf`, `NgIfElse`, `NgElse`, `NgFor`, `NgForEmpty`, `NgLet` rendering Angular 17+ `@if`, `@for`, `@let` template syntax from Java
- **Angular modules** — pre-built `CommonModule`, `FormsModule`, `RouterConfig` wrappers
- **Routing** — `AngularRoutingModule` scans `@NgRoutable`-annotated classes, builds a `DefinedRoute` tree with parent/child nesting, and generates `RouterModule.forRoot(routes)` with configurable `RoutingModuleOptions`
- **Environment module** — `EnvironmentModule` + `EnvironmentOptions` generates a TypeScript `const environment = { ... }` from Java configuration
- **RouterLink** — server-side `RouterLink` component with `[routerLink]`, `[queryParams]`, and `[state]` binding
- **OnClick directive** — `OnClickListenerDirective` + `OnClickListener` bridges JWebMP click events to Angular directives
- **WebSocket group management** — `WebSocketGroupsDirective` + `WebSocketGroupAdd` SPI for declarative WebSocket group subscription from components
- **STOMP publisher helper** — `StompEventBusPublisher` for simple `vertx.eventBus().publish("/toStomp/<addr>", message)` calls
- **NPM resource locator** — discovers and serves bundled `node_modules` resources from the classpath
- **Angular project scaffolding** — generates `angular.json`, `package.json`, `tsconfig.json`, `tsconfig.app.json`, `.gitignore`, and `app.config.json` templates from bundled resources
- **`@NgApp` page disables JWebMP page routing** — `AngularPreStartup` sets `BIND_JW_PAGES=false` so Vert.x routes are managed by this plugin instead

## 📦 Installation

```xml
<dependency>
  <groupId>com.jwebmp.plugins</groupId>
  <artifactId>angular</artifactId>
</dependency>
```

> Version is managed by the `com.jwebmp:jwebmp-bom` imported in the parent POM.

## 🗺️ JPMS Module

```
module com.jwebmp.core.angular {
    requires transitive com.jwebmp.core.base.angular.client;
    requires transitive com.jwebmp.vertx;
    requires transitive io.vertx.eventbusbridge;
    requires transitive io.vertx.stomp;
    requires transitive org.apache.commons.text;
    requires transitive org.apache.commons.lang3;
    requires org.apache.commons.io;

    provides IGuiceScanModuleInclusions      with AngularScanModuleInclusion;
    provides IGuiceConfigurator              with SearchPathConfigurator;
    provides IGuicePreStartup                with AngularPreStartup;
    provides IGuiceModule                    with AngularTSSiteBinder;
    provides VertxRouterConfigurator         with AngularTSSiteBinder;
    provides VertxHttpServerOptionsConfigurator with AngularTSSiteBinder;
    provides IGuicePostStartup               with AngularTSPostStartup;
    provides IOnComponentConfigured          with ConfigureImportReferences;
    provides IOnClickService                 with OnClickListener;
    provides IWebSocketMessageReceiver       with WebSocketAjaxCallReceiver,
                                                  WebSocketDataRequestCallReceiver,
                                                  WebSocketDataSendCallReceiver,
                                                  WSAddToGroupMessageReceiver,
                                                  WSRemoveFromWebsocketGroupMessageReceiver;

    uses AngularScanPackages;
    uses RenderedAssets;
    uses NpmrcConfigurator;
    uses WebSocketGroupAdd;
    uses TypescriptIndexPageConfigurator;
    uses IPageConfigurator;
    uses IWebSocketAuthDataProvider;
}
```

## 🚀 Quick Start

### 1. Define an Angular app

```java
@NgApp(value = "my-app", bootComponent = AppComponent.class)
public class MyApp extends NGApplication<MyApp> { }
```

### 2. Create the boot component

```java
@NgComponent("app-root")
public class AppComponent extends DivSimple<AppComponent>
        implements INgComponent<AppComponent> { }
```

### 3. Add a routable page

```java
@NgRoutable(path = "dashboard", parent = {AppComponent.class})
@NgComponent("app-dashboard")
public class DashboardPage extends DivSimple<DashboardPage>
        implements INgComponent<DashboardPage> { }
```

### 4. Enable TypeScript generation

```bash
# Environment variable
export JWEBMP_PROCESS_ANGULAR_TS=true

# Or system property
mvn clean verify -Djwebmp.process.angular.ts=true
```

### 5. Start the application

```java
IGuiceContext.instance();
// → TypeScript is generated to ~/.jwebmp/<appName>/
// → Vert.x serves the dist at the configured routes
// → STOMP/WebSocket bridge is live at /eventbus
```

> **Note:** The plugin generates the TypeScript project but does **not** run `ng build`. Build the Angular app separately (e.g. `cd ~/.jwebmp/my-app && npm install && ng build`) and the dist output is served automatically.

## 🏗️ Architecture

### Startup Flow

```
IGuiceContext.instance()
 └─ AngularPreStartup           → sets BIND_JW_PAGES=false
     └─ SearchPathConfigurator   → enables annotation/classpath/path scanning
         └─ AngularTSSiteBinder  → binds EnvironmentModule, discovers @NgApp classes,
         │                         creates output dirs, configures STOMP server + router
         └─ AngularTSPostStartup → if TS processing enabled, runs TypeScriptCompiler.compileApp()
                                    for each @NgApp (worker pool, reactive Uni pipeline)
```

### TypeScript Compiler Pipeline

```
TypeScriptCompiler
 ├── AngularAppSetup        → scaffolds angular.json, package.json, tsconfig, .gitignore
 ├── DependencyManager      → resolves @TsDependency / @TsDevDependency
 ├── ComponentProcessor     → processes each INgComponent, INgDirective, INgDataService, etc.
 ├── AngularModuleProcessor → generates boot module, routing module, module imports
 ├── TypeScriptCodeGenerator → renders .ts files from annotation metadata
 ├── AssetManager           → copies @NgAsset, @NgScript, @NgStyleSheet resources
 └── TypeScriptCodeValidator → validates generated output
```

### Vert.x Router Wiring

| Route | Method | Handler | Purpose |
|---|---|---|---|
| `/eventbus/*` | WebSocket | STOMP server | WebSocket → STOMP bridge |
| `/assets/*` | GET | StaticHandler | Angular compiled assets (1-year cache) |
| `/{file}.{ext}` (excl. `index.html`) | GET | StaticHandler | Root-level static files |
| `/**` (SPA fallback) | GET | `sendFile(index.html)` | Angular Router client-side routes |
| Route-per-`@NgRoutable` | GET | StaticHandler | Component-level route bindings |

### STOMP/WebSocket Message Flow

```
Angular Client (STOMP)
 └─ ws://host/eventbus
     ├─ SEND /toBus/incoming    → event bus consumer
     │   ├─ deserialise WebSocketMessageReceiver
     │   ├─ dispatch to registered IWebSocketMessageReceiver (ajax / data / dataSend / group ops)
     │   ├─ run in CallScope (CallScopeSource.WebSocket)
     │   └─ reply or publish results back:
     │       ├─ dataReturns → publish to per-key event bus addresses
     │       ├─ sessionStorage → publish to "SessionStorage"
     │       └─ localStorage → publish to "LocalStorage"
     └─ SUBSCRIBE /toStomp/*   ← server pushes via StompEventBusPublisher
```

### WebSocket Message Receivers

| Receiver | Action | Purpose |
|---|---|---|
| `WebSocketAjaxCallReceiver` | `ajax` | Deserialises `AjaxCall`, resolves `IEvent`, fires event reactively, returns `AjaxResponse` |
| `WebSocketDataRequestCallReceiver` | `data` | Resolves `INgDataService`, calls `getData()`, adds to response data returns |
| `WebSocketDataSendCallReceiver` | `dataSend` | Resolves `INgDataService`, calls `receiveData()` |
| `WSAddToGroupMessageReceiver` | `AddToWebSocketGroup` | Adds session to a WebSocket group |
| `WSRemoveFromWebsocketGroupMessageReceiver` | `RemoveFromWebSocketGroup` | Removes session from a WebSocket group |

### Angular Control-Flow Components

| Java Class | Angular Output | Purpose |
|---|---|---|
| `NgIf` | `@if (condition) { ... }` | Conditional rendering |
| `NgIfElse` | `@if (condition) { ... } @else { ... }` | Conditional with else |
| `NgElse` | `@else { ... }` | Else block |
| `NgFor` | `@for (item of list; track trackBy) { ... }` | Loop with track, index, count, odd/even/first/last |
| `NgForEmpty` | `@empty { ... }` | Empty block for `@for` |
| `NgLet` | `@let variable = expression;` | Local variable declaration |

### Key Classes

| Class | Role |
|---|---|
| `AngularTSSiteBinder` | Core Guice module + `VertxRouterConfigurator` + `VertxHttpServerOptionsConfigurator` — binds environment, configures STOMP, wires router, processes inbound messages |
| `AngularTSPostStartup` | Post-startup hook that runs `TypeScriptCompiler.compileApp()` for each `@NgApp` |
| `AngularPreStartup` | Pre-startup hook that disables JWebMP page routing (`BIND_JW_PAGES=false`) |
| `TypeScriptCompiler` | Orchestrates all compiler stages (setup, dependencies, components, modules, assets, validation) |
| `NGApplication` | Base class for `@NgApp` — extends `Page`, implements `INgApp` + `INgComponent`, sets up HTML head/base/meta |
| `AngularRoutingModule` | Scans `@NgRoutable` classes, builds route tree, generates `RouterModule.forRoot(routes)` |
| `EnvironmentModule` | Generates `const environment = { ... }` from `EnvironmentOptions` |
| `StompEventBusPublisher` | Static helper for publishing to `/toStomp/<address>` |
| `ConfigureImportReferences` | `IOnComponentConfigured` that processes the component tree and configures Angular import references |
| `RouterLink` | Server-side anchor component with `[routerLink]`, `[queryParams]`, `[state]` bindings |
| `AngularWebSocket` | Helper for binding `websocketjw`, `websocketgroup`, `websocketdata` attributes to components |

### SPI Extension Points

| SPI | Purpose |
|---|---|
| `AngularScanPackages` | Add additional packages to the Angular classpath scan |
| `RenderedAssets` | Provide additional assets to include in the build |
| `NpmrcConfigurator` | Customise `.npmrc` file lines |
| `WebSocketGroupAdd` | Custom logic when a component joins a WebSocket group |
| `TypescriptIndexPageConfigurator` | Customise the generated `index.html` |
| `IWebSocketAuthDataProvider` | Provide authentication data for WebSocket connections |
| `IPageConfigurator` | Standard JWebMP page configuration hooks |

## ⚙️ Configuration

| Environment Variable / System Property | Default | Purpose |
|---|---|---|
| `JWEBMP_PROCESS_ANGULAR_TS` / `jwebmp.process.angular.ts` | `false` | Enable/disable TypeScript generation on startup |
| `jwebmp.outputDirectory` | — | Override the generated output directory |
| `jwebmp` | `~` (user home) | Base directory (output goes to `<base>/.jwebmp/<appName>`) |
| `ENVIRONMENT` | `dev` | Runtime environment hint for `EnvironmentOptions` |
| `PORT` | `8080` | Server port |

### STOMP Server Options

| Setting | Value | Notes |
|---|---|---|
| WebSocket path | `/eventbus` | STOMP over WebSocket endpoint |
| Server heartbeat (x) | `10000` ms | Server → client heartbeat interval |
| Client heartbeat (y) | `50000` ms | Expected client → server interval (lenient for background tabs) |
| Max body/header/frame | `Integer.MAX_VALUE` | No size limits |
| Sub-protocols | `v10.stomp`, `v11.stomp`, `v12.stomp` | Advertised on HTTP upgrade |
| Idle timeout | `0` (disabled) | Relies on STOMP heartbeats instead |
| TCP keep-alive | `true` | For intermediary NATs/proxies |
| Compression | `true` | Both request and response |

### STOMP Bridge Patterns

| Direction | Address Pattern | Purpose |
|---|---|---|
| Inbound | `/toBus.*` | Client → server messages |
| Outbound | `/toStomp.*` | Server → client pushes |

## 🔗 Dependencies

```
com.jwebmp.core.angular
 ├── com.jwebmp.core.base.angular.client  (TypeScript Client — annotations, interfaces, EventBusService)
 ├── com.jwebmp.vertx                     (JWebMP Vert.x bridge — HTTP routes, call scopes)
 ├── com.jwebmp.core                      (JWebMP Core — pages, events, AJAX)
 ├── com.guicedee.guicedinjection         (Guice DI + classpath scanning)
 ├── io.vertx.stomp                       (Vert.x STOMP server)
 ├── io.vertx.eventbusbridge              (Vert.x event bus bridge)
 ├── org.apache.commons.text              (Template substitution)
 ├── org.apache.commons.lang3             (ExceptionUtils, StringUtils)
 ├── org.apache.commons.io                (FileUtils, IOUtils)
 └── org.mapstruct                        (DTO mapping — annotation processor)
```

## 🛠️ Build

- **Java**: 25 LTS
- **Maven**: inherits `com.jwebmp:parent:2.0.0-SNAPSHOT`
- **JPMS**: module descriptor at `src/main/java/module-info.java`
- **Annotation processors**: Lombok + MapStruct (configured in `pom.xml`)

```bash
mvn clean install
```

Run tests:

```bash
mvn test
```

## 🔄 CI

GitHub Actions workflow at `.github/workflows/maven-package.yml` (GuicedEE shared workflow).

Required secrets: `USERNAME`, `USER_TOKEN`, `SONA_USERNAME`, `SONA_PASSWORD`.

Example environment file: `.env.example`.

## 📖 Documentation

| Document | Path |
|---|---|
| Architecture overview | `docs/architecture/overview.md` |
| Architecture diagrams index | `docs/architecture/README.md` |
| C4 Context | `docs/architecture/c4-context.md` |
| C4 Container | `docs/architecture/c4-container.md` |
| C4 Component | `docs/architecture/c4-component-angular.md` |
| TS generation sequence | `docs/architecture/sequence-typescript-generation.md` |
| WebSocket/STOMP sequence | `docs/architecture/sequence-websocket-stomp.md` |
| Dependency map | `docs/architecture/dependency-map.md` |
| ERD | `docs/architecture/erd-angular-plugin.md` |
| Interaction flows | `docs/architecture/interaction-flows.md` |

## 🤝 Contributing

Issues and pull requests are welcome. Use Log4j2 for logging and keep generated artifacts out of PRs.

## 📄 License

[Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0)

```mermaid
graph TD
    subgraph Plugin["JWebMP Angular Plugin"]
        lifecycle[AngularPreStartup<br/>AngularTSPostStartup<br/>AngularTsProcessingConfig]
        compiler[JWebMPTypeScriptCompiler<br/>(migration path: TypeScriptCompiler)]
        routing[AngularRoutingModule<br/>DefinedRoute tree]
        binder[AngularTSSiteBinder<br/>Vert.x router + STOMP bridge]
    end

    subgraph Runtime["Runtime Environment"]
        vertx[Vert.x 5 HTTP + Event Bus + STOMP]
        guice[GuicedEE Core / Client / Web / WebSocket]
        angularSpa[Angular 20 SPA dist/ assets]
        listeners[IGuicedWebSocket listeners]
    end

    subgraph Tooling["Tooling & Observability"]
        maven[Maven build<br/>Java 25 LTS]
        logging[Log4j2 via Lombok @Log4j2]
        testing[JUnit + Jacoco<br/>Java Micro Harness / BrowserStack (planned)]
    end

    maven --> lifecycle
    lifecycle --> compiler
    lifecycle --> binder
    compiler --> routing
    compiler --> angularSpa
    routing --> angularSpa
    binder --> vertx
    vertx --> angularSpa
    vertx --> listeners
    listeners --> vertx
    guice --> lifecycle
    guice --> binder
    logging --> binder
    logging --> lifecycle
    testing --> maven

    click compiler href "./overview.md" "Generation overview"
    click binder href "./sequence-websocket-stomp.md" "WebSocket/STOMP sequence"
```

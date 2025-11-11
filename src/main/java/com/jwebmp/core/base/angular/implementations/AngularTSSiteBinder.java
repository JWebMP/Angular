/*
 * Copyright (C) 2017 GedMarc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jwebmp.core.base.angular.implementations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.guicedee.client.CallScopeProperties;
import com.guicedee.client.CallScopeSource;
import com.guicedee.client.CallScoper;
import com.guicedee.client.IGuiceContext;
import com.guicedee.guicedinjection.interfaces.IGuiceModule;
import com.guicedee.guicedinjection.properties.GlobalProperties;
import com.guicedee.guicedservlets.websockets.options.IGuicedWebSocket;
import com.guicedee.guicedservlets.websockets.options.WebSocketMessageReceiver;
import com.guicedee.services.jsonrepresentation.IJsonRepresentation;
import com.guicedee.vertx.spi.VertXPreStartup;
import com.guicedee.vertx.web.spi.VertxHttpServerOptionsConfigurator;
import com.guicedee.vertx.web.spi.VertxRouterConfigurator;
import com.jwebmp.core.annotations.PageConfiguration;
import com.jwebmp.core.base.ajax.AjaxResponse;
import com.jwebmp.core.base.angular.client.AppUtils;
import com.jwebmp.core.base.angular.client.DynamicData;
import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import com.jwebmp.core.base.angular.modules.services.angular.AngularRoutingModule;
import com.jwebmp.core.base.angular.modules.services.base.EnvironmentModule;
import com.jwebmp.core.base.angular.services.DefinedRoute;
import com.jwebmp.core.base.angular.services.compiler.JWebMPTypeScriptCompiler;
import io.github.classgraph.ClassInfo;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeEvent;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.stomp.BridgeOptions;
import io.vertx.ext.stomp.StompServer;
import io.vertx.ext.stomp.StompServerHandler;
import io.vertx.ext.stomp.StompServerOptions;
import io.vertx.core.http.HttpServerOptions;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import io.smallrye.mutiny.Uni;

/**
 * @author GedMarc
 * @version 1.0
 * @since 20 Dec 2016
 */
@Log4j2
public class AngularTSSiteBinder
        extends AbstractModule
        implements IGuiceModule<AngularTSSiteBinder>, VertxRouterConfigurator, VertxHttpServerOptionsConfigurator
{
    @Inject
    private Vertx vertx;

    private File siteHostingLocation;

    public Uni<AjaxResponse<?>> receiveMessage(WebSocketMessageReceiver<?> messageReceived)
    {
        return Uni.createFrom()
                  .item(messageReceived)
                  .onItem()
                  .transformToUni(m -> {
                      CallScoper callScoper = IGuiceContext.get(CallScoper.class);
                      boolean startedScope = callScoper.isStartedScope();
                      if (!startedScope)
                      {
                          callScoper.enter();
                      }
                      var callScopeProperties = IGuiceContext.get(CallScopeProperties.class);
                      if (callScopeProperties.getSource() == null)
                      {
                          callScopeProperties.setSource(CallScopeSource.WebSocket);
                      }

                      callScopeProperties.getProperties()
                                         .put("RequestContextId", m.getWebSocketSessionId());
                      String requestContextId = String.valueOf(callScopeProperties.getProperties()
                                                                                  .get("RequestContextId"));
                      m.setBroadcastGroup(requestContextId);

                      if (IGuicedWebSocket.getMessagesListeners()
                                          .containsKey(m.getAction()))
                      {
                          return IGuicedWebSocket.getMessagesListeners()
                                                 .get(m.getAction())
                                                 .receiveMessage(m)
                                                 .onFailure()
                                                 .invoke(err -> log.error("ERROR Message Received - Message={}", m.toString(), err))
                                                 .eventually(() -> {
                                                     if (!startedScope)
                                                     {
                                                         callScoper.exit();
                                                     }
                                                 });
                      }
                      else
                      {
                          log.warn("No web socket action registered for {}", m.getAction());
                          AjaxResponse<?> ar = IGuiceContext.get(AjaxResponse.class);
                          return Uni.createFrom()
                                    .item(new AjaxResponse<>())
                                    .eventually(() -> {
                                        if (!startedScope)
                                        {
                                            callScoper.exit();
                                        }
                                    });
                      }
                  })
                  .replaceWith(new AjaxResponse<>());
    }

    /**
     * Method onBind ...
     */
    @Override
    public void configure()
    {
        bind(EnvironmentModule.class)
                .toInstance(new EnvironmentModule());

        for (ClassInfo classInfo : IGuiceContext.instance()
                                                .getScanResult()
                                                .getClassesWithAnnotation(NgApp.class))
        {
            if (classInfo.isAbstract() || classInfo.isInterface())
            {
                continue;
            }

            Class<?> loadClass = classInfo.loadClass();
            NgApp app = loadClass.getAnnotation(NgApp.class);
            PageConfiguration pc = loadClass.getAnnotation(PageConfiguration.class);
            String userDir = GlobalProperties.getSystemPropertyOrEnvironment("JWEBMP_ROOT_PATH", new File(System.getProperty("user.dir"))
                    .getPath());
            siteHostingLocation = new File(userDir + "/webroot/");
            try
            {
                FileUtils.forceMkdirParent(siteHostingLocation);
                FileUtils.forceMkdir(siteHostingLocation);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            AngularTSPostStartup.basePath = siteHostingLocation.toPath();
            log.info("Serving Angular TS for defined @NgApp {} at  {}", app.value(), siteHostingLocation.getPath());
        }
    }


    private String bindRouteToPath(Router router, String path, String staticFileLocationPath, File file, DefinedRoute<?> route)
    {
        String newPath = route.getPath();
        if (!newPath.startsWith("/"))
        {
            newPath = "/" + newPath;
        }
        newPath = path + newPath;
        if (newPath.endsWith("/**"))
        {
            newPath = newPath.replace("/**", "/*");
        }
        log.debug("Configuring route - {}", newPath);
        router.route(newPath)
              .handler(StaticHandler.create(FileSystemAccess.ROOT, staticFileLocationPath)
                                    .setAlwaysAsyncFS(true)
                                    .setCacheEntryTimeout(604800)
                                    .setCachingEnabled(true)
                                    .setDefaultContentEncoding("UTF-8")
                                    .setDirectoryListing(false)
                                    .setEnableFSTuning(true)
                                    .setIncludeHidden(false)
                                    .setMaxAgeSeconds(604800)
                                    .setSendVaryHeader(true)
              );

        if (route.getChildren() != null && !route.getChildren()
                                                 .isEmpty())
        {
            for (DefinedRoute<?> child : route.getChildren())
            {
                bindRouteToPath(router, newPath, staticFileLocationPath, file, child);
            }
        }
        return newPath;
    }


    @Override
    public Integer sortOrder()
    {
        return Integer.MIN_VALUE + 100;
    }

    @Override
    public Router builder(Router router)
    {
        System.setProperty("vertx.disableFileCPResolving", "true");
        for (INgApp<?> app : JWebMPTypeScriptCompiler.getAllApps())
        {
            try
            {
                String staticFileLocationPath = AppUtils.getDistPath((Class<? extends INgApp<?>>) app.getClass())
                                                        .getCanonicalPath();

                JsonObject heartbeats = new JsonObject()
                        // server -> client heartbeat period in ms
                        .put("x", 10000)
                        // client -> server expected heartbeat period in ms
                        .put("y", 10000);


                StompServerOptions stompOptions = new StompServerOptions()
                        .setWebsocketBridge(true)
                        .setHeartbeat(heartbeats)
                        .setWebsocketPath("/eventbus");

                log.info("Configuring STOMP server with WebSocket bridge at /eventbus");
                BridgeOptions stompBridgeOptions = new BridgeOptions()
                        .addInboundPermitted(new PermittedOptions().setAddressRegex("/toBus.*"))
                        .addOutboundPermitted(new PermittedOptions().setAddressRegex("/toStomp.*"));

                log.info("STOMP bridge configured with inbound pattern /toBus.* and outbound pattern /toStomp.*");

                StompServer stompServer = StompServer.create(vertx, stompOptions)
                                                     .handler(StompServerHandler.create(vertx)
                                                                                .bridge(stompBridgeOptions));

                // Register WebSocket handlers
                log.info("Registering WebSocket handler for STOMP at /eventbus/*");
                router.route("/eventbus/*")
                      .handler(ctx -> {
                          log.info("Received WebSocket connection request from: " + ctx.request()
                                                                                       .remoteAddress());
                          ctx.request()
                             .toWebSocket()
                             .onSuccess(ws -> {
                                 log.info("WebSocket connection established, passing to STOMP handler");
                                 stompServer.webSocketHandler()
                                            .handle(ws);
                             })
                             .onFailure(err -> {
                                 log.error("Failed to establish WebSocket connection: " + err.getMessage(), err);
                             });
                      });

                // This executes when a websocket message is received via STOMP
                log.info("Registering event bus consumer for STOMP messages at /toBus/incoming");
                vertx.eventBus()
                     .consumer("/toBus/incoming", handler -> {
                         var o = handler.body();
                         log.trace("Received message on /toBus/incoming: " + o);

                         if (o instanceof Buffer buffer)
                         {
                             String message = buffer.toString();
                             WebSocketMessageReceiver<?> mr;
                             try
                             {
                                 mr = IJsonRepresentation.getObjectMapper()
                                                         .readerFor(WebSocketMessageReceiver.class)
                                                         .readValue(message);
                             }
                             catch (JsonProcessingException e)
                             {
                                 throw new RuntimeException(e);
                             }
                             if (mr.getData()
                                   .containsKey("guid"))
                             {
                                 mr.setWebSocketSessionId(mr.getData()
                                                            .get("guid")
                                                            .toString());
                             }
                             if (mr.getData()
                                   .containsKey("dataService"))
                             {
                                 mr.setBroadcastGroup(mr.getData()
                                                        .get("dataService")
                                                        .toString());
                             }

                             WebSocketMessageReceiver<?> finalMr = mr;
                             receiveMessage(finalMr).subscribe()
                                                    .with(ajaxResponse -> {
                                                        DeliveryOptions options = new DeliveryOptions()
                                                                .addHeader("Content-Type", "application/json");

                                                        if (ajaxResponse.getSessionStorage() != null && !ajaxResponse.getSessionStorage()
                                                                                                                     .isEmpty())
                                                        {
                                                            vertx.eventBus()
                                                                 .publish("SessionStorage", ajaxResponse.getSessionStorage());
                                                        }
                                                        if (ajaxResponse.getLocalStorage() != null && !ajaxResponse.getLocalStorage()
                                                                                                                   .isEmpty())
                                                        {
                                                            vertx.eventBus()
                                                                 .publish("LocalStorage", ajaxResponse.getLocalStorage());
                                                        }
                                                        if (ajaxResponse.getDataReturns() != null)
                                                        {
                                                            handler.reply("{}");
                                                            ajaxResponse.getDataReturns()
                                                                        .forEach((key, value) -> {
                                                                            if (value instanceof DynamicData dd)
                                                                            {
                                                                                for (Object object : dd.getOut())
                                                                                {
                                                                                    vertx.eventBus()
                                                                                         .publish(key, object);
                                                                                }
                                                                            }
                                                                            else
                                                                            {
                                                                                vertx.eventBus()
                                                                                     .publish(key, value);
                                                                            }
                                                                        });
                                                        }
                                                        else
                                                        {
                                                            handler.reply(ajaxResponse, options);
                                                        }
                                                    }, failure -> {
                                                        log.fatal("Failed to process message: " + failure.getMessage());
                                                        handler.fail(500, failure.getMessage());
                                                    });
                             //   handler.reply("{}");
                         }
                         else if (o instanceof JsonObject jo)
                         {
                             String jsonString = jo.toString();
                             log.info("Processing JSON message: " + jsonString);
                             var mr = jo.mapTo(WebSocketMessageReceiver.class);
                             if (mr.getData()
                                   .containsKey("guid"))
                             {
                                 mr.setWebSocketSessionId(mr.getData()
                                                            .get("guid")
                                                            .toString());
                             }
                             if (mr.getData()
                                   .containsKey("dataService"))
                             {
                                 mr.setBroadcastGroup(mr.getData()
                                                        .get("dataService")
                                                        .toString());
                             }
                             receiveMessage(mr).subscribe()
                                               .with(ajaxResponse -> {
                                                   DeliveryOptions options = new DeliveryOptions()
                                                           .addHeader("Content-Type", "application/json");

                                                   if (ajaxResponse.getSessionStorage() != null && !ajaxResponse.getSessionStorage()
                                                                                                                .isEmpty())
                                                   {
                                                       vertx.eventBus()
                                                            .publish("SessionStorage", ajaxResponse.getSessionStorage());
                                                   }
                                                   if (ajaxResponse.getLocalStorage() != null && !ajaxResponse.getLocalStorage()
                                                                                                              .isEmpty())
                                                   {
                                                       vertx.eventBus()
                                                            .publish("LocalStorage", ajaxResponse.getLocalStorage());
                                                   }
                                                   if (ajaxResponse.getDataReturns() != null)
                                                   {
                                                       handler.reply("{}");
                                                       ajaxResponse.getDataReturns()
                                                                   .forEach((key, value) -> {
                                                                       if (value instanceof DynamicData dd)
                                                                       {
                                                                           for (Object object : dd.getOut())
                                                                           {
                                                                               vertx.eventBus()
                                                                                    .publish(key, object);
                                                                           }
                                                                       }
                                                                       else
                                                                       {
                                                                           vertx.eventBus()
                                                                                .publish(key, value);
                                                                       }
                                                                   });
                                                   }
                                                   else
                                                   {
                                                       handler.reply(ajaxResponse, options);
                                                   }
                                               }, failure -> {
                                                   log.fatal("Failed to process message: " + failure.getMessage());
                                                   handler.fail(500, failure.getMessage());
                                               });
                             //   handler.reply("{}");
                         }
                         else
                         {
                             log.fatal("Failed to process message: " + o.toString() + " - " + o.getClass()
                                                                                               .getCanonicalName());
                         }
                     });


                String path = "";
                for (DefinedRoute<?> route : AngularRoutingModule.getRoutes(app))
                {
                    bindRouteToPath(router, path, staticFileLocationPath, siteHostingLocation, route);
                }
                log.debug("Configuring parent route - {}", staticFileLocationPath);

                // 1) Explicit /assets/* mount from the dist assets directory
                String assetsStaticDir = staticFileLocationPath + File.separator + "assets";
                router.get("/assets/*")
                      .handler(StaticHandler.create(FileSystemAccess.ROOT, assetsStaticDir)
                                            .setAlwaysAsyncFS(false)
                                            .setCacheEntryTimeout(604800)
                                            .setCachingEnabled(false)
                                            .setDefaultContentEncoding("UTF-8")
                                            .setDirectoryListing(false)
                                            .setEnableFSTuning(false)
                                            .setIncludeHidden(false)
                                            .setMaxAgeSeconds(604800)
                                            .setSendVaryHeader(false)
                      );

                // 2) Real-file handler with root aliasing for assets:
                // Try to serve from dist root first; if not found, fall back to dist/assets
                router.getWithRegex("^/.+\\.[^/]+$")
                      .handler(ctx -> {
                          String normalized = ctx.normalizedPath();
                          if (normalized == null)
                          {
                              ctx.next();
                              return;
                          }
                          // Strip leading '/'
                          String rel = normalized.startsWith("/") ? normalized.substring(1) : normalized;
                          File primary = new File(staticFileLocationPath, rel);
                          if (primary.exists() && primary.isFile())
                          {
                              ctx.response()
                                 .sendFile(primary.getAbsolutePath());
                              return;
                          }
                          File assetsFile = new File(assetsStaticDir, rel);
                          if (assetsFile.exists() && assetsFile.isFile())
                          {
                              ctx.response()
                                 .sendFile(assetsFile.getAbsolutePath());
                              return;
                          }
                          // Not a real file in root or assets; allow SPA fallback to handle
                          ctx.next();
                      });

                // 3) SPA fallback: for any path that is not a file (no extension) and not under known backend/ws prefixes,
                // serve index.html so Angular Router can handle client-side routes (including parameterized ones).
                router.getWithRegex("^/(?!api/|eventbus|sockjs|stomp|assets/|media/|.*\\.[^/]+).*$")
                      .handler(ctx -> ctx.response()
                                         .sendFile(staticFileLocationPath + File.separator + "index.html"));
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        return router;
    }

    @Override
    public HttpServerOptions builder(HttpServerOptions builder)
    {
        builder.setWebSocketSubProtocols(java.util.Arrays.asList("v10.stomp", "v11.stomp", "v12.stomp"));
        return builder;
    }
}

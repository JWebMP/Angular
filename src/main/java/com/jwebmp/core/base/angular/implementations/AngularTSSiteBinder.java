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

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.guicedee.client.CallScoper;
import com.guicedee.client.IGuiceContext;
import com.guicedee.guicedinjection.interfaces.IGuiceModule;
import com.guicedee.guicedinjection.properties.GlobalProperties;
import com.guicedee.guicedservlets.websockets.options.CallScopeProperties;
import com.guicedee.guicedservlets.websockets.options.CallScopeSource;
import com.guicedee.guicedservlets.websockets.options.IGuicedWebSocket;
import com.guicedee.guicedservlets.websockets.options.WebSocketMessageReceiver;
import com.guicedee.vertx.web.spi.VertxRouterConfigurator;
import com.jwebmp.core.annotations.PageConfiguration;
import com.jwebmp.core.base.ajax.AjaxResponse;
import com.jwebmp.core.base.angular.client.AppUtils;
import com.jwebmp.core.base.angular.client.DynamicData;
import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import com.jwebmp.core.base.angular.modules.services.angular.RoutingModule;
import com.jwebmp.core.base.angular.modules.services.base.EnvironmentModule;
import com.jwebmp.core.base.angular.services.DefinedRoute;
import com.jwebmp.core.base.angular.services.compiler.JWebMPTypeScriptCompiler;
import io.github.classgraph.ClassInfo;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
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
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author GedMarc
 * @version 1.0
 * @since 20 Dec 2016
 */
@Log4j2
public class AngularTSSiteBinder
        extends AbstractModule
        implements IGuiceModule<AngularTSSiteBinder>, VertxRouterConfigurator
{
    @Inject
    private Vertx vertx;

    private WorkerExecutor workerExecutor;

    private File siteHostingLocation;

    @Inject
    void setup()
    {
        workerExecutor = vertx.createSharedWorkerExecutor("angular-site-worker-pool");
    }

    public void receiveMessage(WebSocketMessageReceiver<?> messageReceived)
    {
        try
        {
            var callScopeProperties = IGuiceContext.get(CallScopeProperties.class);
            String requestContextId = callScopeProperties.getProperties()
                    .get("RequestContextId")
                    .toString();
            messageReceived.setBroadcastGroup(requestContextId);
            if (IGuicedWebSocket.getMessagesListeners()
                    .containsKey(messageReceived.getAction()))
            {
                IGuicedWebSocket.getMessagesListeners()
                        .get(messageReceived.getAction())
                        .receiveMessage(messageReceived);
            } else
            {
                log.warn("No web socket action registered for {}", messageReceived.getAction());
            }
        } catch (Exception e)
        {
            log.error("ERROR Message Received - Message={}", messageReceived.toString(), e);
        }
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
            //  for (NgApp app : IGuiceContext.get(AnnotationHelper.class)
            //                                .getAnnotationFromClass(loadClass, NgApp.class))
            //   {
            PageConfiguration pc = loadClass.getAnnotation(PageConfiguration.class);
            String userDir = GlobalProperties.getSystemPropertyOrEnvironment("JWEBMP_ROOT_PATH", new File(System.getProperty("user.dir"))
                    .getPath());

            siteHostingLocation = new File(userDir + "/webroot/");
            try
            {
                FileUtils.forceMkdirParent(siteHostingLocation);
                FileUtils.forceMkdir(siteHostingLocation);
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            AngularTSPostStartup.basePath = siteHostingLocation.toPath();
            log.info("Serving Angular TS for defined @NgApp {} at  {}", app.value(), siteHostingLocation.getPath());

            String path = "";
           /* for (DefinedRoute<?> route : RoutingModule.getRoutes())
            {
                bindRouteToPath(path, siteHostingLocation, route);
            }*/
        }
        //}


        //JWebMPSiteBinder.bindSites = false;
        //JWebMPJavaScriptDynamicScriptRenderer.renderJavascript = false;

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

    private void handleBridgeEvent(BridgeEvent event)
    {
        if (event.type() == BridgeEventType.SEND || event.type() == BridgeEventType.PUBLISH)
        {
            String address = event.getRawMessage().getString("address"); // Address message was received from
            //System.out.println("Message received at address: " + address);
        }
        event.complete(true); // Allow the event to proceed
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

                //bind sockjs event bridge

                SockJSBridgeOptions bridgeOptions = new SockJSBridgeOptions();
                bridgeOptions.setPingTimeout(60000);
                bridgeOptions.setMaxAddressLength(64 * 1024);
                bridgeOptions.setReplyTimeout(20000);
                SockJSHandlerOptions handlerOptions = new SockJSHandlerOptions();
                handlerOptions.setHeartbeatInterval(30000);
                handlerOptions.setRegisterWriteHandler(true);

                bridgeOptions.addInboundPermitted(new PermittedOptions().setAddressRegex(".*"));
                bridgeOptions.addOutboundPermitted(new PermittedOptions().setAddressRegex(".*"));

                SockJSHandler sockJSHandler = SockJSHandler.create(vertx, handlerOptions);
                // mount the bridge on the router
                router
                        .route("/eventbus/*")
                        .subRouter(sockJSHandler.bridge(bridgeOptions, event -> handleBridgeEvent(event)));

                vertx.eventBus().consumer("incoming", handler -> {
                    var o = handler.body();
                    if (o instanceof JsonObject jo)
                    {
                        String jsonString = jo.toString();
                        var mr = jo.mapTo(WebSocketMessageReceiver.class);
                        if (mr.getData().containsKey("guid"))
                        {
                            mr.setWebSocketSessionId(mr.getData().get("guid").toString());
                        }
                        if (mr.getData().containsKey("dataService"))
                        {
                            mr.setBroadcastGroup(mr.getData().get("dataService").toString());
                        }
                        workerExecutor.executeBlocking(() -> {
                                    // Blocking code here
                                    CallScoper callScoper = IGuiceContext.get(CallScoper.class);
                                    callScoper.enter();
                                    try
                                    {
                                        CallScopeProperties props = IGuiceContext.get(CallScopeProperties.class);
                                        props.setSource(CallScopeSource.WebSocket);
                                        props.getProperties()
                                                .put("RequestContextId", mr.getWebSocketSessionId());
                                        receiveMessage(mr);
                                        AjaxResponse<?> ar = IGuiceContext.get(AjaxResponse.class);
                                        return ar;
                                    } finally
                                    {
                                        callScoper.exit(); // Always exit the scope
                                    }
                                })
                                .onComplete(complete -> {
                                    //System.out.println("Message processing completed successfully.");
                                    var ajaxResponse = complete.result();
                                    DeliveryOptions options = new DeliveryOptions()
                                            .addHeader("Content-Type", "application/json");

                                    if (ajaxResponse.getSessionStorage() != null && !ajaxResponse.getSessionStorage().isEmpty())
                                    {
                                        // send session storage updates
                                        vertx.eventBus().publish("SessionStorage", ajaxResponse.getSessionStorage());
                                    }
                                    if (ajaxResponse.getLocalStorage() != null && !ajaxResponse.getLocalStorage().isEmpty())
                                    {
                                        // send local storage updates
                                        vertx.eventBus().publish("LocalStorage", ajaxResponse.getLocalStorage());
                                    }
                                    if (ajaxResponse.getDataReturns() != null)
                                    {
                                        handler.reply("{}");
                                        //String listenerName = ajaxResponse.getDataReturns().get("listenerName").toString();
                                        ajaxResponse.getDataReturns().forEach((key, value) -> {
                                            if (value instanceof DynamicData dd)
                                            {
                                                for (Object object : dd.getOut())
                                                {
                                                    vertx.eventBus().publish(key, object);
                                                }
                                            } else
                                                vertx.eventBus().publish(key, value);
                                        });
                                    } else
                                        handler.reply(complete.result(), options);

                                    /*if (!ajaxResponse.getDataReturns().isEmpty())
                                    {
                                        ajaxResponse.getDataReturns().forEach((key, value) -> {
                                            vertx.eventBus().publish(key, value);
                                        });
                                    }*/
                                    //vertx.eventBus().publish()
                                    //handler.reply(complete.result(), options);
                                })
                                .onFailure(res -> {
                                    System.err.println("Failed to process message: " + res.getMessage());
                                    handler.fail(500, res.getMessage()); // Notify sender of fa

                                });
                        //   handler.reply("{}");
                    }
                });



/*

                router.get("/*.js")
                      .handler(StaticHandler.create(FileSystemAccess.ROOT, staticFileLocationPath)
                                            .setAlwaysAsyncFS(true)
                                            .setCacheEntryTimeout(604800)
                                            .setCachingEnabled(true)
                                            .setDefaultContentEncoding("UTF-8")
                                            .setDirectoryListing(false)
                                            .setEnableFSTuning(true)
                                            .setIncludeHidden(false)
                                            .setMaxAgeSeconds(604800)
                                            .setSendVaryHeader(true));
                String assetsStaticDir = FilenameUtils.concat(staticFileLocationPath, "assets/");
                router.get("/assets/*")
                      .handler(StaticHandler.create(FileSystemAccess.ROOT, assetsStaticDir)
                                            .setAlwaysAsyncFS(true)
                                            .setCacheEntryTimeout(604800)
                                            .setCachingEnabled(true)
                                            .setDefaultContentEncoding("UTF-8")
                                            .setDirectoryListing(false)
                                            .setEnableFSTuning(true)
                                            .setIncludeHidden(false)
                                            .setMaxAgeSeconds(604800)
                                            .setSendVaryHeader(true));
                String mediaStaticDir = FilenameUtils.concat(staticFileLocationPath, "media/");
                router.get("/media/*")
                      .handler(StaticHandler.create(FileSystemAccess.ROOT, mediaStaticDir)
                                            .setAlwaysAsyncFS(true)
                                            .setCacheEntryTimeout(604800)
                                            .setCachingEnabled(true)
                                            .setDefaultContentEncoding("UTF-8")
                                            .setDirectoryListing(false)
                                            .setEnableFSTuning(true)
                                            .setIncludeHidden(false)
                                            .setMaxAgeSeconds(604800)
                                            .setSendVaryHeader(true));
                router.get("/*.css")
                      .handler(StaticHandler.create(FileSystemAccess.ROOT, staticFileLocationPath)
                                            .setAlwaysAsyncFS(true)
                                            .setCacheEntryTimeout(604800)
                                            .setCachingEnabled(true)
                                            .setDefaultContentEncoding("UTF-8")
                                            .setDirectoryListing(false)
                                            .setEnableFSTuning(true)
                                            .setIncludeHidden(false)
                                            .setMaxAgeSeconds(604800)
                                            .setSendVaryHeader(true));
                router.get("/*.map")
                      .handler(StaticHandler.create(FileSystemAccess.ROOT, staticFileLocationPath)
                                            .setAlwaysAsyncFS(true)
                                            .setCacheEntryTimeout(604800)
                                            .setCachingEnabled(true)
                                            .setDefaultContentEncoding("UTF-8")
                                            .setDirectoryListing(false)
                                            .setEnableFSTuning(true)
                                            .setIncludeHidden(false)
                                            .setMaxAgeSeconds(604800)
                                            .setSendVaryHeader(true));

*/

                String path = "";
                for (DefinedRoute<?> route : RoutingModule.getRoutes(app))
                {
                    bindRouteToPath(router, path, staticFileLocationPath, siteHostingLocation, route);
                }
                log.debug("Configuring parent route - {}", staticFileLocationPath);
                router.get("/*")
                        .handler(StaticHandler.create(FileSystemAccess.ROOT, staticFileLocationPath)
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
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        return router;
    }
}

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
import com.guicedee.client.IGuiceContext;
import com.guicedee.guicedinjection.interfaces.IGuiceModule;
import com.guicedee.guicedinjection.properties.GlobalProperties;
import com.guicedee.vertx.spi.VertxRouterConfigurator;
import com.jwebmp.core.annotations.PageConfiguration;
import com.jwebmp.core.base.angular.client.AppUtils;
import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import com.jwebmp.core.base.angular.modules.services.angular.RoutingModule;
import com.jwebmp.core.base.angular.modules.services.base.EnvironmentModule;
import com.jwebmp.core.base.angular.services.DefinedRoute;
import com.jwebmp.core.base.angular.services.compiler.JWebMPTypeScriptCompiler;
import io.github.classgraph.ClassInfo;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * @author GedMarc
 * @version 1.0
 * @since 20 Dec 2016
 */
@Log
public class AngularTSSiteBinder
        extends AbstractModule
        implements IGuiceModule<AngularTSSiteBinder>, VertxRouterConfigurator
{
    @Inject
    private Vertx vertx;

    private File siteHostingLocation;

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
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            AngularTSPostStartup.basePath = siteHostingLocation.toPath();
            AngularTSSiteBinder.log.log(Level.CONFIG, "Serving Angular TS for defined @NgApp " + app.value() + " at  " + siteHostingLocation.getPath());

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
        log.config("Configuring route - " + newPath);

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
                router.route("/*")
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
                router.route("/*.js")
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
                router.route("/*.css")
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
                router.route("/*.map")
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

                String path = "";
                for (DefinedRoute<?> route : RoutingModule.getRoutes())
                {
                    bindRouteToPath(router, path, staticFileLocationPath, siteHostingLocation, route);
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        return router;
    }
}

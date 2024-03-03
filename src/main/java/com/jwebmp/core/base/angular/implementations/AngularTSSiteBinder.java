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

import com.google.inject.servlet.ServletModule;
import com.guicedee.client.IGuiceContext;
import com.guicedee.guicedinjection.interfaces.IGuiceModule;
import com.guicedee.guicedinjection.properties.GlobalProperties;
import com.guicedee.guicedservlets.FileSystemResourceServlet;
import com.guicedee.guicedservlets.undertow.GuicedUndertowResourceManager;
import com.jwebmp.core.annotations.PageConfiguration;
import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.modules.services.angular.RoutingModule;
import com.jwebmp.core.base.angular.modules.services.base.EnvironmentModule;
import com.jwebmp.core.base.angular.services.DefinedRoute;
import com.jwebmp.core.implementations.JWebMPJavaScriptDynamicScriptRenderer;
import com.jwebmp.core.implementations.JWebMPSiteBinder;
import io.github.classgraph.ClassInfo;
import io.undertow.server.handlers.resource.PathResourceManager;
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
        extends ServletModule
        implements IGuiceModule<AngularTSSiteBinder>
{
    /**
     * Method onBind ...
     */
    @Override
    public void configureServlets()
    {
        bind(EnvironmentModule.class)
                .toInstance(new EnvironmentModule());

        for (ClassInfo classInfo : IGuiceContext.instance()
                                                .getScanResult()
                                                .getClassesWithAnnotation(NgApp.class))
        {
            Class<?> loadClass = classInfo.loadClass();
            NgApp app = loadClass.getAnnotation(NgApp.class);
            //  for (NgApp app : IGuiceContext.get(AnnotationHelper.class)
            //                                .getAnnotationFromClass(loadClass, NgApp.class))
            //   {
            PageConfiguration pc = loadClass.getAnnotation(PageConfiguration.class);
            String userDir = GlobalProperties.getSystemPropertyOrEnvironment("JWEBMP_ROOT_PATH", FileUtils.getUserDirectory()
                                                                                                          .getPath());
            File file = new File(userDir + "/jwebmp/" + app.value() + "/dist/jwebmp/");
            try
            {
                FileUtils.forceMkdirParent(file);
                FileUtils.forceMkdir(file);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            StringBuilder url;
            url = new StringBuilder(pc.url()
                                      .substring(0, pc.url()
                                                      .length() - 1) + "/");

            serveRegex(url.toString())
                    .with(new FileSystemResourceServlet().setFolder(file));

            GuicedUndertowResourceManager.setPathManager(new PathResourceManager(file.toPath()));

            AngularTSPostStartup.basePath = file.toPath();
            AngularTSSiteBinder.log.log(Level.FINE, "Serving Angular TS for defined @NgApp " + app.value() + " at  " + file.getPath());

            String path = "";
            for (DefinedRoute<?> route : RoutingModule.getRoutes())
            {
                bindRouteToPath(path, file, route);
            }
        }
        //}
        JWebMPSiteBinder.bindSites = false;
        JWebMPJavaScriptDynamicScriptRenderer.renderJavascript = false;

    }

    private String bindRouteToPath(String path, File file, DefinedRoute<?> route)
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
        serveRegex(newPath)
                .with(new FileSystemResourceServlet().setFolder(file));
        if (route.getChildren() != null && !route.getChildren()
                                                 .isEmpty())
        {
            for (DefinedRoute<?> child : route.getChildren())
            {
                bindRouteToPath(newPath, file, child);
            }
        }
        return newPath;
    }

    @Override
    public Integer sortOrder()
    {
        return Integer.MIN_VALUE + 100;
    }
}

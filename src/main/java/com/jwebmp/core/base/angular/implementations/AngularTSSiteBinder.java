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

import com.guicedee.guicedinjection.*;
import com.guicedee.guicedservlets.*;
import com.guicedee.guicedservlets.services.*;
import com.guicedee.guicedservlets.undertow.*;
import com.guicedee.logger.*;
import com.jwebmp.core.annotations.*;
import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.services.*;
import com.jwebmp.core.base.angular.modules.services.angular.*;
import com.jwebmp.core.base.angular.modules.services.base.*;
import com.jwebmp.core.base.angular.services.*;

import com.jwebmp.core.base.angular.services.compiler.*;
import com.jwebmp.core.implementations.*;
import io.github.classgraph.*;
import io.undertow.server.handlers.resource.*;
import org.apache.commons.io.*;

import java.io.*;
import java.util.logging.*;

/**
 * @author GedMarc
 * @version 1.0
 * @since 20 Dec 2016
 */
public class AngularTSSiteBinder
		implements IGuiceSiteBinder<GuiceSiteInjectorModule>
{
	/**
	 * Field log
	 */
	private static final java.util.logging.Logger log = LogFactory.getLog("AngularTSSiteBinder");
	
	/**
	 * Method onBind ...
	 *
	 * @param module of type GuiceSiteInjectorModule
	 */
	@Override
	public void onBind(GuiceSiteInjectorModule module)
	{
		module.bind(EnvironmentModule.class)
		      .toInstance(new EnvironmentModule());
		
		for (ClassInfo classInfo : GuiceContext.instance()
		                                       .getScanResult()
		                                       .getClassesWithAnnotation(NgApp.class))
		{
			Class<?> loadClass = classInfo.loadClass();
			for (NgApp app : AnnotationsMap.getAnnotations(loadClass, NgApp.class))
			{
				PageConfiguration pc = loadClass.getAnnotation(PageConfiguration.class);
				File file = new File(FileUtils.getUserDirectory() + "/jwebmp/" + app.name() + "/dist/jwebmp/");
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
				
				module.serveRegex$(url.toString())
				      .with(new FileSystemResourceServlet().setFolder(file));
				
				GuicedUndertowResourceManager.setPathManager(new PathResourceManager(file.toPath()));
				
				AngularTSPostStartup.basePath = file.toPath();
				AngularTSSiteBinder.log.log(Level.FINE, "Serving Angular TS for defined @NgApp " + app.name() + " at  " + file.getPath());
				
				String path = "";
				for (DefinedRoute<?> route : RoutingModule.getRoutes())
				{
					bindRouteToPath(path, module, file, route);
				}
			}
		}
		JWebMPSiteBinder.bindSites = false;
		JWebMPJavaScriptDynamicScriptRenderer.renderJavascript = false;
		
	}
	
	private String bindRouteToPath(String path, GuiceSiteInjectorModule module, File file, DefinedRoute<?> route)
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
		module.serveRegex$(newPath)
		      .with(new FileSystemResourceServlet().setFolder(file));
		if (route.getChildren() != null && !route.getChildren()
		                                         .isEmpty())
		{
			for (DefinedRoute<?> child : route.getChildren())
			{
				bindRouteToPath(newPath, module, file, child);
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

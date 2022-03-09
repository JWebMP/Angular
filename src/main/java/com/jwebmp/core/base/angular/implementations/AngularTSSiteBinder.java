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
import com.guicedee.logger.*;
import com.jwebmp.core.annotations.*;
import com.jwebmp.core.base.angular.modules.services.base.*;
import com.jwebmp.core.base.angular.services.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.implementations.*;
import io.github.classgraph.*;
import org.apache.commons.io.*;

import java.io.*;
import java.util.*;
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
			NgApp app = loadClass.getAnnotation(NgApp.class);
			PageConfiguration pc = loadClass.getAnnotation(PageConfiguration.class);
			File file = new File(FileUtils.getUserDirectory() + "/jwebmp/" + app.value() + "/dist/jwebmp/");
			StringBuilder url;
			url = new StringBuilder(pc.url()
			                          .substring(0, pc.url()
			                                          .length() - 1) + "/*");
			
			module.serve$(url.toString())
			      .with(new FileSystemResourceServlet().setFolder(file));
			
	/*		JWebMPTypeScriptCompiler.getNgPackageFilterScanResult(loadClass, app)
			                        .getClassesWithAnnotation(NgRoutable.class)
			                        .stream()
			                        .forEach(a -> {
						                        NgRoutable annotation = a.loadClass()
						                                                 .getAnnotation(NgRoutable.class);
						                        if (annotation != null)
						                        {
							                      //  module.serve$("/" + annotation.path() + "*")
							                      //        .with(new FileSystemResourceServlet().setFolder(file));
						                        }
					                        }
			                        );
			*/
			/*Set<String> uniqueMimes = new HashSet<>();
			for (MimeTypes value : MimeTypes.values())
			{
				uniqueMimes.add(value.name()
				                     .replace("$", ""));
			}
			*/
			AngularTSSiteBinder.log.log(Level.FINE, "Serving Angular TS for defined @NgApp " + app.value() + " at  " + file.getPath());
		}
		
		JWebMPSiteBinder.bindSites = false;
		JWebMPJavaScriptDynamicScriptRenderer.renderJavascript = false;
		
	}
	
	@Override
	public Integer sortOrder()
	{
		return Integer.MIN_VALUE + 100;
	}
}

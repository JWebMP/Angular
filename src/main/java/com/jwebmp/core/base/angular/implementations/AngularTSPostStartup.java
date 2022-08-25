package com.jwebmp.core.base.angular.implementations;

import com.guicedee.guicedinjection.interfaces.*;
import com.guicedee.guicedservlets.undertow.*;
import com.guicedee.logger.*;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.services.compiler.*;
import io.undertow.server.handlers.resource.*;

import java.io.*;
import java.nio.file.*;
import java.util.logging.*;

import static com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils.*;
import static com.jwebmp.core.base.angular.client.services.interfaces.IComponent.*;


public class AngularTSPostStartup implements IGuicePostStartup<AngularTSPostStartup>
{
	private static final Logger log = LogFactory.getLog(AngularTSPostStartup.class);
	public static Path basePath;
	public static boolean loadTSOnStartup = true;
	public static boolean buildApp = true;
	@Override
	public void postLoad()
	{
		if (basePath != null)
		{
			GuicedUndertowResourceManager.setPathManager(new PathResourceManager(basePath));
		}
		if(loadTSOnStartup)
		for (INgApp<?> app : JWebMPTypeScriptCompiler.getAllApps())
		{
			JWebMPTypeScriptCompiler compiler = new JWebMPTypeScriptCompiler(app);
			log.info("Post Startup - Generating @NgApp (" + getTsFilename(app.getClass()) + ") " +
			         "in folder " + getClassDirectory(app.getClass()));
			
			try
			{
				compiler.renderAppTS((Class<? extends INgApp<?>>) app.getClass());
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}

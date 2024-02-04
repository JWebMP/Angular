package com.jwebmp.core.base.angular.implementations;

import com.guicedee.guicedinjection.interfaces.IGuicePostStartup;
import com.guicedee.guicedservlets.undertow.GuicedUndertowResourceManager;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import com.jwebmp.core.base.angular.services.compiler.JWebMPTypeScriptCompiler;
import io.undertow.server.handlers.resource.PathResourceManager;
import lombok.extern.java.Log;

import java.io.IOException;
import java.nio.file.Path;

import static com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils.getTsFilename;
import static com.jwebmp.core.base.angular.client.services.interfaces.IComponent.getClassDirectory;

@Log
public class AngularTSPostStartup implements IGuicePostStartup<AngularTSPostStartup>
{
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

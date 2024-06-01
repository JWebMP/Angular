package com.jwebmp.core.base.angular.implementations;

import com.guicedee.guicedinjection.interfaces.IGuicePostStartup;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import com.jwebmp.core.base.angular.services.compiler.JWebMPTypeScriptCompiler;
import lombok.extern.java.Log;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils.getTsFilename;
import static com.jwebmp.core.base.angular.client.services.interfaces.IComponent.getClassDirectory;

@Log
public class AngularTSPostStartup implements IGuicePostStartup<AngularTSPostStartup>
{
    public static Path basePath;
    public static boolean loadTSOnStartup = true;
    public static boolean buildApp = true;

    @Override
    public List<CompletableFuture<Boolean>> postLoad()
    {
		return List.of(CompletableFuture.supplyAsync(() -> {
			if (loadTSOnStartup)
			{
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
			return true;
		}));
    }

    @Override
    public Integer sortOrder()
    {
        return Integer.MAX_VALUE - 500;
    }
}

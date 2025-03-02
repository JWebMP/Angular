package com.jwebmp.core.base.angular.implementations;

import com.google.inject.Inject;
import com.guicedee.guicedinjection.interfaces.IGuicePostStartup;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import com.jwebmp.core.base.angular.services.compiler.JWebMPTypeScriptCompiler;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
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
    @Inject
    private Vertx vertx;

    public static Path basePath;
    //public static boolean loadTSOnStartup = Environment.getProperty("LoadTSOnStartup", "true")
    //                                                   .equals("true");
    public static boolean buildApp = true;

    @Override
    public List<Future<Boolean>> postLoad()
    {
        return List.of(vertx.executeBlocking(() -> {
            for (INgApp<?> app : JWebMPTypeScriptCompiler.getAllApps())
            {
                JWebMPTypeScriptCompiler compiler = new JWebMPTypeScriptCompiler(app);
                log.info("Post Startup - Generating @NgApp (" + getTsFilename(app.getClass()) + ") " +
                        "in folder " + getClassDirectory(app.getClass()));

                try
                {
                    compiler.renderAppTS(app);
                } catch (IOException e)
                {
                    e.printStackTrace();
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

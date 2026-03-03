package com.jwebmp.core.base.angular.implementations;

import com.google.inject.Inject;
import com.guicedee.client.services.lifecycle.IGuicePostStartup;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import com.jwebmp.core.base.angular.services.AngularTsProcessingConfig;
import com.jwebmp.core.base.angular.services.compiler.TypeScriptCompiler;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.core.Vertx;
import lombok.extern.log4j.Log4j2;

import java.nio.file.Path;
import java.util.List;

import static com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils.getTsFilename;
import static com.jwebmp.core.base.angular.client.services.interfaces.IComponent.getClassDirectory;

@Log4j2
public class AngularTSPostStartup implements IGuicePostStartup<AngularTSPostStartup> {
    @Inject
    private Vertx vertx;

    public static Path basePath;
    //public static boolean loadTSOnStartup = Environment.getProperty("LoadTSOnStartup", "true")
    //                                                   .equals("true");
    public static boolean buildApp = true;

    @Override
    public List<Uni<Boolean>> postLoad() {
        // Skip TypeScript rendering if globally disabled, but still allow the server to start
        if (!AngularTsProcessingConfig.isEnabled()) {
            log.info("Angular TypeScript processing is disabled; skipping TS render on post-startup. Web server will still start and serve routes.");
            return List.of(Uni.createFrom().item(true));
        }



        Uni<Boolean> processingUni = Multi.createFrom().iterable(TypeScriptCompiler.getAllApps())
                .onItem().transformToUniAndConcatenate(app ->
                        Uni.createFrom().item(() -> {
                            try {
                                TypeScriptCompiler compiler = new TypeScriptCompiler(app);
                                log.info("Post Startup - Generating @NgApp (" + getTsFilename(app.getClass()) + ") " +
                                        "in folder " + getClassDirectory(app.getClass()));
                                compiler.compileApp();
                            } catch (Throwable t) {
                                log.error("Unable to generate @NgApp (" + getTsFilename(app.getClass()) + ")", t);
                            }
                            return app;
                        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                )
                .collect().asList()
                .map(apps -> true);

        return List.of(processingUni);
    }

    /**
     * Same as vertx startup
     *
     * @return
     */
    @Override
    public Integer sortOrder() {
        return Integer.MAX_VALUE - 5000;
    }
}

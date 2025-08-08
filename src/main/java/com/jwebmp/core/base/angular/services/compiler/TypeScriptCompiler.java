package com.jwebmp.core.base.angular.services.compiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guicedee.client.IGuiceContext;
import com.guicedee.vertx.spi.VertXPreStartup;
import com.jwebmp.core.base.angular.client.AppUtils;
import com.jwebmp.core.base.angular.client.services.interfaces.IComponent;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import com.jwebmp.core.base.angular.services.compiler.assets.AssetManager;
import com.jwebmp.core.base.angular.services.compiler.dependencies.DependencyManager;
import com.jwebmp.core.base.angular.services.compiler.files.TypeScriptFileManager;
import com.jwebmp.core.base.angular.services.compiler.generators.TypeScriptCodeGenerator;
import com.jwebmp.core.base.angular.services.compiler.processors.AngularModuleProcessor;
import com.jwebmp.core.base.angular.services.compiler.processors.ComponentProcessor;
import com.jwebmp.core.base.angular.services.compiler.setup.AngularAppSetup;
import com.jwebmp.core.base.angular.services.compiler.validators.TypeScriptCodeValidator;
import io.github.classgraph.ScanResult;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.DefaultObjectMapper;
import static com.jwebmp.core.base.angular.client.services.interfaces.IComponent.currentAppFile;

/**
 * Main TypeScript compiler that orchestrates the compilation process
 */
@Log4j2
public class TypeScriptCompiler {
    private final INgApp<?> app;
    private final TypeScriptCodeGenerator codeGenerator;
    private final TypeScriptFileManager fileManager;
    private final TypeScriptCodeValidator codeValidator;
    private final AngularAppSetup appSetup;
    private final AssetManager assetManager;
    private final DependencyManager dependencyManager;
    private final AngularModuleProcessor moduleProcessor;
    private final ComponentProcessor componentProcessor;

    private final List<File> completedFiles = new ArrayList<>();
    private static final Set<INgApp<?>> allApps = new LinkedHashSet<>();

    /**
     * Constructor
     *
     * @param app The Angular application
     */
    public TypeScriptCompiler(INgApp<?> app) {
        this.app = app;
        IComponent.app.set(app);

        // Initialize all the components
        this.codeValidator = new TypeScriptCodeValidator();
        this.codeGenerator = new TypeScriptCodeGenerator(app);
        this.fileManager = new TypeScriptFileManager(app, codeGenerator, codeValidator);
        this.appSetup = new AngularAppSetup(app);
        this.assetManager = new AssetManager(app);
        this.dependencyManager = new DependencyManager(app);
        this.componentProcessor = new ComponentProcessor(app, fileManager);
        this.moduleProcessor = new AngularModuleProcessor(app, codeGenerator, fileManager, componentProcessor);

        // Set up the current app file
        File appPath = AppUtils.getAppPath((Class<? extends INgApp<?>>) app.getClass());
        currentAppFile.set(appPath);
        log.info("Application [{}] is compiling to {}. Change with env property \"jwebmp\"", 
                app.getClass().getAnnotation(com.jwebmp.core.base.angular.client.annotations.angular.NgApp.class).value(), 
                appPath.getPath());
    }

    /**
     * Gets all Angular applications
     *
     * @return Set of all Angular applications
     */
    public static Set<INgApp<?>> getAllApps() {
        if (allApps.isEmpty()) {
            for (var classInfo : IGuiceContext.instance().getScanResult().getClassesWithAnnotation(com.jwebmp.core.base.angular.client.annotations.angular.NgApp.class)) {
                if (classInfo.isAbstract() || classInfo.isInterfaceOrAnnotation()) {
                    continue;
                }
                try {
                    INgApp<?> clazz = (INgApp<?>) IGuiceContext.get(classInfo.loadClass());
                    IComponent.app.set(clazz);
                    File appPath = AppUtils.getAppPath((Class<? extends INgApp<?>>) clazz.getClass());
                    currentAppFile.set(appPath);
                    log.debug("Generating Angular Application - ({}) in folder {}", 
                            clazz.getClass().getSimpleName(), 
                            appPath.getPath());
                    allApps.add(clazz);
                } catch (ClassCastException e) {
                    log.error("Cannot render app - {} / Annotated @NgApp does not implement INgApp", classInfo.getSimpleName(), e);
                }
            }
        }
        return allApps;
    }

    /**
     * Compiles the Angular application
     *
     * @return The compiled application
     * @throws IOException If an error occurs during compilation
     */
    public StringBuilder compileApp() throws IOException {
        StringBuilder sb = new StringBuilder();
        Class<? extends INgApp<?>> appClass = (Class<? extends INgApp<?>>) app.getClass();
        File appFile = AppUtils.getAppPath(appClass);
        currentAppFile.set(appFile);
        IComponent.app.set(app);
        ScanResult scan = IGuiceContext.instance().getScanResult();
        var vertx = VertXPreStartup.getVertx();

        try {
            // Set up environment
            setupEnvironment(appClass);

            // Process configuration files
            processConfigFiles(appClass, scan);

            // Process assets, styles, and scripts
            processAssets(appClass, scan);

            // Generate main.ts file
            generateMainTsFile(appClass, sb);

            // Process Angular components
            processAngularComponents(appClass, scan);

            // Render Angular application files
            renderAngularApplicationFiles(appClass);

            log.debug("Angular App Ready");
        } catch (Exception e) {
            log.error("Error compiling Angular application", e);
        }

        return sb;
    }

    /**
     * Sets up the environment
     *
     * @param appClass The Angular application class
     */
    private void setupEnvironment(Class<? extends INgApp<?>> appClass) {
        // Set up environment options
        IGuiceContext.get(com.jwebmp.core.base.angular.modules.services.base.EnvironmentModule.class)
                     .getEnvironmentOptions()
                     .setAppClass(appClass.getCanonicalName());

        IGuiceContext.get(com.jwebmp.core.base.angular.modules.services.base.EnvironmentModule.class)
                     .getEnvironmentOptions()
                     .setProduction(app.getRunningEnvironment()
                                       .equals(com.jwebmp.core.base.servlets.enumarations.DevelopmentEnvironments.Production));
    }

    /**
     * Processes configuration files
     *
     * @param appClass The Angular application class
     * @param scan The scan result
     * @throws IOException If an error occurs during processing
     */
    private void processConfigFiles(Class<? extends INgApp<?>> appClass, ScanResult scan) throws IOException {
        // Process npmrc file
        appSetup.processNpmrcFile(appClass);

        // Process package.json file
        appSetup.processPackageJsonFile(appClass);

        // Process TypeScript config files
        appSetup.processTypeScriptConfigFiles(appClass);

        // Process polyfill file
        appSetup.processPolyfillFile(appClass);

        // Process app config file
        appSetup.processAppConfigFile(appClass, scan);
    }

    /**
     * Processes assets, styles, and scripts
     *
     * @param appClass The Angular application class
     * @param scan The scan result
     * @throws IOException If an error occurs during processing
     */
    private void processAssets(Class<? extends INgApp<?>> appClass, ScanResult scan) throws IOException {
        // Process assets
        var namedAssets = assetManager.processAssets();

        // Process stylesheets
        assetManager.processStylesheets();

        // Process scripts
        assetManager.processScripts();

        // Process resources
        assetManager.processResources(appClass, scan);

        // Process app resources
        assetManager.processAppResources(appClass, scan);
    }

    /**
     * Generates the main.ts file
     *
     * @param appClass The Angular application class
     * @param sb The string builder
     * @throws IOException If an error occurs during generation
     */
    private void generateMainTsFile(Class<? extends INgApp<?>> appClass, StringBuilder sb) throws IOException {
        // Generate main.ts file
        sb.append(codeGenerator.renderAppTS(app));

        // Write main.ts file
        codeGenerator.writeAppTS(app);

        // Generate and write index.html
        appSetup.renderBootIndexHtml(app);
    }

    /**
     * Processes Angular components
     *
     * @param appClass The Angular application class
     * @param scan The scan result
     */
    private void processAngularComponents(Class<? extends INgApp<?>> appClass, ScanResult scan) {
        File srcDirectory = AppUtils.getAppSrcPath(appClass);
        File currentApp = currentAppFile.get();

        // Process all components
        moduleProcessor.processAllComponents(currentApp, scan, appClass, srcDirectory);
    }

    /**
     * Renders Angular application files
     *
     * @param appClass The Angular application class
     * @throws IOException If an error occurs during rendering
     */
    private void renderAngularApplicationFiles(Class<? extends INgApp<?>> appClass) throws IOException {
        ObjectMapper om = IGuiceContext.get(DefaultObjectMapper);
        assetManager.renderAngularApplicationFiles(currentAppFile.get(), appClass, assetManager.processAssets(), app, om);
    }

    /**
     * Installs dependencies for the Angular application
     */
    public void installDependencies() {
        if (dependencyManager.isOsSupported()) {
            dependencyManager.installDependencies(currentAppFile.get());
        } else {
            log.warn("Dependency installation not supported on this operating system: {}", dependencyManager.getOsName());
        }
    }

    /**
     * Builds the Angular application
     */
    public void buildAngularApp() {
        if (dependencyManager.isOsSupported()) {
            dependencyManager.buildAngularApp(currentAppFile.get());
        } else {
            log.warn("Angular build not supported on this operating system: {}", dependencyManager.getOsName());
        }
    }
}

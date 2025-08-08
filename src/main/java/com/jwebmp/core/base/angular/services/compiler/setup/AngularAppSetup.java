package com.jwebmp.core.base.angular.services.compiler.setup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.guicedee.client.IGuiceContext;
import com.guicedee.client.CallScoper;
import com.jwebmp.core.Page;
import com.jwebmp.core.base.ComponentHierarchyBase;
import com.jwebmp.core.base.angular.client.AppUtils;
import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.annotations.boot.NgBootImportProvider;
import com.jwebmp.core.base.angular.client.annotations.boot.NgBootImportReference;
import com.jwebmp.core.base.angular.client.annotations.typescript.TsDependencies;
import com.jwebmp.core.base.angular.client.annotations.typescript.TsDependency;
import com.jwebmp.core.base.angular.client.annotations.typescript.TsDevDependencies;
import com.jwebmp.core.base.angular.client.annotations.typescript.TsDevDependency;
import com.jwebmp.core.base.angular.client.services.AnnotationHelper;
import com.jwebmp.core.base.angular.client.services.interfaces.IComponent;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import com.jwebmp.core.base.angular.modules.services.base.EnvironmentModule;
import com.jwebmp.core.base.angular.services.interfaces.NpmrcConfigurator;
import com.jwebmp.core.base.angular.typescript.JWebMP.ResourceLocator;
import com.jwebmp.core.base.html.Body;
import com.jwebmp.core.base.html.DivSimple;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.DefaultObjectMapper;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Responsible for Angular application setup (environment, npmrc, package.json)
 */
@Log4j2
public class AngularAppSetup
{
    private final INgApp<?> app;
    private final Map<String, TsDependency> namedDependencies = new HashMap<>();

    /**
     * Constructor
     *
     * @param app The Angular application
     */
    public AngularAppSetup(INgApp<?> app)
    {
        this.app = app;
    }

    /**
     * Processes the npmrc file
     *
     * @param appClass The Angular application class
     * @throws IOException If an error occurs during processing
     */
    public void processNpmrcFile(Class<? extends INgApp<?>> appClass) throws IOException
    {
        File npmrcFile = AppUtils.getAppNpmrcPath(appClass, true);
        Set<NpmrcConfigurator> npmrcConfigurators = IGuiceContext.loaderToSetNoInjection(ServiceLoader.load(NpmrcConfigurator.class));
        StringBuilder npmrcString = new StringBuilder();
        for (NpmrcConfigurator npmrcConfigurator : npmrcConfigurators)
        {
            Set<String> lines = npmrcConfigurator.lines();
            lines.forEach(a -> npmrcString.append(a.trim())
                                          .append('\n'));
        }
        FileUtils.writeStringToFile(npmrcFile, npmrcString.toString(), UTF_8);
    }

    /**
     * Processes the package.json file
     *
     * @param appClass The Angular application class
     * @throws IOException If an error occurs during processing
     */
    public void processPackageJsonFile(Class<? extends INgApp<?>> appClass) throws IOException
    {
        File packageJsonFile = AppUtils.getAppPackageJsonPath(appClass, true);
        String packageTemplate = IOUtils.toString(Objects.requireNonNull(ResourceLocator.class.getResourceAsStream("package.json")), UTF_8);

        Map<String, String> dependencies = new TreeMap<>();
        Map<String, String> devDependencies = new TreeMap<>();
        Map<String, String> overrideDependencies = new TreeMap<>();

        processTsDependencies(dependencies, overrideDependencies);
        processTsDevDependencies(devDependencies);

        ObjectMapper om = IGuiceContext.get(DefaultObjectMapper);
        processPackageJsonFile(appClass, packageTemplate, om, dependencies, devDependencies, overrideDependencies, packageJsonFile);
    }

    /**
     * Processes TypeScript dependencies
     *
     * @param dependencies         The dependencies map to populate
     * @param overrideDependencies The override dependencies map to populate
     */
    private void processTsDependencies(Map<String, String> dependencies, Map<String, String> overrideDependencies)
    {
        IGuiceContext.instance()
                     .getScanResult()
                     .getClassesWithAnnotation(TsDependency.class)
                     .stream()
                     .forEach(a -> {
                         TsDependency annotation = a.loadClass()
                                                    .getAnnotation(TsDependency.class);
                         if (annotation != null)
                         {
                             var annotation1 = getNamedTSDependency(annotation);
                             dependencies.putIfAbsent(annotation1.value(), annotation1.version());
                             if (annotation1.overrides())
                             {
                                 overrideDependencies.put(annotation1.value(), annotation1.version());
                             }
                         }
                     });

        IGuiceContext.instance()
                     .getScanResult()
                     .getClassesWithAnnotation(TsDependencies.class)
                     .stream()
                     .forEach(a -> {
                         TsDependencies annotations = a.loadClass()
                                                       .getAnnotation(TsDependencies.class);
                         if (annotations != null)
                         {
                             for (TsDependency annotation : annotations.value())
                             {
                                 var annotation1 = getNamedTSDependency(annotation);
                                 dependencies.putIfAbsent(annotation1.value(), annotation1.version());
                                 if (annotation1.overrides())
                                 {
                                     overrideDependencies.put(annotation1.value(), annotation1.version());
                                 }
                             }
                         }
                     });
    }

    /**
     * Processes TypeScript dev dependencies
     *
     * @param devDependencies The dev dependencies map to populate
     */
    private void processTsDevDependencies(Map<String, String> devDependencies)
    {
        IGuiceContext.instance()
                     .getScanResult()
                     .getClassesWithAnnotation(TsDevDependency.class)
                     .stream()
                     .forEach(a -> {
                         TsDevDependency annotation = a.loadClass()
                                                       .getAnnotation(TsDevDependency.class);
                         if (annotation != null)
                         {
                             devDependencies.put(annotation.value(), annotation.version());
                         }
                     });

        IGuiceContext.instance()
                     .getScanResult()
                     .getClassesWithAnnotation(TsDevDependencies.class)
                     .stream()
                     .forEach(a -> {
                         TsDevDependencies annotations = a.loadClass()
                                                          .getAnnotation(TsDevDependencies.class);
                         if (annotations != null)
                         {
                             for (TsDevDependency annotation : annotations.value())
                             {
                                 devDependencies.put(annotation.value(), annotation.version());
                             }
                         }
                     });
    }

    /**
     * Processes the package.json file
     *
     * @param appClass             The Angular application class
     * @param packageTemplate      The package.json template
     * @param om                   The ObjectMapper
     * @param dependencies         The dependencies map
     * @param devDependencies      The dev dependencies map
     * @param overrideDependencies The override dependencies map
     * @param packageJsonFile      The package.json file
     * @return Whether the file was processed successfully
     */
    private boolean processPackageJsonFile(Class<? extends INgApp<?>> appClass, String packageTemplate, ObjectMapper om,
                                           Map<String, String> dependencies, Map<String, String> devDependencies,
                                           Map<String, String> overrideDependencies, File packageJsonFile)
    {
        try
        {
            // Replace the app name in the template
            String appName = AppUtils.getAppName(appClass);
            packageTemplate = packageTemplate.replace("/*appName*/", appName);

            // Build dependencies JSON string
            StringBuilder dependenciesJson = new StringBuilder("{\n");
            int depCount = 0;
            for (Map.Entry<String, String> entry : dependencies.entrySet())
            {
                if (depCount > 0)
                {
                    dependenciesJson.append(",\n");
                }
                dependenciesJson.append("    \"")
                                .append(entry.getKey()
                                             .replace("\"", "\\\""))
                                .append("\": \"")
                                .append(entry.getValue()
                                             .replace("\"", "\\\""))
                                .append("\"");
                depCount++;
            }
            dependenciesJson.append("\n  }");

            // Build devDependencies JSON string
            StringBuilder devDependenciesJson = new StringBuilder("{\n");
            int devDepCount = 0;
            for (Map.Entry<String, String> entry : devDependencies.entrySet())
            {
                if (devDepCount > 0)
                {
                    devDependenciesJson.append(",\n");
                }
                devDependenciesJson.append("    \"")
                                   .append(entry.getKey()
                                                .replace("\"", "\\\""))
                                   .append("\": \"")
                                   .append(entry.getValue()
                                                .replace("\"", "\\\""))
                                   .append("\"");
                devDepCount++;
            }
            devDependenciesJson.append("\n  }");

            // Build overrides JSON string
            StringBuilder overridesJson = new StringBuilder("{\n");
            int overrideCount = 0;
            for (Map.Entry<String, String> entry : overrideDependencies.entrySet())
            {
                if (overrideCount > 0)
                {
                    overridesJson.append(",\n");
                }
                overridesJson.append("    \"")
                             .append(entry.getKey()
                                          .replace("\"", "\\\""))
                             .append("\": \"")
                             .append(entry.getValue()
                                          .replace("\"", "\\\""))
                             .append("\"");
                overrideCount++;
            }
            overridesJson.append("\n  }");

            // Replace the placeholders in the template
            packageTemplate = packageTemplate.replace("/*dependencies*/", dependenciesJson.toString());
            packageTemplate = packageTemplate.replace("/*devDependencies*/", devDependenciesJson.toString());
            packageTemplate = packageTemplate.replace("/*overrideDependencies*/", overridesJson.toString());

            // Write the modified template to the file
            FileUtils.writeStringToFile(packageJsonFile, packageTemplate, UTF_8);
            return true;
        }
        catch (IOException e)
        {
            log.error("Unable to write package.json", e);
            return false;
        }
    }

    /**
     * Processes the TypeScript config files
     *
     * @param appClass The Angular application class
     * @throws IOException If an error occurs during processing
     */
    public void processTypeScriptConfigFiles(Class<? extends INgApp<?>> appClass) throws IOException
    {
        // Process tsconfig.json
        File tsconfigFile = AppUtils.getAppTsConfigPath(appClass, true);
        String tsconfigTemplate = IOUtils.toString(Objects.requireNonNull(ResourceLocator.class.getResourceAsStream("tsconfig.json")), UTF_8);
        FileUtils.writeStringToFile(tsconfigFile, tsconfigTemplate, UTF_8);

        // Process tsconfig.app.json
        File tsconfigAppFile = AppUtils.getAppTsConfigAppPath(appClass, true);
        String tsconfigAppTemplate = IOUtils.toString(Objects.requireNonNull(ResourceLocator.class.getResourceAsStream("tsconfig.app.json")), UTF_8);
        FileUtils.writeStringToFile(tsconfigAppFile, tsconfigAppTemplate, UTF_8);


        // Process .gitignore
      /*  File gitIgnoreFile = AppUtils.getGitIgnorePath(appClass, true);
        String gitIgnoreTemplate = IOUtils.toString(Objects.requireNonNull(ResourceLocator.class.getResourceAsStream(".gitignore")), UTF_8);
        FileUtils.writeStringToFile(gitIgnoreFile, gitIgnoreTemplate, UTF_8);*/

        // Delete package-lock.json if it exists
        File packageLockFile = new File(AppUtils.getAppPath(appClass)
                                                .getCanonicalPath() + "/package-lock.json");
        if (packageLockFile.exists() && packageLockFile.isFile())
        {
            packageLockFile.delete();
        }
    }

    /**
     * Processes the polyfill file
     *
     * @param appClass The Angular application class
     * @throws IOException If an error occurs during processing
     */
    public void processPolyfillFile(Class<? extends INgApp<?>> appClass) throws IOException
    {
        File polyfillsFile = AppUtils.getAppPolyfillsPath(appClass, true);

        // Try to get the polyfills.ts resource
        InputStream is = ResourceLocator.class.getResourceAsStream("polyfills.ts");
        if (is != null)
        {
            String polyfillsTemplate = IOUtils.toString(is, UTF_8);
            is.close();
            FileUtils.writeStringToFile(polyfillsFile, polyfillsTemplate, UTF_8);
        }
        else
        {
            // Angular 19 doesn't do polyfills by default, so we don't create a default template
            log.info("polyfills.ts resource not found, skipping (Angular 19 doesn't require polyfills by default)");
        }
    }

    /**
     * Gets a named TypeScript dependency
     *
     * @param dependency The dependency
     * @return The named dependency
     */
    private TsDependency getNamedTSDependency(TsDependency dependency)
    {
        String name = dependency.value();
        if (!Strings.isNullOrEmpty(dependency.name()))
        {
            name = dependency.name();
        }

        if (dependency.overrides())
        {
            namedDependencies.put(name, dependency);
            return dependency;
        }

        if (namedDependencies.containsKey(name))
        {
            return namedDependencies.get(name);
        }

        namedDependencies.put(name, dependency);
        return dependency;
    }

    /**
     * Installs dependencies
     *
     * @param appBaseDirectory The application base directory
     */
    public static void installDependencies(File appBaseDirectory)
    {
        if (SystemUtils.IS_OS_WINDOWS)
        {
            try
            {
                ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", FileUtils.getUserDirectory() + "/AppData/Roaming/npm/npm.cmd install");
                processBuilder.inheritIO();
                processBuilder.environment()
                              .putAll(System.getenv());
                processBuilder = processBuilder.directory(appBaseDirectory);
                Process p = processBuilder.start();
                p.waitFor();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else if (SystemUtils.IS_OS_LINUX)
        {
            try
            {
                ProcessBuilder processBuilder = new ProcessBuilder("npm", "install", "--force");
                processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
                processBuilder.environment()
                              .putAll(System.getenv());
                processBuilder = processBuilder.directory(appBaseDirectory);
                Process p = processBuilder.start();
                p.waitFor();
                p.destroyForcibly();
            }
            catch (IOException | InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Builds the Angular application
     *
     * @param appBaseDirectory The application base directory
     */
    public static void buildAngularApp(File appBaseDirectory)
    {
        if (SystemUtils.IS_OS_WINDOWS)
        {
            try
            {
                ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", FileUtils.getUserDirectory() + "/AppData/Roaming/npm/npm.cmd", "run", "build" + (IGuiceContext.get(EnvironmentModule.class)
                                                                                                                                                                                  .getEnvironmentOptions()
                                                                                                                                                                                  .isProduction() ? "-prod" : ""));
                processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
                processBuilder.environment()
                              .putAll(System.getenv());
                processBuilder = processBuilder.directory(appBaseDirectory);
                Process p = processBuilder.start();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if (SystemUtils.IS_OS_LINUX)
        {
            try
            {
                ProcessBuilder processBuilder = new ProcessBuilder("npm", "run", "build" + (IGuiceContext.get(EnvironmentModule.class)
                                                                                                         .getEnvironmentOptions()
                                                                                                         .isProduction() ? "-prod" : ""));
                processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
                processBuilder.environment()
                              .putAll(System.getenv());
                processBuilder = processBuilder.directory(appBaseDirectory);
                Process p = processBuilder.start();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Processes the app config file
     *
     * @param appClass The Angular application class
     * @param scan     The scan result
     * @throws IOException If an error occurs during processing
     */
    public void processAppConfigFile(Class<? extends INgApp<?>> appClass, ScanResult scan) throws IOException
    {
        CallScoper scoper = IGuiceContext.get(CallScoper.class);
        scoper.enter();
        try (var is = ResourceLocator.class.getResourceAsStream("app.config.json"))
        {
            String bootAppString = IOUtils.toString(is, UTF_8);
            StringBuilder bootImportsString = new StringBuilder();
            var ir = scan.getClassesWithAnnotation(NgBootImportReference.class);
            Set<String> imports = new LinkedHashSet<>();

            for (ClassInfo classInfo : ir)
            {
                var a = classInfo.loadClass()
                                 .getAnnotationsByType(NgBootImportReference.class);
                for (NgBootImportReference ngBootImportReference : a)
                {
                    String importString = "import {" + ngBootImportReference.value() + "} from '" + ngBootImportReference.reference() + "'";
                    imports.add(importString);
                }
            }
            imports.forEach(a -> bootImportsString.append(a)
                                                  .append("\n"));

            StringBuilder bootImportProviders = new StringBuilder();
            for (ClassInfo classInfo : scan.getClassesWithAnnotation(NgBootImportProvider.class))
            {
                var a = classInfo.loadClass()
                                 .getAnnotationsByType(NgBootImportProvider.class);
                for (NgBootImportProvider ngBootImportProvider : a)
                {
                    bootImportProviders.append(ngBootImportProvider.value())
                                       .append(",\n");
                }
            }

            bootAppString = bootAppString.formatted(bootImportsString.toString(), bootImportProviders.toString());
            FileUtils.writeStringToFile(new File(AppUtils.getAppSrcPath(appClass) + "/app.config.ts"), bootAppString, UTF_8);
        }
        catch (Throwable e)
        {
            log.error("Unable to process App Config File", e);
        }
        finally
        {
            scoper.exit();
        }
    }

    /**
     * Renders the boot index HTML
     *
     * @param app The Angular application
     * @return The rendered HTML
     */
    public StringBuilder renderBootIndexHtml(INgApp<?> app)
    {
        Page<?> p = (Page) app;
        StringBuilder sb = new StringBuilder();
        Body body = p.getBody();
        List<ComponentHierarchyBase> comps = new ArrayList<>(body.getChildren());

        body.getChildren()
            .clear();
        List<NgComponent> annotations = IGuiceContext.get(AnnotationHelper.class)
                                                     .getAnnotationFromClass(app.getAnnotation()
                                                                                .bootComponent(), NgComponent.class);
        if (annotations.isEmpty())
        {
            throw new RuntimeException("No components found to render for boot index, the boot module specified does not have a @NgComponent");
        }
        else
        {
            body.add(new DivSimple<>().setTag(annotations.get(0)
                                                         .value()));
        }
        p.setBody(body);
        sb.append(p.toString(0));

        // Get the index.html file path
        File indexHtmlFile = AppUtils.getIndexHtmlPath((Class<? extends INgApp<?>>) app.getClass(), true);

        // Write the rendered HTML to the file
        try
        {
            log.info("Writing boot index HTML to file: {}", indexHtmlFile.getAbsolutePath());
            FileUtils.writeStringToFile(indexHtmlFile, sb.toString(), UTF_8, false);
        }
        catch (IOException e)
        {
            log.error("Error writing boot index HTML to file", e);
            throw new RuntimeException("Failed to write boot index HTML to file", e);
        }

        return sb;
    }
}

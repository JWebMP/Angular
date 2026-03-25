package com.jwebmp.core.base.angular.services.compiler.setup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.guicedee.client.IGuiceContext;
import com.guicedee.client.scopes.CallScoper;
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
import com.jwebmp.core.base.angular.client.services.TypescriptIndexPageConfigurator;
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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.guicedee.client.implementations.ObjectBinderKeys.DefaultObjectMapper;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Responsible for Angular application setup (environment, npmrc, package.json)
 */
@Log4j2
public class AngularAppSetup
{
    private final INgApp<?> app;
    private final Map<String, TsDependency> namedDependencies = new HashMap<>();
    private static volatile String npmExecutableOverride;

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

        // Run page configurators to allow dynamic TsDependency additions to the app page/body
        runPageConfiguratorsForDependencies();

        processTsDependencies(dependencies, overrideDependencies);
        processTsDevDependencies(devDependencies);

        ObjectMapper om = IGuiceContext.get(DefaultObjectMapper);
        processPackageJsonFile(appClass, packageTemplate, om, dependencies, devDependencies, overrideDependencies, packageJsonFile);
    }

    /**
     * Runs page configurators' configureAngular on the app to populate dynamic configurations
     * (e.g. TsDependency) before package.json processing
     */
    private void runPageConfiguratorsForDependencies()
    {
        Set<com.jwebmp.core.services.IPageConfigurator> pageConfigurators = IGuiceContext.loaderToSet(ServiceLoader.load(com.jwebmp.core.services.IPageConfigurator.class));
        for (com.jwebmp.core.services.IPageConfigurator configurator : pageConfigurators)
        {
            try
            {
                boolean enabled = true;
                try
                {
                    enabled = configurator.enabled();
                }
                catch (Throwable ignored)
                {
                }
                if (enabled)
                {
                    configurator.configureAngular((com.jwebmp.core.services.IPage<?>) app);
                }
            }
            catch (Throwable t)
            {
                log.warn("IPageConfigurator ({}) failed during configureAngular for dependency collection",
                        configurator.getClass().getName(), t);
            }
        }
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

        // Collect dynamic TsDependency configurations from the app page and body
        if (app instanceof com.jwebmp.core.base.interfaces.IComponentHierarchyBase<?, ?> appComponent)
        {
            collectDynamicTsDependencies(appComponent, dependencies, overrideDependencies);
            if (app instanceof com.jwebmp.core.services.IPage<?> page)
            {
                try
                {
                    var body = page.getBody();
                    if (body != null)
                    {
                        collectDynamicTsDependencies((com.jwebmp.core.base.interfaces.IComponentHierarchyBase<?, ?>) body, dependencies, overrideDependencies);
                    }
                }
                catch (Throwable ignored)
                {
                    // body may not be available
                }
            }
        }
    }

    /**
     * Collects dynamic TsDependency configurations from a component's configurations
     */
    private void collectDynamicTsDependencies(com.jwebmp.core.base.interfaces.IComponentHierarchyBase<?, ?> component,
                                               Map<String, String> dependencies, Map<String, String> overrideDependencies)
    {
        for (var config : component.getConfigurations())
        {
            if (config instanceof TsDependency tsDep)
            {
                var named = getNamedTSDependency(tsDep);
                dependencies.putIfAbsent(named.value(), named.version());
                if (named.overrides())
                {
                    overrideDependencies.put(named.value(), named.version());
                }
            }
        }
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
        boolean defaultForce = SystemUtils.IS_OS_LINUX;
        installDependencies(appBaseDirectory, defaultForce);
    }

    /**
     * Installs dependencies
     *
     * @param appBaseDirectory The application base directory
     * @param force Whether to pass --force to npm install
     */
    public static void installDependencies(File appBaseDirectory, boolean force)
    {
        String npmExecutable = resolveNpmExecutable(appBaseDirectory, false, null);
        if (npmExecutable == null)
        {
            log.warn("npm is not available; skipping dependency installation in {}", appBaseDirectory.getAbsolutePath());
            return;
        }

        List<String> args = new ArrayList<>();
        args.add("install");
        if (force)
        {
            args.add("--force");
        }
        runNpmCommand(appBaseDirectory, npmExecutable, args);
    }

    private static String resolveWindowsNpmCommand()
    {
        // 1. Check the classic npm global install location
        String npmCmd = FileUtils.getUserDirectory() + "/AppData/Roaming/npm/npm.cmd";
        File npmFile = new File(npmCmd);
        if (npmFile.exists())
        {
            return npmFile.getAbsolutePath();
        }

        // 2. Try to find npm.cmd via the system PATH
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null || pathEnv.isBlank())
        {
            pathEnv = System.getenv("Path");
        }
        if (pathEnv != null && !pathEnv.isBlank())
        {
            for (String dir : pathEnv.split(";"))
            {
                if (dir == null || dir.isBlank())
                {
                    continue;
                }
                File candidate = new File(dir.trim(), "npm.cmd");
                if (candidate.isFile())
                {
                    log.info("Found npm on PATH: {}", candidate.getAbsolutePath());
                    return candidate.getAbsolutePath();
                }
            }
        }

        // 3. Try 'where npm.cmd' as a fallback (covers nvm, custom installs, etc.)
        String whereResult = runWhereCommand("npm.cmd");
        if (whereResult != null)
        {
            log.info("Found npm via 'where': {}", whereResult);
            return whereResult;
        }

        // 4. Check common installation directories
        String[] commonPaths = {
                System.getenv("ProgramFiles") + "\\nodejs\\npm.cmd",
                System.getenv("ProgramFiles(x86)") + "\\nodejs\\npm.cmd",
                "C:\\Program Files\\nodejs\\npm.cmd",
                "C:\\Software\\nodejs\\npm.cmd",
                System.getenv("NVM_HOME") != null ? System.getenv("NVM_HOME") + "\\npm.cmd" : null,
                System.getenv("NVM_SYMLINK") != null ? System.getenv("NVM_SYMLINK") + "\\npm.cmd" : null,
        };
        for (String path : commonPaths)
        {
            if (path == null)
            {
                continue;
            }
            File candidate = new File(path);
            if (candidate.isFile())
            {
                log.info("Found npm at common path: {}", candidate.getAbsolutePath());
                return candidate.getAbsolutePath();
            }
        }

        // 5. Fall back to bare "npm" and hope it's on the PATH at execution time
        log.warn("Could not locate npm.cmd on disk; falling back to bare 'npm' command");
        return "npm";
    }

    /**
     * Runs {@code where.exe <command>} on Windows and returns the first result line, or null if not found.
     */
    private static String runWhereCommand(String command)
    {
        try
        {
            ProcessBuilder pb = new ProcessBuilder("where.exe", command);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            String output = new String(p.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
            if (p.waitFor(15, TimeUnit.SECONDS) && p.exitValue() == 0 && !output.isBlank())
            {
                // Return the first line (first match)
                String firstLine = output.lines().findFirst().orElse(null);
                if (firstLine != null && new File(firstLine.trim()).isFile())
                {
                    return firstLine.trim();
                }
            }
        }
        catch (IOException | InterruptedException e)
        {
            // Ignore — 'where' not available or failed
        }
        return null;
    }

    /**
     * Builds the Angular application
     *
     * @param appBaseDirectory The application base directory
     */
    public static void buildAngularApp(File appBaseDirectory)
    {
        String npmExecutable = resolveNpmExecutable(appBaseDirectory, false, null);
        if (npmExecutable == null)
        {
            log.warn("npm is not available; skipping Angular build in {}", appBaseDirectory.getAbsolutePath());
            return;
        }
        String buildTarget = "build" + (IGuiceContext.get(EnvironmentModule.class)
                                                     .getEnvironmentOptions()
                                                     .isProduction() ? "-prod" : "");
        runNpmCommand(appBaseDirectory, npmExecutable, Arrays.asList("run", buildTarget));
    }

    public static void ensureToolchain(File appBaseDirectory,
                                       boolean downloadNpm,
                                       String nodeVersion,
                                       String angularCliVersion,
                                       boolean force)
    {
        String npmExecutable = resolveNpmExecutable(appBaseDirectory, downloadNpm, nodeVersion);
        if (npmExecutable == null)
        {
            log.warn("npm is not available; cannot ensure Angular toolchain in {}", appBaseDirectory.getAbsolutePath());
            return;
        }
        npmExecutableOverride = npmExecutable;

        String desiredCliVersion = resolveAngularCliVersion(appBaseDirectory, angularCliVersion);
        if (desiredCliVersion == null || desiredCliVersion.isBlank())
        {
            log.warn("Angular CLI version could not be resolved; skipping CLI install.");
            return;
        }

        if (needsAngularCliInstall(appBaseDirectory, desiredCliVersion))
        {
            List<String> args = new ArrayList<>();
            args.add("install");
            args.add("--no-save");
            args.add("@angular/cli@" + desiredCliVersion);
            if (force)
            {
                args.add("--force");
            }
            log.info("Installing Angular CLI {} in {}", desiredCliVersion, appBaseDirectory.getAbsolutePath());
            runNpmCommand(appBaseDirectory, npmExecutable, args);
        }
    }

    private static String resolveAngularCliVersion(File appBaseDirectory, String configuredVersion)
    {
        if (configuredVersion != null && !configuredVersion.trim().isEmpty())
        {
            return configuredVersion.trim();
        }
        String fromPackageJson = readPackageJsonDependency(appBaseDirectory, "devDependencies", "@angular/cli");
        if (fromPackageJson != null && !fromPackageJson.isBlank())
        {
            return fromPackageJson.trim();
        }
        return "20";
    }

    private static boolean needsAngularCliInstall(File appBaseDirectory, String desiredVersion)
    {
        File cliPackageJson = new File(appBaseDirectory, "node_modules/@angular/cli/package.json");
        if (!cliPackageJson.isFile())
        {
            return true;
        }
        String installedVersion = readPackageJsonVersion(cliPackageJson);
        if (installedVersion == null)
        {
            return true;
        }
        Integer desiredMajor = parseMajorVersion(desiredVersion);
        Integer installedMajor = parseMajorVersion(installedVersion);
        if (desiredMajor == null || installedMajor == null)
        {
            return true;
        }
        return !desiredMajor.equals(installedMajor);
    }

    private static Integer parseMajorVersion(String version)
    {
        if (version == null)
        {
            return null;
        }
        Matcher matcher = Pattern.compile("(\\d+)").matcher(version);
        if (matcher.find())
        {
            try
            {
                return Integer.parseInt(matcher.group(1));
            }
            catch (NumberFormatException ignored)
            {
                return null;
            }
        }
        return null;
    }

    private static String readPackageJsonDependency(File appBaseDirectory, String section, String name)
    {
        File packageJson = new File(appBaseDirectory, "package.json");
        if (!packageJson.isFile())
        {
            return null;
        }
        try
        {
            ObjectMapper om = new ObjectMapper();
            var root = om.readTree(packageJson);
            var deps = root.path(section);
            if (deps.isObject() && deps.has(name))
            {
                var node = deps.get(name);
                if (node.isTextual())
                {
                    return node.asText();
                }
            }
        }
        catch (IOException ignored)
        {
        }
        return null;
    }

    private static String readPackageJsonVersion(File packageJson)
    {
        try
        {
            ObjectMapper om = new ObjectMapper();
            var root = om.readTree(packageJson);
            var versionNode = root.path("version");
            if (versionNode.isTextual())
            {
                return versionNode.asText();
            }
        }
        catch (IOException ignored)
        {
        }
        return null;
    }

    private static String resolveNpmExecutable(File appBaseDirectory, boolean allowDownload, String nodeVersion)
    {
        if (npmExecutableOverride != null && !npmExecutableOverride.isBlank())
        {
            return npmExecutableOverride;
        }

        String overrideProperty = System.getProperty("jwebmp.npm.path");
        if (overrideProperty != null && !overrideProperty.isBlank())
        {
            log.info("Using npm from system property jwebmp.npm.path: {}", overrideProperty);
            return overrideProperty;
        }

        if (SystemUtils.IS_OS_WINDOWS)
        {
            String npmCmd = resolveWindowsNpmCommand();
            if (canRunNpmCommand(appBaseDirectory, npmCmd))
            {
                log.info("Resolved npm executable on Windows: {}", npmCmd);
                return npmCmd;
            }
            log.warn("npm resolved to '{}' but verification failed (npm --version returned non-zero). " +
                     "Ensure Node.js/npm is installed and on the system PATH, or set -Djwebmp.npm.path=<path-to-npm.cmd>", npmCmd);
        }
        else if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC)
        {
            if (canRunNpmCommand(appBaseDirectory, "npm"))
            {
                return "npm";
            }
            log.warn("npm is not available on the PATH. Ensure Node.js/npm is installed, or set -Djwebmp.npm.path=<path-to-npm>");
        }

        if (!allowDownload)
        {
            log.error("npm could not be found. Options to fix this:\n" +
                      "  1. Install Node.js/npm and ensure it is on the system PATH\n" +
                      "  2. Set the system property -Djwebmp.npm.path=/path/to/npm\n" +
                      "  3. Enable automatic download with -Djwebmp.angular.ensureToolchain=true -Djwebmp.angular.downloadNpm=true");
            return null;
        }

        String downloaded = downloadNodeDistribution(appBaseDirectory, nodeVersion);
        if (downloaded != null && canRunNpmCommand(appBaseDirectory, downloaded))
        {
            return downloaded;
        }
        return null;
    }

    private static boolean canRunNpmCommand(File appBaseDirectory, String npmExecutable)
    {
        List<String> command = buildNpmCommand(npmExecutable, Collections.singletonList("--version"));
        log.debug("Verifying npm executable with command: {}", command);
        int exitCode = runCommand(appBaseDirectory, command, false);
        if (exitCode != 0)
        {
            log.debug("npm verification failed for '{}' (exit code {})", npmExecutable, exitCode);
        }
        return exitCode == 0;
    }

    private static void runNpmCommand(File appBaseDirectory, String npmExecutable, List<String> args)
    {
        List<String> command = buildNpmCommand(npmExecutable, args);
        log.info("Running npm command: {}", command);
        int exitCode = runCommand(appBaseDirectory, command, true);
        if (exitCode != 0)
        {
            log.error("npm command failed with exit code {}: {}", exitCode, command);
        }
    }

    private static int runCommand(File appBaseDirectory, List<String> command, boolean inheritIo)
    {
        try
        {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            if (inheritIo)
            {
                processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
            }
            else
            {
                processBuilder.redirectErrorStream(true);
            }
            processBuilder.environment()
                          .putAll(System.getenv());

            // Ensure the directory containing the npm executable is on the PATH
            // This is critical when npm was found at a non-standard location
            if (!command.isEmpty())
            {
                String firstArg = command.get(0);
                // On Windows the actual executable is the 3rd argument (cmd.exe /c <npm>)
                String executablePath = SystemUtils.IS_OS_WINDOWS && command.size() >= 3
                        ? command.get(2).split("\\s")[0].replace("\"", "")
                        : firstArg;
                File execFile = new File(executablePath);
                if (execFile.isFile() && execFile.getParentFile() != null)
                {
                    String currentPath = processBuilder.environment().getOrDefault("PATH",
                            processBuilder.environment().getOrDefault("Path", ""));
                    String nodeDir = execFile.getParentFile().getAbsolutePath();
                    if (!currentPath.contains(nodeDir))
                    {
                        processBuilder.environment().put("PATH", nodeDir + File.pathSeparator + currentPath);
                    }
                }
            }

            processBuilder = processBuilder.directory(appBaseDirectory);
            Process p = processBuilder.start();

            // Drain stdout/stderr when not inheriting IO to prevent buffer deadlock
            if (!inheritIo)
            {
                p.getInputStream().readAllBytes();
            }

            if (!p.waitFor(10, TimeUnit.MINUTES))
            {
                p.destroyForcibly();
                log.warn("Command timed out after 10 minutes: {}", command);
                return -1;
            }
            return p.exitValue();
        }
        catch (IOException | InterruptedException e)
        {
            log.debug("Failed to execute command {}: {}", command, e.getMessage());
            return -1;
        }
    }

    private static List<String> buildNpmCommand(String npmExecutable, List<String> args)
    {
        if (SystemUtils.IS_OS_WINDOWS)
        {
            StringBuilder command = new StringBuilder();
            command.append(quoteIfNeeded(npmExecutable));
            for (String arg : args)
            {
                command.append(' ')
                       .append(arg);
            }
            return Arrays.asList("cmd.exe", "/c", command.toString());
        }
        List<String> command = new ArrayList<>();
        command.add(npmExecutable);
        command.addAll(args);
        return command;
    }

    private static String quoteIfNeeded(String value)
    {
        if (value == null)
        {
            return "";
        }
        if (value.contains(" "))
        {
            return "\"" + value + "\"";
        }
        return value;
    }

    private static String downloadNodeDistribution(File appBaseDirectory, String nodeVersion)
    {
        if (nodeVersion == null || nodeVersion.trim().isEmpty())
        {
            log.warn("Node version is not configured; skipping Node/npm download.");
            return null;
        }

        String arch = resolveNodeArch();
        if (arch == null)
        {
            log.warn("Unsupported CPU architecture for Node download: {}", System.getProperty("os.arch"));
            return null;
        }

        File toolsDir = resolveToolsDirectory(appBaseDirectory);
        if (!toolsDir.exists() && !toolsDir.mkdirs())
        {
            log.warn("Unable to create tools directory at {}", toolsDir.getAbsolutePath());
            return null;
        }

        String nodeRootName;
        String archiveName;
        String downloadUrl;
        boolean isWindows = SystemUtils.IS_OS_WINDOWS;
        if (isWindows)
        {
            nodeRootName = "node-v" + nodeVersion + "-win-" + arch;
            archiveName = nodeRootName + ".zip";
        }
        else if (SystemUtils.IS_OS_LINUX)
        {
            nodeRootName = "node-v" + nodeVersion + "-linux-" + arch;
            archiveName = nodeRootName + ".tar.gz";
        }
        else if (SystemUtils.IS_OS_MAC)
        {
            nodeRootName = "node-v" + nodeVersion + "-darwin-" + arch;
            archiveName = nodeRootName + ".tar.gz";
        }
        else
        {
            log.warn("Node download is only supported on Windows, Linux, and macOS.");
            return null;
        }

        File nodeRootDir = new File(toolsDir, nodeRootName);
        String npmPath = isWindows
                ? new File(nodeRootDir, "npm.cmd").getAbsolutePath()
                : new File(nodeRootDir, "bin/npm").getAbsolutePath();
        if (new File(npmPath).isFile())
        {
            log.info("Using cached Node/npm at {}", npmPath);
            return npmPath;
        }

        File archiveFile = new File(toolsDir, archiveName);
        downloadUrl = "https://nodejs.org/dist/v" + nodeVersion + "/" + archiveName;
        log.info("Downloading Node.js {} from {}", nodeVersion, downloadUrl);
        if (!downloadFile(downloadUrl, archiveFile))
        {
            log.warn("Failed to download Node.js from {}", downloadUrl);
            return null;
        }

        boolean extracted = isWindows
                ? extractZip(archiveFile, toolsDir)
                : extractTarGz(archiveFile, toolsDir);
        if (!extracted)
        {
            log.warn("Failed to extract Node.js archive {}", archiveFile.getAbsolutePath());
            return null;
        }

        if (new File(npmPath).isFile())
        {
            return npmPath;
        }
        log.warn("npm executable not found after extraction at {}", npmPath);
        return null;
    }

    private static File resolveToolsDirectory(File appBaseDirectory)
    {
        File baseDir = AppUtils.baseUserDirectory != null ? AppUtils.baseUserDirectory : appBaseDirectory;
        return new File(baseDir, ".jwebmp-tools");
    }

    private static String resolveNodeArch()
    {
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        if (arch.contains("aarch64") || arch.contains("arm64"))
        {
            return "arm64";
        }
        if (arch.contains("64"))
        {
            return "x64";
        }
        if (arch.contains("86"))
        {
            return "x86";
        }
        return null;
    }

    private static boolean downloadFile(String url, File destination)
    {
        try
        {
            URL source = new URL(url);
            Files.createDirectories(destination.toPath()
                                                 .getParent());
            try (InputStream inputStream = source.openStream())
            {
                Files.copy(inputStream, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    private static boolean extractZip(File archive, File targetDir)
    {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(archive.toPath())))
        {
            ZipEntry entry;
            Path targetPath = targetDir.toPath()
                                       .toAbsolutePath()
                                       .normalize();
            while ((entry = zis.getNextEntry()) != null)
            {
                Path newPath = targetDir.toPath()
                                        .resolve(entry.getName())
                                        .normalize();
                if (!newPath.toAbsolutePath()
                            .startsWith(targetPath))
                {
                    log.warn("Skipping suspicious zip entry {}", entry.getName());
                    continue;
                }
                if (entry.isDirectory())
                {
                    Files.createDirectories(newPath);
                }
                else
                {
                    Files.createDirectories(newPath.getParent());
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    private static boolean extractTarGz(File archive, File targetDir)
    {
        List<String> command = Arrays.asList("tar", "-xzf", archive.getAbsolutePath(), "-C", targetDir.getAbsolutePath());
        return runCommand(targetDir, command, true) == 0;
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

            List<String> globalAssignments = new ArrayList<>();
            for (ClassInfo classInfo : ir)
            {
                var a = classInfo.loadClass()
                                 .getAnnotationsByType(NgBootImportReference.class);
                for (NgBootImportReference ngBootImportReference : a)
                {
                    String importString;
                    if (ngBootImportReference.sideEffect())
                    {
                        importString = "import '" + ngBootImportReference.reference() + "'";
                    }
                    else if (ngBootImportReference.direct() || !ngBootImportReference.wrapValueInBraces())
                    {
                        importString = "import " + ngBootImportReference.value() + " from '" + ngBootImportReference.reference() + "'";
                    }
                    else
                    {
                        importString = "import {" + ngBootImportReference.value() + "} from '" + ngBootImportReference.reference() + "'";
                    }
                    imports.add(importString);
                    if (ngBootImportReference.assignToGlobal() && !ngBootImportReference.sideEffect())
                    {
                        globalAssignments.add("(globalThis as any)." + ngBootImportReference.value() + " = " + ngBootImportReference.value() + ";");
                    }
                }
            }
            imports.forEach(a -> bootImportsString.append(a)
                                                  .append("\n"));
            globalAssignments.forEach(a -> bootImportsString.append(a)
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

        Set<TypescriptIndexPageConfigurator> indexPageConfigurators = IGuiceContext.loaderToSet(ServiceLoader.load(TypescriptIndexPageConfigurator.class));
        for (TypescriptIndexPageConfigurator a : indexPageConfigurators)
        {
            p = (Page<?>) a.configure(p);
        }

        // Iterate through standard page configurators and allow them to adjust the page specifically for Angular compilation
        Set<com.jwebmp.core.services.IPageConfigurator> pageConfigurators = IGuiceContext.loaderToSet(ServiceLoader.load(com.jwebmp.core.services.IPageConfigurator.class));
        for (com.jwebmp.core.services.IPageConfigurator configurator : pageConfigurators)
        {
            try
            {
                // Respect service enablement if implemented
                boolean enabled = true;
                try
                {
                    enabled = configurator.enabled();
                }
                catch (Throwable ignored)
                {
                    // If not supported, assume enabled
                }
                if (enabled)
                {
                    p = (Page) configurator.configureAngular(p);
                }
            }
            catch (Throwable t)
            {
                log.warn("IPageConfigurator ({} ) failed during configureAngular on boot index page",
                        configurator.getClass().getName(), t);
            }
        }

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

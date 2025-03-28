package com.jwebmp.core.base.angular.services.compiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.guicedee.client.CallScoper;
import com.guicedee.client.IGuiceContext;
import com.guicedee.guicedservlets.websockets.options.CallScopeProperties;
import com.guicedee.vertx.spi.VertXPreStartup;
import com.jwebmp.core.Page;
import com.jwebmp.core.base.ComponentHierarchyBase;
import com.jwebmp.core.base.angular.client.AppUtils;
import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.annotations.angularconfig.NgAsset;
import com.jwebmp.core.base.angular.client.annotations.angularconfig.NgPolyfill;
import com.jwebmp.core.base.angular.client.annotations.angularconfig.NgScript;
import com.jwebmp.core.base.angular.client.annotations.angularconfig.NgStyleSheet;
import com.jwebmp.core.base.angular.client.annotations.boot.NgBootImportProvider;
import com.jwebmp.core.base.angular.client.annotations.boot.NgBootImportReference;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.annotations.typescript.TsDependencies;
import com.jwebmp.core.base.angular.client.annotations.typescript.TsDependency;
import com.jwebmp.core.base.angular.client.annotations.typescript.TsDevDependencies;
import com.jwebmp.core.base.angular.client.annotations.typescript.TsDevDependency;
import com.jwebmp.core.base.angular.client.services.AnnotationHelper;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.modules.services.base.EnvironmentModule;
import com.jwebmp.core.base.angular.services.NGApplication;
import com.jwebmp.core.base.angular.services.RenderedAssets;
import com.jwebmp.core.base.angular.services.interfaces.NpmrcConfigurator;
import com.jwebmp.core.base.angular.typescript.JWebMP.ResourceLocator;
import com.jwebmp.core.base.html.Body;
import com.jwebmp.core.base.html.DivSimple;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;
import com.jwebmp.core.base.servlets.enumarations.DevelopmentEnvironments;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.Resource;
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
import static com.jwebmp.core.base.angular.client.AppUtils.getAppMainTSPath;
import static com.jwebmp.core.base.angular.client.AppUtils.getFile;
import static com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils.getTsFilename;
import static com.jwebmp.core.base.angular.client.services.interfaces.IComponent.currentAppFile;
import static com.jwebmp.core.base.angular.client.services.interfaces.IComponent.getClassDirectory;
import static java.nio.charset.StandardCharsets.UTF_8;

@Log4j2
public class JWebMPTypeScriptCompiler
{
    private final Set<String> assetStringBuilder = new LinkedHashSet<>();
    private final Set<String> stylesGlobal = new LinkedHashSet<>();
    private final Set<String> scripts = new LinkedHashSet<>();
    private final Set<String> npmrc = new LinkedHashSet<>();

    private NgApp ngApp;
    private INgApp<?> app;

    public static ThreadLocal<File> getCurrentAppFile()
    {
        return IComponent.getCurrentAppFile();
    }

    private List<File> completedFiles = new ArrayList<>();

    public JWebMPTypeScriptCompiler(INgApp<?> app)
    {
        this.app = app;
        IComponent.app.set(app);
        this.ngApp = app.getClass()
                .getAnnotation(NgApp.class);
        File appPath = AppUtils.getAppPath((Class<? extends INgApp<?>>) app.getClass());
        currentAppFile.set(appPath);
        log.info("Application [{}] is compiling to {}. Change with env property \"jwebmp\"", ngApp.value(), appPath.getPath());
    }

    private static final Set<INgApp<?>> allApps = new LinkedHashSet<>();

    public static Set<INgApp<?>> getAllApps()
    {
        if (allApps.isEmpty())
        {
            for (ClassInfo classInfo : IGuiceContext.instance()
                    .getScanResult()
                    .getClassesWithAnnotation(NgApp.class))
            {
                if (classInfo.isAbstract() || classInfo.isInterfaceOrAnnotation())
                {
                    continue;
                }
                try
                {
                    INgApp<?> clazz = (INgApp<?>) IGuiceContext.get(classInfo.loadClass());
                    IComponent.app.set(clazz);
                    File appPath = AppUtils.getAppPath((Class<? extends INgApp<?>>) clazz.getClass());
                    currentAppFile.set(appPath);
                    log.debug("Generating Angular Application - ({}) in folder {}", getTsFilename(clazz), getClassDirectory(classInfo.loadClass()));
                    allApps.add(clazz);
                }
                catch (ClassCastException e)
                {
                    log.error("Cannot render app - {} / Annotated @NgApp does not implement INgApp", classInfo.getSimpleName(), e);
                }
            }
        }
        return allApps;
    }

    public StringBuilder renderDataTypeTS(NgApp ngApp, File srcDirectory, INgDataType<?> component, Class<?> requestingClass) throws
                                                                                                                              IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append(component.renderClassTs());
        return sb;
    }

    public StringBuilder renderServiceProviderTS(NgApp ngApp, File srcDirectory, INgServiceProvider<?> component, Class<?> requestingClass) throws
                                                                                                                                            IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append(component.renderClassTs());
        return sb;
    }

    public StringBuilder renderProviderTS(NgApp ngApp, File srcDirectory, INgProvider<?> component, Class<?> requestingClass) throws
                                                                                                                              IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append(component.renderClassTs());
        return sb;
    }


    public StringBuilder renderServiceTS(NgApp ngApp, File srcDirectory, INgDataService<?> component, Class<?> requestingClass) throws
                                                                                                                                IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append(component.renderClassTs());
        return sb;
    }

    public StringBuilder renderDirectiveTS(NgApp ngApp, File srcDirectory, INgDirective<?> component, Class<?> requestingClass) throws
                                                                                                                                IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append(component.renderClassTs());
        return sb;
    }

    public StringBuilder renderComponentTS(NgApp ngApp, File srcDirectory, INgComponent<?> component, Class<?> requestingClass) throws
                                                                                                                                IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append(component.renderClassTs());
        return sb;
    }

    public StringBuilder renderModuleTS(NgApp ngApp, File srcDirectory, INgModule<?> component, Class<?> requestingClass) throws
                                                                                                                          IOException
    {
        StringBuilder sb = new StringBuilder();
        component.setApp(app);
        sb.append(component.renderClassTs());
        return sb;
    }

    public StringBuilder renderAppTS(INgApp<?> appNgApp) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        ngApp = app.getClass()
                .getAnnotation(NgApp.class);
        Class<? extends INgApp<?>> appClass = (Class<? extends INgApp<?>>) appNgApp.getClass();
        AppUtils.getAppPath(appClass);
        INgApp<?> app = appNgApp;
        IComponent.app.set(app);
        ScanResult scan = IGuiceContext.instance()
                .getScanResult();
        CallScoper scoper = IGuiceContext.get(CallScoper.class);
        try
        {
            scoper.enter();

            IGuiceContext.get(EnvironmentModule.class)
                    .getEnvironmentOptions()
                    .setAppClass(appClass.getCanonicalName());

            IGuiceContext.get(EnvironmentModule.class)
                    .getEnvironmentOptions()
                    .setProduction(app.getRunningEnvironment()
                            .equals(DevelopmentEnvironments.Production));

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

            File packageJsonFile = AppUtils.getAppPackageJsonPath(appClass, true);// new File(appBaseDirectory.getCanonicalPath() + "/package.json");

            String packageTemplate = IOUtils.toString(Objects.requireNonNull(ResourceLocator.class.getResourceAsStream("package.json")), UTF_8);

            Map<String, String> dependencies = new TreeMap<>();
            Map<String, String> devDependencies = new TreeMap<>();
            Map<String, String> overrideDependencies = new TreeMap<>();

            scan.getClassesWithAnnotation(TsDependency.class)
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
            scan.getClassesWithAnnotation(TsDevDependency.class)
                    .stream()
                    .forEach(a -> {
                        TsDevDependency annotation = a.loadClass()
                                .getAnnotation(TsDevDependency.class);
                        if (annotation != null)
                        {
                            devDependencies.put(annotation.value(), annotation.version());
                        }
                    });
            scan.getClassesWithAnnotation(TsDependencies.class)
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
            scan.getClassesWithAnnotation(TsDevDependencies.class)
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


            ObjectMapper om = IGuiceContext.get(DefaultObjectMapper);
            String appName = AppUtils.getAppName(appClass);
            packageTemplate = packageTemplate.replace("/*appName*/", appName);
            packageTemplate = packageTemplate.replace("/*dependencies*/", om.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(dependencies));
            packageTemplate = packageTemplate.replace("/*devDependencies*/", om.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(devDependencies));
            packageTemplate = packageTemplate.replace("/*overrideDependencies*/", om.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(overrideDependencies));

            FileUtils.writeStringToFile(packageJsonFile, packageTemplate, UTF_8, false);

            File tsConfigFile = AppUtils.getAppTsConfigAppPath(appClass, true);// new File( AppUtils.getFileReferenceAppFile(appClass,"/tsconfig.app.json"));
            String tsConfigTemplate = IOUtils.toString(Objects.requireNonNull(ResourceLocator.class.getResourceAsStream("tsconfig.app.json")), UTF_8);
            FileUtils.writeStringToFile(tsConfigFile, tsConfigTemplate, UTF_8, false);

            File tsConfigFileAbs = AppUtils.getAppTsConfigPath(appClass, true); //new File(AppUtils.getFileReferenceAppFile(appClass,"/tsconfig.json"));
            String tsConfigTemplateAbs = IOUtils.toString(Objects.requireNonNull(ResourceLocator.class.getResourceAsStream("tsconfig.json")), UTF_8);
            FileUtils.writeStringToFile(tsConfigFileAbs, tsConfigTemplateAbs, UTF_8, false);

            File packageLockFile = new File(AppUtils.getAppPath(appClass)
                    .getCanonicalPath() + "/package-lock.json");
            if (packageLockFile.exists() && packageLockFile.isFile())
            {
                packageLockFile.delete();
            }

            File polyfillFile = AppUtils.getAppPolyfillsPath(appClass, true);// new File(srcDirectory.getCanonicalPath() + "/polyfills.ts");
            StringBuilder polyfills = new StringBuilder();
            for (NgPolyfill globalAnnotation : IGuiceContext.get(AnnotationHelper.class)
                    .getGlobalAnnotations(NgPolyfill.class))
            {
                String newString = globalAnnotation.value();
                polyfills.append("import \"" + newString + "\";\n");
            }
            FileUtils.writeStringToFile(polyfillFile, polyfills.toString(), UTF_8, false);

            //APP.CONFIG.TS
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
                        bootImportProviders.append(ngBootImportProvider.value()).append(",\n");
                    }
                    //.append(",");
                }
                bootAppString = bootAppString.formatted(bootImportsString.toString(), bootImportProviders.toString());
                FileUtils.writeStringToFile(new File(AppUtils.getAppSrcPath(appClass) + "/app.config.ts"), bootAppString, UTF_8);
            }


            Map<String, String> namedAssets = new HashMap<>();
            List<NgAsset> assets = IGuiceContext.get(AnnotationHelper.class)
                    .getGlobalAnnotations(NgAsset.class);

            for (NgAsset ngAsset : assets)
            {
                String name = ngAsset.name();
                if (Strings.isNullOrEmpty(ngAsset.name()))
                {
                    name = ngAsset.value();
                }
                namedAssets.put(name, ngAsset.value());
            }
            for (NgAsset ngAsset : assets)
            {
                String name = ngAsset.name();
                if (ngAsset.replaces().length > 0)
                {
                    for (String replace : ngAsset.replaces())
                    {
                        namedAssets.put(replace, ngAsset.value());
                    }
                }
            }

            Map<String, String> namedStylesheets = new LinkedHashMap<>();
            List<NgStyleSheet> ngStyleSheets = IGuiceContext.get(AnnotationHelper.class)
                    .getGlobalAnnotations(NgStyleSheet.class);
            ngStyleSheets.sort(new Comparator<NgStyleSheet>()
            {
                @Override
                public int compare(NgStyleSheet o1, NgStyleSheet o2)
                {
                    return Integer.compare(o1.sortOrder(), o2.sortOrder());
                }
            });
            for (NgStyleSheet ngAsset : ngStyleSheets)
            {
                String name = ngAsset.name();
                if (Strings.isNullOrEmpty(ngAsset.name()))
                {
                    name = ngAsset.value();
                }
                namedStylesheets.put(name, ngAsset.value());
            }
            for (NgStyleSheet ngAsset : ngStyleSheets)
            {
                String name = ngAsset.name();
                if (ngAsset.replaces().length > 0)
                {
                    for (String replace : ngAsset.replaces())
                    {
                        namedStylesheets.put(replace, ngAsset.value());
                    }
                }
            }

            for (String value : namedStylesheets.values())
            {
                stylesGlobal.add(value);
            }

            for (String stylesheet : app.stylesheets())
            {
                stylesGlobal.add("public/assets/" + stylesheet);
            }

            Map<String, String> namedScripts = new LinkedHashMap<>();

            List<NgScript> allAnnotations = IGuiceContext.get(AnnotationHelper.class)
                    .getGlobalAnnotations(NgScript.class);
            allAnnotations.sort(new Comparator<NgScript>()
            {
                @Override
                public int compare(NgScript o1, NgScript o2)
                {
                    return Integer.compare(o1.sortOrder(), o2.sortOrder());
                }
            });

            for (NgScript ngAsset : allAnnotations)
            {
                String name = ngAsset.name();
                if (Strings.isNullOrEmpty(ngAsset.name()))
                {
                    name = ngAsset.value();
                }
                namedScripts.put(name, ngAsset.value());
            }

            for (NgScript ngAsset : allAnnotations)
            {
                String name = ngAsset.name();
                if (ngAsset.replaces().length > 0)
                {
                    for (String replace : ngAsset.replaces())
                    {
                        namedScripts.put(replace, ngAsset.value());
                    }
                }
            }

            for (String value : namedScripts.values())
            {
                scripts.add(value);
            }

            for (String stylesheet : app.scripts())
            {
                scripts.add("public/assets/" + stylesheet);
            }

            var bootComponentClass = ngApp.bootComponent();
            var appDirectory = getAppMainTSPath((Class<? extends INgApp<?>>) app.getClass(), true);

            List<NgImportReference> importReferences = new ImportsStatementsComponent()
            {
            }.putRelativeLinkInMap(appDirectory, bootComponentClass);

            sb.append("import {%s} from '%s';\n".formatted(importReferences.get(0).value(), importReferences.get(0).reference()));

            //   sb.append(app.renderImports());

            sb.append("""
                            import {bootstrapApplication} from '@angular/platform-browser';
                            import {appConfig} from './app/app.config';
                            
                            bootstrapApplication(""")
                    .append(ngApp.bootComponent()
                            .getSimpleName())
                    .append("\n")
                    .append(", appConfig).catch((err) => console.error(err));");


            log.debug("Writing out angular main.ts file - {}", AppUtils.getAppMainTSPath(appClass, false));
            try
            {
                String bootAppString = sb.toString();
                FileUtils.writeStringToFile(AppUtils.getAppMainTSPath(appClass, true), bootAppString, UTF_8, false);
            }
            catch (IOException e)
            {
                log.error("Unable to write out angular main.ts file", e);
            }

            File mainIndexHtmlTsFile = AppUtils.getIndexHtmlPath(appClass, true);//new File(srcDirectory.getCanonicalPath() + "/" + "index.html");
            log.debug("Writing out index.html - {}", mainIndexHtmlTsFile.getCanonicalPath());
            try
            {
                FileUtils.writeStringToFile(mainIndexHtmlTsFile, renderBootIndexHtml(app).toString(), UTF_8, false);
            }
            catch (IOException e)
            {
                log.error("Unable to write out index.html file", e);
            }

            log.info("Writing out src/assets/ resources...");
            for (Resource resource : scan.getResourcesMatchingWildcard("assets/**"))
            {
                AppUtils.saveAsset(appClass, resource.getURL()
                        .openStream(), resource.getPath());
            }
            for (Resource resource : scan.getResourcesMatchingWildcard("src/assets/**"))
            {
                AppUtils.saveAsset(appClass, resource.getURL()
                        .openStream(), resource.getPath());
            }
            for (Resource resource : scan.getResourcesMatchingWildcard("src/public/**"))
            {
                AppUtils.saveAsset(appClass, resource.getURL()
                        .openStream(), resource.getPath());
            }
            for (Resource resource : scan.getResourcesMatchingWildcard("public/**"))
            {
                AppUtils.saveAsset(appClass, resource.getURL()
                        .openStream(), resource.getPath());
            }

            log.info("Loading resources from assets directory");
            for (Resource allResource : scan.getResourcesMatchingWildcard("app/**"))
            {
                String assetLocation = allResource.getPathRelativeToClasspathElement();
                InputStream fileStream = allResource.getURL()
                        .openStream();
                AppUtils.saveAppResourceFile(appClass, fileStream, assetLocation);
            }

            File finalSrcDirectory = AppUtils.getAppSrcPath(appClass);

            scan.getClassesWithAnnotation(NgModule.class)
                    .stream()
                    .forEach(a -> {
                        Set<Class<?>> classes = new HashSet<>();
                        if (a.isInterface() || a.isAbstract())
                        {
                            for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
                            {
                                if (!subclass.isAbstract() && !subclass.isInterface())
                                {
                                    classes.add(subclass.loadClass());
                                }
                            }
                        }
                        else
                        {
                            Class<?> aClass = a.loadClass();
                            classes.add(aClass);
                        }
                        for (Class<?> aClass : classes)
                        {
                            File classFile = null;
                            classFile = getFile(appClass, aClass, ".ts");
                            if (!completedFiles.contains(classFile))
                            {
                                try
                                {
                                    completedFiles.add(classFile);
                                    FileUtils.forceMkdirParent(classFile);
                                    INgModule<?> modd = (INgModule<?>) IGuiceContext.get(aClass);
                                    modd.setApp(app);
                                    FileUtils.write(classFile, renderModuleTS(ngApp, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

            //components load through the child hierarchy of the app --
            if (app instanceof NGApplication<?> application)
            {
                var standaloneComponents = scan.getClassesWithAnnotation(NgComponent.class)
                        .stream()
                        .filter(a -> !a.isAbstract() && !a.isInterface())
                        .filter(a -> a.loadClass()
                                .getAnnotation(NgComponent.class)
                                .standalone())
                        .filter(a -> {
                            String packageName = a.getPackageName();
                            return packageName.startsWith("com.jwebmp") ||
                                    packageName.startsWith("com.guicedee") ||
                                    packageName.startsWith(app.getClass().getPackageName());
                        });

                // Process the filtered components
                standaloneComponents.distinct().forEach(aClass -> {
                    var vertx = VertXPreStartup.getVertx();

                    vertx.<Boolean>executeBlocking(() -> {
                        var callScoper = IGuiceContext.get(CallScoper.class);
                        boolean x = processStandaloneComponent(application, aClass, callScoper, app, appClass, finalSrcDirectory);
                        return x;
                    }, false);
                });
            }

            scan.getClassesWithAnnotation(NgDirective.class)
                    .stream()
                    // .filter(a -> !(a.isInterface() || a.isAbstract()))
                    .forEach(a -> {
                        Set<Class<?>> classes = new HashSet<>();
                        if (a.isInterface() || a.isAbstract())
                        {
                            for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
                            {
                                if (!subclass.isAbstract() && !subclass.isInterface())
                                {
                                    classes.add(subclass.loadClass());
                                }
                            }
                        }
                        else
                        {
                            Class<?> aClass = a.loadClass();
                            classes.add(aClass);
                        }
                        for (Class<?> aClass : classes)
                        {
                            File classFile = null;
                            classFile = getFile(appClass, aClass, ".ts");
                            if (!completedFiles.contains(classFile))
                            {
                                try
                                {
                                    completedFiles.add(classFile);
                                    FileUtils.forceMkdirParent(classFile);
                                    INgDirective<?> modd = (INgDirective<?>) IGuiceContext.get(aClass);
                                    FileUtils.write(classFile, renderDirectiveTS(ngApp, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }


                    });

            scan.getClassesWithAnnotation(NgDataService.class)
                    .stream()
                    //    .filter(a -> !(a.isInterface() || a.isAbstract()))
                    .forEach(a -> {
                        Set<Class<?>> classes = new HashSet<>();
                        if (a.isInterface() || a.isAbstract())
                        {
                            for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
                            {
                                if (!subclass.isAbstract() && !subclass.isInterface())
                                {
                                    classes.add(subclass.loadClass());
                                }
                            }
                        }
                        else
                        {
                            Class<?> aClass = a.loadClass();
                            classes.add(aClass);
                        }
                        for (Class<?> aClass : classes)
                        {
                            File classFile = null;
                            classFile = getFile(appClass, aClass, ".ts");
                            if (!completedFiles.contains(classFile))
                            {
                                try
                                {
                                    completedFiles.add(classFile);
                                    FileUtils.forceMkdirParent(classFile);
                                    INgDataService modd = (INgDataService) IGuiceContext.get(aClass);
                                    FileUtils.write(classFile, renderServiceTS(ngApp, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });


            scan.getClassesWithAnnotation(NgProvider.class)
                    .stream()
                    //.filter(a -> !(a.isInterface() || a.isAbstract()))
                    .forEach(a -> {
                        Set<Class<?>> classes = new HashSet<>();
                        if (a.isInterface() || a.isAbstract())
                        {
                            for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
                            {
                                if (!subclass.isAbstract() && !subclass.isInterface())
                                {
                                    classes.add(subclass.loadClass());
                                }
                            }
                        }
                        else
                        {
                            Class<?> aClass = a.loadClass();
                            classes.add(aClass);
                        }
                        for (Class<?> aClass : classes)
                        {
                            File classFile = null;
                            classFile = getFile(appClass, aClass, ".ts");
                            if (!completedFiles.contains(classFile))
                            {
                                try
                                {
                                    completedFiles.add(classFile);
                                    FileUtils.forceMkdirParent(classFile);
                                    INgProvider<?> modd = (INgProvider<?>) IGuiceContext.get(aClass);
                                    FileUtils.write(classFile, renderProviderTS(ngApp, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

            scan.getClassesWithAnnotation(NgDataType.class)
                    .stream()
                    //   .filter(a -> !(a.isInterface() || a.isAbstract()))
                    .forEach(a -> {
                        Set<Class<?>> classes = new HashSet<>();
                        if (a.isInterface() || a.isAbstract())
                        {
                            for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
                            {
                                if (!subclass.isAbstract() && !subclass.isInterface())
                                {
                                    classes.add(subclass.loadClass());
                                }
                            }
                        }
                        else
                        {
                            Class<?> aClass = a.loadClass();
                            classes.add(aClass);
                        }
                        for (Class<?> aClass : classes)
                        {
                            File classFile = null;
                            classFile = getFile(appClass, aClass, ".ts");
                            if (!completedFiles.contains(classFile))
                            {
                                try
                                {
                                    completedFiles.add(classFile);
                                    FileUtils.forceMkdirParent(classFile);
                                    INgDataType modd = (INgDataType) IGuiceContext.get(aClass);
                                    FileUtils.write(classFile, renderDataTypeTS(ngApp, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

            scan.getClassesWithAnnotation(NgServiceProvider.class)
                    .stream()
                    //   .filter(a -> !(a.isInterface() || a.isAbstract()))
                    .forEach(a -> {
                        Set<Class<?>> classes = new HashSet<>();
                        if (a.isInterface() || a.isAbstract())
                        {
                            for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(a.loadClass()) : scan.getClassesImplementing(a.loadClass()))
                            {
                                if (!subclass.isAbstract() && !subclass.isInterface())
                                {
                                    classes.add(subclass.loadClass());
                                }
                            }
                        }
                        else
                        {
                            Class<?> aClass = a.loadClass();
                            classes.add(aClass);
                        }
                        for (Class<?> aClass : classes)
                        {
                            File classFile = null;
                            classFile = getFile(appClass, aClass, ".ts");
                            if (!completedFiles.contains(classFile))
                            {
                                try
                                {
                                    completedFiles.add(classFile);
                                    FileUtils.forceMkdirParent(classFile);
                                    INgServiceProvider<?> modd = (INgServiceProvider<?>) IGuiceContext.get(aClass);
                                    FileUtils.write(classFile, renderServiceProviderTS(ngApp, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });


            log.info("Registering Assets...");
            List<String> assetList = AppUtils.getAssetList(appClass);
            if (assetList != null)
            {
                for (String assetName : assetList)
                {
                    assetStringBuilder.add(assetName);
                }
            }

            Set<RenderedAssets> renderedAssets = IGuiceContext.loaderToSet(ServiceLoader.load(RenderedAssets.class));
            for (RenderedAssets<?> renderedAsset : renderedAssets)
            {
                for (String asset : renderedAsset.assets())
                {
                    namedAssets.put(asset, asset);
                }
            }

            for (String value : namedAssets.values())
            {
                assetStringBuilder.add(value);
            }

            for (String asset : app.assets())
            {
                assetStringBuilder.add(asset);
            }

            StringBuilder assetsAngular19 = new StringBuilder();
            assetsAngular19.append("""
                    
                                [
                                  {
                                    "glob": "**/*",
                                    "input": "public"
                                  }
                                ]
                    """);

            assetStringBuilder.removeIf(a -> stylesGlobal.contains(a));

            String angularTemplate = IOUtils.toString(Objects.requireNonNull(ResourceLocator.class.getResourceAsStream("angular.json")), UTF_8);

            angularTemplate = angularTemplate.replace("/*BuildAssets*/", assetsAngular19);


            angularTemplate = angularTemplate.replace("/*BuildStylesSCSS*/", om.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(stylesGlobal));
            angularTemplate = angularTemplate.replace("/*BuildScripts*/", om.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(scripts));
            angularTemplate = angularTemplate.replace("/*MainTSFile*/", om.writerWithDefaultPrettyPrinter()
                    .writeValueAsString("src/main.ts"));

            File angularFile = AppUtils.getAngularJsonPath(appClass, true);// new File(appBaseDirectory.getCanonicalPath() + "/angular.json");
            FileUtils.writeStringToFile(angularFile, angularTemplate, UTF_8, false);


           /* if (buildApp)
            {
                System.out.println("Installing node-modules...");
                installDependencies(AppUtils.getAppPath(appClass));
            }
            if (buildApp)
            {
                System.out.println("Building Angular Client App...");
                installAngular(AppUtils.getAppPath(appClass));
            }*/
        }
        finally
        {
            scoper.exit();
        }
        System.out.println("App Ready");
        //serveAngular(appBaseDirectory);
        return sb;
    }

    private boolean processStandaloneComponent(NGApplication<?> application, ClassInfo aClass, CallScoper callScoper, INgApp<?> app, Class<? extends INgApp<?>> appClass, File finalSrcDirectory)
    {
        try
        {
            callScoper.enter();
            var scopeProperties = IGuiceContext.get(CallScopeProperties.class);
            //scopeProperties.setSource(CallScopeSource.Http)
            IComponent.app.set(application);
            File appPath = AppUtils.getAppPath((Class<? extends INgApp<?>>) app.getClass());
            currentAppFile.set(appPath);
            Class<?> clazz = aClass.loadClass();
            IComponentHierarchyBase<?, ?> ngComponent = (IComponentHierarchyBase<?, ?>) IGuiceContext.get(clazz);
            var html = ngComponent.toString(0);
            {
                var childClass = clazz;
                if (childClass.isAnnotationPresent(NgComponent.class) &&
                        ngComponent instanceof INgComponent<?> component)
                {
                    DivSimple<?> dummyAdd = new DivSimple<>();
                    dummyAdd.add(ngComponent);
                    dummyAdd.toString(true);
                    File classFile = null;
                    classFile = getFile(appClass, childClass, ".ts");
                    var htmlFile = getFile(appClass, clazz, ".html");
                    var cssFile = getFile(appClass, clazz, ".scss");
                    if (!classFile.getCanonicalPath().replace('\\', '/').contains("src/app/"))
                    {
                        log.error("Unable to write out component file - {}", classFile.getCanonicalPath());
                        return false;
                    }
                    StringBuilder cssString = ngComponent.cast()
                            .asStyleBase()
                            .renderCss(1);

                    if (!completedFiles.contains(classFile))
                    {
                        try
                        {
                            completedFiles.add(classFile);
                            FileUtils.forceMkdirParent(classFile);
                            FileUtils.write(classFile, renderComponentTS(ngApp, finalSrcDirectory, component, component.getClass()), UTF_8, false);
                            FileUtils.write(htmlFile, html, UTF_8, false);
                            FileUtils.write(cssFile, cssString.toString(), UTF_8, false);
                            return true;
                        }
                        catch (Exception e)
                        {
                            log.error("Unable to write out component file", e);
                            return false;
                        }
                    }
                }
            }
        }
        catch (Throwable e)
        {
            log.error("Error rendering routes", e);
        }
        finally
        {
            callScoper.exit();
        }
        return true;
    }

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

        return sb;
    }

    public static void installDependencies(File appBaseDirectory)
    {
        if (SystemUtils.IS_OS_WINDOWS)
        {
            try
            {
                ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", FileUtils.getUserDirectory() + "/AppData/Roaming/npm/npm.cmd install");
                //processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                //processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
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
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void installAngular(File appBaseDirectory)
    {
        if (SystemUtils.IS_OS_WINDOWS)
        {
            try
            {
                ProcessBuilder processBuilder =

                        new ProcessBuilder("cmd.exe", "/c", FileUtils.getUserDirectory() + "/AppData/Roaming/npm/npm.cmd", "run", "build" + (IGuiceContext.get(EnvironmentModule.class)
                                .getEnvironmentOptions()
                                .isProduction() ? "-prod" : ""));
                processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
                processBuilder.environment()
                        .putAll(System.getenv());
                processBuilder = processBuilder.directory(appBaseDirectory);
                Process p = processBuilder.start();
				/*try
				{
					p.waitFor();
					p.destroyForcibly();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}*/
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

    private static final Map<String, TsDependency> namedDependencies = new HashMap<>();

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
        }
        else
        {
            namedDependencies.putIfAbsent(name, dependency);
        }
        TsDependency dep = namedDependencies.get(name);
        return dep;
    }

}

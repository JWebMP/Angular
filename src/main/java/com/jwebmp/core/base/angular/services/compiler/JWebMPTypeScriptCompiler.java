package com.jwebmp.core.base.angular.services.compiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.guicedee.client.CallScopeProperties;
import com.guicedee.client.CallScoper;
import com.guicedee.client.IGuiceContext;
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
import com.jwebmp.core.base.angular.client.services.TypescriptIndexPageConfigurator;
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
import java.util.function.Predicate;

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

    private final Predicate<Class<?>> packageFilter = a -> {
        String packageName = a.getPackageName();
        return packageName.startsWith("com.jwebmp") ||
                packageName.startsWith("com.guicedee") ||
                packageName.startsWith(app.getClass()
                                          .getPackageName());
    };
    private final Predicate<ClassInfo> packageFilterClassInfo = a -> {
        String packageName = a.getPackageName();
        return packageName.startsWith("com.jwebmp") ||
                packageName.startsWith("com.guicedee") ||
                packageName.startsWith(app.getClass()
                                          .getPackageName());
    };

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

        var vertx = VertXPreStartup.getVertx();

        try
        {
            // scoper.enter();

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
            vertx.executeBlocking(() -> {
                return processPackageJsonFile(currentAppFile.get(), appClass, packageTemplate, om, dependencies, devDependencies, overrideDependencies, packageJsonFile);
            }, false);

            vertx.executeBlocking(() -> {
                processTypeScriptConfigFiles(currentAppFile.get(), appClass);
                return true;
            }, false);

            vertx.executeBlocking(() -> {
                processPolyfillFile(currentAppFile.get(), appClass);
                return true;
            }, false);

            vertx.executeBlocking(() -> {
                processAppConfigFile(currentAppFile.get(), scan, appClass);
                return true;
            }, false);


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

            sb.append("import {%s} from '%s';\n".formatted(importReferences.getFirst()
                                                                           .value(), importReferences.getFirst()
                                                                                                     .reference()));

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
                vertx.executeBlocking(() -> {
                    String assetLocation = allResource.getPathRelativeToClasspathElement();
                    InputStream fileStream = allResource.getURL()
                                                        .openStream();
                    AppUtils.saveAppResourceFile(appClass, fileStream, assetLocation);
                    return true;
                }, false);
            }

            File finalSrcDirectory = AppUtils.getAppSrcPath(appClass);

            var currentApp = currentAppFile.get();

            scan.getClassesWithAnnotation(NgModule.class)
                .stream()
                //.filter(packageFilterClassInfo)
                .forEach(a -> {
                    vertx.executeBlocking(() -> {
                        processNgModuleFiles(currentApp, a, scan, appClass, app, finalSrcDirectory);
                        return true;
                    }, false);
                });

            //components load through the child hierarchy of the app --
            if (app instanceof NGApplication<?> application)
            {
                var standaloneComponents = scan.getClassesWithAnnotation(NgComponent.class)
                                               .stream()
                                               .filter(a -> !a.isAbstract() && !a.isInterface())
                                               .filter(a -> a.loadClass()
                                                             .getAnnotation(NgComponent.class)
                                                             .standalone());
                //.filter(packageFilterClassInfo);

                // Process the filtered components
                standaloneComponents.distinct()
                                    .forEach(aClass -> {
                                        vertx.executeBlocking(() -> {
                                            var callScoper = IGuiceContext.get(CallScoper.class);
                                            boolean x = processStandaloneComponent(currentApp, application, aClass, callScoper, app, appClass, finalSrcDirectory);
                                            return x;
                                        }, false);
                                    });
            }

            scan.getClassesWithAnnotation(NgDirective.class)
                .stream()
                //.filter(packageFilterClassInfo)
                .forEach(a -> {
                    vertx.executeBlocking(() -> {
                        processNgDirectiveFiles(currentApp, a, scan, appClass, finalSrcDirectory);
                        return true;
                    }, false);
                });

            scan.getClassesWithAnnotation(NgDataService.class)
                .stream()
                //.filter(packageFilterClassInfo)
                .forEach(a -> {
                    vertx.executeBlocking(() -> {
                        processNgDataServiceFiles(currentApp, a, scan, appClass, finalSrcDirectory);
                        return true;
                    }, false);
                });


            scan.getClassesWithAnnotation(NgProvider.class)
                .stream()
                //.filter(packageFilterClassInfo)
                .forEach(a -> {
                    vertx.executeBlocking(() -> {
                        processNgProviderFiles(currentApp, a, scan, appClass, finalSrcDirectory);
                        return true;
                    }, false);
                });

            scan.getClassesWithAnnotation(NgDataType.class)
                .stream()
                //.filter(packageFilterClassInfo)
                .forEach(a -> {
                    vertx.executeBlocking(() -> {
                        processNgDataTypeFiles(currentApp, a, scan, appClass, finalSrcDirectory);
                        return true;
                    }, false);
                });

            scan.getClassesWithAnnotation(NgServiceProvider.class)
                .stream()
                //.filter(packageFilterClassInfo)
                .forEach(a -> {
                    vertx.executeBlocking(() -> {
                        processNgServiceProviderFiles(currentApp, a, scan, appClass, finalSrcDirectory);
                        return true;
                    }, false);
                });

            vertx.executeBlocking(() -> {
                renderAngularApplicationFiles(currentApp, appClass, namedAssets, app, om);
                return true;
            }, false);

        }
        finally
        {
            //   scoper.exit();
        }
        log.debug("Angular App Ready");
        return sb;
    }

    private void renderAngularApplicationFiles(File currentApp, Class<? extends INgApp<?>> appClass, Map<String, String> namedAssets, INgApp<?> app, ObjectMapper om) throws IOException
    {
        currentAppFile.set(currentApp);
        // CallScoper scoper = IGuiceContext.get(CallScoper.class);
        // scoper.enter();
        try
        {
            log.debug("Registering Assets...");
            List<String> assetList = AppUtils.getAssetList(appClass);
            if (assetList != null)
            {
                assetStringBuilder.addAll(assetList);
            }
            @SuppressWarnings({"rawtypes", "unchecked"})
            Set<RenderedAssets> renderedAssets = IGuiceContext.loaderToSet(ServiceLoader.load(RenderedAssets.class));
            for (RenderedAssets<?> renderedAsset : renderedAssets)
            {
                for (String asset : renderedAsset.assets())
                {
                    namedAssets.put(asset, asset);
                }
            }
            assetStringBuilder.addAll(namedAssets.values());
            assetStringBuilder.addAll(app.assets());
            StringBuilder assetsAngular19 = new StringBuilder();
            assetsAngular19.append("""
                    
                                [
                                  {
                                    "glob": "**/*",
                                    "input": "public"
                                  }
                                ]
                    """);
            assetStringBuilder.removeIf(stylesGlobal::contains);

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
        }
        catch (Throwable e)
        {
            log.error("Unable to write out angular.json file", e);
        }
        finally
        {
            //   scoper.exit();
        }
    }

    private void processNgServiceProviderFiles(File currentApp, ClassInfo a, ScanResult scan, Class<? extends INgApp<?>> appClass, File finalSrcDirectory)
    {
        //  CallScoper scoper = IGuiceContext.get(CallScoper.class);
        //  scoper.enter();
        currentAppFile.set(currentApp);
        try
        {
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
                    completedFiles.add(classFile);
                    FileUtils.forceMkdirParent(classFile);
                    INgServiceProvider<?> modd = (INgServiceProvider<?>) IGuiceContext.get(aClass);
                    FileUtils.write(classFile, renderServiceProviderTS(ngApp, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
                }
            }
        }
        catch (Throwable e)
        {
            log.error("Unable to process NgServiceProvider files", e);
        }
        finally
        {
            //    scoper.exit();
        }
    }

    private void processNgDataTypeFiles(File currentApp, ClassInfo a, ScanResult scan, Class<? extends INgApp<?>> appClass, File finalSrcDirectory)
    {
        currentAppFile.set(currentApp);
        // CallScoper scoper = IGuiceContext.get(CallScoper.class);
        // scoper.enter();
        try
        {
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
                    completedFiles.add(classFile);
                    FileUtils.forceMkdirParent(classFile);
                    INgDataType modd = (INgDataType) IGuiceContext.get(aClass);
                    FileUtils.write(classFile, renderDataTypeTS(ngApp, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
                }
            }
        }
        catch (Throwable e)
        {
            log.error("Unable to process NgDataType files", e);
        }
    }

    private void processNgProviderFiles(File currentApp, ClassInfo a, ScanResult scan, Class<? extends INgApp<?>> appClass, File finalSrcDirectory)
    {
        currentAppFile.set(currentApp);
        // CallScoper scoper = IGuiceContext.get(CallScoper.class);
        //  scoper.enter();
        try
        {
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
                    completedFiles.add(classFile);
                    FileUtils.forceMkdirParent(classFile);
                    INgProvider<?> modd = (INgProvider<?>) IGuiceContext.get(aClass);
                    FileUtils.write(classFile, renderProviderTS(ngApp, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
                }
            }
        }
        catch (Throwable e)
        {
            log.error("Unable to process NgProvider files", e);
        }
    }

    private void processNgDataServiceFiles(File currentApp, ClassInfo a, ScanResult scan, Class<? extends INgApp<?>> appClass, File finalSrcDirectory)
    {
        currentAppFile.set(currentApp);
        //  CallScoper scoper = IGuiceContext.get(CallScoper.class);
        //  scoper.enter();
        try
        {
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
                    completedFiles.add(classFile);
                    FileUtils.forceMkdirParent(classFile);
                    INgDataService modd = (INgDataService) IGuiceContext.get(aClass);
                    FileUtils.write(classFile, renderServiceTS(ngApp, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
                }
            }
        }
        catch (Throwable e)
        {
            log.error("Unable to process NgDataService files", e);
        }
    }

    private void processNgDirectiveFiles(File currentApp, ClassInfo a, ScanResult scan, Class<? extends INgApp<?>> appClass, File finalSrcDirectory)
    {
        currentAppFile.set(currentApp);
        //    CallScoper scoper = IGuiceContext.get(CallScoper.class);
        //    scoper.enter();
        try
        {
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
                    completedFiles.add(classFile);
                    FileUtils.forceMkdirParent(classFile);
                    INgDirective<?> modd = (INgDirective<?>) IGuiceContext.get(aClass);
                    FileUtils.write(classFile, renderDirectiveTS(ngApp, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
                }
            }
        }
        catch (Throwable e)
        {
            log.error("Unable to process NgDirective files", e);
        }
    }

    private void processNgModuleFiles(File currentApp, ClassInfo a, ScanResult scan, Class<? extends INgApp<?>> appClass, INgApp<?> app, File finalSrcDirectory)
    {
        currentAppFile.set(currentApp);
        //CallScoper scoper = IGuiceContext.get(CallScoper.class);
        //  scoper.enter();
        try
        {
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
                    completedFiles.add(classFile);
                    FileUtils.forceMkdirParent(classFile);
                    INgModule<?> modd = (INgModule<?>) IGuiceContext.get(aClass);
                    modd.setApp(app);
                    FileUtils.write(classFile, renderModuleTS(ngApp, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
                }
            }
        }
        catch (Throwable e)
        {
            log.error("Unable to process NgModule files", e);
        }
    }

    private void processAppConfigFile(File file, ScanResult scan, Class<? extends INgApp<?>> appClass) throws IOException
    {
        currentAppFile.set(file);
        //  CallScoper scoper = IGuiceContext.get(CallScoper.class);
        //  scoper.enter();
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
                    String importString;
                    if (ngBootImportReference.direct() || !ngBootImportReference.wrapValueInBraces())
                    {
                        importString = "import " + ngBootImportReference.value() + " from '" + ngBootImportReference.reference() + "'";
                    }
                    else
                    {
                        importString = "import {" + ngBootImportReference.value() + "} from '" + ngBootImportReference.reference() + "'";
                    }
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
                //.append(",");
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
            //      scoper.exit();

        }
    }

    private void processPolyfillFile(File file, Class<? extends INgApp<?>> appClass) throws IOException
    {
        currentAppFile.set(file);
        File polyfillFile = AppUtils.getAppPolyfillsPath(appClass, true);// new File(srcDirectory.getCanonicalPath() + "/polyfills.ts");
        StringBuilder polyfills = new StringBuilder();
        for (NgPolyfill globalAnnotation : IGuiceContext.get(AnnotationHelper.class)
                                                        .getGlobalAnnotations(NgPolyfill.class))
        {
            String newString = globalAnnotation.value();
            polyfills.append("import \"" + newString + "\";\n");
        }
        FileUtils.writeStringToFile(polyfillFile, polyfills.toString(), UTF_8, false);
    }

    private void processTypeScriptConfigFiles(File file, Class<? extends INgApp<?>> appClass) throws IOException
    {
        currentAppFile.set(file);
        File tsConfigFile = AppUtils.getAppTsConfigAppPath(appClass, true);// new File( AppUtils.getFileReferenceAppFile(appClass,"/tsconfig.app.json"));
        String tsConfigTemplate = IOUtils.toString(Objects.requireNonNull(ResourceLocator.class.getResourceAsStream("tsconfig.app.json")), UTF_8);
        FileUtils.writeStringToFile(tsConfigFile, tsConfigTemplate, UTF_8, false);

        File tsConfigFileAbs = AppUtils.getAppTsConfigPath(appClass, true); //new File(AppUtils.getFileReferenceAppFile(appClass,"/tsconfig.json"));
        String tsConfigTemplateAbs = IOUtils.toString(Objects.requireNonNull(ResourceLocator.class.getResourceAsStream("tsconfig.json")), UTF_8);
        FileUtils.writeStringToFile(tsConfigFileAbs, tsConfigTemplateAbs, UTF_8, false);


        File gitIgnoreFile = AppUtils.getGitIgnorePath(appClass, true); //new File(AppUtils.getFileReferenceAppFile(appClass,"/tsconfig.json"));
        String gitIgnoreFileAbs = IOUtils.toString(Objects.requireNonNull(ResourceLocator.class.getResourceAsStream(".gitignore")), UTF_8);
        FileUtils.writeStringToFile(gitIgnoreFile, gitIgnoreFileAbs, UTF_8, false);

        File packageLockFile = new File(AppUtils.getAppPath(appClass)
                                                .getCanonicalPath() + "/package-lock.json");
        if (packageLockFile.exists() && packageLockFile.isFile())
        {
            packageLockFile.delete();
        }
    }

    private boolean processPackageJsonFile(File file, Class<? extends INgApp<?>> appClass, String packageTemplate, ObjectMapper om, Map<String, String> dependencies, Map<String, String> devDependencies, Map<String, String> overrideDependencies, File packageJsonFile) throws IOException
    {
        currentAppFile.set(file);
        //   CallScoper callScoper = IGuiceContext.get(CallScoper.class);
        //   callScoper.enter();
        try
        {
            String appName = AppUtils.getAppName(appClass);
            packageTemplate = packageTemplate.replace("/*appName*/", appName);
            packageTemplate = packageTemplate.replace("/*dependencies*/", om.writerWithDefaultPrettyPrinter()
                                                                            .writeValueAsString(dependencies));
            packageTemplate = packageTemplate.replace("/*devDependencies*/", om.writerWithDefaultPrettyPrinter()
                                                                               .writeValueAsString(devDependencies));
            packageTemplate = packageTemplate.replace("/*overrideDependencies*/", om.writerWithDefaultPrettyPrinter()
                                                                                    .writeValueAsString(overrideDependencies));

            FileUtils.writeStringToFile(packageJsonFile, packageTemplate, UTF_8, false);
        }
        finally
        {
            //      callScoper.exit();
        }
        return true;
    }

    private boolean processStandaloneComponent(File currentApp, NGApplication<?> application, ClassInfo aClass, CallScoper callScoper, INgApp<?> app, Class<? extends INgApp<?>> appClass, File finalSrcDirectory)
    {
        currentAppFile.set(currentApp);
        try
        {
            //callScoper.enter();
            //var scopeProperties = IGuiceContext.get(CallScopeProperties.class);
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
                    if (!classFile.getCanonicalPath()
                                  .replace('\\', '/')
                                  .contains("src/app/"))
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
            //   callScoper.exit();
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

        Set<TypescriptIndexPageConfigurator> indexPageConfigurators = IGuiceContext.loaderToSet(ServiceLoader.load(TypescriptIndexPageConfigurator.class));
        indexPageConfigurators.forEach(a -> a.configure(p));

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

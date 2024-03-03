package com.jwebmp.core.base.angular.services.compiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.Page;
import com.jwebmp.core.base.ComponentHierarchyBase;
import com.jwebmp.core.base.angular.client.AppUtils;
import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.annotations.angularconfig.NgAsset;
import com.jwebmp.core.base.angular.client.annotations.angularconfig.NgPolyfill;
import com.jwebmp.core.base.angular.client.annotations.angularconfig.NgScript;
import com.jwebmp.core.base.angular.client.annotations.angularconfig.NgStyleSheet;
import com.jwebmp.core.base.angular.client.annotations.typescript.TsDependencies;
import com.jwebmp.core.base.angular.client.annotations.typescript.TsDependency;
import com.jwebmp.core.base.angular.client.annotations.typescript.TsDevDependencies;
import com.jwebmp.core.base.angular.client.annotations.typescript.TsDevDependency;
import com.jwebmp.core.base.angular.client.services.AnnotationHelper;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.modules.services.base.AngularAppBootModule;
import com.jwebmp.core.base.angular.modules.services.base.EnvironmentModule;
import com.jwebmp.core.base.angular.services.RenderedAssets;
import com.jwebmp.core.base.angular.typescript.JWebMP.ResourceLocator;
import com.jwebmp.core.base.html.Body;
import com.jwebmp.core.base.html.DivSimple;
import com.jwebmp.core.base.servlets.enumarations.DevelopmentEnvironments;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.DefaultObjectMapper;
import static com.jwebmp.core.base.angular.client.AppUtils.getFile;
import static com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils.getTsFilename;
import static com.jwebmp.core.base.angular.client.services.interfaces.IComponent.currentAppFile;
import static com.jwebmp.core.base.angular.client.services.interfaces.IComponent.getClassDirectory;
import static com.jwebmp.core.base.angular.implementations.AngularTSPostStartup.buildApp;
import static java.nio.charset.StandardCharsets.UTF_8;

@Log
public class JWebMPTypeScriptCompiler
{
    private static JWebMPTypeScriptCompiler instance;


    private final Set<String> assetStringBuilder = new LinkedHashSet<>();
    private final Set<String> stylesGlobal = new LinkedHashSet<>();
    private final Set<String> scripts = new LinkedHashSet<>();

    private NgApp ngApp;
    private INgApp<?> app;

    public static ThreadLocal<File> getCurrentAppFile()
    {
        return IComponent.getCurrentAppFile();
    }


    public JWebMPTypeScriptCompiler(INgApp<?> app)
    {
        instance = this;
        this.app = app;
        this.ngApp = app.getClass()
                        .getAnnotation(NgApp.class);
        File appPath = AppUtils.getAppPath((Class<? extends INgApp<?>>) app.getClass());
        currentAppFile.set(appPath);
        log.info("Application [" + ngApp.value() + "] is compiling to " + appPath.getPath() + ". Change with env property \"jwebmp\"");
    }

    public static Set<INgApp<?>> getAllApps()
    {
        Set<INgApp<?>> out = new LinkedHashSet<>();
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
                System.out.println("Generating Angular Application - (" + getTsFilename(clazz) + ") in folder " + getClassDirectory(classInfo.loadClass()));
                out.add(clazz);
            }
            catch (ClassCastException e)
            {
                System.out.println("Cannot render app - " + classInfo.getSimpleName() + " / Annotated @NgApp does not implement INgApp");
            }
        }
        return out;
    }

    public StringBuilder renderDataTypeTS(NgApp ngApp, AngularAppBootModule appBootModule, File srcDirectory, INgDataType<?> component, Class<?> requestingClass) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append(component.renderClassTs());
        return sb;
    }

    public StringBuilder renderServiceProviderTS(NgApp ngApp, AngularAppBootModule appBootModule, File srcDirectory, INgServiceProvider<?> component, Class<?> requestingClass) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append(component.renderClassTs());
        return sb;
    }

    public StringBuilder renderProviderTS(NgApp ngApp, AngularAppBootModule appBootModule, File srcDirectory, INgProvider<?> component, Class<?> requestingClass) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append(component.renderClassTs());
        return sb;
    }


    public StringBuilder renderServiceTS(NgApp ngApp, AngularAppBootModule appBootModule, File srcDirectory, INgDataService<?> component, Class<?> requestingClass) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append(component.renderClassTs());
        return sb;
    }

    public StringBuilder renderDirectiveTS(NgApp ngApp, AngularAppBootModule appBootModule, File srcDirectory, INgDirective<?> component, Class<?> requestingClass) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append(component.renderClassTs());
        return sb;
    }

    public StringBuilder renderComponentTS(NgApp ngApp, AngularAppBootModule appBootModule, File srcDirectory, INgComponent<?> component, Class<?> requestingClass) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append(component.renderClassTs());
        return sb;
    }
	
	/*public String getFileReference(String baseDirectory, Class<?> clazz, String... extension)
	{
		String classLocationDirectory = getClassLocationDirectory(clazz);
		classLocationDirectory = classLocationDirectory.replaceAll("\\\\", "/");
		String baseLocation = baseDirectory;
		baseLocation.replaceAll("\\\\", "/");
		baseLocation += "/src/app/";
		classLocationDirectory = baseLocation + classLocationDirectory + getTsFilename(clazz) + (extension.length > 0 ? extension[0] : "");
		
		return classLocationDirectory;
	}*/
	
	/*public File getFile(Class<?> clazz, String... extension)
	{
		//	ITSComponent.getFile(appBaseDirectory.getPath(), clazz, extension);
		String baseDir = getFileReference(appBaseDirectory.getPath(), clazz, extension);
		File file = new File(baseDir);
		return file;
	}*/
	
	/*public File getFile(String filename, String... extension) throws IOException
	{
		String baseDir = appBaseDirectory.getCanonicalPath()
		                                 .replaceAll("\\\\", "/")
		                 + "/src/app/"
				                 .replaceAll("\\\\", "/");
		return new File(FilenameUtils.concat(baseDir, filename)
		                + (extension.length > 0 ? extension[0] : ""));
	}*/

    public StringBuilder renderModuleTS(NgApp ngApp, AngularAppBootModule appBootModule, File srcDirectory, INgModule<?> component, Class<?> requestingClass) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        if (component instanceof AngularAppBootModule)
        {
            component = appBootModule;
        }
        component.setApp(app);
        sb.append(component.renderClassTs());
        return sb;
    }

    public StringBuilder renderAppTS(Class<? extends INgApp<?>> appClass) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        ngApp = app.getClass()
                   .getAnnotation(NgApp.class);
        AppUtils.getAppPath(appClass);

        INgApp<?> app = IGuiceContext.get(appClass);

        ScanResult scan = IGuiceContext.instance()
                                       .getScanResult();

        AngularAppBootModule appBootModule = new AngularAppBootModule();
        appBootModule.setBootModule(ngApp.bootComponent());

        IGuiceContext.get(EnvironmentModule.class)
                     .getEnvironmentOptions()
                     .setAppClass(appClass.getCanonicalName());

        IGuiceContext.get(EnvironmentModule.class)
                     .getEnvironmentOptions()
                     .setProduction(app.getRunningEnvironment()
                                       .equals(DevelopmentEnvironments.Production));

        File packageJsonFile = AppUtils.getAppPackageJsonPath(appClass, true);// new File(appBaseDirectory.getCanonicalPath() + "/package.json");

        String packageTemplate = IOUtils.toString(Objects.requireNonNull(ResourceLocator.class.getResourceAsStream("package.json")), UTF_8);

        Map<String, String> dependencies = new HashMap<>();
        Map<String, String> devDependencies = new HashMap<>();
        Map<String, String> overrideDependencies = new HashMap<>();

        scan
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
                        }
                );
        scan
                .getClassesWithAnnotation(TsDevDependency.class)
                .stream()
                .forEach(a -> {
                            TsDevDependency annotation = a.loadClass()
                                                          .getAnnotation(TsDevDependency.class);
                            if (annotation != null)
                            {
                                devDependencies.put(annotation.value(), annotation.version());
                            }
                        }
                );
        scan
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
                        }
                );
        scan
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
                        }
                );


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

        System.out.println("Registering Assets...");
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
            assetStringBuilder.add("src/assets/" + asset);
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
            stylesGlobal.add("src/assets/" + stylesheet);
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
            scripts.add("src/assets/" + stylesheet);
        }

        String angularTemplate = IOUtils.toString(Objects.requireNonNull(ResourceLocator.class.getResourceAsStream("angular.json")), UTF_8);
        angularTemplate = angularTemplate.replace("/*BuildAssets*/", om.writerWithDefaultPrettyPrinter()
                                                                       .writeValueAsString(assetStringBuilder));
        angularTemplate = angularTemplate.replace("/*BuildStylesSCSS*/", om.writerWithDefaultPrettyPrinter()
                                                                           .writeValueAsString(stylesGlobal));
        angularTemplate = angularTemplate.replace("/*BuildScripts*/", om.writerWithDefaultPrettyPrinter()
                                                                        .writeValueAsString(scripts));
        angularTemplate = angularTemplate.replace("/*MainTSFile*/", om.writerWithDefaultPrettyPrinter()
                                                                      .writeValueAsString("src/main.ts")
        );

        File angularFile = AppUtils.getAngularJsonPath(appClass, true);// new File(appBaseDirectory.getCanonicalPath() + "/angular.json");
        FileUtils.writeStringToFile(angularFile, angularTemplate, UTF_8, false);
        sb.append(app.renderImports());

        sb.append("if(EnvironmentModule.production) {\n" + " enableProdMode();\n" + "}\n" + "" + "platformBrowserDynamic().bootstrapModule(")
          .append(appBootModule.getClass()
                               .getSimpleName())
          .append(")\n")
          .append(".catch(err => console.error(err));\n");


        System.out.println("Writing out angular main.ts file - " + AppUtils.getAppMainTSPath(appClass, false));
        try
        {
            FileUtils.writeStringToFile(AppUtils.getAppMainTSPath(appClass, true), sb.toString(), UTF_8, false);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        File mainIndexHtmlTsFile = AppUtils.getIndexHtmlPath(appClass, true);//new File(srcDirectory.getCanonicalPath() + "/" + "index.html");
        System.out.println("Writing out index.html - " + mainIndexHtmlTsFile.getCanonicalPath());
        try
        {
            FileUtils.writeStringToFile(mainIndexHtmlTsFile, renderBootIndexHtml(app, appBootModule).toString(), UTF_8, false);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        System.out.println("Writing out src/assets/ resources...");
        for (Resource resource : scan.getResourcesMatchingWildcard("src/assets/**"))
        {
            AppUtils.saveAsset(appClass, resource.getURL()
                                                 .openStream(), resource.getPath());
        }

        log.config("Loading resources from assets directory");
        for (Resource allResource : scan.getResourcesMatchingWildcard("app/**"))
        {
            String assetLocation = allResource.getPathRelativeToClasspathElement();
            InputStream fileStream = allResource.getURL()
                                                .openStream();
            AppUtils.saveAppResourceFile(appClass, fileStream, assetLocation);
        }


        File finalSrcDirectory = AppUtils.getAppSrcPath(appClass);

        scan
                .getClassesWithAnnotation(NgModule.class)
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
                        try
                        {
                            FileUtils.forceMkdirParent(classFile);
                            INgModule<?> modd = (INgModule<?>) IGuiceContext.get(aClass);
                            modd.setApp(app);
                            FileUtils.write(classFile, renderModuleTS(ngApp, appBootModule, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });

        scan
                .getClassesWithAnnotation(NgComponent.class)
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
                        try
                        {
                            FileUtils.forceMkdirParent(classFile);
                            INgComponent<?> modd = (INgComponent<?>) IGuiceContext.get(aClass);
                            ComponentHierarchyBase chb2 = (ComponentHierarchyBase) modd;
                            FileUtils.write(classFile, renderComponentTS(ngApp, appBootModule, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });

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

                    try
                    {
                        FileUtils.forceMkdirParent(classFile);
                        INgDirective<?> modd = (INgDirective<?>) IGuiceContext.get(aClass);
                        FileUtils.write(classFile, renderDirectiveTS(ngApp, appBootModule, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
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
                    try
                    {
                        FileUtils.forceMkdirParent(classFile);
                        INgDataService modd = (INgDataService) IGuiceContext.get(aClass);
                        FileUtils.write(classFile, renderServiceTS(ngApp, appBootModule, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
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
                    try
                    {
                        FileUtils.forceMkdirParent(classFile);
                        INgProvider<?> modd = (INgProvider<?>) IGuiceContext.get(aClass);
                        FileUtils.write(classFile, renderProviderTS(ngApp, appBootModule, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
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
                    try
                    {
                        FileUtils.forceMkdirParent(classFile);
                        INgDataType modd = (INgDataType) IGuiceContext.get(aClass);
                        FileUtils.write(classFile, renderDataTypeTS(ngApp, appBootModule, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
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
                    try
                    {
                        FileUtils.forceMkdirParent(classFile);
                        INgServiceProvider<?> modd = (INgServiceProvider<?>) IGuiceContext.get(aClass);
                        FileUtils.write(classFile, renderServiceProviderTS(ngApp, appBootModule, finalSrcDirectory, modd, modd.getClass()), UTF_8, false);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            });


        if (buildApp)
        {
            System.out.println("Installing node-modules...");
            installDependencies(AppUtils.getAppPath(appClass));
        }
        if (buildApp)
        {
            System.out.println("Building Angular Client App...");
            installAngular(AppUtils.getAppPath(appClass));
        }
        //System.out.println("Starting Local Angular Client...");
        //serveAngular(appBaseDirectory);
        return sb;
    }

    public StringBuilder renderBootIndexHtml(INgApp<?> app, AngularAppBootModule appBootModule)
    {
        Page<?> p = (Page) app;
        StringBuilder sb = new StringBuilder();
        Body body = p.getBody();
        List<ComponentHierarchyBase> comps = new ArrayList<>(body.getChildren());

        body.getChildren()
            .clear();
        List<NgComponent> annotations = IGuiceContext.get(AnnotationHelper.class)
                                                     .getAnnotationFromClass(appBootModule.getBootModule(), NgComponent.class);
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
                processBuilder
                        .environment()
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
                processBuilder
                        .environment()
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
                                                                                                                                                          .isProduction() ? "-prod" : "")
                        );
                processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
                processBuilder
                        .environment()
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
                                                                                                         .isProduction() ? "-prod" : "")
                );
                processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
                processBuilder
                        .environment()
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

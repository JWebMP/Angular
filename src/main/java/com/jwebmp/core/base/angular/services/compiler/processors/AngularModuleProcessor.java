package com.jwebmp.core.base.angular.services.compiler.processors;

import com.guicedee.client.scopes.CallScoper;
import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.AppUtils;
import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.services.NGApplication;
import com.jwebmp.core.base.angular.services.compiler.files.TypeScriptFileManager;
import com.jwebmp.core.base.angular.services.compiler.generators.TypeScriptCodeGenerator;
import com.jwebmp.core.base.html.DivSimple;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Responsible for processing Angular modules and components
 */
@Log4j2
public class AngularModuleProcessor
{
    private final INgApp<?> app;
    private final TypeScriptCodeGenerator codeGenerator;
    private final TypeScriptFileManager fileManager;
    private final ComponentProcessor componentProcessor;

    /**
     * Constructor
     *
     * @param app                The Angular application
     * @param codeGenerator      The TypeScript code generator
     * @param fileManager        The TypeScript file manager
     * @param componentProcessor The component processor
     */
    public AngularModuleProcessor(INgApp<?> app, TypeScriptCodeGenerator codeGenerator,
                                  TypeScriptFileManager fileManager, ComponentProcessor componentProcessor)
    {
        this.app = app;
        this.codeGenerator = codeGenerator;
        this.fileManager = fileManager;
        this.componentProcessor = componentProcessor;
    }

    /**
     * Processes Angular modules
     *
     * @param currentApp   The current application directory
     * @param scan         The scan result
     * @param appClass     The Angular application class
     * @param srcDirectory The source directory
     */
    public void processAngularModules(File currentApp, ScanResult scan, Class<? extends INgApp<?>> appClass, File srcDirectory)
    {
        scan.getClassesWithAnnotation(NgModule.class)
            .stream()
            .forEach(a -> {
                LogManager.getLogger("TypescriptCompiler")
                          .debug("Rendering NgModule [{}]", a.getSimpleName());
                processNgModuleFiles(currentApp, a, scan, appClass, srcDirectory);
            });
    }

    /**
     * Processes standalone components
     *
     * @param currentApp   The current application directory
     * @param scan         The scan result
     * @param appClass     The Angular application class
     * @param srcDirectory The source directory
     */
    public void processStandaloneComponents(File currentApp, ScanResult scan, Class<? extends INgApp<?>> appClass, File srcDirectory)
    {
        if (app instanceof NGApplication<?> application)
        {
            var standaloneComponents = scan.getClassesWithAnnotation(NgComponent.class)
                                           .stream()
                                           .filter(a -> !a.isAbstract() && !a.isInterface())
                                           .filter(a -> a.loadClass()
                                                         .getAnnotation(NgComponent.class)
                                                         .standalone());

            standaloneComponents.distinct()
                                .forEach(aClass -> {
                                    LogManager.getLogger("TypescriptCompiler")
                                              .debug("Rendering Standalone Component [{}]", aClass.getSimpleName());
                                    processStandaloneComponent(currentApp, application, aClass, appClass, srcDirectory);
                                });
        }
    }

    /**
     * Processes Angular module files
     *
     * @param currentApp   The current application directory
     * @param a            The class info
     * @param scan         The scan result
     * @param appClass     The Angular application class
     * @param srcDirectory The source directory
     */
    public void processNgModuleFiles(File currentApp, ClassInfo a, ScanResult scan, Class<? extends INgApp<?>> appClass, File srcDirectory)
    {
        CallScoper scoper = IGuiceContext.get(CallScoper.class);
        scoper.enter();
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
                if (INgModule.class.isAssignableFrom(aClass))
                {
                    INgModule<?> module = (INgModule<?>) IGuiceContext.get(aClass);
                    module.setApp(app);
                    File file = fileManager.getComponentFilePath(module);
                    if (file != null)
                    {
                        fileManager.writeComponentToFile(module);
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error("Unable to render module - " + a.getSimpleName(), e);
        }
        finally
        {
            scoper.exit();
        }
    }

    /**
     * Processes a standalone component
     *
     * @param currentApp   The current application directory
     * @param application  The NGApplication
     * @param aClass       The class info
     * @param appClass     The Angular application class
     * @param srcDirectory The source directory
     * @return Whether the component was processed successfully
     */
    protected boolean processStandaloneComponent(File currentApp, NGApplication<?> application, ClassInfo aClass,
                                                 Class<? extends INgApp<?>> appClass, File srcDirectory)
    {
        IComponent.getCurrentAppFile()
                  .set(currentApp);
        try
        {
            IComponent.app.set(application);
            File appPath = AppUtils.getAppPath((Class<? extends INgApp<?>>) application.getClass());
            IComponent.getCurrentAppFile()
                      .set(appPath);

            Class<?> clazz = aClass.loadClass();
            if (clazz.getCanonicalName()
                     .contains("HomePage"))
            {
                System.out.println("Here");
            }
            if (clazz.isAnnotationPresent(NgComponent.class) && INgComponent.class.isAssignableFrom(clazz))
            {
                // Get the component instance
                Object componentObj = IGuiceContext.get(clazz);

                // Cast to IComponentHierarchyBase for adding to dummy parent
                if (componentObj instanceof IComponentHierarchyBase<?, ?> hierarchyBase)
                {
                    // Add to dummy parent and call toString to trigger initialization
                    DivSimple<?> dummyAdd = new DivSimple<>();
                    dummyAdd.add(hierarchyBase);
                    dummyAdd.toString(true);
                }

                // Cast to INgComponent for TypeScript generation
                INgComponent<?> component = (INgComponent<?>) componentObj;

                // Generate TypeScript
                String typeScript = codeGenerator.renderComponentTS(component)
                                                 .toString();

                // Get file paths
                File tsFile = fileManager.getComponentFilePath(component);
                if (tsFile == null || !tsFile.getCanonicalPath()
                                             .replace('\\', '/')
                                             .contains("src/app/"))
                {
                    log.error("Unable to write out component file - {}",
                            tsFile != null ? tsFile.getCanonicalPath() : "null path");
                    return false;
                }

                // Generate HTML and CSS
                // Get the HTML content by calling toString on the component
                IComponentHierarchyBase<?, ?> hierarchyBase = (IComponentHierarchyBase<?, ?>) componentObj;
                String html = hierarchyBase.toString(0);

                // Generate CSS using the style base
                StringBuilder cssString = hierarchyBase.cast()
                                                       .asStyleBase()
                                                       .renderCss(1);

                // Get HTML and CSS file paths
                String tsPath = tsFile.getPath();
                File htmlFile = new File(tsPath.substring(0, tsPath.lastIndexOf(".")) + ".html");
                File cssFile = new File(tsPath.substring(0, tsPath.lastIndexOf(".")) + ".scss");

                // Write files
                try
                {
                    FileUtils.forceMkdirParent(tsFile);
                    fileManager.writeComponentToFile(component, true);
                    FileUtils.writeStringToFile(htmlFile, html, UTF_8);
                    FileUtils.writeStringToFile(cssFile, cssString.toString(), UTF_8);
                    return true;
                }
                catch (Exception e)
                {
                    log.error("Unable to write component files", e);
                    return false;
                }
            }
        }
        catch (Exception e)
        {
            log.error("Unable to render standalone component - " + aClass.getSimpleName(), e);
        }
        return false;
    }

    /**
     * Processes service provider files
     *
     * @param currentApp   The current application directory
     * @param a            The class info
     * @param scan         The scan result
     * @param appClass     The Angular application class
     * @param srcDirectory The source directory
     */
    public void processNgServiceProviderFiles(File currentApp, ClassInfo a, ScanResult scan, Class<? extends INgApp<?>> appClass, File srcDirectory)
    {
        try
        {
            Set<Class<?>> classes = new HashSet<>();
            Class<?> aClass = a.loadClass();

            if (a.isInterface() || a.isAbstract())
            {
                for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(aClass) : scan.getClassesImplementing(aClass))
                {
                    if (!subclass.isAbstract() && !subclass.isInterface())
                    {
                        classes.add(subclass.loadClass());
                    }
                }
            }
            else
            {
                classes.add(aClass);
            }

            for (Class<?> clazz : classes)
            {
                if (INgServiceProvider.class.isAssignableFrom(clazz))
                {
                    INgServiceProvider<?> component = (INgServiceProvider<?>) IGuiceContext.get(clazz);
                    String typeScript = codeGenerator.renderServiceProviderTS(component)
                                                     .toString();
                    File file = fileManager.getComponentFilePath(component);
                    if (file != null)
                    {
                        fileManager.writeComponentToFile(component);
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error("Unable to render service provider - " + a.getSimpleName(), e);
        }
    }

    /**
     * Processes data type files
     *
     * @param currentApp   The current application directory
     * @param a            The class info
     * @param scan         The scan result
     * @param appClass     The Angular application class
     * @param srcDirectory The source directory
     */
    public void processNgDataTypeFiles(File currentApp, ClassInfo a, ScanResult scan, Class<? extends INgApp<?>> appClass, File srcDirectory)
    {
        try
        {
            Set<Class<?>> classes = new HashSet<>();
            Class<?> aClass = a.loadClass();

            if (a.isInterface() || a.isAbstract())
            {
                for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(aClass) : scan.getClassesImplementing(aClass))
                {
                    if (!subclass.isAbstract() && !subclass.isInterface())
                    {
                        classes.add(subclass.loadClass());
                    }
                }
            }
            else
            {
                classes.add(aClass);
            }

            for (Class<?> clazz : classes)
            {
                if (INgDataType.class.isAssignableFrom(clazz))
                {
                    INgDataType<?> component = (INgDataType<?>) IGuiceContext.get(clazz);
                    String typeScript = codeGenerator.renderDataTypeTS(component)
                                                     .toString();
                    File file = fileManager.getComponentFilePath(component);
                    if (file != null)
                    {
                        fileManager.writeComponentToFile(component);
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error("Unable to render data type - " + a.getSimpleName(), e);
        }
    }

    /**
     * Processes provider files
     *
     * @param currentApp   The current application directory
     * @param a            The class info
     * @param scan         The scan result
     * @param appClass     The Angular application class
     * @param srcDirectory The source directory
     */
    public void processNgProviderFiles(File currentApp, ClassInfo a, ScanResult scan, Class<? extends INgApp<?>> appClass, File srcDirectory)
    {
        try
        {
            Set<Class<?>> classes = new HashSet<>();
            Class<?> aClass = a.loadClass();

            if (a.isInterface() || a.isAbstract())
            {
                for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(aClass) : scan.getClassesImplementing(aClass))
                {
                    if (!subclass.isAbstract() && !subclass.isInterface())
                    {
                        classes.add(subclass.loadClass());
                    }
                }
            }
            else
            {
                classes.add(aClass);
            }

            for (Class<?> clazz : classes)
            {
                if (INgProvider.class.isAssignableFrom(clazz))
                {
                    INgProvider<?> component = (INgProvider<?>) IGuiceContext.get(clazz);
                    String typeScript = codeGenerator.renderProviderTS(component)
                                                     .toString();
                    File file = fileManager.getComponentFilePath(component);
                    if (file != null)
                    {
                        fileManager.writeComponentToFile(component);
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error("Unable to render provider - " + a.getSimpleName(), e);
        }
    }

    /**
     * Processes data service files
     *
     * @param currentApp   The current application directory
     * @param a            The class info
     * @param scan         The scan result
     * @param appClass     The Angular application class
     * @param srcDirectory The source directory
     */
    public void processNgDataServiceFiles(File currentApp, ClassInfo a, ScanResult scan, Class<? extends INgApp<?>> appClass, File srcDirectory)
    {
        try
        {
            Set<Class<?>> classes = new HashSet<>();
            Class<?> aClass = a.loadClass();

            if (a.isInterface() || a.isAbstract())
            {
                for (ClassInfo subclass : !a.isInterface() ? scan.getSubclasses(aClass) : scan.getClassesImplementing(aClass))
                {
                    if (!subclass.isAbstract() && !subclass.isInterface())
                    {
                        classes.add(subclass.loadClass());
                    }
                }
            }
            else
            {
                classes.add(aClass);
            }

            for (Class<?> clazz : classes)
            {
                if (INgDataService.class.isAssignableFrom(clazz))
                {
                    INgDataService<?> component = (INgDataService<?>) IGuiceContext.get(clazz);
                    String typeScript = codeGenerator.renderServiceTS(component)
                                                     .toString();
                    File file = fileManager.getComponentFilePath(component);
                    if (file != null)
                    {
                        fileManager.writeComponentToFile(component);
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error("Unable to render data service - " + a.getSimpleName(), e);
        }
    }

    /**
     * Processes directive files
     *
     * @param currentApp   The current application directory
     * @param a            The class info
     * @param scan         The scan result
     * @param appClass     The Angular application class
     * @param srcDirectory The source directory
     */
    public void processNgDirectiveFiles(File currentApp, ClassInfo a, ScanResult scan, Class<? extends INgApp<?>> appClass, File srcDirectory)
    {
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
                if (INgDirective.class.isAssignableFrom(aClass))
                {
                    INgDirective<?> component = (INgDirective<?>) IGuiceContext.get(aClass);
                    File file = fileManager.getComponentFilePath(component);
                    if (file != null)
                    {
                        fileManager.writeComponentToFile(component);
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error("Unable to render directive - " + a.getSimpleName(), e);
        }
    }

    /**
     * Processes all directives
     *
     * @param currentApp   The current application directory
     * @param scan         The scan result
     * @param appClass     The Angular application class
     * @param srcDirectory The source directory
     */
    public void processDirectives(File currentApp, ScanResult scan, Class<? extends INgApp<?>> appClass, File srcDirectory)
    {
        scan.getClassesWithAnnotation(NgDirective.class)
            .stream()
            .forEach(a -> {
                LogManager.getLogger("TypescriptCompiler")
                          .debug("Rendering NgDirective [{}]", a.getSimpleName());
                processNgDirectiveFiles(currentApp, a, scan, appClass, srcDirectory);
            });
    }

    /**
     * Processes all data services
     *
     * @param currentApp   The current application directory
     * @param scan         The scan result
     * @param appClass     The Angular application class
     * @param srcDirectory The source directory
     */
    public void processDataServices(File currentApp, ScanResult scan, Class<? extends INgApp<?>> appClass, File srcDirectory)
    {
        scan.getClassesWithAnnotation(NgDataService.class)
            .stream()
            .forEach(a -> {
                LogManager.getLogger("TypescriptCompiler")
                          .debug("Rendering NgDataService [{}]", a.getSimpleName());
                processNgDataServiceFiles(currentApp, a, scan, appClass, srcDirectory);
            });
    }

    /**
     * Processes all providers
     *
     * @param currentApp   The current application directory
     * @param scan         The scan result
     * @param appClass     The Angular application class
     * @param srcDirectory The source directory
     */
    public void processProviders(File currentApp, ScanResult scan, Class<? extends INgApp<?>> appClass, File srcDirectory)
    {
        scan.getClassesWithAnnotation(NgProvider.class)
            .stream()
            .forEach(a -> {
                LogManager.getLogger("TypescriptCompiler")
                          .debug("Rendering NgProvider [{}]", a.getSimpleName());
                processNgProviderFiles(currentApp, a, scan, appClass, srcDirectory);
            });
    }

    /**
     * Processes all data types
     *
     * @param currentApp   The current application directory
     * @param scan         The scan result
     * @param appClass     The Angular application class
     * @param srcDirectory The source directory
     */
    public void processDataTypes(File currentApp, ScanResult scan, Class<? extends INgApp<?>> appClass, File srcDirectory)
    {
        scan.getClassesWithAnnotation(NgDataType.class)
            .stream()
            .forEach(a -> {
                LogManager.getLogger("TypescriptCompiler")
                          .debug("Rendering NgDataType [{}]", a.getSimpleName());
                processNgDataTypeFiles(currentApp, a, scan, appClass, srcDirectory);
            });
    }

    /**
     * Processes all service providers
     *
     * @param currentApp   The current application directory
     * @param scan         The scan result
     * @param appClass     The Angular application class
     * @param srcDirectory The source directory
     */
    public void processServiceProviders(File currentApp, ScanResult scan, Class<? extends INgApp<?>> appClass, File srcDirectory)
    {
        scan.getClassesWithAnnotation(NgServiceProvider.class)
            .stream()
            .forEach(a -> {
                LogManager.getLogger("TypescriptCompiler")
                          .debug("Rendering NgServiceProvider [{}]", a.getSimpleName());
                processNgServiceProviderFiles(currentApp, a, scan, appClass, srcDirectory);
            });
    }

    /**
     * Processes all Angular components
     * This method processes all types of Angular components in one go
     *
     * @param currentApp   The current application directory
     * @param scan         The scan result
     * @param appClass     The Angular application class
     * @param srcDirectory The source directory
     */
    public void processAllComponents(File currentApp, ScanResult scan, Class<? extends INgApp<?>> appClass, File srcDirectory)
    {
        log.info("Processing all Angular components");

        // Process Angular modules
        processAngularModules(currentApp, scan, appClass, srcDirectory);

        // Process standalone components
        processStandaloneComponents(currentApp, scan, appClass, srcDirectory);

        // Process directives
        processDirectives(currentApp, scan, appClass, srcDirectory);

        // Process data services
        processDataServices(currentApp, scan, appClass, srcDirectory);

        // Process providers
        processProviders(currentApp, scan, appClass, srcDirectory);

        // Process data types
        processDataTypes(currentApp, scan, appClass, srcDirectory);

        // Process service providers
        processServiceProviders(currentApp, scan, appClass, srcDirectory);

        log.info("Finished processing all Angular components");
    }
}

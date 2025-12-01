package com.jwebmp.core.base.angular.typescript;

import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.services.compiler.JWebMPTypeScriptCompiler;
import com.jwebmp.core.base.angular.services.compiler.assets.AssetManager;
import com.jwebmp.core.base.angular.services.compiler.files.TypeScriptFileManager;
import com.jwebmp.core.base.angular.services.compiler.generators.TypeScriptCodeGenerator;
import com.jwebmp.core.base.angular.services.compiler.processors.AngularModuleProcessor;
import com.jwebmp.core.base.angular.services.compiler.processors.ComponentProcessor;
import com.jwebmp.core.base.angular.services.compiler.setup.AngularAppSetup;
import com.jwebmp.core.base.angular.services.compiler.validators.TypeScriptCodeValidator;
import io.github.classgraph.ClassInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils.getTsFilename;
import static com.jwebmp.core.base.angular.client.services.interfaces.IComponent.getClassDirectory;

/**
 * Tests for the migrated classes from JWebMPTypeScriptCompiler
 */
public class MigratedClassesTest
{

    /*
     *//**
 * Test the TypeScriptCodeGenerator class
 *//*
    @Test
    public void testTypeScriptCodeGenerator() throws IOException {
        IGuiceContext.instance().inject();

        // Get the first app
        INgApp<?> app = JWebMPTypeScriptCompiler.getAllApps().iterator().next();
        JWebMPTypeScriptCompiler compiler = new JWebMPTypeScriptCompiler(app);
        TypeScriptCodeGenerator codeGenerator = compiler.getCodeGenerator();

        // Test renderAppTS
        System.out.println("Testing renderAppTS...");
        StringBuilder appTs = codeGenerator.renderAppTS(app);
        Assertions.assertNotNull(appTs);
        Assertions.assertTrue(appTs.length() > 0);
        System.out.println("Generated app.ts content:");
        System.out.println(appTs);

        // Test writeAppTS
        System.out.println("Testing writeAppTS...");
        codeGenerator.writeAppTS(app);
        System.out.println("Generating @NgApp (" + getTsFilename(app.getClass()) + ") " +
                "in folder " + getClassDirectory(app.getClass()));

        // Find a component to test with
        IComponent<?> component = findTestComponent();
        if (component != null) {
            // Test generateTypeScriptForComponent
            System.out.println("Testing generateTypeScriptForComponent...");
            String typeScript = codeGenerator.generateTypeScriptForComponent(component);
            Assertions.assertNotNull(typeScript);
            Assertions.assertFalse(typeScript.isEmpty());
            System.out.println("Generated TypeScript for component " + component.getClass().getSimpleName() + ":");
            System.out.println(typeScript);

            // Test clearTypeScriptCache
            System.out.println("Testing clearTypeScriptCache...");
            codeGenerator.clearTypeScriptCache(component);
            String regeneratedTypeScript = codeGenerator.generateTypeScriptForComponent(component);
            Assertions.assertNotNull(regeneratedTypeScript);
            Assertions.assertFalse(regeneratedTypeScript.isEmpty());
        }
    }

    *//**
 * Test the TypeScriptFileManager class
 *//*
    @Test
    public void testTypeScriptFileManager() throws IOException {
        IGuiceContext.instance().inject();

        // Get the first app
        INgApp<?> app = JWebMPTypeScriptCompiler.getAllApps().iterator().next();
        JWebMPTypeScriptCompiler compiler = new JWebMPTypeScriptCompiler(app);
        TypeScriptFileManager fileManager = compiler.getFileManager();

        // Find a component to test with
        IComponent<?> component = findTestComponent();
        if (component != null) {
            // Test getComponentFilePath
            System.out.println("Testing getComponentFilePath...");
            File filePath = fileManager.getComponentFilePath(component);
            Assertions.assertNotNull(filePath);
            System.out.println("File path for component " + component.getClass().getSimpleName() + ": " + filePath.getAbsolutePath());

            // Test writeComponentToFile
            System.out.println("Testing writeComponentToFile...");
            File writtenFile = fileManager.writeComponentToFile(component);
            Assertions.assertNotNull(writtenFile);
            Assertions.assertTrue(writtenFile.exists());
            System.out.println("Wrote component to file: " + writtenFile.getAbsolutePath());

            // Test clearFilePathCache
            System.out.println("Testing clearFilePathCache...");
            fileManager.clearFilePathCache(component);
            File regeneratedFilePath = fileManager.getComponentFilePath(component);
            Assertions.assertNotNull(regeneratedFilePath);
            Assertions.assertEquals(filePath, regeneratedFilePath);
        }
    }

    *//**
 * Test the ComponentProcessor class
 *//*
    @Test
    public void testComponentProcessor() throws IOException {
        IGuiceContext.instance().inject();

        // Get the first app
        INgApp<?> app = JWebMPTypeScriptCompiler.getAllApps().iterator().next();
        JWebMPTypeScriptCompiler compiler = new JWebMPTypeScriptCompiler(app);
        ComponentProcessor componentProcessor = compiler.getComponentProcessor();

        // Test processComponentsOfType
        System.out.println("Testing processComponentsOfType...");
        Map<IComponent<?>, File> componentFiles = componentProcessor.processComponentsOfType(INgComponent.class, false);
        Assertions.assertNotNull(componentFiles);
        System.out.println("Processed " + componentFiles.size() + " components");

        // Test with multiple component types
        List<IComponent<?>> components = new ArrayList<>();

        // Add a component of each type if available
        IComponent<?> component = findComponentOfType(INgComponent.class);
        if (component != null) components.add(component);

        component = findComponentOfType(INgDirective.class);
        if (component != null) components.add(component);

        component = findComponentOfType(INgDataService.class);
        if (component != null) components.add(component);

        component = findComponentOfType(INgDataType.class);
        if (component != null) components.add(component);

        component = findComponentOfType(INgModule.class);
        if (component != null) components.add(component);

        // Test processComponents
        if (!components.isEmpty()) {
            System.out.println("Testing processComponents...");
            Map<IComponent<?>, File> mixedComponentFiles = componentProcessor.processComponents(components, false);
            Assertions.assertNotNull(mixedComponentFiles);
            Assertions.assertEquals(components.size(), mixedComponentFiles.size());
            System.out.println("Processed " + mixedComponentFiles.size() + " mixed components");
        }
    }

    *//**
 * Test the AngularModuleProcessor class
 *//*
    @Test
    public void testAngularModuleProcessor() throws IOException {
        IGuiceContext.instance().inject();

        // Get the first app
        INgApp<?> app = JWebMPTypeScriptCompiler.getAllApps().iterator().next();
        JWebMPTypeScriptCompiler compiler = new JWebMPTypeScriptCompiler(app);
        AngularModuleProcessor moduleProcessor = compiler.getModuleProcessor();

        // Get the app path
        File appPath = IComponent.getCurrentAppFile().get();
        File srcDirectory = new File(appPath, "src");

        // Test processAngularModules
        System.out.println("Testing processAngularModules...");
        moduleProcessor.processAngularModules(appPath, IGuiceContext.instance().getScanResult(), 
                (Class<? extends INgApp<?>>) app.getClass(), srcDirectory);

        // Test processStandaloneComponents
        System.out.println("Testing processStandaloneComponents...");
        moduleProcessor.processStandaloneComponents(appPath, IGuiceContext.instance().getScanResult(), 
                (Class<? extends INgApp<?>>) app.getClass(), srcDirectory);

        // Test processAllComponents
        System.out.println("Testing processAllComponents...");
        moduleProcessor.processAllComponents(appPath, IGuiceContext.instance().getScanResult(), 
                (Class<? extends INgApp<?>>) app.getClass(), srcDirectory);
    }

    *//**
 * Test the AssetManager class
 *//*
    @Test
    public void testAssetManager() throws IOException {
        IGuiceContext.instance().inject();

        // Get the first app
        INgApp<?> app = JWebMPTypeScriptCompiler.getAllApps().iterator().next();
        JWebMPTypeScriptCompiler compiler = new JWebMPTypeScriptCompiler(app);
        AssetManager assetManager = compiler.getAssetManager();

        // Test processAssets
        System.out.println("Testing processAssets...");
        Map<String, String> namedAssets = assetManager.processAssets();
        Assertions.assertNotNull(namedAssets);
        System.out.println("Processed " + namedAssets.size() + " assets");

        // Test processStylesheets
        System.out.println("Testing processStylesheets...");
        assetManager.processStylesheets();
        Set<String> stylesGlobal = assetManager.getStylesGlobal();
        Assertions.assertNotNull(stylesGlobal);
        System.out.println("Processed " + stylesGlobal.size() + " stylesheets");

        // Test processScripts
        System.out.println("Testing processScripts...");
        assetManager.processScripts();
        Set<String> scripts = assetManager.getScripts();
        Assertions.assertNotNull(scripts);
        System.out.println("Processed " + scripts.size() + " scripts");

        // Test renderAngularApplicationFiles
        System.out.println("Testing renderAngularApplicationFiles...");
        File appPath = IComponent.getCurrentAppFile().get();
        assetManager.renderAngularApplicationFiles(appPath, (Class<? extends INgApp<?>>) app.getClass(), 
                namedAssets, app, IGuiceContext.get(com.guicedee.client.implementations.ObjectBinderKeys.DefaultObjectMapper));
    }

    *//**
 * Test the AngularAppSetup class
 *//*
    @Test
    public void testAngularAppSetup() throws IOException {
        IGuiceContext.instance().inject();

        // Get the first app
        INgApp<?> app = JWebMPTypeScriptCompiler.getAllApps().iterator().next();
        JWebMPTypeScriptCompiler compiler = new JWebMPTypeScriptCompiler(app);
        AngularAppSetup appSetup = compiler.getAppSetup();

        // Test processNpmrcFile
        System.out.println("Testing processNpmrcFile...");
        appSetup.processNpmrcFile((Class<? extends INgApp<?>>) app.getClass());

        // Test processPackageJsonFile
        System.out.println("Testing processPackageJsonFile...");
        appSetup.processPackageJsonFile((Class<? extends INgApp<?>>) app.getClass());

        // Test processTypeScriptConfigFiles
        System.out.println("Testing processTypeScriptConfigFiles...");
        appSetup.processTypeScriptConfigFiles((Class<? extends INgApp<?>>) app.getClass());

        // Test processPolyfillFile
        System.out.println("Testing processPolyfillFile...");
        appSetup.processPolyfillFile((Class<? extends INgApp<?>>) app.getClass());

        // Test processAppConfigFile
        System.out.println("Testing processAppConfigFile...");
        appSetup.processAppConfigFile((Class<? extends INgApp<?>>) app.getClass(), IGuiceContext.instance().getScanResult());

        // Test renderBootIndexHtml
        System.out.println("Testing renderBootIndexHtml...");
        StringBuilder bootIndexHtml = appSetup.renderBootIndexHtml(app);
        Assertions.assertNotNull(bootIndexHtml);
        Assertions.assertTrue(bootIndexHtml.length() > 0);
        System.out.println("Generated boot index HTML:");
        System.out.println(bootIndexHtml);
    }

    *//**
 * Find a component of a specific type
 *//*
    private <T> IComponent<?> findComponentOfType(Class<T> componentType) {
        for (ClassInfo classInfo : IGuiceContext.instance().getScanResult().getAllClasses()) {
            try {
                Class<?> clazz = classInfo.loadClass();
                if (componentType.isAssignableFrom(clazz) && !clazz.isInterface() && !clazz.isAnnotation()) {
                    try {
                        return (IComponent<?>) IGuiceContext.get(clazz);
                    } catch (Exception e) {
                        // Continue to next class
                    }
                }
            } catch (Exception e) {
                // Continue to next class
            }
        }
        return null;
    }

    *//**
 * Find a component to test with
 * This method tries to find a component in the application to use for testing
 *//*
    private IComponent<?> findTestComponent() {
        // Try to find a component
        IComponent<?> component = findComponentOfType(INgComponent.class);
        if (component != null) return component;

        // Try to find a directive
        component = findComponentOfType(INgDirective.class);
        if (component != null) return component;

        // Try to find a service
        component = findComponentOfType(INgDataService.class);
        if (component != null) return component;

        // Try to find a data type
        component = findComponentOfType(INgDataType.class);
        if (component != null) return component;

        // Try to find a module
        component = findComponentOfType(INgModule.class);
        if (component != null) return component;

        return null;
    }*/
}

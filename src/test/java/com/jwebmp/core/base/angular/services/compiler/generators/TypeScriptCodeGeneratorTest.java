package com.jwebmp.core.base.angular.services.compiler.generators;

import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.AppUtils;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import io.github.classgraph.ClassInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

/**
 * Tests for the TypeScriptCodeGenerator class
 */
public class TypeScriptCodeGeneratorTest {
    private TypeScriptCodeGenerator codeGenerator;
    private INgApp<?> testApp;

    @BeforeEach
    public void setup() {
        // Find a test app to use
        for (ClassInfo classInfo : IGuiceContext.instance().getScanResult().getAllClasses()) {
            try {
                Class<?> aClass = classInfo.loadClass();
                if (INgApp.class.isAssignableFrom(aClass) && !aClass.isInterface()) {
                    testApp = (INgApp<?>) IGuiceContext.get(aClass);
                    break;
                }
            } catch (Exception e) {
                // Continue to next class
            }
        }

        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping tests.");
            return;
        }

        // Set up the currentAppFile ThreadLocal with the actual app path
        File appPath = AppUtils.getAppPath((Class<? extends INgApp<?>>) testApp.getClass());
        IComponent.getCurrentAppFile().set(appPath);
        System.out.println("[DEBUG_LOG] Set currentAppFile to: " + appPath.getAbsolutePath());

        // Initialize the code generator
        codeGenerator = new TypeScriptCodeGenerator(testApp);
    }

    @Test
    public void testGenerateTypeScriptForComponent() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing generateTypeScriptForComponent");

        // Find a component to test with
        IComponent<?> component = findTestComponent();
        if (component != null) {
            // Generate TypeScript for the component
            String typeScript = codeGenerator.generateTypeScriptForComponent(component);

            // Verify the TypeScript is not null or empty
            Assertions.assertNotNull(typeScript);
            Assertions.assertFalse(typeScript.isEmpty());

            System.out.println("[DEBUG_LOG] Generated TypeScript for component " + component.getClass().getSimpleName() + ":");
            System.out.println("[DEBUG_LOG] " + typeScript);

            // Test the cache by generating again
            String cachedTypeScript = codeGenerator.generateTypeScriptForComponent(component);
            Assertions.assertEquals(typeScript, cachedTypeScript);

            // Test clearing the cache
            codeGenerator.clearTypeScriptCache(component);
            String regeneratedTypeScript = codeGenerator.generateTypeScriptForComponent(component);
            Assertions.assertNotNull(regeneratedTypeScript);
            Assertions.assertFalse(regeneratedTypeScript.isEmpty());
        } else {
            System.out.println("[DEBUG_LOG] No test component found. Skipping test.");
        }
    }

    @Test
    public void testRenderComponentTS() throws IOException {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing renderComponentTS");

        // Find a component to test with
        INgComponent<?> component = findComponentOfType(INgComponent.class);
        if (component != null) {
            // Render TypeScript for the component
            StringBuilder typeScript = codeGenerator.renderComponentTS(component);

            // Verify the TypeScript is not null or empty
            Assertions.assertNotNull(typeScript);
            Assertions.assertTrue(typeScript.length() > 0);

            System.out.println("[DEBUG_LOG] Rendered TypeScript for component " + component.getClass().getSimpleName() + ":");
            System.out.println("[DEBUG_LOG] " + typeScript);
        } else {
            System.out.println("[DEBUG_LOG] No component found. Skipping test.");
        }
    }

    @Test
    public void testRenderDirectiveTS() throws IOException {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing renderDirectiveTS");

        // Find a directive to test with
        INgDirective<?> directive = findComponentOfType(INgDirective.class);
        if (directive != null) {
            // Render TypeScript for the directive
            StringBuilder typeScript = codeGenerator.renderDirectiveTS(directive);

            // Verify the TypeScript is not null or empty
            Assertions.assertNotNull(typeScript);
            Assertions.assertTrue(typeScript.length() > 0);

            System.out.println("[DEBUG_LOG] Rendered TypeScript for directive " + directive.getClass().getSimpleName() + ":");
            System.out.println("[DEBUG_LOG] " + typeScript);
        } else {
            System.out.println("[DEBUG_LOG] No directive found. Skipping test.");
        }
    }

    @Test
    public void testRenderServiceTS() throws IOException {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing renderServiceTS");

        // Find a service to test with
        INgDataService<?> service = findComponentOfType(INgDataService.class);
        if (service != null) {
            // Render TypeScript for the service
            StringBuilder typeScript = codeGenerator.renderServiceTS(service);

            // Verify the TypeScript is not null or empty
            Assertions.assertNotNull(typeScript);
            Assertions.assertTrue(typeScript.length() > 0);

            System.out.println("[DEBUG_LOG] Rendered TypeScript for service " + service.getClass().getSimpleName() + ":");
            System.out.println("[DEBUG_LOG] " + typeScript);
        } else {
            System.out.println("[DEBUG_LOG] No service found. Skipping test.");
        }
    }

    @Test
    public void testRenderDataTypeTS() throws IOException {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing renderDataTypeTS");

        // Find a data type to test with
        INgDataType<?> dataType = findComponentOfType(INgDataType.class);
        if (dataType != null) {
            // Render TypeScript for the data type
            StringBuilder typeScript = codeGenerator.renderDataTypeTS(dataType);

            // Verify the TypeScript is not null or empty
            Assertions.assertNotNull(typeScript);
            Assertions.assertTrue(typeScript.length() > 0);

            System.out.println("[DEBUG_LOG] Rendered TypeScript for data type " + dataType.getClass().getSimpleName() + ":");
            System.out.println("[DEBUG_LOG] " + typeScript);
        } else {
            System.out.println("[DEBUG_LOG] No data type found. Skipping test.");
        }
    }

    @Test
    public void testRenderModuleTS() throws IOException {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing renderModuleTS");

        // Find a module to test with
        INgModule<?> module = findComponentOfType(INgModule.class);
        if (module != null) {
            // Render TypeScript for the module
            StringBuilder typeScript = codeGenerator.renderModuleTS(module);

            // Verify the TypeScript is not null or empty
            Assertions.assertNotNull(typeScript);
            Assertions.assertTrue(typeScript.length() > 0);

            System.out.println("[DEBUG_LOG] Rendered TypeScript for module " + module.getClass().getSimpleName() + ":");
            System.out.println("[DEBUG_LOG] " + typeScript);
        } else {
            System.out.println("[DEBUG_LOG] No module found. Skipping test.");
        }
    }

    @Test
    public void testRenderProviderTS() throws IOException {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing renderProviderTS");

        // Find a provider to test with
        INgProvider<?> provider = findComponentOfType(INgProvider.class);
        if (provider != null) {
            // Render TypeScript for the provider
            StringBuilder typeScript = codeGenerator.renderProviderTS(provider);

            // Verify the TypeScript is not null or empty
            Assertions.assertNotNull(typeScript);
            Assertions.assertTrue(typeScript.length() > 0);

            System.out.println("[DEBUG_LOG] Rendered TypeScript for provider " + provider.getClass().getSimpleName() + ":");
            System.out.println("[DEBUG_LOG] " + typeScript);
        } else {
            System.out.println("[DEBUG_LOG] No provider found. Skipping test.");
        }
    }

    @Test
    public void testRenderServiceProviderTS() throws IOException {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing renderServiceProviderTS");

        // Find a service provider to test with
        INgServiceProvider<?> serviceProvider = findComponentOfType(INgServiceProvider.class);
        if (serviceProvider != null) {
            // Render TypeScript for the service provider
            StringBuilder typeScript = codeGenerator.renderServiceProviderTS(serviceProvider);

            // Verify the TypeScript is not null or empty
            Assertions.assertNotNull(typeScript);
            Assertions.assertTrue(typeScript.length() > 0);

            System.out.println("[DEBUG_LOG] Rendered TypeScript for service provider " + serviceProvider.getClass().getSimpleName() + ":");
            System.out.println("[DEBUG_LOG] " + typeScript);
        } else {
            System.out.println("[DEBUG_LOG] No service provider found. Skipping test.");
        }
    }

    @Test
    public void testRenderAppTS() throws IOException {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing renderAppTS");

        // Render TypeScript for the app
        StringBuilder typeScript = codeGenerator.renderAppTS(testApp);

        // Verify the TypeScript is not null or empty
        Assertions.assertNotNull(typeScript);
        Assertions.assertTrue(typeScript.length() > 0);

        System.out.println("[DEBUG_LOG] Rendered TypeScript for app " + testApp.getClass().getSimpleName() + ":");
        System.out.println("[DEBUG_LOG] " + typeScript);

        // Verify it contains expected content
        Assertions.assertTrue(typeScript.toString().contains("import {bootstrapApplication} from '@angular/platform-browser'"));
        Assertions.assertTrue(typeScript.toString().contains("import {appConfig} from './app/app.config'"));
        Assertions.assertTrue(typeScript.toString().contains("bootstrapApplication("));
    }

    /**
     * Find a component of a specific type
     */
    private <T> T findComponentOfType(Class<T> componentType) {
        for (ClassInfo classInfo : IGuiceContext.instance().getScanResult().getAllClasses()) {
            try {
                Class<?> clazz = classInfo.loadClass();
                if (componentType.isAssignableFrom(clazz) && !clazz.isInterface() && !clazz.isAnnotation()) {
                    try {
                        return (T) IGuiceContext.get(clazz);
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

    /**
     * Find a component to test with
     * This method tries to find a component in the application to use for testing
     */
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

        // Try to find a provider
        component = findComponentOfType(INgProvider.class);
        if (component != null) return component;

        // Try to find a service provider
        component = findComponentOfType(INgServiceProvider.class);
        if (component != null) return component;

        return null;
    }
}
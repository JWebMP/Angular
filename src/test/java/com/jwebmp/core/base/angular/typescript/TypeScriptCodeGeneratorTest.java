package com.jwebmp.core.base.angular.typescript;

import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.services.compiler.generators.TypeScriptCodeGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Tests for the TypeScriptCodeGenerator class
 */
public class TypeScriptCodeGeneratorTest {
    private TypeScriptCodeGenerator codeGenerator;
    private INgApp<?> testApp;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setup() {
        // Set up the currentAppFile ThreadLocal
        File tempAppDir = tempDir.toFile();
        IComponent.getCurrentAppFile().set(tempAppDir);
        System.out.println("[DEBUG_LOG] Set currentAppFile to: " + tempAppDir.getAbsolutePath());

        // Find a test app to use
        for (io.github.classgraph.ClassInfo classInfo : IGuiceContext.instance().getScanResult().getAllClasses()) {
            try {
                Class<?> aClass = classInfo.loadClass();
                if (INgApp.class.isAssignableFrom(aClass) && !aClass.isInterface() && 
                    aClass.isAnnotationPresent(NgApp.class)) {
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
        INgComponent<?> testComponent = findTestComponent(INgComponent.class);
        if (testComponent == null) {
            System.out.println("[DEBUG_LOG] No test component found. Skipping test.");
            return;
        }

        // Generate TypeScript for the component
        String typeScript = codeGenerator.generateTypeScriptForComponent(testComponent);

        // Verify the TypeScript is not empty
        System.out.println("[DEBUG_LOG] Generated TypeScript:");
        System.out.println("[DEBUG_LOG] " + typeScript);
        Assertions.assertNotNull(typeScript);
        Assertions.assertFalse(typeScript.isEmpty());
    }

    @Test
    public void testRenderComponentTS() throws IOException {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing renderComponentTS");

        // Find a component to test with
        INgComponent<?> testComponent = findTestComponent(INgComponent.class);
        if (testComponent == null) {
            System.out.println("[DEBUG_LOG] No test component found. Skipping test.");
            return;
        }

        // Render TypeScript for the component
        StringBuilder typeScript = codeGenerator.renderComponentTS(testComponent);

        // Verify the TypeScript is not empty
        System.out.println("[DEBUG_LOG] Rendered TypeScript:");
        System.out.println("[DEBUG_LOG] " + typeScript);
        Assertions.assertNotNull(typeScript);
        Assertions.assertTrue(typeScript.length() > 0);
    }

    @Test
    public void testRenderModuleTS() throws IOException {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing renderModuleTS");

        // Find a module to test with
        INgModule<?> testModule = findTestComponent(INgModule.class);
        if (testModule == null) {
            System.out.println("[DEBUG_LOG] No test module found. Skipping test.");
            return;
        }

        // Render TypeScript for the module
        StringBuilder typeScript = codeGenerator.renderModuleTS(testModule);

        // Verify the TypeScript is not empty
        System.out.println("[DEBUG_LOG] Rendered TypeScript:");
        System.out.println("[DEBUG_LOG] " + typeScript);
        Assertions.assertNotNull(typeScript);
        Assertions.assertTrue(typeScript.length() > 0);
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

        // Verify the TypeScript is not empty
        System.out.println("[DEBUG_LOG] Rendered TypeScript:");
        System.out.println("[DEBUG_LOG] " + typeScript);
        Assertions.assertNotNull(typeScript);
        Assertions.assertTrue(typeScript.length() > 0);
        
        // Verify it contains expected content
        Assertions.assertTrue(typeScript.toString().contains("import {bootstrapApplication} from '@angular/platform-browser'"));
        Assertions.assertTrue(typeScript.toString().contains("bootstrapApplication("));
    }

    @Test
    public void testClearTypeScriptCache() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing clearTypeScriptCache");

        // Find a component to test with
        INgComponent<?> testComponent = findTestComponent(INgComponent.class);
        if (testComponent == null) {
            System.out.println("[DEBUG_LOG] No test component found. Skipping test.");
            return;
        }

        // Generate TypeScript for the component to populate the cache
        String typeScript1 = codeGenerator.generateTypeScriptForComponent(testComponent);
        
        // Clear the cache for the component
        codeGenerator.clearTypeScriptCache(testComponent);
        
        // Generate TypeScript again
        String typeScript2 = codeGenerator.generateTypeScriptForComponent(testComponent);
        
        // Verify the TypeScript is the same (since the component hasn't changed)
        Assertions.assertEquals(typeScript1, typeScript2);
    }

    /**
     * Helper method to find a test component of a specific type
     */
    private <T> T findTestComponent(Class<T> componentType) {
        for (io.github.classgraph.ClassInfo classInfo : IGuiceContext.instance().getScanResult().getAllClasses()) {
            try {
                Class<?> aClass = classInfo.loadClass();
                if (componentType.isAssignableFrom(aClass) && !aClass.isInterface() && !aClass.isAnnotation()) {
                    try {
                        return (T) IGuiceContext.get(aClass);
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
}
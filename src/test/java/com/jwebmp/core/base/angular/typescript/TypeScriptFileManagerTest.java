package com.jwebmp.core.base.angular.typescript;

import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.services.compiler.files.TypeScriptFileManager;
import com.jwebmp.core.base.angular.services.compiler.generators.TypeScriptCodeGenerator;
import com.jwebmp.core.base.angular.services.compiler.validators.TypeScriptCodeValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Tests for the TypeScriptFileManager class
 */
public class TypeScriptFileManagerTest {
    private TypeScriptFileManager fileManager;
    private TypeScriptCodeGenerator codeGenerator;
    private TypeScriptCodeValidator codeValidator;
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

        // Initialize the dependencies
        codeGenerator = new TypeScriptCodeGenerator(testApp);
        codeValidator = new TypeScriptCodeValidator();
        
        // Initialize the file manager
        fileManager = new TypeScriptFileManager(testApp, codeGenerator, codeValidator);
    }

    @Test
    public void testGetComponentFilePath() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing getComponentFilePath");

        // Find a component to test with
        INgComponent<?> testComponent = findTestComponent(INgComponent.class);
        if (testComponent == null) {
            System.out.println("[DEBUG_LOG] No test component found. Skipping test.");
            return;
        }

        // Get the file path for the component
        File filePath = fileManager.getComponentFilePath(testComponent);

        // Verify the file path is not null
        System.out.println("[DEBUG_LOG] Component file path: " + (filePath != null ? filePath.getAbsolutePath() : "null"));
        Assertions.assertNotNull(filePath);
        
        // Verify the file path has the correct extension
        Assertions.assertTrue(filePath.getPath().endsWith(".ts"));
    }

    @Test
    public void testWriteComponentToFile() throws IOException {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing writeComponentToFile");

        // Find a component to test with
        INgComponent<?> testComponent = findTestComponent(INgComponent.class);
        if (testComponent == null) {
            System.out.println("[DEBUG_LOG] No test component found. Skipping test.");
            return;
        }

        // Write the component to a file
        File file = fileManager.writeComponentToFile(testComponent);

        // Verify the file is not null
        System.out.println("[DEBUG_LOG] Written file: " + (file != null ? file.getAbsolutePath() : "null"));
        Assertions.assertNotNull(file);
        
        // Verify the file exists
        Assertions.assertTrue(file.exists());
        
        // Verify the file has content
        String content = Files.readString(file.toPath());
        System.out.println("[DEBUG_LOG] File content: " + content);
        Assertions.assertFalse(content.isEmpty());
    }

    @Test
    public void testProcessComponents() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processComponents");

        // Find components to test with
        Collection<IComponent<?>> components = new ArrayList<>();
        INgComponent<?> component = findTestComponent(INgComponent.class);
        if (component != null) {
            components.add(component);
        }
        
        INgModule<?> module = findTestComponent(INgModule.class);
        if (module != null) {
            components.add(module);
        }
        
        if (components.isEmpty()) {
            System.out.println("[DEBUG_LOG] No test components found. Skipping test.");
            return;
        }

        // Process the components
        Map<IComponent<?>, File> result = fileManager.processComponents(components, true);

        // Verify the result is not empty
        System.out.println("[DEBUG_LOG] Processed " + result.size() + " components");
        Assertions.assertFalse(result.isEmpty());
        
        // Verify each component has a file
        for (Map.Entry<IComponent<?>, File> entry : result.entrySet()) {
            System.out.println("[DEBUG_LOG] Component: " + entry.getKey().getClass().getSimpleName() + 
                              ", File: " + entry.getValue().getAbsolutePath());
            Assertions.assertNotNull(entry.getValue());
            Assertions.assertTrue(entry.getValue().exists());
        }
    }

    @Test
    public void testClearFilePathCache() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing clearFilePathCache");

        // Find a component to test with
        INgComponent<?> testComponent = findTestComponent(INgComponent.class);
        if (testComponent == null) {
            System.out.println("[DEBUG_LOG] No test component found. Skipping test.");
            return;
        }

        // Get the file path for the component to populate the cache
        File filePath1 = fileManager.getComponentFilePath(testComponent);
        
        // Clear the cache for the component
        fileManager.clearFilePathCache(testComponent);
        
        // Get the file path again
        File filePath2 = fileManager.getComponentFilePath(testComponent);
        
        // Verify the file paths are the same (since the component hasn't changed)
        Assertions.assertEquals(filePath1.getAbsolutePath(), filePath2.getAbsolutePath());
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
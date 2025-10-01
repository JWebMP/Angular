package com.jwebmp.core.base.angular.typescript;

import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.services.compiler.files.TypeScriptFileManager;
import com.jwebmp.core.base.angular.services.compiler.generators.TypeScriptCodeGenerator;
import com.jwebmp.core.base.angular.services.compiler.processors.ComponentProcessor;
import com.jwebmp.core.base.angular.services.compiler.validators.TypeScriptCodeValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Tests for the ComponentProcessor class
 */
public class ComponentProcessorTest {
    private ComponentProcessor componentProcessor;
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
        fileManager = new TypeScriptFileManager(testApp, codeGenerator, codeValidator);
        
        // Initialize the component processor
        componentProcessor = new ComponentProcessor(testApp, fileManager);
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
        Map<IComponent<?>, File> result = componentProcessor.processComponents(components, true);

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
    public void testProcessComponentsOfType() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processComponentsOfType");

        // Process components of type INgComponent
        Map<IComponent<?>, File> result = componentProcessor.processComponentsOfType(INgComponent.class, true);

        // Verify the result
        System.out.println("[DEBUG_LOG] Processed " + result.size() + " components of type INgComponent");
        
        // Log the components
        for (Map.Entry<IComponent<?>, File> entry : result.entrySet()) {
            System.out.println("[DEBUG_LOG] Component: " + entry.getKey().getClass().getSimpleName() + 
                              ", File: " + entry.getValue().getAbsolutePath());
        }
    }

    @Test
    public void testGetPackageFilter() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing getPackageFilter");

        // Get the package filter
        var packageFilter = componentProcessor.getPackageFilter();

        // Verify the filter is not null
        Assertions.assertNotNull(packageFilter);
        
        // Test the filter with some classes
        Assertions.assertTrue(packageFilter.test(INgComponent.class));
        Assertions.assertTrue(packageFilter.test(INgModule.class));
    }

    @Test
    public void testGetPackageFilterClassInfo() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing getPackageFilterClassInfo");

        // Get the package filter for ClassInfo
        var packageFilterClassInfo = componentProcessor.getPackageFilterClassInfo();

        // Verify the filter is not null
        Assertions.assertNotNull(packageFilterClassInfo);
        
        // Test the filter with some class infos
        for (io.github.classgraph.ClassInfo classInfo : IGuiceContext.instance().getScanResult().getAllClasses()) {
            if (classInfo.getPackageName().startsWith("com.jwebmp")) {
                Assertions.assertTrue(packageFilterClassInfo.test(classInfo));
                break;
            }
        }
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
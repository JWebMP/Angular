package com.jwebmp.core.base.angular.services.compiler.files;

import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.AppUtils;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.services.compiler.generators.TypeScriptCodeGenerator;
import com.jwebmp.core.base.angular.services.compiler.validators.TypeScriptCodeValidator;
import io.github.classgraph.ClassInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
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
        IComponent<?> component = findTestComponent();
        if (component != null) {
            // Get the file path for the component
            File filePath = fileManager.getComponentFilePath(component);

            // Verify the file path is not null
            Assertions.assertNotNull(filePath);
            System.out.println("[DEBUG_LOG] File path for component " + component.getClass().getSimpleName() + ": " + filePath.getAbsolutePath());

            // Test the cache by getting the file path again
            File cachedFilePath = fileManager.getComponentFilePath(component);
            Assertions.assertEquals(filePath, cachedFilePath);

            // Test clearing the cache
            fileManager.clearFilePathCache(component);
            File regeneratedFilePath = fileManager.getComponentFilePath(component);
            Assertions.assertNotNull(regeneratedFilePath);
            Assertions.assertEquals(filePath, regeneratedFilePath);
        } else {
            System.out.println("[DEBUG_LOG] No test component found. Skipping test.");
        }
    }

    @Test
    public void testWriteComponentToFile() throws IOException {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing writeComponentToFile");

        // Find a component to test with
        IComponent<?> component = findTestComponent();
        if (component != null) {
            // Write the component to a file
            File file = fileManager.writeComponentToFile(component);

            // Verify the file is not null and exists
            Assertions.assertNotNull(file);
            Assertions.assertTrue(file.exists());
            System.out.println("[DEBUG_LOG] Wrote component " + component.getClass().getSimpleName() + " to file: " + file.getAbsolutePath());

            // Verify the file has content
            String content = Files.readString(file.toPath());
            System.out.println("[DEBUG_LOG] File content length: " + content.length());
            Assertions.assertFalse(content.isEmpty());

            // Test writing with force flag
            File forcedFile = fileManager.writeComponentToFile(component, true);
            Assertions.assertNotNull(forcedFile);
            Assertions.assertTrue(forcedFile.exists());
            Assertions.assertEquals(file, forcedFile);
        } else {
            System.out.println("[DEBUG_LOG] No test component found. Skipping test.");
        }
    }

    @Test
    public void testProcessComponents() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processComponents");

        // Create a list of components to process
        List<IComponent<?>> components = new ArrayList<>();

        // Add components of different types if available
        addComponentIfAvailable(components, INgComponent.class);
        addComponentIfAvailable(components, INgDirective.class);
        addComponentIfAvailable(components, INgDataService.class);
        addComponentIfAvailable(components, INgDataType.class);
        addComponentIfAvailable(components, INgModule.class);
        addComponentIfAvailable(components, INgProvider.class);
        addComponentIfAvailable(components, INgServiceProvider.class);

        if (!components.isEmpty()) {
            // Process the components
            Map<IComponent<?>, File> result = fileManager.processComponents(components, false);

            // Verify the result is not null and has the expected size
            Assertions.assertNotNull(result);
            Assertions.assertEquals(components.size(), result.size());
            System.out.println("[DEBUG_LOG] Processed " + result.size() + " components");

            // Verify each component was processed
            for (IComponent<?> component : components) {
                Assertions.assertTrue(result.containsKey(component));
                File file = result.get(component);
                Assertions.assertNotNull(file);
                Assertions.assertTrue(file.exists());
                System.out.println("[DEBUG_LOG] Component " + component.getClass().getSimpleName() + " was processed to file: " + file.getAbsolutePath());
            }
        } else {
            System.out.println("[DEBUG_LOG] No components found to process. Skipping test.");
        }
    }

    /**
     * Helper method to add a component of a specific type to a list if available
     */
    private <T extends IComponent<?>> void addComponentIfAvailable(List<IComponent<?>> components, Class<T> componentType) {
        T component = findComponentOfType(componentType);
        if (component != null) {
            components.add(component);
        }
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

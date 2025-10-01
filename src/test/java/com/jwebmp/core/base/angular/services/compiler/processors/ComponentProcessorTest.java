package com.jwebmp.core.base.angular.services.compiler.processors;

import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.AppUtils;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.services.compiler.files.TypeScriptFileManager;
import com.jwebmp.core.base.angular.services.compiler.generators.TypeScriptCodeGenerator;
import com.jwebmp.core.base.angular.services.compiler.validators.TypeScriptCodeValidator;
import io.github.classgraph.ClassInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tests for the ComponentProcessor class
 */
public class ComponentProcessorTest {
    private ComponentProcessor componentProcessor;
    private TypeScriptFileManager fileManager;
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
        TypeScriptCodeGenerator codeGenerator = new TypeScriptCodeGenerator(testApp);
        TypeScriptCodeValidator codeValidator = new TypeScriptCodeValidator();
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
            Map<IComponent<?>, File> result = componentProcessor.processComponents(components, false);

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

    @Test
    public void testProcessComponentsOfType() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processComponentsOfType");

        // Test processing components of different types
        testProcessComponentsOfType(INgComponent.class, "Component");
        testProcessComponentsOfType(INgDirective.class, "Directive");
        testProcessComponentsOfType(INgDataService.class, "DataService");
        testProcessComponentsOfType(INgDataType.class, "DataType");
        testProcessComponentsOfType(INgModule.class, "Module");
        testProcessComponentsOfType(INgProvider.class, "Provider");
        testProcessComponentsOfType(INgServiceProvider.class, "ServiceProvider");
    }

    @Test
    public void testPackageFilter() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing package filter");

        // Get the package filter
        var packageFilter = componentProcessor.getPackageFilter();
        
        // Test the package filter with different packages
        Assertions.assertTrue(packageFilter.test(INgComponent.class), "Package filter should accept JWebMP classes");
        Assertions.assertTrue(packageFilter.test(IGuiceContext.class), "Package filter should accept GuicedEE classes");
        Assertions.assertTrue(packageFilter.test(testApp.getClass()), "Package filter should accept app classes");
        Assertions.assertFalse(packageFilter.test(String.class), "Package filter should reject non-JWebMP, non-GuicedEE, non-app classes");
    }

    @Test
    public void testPackageFilterClassInfo() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing package filter for ClassInfo");

        // Get the package filter for ClassInfo
        var packageFilterClassInfo = componentProcessor.getPackageFilterClassInfo();
        
        // Find class infos to test with
        ClassInfo jwebmpClassInfo = findClassInfoOfType(INgComponent.class);
        ClassInfo guicedeeClassInfo = findClassInfoOfType(IGuiceContext.class);
        ClassInfo appClassInfo = findClassInfoOfType(testApp.getClass());
        ClassInfo javaClassInfo = findClassInfoOfType(String.class);
        
        if (jwebmpClassInfo != null) {
            Assertions.assertTrue(packageFilterClassInfo.test(jwebmpClassInfo), "Package filter should accept JWebMP class infos");
        }
        
        if (guicedeeClassInfo != null) {
            Assertions.assertTrue(packageFilterClassInfo.test(guicedeeClassInfo), "Package filter should accept GuicedEE class infos");
        }
        
        if (appClassInfo != null) {
            Assertions.assertTrue(packageFilterClassInfo.test(appClassInfo), "Package filter should accept app class infos");
        }
        
        if (javaClassInfo != null) {
            Assertions.assertFalse(packageFilterClassInfo.test(javaClassInfo), "Package filter should reject non-JWebMP, non-GuicedEE, non-app class infos");
        }
    }

    /**
     * Helper method to test processing components of a specific type
     */
    private <T extends IComponent<?>> void testProcessComponentsOfType(Class<T> componentType, String typeName) {
        System.out.println("[DEBUG_LOG] Testing processing " + typeName + " components");
        
        // Process components of the specified type
        Map<IComponent<?>, File> result = componentProcessor.processComponentsOfType(componentType, false);
        
        // Verify the result is not null
        Assertions.assertNotNull(result);
        System.out.println("[DEBUG_LOG] Processed " + result.size() + " " + typeName + " components");
        
        // Verify each component was processed
        for (Map.Entry<IComponent<?>, File> entry : result.entrySet()) {
            IComponent<?> component = entry.getKey();
            File file = entry.getValue();
            Assertions.assertNotNull(file);
            Assertions.assertTrue(file.exists());
            System.out.println("[DEBUG_LOG] " + typeName + " " + component.getClass().getSimpleName() + " was processed to file: " + file.getAbsolutePath());
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
     * Find a class info of a specific type
     */
    private ClassInfo findClassInfoOfType(Class<?> type) {
        for (ClassInfo classInfo : IGuiceContext.instance().getScanResult().getAllClasses()) {
            try {
                Class<?> clazz = classInfo.loadClass();
                if (type.isAssignableFrom(clazz)) {
                    return classInfo;
                }
            } catch (Exception e) {
                // Continue to next class
            }
        }
        return null;
    }
}
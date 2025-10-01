package com.jwebmp.core.base.angular.typescript;

import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.services.compiler.files.TypeScriptFileManager;
import com.jwebmp.core.base.angular.services.compiler.generators.TypeScriptCodeGenerator;
import com.jwebmp.core.base.angular.services.compiler.processors.AngularModuleProcessor;
import com.jwebmp.core.base.angular.services.compiler.processors.ComponentProcessor;
import com.jwebmp.core.base.angular.services.compiler.validators.TypeScriptCodeValidator;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

/**
 * Tests for the AngularModuleProcessor class
 */
public class AngularModuleProcessorTest {
    private AngularModuleProcessor moduleProcessor;
    private TypeScriptCodeGenerator codeGenerator;
    private TypeScriptFileManager fileManager;
    private ComponentProcessor componentProcessor;
    private TypeScriptCodeValidator codeValidator;
    private INgApp<?> testApp;
    private ScanResult scanResult;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setup() {
        // Set up the currentAppFile ThreadLocal
        File tempAppDir = tempDir.toFile();
        IComponent.getCurrentAppFile().set(tempAppDir);
        System.out.println("[DEBUG_LOG] Set currentAppFile to: " + tempAppDir.getAbsolutePath());

        // Get the scan result
        scanResult = IGuiceContext.instance().getScanResult();

        // Find a test app to use
        for (io.github.classgraph.ClassInfo classInfo : scanResult.getAllClasses()) {
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
        componentProcessor = new ComponentProcessor(testApp, fileManager);
        
        // Initialize the module processor
        moduleProcessor = new AngularModuleProcessor(testApp, codeGenerator, fileManager, componentProcessor);
    }

    @Test
    public void testProcessAngularModules() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processAngularModules");

        // Process Angular modules
        try {
            moduleProcessor.processAngularModules(
                tempDir.toFile(),
                scanResult,
                (Class<? extends INgApp<?>>) testApp.getClass(),
                new File(tempDir.toFile(), "src")
            );
            
            // Verify the method doesn't throw an exception
            Assertions.assertTrue(true);
        } catch (Exception e) {
            System.out.println("[DEBUG_LOG] Exception in processAngularModules: " + e.getMessage());
            e.printStackTrace();
            Assertions.fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testProcessStandaloneComponents() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processStandaloneComponents");

        // Process standalone components
        try {
            moduleProcessor.processStandaloneComponents(
                tempDir.toFile(),
                scanResult,
                (Class<? extends INgApp<?>>) testApp.getClass(),
                new File(tempDir.toFile(), "src")
            );
            
            // Verify the method doesn't throw an exception
            Assertions.assertTrue(true);
        } catch (Exception e) {
            System.out.println("[DEBUG_LOG] Exception in processStandaloneComponents: " + e.getMessage());
            e.printStackTrace();
            Assertions.fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testProcessAllComponents() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processAllComponents");

        // Process all components
        try {
            moduleProcessor.processAllComponents(
                tempDir.toFile(),
                scanResult,
                (Class<? extends INgApp<?>>) testApp.getClass(),
                new File(tempDir.toFile(), "src")
            );
            
            // Verify the method doesn't throw an exception
            Assertions.assertTrue(true);
        } catch (Exception e) {
            System.out.println("[DEBUG_LOG] Exception in processAllComponents: " + e.getMessage());
            e.printStackTrace();
            Assertions.fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testProcessNgModuleFiles() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processNgModuleFiles");

        // Find a module to test with
        INgModule<?> testModule = findTestComponent(INgModule.class);
        if (testModule == null) {
            System.out.println("[DEBUG_LOG] No test module found. Skipping test.");
            return;
        }

        // Get the class info for the module
        io.github.classgraph.ClassInfo moduleClassInfo = scanResult.getClassInfo(testModule.getClass().getName());
        if (moduleClassInfo == null) {
            System.out.println("[DEBUG_LOG] No class info found for module. Skipping test.");
            return;
        }

        // Process the module
        try {
            moduleProcessor.processNgModuleFiles(
                tempDir.toFile(),
                moduleClassInfo,
                scanResult,
                (Class<? extends INgApp<?>>) testApp.getClass(),
                new File(tempDir.toFile(), "src")
            );
            
            // Verify the method doesn't throw an exception
            Assertions.assertTrue(true);
        } catch (Exception e) {
            System.out.println("[DEBUG_LOG] Exception in processNgModuleFiles: " + e.getMessage());
            e.printStackTrace();
            Assertions.fail("Exception thrown: " + e.getMessage());
        }
    }

    /**
     * Helper method to find a test component of a specific type
     */
    private <T> T findTestComponent(Class<T> componentType) {
        for (io.github.classgraph.ClassInfo classInfo : scanResult.getAllClasses()) {
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
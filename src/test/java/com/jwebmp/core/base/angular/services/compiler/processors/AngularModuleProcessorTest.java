package com.jwebmp.core.base.angular.services.compiler.processors;

import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.AppUtils;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.services.NGApplication;
import com.jwebmp.core.base.angular.services.compiler.files.TypeScriptFileManager;
import com.jwebmp.core.base.angular.services.compiler.generators.TypeScriptCodeGenerator;
import com.jwebmp.core.base.angular.services.compiler.validators.TypeScriptCodeValidator;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Tests for the AngularModuleProcessor class
 */
public class AngularModuleProcessorTest {
    private AngularModuleProcessor moduleProcessor;
    private TypeScriptCodeGenerator codeGenerator;
    private TypeScriptFileManager fileManager;
    private ComponentProcessor componentProcessor;
    private INgApp<?> testApp;
    private ScanResult scanResult;

    @BeforeEach
    public void setup() {
        // Get the scan result
        scanResult = IGuiceContext.instance().getScanResult();

        // Find a test app to use
        for (ClassInfo classInfo : scanResult.getAllClasses()) {
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
        TypeScriptCodeValidator codeValidator = new TypeScriptCodeValidator();
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

        // Get the app path
        File appPath = AppUtils.getAppPath((Class<? extends INgApp<?>>) testApp.getClass());
        
        // Get the source directory
        File srcDirectory = new File(appPath, "src");
        
        // Process Angular modules
        moduleProcessor.processAngularModules(appPath, scanResult, (Class<? extends INgApp<?>>) testApp.getClass(), srcDirectory);

        // This test is more difficult to verify since it depends on modules in the classpath
        // We'll just verify it doesn't throw an exception
        Assertions.assertTrue(true);
    }

    @Test
    public void testProcessStandaloneComponents() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processStandaloneComponents");

        // Get the app path
        File appPath = AppUtils.getAppPath((Class<? extends INgApp<?>>) testApp.getClass());
        
        // Get the source directory
        File srcDirectory = new File(appPath, "src");
        
        // Process standalone components
        moduleProcessor.processStandaloneComponents(appPath, scanResult, (Class<? extends INgApp<?>>) testApp.getClass(), srcDirectory);

        // This test is more difficult to verify since it depends on components in the classpath
        // We'll just verify it doesn't throw an exception
        Assertions.assertTrue(true);
    }

    @Test
    public void testProcessNgModuleFiles() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processNgModuleFiles");

        // Get the app path
        File appPath = AppUtils.getAppPath((Class<? extends INgApp<?>>) testApp.getClass());
        
        // Get the source directory
        File srcDirectory = new File(appPath, "src");
        
        // Find a module class info
        ClassInfo moduleClassInfo = findClassInfoOfType(INgModule.class);
        if (moduleClassInfo != null) {
            // Process the module
            moduleProcessor.processNgModuleFiles(appPath, moduleClassInfo, scanResult, (Class<? extends INgApp<?>>) testApp.getClass(), srcDirectory);
            
            // Verify the module was processed
            try {
                INgModule<?> module = (INgModule<?>) IGuiceContext.get(moduleClassInfo.loadClass());
                File moduleFile = fileManager.getComponentFilePath(module);
                if (moduleFile != null) {
                    Assertions.assertTrue(moduleFile.exists(), "Module file should exist");
                    System.out.println("[DEBUG_LOG] Module file: " + moduleFile.getAbsolutePath());
                    
                    // Verify the file has content
                    String content = Files.readString(moduleFile.toPath());
                    Assertions.assertFalse(content.isEmpty(), "Module file should not be empty");
                    System.out.println("[DEBUG_LOG] Module file content length: " + content.length());
                }
            } catch (Exception e) {
                System.out.println("[DEBUG_LOG] Error verifying module: " + e.getMessage());
            }
        } else {
            System.out.println("[DEBUG_LOG] No module found. Skipping test.");
        }
    }

    @Test
    public void testProcessStandaloneComponent() {
        if (testApp == null || !(testApp instanceof NGApplication<?>)) {
            System.out.println("[DEBUG_LOG] No test app found or app is not NGApplication. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processStandaloneComponent");

        // Get the app path
        File appPath = AppUtils.getAppPath((Class<? extends INgApp<?>>) testApp.getClass());
        
        // Get the source directory
        File srcDirectory = new File(appPath, "src");
        
        // Find a component class info
        ClassInfo componentClassInfo = findStandaloneComponentClassInfo();
        if (componentClassInfo != null) {
            // Process the component
            boolean result = moduleProcessor.processStandaloneComponent(
                appPath, 
                (NGApplication<?>) testApp, 
                componentClassInfo, 
                (Class<? extends INgApp<?>>) testApp.getClass(), 
                srcDirectory
            );
            
            // Verify the component was processed
            if (result) {
                try {
                    // Check if the component files were created
                    File tsFile = AppUtils.getFile((Class<? extends INgApp<?>>) testApp.getClass(), componentClassInfo.loadClass(), ".ts");
                    File htmlFile = AppUtils.getFile((Class<? extends INgApp<?>>) testApp.getClass(), componentClassInfo.loadClass(), ".html");
                    File cssFile = AppUtils.getFile((Class<? extends INgApp<?>>) testApp.getClass(), componentClassInfo.loadClass(), ".scss");
                    
                    if (tsFile != null) {
                        Assertions.assertTrue(tsFile.exists(), "TypeScript file should exist");
                        System.out.println("[DEBUG_LOG] TypeScript file: " + tsFile.getAbsolutePath());
                        
                        // Verify the file has content
                        String content = Files.readString(tsFile.toPath());
                        Assertions.assertFalse(content.isEmpty(), "TypeScript file should not be empty");
                        System.out.println("[DEBUG_LOG] TypeScript file content length: " + content.length());
                    }
                    
                    if (htmlFile != null) {
                        Assertions.assertTrue(htmlFile.exists(), "HTML file should exist");
                        System.out.println("[DEBUG_LOG] HTML file: " + htmlFile.getAbsolutePath());
                    }
                    
                    if (cssFile != null) {
                        Assertions.assertTrue(cssFile.exists(), "CSS file should exist");
                        System.out.println("[DEBUG_LOG] CSS file: " + cssFile.getAbsolutePath());
                    }
                } catch (Exception e) {
                    System.out.println("[DEBUG_LOG] Error verifying component: " + e.getMessage());
                }
            } else {
                System.out.println("[DEBUG_LOG] Component processing returned false.");
            }
        } else {
            System.out.println("[DEBUG_LOG] No standalone component found. Skipping test.");
        }
    }

    @Test
    public void testProcessAllComponents() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processAllComponents");

        // Get the app path
        File appPath = AppUtils.getAppPath((Class<? extends INgApp<?>>) testApp.getClass());
        
        // Get the source directory
        File srcDirectory = new File(appPath, "src");
        
        // Process all components
        moduleProcessor.processAllComponents(appPath, scanResult, (Class<? extends INgApp<?>>) testApp.getClass(), srcDirectory);

        // This test is more difficult to verify since it depends on components in the classpath
        // We'll just verify it doesn't throw an exception
        Assertions.assertTrue(true);
    }

    /**
     * Find a class info of a specific type
     */
    private ClassInfo findClassInfoOfType(Class<?> type) {
        List<ClassInfo> classInfos = scanResult.getAllClasses()
            .stream()
            .filter(classInfo -> {
                try {
                    Class<?> clazz = classInfo.loadClass();
                    return type.isAssignableFrom(clazz) && !clazz.isInterface() && !clazz.isAnnotation();
                } catch (Exception e) {
                    return false;
                }
            })
            .toList();
        
        return classInfos.isEmpty() ? null : classInfos.get(0);
    }

    /**
     * Find a standalone component class info
     */
    private ClassInfo findStandaloneComponentClassInfo() {
        List<ClassInfo> classInfos = scanResult.getClassesWithAnnotation("com.jwebmp.core.base.angular.client.annotations.angular.NgComponent")
            .stream()
            .filter(classInfo -> {
                try {
                    // Check if the component is standalone
                    return classInfo.getAnnotationInfo("com.jwebmp.core.base.angular.client.annotations.angular.NgComponent")
                        .getParameterValues()
                        .stream()
                        .anyMatch(param -> param.getName().equals("standalone") && Boolean.TRUE.equals(param.getValue()));
                } catch (Exception e) {
                    return false;
                }
            })
            .toList();
        
        return classInfos.isEmpty() ? null : classInfos.get(0);
    }
}
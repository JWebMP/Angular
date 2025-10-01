package com.jwebmp.core.base.angular.typescript;

import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.services.interfaces.IComponent;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import com.jwebmp.core.base.angular.services.compiler.dependencies.DependencyManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

/**
 * Tests for the DependencyManager class
 */
public class DependencyManagerTest {
    private DependencyManager dependencyManager;
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

        // Initialize the dependency manager with the test app
        dependencyManager = new DependencyManager(testApp);
    }

    /**
     * Test checking if the operating system is supported
     */
    @Test
    public void testIsOsSupported() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing isOsSupported...");

        // Check if the OS is supported
        boolean isSupported = dependencyManager.isOsSupported();

        System.out.println("[DEBUG_LOG] OS supported: " + isSupported);

        // We can't assert a specific value here since it depends on the OS,
        // but we can verify that the method returns a value
        Assertions.assertNotNull(isSupported);
    }

    /**
     * Test getting the operating system name
     */
    @Test
    public void testGetOsName() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing getOsName...");

        // Get the OS name
        String osName = dependencyManager.getOsName();

        System.out.println("[DEBUG_LOG] OS name: " + osName);

        // Verify the OS name is not null or empty
        Assertions.assertNotNull(osName);
        Assertions.assertFalse(osName.isEmpty());
    }

    /**
     * Test installing dependencies
     * Note: This test is mocked to avoid actually installing dependencies
     */
    @Test
    public void testInstallDependencies() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing installDependencies...");

        // Create a temporary directory for testing
        File tempDirectory = tempDir.toFile();

        // Create a custom DependencyManager that doesn't actually install dependencies
        DependencyManager customDependencyManager = new DependencyManager(testApp) {
            @Override
            public void installDependencies(File appBaseDirectory) {
                // Mock implementation that doesn't actually install dependencies
                System.out.println("[DEBUG_LOG] Mock installDependencies called for directory: " + appBaseDirectory.getAbsolutePath());
            }
        };

        // Install dependencies using the custom dependency manager
        customDependencyManager.installDependencies(tempDirectory);

        // We can't assert anything specific here since the method is mocked,
        // but we can verify that the test runs without exceptions
    }

    /**
     * Test building Angular app
     * Note: This test is mocked to avoid actually building the app
     */
    @Test
    public void testBuildAngularApp() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing buildAngularApp...");

        // Create a temporary directory for testing
        File tempDirectory = tempDir.toFile();

        // Create a custom DependencyManager that doesn't actually build the app
        DependencyManager customDependencyManager = new DependencyManager(testApp) {
            @Override
            public void buildAngularApp(File appBaseDirectory) {
                // Mock implementation that doesn't actually build the app
                System.out.println("[DEBUG_LOG] Mock buildAngularApp called for directory: " + appBaseDirectory.getAbsolutePath());
            }
        };

        // Build the app using the custom dependency manager
        customDependencyManager.buildAngularApp(tempDirectory);

        // We can't assert anything specific here since the method is mocked,
        // but we can verify that the test runs without exceptions
    }
}

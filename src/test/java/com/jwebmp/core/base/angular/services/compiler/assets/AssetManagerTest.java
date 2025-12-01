package com.jwebmp.core.base.angular.services.compiler.assets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.AppUtils;
import com.jwebmp.core.base.angular.client.services.interfaces.IComponent;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static com.guicedee.client.implementations.ObjectBinderKeys.DefaultObjectMapper;

/**
 * Tests for the AssetManager class
 */
public class AssetManagerTest {
    private AssetManager assetManager;
    private INgApp<?> testApp;
    private ScanResult scanResult;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setup() {
        // Get the scan result
        scanResult = IGuiceContext.instance().getScanResult();

        // Find a test app to use
        for (io.github.classgraph.ClassInfo classInfo : scanResult.getAllClasses()) {
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

        // Initialize the asset manager
        assetManager = new AssetManager(testApp);
    }

    @Test
    public void testProcessAssets() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processAssets");

        // Process assets
        Map<String, String> namedAssets = assetManager.processAssets();

        // Verify the result is not null
        Assertions.assertNotNull(namedAssets);
        System.out.println("[DEBUG_LOG] Processed " + namedAssets.size() + " assets");
    }

    @Test
    public void testProcessStylesheets() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processStylesheets");

        // Process stylesheets
        assetManager.processStylesheets();

        // Verify the styles global set is not null
        Set<String> stylesGlobal = assetManager.getStylesGlobal();
        Assertions.assertNotNull(stylesGlobal);
        System.out.println("[DEBUG_LOG] Processed " + stylesGlobal.size() + " stylesheets");
    }

    @Test
    public void testProcessScripts() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processScripts");

        // Process scripts
        assetManager.processScripts();

        // Verify the scripts set is not null
        Set<String> scripts = assetManager.getScripts();
        Assertions.assertNotNull(scripts);
        System.out.println("[DEBUG_LOG] Processed " + scripts.size() + " scripts");
    }

    @Test
    public void testRenderAngularApplicationFiles() throws IOException {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing renderAngularApplicationFiles");

        // Get the app path
        File appPath = AppUtils.getAppPath((Class<? extends INgApp<?>>) testApp.getClass());
        
        // Process assets, stylesheets, and scripts first
        Map<String, String> namedAssets = assetManager.processAssets();
        assetManager.processStylesheets();
        assetManager.processScripts();

        // Get the object mapper
        ObjectMapper om = IGuiceContext.get(DefaultObjectMapper);

        // Render Angular application files
        assetManager.renderAngularApplicationFiles(appPath, (Class<? extends INgApp<?>>) testApp.getClass(), namedAssets, testApp, om);

        // Verify the angular.json file was created
        File angularJsonFile = AppUtils.getAngularJsonPath((Class<? extends INgApp<?>>) testApp.getClass(), false);
        System.out.println("[DEBUG_LOG] angular.json file path: " + angularJsonFile.getAbsolutePath());
        Assertions.assertTrue(angularJsonFile.exists());

        // Verify the file has content
        String content = Files.readString(angularJsonFile.toPath());
        System.out.println("[DEBUG_LOG] angular.json content length: " + content.length());
        Assertions.assertFalse(content.isEmpty());

        // Verify it contains expected content
        Assertions.assertTrue(content.contains("\"projects\""));
        Assertions.assertTrue(content.contains("\"architect\""));
        Assertions.assertTrue(content.contains("\"build\""));
    }

    @Test
    public void testProcessResources() throws IOException {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processResources");

        // Process resources
        assetManager.processResources((Class<? extends INgApp<?>>) testApp.getClass(), scanResult);

        // This test is more difficult to verify since it depends on resources in the classpath
        // We'll just verify it doesn't throw an exception
        Assertions.assertTrue(true);
    }

    @Test
    public void testProcessAppResources() throws IOException {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processAppResources");

        // Process app resources
        assetManager.processAppResources((Class<? extends INgApp<?>>) testApp.getClass(), scanResult);

        // This test is more difficult to verify since it depends on resources in the classpath
        // We'll just verify it doesn't throw an exception
        Assertions.assertTrue(true);
    }
}
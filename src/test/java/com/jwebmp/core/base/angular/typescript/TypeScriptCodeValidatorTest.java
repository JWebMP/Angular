package com.jwebmp.core.base.angular.typescript;

import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.client.services.interfaces.IComponent;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import com.jwebmp.core.base.angular.services.compiler.validators.TypeScriptCodeValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

/**
 * Tests for the TypeScriptCodeValidator class
 */
public class TypeScriptCodeValidatorTest {
    private TypeScriptCodeValidator codeValidator;
    private INgApp<?> testApp;
    private IComponent<?> testComponent;

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

        // Find a test component to use
        for (io.github.classgraph.ClassInfo classInfo : IGuiceContext.instance().getScanResult().getAllClasses()) {
            try {
                Class<?> aClass = classInfo.loadClass();
                if (INgComponent.class.isAssignableFrom(aClass) && !aClass.isInterface() && !aClass.isAnnotation()) {
                    testComponent = (IComponent<?>) IGuiceContext.get(aClass);
                    break;
                }
            } catch (Exception e) {
                // Continue to next class
            }
        }

        // Initialize the code validator
        codeValidator = new TypeScriptCodeValidator();
    }

    @Test
    public void testValidateTypeScript() {
        System.out.println("[DEBUG_LOG] Testing validateTypeScript");

        // Skip test if no test component is found
        if (testComponent == null) {
            System.out.println("[DEBUG_LOG] No test component found. Skipping test.");
            return;
        }

        // Create a simple TypeScript code with issues to fix
        String typeScript = "import {Component} from '@angular/core'\nimport {Component} from '@angular/core'\n\n" +
                "@Component({\n" +
                "  selector: 'app-test',\n" +
                "  template: '<div>Test</div>',\n" +
                "  styles: []\n" +
                "})\n" +
                "export class TestComponent {\n" +
                "  title = 'Test'\n" + // Missing semicolon
                "  items = [1, 2, 3,]\n" + // Trailing comma
                "  obj = {a: 1, b: 2,}\n" + // Trailing comma
                "}";

        // Validate the TypeScript using a real component
        String validatedTypeScript = codeValidator.validateTypeScript(typeScript, testComponent);

        System.out.println("[DEBUG_LOG] Original TypeScript:");
        System.out.println("[DEBUG_LOG] " + typeScript);
        System.out.println("[DEBUG_LOG] Validated TypeScript:");
        System.out.println("[DEBUG_LOG] " + validatedTypeScript);

        // Verify the TypeScript is not empty
        Assertions.assertNotNull(validatedTypeScript);
        Assertions.assertFalse(validatedTypeScript.isEmpty());

        // Verify duplicate imports are fixed
        Assertions.assertEquals(1, countOccurrences(validatedTypeScript, "import {Component} from '@angular/core'"));

        // Verify missing semicolons are fixed
        Assertions.assertTrue(validatedTypeScript.contains("title = 'Test';"));

        // Verify trailing commas are fixed
        Assertions.assertTrue(validatedTypeScript.contains("items = [1, 2, 3]"));
        Assertions.assertTrue(validatedTypeScript.contains("obj = {a: 1, b: 2}"));
    }

    @Test
    public void testFixMissingSemicolons() {
        System.out.println("[DEBUG_LOG] Testing fixMissingSemicolons");

        // Skip test if no test component is found
        if (testComponent == null) {
            System.out.println("[DEBUG_LOG] No test component found. Skipping test.");
            return;
        }

        // Create a simple TypeScript code with missing semicolons
        String typeScript = "const a = 1\n" +
                "let b = 2\n" +
                "function test() {\n" +
                "  return 3\n" +
                "}\n" +
                "// This is a comment\n" +
                "const c = test()";

        // Fix missing semicolons
        String fixedTypeScript = codeValidator.fixMissingSemicolons(typeScript);

        System.out.println("[DEBUG_LOG] Original TypeScript:");
        System.out.println("[DEBUG_LOG] " + typeScript);
        System.out.println("[DEBUG_LOG] Fixed TypeScript:");
        System.out.println("[DEBUG_LOG] " + fixedTypeScript);

        // Verify semicolons are added where needed
        Assertions.assertTrue(fixedTypeScript.contains("const a = 1;"));
        Assertions.assertTrue(fixedTypeScript.contains("let b = 2;"));
        Assertions.assertTrue(fixedTypeScript.contains("return 3;"));
        Assertions.assertTrue(fixedTypeScript.contains("const c = test();"));
    }

    @Test
    public void testFixDuplicateImports() {
        System.out.println("[DEBUG_LOG] Testing fixDuplicateImports");

        // Skip test if no test component is found
        if (testComponent == null) {
            System.out.println("[DEBUG_LOG] No test component found. Skipping test.");
            return;
        }

        // Create a simple TypeScript code with duplicate imports
        String typeScript = "import {Component} from '@angular/core'\n" +
                "import {Injectable} from '@angular/core'\n" +
                "import {Component} from '@angular/core'\n" +
                "import {NgModule} from '@angular/core'\n\n" +
                "// Rest of the code\n" +
                "export class TestComponent {}";

        // Fix duplicate imports
        String fixedTypeScript = codeValidator.fixDuplicateImports(typeScript);

        System.out.println("[DEBUG_LOG] Original TypeScript:");
        System.out.println("[DEBUG_LOG] " + typeScript);
        System.out.println("[DEBUG_LOG] Fixed TypeScript:");
        System.out.println("[DEBUG_LOG] " + fixedTypeScript);

        // Verify duplicate imports are removed
        Assertions.assertEquals(1, countOccurrences(fixedTypeScript, "import {Component} from '@angular/core'"));
        Assertions.assertEquals(1, countOccurrences(fixedTypeScript, "import {Injectable} from '@angular/core'"));
        Assertions.assertEquals(1, countOccurrences(fixedTypeScript, "import {NgModule} from '@angular/core'"));
    }

    @Test
    public void testFixTrailingCommas() {
        System.out.println("[DEBUG_LOG] Testing fixTrailingCommas");

        // Skip test if no test component is found
        if (testComponent == null) {
            System.out.println("[DEBUG_LOG] No test component found. Skipping test.");
            return;
        }

        // Create a simple TypeScript code with trailing commas
        String typeScript = "const arr = [1, 2, 3,];\n" +
                "const obj = {a: 1, b: 2,};\n" +
                "function test(a, b,) {}";

        // Fix trailing commas
        String fixedTypeScript = codeValidator.fixTrailingCommas(typeScript);

        System.out.println("[DEBUG_LOG] Original TypeScript:");
        System.out.println("[DEBUG_LOG] " + typeScript);
        System.out.println("[DEBUG_LOG] Fixed TypeScript:");
        System.out.println("[DEBUG_LOG] " + fixedTypeScript);

        // Verify trailing commas are removed
        Assertions.assertTrue(fixedTypeScript.contains("const arr = [1, 2, 3];"));
        Assertions.assertTrue(fixedTypeScript.contains("const obj = {a: 1, b: 2};"));
    }

    @Test
    public void testFormatTypeScript() {
        System.out.println("[DEBUG_LOG] Testing formatTypeScript");

        // Skip test if no test component is found
        if (testComponent == null) {
            System.out.println("[DEBUG_LOG] No test component found. Skipping test.");
            return;
        }

        // Create a simple TypeScript code with formatting issues
        String typeScript = "function test() {\nreturn {\na: 1,\nb: 2\n}\n}";

        // Format the TypeScript
        String formattedTypeScript = codeValidator.formatTypeScript(typeScript);

        System.out.println("[DEBUG_LOG] Original TypeScript:");
        System.out.println("[DEBUG_LOG] " + typeScript);
        System.out.println("[DEBUG_LOG] Formatted TypeScript:");
        System.out.println("[DEBUG_LOG] " + formattedTypeScript);

        // Verify the TypeScript is formatted correctly
        Assertions.assertTrue(formattedTypeScript.contains("function test() {"));
        Assertions.assertTrue(formattedTypeScript.contains("  return {"));
        Assertions.assertTrue(formattedTypeScript.contains("    a: 1,"));
        Assertions.assertTrue(formattedTypeScript.contains("    b: 2"));
        Assertions.assertTrue(formattedTypeScript.contains("  }"));
        Assertions.assertTrue(formattedTypeScript.contains("}"));
    }

    /**
     * Helper method to count occurrences of a substring in a string
     */
    private int countOccurrences(String str, String subStr) {
        int count = 0;
        int lastIndex = 0;
        while (lastIndex != -1) {
            lastIndex = str.indexOf(subStr, lastIndex);
            if (lastIndex != -1) {
                count++;
                lastIndex += subStr.length();
            }
        }
        return count;
    }
}

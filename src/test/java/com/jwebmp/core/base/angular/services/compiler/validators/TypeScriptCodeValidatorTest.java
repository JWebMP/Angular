package com.jwebmp.core.base.angular.services.compiler.validators;

import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.AppUtils;
import com.jwebmp.core.base.angular.client.services.interfaces.IComponent;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import io.github.classgraph.ClassInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * Tests for the TypeScriptCodeValidator class
 */
public class TypeScriptCodeValidatorTest {
    private TypeScriptCodeValidator codeValidator;
    private INgApp<?> testApp;
    private IComponent<?> testComponent;

    @BeforeEach
    public void setup() {
        // Initialize the code validator
        codeValidator = new TypeScriptCodeValidator();

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

        // Find a test component to use
        testComponent = findTestComponent();
    }

    @Test
    public void testValidateTypeScript() {
        if (testApp == null || testComponent == null) {
            System.out.println("[DEBUG_LOG] No test app or component found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing validateTypeScript");

        // Create some TypeScript code with issues to validate
        String typeScript = "import {Component} from '@angular/core'\n" +
                "import {Component} from '@angular/core'\n" + // Duplicate import
                "\n" +
                "@Component({\n" +
                "  selector: 'app-test',\n" +
                "  templateUrl: './test.component.html',\n" +
                "  styleUrls: ['./test.component.scss'],\n" +
                "})\n" + // Trailing comma
                "export class TestComponent {\n" +
                "  title = 'Test Component'\n" + // Missing semicolon
                "  constructor() {\n" +
                "    console.log('Test Component initialized')\n" + // Missing semicolon
                "  }\n" +
                "}\n";

        // Validate the TypeScript code
        String validatedTypeScript = codeValidator.validateTypeScript(typeScript, testComponent);

        // Verify the TypeScript is not null or empty
        Assertions.assertNotNull(validatedTypeScript);
        Assertions.assertFalse(validatedTypeScript.isEmpty());

        System.out.println("[DEBUG_LOG] Original TypeScript:");
        System.out.println("[DEBUG_LOG] " + typeScript);
        System.out.println("[DEBUG_LOG] Validated TypeScript:");
        System.out.println("[DEBUG_LOG] " + validatedTypeScript);

        // Verify that issues were fixed
        // 1. Duplicate imports should be removed
        int importCount = countOccurrences(validatedTypeScript, "import {Component} from '@angular/core'");
        Assertions.assertEquals(1, importCount, "Duplicate imports should be removed");

        // 2. Missing semicolons should be added
        Assertions.assertTrue(validatedTypeScript.contains("title = 'Test Component';"), "Missing semicolons should be added");
        Assertions.assertTrue(validatedTypeScript.contains("console.log('Test Component initialized');"), "Missing semicolons should be added");

        // 3. Trailing commas should be removed
        Assertions.assertFalse(validatedTypeScript.contains("styleUrls: ['./test.component.scss'],"), "Trailing commas should be removed");
        Assertions.assertTrue(validatedTypeScript.contains("styleUrls: ['./test.component.scss']"), "Trailing commas should be removed");
    }

    @Test
    public void testFixMissingSemicolons() {
        System.out.println("[DEBUG_LOG] Testing fixMissingSemicolons");

        // Create some TypeScript code with missing semicolons
        String typeScript = "import {Component} from '@angular/core'\n" +
                "\n" +
                "@Component({\n" +
                "  selector: 'app-test',\n" +
                "  templateUrl: './test.component.html',\n" +
                "  styleUrls: ['./test.component.scss']\n" +
                "})\n" +
                "export class TestComponent {\n" +
                "  title = 'Test Component'\n" + // Missing semicolon
                "  count = 0\n" + // Missing semicolon
                "  constructor() {\n" +
                "    console.log('Test Component initialized')\n" + // Missing semicolon
                "    this.increment()\n" + // Missing semicolon
                "  }\n" +
                "  increment() {\n" +
                "    this.count++\n" + // Missing semicolon
                "  }\n" +
                "}\n";

        // Fix missing semicolons
        String fixedTypeScript = codeValidator.fixMissingSemicolons(typeScript);

        // Verify the TypeScript is not null or empty
        Assertions.assertNotNull(fixedTypeScript);
        Assertions.assertFalse(fixedTypeScript.isEmpty());

        System.out.println("[DEBUG_LOG] Original TypeScript:");
        System.out.println("[DEBUG_LOG] " + typeScript);
        System.out.println("[DEBUG_LOG] Fixed TypeScript:");
        System.out.println("[DEBUG_LOG] " + fixedTypeScript);

        // Verify that missing semicolons were added
        Assertions.assertTrue(fixedTypeScript.contains("title = 'Test Component';"), "Missing semicolons should be added");
        Assertions.assertTrue(fixedTypeScript.contains("count = 0;"), "Missing semicolons should be added");
        Assertions.assertTrue(fixedTypeScript.contains("console.log('Test Component initialized');"), "Missing semicolons should be added");
        Assertions.assertTrue(fixedTypeScript.contains("this.increment();"), "Missing semicolons should be added");
        Assertions.assertTrue(fixedTypeScript.contains("this.count++;"), "Missing semicolons should be added");
    }

    @Test
    public void testFixDuplicateImports() {
        System.out.println("[DEBUG_LOG] Testing fixDuplicateImports");

        // Create some TypeScript code with duplicate imports
        String typeScript = "import {Component} from '@angular/core'\n" +
                "import {Injectable} from '@angular/core'\n" +
                "import {Component} from '@angular/core'\n" + // Duplicate import
                "import {OnInit} from '@angular/core'\n" +
                "import {Injectable} from '@angular/core'\n" + // Duplicate import
                "\n" +
                "@Component({\n" +
                "  selector: 'app-test',\n" +
                "  templateUrl: './test.component.html',\n" +
                "  styleUrls: ['./test.component.scss']\n" +
                "})\n" +
                "export class TestComponent implements OnInit {\n" +
                "  constructor() {}\n" +
                "  ngOnInit() {}\n" +
                "}\n";

        // Fix duplicate imports
        String fixedTypeScript = codeValidator.fixDuplicateImports(typeScript);

        // Verify the TypeScript is not null or empty
        Assertions.assertNotNull(fixedTypeScript);
        Assertions.assertFalse(fixedTypeScript.isEmpty());

        System.out.println("[DEBUG_LOG] Original TypeScript:");
        System.out.println("[DEBUG_LOG] " + typeScript);
        System.out.println("[DEBUG_LOG] Fixed TypeScript:");
        System.out.println("[DEBUG_LOG] " + fixedTypeScript);

        // Verify that duplicate imports were removed
        int componentImportCount = countOccurrences(fixedTypeScript, "import {Component} from '@angular/core'");
        int injectableImportCount = countOccurrences(fixedTypeScript, "import {Injectable} from '@angular/core'");
        int onInitImportCount = countOccurrences(fixedTypeScript, "import {OnInit} from '@angular/core'");

        Assertions.assertEquals(1, componentImportCount, "Duplicate Component imports should be removed");
        Assertions.assertEquals(1, injectableImportCount, "Duplicate Injectable imports should be removed");
        Assertions.assertEquals(1, onInitImportCount, "OnInit import should be preserved");
    }

    @Test
    public void testFixTrailingCommas() {
        System.out.println("[DEBUG_LOG] Testing fixTrailingCommas");

        // Create some TypeScript code with trailing commas
        String typeScript = "import {Component} from '@angular/core'\n" +
                "\n" +
                "@Component({\n" +
                "  selector: 'app-test',\n" +
                "  templateUrl: './test.component.html',\n" +
                "  styleUrls: ['./test.component.scss'],\n" + // Trailing comma
                "})\n" +
                "export class TestComponent {\n" +
                "  items = [\n" +
                "    'item1',\n" +
                "    'item2',\n" +
                "    'item3',\n" + // Trailing comma
                "  ]\n" +
                "  config = {\n" +
                "    name: 'Test',\n" +
                "    value: 123,\n" + // Trailing comma
                "  }\n" +
                "}\n";

        // Fix trailing commas
        String fixedTypeScript = codeValidator.fixTrailingCommas(typeScript);

        // Verify the TypeScript is not null or empty
        Assertions.assertNotNull(fixedTypeScript);
        Assertions.assertFalse(fixedTypeScript.isEmpty());

        System.out.println("[DEBUG_LOG] Original TypeScript:");
        System.out.println("[DEBUG_LOG] " + typeScript);
        System.out.println("[DEBUG_LOG] Fixed TypeScript:");
        System.out.println("[DEBUG_LOG] " + fixedTypeScript);

        // Verify that trailing commas were removed
        Assertions.assertFalse(fixedTypeScript.contains("styleUrls: ['./test.component.scss'],"), "Trailing commas should be removed");
        Assertions.assertTrue(fixedTypeScript.contains("styleUrls: ['./test.component.scss']"), "Trailing commas should be removed");
        
        Assertions.assertFalse(fixedTypeScript.contains("'item3',"), "Trailing commas should be removed");
        Assertions.assertTrue(fixedTypeScript.contains("'item3'"), "Trailing commas should be removed");
        
        Assertions.assertFalse(fixedTypeScript.contains("value: 123,"), "Trailing commas should be removed");
        Assertions.assertTrue(fixedTypeScript.contains("value: 123"), "Trailing commas should be removed");
    }

    @Test
    public void testFormatTypeScript() {
        System.out.println("[DEBUG_LOG] Testing formatTypeScript");

        // Create some TypeScript code with formatting issues
        String typeScript = "import {Component} from '@angular/core';\n" +
                "\n" +
                "@Component({\n" +
                "selector: 'app-test',\n" +
                "templateUrl: './test.component.html',\n" +
                "styleUrls: ['./test.component.scss']\n" +
                "})\n" +
                "export class TestComponent {\n" +
                "title = 'Test Component';\n" +
                "constructor() {\n" +
                "console.log('Test Component initialized');\n" +
                "}\n" +
                "increment() {\n" +
                "let count = 0;\n" +
                "count++;\n" +
                "return count;\n" +
                "}\n" +
                "}\n";

        // Format the TypeScript code
        String formattedTypeScript = codeValidator.formatTypeScript(typeScript);

        // Verify the TypeScript is not null or empty
        Assertions.assertNotNull(formattedTypeScript);
        Assertions.assertFalse(formattedTypeScript.isEmpty());

        System.out.println("[DEBUG_LOG] Original TypeScript:");
        System.out.println("[DEBUG_LOG] " + typeScript);
        System.out.println("[DEBUG_LOG] Formatted TypeScript:");
        System.out.println("[DEBUG_LOG] " + formattedTypeScript);

        // Verify that the code is properly indented
        Assertions.assertTrue(formattedTypeScript.contains("  selector: 'app-test',"), "Code should be properly indented");
        Assertions.assertTrue(formattedTypeScript.contains("  title = 'Test Component';"), "Code should be properly indented");
        Assertions.assertTrue(formattedTypeScript.contains("  constructor() {"), "Code should be properly indented");
        Assertions.assertTrue(formattedTypeScript.contains("    console.log('Test Component initialized');"), "Code should be properly indented");
        Assertions.assertTrue(formattedTypeScript.contains("  increment() {"), "Code should be properly indented");
        Assertions.assertTrue(formattedTypeScript.contains("    let count = 0;"), "Code should be properly indented");
        Assertions.assertTrue(formattedTypeScript.contains("    count++;"), "Code should be properly indented");
        Assertions.assertTrue(formattedTypeScript.contains("    return count;"), "Code should be properly indented");
    }

    /**
     * Count occurrences of a substring in a string
     */
    private int countOccurrences(String str, String substr) {
        int count = 0;
        int index = 0;
        while ((index = str.indexOf(substr, index)) != -1) {
            count++;
            index += substr.length();
        }
        return count;
    }

    /**
     * Find a component to test with
     * This method tries to find a component in the application to use for testing
     */
    private IComponent<?> findTestComponent() {
        // Try to find a component
        for (ClassInfo classInfo : IGuiceContext.instance().getScanResult().getAllClasses()) {
            try {
                Class<?> clazz = classInfo.loadClass();
                if (INgComponent.class.isAssignableFrom(clazz) && !clazz.isInterface() && !clazz.isAnnotation()) {
                    try {
                        return (IComponent<?>) IGuiceContext.get(clazz);
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
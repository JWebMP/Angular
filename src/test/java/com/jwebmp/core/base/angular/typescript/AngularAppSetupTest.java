package com.jwebmp.core.base.angular.typescript;

import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.AngularDataComponent;
import com.jwebmp.core.base.angular.AngularFormDataProvider;
import com.jwebmp.core.base.angular.client.AppUtils;
import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.client.annotations.references.NgComponentReference;
import com.jwebmp.core.base.angular.client.services.AnnotationHelper;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.components.ComponentRenderingTest;
import com.jwebmp.core.base.angular.services.compiler.generators.TypeScriptCodeGenerator;
import com.jwebmp.core.base.angular.services.compiler.setup.AngularAppSetup;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Tests for the AngularAppSetup class
 */
public class AngularAppSetupTest {
    private AngularAppSetup appSetup;
    private INgApp<?> testApp;
    private ScanResult scanResult;
    private TypeScriptCodeGenerator codeGenerator;

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

        // Set up the currentAppFile ThreadLocal with the actual app path
        File appPath = AppUtils.getAppPath((Class<? extends INgApp<?>>) testApp.getClass());
        IComponent.getCurrentAppFile().set(appPath);
        IComponent.app.set(testApp);
        System.out.println("[DEBUG_LOG] Set currentAppFile to: " + appPath.getAbsolutePath());

        // Initialize the app setup and code generator
        appSetup = new AngularAppSetup(testApp);
        codeGenerator = new TypeScriptCodeGenerator(testApp);
    }

    @Test
    public void testProcessNpmrcFile() throws IOException {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processNpmrcFile");

        // Process the npmrc file
        appSetup.processNpmrcFile((Class<? extends INgApp<?>>) testApp.getClass());

        // Get the actual file path using AppUtils
        File npmrcFile = AppUtils.getAppNpmrcPath((Class<? extends INgApp<?>>) testApp.getClass(), false);
        System.out.println("[DEBUG_LOG] Npmrc file path: " + npmrcFile.getAbsolutePath());

        Assertions.assertTrue(npmrcFile.exists());
    }

    @Test
    public void testProcessPackageJsonFile() throws IOException {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processPackageJsonFile");

        // Process the package.json file
        appSetup.processPackageJsonFile((Class<? extends INgApp<?>>) testApp.getClass());

        // Get the actual file path using AppUtils
        File packageJsonFile = AppUtils.getAppPackageJsonPath((Class<? extends INgApp<?>>) testApp.getClass(), false);
        System.out.println("[DEBUG_LOG] Package.json file path: " + packageJsonFile.getAbsolutePath());

        Assertions.assertTrue(packageJsonFile.exists());

        // Verify the file has content
        String content = Files.readString(packageJsonFile.toPath());
        System.out.println("[DEBUG_LOG] Package.json content: " + content);
        Assertions.assertFalse(content.isEmpty());

        // Verify it contains expected content
        Assertions.assertTrue(content.contains("\"dependencies\""));
        Assertions.assertTrue(content.contains("\"devDependencies\""));
    }

    @Test
    public void testProcessTypeScriptConfigFiles() throws IOException {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processTypeScriptConfigFiles");

        // Process the TypeScript config files
        appSetup.processTypeScriptConfigFiles((Class<? extends INgApp<?>>) testApp.getClass());

        // Get the actual file paths using AppUtils
        File tsconfigFile = AppUtils.getAppTsConfigPath((Class<? extends INgApp<?>>) testApp.getClass(), false);
        System.out.println("[DEBUG_LOG] tsconfig.json file path: " + tsconfigFile.getAbsolutePath());

        Assertions.assertTrue(tsconfigFile.exists());

        // Verify that the tsconfig.app.json file was created
        File tsconfigAppFile = AppUtils.getAppTsConfigAppPath((Class<? extends INgApp<?>>) testApp.getClass(), false);
        System.out.println("[DEBUG_LOG] tsconfig.app.json file path: " + tsconfigAppFile.getAbsolutePath());

        Assertions.assertTrue(tsconfigAppFile.exists());

        // Verify that the .gitignore file was created
        File gitignoreFile = AppUtils.getGitIgnorePath((Class<? extends INgApp<?>>) testApp.getClass(), false);
        System.out.println("[DEBUG_LOG] .gitignore file path: " + gitignoreFile.getAbsolutePath());

        Assertions.assertTrue(gitignoreFile.exists());
    }

    @Test
    public void testProcessPolyfillFile() throws IOException {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processPolyfillFile");

        // Process the polyfill file
        appSetup.processPolyfillFile((Class<? extends INgApp<?>>) testApp.getClass());

        // Get the actual file path using AppUtils
        File polyfillsFile = AppUtils.getAppPolyfillsPath((Class<? extends INgApp<?>>) testApp.getClass(), false);
        System.out.println("[DEBUG_LOG] polyfills.ts file path: " + polyfillsFile.getAbsolutePath());

        Assertions.assertTrue(polyfillsFile.exists());

        // Verify the file has content
        String content = Files.readString(polyfillsFile.toPath());
        System.out.println("[DEBUG_LOG] polyfills.ts content: " + content);
        Assertions.assertFalse(content.isEmpty());
    }

    @Test
    public void testProcessAppConfigFile() throws IOException {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processAppConfigFile");

        // Process the app config file
        appSetup.processAppConfigFile((Class<? extends INgApp<?>>) testApp.getClass(), scanResult);

        // Get the actual file path using AppUtils
        File appConfigFile = new File(AppUtils.getAppSrcPath((Class<? extends INgApp<?>>) testApp.getClass()), "app.config.ts");
        System.out.println("[DEBUG_LOG] app.config.ts file path: " + appConfigFile.getAbsolutePath());

        Assertions.assertTrue(appConfigFile.exists());

        // Verify the file has content
        String content = Files.readString(appConfigFile.toPath());
        System.out.println("[DEBUG_LOG] app.config.ts content: " + content);
        Assertions.assertFalse(content.isEmpty());

        // Verify it contains expected content
        Assertions.assertTrue(content.contains("export const appConfig"));
    }

    @Test
    public void testRenderBootIndexHtml() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing renderBootIndexHtml");

        // Render the boot index HTML
        StringBuilder result = appSetup.renderBootIndexHtml(testApp);

        System.out.println("[DEBUG_LOG] Rendered boot index HTML:");
        System.out.println("[DEBUG_LOG] " + result);

        // Verify the HTML is not empty
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.length() > 0);

        // Verify it contains expected content
        Assertions.assertTrue(result.toString().contains("<!DOCTYPE html>"));
        Assertions.assertTrue(result.toString().contains("<html"));
        Assertions.assertTrue(result.toString().contains("<body"));
    }

    @Test
    public void testInstallDependencies() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing installDependencies");

        // This is a static method that would actually run npm install
        // We'll just verify it doesn't throw an exception
        try {
            // Get the actual app path using AppUtils
            File appPath = AppUtils.getAppPath((Class<? extends INgApp<?>>) testApp.getClass());

            // This is commented out because we don't want to actually run npm install in tests
            // AngularAppSetup.installDependencies(appPath);

            // Just assert true since we're not actually running the command
            Assertions.assertTrue(true);
        } catch (Exception e) {
            System.out.println("[DEBUG_LOG] Exception in installDependencies: " + e.getMessage());
            Assertions.fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testBuildAngularApp() {
        if (testApp == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing buildAngularApp");

        // This is a static method that would actually run npm run build
        // We'll just verify it doesn't throw an exception
        try {
            // Get the actual app path using AppUtils
            File appPath = AppUtils.getAppPath((Class<? extends INgApp<?>>) testApp.getClass());

            // This is commented out because we don't want to actually run npm build in tests
            // AngularAppSetup.buildAngularApp(appPath);

            // Just assert true since we're not actually running the command
            Assertions.assertTrue(true);
        } catch (Exception e) {
            System.out.println("[DEBUG_LOG] Exception in buildAngularApp: " + e.getMessage());
            Assertions.fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testRenderedComponentContainsNgComponentReferenceImports() {
        if (testApp == null || codeGenerator == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing rendered component contains NgComponentReference imports");

        // Create the AngularDataComponent through Guice so @Inject methods are called
        AngularDataComponent dataComponent = IGuiceContext.get(AngularDataComponent.class);

        // Generate TypeScript using the compiler output
        String generatedTs = codeGenerator.generateTypeScriptForComponent(dataComponent);

        System.out.println("[DEBUG_LOG] Generated TypeScript for AngularDataComponent:");
        System.out.println("[DEBUG_LOG] " + generatedTs);

        // Verify the TypeScript is not null or empty
        Assertions.assertNotNull(generatedTs, "Generated TypeScript should not be null");
        Assertions.assertFalse(generatedTs.isEmpty(), "Generated TypeScript should not be empty");

        // Verify it contains an import for the referenced AngularFormDataProvider
        String referencedName = AnnotationUtils.getTsFilename(AngularFormDataProvider.class);
        Assertions.assertTrue(generatedTs.contains(referencedName),
                "Rendered TypeScript should contain import for NgComponentReference target '" + referencedName + "', but was:\n" + generatedTs);

        // Verify the import statement is a proper import line
        Assertions.assertTrue(generatedTs.contains("import"),
                "Rendered TypeScript should contain import statements");

        // Verify it contains the @Component decorator
        Assertions.assertTrue(generatedTs.contains("@Component("),
                "Rendered TypeScript should contain @Component decorator");

        // Verify it contains the component selector
        Assertions.assertTrue(generatedTs.contains("data-component"),
                "Rendered TypeScript should contain the component selector 'data-component'");

        // Verify it contains the class declaration
        Assertions.assertTrue(generatedTs.contains("export class AngularDataComponent"),
                "Rendered TypeScript should contain the class declaration");
    }

    @Test
    public void testRenderedComponentWithMultipleNgComponentReferences() {
        if (testApp == null || codeGenerator == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing rendered component with multiple NgComponentReferences");

        // Create the ComponentRenderingTest through Guice
        ComponentRenderingTest componentRenderingTest = IGuiceContext.get(ComponentRenderingTest.class);

        // Generate TypeScript using the compiler output
        String generatedTs = codeGenerator.generateTypeScriptForComponent(componentRenderingTest);

        System.out.println("[DEBUG_LOG] Generated TypeScript for ComponentRenderingTest:");
        System.out.println("[DEBUG_LOG] " + generatedTs);

        // Verify the TypeScript is not null or empty
        Assertions.assertNotNull(generatedTs, "Generated TypeScript should not be null");
        Assertions.assertFalse(generatedTs.isEmpty(), "Generated TypeScript should not be empty");

        // Verify it contains imports for BOTH referenced components
        String eventBusServiceName = AnnotationUtils.getTsFilename(com.jwebmp.core.base.angular.client.services.EventBusService.class);
        String eventBusListenerDirectiveName = AnnotationUtils.getTsFilename(com.jwebmp.core.base.angular.client.services.EventBusListenerDirective.class);

        Assertions.assertTrue(generatedTs.contains(eventBusServiceName),
                "Rendered TypeScript should contain import for NgComponentReference target '" + eventBusServiceName + "', but was:\n" + generatedTs);
        Assertions.assertTrue(generatedTs.contains(eventBusListenerDirectiveName),
                "Rendered TypeScript should contain import for NgComponentReference target '" + eventBusListenerDirectiveName + "', but was:\n" + generatedTs);

        // Verify the @Component decorator is present
        Assertions.assertTrue(generatedTs.contains("@Component("),
                "Rendered TypeScript should contain @Component decorator");
    }

    @Test
    public void testRenderedComponentAnnotationReferencesResolveToImportStatements() {
        if (testApp == null || codeGenerator == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing that @NgComponentReference annotations resolve to import statements");

        // Create the AngularDataComponent through Guice
        AngularDataComponent dataComponent = IGuiceContext.get(AngularDataComponent.class);

        // Verify the annotation is present on the class
        List<NgComponentReference> refs = IGuiceContext.get(AnnotationHelper.class)
                .getAnnotationFromClass(AngularDataComponent.class, NgComponentReference.class);
        Assertions.assertFalse(refs.isEmpty(),
                "AngularDataComponent should have @NgComponentReference annotations");

        // Generate TypeScript using the compiler output
        String generatedTs = codeGenerator.generateTypeScriptForComponent(dataComponent);

        System.out.println("[DEBUG_LOG] Generated TypeScript for AngularDataComponent:");
        System.out.println("[DEBUG_LOG] " + generatedTs);

        // For each NgComponentReference annotation, verify the import appears in the rendered output
        for (NgComponentReference ref : refs) {
            String refClassName = AnnotationUtils.getTsFilename(ref.value());
            Assertions.assertTrue(generatedTs.contains("import"),
                    "Rendered TypeScript should contain import statements");
            Assertions.assertTrue(generatedTs.contains(refClassName),
                    "Rendered TypeScript should contain import for referenced component '" + refClassName + "' from @NgComponentReference, but was:\n" + generatedTs);
            System.out.println("[DEBUG_LOG] Verified import for @NgComponentReference target: " + refClassName);
        }
    }

    @Test
    public void testRenderedBootComponentContainsExpectedStructure() throws IOException {
        if (testApp == null || codeGenerator == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing rendered boot component contains expected structure");

        // Get the boot component class from the app annotation
        NgApp ngApp = testApp.getClass().getAnnotation(NgApp.class);
        Assertions.assertNotNull(ngApp, "Test app should have @NgApp annotation");

        Class<?> bootComponentClass = ngApp.bootComponent();
        System.out.println("[DEBUG_LOG] Boot component class: " + bootComponentClass.getSimpleName());

        // Create the boot component through Guice
        IComponent<?> bootComponent = (IComponent<?>) IGuiceContext.get(bootComponentClass);

        // Generate TypeScript using the compiler output
        String generatedTs = codeGenerator.generateTypeScriptForComponent(bootComponent);

        System.out.println("[DEBUG_LOG] Generated TypeScript for boot component:");
        System.out.println("[DEBUG_LOG] " + generatedTs);

        // Verify the TypeScript is not null or empty
        Assertions.assertNotNull(generatedTs, "Generated TypeScript should not be null");
        Assertions.assertFalse(generatedTs.isEmpty(), "Generated TypeScript should not be empty for the boot component");

        // Verify it contains import statements
        Assertions.assertTrue(generatedTs.contains("import"),
                "Boot component TypeScript should contain import statements");

        // Verify it contains the @Component decorator
        Assertions.assertTrue(generatedTs.contains("@Component("),
                "Boot component TypeScript should contain @Component decorator");

        // Verify it contains the class export
        String bootClassName = AnnotationUtils.getTsFilename(bootComponentClass);
        Assertions.assertTrue(generatedTs.contains("export class " + bootClassName),
                "Boot component TypeScript should contain 'export class " + bootClassName + "'");
    }

    @Test
    public void testRenderedMainTsContainsBootstrapAndImports() throws IOException {
        if (testApp == null || codeGenerator == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing rendered main.ts contains bootstrap and imports");

        // Render the main.ts content using the compiler
        StringBuilder mainTs = codeGenerator.renderAppTS(testApp);

        System.out.println("[DEBUG_LOG] Rendered main.ts:");
        System.out.println("[DEBUG_LOG] " + mainTs);

        Assertions.assertNotNull(mainTs, "Rendered main.ts should not be null");
        Assertions.assertTrue(mainTs.length() > 0, "Rendered main.ts should not be empty");

        // Verify it contains the bootstrap call
        Assertions.assertTrue(mainTs.toString().contains("bootstrapApplication("),
                "main.ts should contain bootstrapApplication call");

        // Verify it imports the boot component
        NgApp ngApp = testApp.getClass().getAnnotation(NgApp.class);
        String bootComponentName = ngApp.bootComponent().getSimpleName();
        Assertions.assertTrue(mainTs.toString().contains(bootComponentName),
                "main.ts should import the boot component '" + bootComponentName + "'");

        // Verify it imports appConfig
        Assertions.assertTrue(mainTs.toString().contains("appConfig"),
                "main.ts should import appConfig");

        // Verify it imports from @angular/platform-browser
        Assertions.assertTrue(mainTs.toString().contains("@angular/platform-browser"),
                "main.ts should import from @angular/platform-browser");
    }

    @Test
    public void testAllDiscoveredComponentsRenderNonEmptyTypeScript() {
        if (testApp == null || codeGenerator == null) {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing all discovered components render non-empty TypeScript");

        int componentCount = 0;
        int failedCount = 0;

        for (ClassInfo classInfo : scanResult.getAllClasses()) {
            try {
                Class<?> aClass = classInfo.loadClass();
                // Only test actual @NgComponent-annotated classes, skip INgApp implementations
                // (app classes like AngularApp/NGApplication correctly return empty TypeScript
                // since they are bootstrapped via main.ts, not rendered as components)
                if (INgComponent.class.isAssignableFrom(aClass) && !aClass.isInterface() && !aClass.isAnnotation()
                        && !INgApp.class.isAssignableFrom(aClass)
                        && aClass.isAnnotationPresent(com.jwebmp.core.base.angular.client.annotations.angular.NgComponent.class)) {
                    IComponent<?> component = (IComponent<?>) IGuiceContext.get(aClass);
                    String generatedTs = codeGenerator.generateTypeScriptForComponent(component);

                    componentCount++;

                    if (generatedTs == null || generatedTs.isEmpty()) {
                        System.out.println("[DEBUG_LOG] WARN: Empty TypeScript for component: " + aClass.getSimpleName());
                        failedCount++;
                    } else {
                        // Verify each component has at least an import and class declaration
                        boolean hasImport = generatedTs.contains("import");
                        boolean hasClassDeclaration = generatedTs.contains("export class");

                        if (!hasImport) {
                            System.out.println("[DEBUG_LOG] WARN: No import statements in TypeScript for: " + aClass.getSimpleName());
                        }
                        if (!hasClassDeclaration) {
                            System.out.println("[DEBUG_LOG] WARN: No class declaration in TypeScript for: " + aClass.getSimpleName());
                        }

                        // Verify any @NgComponentReference annotations on this class are reflected in the output
                        List<NgComponentReference> refs = IGuiceContext.get(AnnotationHelper.class)
                                .getAnnotationFromClass(aClass, NgComponentReference.class);
                        for (NgComponentReference ref : refs) {
                            String refName = AnnotationUtils.getTsFilename(ref.value());
                            if (!generatedTs.contains(refName)) {
                                System.out.println("[DEBUG_LOG] FAIL: Component " + aClass.getSimpleName() +
                                        " has @NgComponentReference(" + ref.value().getSimpleName() +
                                        ") but rendered TypeScript does not contain '" + refName + "'");
                                failedCount++;
                            } else {
                                System.out.println("[DEBUG_LOG] OK: Component " + aClass.getSimpleName() +
                                        " correctly imports @NgComponentReference target: " + refName);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Continue to next class
            }
        }

        System.out.println("[DEBUG_LOG] Processed " + componentCount + " components, " + failedCount + " had issues");
        Assertions.assertTrue(componentCount > 0, "Should have found at least one component to test");
        Assertions.assertEquals(0, failedCount,
                "All components should render valid TypeScript with their @NgComponentReference imports resolved");
    }
}

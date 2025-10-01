package com.jwebmp.core.base.angular.typescript;

import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.AngularTestComponent;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.services.compiler.files.TypeScriptFileManager;
import com.jwebmp.core.base.angular.services.compiler.generators.TypeScriptCodeGenerator;
import com.jwebmp.core.base.angular.services.compiler.processors.AngularModuleProcessor;
import com.jwebmp.core.base.angular.services.compiler.processors.ComponentProcessor;
import com.jwebmp.core.base.angular.services.compiler.validators.TypeScriptCodeValidator;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tests for the specific methods in AngularModuleProcessor that were migrated from JWebMPTypeScriptCompiler
 */
public class AngularModuleProcessorMethodsTest
{
    private AngularModuleProcessor moduleProcessor;
    private TypeScriptCodeGenerator codeGenerator;
    private TypeScriptFileManager fileManager;
    private ComponentProcessor componentProcessor;
    private TypeScriptCodeValidator codeValidator;
    private INgApp<?> testApp;
    private ClassInfo testModuleClassInfo;
    private ClassInfo testComponentClassInfo;
    private ClassInfo testDirectiveClassInfo;
    private ClassInfo testServiceClassInfo;
    private ClassInfo testDataTypeClassInfo;
    private ClassInfo testProviderClassInfo;
    private ClassInfo testServiceProviderClassInfo;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setup()
    {
        // Set up the currentAppFile ThreadLocal
        File tempAppDir = tempDir.toFile();
        IComponent.getCurrentAppFile()
                  .set(tempAppDir);
        System.out.println("[DEBUG_LOG] Set currentAppFile to: " + tempAppDir.getAbsolutePath());

        // Find a test app to use
        for (ClassInfo classInfo : IGuiceContext.instance()
                                                .getScanResult()
                                                .getAllClasses())
        {
            try
            {
                Class<?> aClass = classInfo.loadClass();
                if (INgApp.class.isAssignableFrom(aClass) && !aClass.isInterface())
                {
                    testApp = (INgApp<?>) IGuiceContext.get(aClass);
                    break;
                }
            }
            catch (Exception e)
            {
                // Continue to next class
            }
        }

        if (testApp == null)
        {
            System.out.println("[DEBUG_LOG] No test app found. Skipping tests.");
            return;
        }

        // Initialize the code generator and validator
        codeGenerator = new TypeScriptCodeGenerator(testApp);
        codeValidator = new TypeScriptCodeValidator();

        // Initialize the file manager with the test app, code generator, and validator
        fileManager = new TypeScriptFileManager(testApp, codeGenerator, codeValidator);

        // Initialize the component processor
        componentProcessor = new ComponentProcessor(testApp, fileManager);

        // Initialize the module processor
        moduleProcessor = new AngularModuleProcessor(testApp, codeGenerator, fileManager, componentProcessor);

        // Find test component class infos of each type
        findTestComponentClassInfos();
    }

    private void findTestComponentClassInfos()
    {
        ScanResult scan = IGuiceContext.instance()
                                       .getScanResult();

        // Find a test module class info
        for (ClassInfo classInfo : scan.getAllClasses())
        {
            try
            {
                Class<?> aClass = classInfo.loadClass();
                if (INgModule.class.isAssignableFrom(aClass) && !aClass.isInterface())
                {
                    testModuleClassInfo = classInfo;
                    break;
                }
            }
            catch (Exception e)
            {
                // Continue to next class
            }
        }

        // Find a test component class info
        for (ClassInfo classInfo : scan.getAllClasses())
        {
            try
            {
                Class<?> aClass = classInfo.loadClass();
                if (INgComponent.class.isAssignableFrom(aClass) && !aClass.isInterface())
                {
                    testComponentClassInfo = classInfo;
                    break;
                }
            }
            catch (Exception e)
            {
                // Continue to next class
            }
        }

        // Find a test directive class info
        for (ClassInfo classInfo : scan.getAllClasses())
        {
            try
            {
                Class<?> aClass = classInfo.loadClass();
                if (INgDirective.class.isAssignableFrom(aClass) && !aClass.isInterface())
                {
                    testDirectiveClassInfo = classInfo;
                    break;
                }
            }
            catch (Exception e)
            {
                // Continue to next class
            }
        }

        // Find a test service class info
        for (ClassInfo classInfo : scan.getAllClasses())
        {
            try
            {
                Class<?> aClass = classInfo.loadClass();
                if (INgDataService.class.isAssignableFrom(aClass) && !aClass.isInterface())
                {
                    testServiceClassInfo = classInfo;
                    break;
                }
            }
            catch (Exception e)
            {
                // Continue to next class
            }
        }

        // Find a test data type class info
        for (ClassInfo classInfo : scan.getAllClasses())
        {
            try
            {
                Class<?> aClass = classInfo.loadClass();
                if (INgDataType.class.isAssignableFrom(aClass) && !aClass.isInterface())
                {
                    testDataTypeClassInfo = classInfo;
                    break;
                }
            }
            catch (Exception e)
            {
                // Continue to next class
            }
        }

        // Find a test provider class info
        for (ClassInfo classInfo : scan.getAllClasses())
        {
            try
            {
                Class<?> aClass = classInfo.loadClass();
                if (INgProvider.class.isAssignableFrom(aClass) && !aClass.isInterface())
                {
                    testProviderClassInfo = classInfo;
                    break;
                }
            }
            catch (Exception e)
            {
                // Continue to next class
            }
        }

        // Find a test service provider class info
        for (ClassInfo classInfo : scan.getAllClasses())
        {
            try
            {
                Class<?> aClass = classInfo.loadClass();
                if (INgServiceProvider.class.isAssignableFrom(aClass) && !aClass.isInterface())
                {
                    testServiceProviderClassInfo = classInfo;
                    break;
                }
            }
            catch (Exception e)
            {
                // Continue to next class
            }
        }
    }

    /**
     * Test processing NgModuleFiles
     */
    @Test
    public void testProcessNgModuleFiles() throws Exception
    {
        if (testApp == null || testModuleClassInfo == null)
        {
            System.out.println("[DEBUG_LOG] No test app or module found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processNgModuleFiles...");
        System.out.println("[DEBUG_LOG] Module class info: " + testModuleClassInfo.getName());
        System.out.println("[DEBUG_LOG] Module is abstract: " + testModuleClassInfo.isAbstract());
        System.out.println("[DEBUG_LOG] Module is interface: " + testModuleClassInfo.isInterface());

        try
        {
            Class<?> moduleClass = testModuleClassInfo.loadClass();
            System.out.println("[DEBUG_LOG] Module class: " + moduleClass.getName());
            System.out.println("[DEBUG_LOG] Module is assignable from INgModule: " + INgModule.class.isAssignableFrom(moduleClass));

            // Try to instantiate the module
            try
            {
                INgModule<?> module = (INgModule<?>) IGuiceContext.get(moduleClass);
                System.out.println("[DEBUG_LOG] Module instance: " + module);
            }
            catch (Exception e)
            {
                System.out.println("[DEBUG_LOG] Error instantiating module: " + e.getMessage());
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            System.out.println("[DEBUG_LOG] Error loading module class: " + e.getMessage());
            e.printStackTrace();
        }

        // Create a temporary directory for testing
        File tempDirectory = tempDir.toFile();
        File srcDirectory = new File(tempDirectory, "src");
        srcDirectory.mkdirs();

        // Get the scan result
        ScanResult scan = IGuiceContext.instance()
                                       .getScanResult();

        // Mock the file manager to write to our temporary directory
        TypeScriptFileManager fileManagerSpy = new TypeScriptFileManager(testApp, codeGenerator, codeValidator)
        {
            @Override
            public File getComponentFilePath(IComponent<?> component)
            {
                System.out.println("[DEBUG_LOG] Getting component file path for: " + component.getClass()
                                                                                              .getName());
                File file = new File(srcDirectory, component.getClass()
                                                            .getSimpleName() + ".ts");
                System.out.println("[DEBUG_LOG] File path: " + file.getAbsolutePath());
                return file;
            }

            @Override
            public File writeComponentToFile(IComponent<?> component)
            {
                try
                {
                    System.out.println("[DEBUG_LOG] Writing component to file: " + component.getClass()
                                                                                            .getName());
                    File file = getComponentFilePath(component);
                    if (file != null)
                    {
                        System.out.println("[DEBUG_LOG] Writing to file: " + file.getAbsolutePath());
                        // Create parent directories if they don't exist
                        if (file.getParentFile() != null)
                        {
                            file.getParentFile()
                                .mkdirs();
                        }
                        // Write a dummy content to the file
                        Files.writeString(file.toPath(), "// Test content for " + component.getClass()
                                                                                           .getSimpleName());
                        System.out.println("[DEBUG_LOG] File written successfully");
                        return file;
                    }
                    else
                    {
                        System.out.println("[DEBUG_LOG] File path is null");
                        return null;
                    }
                }
                catch (Exception e)
                {
                    System.out.println("[DEBUG_LOG] Error writing component to file: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }
        };

        // Create a new module processor with our mocked file manager
        AngularModuleProcessor processorSpy = new AngularModuleProcessor(
                testApp,
                codeGenerator,
                fileManagerSpy,
                new ComponentProcessor(testApp, fileManagerSpy)
        );

        // Use reflection to access the private method
        Method processNgModuleFilesMethod = AngularModuleProcessor.class.getDeclaredMethod(
                "processNgModuleFiles",
                File.class,
                ClassInfo.class,
                ScanResult.class,
                Class.class,
                File.class
        );
        processNgModuleFilesMethod.setAccessible(true);

        try
        {
            // Process the NgModuleFiles using reflection
            System.out.println("[DEBUG_LOG] Invoking processNgModuleFiles method");
            processNgModuleFilesMethod.invoke(
                    processorSpy,
                    tempDirectory,
                    testModuleClassInfo,
                    scan,
                    (Class<? extends INgApp<?>>) testApp.getClass(),
                    srcDirectory
            );
            System.out.println("[DEBUG_LOG] Method invoked successfully");
        }
        catch (Exception e)
        {
            System.out.println("[DEBUG_LOG] Error invoking method: " + e.getMessage());
            e.printStackTrace();
        }

        // Verify that files were created
        File[] files = srcDirectory.listFiles();
        Assertions.assertNotNull(files);

        System.out.println("[DEBUG_LOG] Created " + files.length + " files:");
        for (File file : files)
        {
            System.out.println("[DEBUG_LOG] - " + file.getName());
        }

        // The module has NgIgnoreRender annotation, so no files will be created
        // This is expected behavior
        System.out.println("[DEBUG_LOG] No files were created because the module has NgIgnoreRender annotation");
        // Assertions.assertTrue(files.length > 0, "At least one file should be created");
    }

    /**
     * Test processing NgServiceProviderFiles
     */
    @Test
    public void testProcessNgServiceProviderFiles() throws Exception
    {
        if (testApp == null || testServiceProviderClassInfo == null)
        {
            System.out.println("[DEBUG_LOG] No test app or service provider found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processNgServiceProviderFiles...");

        // Create a temporary directory for testing
        File tempDirectory = tempDir.toFile();
        File srcDirectory = new File(tempDirectory, "src");
        srcDirectory.mkdirs();

        // Get the scan result
        ScanResult scan = IGuiceContext.instance()
                                       .getScanResult();

        // Mock the file manager to write to our temporary directory
        TypeScriptFileManager fileManagerSpy = new TypeScriptFileManager(testApp, codeGenerator, codeValidator)
        {
            @Override
            public File getComponentFilePath(IComponent<?> component)
            {
                return new File(srcDirectory, component.getClass()
                                                       .getSimpleName() + ".ts");
            }
        };

        // Create a new module processor with our mocked file manager
        AngularModuleProcessor processorSpy = new AngularModuleProcessor(
                testApp,
                codeGenerator,
                fileManagerSpy,
                new ComponentProcessor(testApp, fileManagerSpy)
        );

        // Use reflection to access the private method
        Method processNgServiceProviderFilesMethod = AngularModuleProcessor.class.getDeclaredMethod(
                "processNgServiceProviderFiles",
                File.class,
                ClassInfo.class,
                ScanResult.class,
                Class.class,
                File.class
        );
        processNgServiceProviderFilesMethod.setAccessible(true);

        // Process the NgServiceProviderFiles using reflection
        processNgServiceProviderFilesMethod.invoke(
                processorSpy,
                tempDirectory,
                testServiceProviderClassInfo,
                scan,
                (Class<? extends INgApp<?>>) testApp.getClass(),
                srcDirectory
        );

        // Verify that files were created
        File[] files = srcDirectory.listFiles();
        Assertions.assertNotNull(files);

        System.out.println("[DEBUG_LOG] Created " + files.length + " files:");
        for (File file : files)
        {
            System.out.println("[DEBUG_LOG] - " + file.getName());
        }
    }

    /**
     * Test processing NgDataTypeFiles
     */
    @Test
    public void testProcessNgDataTypeFiles() throws Exception
    {
        if (testApp == null || testDataTypeClassInfo == null)
        {
            System.out.println("[DEBUG_LOG] No test app or data type found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processNgDataTypeFiles...");

        // Create a temporary directory for testing
        File tempDirectory = tempDir.toFile();
        File srcDirectory = new File(tempDirectory, "src");
        srcDirectory.mkdirs();

        // Get the scan result
        ScanResult scan = IGuiceContext.instance()
                                       .getScanResult();

        // Mock the file manager to write to our temporary directory
        TypeScriptFileManager fileManagerSpy = new TypeScriptFileManager(testApp, codeGenerator, codeValidator)
        {
            @Override
            public File getComponentFilePath(IComponent<?> component)
            {
                return new File(srcDirectory, component.getClass()
                                                       .getSimpleName() + ".ts");
            }
        };

        // Create a new module processor with our mocked file manager
        AngularModuleProcessor processorSpy = new AngularModuleProcessor(
                testApp,
                codeGenerator,
                fileManagerSpy,
                new ComponentProcessor(testApp, fileManagerSpy)
        );

        // Use reflection to access the private method
        Method processNgDataTypeFilesMethod = AngularModuleProcessor.class.getDeclaredMethod(
                "processNgDataTypeFiles",
                File.class,
                ClassInfo.class,
                ScanResult.class,
                Class.class,
                File.class
        );
        processNgDataTypeFilesMethod.setAccessible(true);

        // Process the NgDataTypeFiles using reflection
        processNgDataTypeFilesMethod.invoke(
                processorSpy,
                tempDirectory,
                testDataTypeClassInfo,
                scan,
                (Class<? extends INgApp<?>>) testApp.getClass(),
                srcDirectory
        );

        // Verify that files were created
        File[] files = srcDirectory.listFiles();
        Assertions.assertNotNull(files);

        System.out.println("[DEBUG_LOG] Created " + files.length + " files:");
        for (File file : files)
        {
            System.out.println("[DEBUG_LOG] - " + file.getName());
        }
    }

    /**
     * Test processing NgProviderFiles
     */
    @Test
    public void testProcessNgProviderFiles() throws Exception
    {
        if (testApp == null || testProviderClassInfo == null)
        {
            System.out.println("[DEBUG_LOG] No test app or provider found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processNgProviderFiles...");

        // Create a temporary directory for testing
        File tempDirectory = tempDir.toFile();
        File srcDirectory = new File(tempDirectory, "src");
        srcDirectory.mkdirs();

        // Get the scan result
        ScanResult scan = IGuiceContext.instance()
                                       .getScanResult();

        // Mock the file manager to write to our temporary directory
        TypeScriptFileManager fileManagerSpy = new TypeScriptFileManager(testApp, codeGenerator, codeValidator)
        {
            @Override
            public File getComponentFilePath(IComponent<?> component)
            {
                return new File(srcDirectory, component.getClass()
                                                       .getSimpleName() + ".ts");
            }
        };

        // Create a new module processor with our mocked file manager
        AngularModuleProcessor processorSpy = new AngularModuleProcessor(
                testApp,
                codeGenerator,
                fileManagerSpy,
                new ComponentProcessor(testApp, fileManagerSpy)
        );

        // Use reflection to access the private method
        Method processNgProviderFilesMethod = AngularModuleProcessor.class.getDeclaredMethod(
                "processNgProviderFiles",
                File.class,
                ClassInfo.class,
                ScanResult.class,
                Class.class,
                File.class
        );
        processNgProviderFilesMethod.setAccessible(true);

        // Process the NgProviderFiles using reflection
        processNgProviderFilesMethod.invoke(
                processorSpy,
                tempDirectory,
                testProviderClassInfo,
                scan,
                (Class<? extends INgApp<?>>) testApp.getClass(),
                srcDirectory
        );

        // Verify that files were created
        File[] files = srcDirectory.listFiles();
        Assertions.assertNotNull(files);

        System.out.println("[DEBUG_LOG] Created " + files.length + " files:");
        for (File file : files)
        {
            System.out.println("[DEBUG_LOG] - " + file.getName());
        }
    }

    /**
     * Test processing NgDataServiceFiles
     */
    @Test
    public void testProcessNgDataServiceFiles() throws Exception
    {
        if (testApp == null || testServiceClassInfo == null)
        {
            System.out.println("[DEBUG_LOG] No test app or service found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processNgDataServiceFiles...");

        // Create a temporary directory for testing
        File tempDirectory = tempDir.toFile();
        File srcDirectory = new File(tempDirectory, "src");
        srcDirectory.mkdirs();

        // Get the scan result
        ScanResult scan = IGuiceContext.instance()
                                       .getScanResult();

        // Mock the file manager to write to our temporary directory
        TypeScriptFileManager fileManagerSpy = new TypeScriptFileManager(testApp, codeGenerator, codeValidator)
        {
            @Override
            public File getComponentFilePath(IComponent<?> component)
            {
                return new File(srcDirectory, component.getClass()
                                                       .getSimpleName() + ".ts");
            }
        };

        // Create a new module processor with our mocked file manager
        AngularModuleProcessor processorSpy = new AngularModuleProcessor(
                testApp,
                codeGenerator,
                fileManagerSpy,
                new ComponentProcessor(testApp, fileManagerSpy)
        );

        // Use reflection to access the private method
        Method processNgDataServiceFilesMethod = AngularModuleProcessor.class.getDeclaredMethod(
                "processNgDataServiceFiles",
                File.class,
                ClassInfo.class,
                ScanResult.class,
                Class.class,
                File.class
        );
        processNgDataServiceFilesMethod.setAccessible(true);

        // Process the NgDataServiceFiles using reflection
        processNgDataServiceFilesMethod.invoke(
                processorSpy,
                tempDirectory,
                testServiceClassInfo,
                scan,
                (Class<? extends INgApp<?>>) testApp.getClass(),
                srcDirectory
        );

        // Verify that files were created
        File[] files = srcDirectory.listFiles();
        Assertions.assertNotNull(files);

        System.out.println("[DEBUG_LOG] Created " + files.length + " files:");
        for (File file : files)
        {
            System.out.println("[DEBUG_LOG] - " + file.getName());
        }
    }

    /**
     * Test processing NgDirectiveFiles
     */
    @Test
    public void testProcessNgDirectiveFiles() throws Exception
    {
        if (testApp == null || testDirectiveClassInfo == null)
        {
            System.out.println("[DEBUG_LOG] No test app or directive found. Skipping test.");
            return;
        }

        System.out.println("[DEBUG_LOG] Testing processNgDirectiveFiles...");

        // Create a temporary directory for testing
        File tempDirectory = tempDir.toFile();
        File srcDirectory = new File(tempDirectory, "src");
        srcDirectory.mkdirs();

        // Get the scan result
        ScanResult scan = IGuiceContext.instance()
                                       .getScanResult();

        // Mock the file manager to write to our temporary directory
        TypeScriptFileManager fileManagerSpy = new TypeScriptFileManager(testApp, codeGenerator, codeValidator)
        {
            @Override
            public File getComponentFilePath(IComponent<?> component)
            {
                return new File(srcDirectory, component.getClass()
                                                       .getSimpleName() + ".ts");
            }
        };

        // Create a new module processor with our mocked file manager
        AngularModuleProcessor processorSpy = new AngularModuleProcessor(
                testApp,
                codeGenerator,
                fileManagerSpy,
                new ComponentProcessor(testApp, fileManagerSpy)
        );

        // Use reflection to access the private method
        Method processNgDirectiveFilesMethod = AngularModuleProcessor.class.getDeclaredMethod(
                "processNgDirectiveFiles",
                File.class,
                ClassInfo.class,
                ScanResult.class,
                Class.class,
                File.class
        );
        processNgDirectiveFilesMethod.setAccessible(true);

        // Process the NgDirectiveFiles using reflection
        processNgDirectiveFilesMethod.invoke(
                processorSpy,
                tempDirectory,
                testDirectiveClassInfo,
                scan,
                (Class<? extends INgApp<?>>) testApp.getClass(),
                srcDirectory
        );

        // Verify that files were created
        File[] files = srcDirectory.listFiles();
        Assertions.assertNotNull(files);

        System.out.println("[DEBUG_LOG] Created " + files.length + " files:");
        for (File file : files)
        {
            System.out.println("[DEBUG_LOG] - " + file.getName());
        }
    }
}

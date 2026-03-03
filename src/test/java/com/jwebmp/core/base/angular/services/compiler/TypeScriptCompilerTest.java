package com.jwebmp.core.base.angular.services.compiler;
import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.ProductDetail;
import com.jwebmp.core.base.angular.client.AppUtils;
import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.client.services.interfaces.IComponent;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import com.jwebmp.core.base.angular.modules.directives.OnClickListenerDirective;
import com.jwebmp.core.base.angular.services.NGApplication;
import com.jwebmp.core.base.angular.services.compiler.files.TypeScriptFileManager;
import com.jwebmp.core.base.angular.services.compiler.generators.TypeScriptCodeGenerator;
import com.jwebmp.core.base.angular.services.compiler.validators.TypeScriptCodeValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
public class TypeScriptCompilerTest
{
    private TypeScriptCodeGenerator codeGenerator;
    private TypeScriptFileManager fileManager;
    private INgApp<?> app;
    // Mock app class with NgApp annotation for testing
    @NgApp(value = "test-app", bootComponent = ProductDetail.class)
    public static class TestApp extends NGApplication<TestApp>
    {
        public TestApp()
        {
            super();
        }
    }
    @BeforeAll
    public static void initGuice()
    {
        IGuiceContext.instance().inject();
    }
    @BeforeEach
    public void setup()
    {
        // Use the first available app from the scan, or create a test one
        var allApps = JWebMPTypeScriptCompiler.getAllApps();
        if (!allApps.isEmpty())
        {
            app = allApps.iterator().next();
        }
        else
        {
            app = IGuiceContext.get(TestApp.class);
        }
        IComponent.app.set(app);
        File appPath = AppUtils.getAppPath((Class<? extends INgApp<?>>) app.getClass());
        IComponent.getCurrentAppFile().set(appPath);
        codeGenerator = new TypeScriptCodeGenerator(app);
        TypeScriptCodeValidator codeValidator = new TypeScriptCodeValidator();
        fileManager = new TypeScriptFileManager(app, codeGenerator, codeValidator);
    }
    @Test
    public void testProductDetailComponent() throws IOException
    {
        // Create the ProductDetail component through Guice so @Inject methods are called
        ProductDetail productDetail = IGuiceContext.get(ProductDetail.class);
        IComponent.app.set(app);
        // Generate TypeScript for the component
        String generatedTs = codeGenerator.generateTypeScriptForComponent(productDetail);
        // Print the generated TypeScript for debugging
        System.out.println("[DEBUG_LOG] Generated TypeScript for ProductDetail:");
        System.out.println("[DEBUG_LOG] " + generatedTs);
        // Verify the TypeScript is not null or empty
        assertNotNull(generatedTs);
        assertFalse(generatedTs.isEmpty(), "Generated TypeScript should not be empty");
        // Verify it contains the expected component decorator
        assertTrue(generatedTs.contains("@Component("), "Should contain @Component decorator");
        assertTrue(generatedTs.contains("selector:'product-detail'"), "Should contain the selector");
        assertTrue(generatedTs.contains("export class ProductDetail"), "Should contain the class declaration");
        // Verify it contains import statements (the key fix)
        assertTrue(generatedTs.contains("import"), "Should contain import statements");
        assertTrue(generatedTs.contains("Component"), "Should import Component from @angular/core");
        // Write the component to a file
        File file = fileManager.writeComponentToFile(productDetail, true);
        assertNotNull(file, "Component file should not be null");
        assertTrue(file.exists(), "Component file was not created");
    }
    @Test
    public void testOnClickListenerDirective() throws IOException
    {
        // Create the OnClickListenerDirective through Guice
        OnClickListenerDirective directive = IGuiceContext.get(OnClickListenerDirective.class);
        IComponent.app.set(app);
        // Generate TypeScript for the directive
        String generatedTs = codeGenerator.generateTypeScriptForComponent(directive);
        // Print the generated TypeScript for debugging
        System.out.println("[DEBUG_LOG] Generated TypeScript for OnClickListenerDirective:");
        System.out.println("[DEBUG_LOG] " + generatedTs);
        // Verify the TypeScript is not null or empty
        assertNotNull(generatedTs);
        assertFalse(generatedTs.isEmpty(), "Generated TypeScript should not be empty");
        // Verify it contains import statements
        assertTrue(generatedTs.contains("import"), "Should contain import statements");
        assertTrue(generatedTs.contains("Directive"), "Should import Directive");
        assertTrue(generatedTs.contains("@Directive("), "Should contain @Directive decorator");
        assertTrue(generatedTs.contains("selector:'[clickClassName]'"), "Should contain the selector");
        assertTrue(generatedTs.contains("export class OnClickListenerDirective"), "Should contain the class declaration");
        // Write the directive to a file
        File file = fileManager.writeComponentToFile(directive, true);
        assertNotNull(file, "Directive file should not be null");
        assertTrue(file.exists(), "Directive file was not created");
    }
}

package com.jwebmp.core.base.angular.onparent;

import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.AppUtils;
import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.client.services.AnnotationHelper;
import com.jwebmp.core.base.angular.client.services.interfaces.IComponent;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import com.jwebmp.core.base.angular.services.compiler.generators.TypeScriptCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that onParent=true annotations on referenced classes propagate correctly
 * to the referencing component's rendered TypeScript output.
 */
public class OnParentPropagationTest
{
    private INgApp<?> testApp;
    private TypeScriptCodeGenerator codeGenerator;

    @BeforeEach
    public void setup()
    {
        var scanResult = IGuiceContext.instance().getScanResult();
        for (var classInfo : scanResult.getAllClasses())
        {
            try
            {
                Class<?> aClass = classInfo.loadClass();
                if (INgApp.class.isAssignableFrom(aClass) && !aClass.isInterface() &&
                    aClass.isAnnotationPresent(NgApp.class))
                {
                    testApp = (INgApp<?>) IGuiceContext.get(aClass);
                    break;
                }
            }
            catch (Exception e)
            {
                // Continue
            }
        }
        if (testApp != null)
        {
            File appPath = AppUtils.getAppPath((Class<? extends INgApp<?>>) testApp.getClass());
            IComponent.getCurrentAppFile().set(appPath);
            IComponent.app.set(testApp);
            codeGenerator = new TypeScriptCodeGenerator(testApp);
        }

        // Ensure AnnotationHelper has scanned all our test classes
        AnnotationHelper.startup();
    }

    private String renderComponent(Class<?> componentClass)
    {
        var component = (IComponent<?>) IGuiceContext.get(componentClass);
        return codeGenerator.generateTypeScriptForComponent(component);
    }

    // ─── Import propagation ────────────────────────────────────────────────────

    @Test
    public void testOnParentImportPropagatesFromDataTypeToComponent()
    {
        if (testApp == null) return;
        String rendered = renderComponent(TestConfigConsumerComponent.class);

        System.out.println("[DEBUG_LOG] TestConfigConsumerComponent rendered:\n" + rendered);

        // The onParent import "inject" from TestAppConfig should appear
        assertTrue(rendered.contains("inject"),
                "Should contain 'inject' import propagated from TestAppConfig onParent=true, but was:\n" + rendered);
        // The onParent field from TestAppConfig should appear
        assertTrue(rendered.contains("appConfig: TestAppConfig = inject(TestAppConfig)"),
                "Should contain onParent field from TestAppConfig, but was:\n" + rendered);
    }

    @Test
    public void testOnSelfFalseDoesNotAppearOnReferencedClassOwnOutput()
    {
        if (testApp == null) return;
        String rendered = renderComponent(TestAppConfig.class);

        System.out.println("[DEBUG_LOG] TestAppConfig rendered:\n" + rendered);

        // The onSelf=false field should NOT appear on TestAppConfig itself
        assertFalse(rendered.contains("appConfig: TestAppConfig = inject(TestAppConfig)"),
                "onSelf=false field should NOT appear on its own class, but was:\n" + rendered);
    }

    // ─── Field propagation ─────────────────────────────────────────────────────

    @Test
    public void testOnParentFieldPropagatesFromProviderToComponent()
    {
        if (testApp == null) return;
        String rendered = renderComponent(TestProviderConsumerComponent.class);

        System.out.println("[DEBUG_LOG] TestProviderConsumerComponent rendered:\n" + rendered);

        assertTrue(rendered.contains("testProvider: TestParentProvider = inject(TestParentProvider)"),
                "Should contain onParent field from TestParentProvider, but was:\n" + rendered);
    }

    // ─── Signal propagation ────────────────────────────────────────────────────

    @Test
    public void testOnParentSignalPropagatesFromProviderToComponent()
    {
        if (testApp == null) return;
        String rendered = renderComponent(TestProviderConsumerComponent.class);

        System.out.println("[DEBUG_LOG] TestProviderConsumerComponent signals:\n" + rendered);

        assertTrue(rendered.contains("providerReady"),
                "Should contain onParent signal 'providerReady' from TestParentProvider, but was:\n" + rendered);
    }

    // ─── Method propagation ────────────────────────────────────────────────────

    @Test
    public void testOnParentMethodPropagatesFromProviderToComponent()
    {
        if (testApp == null) return;
        String rendered = renderComponent(TestProviderConsumerComponent.class);

        System.out.println("[DEBUG_LOG] TestProviderConsumerComponent methods:\n" + rendered);

        assertTrue(rendered.contains("refreshProvider()"),
                "Should contain onParent method 'refreshProvider' from TestParentProvider, but was:\n" + rendered);
    }

    // ─── Constructor parameter propagation ─────────────────────────────────────

    @Test
    public void testOnParentConstructorParamPropagatesFromProviderToComponent()
    {
        if (testApp == null) return;
        String rendered = renderComponent(TestProviderConsumerComponent.class);

        System.out.println("[DEBUG_LOG] TestProviderConsumerComponent constructor:\n" + rendered);

        assertTrue(rendered.contains("providerParam: TestParentProvider"),
                "Should contain onParent constructor param from TestParentProvider, but was:\n" + rendered);
    }

    // ─── Constructor body propagation ──────────────────────────────────────────

    @Test
    public void testOnParentConstructorBodyPropagatesFromDirectiveToComponent()
    {
        if (testApp == null) return;
        String rendered = renderComponent(TestDirectiveConsumerComponent.class);

        System.out.println("[DEBUG_LOG] TestDirectiveConsumerComponent rendered:\n" + rendered);

        assertTrue(rendered.contains("highlight directive initialized"),
                "Should contain onParent constructor body from TestHighlightDirective, but was:\n" + rendered);
    }

    // ─── Directive import propagation ──────────────────────────────────────────

    @Test
    public void testOnParentImportPropagatesFromDirectiveToComponent()
    {
        if (testApp == null) return;
        String rendered = renderComponent(TestDirectiveConsumerComponent.class);

        System.out.println("[DEBUG_LOG] TestDirectiveConsumerComponent imports:\n" + rendered);

        assertTrue(rendered.contains("Renderer2"),
                "Should contain onParent import 'Renderer2' from TestHighlightDirective, but was:\n" + rendered);
    }

    // ─── Multiple references ───────────────────────────────────────────────────

    @Test
    public void testMultipleReferencesAllPropagateOnParent()
    {
        if (testApp == null) return;
        String rendered = renderComponent(TestMultiRefConsumerComponent.class);

        System.out.println("[DEBUG_LOG] TestMultiRefConsumerComponent rendered:\n" + rendered);

        // From TestAppConfig
        assertTrue(rendered.contains("appConfig: TestAppConfig = inject(TestAppConfig)"),
                "Should contain onParent field from TestAppConfig, but was:\n" + rendered);

        // From TestParentProvider
        assertTrue(rendered.contains("testProvider: TestParentProvider = inject(TestParentProvider)"),
                "Should contain onParent field from TestParentProvider, but was:\n" + rendered);

        // From TestHighlightDirective
        assertTrue(rendered.contains("highlightActive"),
                "Should contain onParent field from TestHighlightDirective, but was:\n" + rendered);

        // Import from TestHighlightDirective
        assertTrue(rendered.contains("Renderer2"),
                "Should contain onParent import 'Renderer2' from TestHighlightDirective, but was:\n" + rendered);
    }

    // ─── onSelf=true stays only on self ────────────────────────────────────────

    @Test
    public void testOnSelfFieldDoesNotPropagateToParent()
    {
        if (testApp == null) return;
        String rendered = renderComponent(TestConfigConsumerComponent.class);

        // The onSelf=true field from TestAppConfig should NOT appear on the consumer
        assertFalse(rendered.contains("private configValue: string = 'default'"),
                "onSelf=true field should NOT propagate to parent component, but was:\n" + rendered);
    }

    @Test
    public void testOnSelfMethodDoesNotPropagateToParent()
    {
        if (testApp == null) return;
        String rendered = renderComponent(TestProviderConsumerComponent.class);

        // The onSelf=true method "getState" should NOT appear on consumer as a standalone method
        assertFalse(rendered.contains("getState(): string"),
                "onSelf=true method should NOT propagate to parent component, but was:\n" + rendered);
    }
}





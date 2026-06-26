package com.jwebmp.core.base.angular.onparent;

import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.AppUtils;
import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.client.services.AnnotationHelper;
import com.jwebmp.core.base.angular.client.services.interfaces.IComponent;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import com.jwebmp.core.base.angular.services.compiler.generators.TypeScriptCodeGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that {@code @NgConstructorParameter} duplicates are collapsed by parameter name when a
 * component pulls the same injectable in from multiple sources (self + multiple
 * {@code @NgComponentReference} parents). A duplicate constructor identifier is invalid TypeScript,
 * so this guards against output like:
 * <pre>constructor( ..., public contextIdService: ContextIdService, ..., public contextIdService : ContextIdService )</pre>
 */
public class ConstructorParameterDeduplicationTest
{
    private static String rendered;
    private static boolean initialized = false;

    @BeforeAll
    public static void setup()
    {
        var scanResult = IGuiceContext.instance()
                                      .getScanResult();
        INgApp<?> testApp = null;
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
            catch (Exception ignored)
            {
            }
        }
        if (testApp == null)
        {
            return;
        }

        File appPath = AppUtils.getAppPath((Class<? extends INgApp<?>>) testApp.getClass());
        IComponent.getCurrentAppFile()
                  .set(appPath);
        IComponent.app.set(testApp);
        AnnotationHelper.startup();

        var codeGenerator = new TypeScriptCodeGenerator(testApp);
        var component = (IComponent<?>) IGuiceContext.get(DuplicateParamConsumerComponent.class);
        rendered = codeGenerator.generateTypeScriptForComponent(component);
        initialized = true;

        System.out.println("[DEBUG_LOG] DuplicateParamConsumerComponent rendered output:");
        System.out.println(rendered);
    }

    @Test
    public void testContextIdServiceParameterDeclaredOnce()
    {
        if (!initialized)
        {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        String constructorParams = extractConstructorParameterList(rendered);
        assertNotNull(constructorParams, "Rendered component should contain a constructor. Output:\n" + rendered);

        int occurrences = countOccurrences(constructorParams, "contextIdService");
        assertEquals(1, occurrences,
                "contextIdService should be declared exactly once in the constructor, but the parameter list was:\n"
                        + constructorParams + "\n\nFull output:\n" + rendered);
    }

    @Test
    public void testPublicVisibilityWinsOverPrivate()
    {
        if (!initialized)
        {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        String constructorParams = extractConstructorParameterList(rendered);
        assertNotNull(constructorParams, "Rendered component should contain a constructor. Output:\n" + rendered);

        // The consumer declares the param `private` while the referenced parents declare it `public`.
        // The most visible declaration must win so the injected member is publicly accessible.
        assertTrue(constructorParams.contains("public contextIdService"),
                "The public contextIdService declaration should win over private. Parameter list:\n"
                        + constructorParams + "\n\nFull output:\n" + rendered);
        assertFalse(constructorParams.contains("private contextIdService"),
                "The private contextIdService declaration should have been replaced by the public one. Parameter list:\n"
                        + constructorParams + "\n\nFull output:\n" + rendered);
    }

    private static String extractConstructorParameterList(String ts)
    {
        Matcher m = Pattern.compile("constructor\\s*\\(([^)]*)\\)", Pattern.DOTALL)
                           .matcher(ts);
        return m.find() ? m.group(1) : null;
    }

    private static int countOccurrences(String haystack, String needle)
    {
        int count = 0;
        int idx = 0;
        while ((idx = haystack.indexOf(needle, idx)) != -1)
        {
            count++;
            idx += needle.length();
        }
        return count;
    }
}



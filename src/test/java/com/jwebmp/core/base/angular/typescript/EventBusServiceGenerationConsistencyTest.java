package com.jwebmp.core.base.angular.typescript;

import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.AppUtils;
import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.client.services.EventBusService;
import com.jwebmp.core.base.angular.client.services.interfaces.IComponent;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import com.jwebmp.core.base.angular.services.compiler.generators.TypeScriptCodeGenerator;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Guards against TypeScript-compilation regressions in generated Angular services.
 *
 * <p>When a JWebMP event is added to a page/component, the {@link EventBusService} is pulled
 * into the generated Angular app. If that service references a member via {@code this.X} that is
 * never declared (e.g. a constructor parameter that was dropped), the downstream
 * {@code ng build} fails with {@code TS2339: Property 'X' does not exist}. That feedback only
 * surfaces after a full Angular build.</p>
 *
 * <p>This test reproduces that signal during the normal Maven build by generating the service
 * TypeScript with the real {@link TypeScriptCodeGenerator} and asserting every {@code this.X}
 * member access resolves to a declared field, constructor parameter, getter/setter or method.</p>
 */
public class EventBusServiceGenerationConsistencyTest
{
    /** Matches {@code this.<member>} accesses (identifiers may contain {@code $}, e.g. RxJS subjects). */
    private static final Pattern THIS_MEMBER = Pattern.compile("this\\.([A-Za-z_$][\\w$]*)");

    /** Matches the (first) constructor parameter list. */
    private static final Pattern CONSTRUCTOR = Pattern.compile("constructor\\s*\\(([^)]*)\\)", Pattern.DOTALL);

    /** Matches a parameter property declaration inside the constructor, e.g. {@code private foo: Bar}. */
    private static final Pattern CTOR_PARAM_PROP =
            Pattern.compile("(?:private|public|protected|readonly)\\s+([A-Za-z_$][\\w$]*)");

    /** Matches a method / getter / setter declaration line, e.g. {@code private foo(...): void {}. */
    private static final Pattern METHOD_DECL =
            Pattern.compile("^\\s*(?:(?:public|private|protected|static|async|override|get|set|readonly)\\s+)*([A-Za-z_$][\\w$]*)\\s*\\(");

    /** Matches a field declaration line, e.g. {@code private foo?: Bar;} (requires an access/readonly modifier). */
    private static final Pattern FIELD_DECL =
            Pattern.compile("^\\s*(?:(?:public|private|protected|static|readonly|override)\\s+)+([A-Za-z_$][\\w$]*)\\s*[?!:=;]");

    private INgApp<?> testApp;
    private TypeScriptCodeGenerator codeGenerator;

    @BeforeEach
    public void setup()
    {
        ScanResult scanResult = IGuiceContext.instance().getScanResult();
        for (ClassInfo classInfo : scanResult.getAllClasses())
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
                // continue scanning
            }
        }

        if (testApp == null)
        {
            return;
        }

        File appPath = AppUtils.getAppPath((Class<? extends INgApp<?>>) testApp.getClass());
        IComponent.getCurrentAppFile().set(appPath);
        IComponent.app.set(testApp);
        codeGenerator = new TypeScriptCodeGenerator(testApp);
    }

    @Test
    public void testGeneratedEventBusServiceDeclaresContextIdService()
    {
        if (testApp == null)
        {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        String ts = generateEventBusServiceTs();

        // The service heavily uses this.contextIdService.* – it must be injected as a constructor parameter.
        Assertions.assertTrue(
                Pattern.compile("contextIdService\\s*:\\s*ContextIdService").matcher(ts).find(),
                "Generated EventBusService must inject ContextIdService as a constructor parameter, " +
                        "otherwise ng build fails with TS2339. Generated TypeScript:\n" + ts);
    }

    @Test
    public void testGeneratedEventBusServiceHasNoUndeclaredThisMembers()
    {
        if (testApp == null)
        {
            System.out.println("[DEBUG_LOG] No test app found. Skipping test.");
            return;
        }

        String ts = generateEventBusServiceTs();

        // Comments (incl. commented-out code such as "/* if (this.eventBus) ... */") never reach the
        // TypeScript compiler, so they must be excluded before checking member usage.
        String code = stripComments(ts);

        Set<String> declared = collectDeclaredMembers(code);
        Set<String> undeclared = new TreeSet<>();

        Matcher usage = THIS_MEMBER.matcher(code);
        while (usage.find())
        {
            String member = usage.group(1);
            if (!declared.contains(member))
            {
                undeclared.add(member);
            }
        }

        Assertions.assertTrue(undeclared.isEmpty(),
                "Generated EventBusService accesses members via 'this.' that are never declared " +
                        "(these become TS2339 errors during ng build): " + undeclared +
                        "\nDeclared members were: " + new TreeSet<>(declared) +
                        "\nGenerated TypeScript:\n" + ts);
    }

    private String generateEventBusServiceTs()
    {
        EventBusService<?> service = IGuiceContext.get(EventBusService.class);
        String ts = codeGenerator.generateTypeScriptForComponent(service);
        Assertions.assertNotNull(ts, "Generated EventBusService TypeScript should not be null");
        Assertions.assertFalse(ts.isBlank(), "Generated EventBusService TypeScript should not be blank");
        return ts;
    }

    /**
     * Removes block comments ({@code /* ... *&#47;}, including JSDoc) and line comments ({@code //...})
     * so that commented-out code is not mistaken for live member access. Line comments preceded by a
     * colon are preserved to avoid truncating URLs such as {@code ws://...} inside string literals.
     */
    private String stripComments(String ts)
    {
        String noBlocks = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL).matcher(ts).replaceAll("");
        return Pattern.compile("(?<!:)//[^\\n]*").matcher(noBlocks).replaceAll("");
    }

    /**
     * Collects every member name declared on the class: constructor parameter-properties, fields,
     * and methods/getters/setters. The collection intentionally over-approximates (it may also pick
     * up control-flow keywords such as {@code if}); this only risks missing a defect, never a false
     * failure, which keeps the build stable.
     */
    private Set<String> collectDeclaredMembers(String ts)
    {
        Set<String> declared = new LinkedHashSet<>();

        Matcher ctor = CONSTRUCTOR.matcher(ts);
        if (ctor.find())
        {
            Matcher param = CTOR_PARAM_PROP.matcher(ctor.group(1));
            while (param.find())
            {
                declared.add(param.group(1));
            }
        }

        for (String line : ts.split("\\R"))
        {
            Matcher field = FIELD_DECL.matcher(line);
            if (field.find())
            {
                declared.add(field.group(1));
            }
            Matcher method = METHOD_DECL.matcher(line);
            if (method.find())
            {
                declared.add(method.group(1));
            }
        }
        return declared;
    }
}




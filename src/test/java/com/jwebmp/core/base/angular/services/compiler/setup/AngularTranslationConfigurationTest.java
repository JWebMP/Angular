package com.jwebmp.core.base.angular.services.compiler.setup;

import com.jwebmp.core.base.angular.client.annotations.angular.*;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.Map;
import java.util.zip.*;
import static org.junit.jupiter.api.Assertions.*;

class AngularTranslationConfigurationTest
{
    @TempDir Path directory;

    @NgTranslations(supportedLanguages = {"de", "fr"}, namespaces = {"orders"})
    @NgTranslationSource(namespace = "orders", resource = "custom/orders")
    @NgTranslationSource(namespace = "orders", url = "/rest/orders/{language}", priority = 200)
    static class Application {}

    @NgTranslations(namespaces = {"orders"})
    static class LibraryOnly {}

    @NgTranslations
    @NgTranslationSource(namespace = "orders", resource = "missing")
    static class MissingSource {}

    @NgTranslations
    @NgTranslationSource(namespace = "orders", url = "/fixed-url")
    static class BadUrl {}

    @NgApp(value = "translation-test", bootComponent = TranslationComponent.class)
    @NgTranslations(supportedLanguages = {"de"})
    public static class TestApplication extends com.jwebmp.core.base.angular.services.NGApplication<TestApplication> {}

    @Test
    void rendersBootstrapAndComponentPipeImports() throws Exception
    {
        System.setProperty("jwebmp.outputDirectory", Path.of("target", "translation-integration").toAbsolutePath().toString());
        var application = new TestApplication();
        com.jwebmp.core.base.angular.client.services.interfaces.IComponent.app.set(application);
        com.jwebmp.core.base.angular.client.services.interfaces.IComponent.getCurrentAppFile().set(directory.toFile());
        var component = new TranslationComponent();
        new com.jwebmp.core.base.angular.implementations.configurations.ConfigureImportReferences()
                .onComponentConfigured(null, component);
        var config = (com.jwebmp.core.base.angular.client.services.ComponentConfiguration<?>)
                component.getProperties().get("AngularConfiguration");
        assertTrue(config.getImportModules().stream().anyMatch(item -> item.value().equals("TranslocoPipe")), config.renderImportStatements() + " modules=" + config.renderImportModules());
        assertTrue(config.getConstructorParameters().stream().anyMatch(item -> item.value().equals("public translationService: TranslationService")));
        assertFalse(config.getImportProviders().stream().anyMatch(item -> item.value().equals("TranslationService")), "Service must remain root-scoped");
        Path output = Path.of("target", "translation-integration");
        Files.createDirectories(output);
        Files.writeString(output.resolve("TranslationComponent.ts"), component.renderClassTs().toString());
        Files.writeString(output.resolve("TranslationComponent.html"), component.toString(0));
        Files.writeString(output.resolve("component-path.txt"),
                com.jwebmp.core.base.angular.client.AppUtils.getAppSrcPath(TestApplication.class).toPath()
                        .relativize(com.jwebmp.core.base.angular.client.AppUtils.getFile(TestApplication.class, TranslationComponent.class, ".ts").toPath())
                        .toString().replace('\\', '/'));
        try (var scan = scan(jar("bootstrap", Map.of("META-INF/jwebmp/i18n/orders/en.json", "{\"save\":\"Save\"}"))))
        {
            var generated = AngularTranslationConfiguration.collect(TestApplication.class, scan);
            var imports = new StringBuilder();
            var providers = new StringBuilder();
            AngularTranslationConfiguration.append(generated, TestApplication.class, imports, providers);
            assertTrue(imports.toString().contains("TranslationService/TranslationService"));
            assertTrue(providers.toString().contains("provideAppInitializer"));
            Files.writeString(output.resolve("app.config.ts"), imports + "\nexport const appConfig = {providers: [" + providers + "]};\n");
        }
    }

    private Path jar(String name, Map<String, String> resources) throws Exception
    {
        Path jar = directory.resolve(name + ".jar");
        try (var output = new ZipOutputStream(Files.newOutputStream(jar)))
        {
            for (var entry : resources.entrySet())
            {
                output.putNextEntry(new ZipEntry(entry.getKey()));
                output.write(entry.getValue().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                output.closeEntry();
            }
        }
        return jar;
    }

    private ScanResult scan(Path... jars)
    {
        return new ClassGraph().overrideClasspath((Object[]) jars).scan();
    }

    @Test
    void discoversJarsMergesOverridesAndWritesAppSelectedBundles() throws Exception
    {
        Path library = jar("library", Map.of(
                "META-INF/jwebmp/i18n/orders/en.json", "{\"save\":\"Save\",\"nested\":{\"cancel\":\"Cancel\"}}",
                "META-INF/jwebmp/i18n/orders/de.json", "{\"save\":\"Speichern\"}",
                "META-INF/jwebmp/i18n/other/en.json", "{\"private\":\"Other application\"}"));
        Path application = jar("application", Map.of("custom/orders/en.json", "{\"save\":\"Save order\"}"));
        try (var scan = scan(library, application))
        {
            var generated = AngularTranslationConfiguration.collect(Application.class, scan);
            assertEquals(Map.of("orders.save", "Save order", "orders.nested.cancel", "Cancel"), generated.bundles().get("en"));
            assertEquals(Map.of("orders.save", "Speichern"), generated.bundles().get("de"));
            assertTrue(generated.bundles().get("fr").isEmpty());
            assertEquals(java.util.Set.of("orders"), generated.config().get("namespaces"));
            AngularTranslationConfiguration.write(generated, directory.resolve("public"));
            assertTrue(Files.readString(directory.resolve("public/i18n/jwebmp/manifest.json")).contains("orders"));
            assertFalse(Files.readString(directory.resolve("public/i18n/jwebmp/en.json")).contains("private"));
        }
    }

    @Test
    void reportsEqualPriorityConflictWithBothOrigins() throws Exception
    {
        Path a = jar("first", Map.of("META-INF/jwebmp/i18n/orders/en.json", "{\"save\":\"Save\"}"));
        Path b = jar("second", Map.of("META-INF/jwebmp/i18n/orders/en.json", "{\"save\":\"Submit\"}"));
        try (var scan = scan(a, b))
        {
            var error = assertThrows(IllegalArgumentException.class, () -> AngularTranslationConfiguration.collect(LibraryOnly.class, scan));
            assertTrue(error.getMessage().contains("first.jar"));
            assertTrue(error.getMessage().contains("second.jar"));
            assertTrue(error.getMessage().contains("orders.save"));
        }
    }

    @Test
    void rejectsInvalidAndDuplicateDictionaryKeys() throws Exception
    {
        for (String json : new String[]{"{\"x\":1}", "{\"x\":\"a\",\"x\":\"b\"}",
                "{\"a.b\":\"x\",\"a\":{\"b\":\"y\"}}", "{\"__proto__\":{\"x\":\"y\"}}"})
        {
            Path jar = jar("invalid" + Math.abs(json.hashCode()), Map.of("META-INF/jwebmp/i18n/orders/en.json", json));
            try (var scan = scan(jar))
            {
                assertThrows(IllegalArgumentException.class, () -> AngularTranslationConfiguration.collect(LibraryOnly.class, scan));
            }
        }
    }

    @Test
    void rejectsMissingExplicitResourcesAndInvalidUrlButLeavesUnannotatedAppsAlone() throws Exception
    {
        try (var scan = scan(jar("empty", Map.of())))
        {
            assertNull(AngularTranslationConfiguration.collect(Object.class, scan));
            assertThrows(IllegalArgumentException.class, () -> AngularTranslationConfiguration.collect(MissingSource.class, scan));
            assertThrows(IllegalArgumentException.class, () -> AngularTranslationConfiguration.collect(BadUrl.class, scan));
        }
    }
}

package com.jwebmp.core.base.angular.services.compiler.setup;

import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.client.annotations.angularconfig.NgBudget;
import com.jwebmp.core.base.angular.client.annotations.angularconfig.NgBuildConfiguration;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import com.jwebmp.core.base.angular.typescript.JWebMP.ResourceLocator;
import com.jwebmp.core.base.html.DivSimple;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Objects;

import static com.jwebmp.core.base.angular.client.annotations.angularconfig.NgBudget.Type.*;
import static com.jwebmp.core.base.angular.client.annotations.angularconfig.NgBuildConfiguration.BooleanOption.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

class AngularBuildConfigurationTest
{
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final String BUILD = "/projects/JWebMP/architect/build";
    private static final String CONFIG = BUILD + "/configurations";

    static class Boot extends DivSimple<Boot> implements INgComponent<Boot> {}

    @NgApp(value = "defaults", bootComponent = Boot.class)
    static class Defaults {}

    @NgApp(value = "custom", bootComponent = Boot.class,
            production = @NgBuildConfiguration(
                    budgets = @NgBudget(type = ANY_COMPONENT_STYLE, maximumWarning = "30kb", maximumError = "50kb"),
                    optimization = TRUE, sourceMap = FALSE, extractLicenses = TRUE,
                    outputHashing = NgBuildConfiguration.OutputHashing.BUNDLES),
            development = @NgBuildConfiguration(
                    budgets = @NgBudget(type = INITIAL, maximumWarning = "5mb", maximumError = "15mb"),
                    optimization = TRUE, sourceMap = FALSE, extractLicenses = TRUE,
                    outputHashing = NgBuildConfiguration.OutputHashing.NONE))
    static class Custom {}

    static class Inherited extends Custom {}

    @NgApp(value = "clear", bootComponent = Boot.class,
            production = @NgBuildConfiguration(inheritBudgets = false),
            development = @NgBuildConfiguration(inheritBudgets = false, budgets = {
                    @NgBudget(type = BUNDLE, name = "main", baseline = "100kb", minimumWarning = "10%",
                            minimumError = "20%", maximumWarning = "30%", maximumError = "40%",
                            warning = "5%", error = "10%"),
                    @NgBudget(type = BUNDLE, name = "vendor", maximumError = "1mb")
            }))
    static class Replace {}

    @NgApp(value = "replace-production", bootComponent = Boot.class,
            production = @NgBuildConfiguration(inheritBudgets = false,
                    budgets = @NgBudget(type = INITIAL, maximumWarning = "3mb")))
    static class ReplaceProduction {}

    @NgApp(value = "json", bootComponent = Boot.class,
            production = @NgBuildConfiguration(optimization = FALSE, optionsJson = """
                    {
                      "optimization": {"scripts": true, "styles": {"minify": false}},
                      "sourceMap": {"scripts": true, "hidden": true, "vendor": null},
                      "outputHashing": null,
                      "budgets": [],
                      "allowedCommonJsDependencies": ["legacy-library"]
                    }
                    """))
    static class JsonOptions {}

    @NgApp(value = "bad", bootComponent = Boot.class,
            production = @NgBuildConfiguration(optionsJson = "{"))
    static class InvalidJson {}

    @NgApp(value = "bad", bootComponent = Boot.class,
            development = @NgBuildConfiguration(optionsJson = "[]"))
    static class InvalidShape {}

    @NgApp(value = "bad", bootComponent = Boot.class,
            production = @NgBuildConfiguration(optionsJson = "null"))
    static class NullJson {}

    @NgApp(value = "bad", bootComponent = Boot.class,
            production = @NgBuildConfiguration(optionsJson = " "))
    static class EmptyJson {}

    @NgApp(value = "bad", bootComponent = Boot.class,
            production = @NgBuildConfiguration(optionsJson = "{} {}"))
    static class TrailingJson {}

    @NgApp(value = "bad", bootComponent = Boot.class,
            production = @NgBuildConfiguration(optionsJson = "{\"sourceMap\":true,\"sourceMap\":false}"))
    static class DuplicateJson {}

    @NgApp(value = "bad", bootComponent = Boot.class,
            production = @NgBuildConfiguration(budgets = {
                    @NgBudget(type = INITIAL, maximumError = "1mb"),
                    @NgBudget(type = INITIAL, maximumError = "2mb")
            }))
    static class DuplicateBudget {}

    @NgApp(value = "bad", bootComponent = Boot.class,
            production = @NgBuildConfiguration(budgets = @NgBudget(type = BUNDLE, maximumError = "1mb")))
    static class UnnamedBundle {}

    private String template() throws IOException
    {
        try (var stream = Objects.requireNonNull(ResourceLocator.class.getResourceAsStream("angular.json")))
        {
            return new String(stream.readAllBytes(), UTF_8)
                    .replace("/*MainTSFile*/", "\"src/main.ts\"")
                    .replace("/*BuildAssets*/", "[{\"glob\":\"**/*\",\"input\":\"public\"}]")
                    .replace("/*BuildStylesSCSS*/", "[\"src/styles.scss\"]")
                    .replace("/*BuildScripts*/", "[]");
        }
    }

    private JsonNode render(Class<?> app) throws IOException
    {
        return JSON.readTree(AngularBuildConfiguration.apply(template(), app));
    }

    @Test
    void preservesTemplateDefaultsAndUnannotatedApps() throws IOException
    {
        String template = template();
        assertEquals(JSON.readTree(template), render(Defaults.class));
        assertEquals(template, AngularBuildConfiguration.apply(template, Object.class));
    }

    @Test
    void appliesIndependentTypedOptionsWithoutChangingOtherWorkspaceSettings() throws IOException
    {
        JsonNode root = render(Custom.class);
        JsonNode production = root.at(CONFIG + "/production");
        JsonNode development = root.at(CONFIG + "/development");
        assertEquals(2, production.path("budgets").size());
        assertEquals("2500kb", production.at("/budgets/0/maximumWarning").asString());
        assertEquals("10mb", production.at("/budgets/0/maximumError").asString());
        assertEquals("anyComponentStyle", production.at("/budgets/1/type").asString());
        assertEquals("30kb", production.at("/budgets/1/maximumWarning").asString());
        assertEquals("50kb", production.at("/budgets/1/maximumError").asString());
        assertTrue(production.path("optimization").asBoolean());
        assertFalse(production.path("sourceMap").asBoolean());
        assertTrue(production.path("extractLicenses").asBoolean());
        assertEquals("bundles", production.path("outputHashing").asString());
        assertEquals(1, development.path("budgets").size());
        assertEquals("5mb", development.at("/budgets/0/maximumWarning").asString());
        assertTrue(development.path("optimization").asBoolean());
        assertFalse(development.path("sourceMap").asBoolean());
        assertTrue(development.path("extractLicenses").asBoolean());
        assertEquals("none", development.path("outputHashing").asString());
        JsonNode original = JSON.readTree(template());
        assertEquals(original.at(BUILD + "/options"), root.at(BUILD + "/options"));
        assertEquals(original.at("/projects/JWebMP/architect/serve"), root.at("/projects/JWebMP/architect/serve"));
        assertEquals("development", root.at(BUILD + "/defaultConfiguration").asString());
        assertEquals(root, render(Inherited.class));
    }

    @Test
    void canDisableBudgetsAndUseNamedBudgetsWithAllThresholdFields() throws IOException
    {
        JsonNode root = render(Replace.class);
        assertEquals(0, root.at(CONFIG + "/production/budgets").size());
        JsonNode budgets = root.at(CONFIG + "/development/budgets");
        assertEquals(2, budgets.size());
        assertEquals(JSON.readTree("""
                {"type":"bundle","name":"main","baseline":"100kb","minimumWarning":"10%",
                 "minimumError":"20%","maximumWarning":"30%","maximumError":"40%","warning":"5%","error":"10%"}
                """), budgets.get(0));
        assertEquals("vendor", budgets.get(1).path("name").asString());
        assertFalse(budgets.get(1).has("baseline"));
        JsonNode replacement = render(ReplaceProduction.class).at(CONFIG + "/production/budgets");
        assertEquals(1, replacement.size());
        assertEquals("initial", replacement.get(0).path("type").asString());
        assertEquals("3mb", replacement.get(0).path("maximumWarning").asString());
        assertFalse(replacement.get(0).has("maximumError"));
    }

    @Test
    void appliesJsonLastAndKeepsOtherEnvironmentUnchanged() throws IOException
    {
        JsonNode root = render(JsonOptions.class);
        JsonNode production = root.at(CONFIG + "/production");
        assertTrue(production.at("/optimization/scripts").asBoolean());
        assertFalse(production.at("/optimization/styles/minify").asBoolean());
        assertTrue(production.at("/sourceMap/hidden").asBoolean());
        assertFalse(production.has("outputHashing"));
        assertEquals(0, production.path("budgets").size());
        assertEquals("legacy-library", production.at("/allowedCommonJsDependencies/0").asString());
        assertEquals(render(Defaults.class).at(CONFIG + "/development"), root.at(CONFIG + "/development"));
    }

    @Test
    void recursivelyMergesObjectsAndRemovesNestedValues() throws IOException
    {
        String template = template().replace("\"outputHashing\": \"all\"", """
                "outputHashing": "all",
                "sourceMap": {"scripts": false, "styles": true, "vendor": true},
                "optimization": {"styles": {"inlineCritical": true}, "fonts": true}
                """);
        JsonNode production = JSON.readTree(AngularBuildConfiguration.apply(template, JsonOptions.class))
                .at(CONFIG + "/production");
        assertTrue(production.at("/sourceMap/styles").asBoolean());
        assertFalse(production.path("sourceMap").has("vendor"));
        // A typed boolean intentionally replaces the prior optimization object before JSON merges.
        assertFalse(production.path("optimization").has("fonts"));
        assertTrue(production.at("/optimization/scripts").asBoolean());
    }

    @Test
    void rejectsInvalidOverridesWithAppAndEnvironmentContext() throws IOException
    {
        String template = template();
        for (Class<?> invalid : new Class<?>[] {InvalidJson.class, InvalidShape.class, TrailingJson.class,
                DuplicateJson.class, NullJson.class, EmptyJson.class, DuplicateBudget.class, UnnamedBundle.class})
        {
            var error = assertThrows(IllegalArgumentException.class,
                    () -> AngularBuildConfiguration.apply(template, invalid));
            assertTrue(error.getMessage().contains(invalid.getName()), error.getMessage());
            assertTrue(error.getMessage().contains(invalid == InvalidShape.class ? "development" : "production"));
        }
    }
}

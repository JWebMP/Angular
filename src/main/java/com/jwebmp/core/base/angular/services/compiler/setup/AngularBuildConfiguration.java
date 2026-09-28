package com.jwebmp.core.base.angular.services.compiler.setup;

import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.client.annotations.angularconfig.NgBudget;
import com.jwebmp.core.base.angular.client.annotations.angularconfig.NgBuildConfiguration;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Applies app-specific overrides to the fully substituted angular.json template. */
public final class AngularBuildConfiguration
{
    private static final JsonMapper JSON = JsonMapper.builder()
            .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .build();

    private AngularBuildConfiguration() {}

    public static String apply(String template, Class<?> appClass)
    {
        NgApp app = appClass.getAnnotation(NgApp.class);
        if (app == null) return template;
        JsonNode root = JSON.readTree(template);
        JsonNode configurations = root.at("/projects/JWebMP/architect/build/configurations");
        apply(object(configurations.get("production"), "production"), app.production(), appClass, "production");
        apply(object(configurations.get("development"), "development"), app.development(), appClass, "development");
        return JSON.writerWithDefaultPrettyPrinter().writeValueAsString(root);
    }

    private static void apply(ObjectNode target, NgBuildConfiguration config, Class<?> appClass, String environment)
    {
        String context = "@NgApp " + appClass.getName() + "." + environment;
        if (!config.inheritBudgets() || config.budgets().length > 0)
        {
            var budgets = JSON.createArrayNode();
            if (config.inheritBudgets() && target.has("budgets"))
            {
                for (JsonNode budget : target.get("budgets")) budgets.add(budget);
            }
            Set<String> keys = new HashSet<>();
            for (NgBudget budget : config.budgets())
            {
                String key = budget.type().value() + ":" + budget.name();
                if (!keys.add(key)) throw new IllegalArgumentException(context + ": duplicate budget " + key);
                if (budget.type() == NgBudget.Type.BUNDLE && budget.name().isBlank())
                    throw new IllegalArgumentException(context + ": BUNDLE budget requires a name");
                ObjectNode value = JSON.createObjectNode();
                value.put("type", budget.type().value());
                put(value, "name", budget.name());
                put(value, "baseline", budget.baseline());
                put(value, "maximumWarning", budget.maximumWarning());
                put(value, "maximumError", budget.maximumError());
                put(value, "minimumWarning", budget.minimumWarning());
                put(value, "minimumError", budget.minimumError());
                put(value, "warning", budget.warning());
                put(value, "error", budget.error());
                int match = -1;
                for (int i = 0; i < budgets.size(); i++)
                {
                    JsonNode existing = budgets.get(i);
                    if (budget.type().value().equals(existing.path("type").asString())
                            && budget.name().equals(existing.path("name").asString("")))
                    {
                        match = i;
                        break;
                    }
                }
                if (match < 0) budgets.add(value);
                else budgets.set(match, value);
            }
            target.set("budgets", budgets);
        }
        put(target, "optimization", config.optimization());
        put(target, "sourceMap", config.sourceMap());
        put(target, "extractLicenses", config.extractLicenses());
        if (config.outputHashing() != NgBuildConfiguration.OutputHashing.DEFAULT)
            target.put("outputHashing", config.outputHashing().name().toLowerCase(Locale.ROOT));
        JsonNode overrides;
        try
        {
            overrides = JSON.readTree(config.optionsJson());
        }
        catch (JacksonException e)
        {
            throw new IllegalArgumentException(context + ": optionsJson must be a valid JSON object", e);
        }
        merge(target, object(overrides, context + ".optionsJson"));
    }

    private static ObjectNode object(JsonNode node, String context)
    {
        if (node instanceof ObjectNode result) return result;
        throw new IllegalArgumentException(context + " must be a JSON object");
    }

    private static void put(ObjectNode target, String key, String value)
    {
        if (!value.isEmpty()) target.put(key, value);
    }

    private static void put(ObjectNode target, String key, NgBuildConfiguration.BooleanOption value)
    {
        if (value != NgBuildConfiguration.BooleanOption.DEFAULT)
            target.put(key, value == NgBuildConfiguration.BooleanOption.TRUE);
    }

    private static void merge(ObjectNode target, ObjectNode overrides)
    {
        for (var entry : overrides.properties())
        {
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            if (value.isNull()) target.remove(key);
            else if (value instanceof ObjectNode object)
            {
                ObjectNode child = target.get(key) instanceof ObjectNode existing ? existing : JSON.createObjectNode();
                merge(child, object);
                target.set(key, child);
            }
            else target.set(key, value);
        }
    }
}

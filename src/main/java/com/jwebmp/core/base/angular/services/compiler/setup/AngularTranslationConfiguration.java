package com.jwebmp.core.base.angular.services.compiler.setup;

import com.jwebmp.core.base.angular.client.AppUtils;
import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.services.TranslationService;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Discovers and merges app-selected library dictionaries using the existing ClassGraph scan. */
public final class AngularTranslationConfiguration
{
    private static final String ROOT = "META-INF/jwebmp/i18n/";
    private static final JsonMapper JSON = JsonMapper.builder().enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build();

    private AngularTranslationConfiguration() {}

    public static NgTranslations configuration(Class<?> appClass)
    {
        NgTranslations result = appClass.getAnnotation(NgTranslations.class);
        NgApp app = appClass.getAnnotation(NgApp.class);
        return result != null || app == null ? result : app.bootComponent().getAnnotation(NgTranslations.class);
    }

    record Entry(String language, String namespace, int priority, String origin, Map<String, String> values) {}
    record Owner(int priority, String origin) {}
    record Generated(Map<String, Object> config, Map<String, Map<String, String>> bundles, boolean messageFormat) {}

    static Generated collect(Class<?> appClass, ScanResult scan) throws IOException
    {
        NgTranslations annotation = configuration(appClass);
        if (annotation == null) return null;
        Set<String> languages = new TreeSet<>();
        languages.add(language(annotation.defaultLanguage()));
        for (String value : annotation.supportedLanguages()) languages.add(language(value));
        if (annotation.timeoutMs() <= 0) throw new IllegalArgumentException("NgTranslations.timeoutMs must be positive");
        Set<String> selected = new TreeSet<>();
        for (String value : annotation.namespaces()) selected.add(namespace(value));
        Set<String> namespaces = new TreeSet<>(selected);
        List<Entry> entries = new ArrayList<>();
        for (Resource resource : scan.getResourcesMatchingWildcard(ROOT + "*/*.json"))
        {
            String relative = resource.getPath().substring(ROOT.length());
            String[] parts = relative.split("/");
            if (parts.length != 2) continue;
            String scope = namespace(parts[0]);
            if (!selected.isEmpty() && !selected.contains(scope)) continue;
            String lang = language(parts[1].substring(0, parts[1].length() - 5));
            if (!languages.contains(lang)) continue;
            namespaces.add(scope);
            entries.add(read(resource, lang, scope, 0));
        }
        List<NgTranslationSource> sources = new ArrayList<>();
        NgApp app = appClass.getAnnotation(NgApp.class);
        if (app != null) sources.addAll(Arrays.asList(app.bootComponent().getAnnotationsByType(NgTranslationSource.class)));
        sources.addAll(Arrays.asList(appClass.getAnnotationsByType(NgTranslationSource.class)));
        List<Map<String, Object>> urls = new ArrayList<>();
        for (NgTranslationSource source : sources)
        {
            String scope = namespace(source.namespace());
            namespaces.add(scope);
            if (source.resource().isBlank() == source.url().isBlank())
                throw new IllegalArgumentException("Translation source must specify exactly one resource or URL: " + scope);
            if (!source.url().isBlank())
            {
                if (!source.url().contains("{language}")) throw new IllegalArgumentException("Translation URL requires {language}: " + source.url());
                urls.add(Map.of("namespace", scope, "url", source.url(), "priority", source.priority(), "optional", source.optional()));
                continue;
            }
            String directory = source.resource().replaceAll("/+$", "");
            if (!directory.matches("[A-Za-z0-9_.-]+(?:/[A-Za-z0-9_.-]+)*") || Arrays.asList(directory.split("/")).contains(".."))
                throw new IllegalArgumentException("Invalid translation resource directory: " + directory);
            boolean found = false;
            for (String lang : languages)
            {
                for (Resource resource : scan.getResourcesWithPath(directory + "/" + lang + ".json"))
                {
                    found = true;
                    entries.add(read(resource, lang, scope, source.priority()));
                }
            }
            if (!found) throw new IllegalArgumentException("No supported translation resources found at " + directory);
        }
        entries.sort(Comparator.comparingInt(Entry::priority).thenComparing(Entry::origin));
        Map<String, Map<String, String>> bundles = new TreeMap<>();
        for (String lang : languages)
        {
            Map<String, String> values = new TreeMap<>();
            Map<String, Owner> owners = new HashMap<>();
            for (Entry entry : entries)
            {
                if (!entry.language().equals(lang)) continue;
                for (var item : entry.values().entrySet())
                {
                    Owner owner = owners.get(item.getKey());
                    if (owner != null && owner.priority() == entry.priority() && !values.get(item.getKey()).equals(item.getValue()))
                        throw new IllegalArgumentException("Conflicting translation " + lang + ":" + item.getKey() + " in " + owner.origin() + " and " + entry.origin());
                    values.put(item.getKey(), item.getValue());
                    owners.put(item.getKey(), new Owner(entry.priority(), entry.origin()));
                }
            }
            bundles.put(lang, values);
        }
        urls.sort(Comparator.<Map<String, Object>>comparingInt(item -> (Integer) item.get("priority"))
                .thenComparing(item -> item.get("namespace").toString()).thenComparing(item -> item.get("url").toString()));
        Map<String, Object> config = new TreeMap<>();
        config.put("defaultLanguage", annotation.defaultLanguage());
        config.put("languages", languages);
        config.put("namespaces", namespaces);
        config.put("sources", urls);
        config.put("timeoutMs", annotation.timeoutMs());
        config.put("bundleUrl", "i18n/jwebmp/{language}.json");
        return new Generated(config, bundles, annotation.messageFormat());
    }

    private static Entry read(Resource resource, String language, String namespace, int priority) throws IOException
    {
        String origin = resource.getURI().toString();
        try (var stream = resource.open())
        {
            Map<?, ?> dictionary = JSON.readValue(stream, Map.class);
            Map<String, String> values = new TreeMap<>();
            flatten(dictionary, namespace, values);
            return new Entry(language, namespace, priority, origin, values);
        }
        catch (RuntimeException error)
        {
            throw new IllegalArgumentException("Invalid translation dictionary " + origin + ": " + error.getMessage(), error);
        }
    }

    private static void flatten(Object value, String path, Map<String, String> result)
    {
        if (value instanceof String text)
        {
            if (result.putIfAbsent(path, text) != null) throw new IllegalArgumentException("Duplicate translation key: " + path);
        }
        else if (value instanceof Map<?, ?> map)
        {
            for (var item : map.entrySet())
            {
                String key = item.getKey().toString();
                for (String part : key.split("\\.", -1))
                    if (part.isEmpty() || Set.of("__proto__", "constructor", "prototype").contains(part))
                        throw new IllegalArgumentException("Invalid translation key: " + key);
                flatten(item.getValue(), path + "." + key, result);
            }
        }
        else throw new IllegalArgumentException("Expected translation object or string at " + path);
    }

    private static String language(String value)
    {
        if (!value.matches("[A-Za-z]{2,8}(?:-[A-Za-z0-9]{1,8})*")) throw new IllegalArgumentException("Invalid translation language: " + value);
        return value;
    }

    private static String namespace(String value)
    {
        if (!value.matches("[A-Za-z][A-Za-z0-9_-]*") || Set.of("constructor", "prototype").contains(value))
            throw new IllegalArgumentException("Invalid translation namespace: " + value);
        return value;
    }

    static void write(Generated generated, Path publicDirectory) throws IOException
    {
        Path directory = publicDirectory.resolve("i18n/jwebmp");
        Files.createDirectories(directory);
        for (var bundle : generated.bundles().entrySet())
            Files.writeString(directory.resolve(bundle.getKey() + ".json"), JSON.writeValueAsString(bundle.getValue()));
        Files.writeString(directory.resolve("manifest.json"), JSON.writeValueAsString(Map.of(
                "defaultLanguage", generated.config().get("defaultLanguage"), "languages", generated.config().get("languages"),
                "namespaces", generated.config().get("namespaces"))));
    }

    static void append(Generated generated, Class<? extends INgApp<?>> appClass, StringBuilder imports, StringBuilder providers)
    {
        if (generated == null) return;
        String relative = AppUtils.getAppSrcPath(appClass).toPath().relativize(AppUtils.getFile(appClass, TranslationService.class, ".ts").toPath())
                .toString().replace('\\', '/').replaceAll("\\.ts$", "");
        imports.append("import {TranslationService} from './").append(relative).append("';\n")
                .append("import {inject as jwebmpInject, provideAppInitializer} from '@angular/core';\n")
                .append("import {provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';\n")
                .append("import {provideTransloco} from '@jsverse/transloco';\n");
        Map<String, Object> engine = new TreeMap<>();
        engine.put("defaultLang", generated.config().get("defaultLanguage"));
        engine.put("availableLangs", generated.config().get("languages"));
        engine.put("reRenderOnLangChange", true);
        // JWebMP publishes already-merged fallback dictionaries; Transloco must not initiate a separate loader.
        String value = "{provide: 'JWEBMP_TRANSLATIONS', useValue: " + JSON.writeValueAsString(generated.config()) + "},\n"
                + "provideHttpClient(withInterceptorsFromDi()),\nprovideTransloco({config: " + JSON.writeValueAsString(engine) + "}),\n";
        if (generated.messageFormat())
        {
            imports.append("import {provideTranslocoMessageformat} from '@jsverse/transloco-messageformat';\n");
            value += "provideTranslocoMessageformat(),\n";
        }
        value += "provideAppInitializer(() => jwebmpInject(TranslationService).initialize()),\n";
        // Consumer providers follow these defaults, preserving application HTTP overrides.
        providers.insert(0, value);
    }
}

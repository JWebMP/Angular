package com.jwebmp.core.base.angular.services.compiler.setup;

import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import com.jwebmp.core.base.angular.services.NGApplication;
import com.jwebmp.core.base.html.DivSimple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class AngularDependencyOverrideTest {
    @TempDir Path directory;

    @NgApp(value = "dependency-override-test", bootComponent = TestBoot.class)
    static class TestApp extends NGApplication<TestApp> { }

    @NgComponent("dependency-override-boot")
    static class TestBoot extends DivSimple<TestBoot> implements INgComponent<TestBoot> { }

    @Test
    void explicitOverrideReplacesAnEarlierPluginRangeInTheGeneratedManifest() throws Exception {
        var dependencies = new TreeMap<>(Map.of("@fullcalendar/core", "^6.1.21", "rxjs", "^7.8.0"));
        var overrides = new TreeMap<>(Map.of("@fullcalendar/core", "6.1.21", "@fullcalendar/premium-common", "6.1.21"));
        var file = directory.resolve("package.json").toFile();
        var mapper = JsonMapper.builder().build();
        var writer = AngularAppSetup.class.getDeclaredMethod("processPackageJsonFile", Class.class,
                String.class, ObjectMapper.class, Map.class, Map.class, Map.class, File.class);
        writer.setAccessible(true);
        var template = """
                {"name":"/*appName*/", "dependencies":/*dependencies*/,
                 "devDependencies":/*devDependencies*/, "overrides":/*overrideDependencies*/}
                """;
        assertEquals(true, writer.invoke(new AngularAppSetup(null), TestApp.class, template, mapper,
                dependencies, new TreeMap<String, String>(), overrides, file));
        var manifest = mapper.readTree(file);
        for (var entry : overrides.entrySet()) {
            assertEquals(entry.getValue(), manifest.get("dependencies").get(entry.getKey()).asString());
            assertEquals(entry.getValue(), manifest.get("overrides").get(entry.getKey()).asString());
        }
        assertEquals("^7.8.0", manifest.get("dependencies").get("rxjs").asString());
    }
}

package com.jwebmp.core.base.angular.services.compiler.setup;

import com.guicedee.guicedinjection.GuiceContext;
import com.jwebmp.core.base.angular.client.annotations.angular.AngularToolchainVersions;
import com.jwebmp.core.base.angular.client.annotations.angularconfig.NgStyleSheet;
import com.jwebmp.core.base.angular.client.services.AnnotationHelper;
import com.jwebmp.core.base.angular.services.NGApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class WeatherIconsMetadataTest {
    @TempDir Path temporary;

    @Test void generatorRetainsWeatherAssetsOutsideTheScan() throws Exception {
        var scan = GuiceContext.instance().getScanResult();
        assertNull(scan.getClassInfo("com.jwebmp.plugins.weathericons.WeatherIconsPageConfigurator"));
        assertNull(scan.getClassInfo("com.jwebmp.plugins.weathericons.WeatherIcon"));
        var styles = AnnotationHelper.instance.getGlobalAnnotations(NgStyleSheet.class).stream()
                .map(NgStyleSheet::value).filter(value -> value.startsWith("node_modules/weathericons/")).toList();
        assertEquals(List.of("node_modules/weathericons/css/weather-icons.min.css",
                "node_modules/weathericons/css/weather-icons-wind.min.css"), styles);

        var app = new NGApplication<>();
        var dependencies = new TreeMap<String, String>();
        var collect = AngularAppSetup.class.getDeclaredMethod("processTsDependencies", Map.class, Map.class);
        collect.setAccessible(true);
        collect.invoke(new AngularAppSetup(app), dependencies, new TreeMap<String, String>());
        assertEquals("^2.1.0", dependencies.get("weathericons"));

        // Save a minimal Angular consumer only when explicitly requested; ordinary tests use a temporary directory.
        Path output = Path.of(System.getProperty("weather.fixture.output", temporary.toString()));
        writeConsumer(output, dependencies.get("weathericons"), styles);
    }

    private static void writeConsumer(Path output, String weatherVersion, List<String> styles) throws Exception {
        Files.createDirectories(output.resolve("src"));
        var json = JsonMapper.builder().build();
        var manifest = json.createObjectNode();
        manifest.put("name", "weather-spi-fixture").put("version", "1.0.0").put("private", true);
        manifest.putObject("scripts").put("build", "ng build --configuration production");
        var runtime = manifest.putObject("dependencies");
        for (String name : List.of("core", "common", "compiler", "platform-browser"))
            runtime.put("@angular/" + name, AngularToolchainVersions.ANGULAR);
        runtime.put("rxjs", "^7.8.0").put("tslib", "^2.8.0").put("weathericons", weatherVersion);
        manifest.putObject("devDependencies").put("@angular/build", AngularToolchainVersions.BUILD)
                .put("@angular/cli", AngularToolchainVersions.BUILD)
                .put("@angular/compiler-cli", AngularToolchainVersions.ANGULAR)
                .put("typescript", AngularToolchainVersions.TYPESCRIPT);
        Files.writeString(output.resolve("package.json"), json.writerWithDefaultPrettyPrinter().writeValueAsString(manifest));
        var workspace = json.createObjectNode().put("version", 1);
        var project = workspace.putObject("projects").putObject("weather");
        project.put("root", "").put("sourceRoot", "src").put("projectType", "application");
        var build = project.putObject("architect").putObject("build").put("builder", "@angular/build:application");
        var options = build.putObject("options").put("browser", "src/main.ts").put("index", "src/index.html")
                .put("tsConfig", "tsconfig.json").put("outputPath", "dist/weather");
        var styleList = options.putArray("styles");
        styles.forEach(styleList::add);
        build.putObject("configurations").putObject("production").put("optimization", true).put("outputHashing", "all");
        Files.writeString(output.resolve("angular.json"), json.writerWithDefaultPrettyPrinter().writeValueAsString(workspace));
        Files.writeString(output.resolve("tsconfig.json"), """
                {"compilerOptions":{"target":"ES2022","module":"ESNext","moduleResolution":"bundler","strict":true,
                "experimentalDecorators":true,"skipLibCheck":true,"lib":["ES2022","DOM"]},
                "angularCompilerOptions":{"strictTemplates":true},"files":["src/main.ts"]}
                """);
        Files.writeString(output.resolve("src/main.ts"), """
                import {Component} from '@angular/core';
                import {bootstrapApplication} from '@angular/platform-browser';
                @Component({selector:'app-root', standalone:true, template:'<i class="wi wi-day-sunny" aria-label="Sunny"></i>'})
                class App {}
                bootstrapApplication(App).catch(console.error);
                """);
        Files.writeString(output.resolve("src/index.html"), "<!doctype html><html><head><base href=\"/\"></head><body><app-root></app-root></body></html>");
    }
}

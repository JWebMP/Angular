package com.jwebmp.core.base.angular.typescript;
import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.services.compiler.setup.AngularApplicationToolchain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;
class AngularApplicationToolchainTest {
    @TempDir Path directory;
    @Test void defaultDoesNotChangeUserManifest() {
        assertEquals("user configuration", AngularApplicationToolchain.manifest("user configuration", getClass()));
    }
    public static class Boot extends com.jwebmp.core.base.html.DivSimple<Boot>
        implements com.jwebmp.core.base.angular.client.services.interfaces.INgComponent<Boot> {}
    @NgApp(value="application-only-test", bootComponent=Boot.class, applicationOnly=true)
    static class App {}
    @Test void scriptsUseProductionAndRetainWatch() {
        String result = AngularApplicationToolchain.manifest("{\"scripts\":{}}", App.class);
        assertTrue(result.contains("JWebMP:build:production"));
        assertTrue(result.contains("JWebMP:build:development --watch"));
        assertTrue(result.contains("JWebMP:serve:development"));
    }
    @Test void existingConfigurationsAreUntouched() throws Exception {
        Path existing = directory.resolve("angular.json");
        Files.writeString(existing, "user config");
        assertThrows(IllegalStateException.class, () -> AngularApplicationToolchain.preflight(App.class, directory));
        assertEquals("user config", Files.readString(existing));
    }
    @Test void onlyFrameworkScaffoldingIsOmitted() {
        assertTrue(AngularApplicationToolchain.omitScaffold(App.class,
            "com.jwebmp.core.base.angular.client.services.interfaces.INgModule", "karma"));
        assertFalse(AngularApplicationToolchain.omitScaffold(App.class, "consumer.Tests", "karma"));
    }
    @Test void regenerationAcceptsOnlyUnchangedOwnedManifests() throws Exception {
        Files.writeString(directory.resolve("package.json"), "{}");
        AngularApplicationToolchain.record(App.class, directory);
        assertDoesNotThrow(() -> AngularApplicationToolchain.preflight(App.class, directory));
        Files.writeString(directory.resolve("package.json"), "{\"scripts\":{\"test\":\"custom\"}}");
        assertThrows(IllegalStateException.class, () -> AngularApplicationToolchain.preflight(App.class, directory));
    }
    @Test void realTestsBlockRegenerationButDependencyTestsDoNot() throws Exception {
        Files.createDirectories(directory.resolve("node_modules/example"));
        Files.writeString(directory.resolve("node_modules/example/example.spec.ts"), "test");
        assertDoesNotThrow(() -> AngularApplicationToolchain.preflight(App.class, directory));
        Files.createDirectories(directory.resolve("src"));
        Files.writeString(directory.resolve("src/example.spec.ts"), "test");
        assertThrows(IllegalStateException.class, () -> AngularApplicationToolchain.preflight(App.class, directory));
    }
    @Test void productionAndAssetsSurviveWorkspaceGeneration() {
        String original = "{\"projects\":{\"JWebMP\":{\"architect\":{\"test\":{},\"serve\":{},\"build\":{\"options\":{\"assets\":[\"public\"]},\"configurations\":{\"production\":{\"outputHashing\":\"all\",\"budgets\":[]}}}}}}}";
        var json = tools.jackson.databind.json.JsonMapper.builder().build();
        var output = json.readTree(AngularApplicationToolchain.workspace(original, App.class));
        assertEquals(json.readTree(original).at("/projects/JWebMP/architect/build"), output.at("/projects/JWebMP/architect/build"));
        assertTrue(output.at("/projects/JWebMP/architect").has("serve"));
        assertFalse(output.at("/projects/JWebMP/architect").has("test"));
    }
}

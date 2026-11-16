package com.jwebmp.core.base.angular.typescript;

import com.jwebmp.core.base.angular.services.compiler.setup.AngularAppSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(10)
class NpmCommandFailureTest {
    @TempDir
    Path directory;

    @Test
    void failedInstallStopsStandardWorkspaceBuild() throws Exception {
        Files.writeString(directory.resolve("package.json"), "{\"devDependencies\":{\"@angular/build\":\"22.2.4\"}}");
        assertCommandFails("install");
    }

    @Test
    void failedProductionBuildStopsApplicationOnlyWorkspace() throws Exception {
        Files.writeString(directory.resolve("package.json"), "{\"devDependencies\":{\"@angular-devkit/architect\":\"0.2202.4\"}}");
        assertCommandFails("run", "build-prod");
    }

    @Test
    void successfulCommandCompletesNormally() throws Exception {
        String executable = fakeNpm(0);
        assertDoesNotThrow(() -> runner().invoke(null, directory.toFile(), executable, List.of("install")));
    }

    private void assertCommandFails(String... args) throws Exception {
        String executable = fakeNpm(23);
        InvocationTargetException failure = assertThrows(InvocationTargetException.class,
                () -> runner().invoke(null, directory.toFile(), executable, List.of(args)));
        IllegalStateException cause = assertInstanceOf(IllegalStateException.class, failure.getCause());
        assertTrue(cause.getMessage().contains("exit code 23"));
        assertTrue(cause.getMessage().contains(args[0]));
    }

    private Method runner() throws Exception {
        Method method = AngularAppSetup.class.getDeclaredMethod("runNpmCommand", File.class, String.class, List.class);
        method.setAccessible(true);
        return method;
    }

    private String fakeNpm(int exitCode) throws Exception {
        boolean windows = System.getProperty("os.name").startsWith("Windows");
        Path executable = directory.resolve(windows ? "fake npm.cmd" : "fake npm.sh");
        Files.writeString(executable, windows ? "@exit /b " + exitCode + "\r\n" : "#!/bin/sh\nexit " + exitCode + "\n");
        if (!windows) {
            assertTrue(executable.toFile().setExecutable(true));
        }
        return executable.toAbsolutePath().toString();
    }
}

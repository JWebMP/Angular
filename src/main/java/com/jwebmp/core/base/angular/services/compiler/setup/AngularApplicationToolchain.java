package com.jwebmp.core.base.angular.services.compiler.setup;

import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.client.annotations.angular.AngularToolchainVersions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/** Framework-owned application build contract. Versions verified against npm package metadata. */
public final class AngularApplicationToolchain {
    public static final String ANGULAR = AngularToolchainVersions.ANGULAR;
    public static final String BUILD = AngularToolchainVersions.BUILD;
    public static final String ARCHITECT = AngularToolchainVersions.ARCHITECT;
    public static final String TYPESCRIPT = AngularToolchainVersions.TYPESCRIPT;
    public static final String NODE = AngularToolchainVersions.NODE;
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Set<String> SCAFFOLD = Set.of("@angular/cli", "@eslint/js", "angular-eslint",
        "eslint", "typescript-eslint", "@types/jasmine", "jasmine-core", "karma", "karma-chrome-launcher",
        "karma-coverage", "karma-jasmine", "karma-jasmine-html-reporter", "ng-packagr", "istanbul-lib-instrument");
    private AngularApplicationToolchain() {}
    public static boolean enabled(Class<?> type) {
        NgApp config = type.getAnnotation(NgApp.class);
        return config != null && config.applicationOnly();
    }
    public static boolean omitScaffold(Class<?> app, String declaringType, String dependency) {
        return enabled(app) && declaringType.equals("com.jwebmp.core.base.angular.client.services.interfaces.INgModule")
            && SCAFFOLD.contains(dependency);
    }
    public static void dependencies(Class<?> type, Map<String,String> runtime, Map<String,String> development) {
        if (!enabled(type)) return;
        runtime.replaceAll((name, version) -> name.startsWith("@angular/") ? ANGULAR : version);
        development.put("@angular/build", BUILD);
        development.put("@angular/compiler-cli", ANGULAR);
        development.put("typescript", TYPESCRIPT);
        development.put("@angular-devkit/architect", ARCHITECT);
    }
    public static String manifest(String template, Class<?> type) {
        if (!enabled(type)) return template;
        ObjectNode root = (ObjectNode) JSON.readTree(template);
        ObjectNode scripts = root.putObject("scripts");
        scripts.put("start", "architect JWebMP:serve:development");
        scripts.put("build", "architect JWebMP:build");
        scripts.put("build-prod", "architect JWebMP:build:production");
        scripts.put("watch", "architect JWebMP:build:development --watch");
        root.putObject("engines").put("node", NODE);
        return JSON.writerWithDefaultPrettyPrinter().writeValueAsString(root);
    }
    public static String workspace(String template, Class<?> type) {
        if (!enabled(type)) return template;
        ObjectNode root = (ObjectNode) JSON.readTree(template);
        root.remove("$schema");
        ((ObjectNode)root.at("/projects/JWebMP/architect")).remove("test");
        return JSON.writerWithDefaultPrettyPrinter().writeValueAsString(root);
    }
    private static final String OWNERSHIP = ".jwebmp-application-toolchain.json";
    private static final Set<String> MANIFESTS = Set.of("package.json", "angular.json", "tsconfig.json", "tsconfig.app.json");
    /** Records precisely the generator-owned configuration, after successful generation. */
    public static void record(Class<?> type, Path directory) throws IOException {
        if (!enabled(type)) return;
        ObjectNode record = JSON.createObjectNode();
        for (String name : MANIFESTS) {
            Path file = directory.resolve(name);
            if (Files.isRegularFile(file)) record.put(name, Files.readString(file));
        }
        Files.writeString(directory.resolve(OWNERSHIP), JSON.writeValueAsString(record));
    }
    /** Refuse unowned or edited configuration before any generator writes. */
    public static void preflight(Class<?> type, Path directory) throws IOException {
        if (!enabled(type) || !Files.exists(directory)) return;
        Path ownership = directory.resolve(OWNERSHIP);
        var record = Files.isRegularFile(ownership) ? JSON.readTree(Files.readString(ownership)) : JSON.createObjectNode();
        for (String name : MANIFESTS) {
            Path file = directory.resolve(name);
            if (Files.exists(file) && Files.size(file) > 0 && (!record.has(name) || !Files.readString(file).equals(record.get(name).asString())))
                throw new IllegalStateException("Preserve and review existing configuration before application-only generation: " + file);
        }
        Files.walkFileTree(directory, new java.nio.file.SimpleFileVisitor<Path>() {
            @Override public java.nio.file.FileVisitResult preVisitDirectory(Path path, java.nio.file.attribute.BasicFileAttributes attrs) {
                String name = path.getFileName().toString();
                return Set.of("node_modules", ".git", ".angular", "dist").contains(name)
                    ? java.nio.file.FileVisitResult.SKIP_SUBTREE : java.nio.file.FileVisitResult.CONTINUE;
            }
            @Override public java.nio.file.FileVisitResult visitFile(Path path, java.nio.file.attribute.BasicFileAttributes attrs) {
                String name = path.getFileName().toString();
                if (name.endsWith(".spec.ts") || name.endsWith(".test.ts") || name.startsWith("eslint.config.")
                    || name.startsWith(".eslintrc") || name.startsWith("karma.conf.") || name.equals("tsconfig.spec.json"))
                    throw new IllegalStateException("Existing test/lint configuration requires a full toolchain: " + path);
                return java.nio.file.FileVisitResult.CONTINUE;
            }
        });
    }
}

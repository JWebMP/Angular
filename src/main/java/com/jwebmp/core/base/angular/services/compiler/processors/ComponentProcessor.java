package com.jwebmp.core.base.angular.services.compiler.processors;

import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.services.interfaces.IComponent;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import com.jwebmp.core.base.angular.services.compiler.files.TypeScriptFileManager;
import io.github.classgraph.ClassInfo;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Responsible for batch processing of components
 */
@Log4j2
public class ComponentProcessor {
    private final INgApp<?> app;
    private final TypeScriptFileManager fileManager;
    private Predicate<Class<?>> packageFilter;
    private Predicate<ClassInfo> packageFilterClassInfo;

    /**
     * Constructor
     *
     * @param app The Angular application
     * @param fileManager The TypeScript file manager
     */
    public ComponentProcessor(INgApp<?> app, TypeScriptFileManager fileManager) {
        this.app = app;
        this.fileManager = fileManager;

        // Initialize the package filters after app is set
        this.packageFilter = a -> {
            String packageName = a.getPackageName();
            return packageName.startsWith("com.jwebmp") ||
                    packageName.startsWith("com.guicedee") ||
                    packageName.startsWith(app.getClass().getPackageName());
        };

        this.packageFilterClassInfo = a -> {
            String packageName = a.getPackageName();
            return packageName.startsWith("com.jwebmp") ||
                    packageName.startsWith("com.guicedee") ||
                    packageName.startsWith(app.getClass().getPackageName());
        };
    }

    /**
     * Processes multiple components at once, generating TypeScript and writing to files
     *
     * @param components The components to process
     * @param forceWrite Whether to force writing files even if content hasn't changed
     * @return Map of components to their written files
     */
    public Map<IComponent<?>, File> processComponents(Collection<IComponent<?>> components, boolean forceWrite) {
        return fileManager.processComponents(components, forceWrite);
    }

    /**
     * Processes all components of a specific type
     *
     * @param componentType The type of components to process
     * @param forceWrite    Whether to force writing files even if content hasn't changed
     * @return Map of components to their written files
     */
    public <T> Map<IComponent<?>, File> processComponentsOfType(Class<T> componentType, boolean forceWrite) {
        List<IComponent<?>> components = new ArrayList<>();

        // Find all components of the specified type
        for (ClassInfo classInfo : IGuiceContext.instance().getScanResult().getAllClasses()) {
            try {
                Class<?> clazz = classInfo.loadClass();
                if (componentType.isAssignableFrom(clazz) && !clazz.isInterface() && !clazz.isAnnotation()) {
                    try {
                        IComponent<?> component = (IComponent<?>) IGuiceContext.get(clazz);
                        components.add(component);
                    } catch (Exception e) {
                        // Continue to next class
                    }
                }
            } catch (Exception e) {
                // Continue to next class
            }
        }

        return processComponents(components, forceWrite);
    }

    /**
     * Gets the package filter predicate
     *
     * @return The package filter predicate
     */
    public Predicate<Class<?>> getPackageFilter() {
        return packageFilter;
    }

    /**
     * Gets the package filter predicate for ClassInfo
     *
     * @return The package filter predicate for ClassInfo
     */
    public Predicate<ClassInfo> getPackageFilterClassInfo() {
        return packageFilterClassInfo;
    }
}

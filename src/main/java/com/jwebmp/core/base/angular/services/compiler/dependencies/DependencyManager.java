package com.jwebmp.core.base.angular.services.compiler.dependencies;

import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import com.jwebmp.core.base.angular.services.compiler.setup.AngularAppSetup;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;

/**
 * Responsible for dependency management and build process
 */
@Log4j2
public class DependencyManager {
    private final INgApp<?> app;

    /**
     * Constructor
     *
     * @param app The Angular application
     */
    public DependencyManager(INgApp<?> app) {
        this.app = app;
    }

    /**
     * Installs dependencies for the Angular application
     *
     * @param appBaseDirectory The application base directory
     */
    public void installDependencies(File appBaseDirectory) {
        log.info("Installing dependencies for Angular application in {}", appBaseDirectory.getAbsolutePath());
        AngularAppSetup.installDependencies(appBaseDirectory);
    }

    /**
     * Builds the Angular application
     *
     * @param appBaseDirectory The application base directory
     */
    public void buildAngularApp(File appBaseDirectory) {
        log.info("Building Angular application in {}", appBaseDirectory.getAbsolutePath());
        AngularAppSetup.buildAngularApp(appBaseDirectory);
    }

    /**
     * Checks if the operating system is supported
     *
     * @return Whether the operating system is supported
     */
    public boolean isOsSupported() {
        return SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_LINUX;
    }

    /**
     * Gets the operating system name
     *
     * @return The operating system name
     */
    public String getOsName() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return "Windows";
        } else if (SystemUtils.IS_OS_LINUX) {
            return "Linux";
        } else {
            return "Unsupported OS";
        }
    }
}
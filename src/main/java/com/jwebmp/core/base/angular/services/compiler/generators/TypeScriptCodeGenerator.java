package com.jwebmp.core.base.angular.services.compiler.generators;

import com.jwebmp.core.base.angular.client.AppUtils;
import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Responsible for generating TypeScript code for different component types
 */
@Log4j2
public class TypeScriptCodeGenerator
{
    // Cache for generated TypeScript code
    private final Map<String, String> typeScriptCache = new HashMap<>();
    private final INgApp<?> app;
    private final NgApp ngApp;

    /**
     * Constructor
     *
     * @param app The Angular application
     */
    public TypeScriptCodeGenerator(INgApp<?> app)
    {
        this.app = app;
        this.ngApp = app.getClass()
                        .getAnnotation(NgApp.class);
    }

    /**
     * Generates TypeScript code for a single component
     *
     * @param component The component to generate TypeScript for
     * @return The generated TypeScript code
     */
    public String generateTypeScriptForComponent(IComponent<?> component)
    {
        String cacheKey = component.getClass()
                                   .getName();

        // Check if we have a cached version
        if (typeScriptCache.containsKey(cacheKey))
        {
            log.debug("Using cached TypeScript for component {}", component.getClass()
                                                                           .getSimpleName());
            return typeScriptCache.get(cacheKey);
        }

        try
        {
            String typeScript = generateTypeScriptByComponentType(component);

            // Cache the result
            typeScriptCache.put(cacheKey, typeScript);

            return typeScript;
        }
        catch (IOException e)
        {
            log.error("Error generating TypeScript for component {}", component.getClass()
                                                                               .getName(), e);
            return "";
        }
    }

    /**
     * Generates TypeScript code based on the component type
     *
     * @param component The component to generate TypeScript for
     * @return The generated TypeScript code
     * @throws IOException If an error occurs during generation
     */
    private String generateTypeScriptByComponentType(IComponent<?> component) throws IOException
    {
        if (component instanceof INgComponent<?>)
        {
            return renderComponentTS((INgComponent<?>) component).toString();
        }
        else if (component instanceof INgDirective<?>)
        {
            return renderDirectiveTS((INgDirective<?>) component).toString();
        }
        else if (component instanceof INgDataService<?>)
        {
            return renderServiceTS((INgDataService<?>) component).toString();
        }
        else if (component instanceof INgModule<?>)
        {
            return renderModuleTS((INgModule<?>) component).toString();
        }
        else if (component instanceof INgDataType<?>)
        {
            return renderDataTypeTS((INgDataType<?>) component).toString();
        }
        else if (component instanceof INgProvider<?>)
        {
            return renderProviderTS((INgProvider<?>) component).toString();
        }
        else if (component instanceof INgServiceProvider<?>)
        {
            return renderServiceProviderTS((INgServiceProvider<?>) component).toString();
        }
        else
        {
            return component.renderClassTs()
                            .toString();
        }
    }

    /**
     * Clears the TypeScript cache for a specific component or all components if null
     *
     * @param component The component to clear the cache for, or null to clear all
     */
    public void clearTypeScriptCache(IComponent<?> component)
    {
        if (component == null)
        {
            log.debug("Clearing entire TypeScript cache");
            typeScriptCache.clear();
        }
        else
        {
            String cacheKey = component.getClass()
                                       .getName();
            log.debug("Clearing TypeScript cache for component {}", component.getClass()
                                                                             .getSimpleName());
            typeScriptCache.remove(cacheKey);
        }
    }

    /**
     * Renders TypeScript for a data type component
     *
     * @param component The data type component
     * @return The rendered TypeScript
     * @throws IOException If an error occurs during rendering
     */
    public StringBuilder renderDataTypeTS(INgDataType<?> component) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append(component.renderClassTs());
        return sb;
    }

    /**
     * Renders TypeScript for a service provider component
     *
     * @param component The service provider component
     * @return The rendered TypeScript
     * @throws IOException If an error occurs during rendering
     */
    public StringBuilder renderServiceProviderTS(INgServiceProvider<?> component) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append(component.renderClassTs());
        return sb;
    }

    /**
     * Renders TypeScript for a provider component
     *
     * @param component The provider component
     * @return The rendered TypeScript
     * @throws IOException If an error occurs during rendering
     */
    public StringBuilder renderProviderTS(INgProvider<?> component) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append(component.renderClassTs());
        return sb;
    }

    /**
     * Renders TypeScript for a service component
     *
     * @param component The service component
     * @return The rendered TypeScript
     * @throws IOException If an error occurs during rendering
     */
    public StringBuilder renderServiceTS(INgDataService<?> component) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append(component.renderClassTs());
        return sb;
    }

    /**
     * Renders TypeScript for a directive component
     *
     * @param component The directive component
     * @return The rendered TypeScript
     * @throws IOException If an error occurs during rendering
     */
    public StringBuilder renderDirectiveTS(INgDirective<?> component) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append(component.renderClassTs());
        return sb;
    }

    /**
     * Renders TypeScript for a component
     *
     * @param component The component
     * @return The rendered TypeScript
     * @throws IOException If an error occurs during rendering
     */
    public StringBuilder renderComponentTS(INgComponent<?> component) throws IOException
    {
        // Check if the component has the NgComponent annotation
        if (!component.getClass()
                      .isAnnotationPresent(com.jwebmp.core.base.angular.client.annotations.angular.NgComponent.class))
        {
            //do not render angular typescripts for components that are not ng components
            return new StringBuilder();
        }

        // If the component has the NgComponent annotation, use the standard rendering
        StringBuilder sb = new StringBuilder();
        sb.append(component.renderClassTs());
        return sb;
    }

    /**
     * Renders TypeScript for a module component
     *
     * @param component The module component
     * @return The rendered TypeScript
     * @throws IOException If an error occurs during rendering
     */
    public StringBuilder renderModuleTS(INgModule<?> component) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        component.setApp(app);
        sb.append(component.renderClassTs());
        return sb;
    }

    /**
     * Renders the main.ts file for the Angular application
     *
     * @param appNgApp The Angular application
     * @return The rendered TypeScript
     * @throws IOException If an error occurs during rendering
     */
    public StringBuilder renderAppTS(INgApp<?> appNgApp) throws IOException
    {
        StringBuilder sb = new StringBuilder();

        // Get the boot component class
        var bootComponentClass = ngApp.bootComponent();

        // Get the app directory
        var appDirectory = AppUtils.getAppMainTSPath((Class<? extends INgApp<?>>) app.getClass(), true);

        // Generate import statements using anonymous inner class
        List<NgImportReference> importReferences = new ImportsStatementsComponent()
        {
            // Anonymous inner class implementation
        }.putRelativeLinkInMap(appDirectory, bootComponentClass);

        // Add import for the boot component
        if (!importReferences.isEmpty())
        {
            NgImportReference firstImport = importReferences.get(0);
            sb.append("import {")
              .append(firstImport.value())
              .append("} from '")
              .append(firstImport.reference())
              .append("';\n");
        }

        // Add standard imports and bootstrap code
        sb.append("""
                  import {bootstrapApplication} from '@angular/platform-browser';
                  import {appConfig} from './app/app.config';
                  
                  bootstrapApplication(""")
          .append(ngApp.bootComponent()
                       .getSimpleName())
          .append("\n")
          .append(", appConfig).catch((err) => console.error(err));");

        return sb;
    }

    /**
     * Writes the main.ts file for the Angular application
     *
     * @param appNgApp The Angular application
     * @throws IOException If an error occurs during writing
     */
    public void writeAppTS(INgApp<?> appNgApp) throws IOException
    {
        Class<? extends INgApp<?>> appClass = (Class<? extends INgApp<?>>) appNgApp.getClass();
        File mainTsFile = AppUtils.getAppMainTSPath(appClass, true);

        // Generate the main.ts content
        StringBuilder mainTsContent = renderAppTS(appNgApp);

        // Write the file
        log.debug("Writing out angular main.ts file - {}", mainTsFile.getAbsolutePath());
        try
        {
            FileUtils.writeStringToFile(mainTsFile, mainTsContent.toString(), UTF_8, false);
        }
        catch (IOException e)
        {
            log.error("Unable to write out angular main.ts file", e);
            throw e;
        }
    }
}

package com.jwebmp.core.base.angular.services.compiler.files;

import com.jwebmp.core.base.angular.client.AppUtils;
import com.jwebmp.core.base.angular.client.services.interfaces.IComponent;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import com.jwebmp.core.base.angular.services.compiler.generators.TypeScriptCodeGenerator;
import com.jwebmp.core.base.angular.services.compiler.validators.TypeScriptCodeValidator;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Responsible for file management (getting file paths, writing to files)
 */
@Log4j2
public class TypeScriptFileManager
{
    private final INgApp<?> app;
    private final TypeScriptCodeGenerator codeGenerator;
    private final TypeScriptCodeValidator codeValidator;

    // Cache for component file paths
    private final Map<String, File> filePathCache = new HashMap<>();

    /**
     * Constructor
     *
     * @param app           The Angular application
     * @param codeGenerator The TypeScript code generator
     * @param codeValidator The TypeScript code validator
     */
    public TypeScriptFileManager(INgApp<?> app, TypeScriptCodeGenerator codeGenerator, TypeScriptCodeValidator codeValidator)
    {
        this.app = app;
        this.codeGenerator = codeGenerator;
        this.codeValidator = codeValidator;
    }

    /**
     * Gets the file path for a component
     *
     * @param component The component to get the file path for
     * @return The file path
     */
    public File getComponentFilePath(IComponent<?> component)
    {
        String cacheKey = component.getClass()
                                   .getName();

        // Check if we have a cached version
        if (filePathCache.containsKey(cacheKey))
        {
            log.debug("Using cached file path for component {}", component.getClass()
                                                                          .getSimpleName());
            return filePathCache.get(cacheKey);
        }

        try
        {
            File filePath = AppUtils.getFile((Class<? extends INgApp<?>>) app.getClass(), component.getClass(), ".ts");

            // Cache the result
            if (filePath != null)
            {
                filePathCache.put(cacheKey, filePath);
            }

            return filePath;
        }
        catch (Exception e)
        {
            log.error("Error getting file path for component {}", component.getClass()
                                                                           .getName(), e);
            return null;
        }
    }

    /**
     * Writes TypeScript code for a component to a file
     *
     * @param component The component to generate TypeScript for
     * @return The file that was written
     */
    public File writeComponentToFile(IComponent<?> component)
    {
        return writeComponentToFile(component, true);
    }

    /**
     * Writes TypeScript code for a component to a file, forcing a write even if the content hasn't changed
     *
     * @param component  The component to generate TypeScript for
     * @param forceWrite Whether to force writing the file even if content hasn't changed
     * @return The file that was written
     */
    public File writeComponentToFile(IComponent<?> component, boolean forceWrite)
    {
        try
        {
            File file = getComponentFilePath(component);
            if (file != null)
            {
                // Create parent directories if they don't exist
                FileUtils.forceMkdirParent(file);

                // Generate TypeScript code (uses cache if available)
                String typeScript = codeGenerator.generateTypeScriptForComponent(component);

                // Validate and format the TypeScript code
                typeScript = codeValidator.validateTypeScript(typeScript, component);
                typeScript = codeValidator.formatTypeScript(typeScript);

                // Check if the file exists and if the content is the same
                boolean shouldWrite = forceWrite;
                if (!forceWrite && file.exists())
                {
                    String existingContent = FileUtils.readFileToString(file, UTF_8);
                    if (!existingContent.equals(typeScript))
                    {
                        shouldWrite = true;
                    }
                }

                // Write the file if needed
                if (shouldWrite || !file.exists())
                {
                    FileUtils.writeStringToFile(file, typeScript, UTF_8, false);
                    log.debug("Wrote TypeScript file for component {}: {}",
                            component.getClass()
                                     .getSimpleName(), file.getAbsolutePath());
                }
                else
                {
                    log.debug("File content unchanged for component {}, skipping write",
                            component.getClass()
                                     .getSimpleName());
                }

                return file;
            }
            return null;
        }
        catch (IOException e)
        {
            log.error("Error writing component {} to file", component.getClass()
                                                                     .getName(), e);
            return null;
        }
    }

    /**
     * Processes multiple components at once, generating TypeScript and writing to files
     *
     * @param components The components to process
     * @param forceWrite Whether to force writing files even if content hasn't changed
     * @return Map of components to their written files
     */
    public Map<IComponent<?>, File> processComponents(Collection<IComponent<?>> components, boolean forceWrite)
    {
        Map<IComponent<?>, File> result = new HashMap<>();

        if (components == null || components.isEmpty())
        {
            log.warn("No components provided to process");
            return result;
        }

        log.info("Processing {} components", components.size());

        // Process each component
        for (IComponent<?> component : components)
        {
            try
            {
                File file = writeComponentToFile(component, forceWrite);
                if (file != null)
                {
                    result.put(component, file);
                }
            }
            catch (Exception e)
            {
                log.error("Error processing component {}", component.getClass()
                                                                    .getName(), e);
            }
        }

        log.info("Processed {} components successfully", result.size());

        return result;
    }

    /**
     * Clears the file path cache for a specific component or all components if null
     *
     * @param component The component to clear the cache for, or null to clear all
     */
    public void clearFilePathCache(IComponent<?> component)
    {
        if (component == null)
        {
            log.debug("Clearing entire file path cache");
            filePathCache.clear();
        }
        else
        {
            String cacheKey = component.getClass()
                                       .getName();
            log.debug("Clearing file path cache for component {}", component.getClass()
                                                                            .getSimpleName());
            filePathCache.remove(cacheKey);
        }
    }
}

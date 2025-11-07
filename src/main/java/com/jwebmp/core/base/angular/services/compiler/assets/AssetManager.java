package com.jwebmp.core.base.angular.services.compiler.assets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.guicedee.client.CallScoper;
import com.guicedee.client.IGuiceContext;
import com.jwebmp.core.base.angular.client.AppUtils;
import com.jwebmp.core.base.angular.client.annotations.angularconfig.NgAsset;
import com.jwebmp.core.base.angular.client.annotations.angularconfig.NgScript;
import com.jwebmp.core.base.angular.client.annotations.angularconfig.NgStyleSheet;
import com.jwebmp.core.base.angular.client.services.AnnotationHelper;
import com.jwebmp.core.base.angular.client.services.interfaces.INgApp;
import com.jwebmp.core.base.angular.services.RenderedAssets;
import com.jwebmp.core.base.angular.typescript.JWebMP.ResourceLocator;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.DefaultObjectMapper;
import static com.jwebmp.core.base.angular.client.services.interfaces.IComponent.currentAppFile;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Responsible for asset management (stylesheets, scripts, resources)
 */
@Log4j2
public class AssetManager
{
    private final INgApp<?> app;
    private final Set<String> assetStringBuilder = new LinkedHashSet<>();
    private final Set<String> stylesGlobal = new LinkedHashSet<>();
    private final Set<String> scripts = new LinkedHashSet<>();

    /**
     * Constructor
     *
     * @param app The Angular application
     */
    public AssetManager(INgApp<?> app)
    {
        this.app = app;
    }

    /**
     * Processes assets
     *
     * @return Map of named assets
     */
    public Map<String, String> processAssets()
    {
        Map<String, String> namedAssets = new HashMap<>();
        List<NgAsset> assets = IGuiceContext.get(AnnotationHelper.class)
                                            .getGlobalAnnotations(NgAsset.class);

        // Process assets
        for (NgAsset ngAsset : assets)
        {
            String name = ngAsset.name();
            if (Strings.isNullOrEmpty(ngAsset.name()))
            {
                name = ngAsset.value();
            }
            namedAssets.put(name, ngAsset.value());
        }

        // Process asset replacements
        for (NgAsset ngAsset : assets)
        {
            if (ngAsset.replaces().length > 0)
            {
                for (String replace : ngAsset.replaces())
                {
                    namedAssets.put(replace, ngAsset.value());
                }
            }
        }

        return namedAssets;
    }

    /**
     * Processes stylesheets
     */
    public void processStylesheets()
    {
        Map<String, String> namedStylesheets = new LinkedHashMap<>();
        List<NgStyleSheet> ngStyleSheets = IGuiceContext.get(AnnotationHelper.class)
                                                        .getGlobalAnnotations(NgStyleSheet.class);

        // Sort stylesheets by sort order
        ngStyleSheets.sort(Comparator.comparingInt(NgStyleSheet::sortOrder));

        // Process stylesheets
        for (NgStyleSheet ngAsset : ngStyleSheets)
        {
            String name = ngAsset.name();
            if (Strings.isNullOrEmpty(ngAsset.name()))
            {
                name = ngAsset.value();
            }
            namedStylesheets.put(name, ngAsset.value());
        }

        // Process stylesheet replacements
        for (NgStyleSheet ngAsset : ngStyleSheets)
        {
            if (ngAsset.replaces().length > 0)
            {
                for (String replace : ngAsset.replaces())
                {
                    namedStylesheets.put(replace, ngAsset.value());
                }
            }
        }

        // Add stylesheets to global styles
        for (String value : namedStylesheets.values())
        {
            stylesGlobal.add(value);
        }

        // Add app stylesheets
        for (String stylesheet : app.stylesheets())
        {
            stylesGlobal.add("public/assets/" + stylesheet);
        }
    }

    /**
     * Processes scripts
     */
    public void processScripts()
    {
        Map<String, String> namedScripts = new LinkedHashMap<>();
        List<NgScript> allAnnotations = IGuiceContext.get(AnnotationHelper.class)
                                                     .getGlobalAnnotations(NgScript.class);

        // Sort scripts by sort order
        allAnnotations.sort(Comparator.comparingInt(NgScript::sortOrder));

        // Process scripts
        for (NgScript ngAsset : allAnnotations)
        {
            String name = ngAsset.name();
            if (Strings.isNullOrEmpty(ngAsset.name()))
            {
                name = ngAsset.value();
            }
            namedScripts.put(name, ngAsset.value());
        }

        // Process script replacements
        for (NgScript ngAsset : allAnnotations)
        {
            if (ngAsset.replaces().length > 0)
            {
                for (String replace : ngAsset.replaces())
                {
                    namedScripts.put(replace, ngAsset.value());
                }
            }
        }

        // Add scripts to global scripts
        for (String value : namedScripts.values())
        {
            scripts.add(value);
        }

        // Add app scripts
        for (String script : app.scripts())
        {
            scripts.add("public/assets/" + script);
        }
    }

    /**
     * Processes resources
     *
     * @param appClass The Angular application class
     * @param scan     The scan result
     * @throws IOException If an error occurs during processing
     */
    public void processResources(Class<? extends INgApp<?>> appClass, ScanResult scan) throws IOException
    {
        log.info("Writing out src/assets/ resources...");

        // Process assets
        for (Resource resource : scan.getResourcesMatchingWildcard("assets/**"))
        {
            AppUtils.saveAsset(appClass, resource.getURL()
                                                 .openStream(), resource.getPath());
        }

        // Process src/assets
        for (Resource resource : scan.getResourcesMatchingWildcard("src/assets/**"))
        {
            AppUtils.saveAsset(appClass, resource.getURL()
                                                 .openStream(), resource.getPath());
        }

        // Process src/public
        for (Resource resource : scan.getResourcesMatchingWildcard("src/public/**"))
        {
            AppUtils.saveAsset(appClass, resource.getURL()
                                                 .openStream(), resource.getPath());
        }

        // Process public
        for (Resource resource : scan.getResourcesMatchingWildcard("public/**"))
        {
            AppUtils.saveAsset(appClass, resource.getURL()
                                                 .openStream(), resource.getPath());
        }
    }

    /**
     * Gets the global styles
     *
     * @return The global styles
     */
    public Set<String> getStylesGlobal()
    {
        return stylesGlobal;
    }

    /**
     * Gets the scripts
     *
     * @return The scripts
     */
    public Set<String> getScripts()
    {
        return scripts;
    }

    /**
     * Gets the asset string builder
     *
     * @return The asset string builder
     */
    public Set<String> getAssetStringBuilder()
    {
        return assetStringBuilder;
    }

    /**
     * Processes app resources
     *
     * @param appClass The Angular application class
     * @param scan     The scan result
     * @throws IOException If an error occurs during processing
     */
    public void processAppResources(Class<? extends INgApp<?>> appClass, ScanResult scan) throws IOException
    {
        log.info("Loading resources from app directory");
        for (Resource resource : scan.getResourcesMatchingWildcard("app/**"))
        {
            String assetLocation = resource.getPathRelativeToClasspathElement();
            AppUtils.saveAppResourceFile(appClass, resource.getURL()
                                                           .openStream(), assetLocation);
        }
    }

    /**
     * Renders Angular application files
     *
     * @param currentApp  The current app file
     * @param appClass    The Angular application class
     * @param namedAssets The named assets
     * @param app         The Angular application
     * @param om          The object mapper
     * @throws IOException If an error occurs during rendering
     */
    public void renderAngularApplicationFiles(File currentApp, Class<? extends INgApp<?>> appClass, Map<String, String> namedAssets, INgApp<?> app, ObjectMapper om) throws IOException
    {
        currentAppFile.set(currentApp);
        CallScoper scoper = IGuiceContext.get(CallScoper.class);
        scoper.enter();
        try
        {
            log.trace("Registering Assets...");
            List<String> assetList = AppUtils.getAssetList(appClass);
            if (assetList != null)
            {
                assetStringBuilder.addAll(assetList);
            }
            @SuppressWarnings({"rawtypes", "unchecked"})
            Set<RenderedAssets> renderedAssets = IGuiceContext.loaderToSet(ServiceLoader.load(RenderedAssets.class));
            for (RenderedAssets<?> renderedAsset : renderedAssets)
            {
                for (String asset : renderedAsset.assets())
                {
                    namedAssets.put(asset, asset);
                }
            }
            assetStringBuilder.addAll(namedAssets.values());
            assetStringBuilder.addAll(app.assets());
            StringBuilder assetsAngular19 = new StringBuilder();
            assetsAngular19.append("""
                    
                                [
                                  {
                                    "glob": "**/*",
                                    "input": "public"
                                  }
                                ]
                    """);
            assetStringBuilder.removeIf(stylesGlobal::contains);

            String angularTemplate = IOUtils.toString(Objects.requireNonNull(ResourceLocator.class.getResourceAsStream("angular.json")), UTF_8);

            angularTemplate = angularTemplate.replace("/*BuildAssets*/", assetsAngular19);

            // Convert stylesGlobal to JSON string manually
            StringBuilder stylesJson = new StringBuilder("[");
            int styleCount = 0;
            for (String style : stylesGlobal)
            {
                if (styleCount > 0)
                {
                    stylesJson.append(",");
                }
                stylesJson.append("\n  \"")
                          .append(style.replace("\"", "\\\""))
                          .append("\"");
                styleCount++;
            }
            stylesJson.append("\n]");
            angularTemplate = angularTemplate.replace("/*BuildStylesSCSS*/", stylesJson.toString());

            // Convert scripts to JSON string manually
            StringBuilder scriptsJson = new StringBuilder("[");
            int scriptCount = 0;
            for (String script : scripts)
            {
                if (scriptCount > 0)
                {
                    scriptsJson.append(",");
                }
                scriptsJson.append("\n  \"")
                           .append(script.replace("\"", "\\\""))
                           .append("\"");
                scriptCount++;
            }
            scriptsJson.append("\n]");
            angularTemplate = angularTemplate.replace("/*BuildScripts*/", scriptsJson.toString());

            // Replace MainTSFile with a simple string
            angularTemplate = angularTemplate.replace("/*MainTSFile*/", "\"src/main.ts\"");

            File angularFile = AppUtils.getAngularJsonPath(appClass, true);
            FileUtils.writeStringToFile(angularFile, angularTemplate, UTF_8, false);
        }
        catch (Throwable e)
        {
            log.error("Unable to write out angular.json file", e);
        }
        finally
        {
            scoper.exit();
        }
    }
}

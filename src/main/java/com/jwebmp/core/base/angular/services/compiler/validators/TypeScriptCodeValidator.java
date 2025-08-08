package com.jwebmp.core.base.angular.services.compiler.validators;

import com.jwebmp.core.base.angular.client.services.interfaces.IComponent;
import lombok.extern.log4j.Log4j2;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Responsible for validating and formatting TypeScript code
 */
@Log4j2
public class TypeScriptCodeValidator
{

    /**
     * Validates and fixes common issues in TypeScript code
     *
     * @param typeScript The TypeScript code to validate
     * @param component  The component that generated the TypeScript
     * @return The validated and fixed TypeScript code
     */
    public String validateTypeScript(String typeScript, IComponent<?> component)
    {
        if (typeScript == null || typeScript.isEmpty())
        {
            log.warn("Empty TypeScript generated for component {}", component.getClass()
                                                                             .getName());
            return "";
        }

        // Fix missing semicolons at the end of statements
        // typeScript = fixMissingSemicolons(typeScript);

        // Fix duplicate imports
        //typeScript = fixDuplicateImports(typeScript);

        // Fix trailing commas in arrays and objects
        //typeScript = fixTrailingCommas(typeScript);

        return typeScript;
    }

    /**
     * Fixes missing semicolons at the end of statements
     *
     * @param typeScript The TypeScript code to fix
     * @return The fixed TypeScript code
     */
    public String fixMissingSemicolons(String typeScript)
    {
        // This implementation adds semicolons to lines that should have them but don't
        String[] lines = typeScript.split("\n");
        StringBuilder fixed = new StringBuilder();

        for (String line : lines)
        {
            String trimmed = line.trim();

            // Skip lines that already end with semicolons, braces, or are empty
            if (trimmed.isEmpty() ||
                    trimmed.endsWith(";") ||
                    trimmed.endsWith("{") ||
                    trimmed.endsWith("}") ||
                    trimmed.endsWith(",") ||
                    trimmed.startsWith("@") ||       // Skip decorators
                    trimmed.startsWith("import") ||  // Imports are handled separately
                    trimmed.startsWith("export") && !trimmed.contains("=") && !trimmed.contains("(") || // Skip export declarations without assignments or methods
                    trimmed.startsWith("//") ||      // Skip comments
                    trimmed.startsWith("/*") ||
                    trimmed.endsWith("*/"))
            {
                fixed.append(line)
                     .append("\n");
                continue;
            }

            // Add semicolon to lines that need it
            // This includes:
            // - Lines with assignments (=)
            // - Lines with method calls (containing parentheses)
            // - Lines with return statements
            // - Lines with increment/decrement operators (++, --)
            // - Lines with this. references (likely method calls or property access)
            if (trimmed.contains("=") ||
                    trimmed.contains("(") ||         // Any line with parentheses (method calls)
                    trimmed.contains("return ") ||
                    trimmed.contains("++") ||
                    trimmed.contains("--") ||
                    trimmed.contains("this."))
            {
                fixed.append(line)
                     .append(";")
                     .append("\n");
            }
            else
            {
                fixed.append(line)
                     .append("\n");
            }
        }

        return fixed.toString();
    }

    /**
     * Fixes duplicate imports in TypeScript code
     *
     * @param typeScript The TypeScript code to fix
     * @return The fixed TypeScript code
     */
    public String fixDuplicateImports(String typeScript)
    {
        String[] lines = typeScript.split("\n");
        Set<String> uniqueImports = new LinkedHashSet<>();
        StringBuilder nonImports = new StringBuilder();
        boolean importsSection = true;

        for (String line : lines)
        {
            if (line.trim()
                    .startsWith("import "))
            {
                uniqueImports.add(line.trim());
            }
            else
            {
                importsSection = false;
                nonImports.append(line)
                          .append("\n");
            }
        }

        // Rebuild the TypeScript with unique imports
        StringBuilder fixed = new StringBuilder();
        for (String importLine : uniqueImports)
        {
            fixed.append(importLine)
                 .append("\n");
        }

        // Add a blank line between imports and code if needed
        if (!uniqueImports.isEmpty() && nonImports.length() > 0)
        {
            fixed.append("\n");
        }

        fixed.append(nonImports);

        return fixed.toString();
    }

    /**
     * Fixes trailing commas in arrays and objects
     *
     * @param typeScript The TypeScript code to fix
     * @return The fixed TypeScript code
     */
    public String fixTrailingCommas(String typeScript)
    {
        // This is a simple implementation that could be enhanced
        // It removes trailing commas before closing brackets

        // Fix arrays
        typeScript = typeScript.replaceAll(",\\s*\\]", "]");

        // Fix objects
        typeScript = typeScript.replaceAll(",\\s*\\}", "}");

        return typeScript;
    }

    /**
     * Formats TypeScript code to make it more readable
     *
     * @param typeScript The TypeScript code to format
     * @return The formatted TypeScript code
     */
    public String formatTypeScript(String typeScript)
    {
        // Formatter disabled as it was breaking TypeScript files
        return typeScript;
    }
}

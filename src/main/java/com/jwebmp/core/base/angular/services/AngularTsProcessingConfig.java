package com.jwebmp.core.base.angular.services;

import com.guicedee.client.CallScopeProperties;
import com.guicedee.client.IGuiceContext;
import lombok.extern.log4j.Log4j2;

/**
 * Global configuration for enabling/disabling Angular TypeScript processing/rendering.
 * Controlled via either a system property or environment variable.
 *
 * Default: disabled (false)
 *
 * System property: jwebmp.process.angular.ts
 * Environment variable: JWEBMP_PROCESS_ANGULAR_TS
 */
@Log4j2
public final class AngularTsProcessingConfig {
    public static final String SYS_PROP = "jwebmp.process.angular.ts";
    public static final String ENV_VAR = "JWEBMP_PROCESS_ANGULAR_TS";

    private static volatile Boolean cached;

    private AngularTsProcessingConfig() {}

    public static boolean isEnabled() {
        if (cached != null) {
            return cached;
        }
        synchronized (AngularTsProcessingConfig.class) {
            if (cached != null) {
                return cached;
            }
            Boolean value = readFlag();
            cached = value;
            if (Boolean.FALSE.equals(value)) {
                log.info("Angular TypeScript processing is DISABLED (set {} system property or {} env var to true to enable)", SYS_PROP, ENV_VAR);
            } else {
                log.info("Angular TypeScript processing is ENABLED via configuration");
            }
            return cached;
        }
    }

    private static Boolean readFlag() {
        // Priority: CallScopeProperties -> System property -> Environment variable
        try {
            CallScopeProperties csp = IGuiceContext.get(CallScopeProperties.class);
            if (csp != null && csp.getProperties() != null) {
                Object v = csp.getProperties().get(SYS_PROP);
                if (v != null) {
                    return parseBoolean(v.toString());
                }
            }
        } catch (Throwable ignored) {
            // Context may not be available early; ignore
        }
        String sys = System.getProperty(SYS_PROP);
        if (sys != null) {
            return parseBoolean(sys);
        }
        String env = System.getenv(ENV_VAR);
        if (env != null) {
            return parseBoolean(env);
        }
        // Default false
        return Boolean.FALSE;
    }

    private static boolean parseBoolean(String s) {
        if (s == null) return false;
        String v = s.trim().toLowerCase();
        return v.equals("true") || v.equals("1") || v.equals("yes") || v.equals("y") || v.equals("on");
    }

    // For tests
    public static void override(Boolean value) {
        cached = value;
    }

    public static void clearCache() {
        cached = null;
    }
}

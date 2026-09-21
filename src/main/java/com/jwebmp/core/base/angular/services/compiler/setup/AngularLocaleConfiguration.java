package com.jwebmp.core.base.angular.services.compiler.setup;

import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.client.annotations.angular.NgLocale;

/** App-scoped locale configuration rendered before the application config is evaluated. */
final class AngularLocaleConfiguration
{
    private AngularLocaleConfiguration() {}

    static void append(Class<?> appClass, StringBuilder imports, StringBuilder providers)
    {
        NgLocale locale = appClass.getAnnotation(NgLocale.class);
        NgApp app = appClass.getAnnotation(NgApp.class);
        if (locale == null && app != null)
        {
            locale = app.bootComponent().getAnnotation(NgLocale.class);
        }
        if (locale == null)
        {
            return;
        }
        String id = validate(locale.value());
        String dataLocale = locale.dataLocale().isEmpty() ? id : validate(locale.dataLocale());
        // Angular ships the default English data as en, not en-US.
        if (dataLocale.equals("en-US"))
        {
            dataLocale = "en";
        }
        imports.append("import {LOCALE_ID as jwebmpLocaleId} from '@angular/core';\n")
               .append("import {registerLocaleData as jwebmpRegisterLocaleData} from '@angular/common';\n")
               .append("import jwebmpLocaleData from '@angular/common/locales/").append(dataLocale).append("';\n");
        if (locale.extraData())
        {
            imports.append("import jwebmpLocaleExtraData from '@angular/common/locales/extra/")
                   .append(dataLocale).append("';\n");
        }
        imports.append("jwebmpRegisterLocaleData(jwebmpLocaleData, '").append(id).append("'");
        if (locale.extraData())
        {
            imports.append(", jwebmpLocaleExtraData");
        }
        imports.append(");\n");
        providers.append("{provide: jwebmpLocaleId, useValue: '").append(id).append("'},\n");
        var registered = new java.util.LinkedHashSet<String>();
        registered.add(id.toLowerCase(java.util.Locale.ROOT));
        int index = 0;
        for (String supported : locale.supportedLocales())
        {
            validate(supported);
            if (!registered.add(supported.toLowerCase(java.util.Locale.ROOT)))
            {
                continue;
            }
            String file = supported.equals("en-US") ? "en" : supported;
            String alias = "jwebmpSupportedLocale" + index++;
            imports.append("import ").append(alias).append(" from '@angular/common/locales/")
                   .append(file).append("';\n");
            if (locale.extraData())
            {
                imports.append("import ").append(alias).append("Extra from '@angular/common/locales/extra/")
                       .append(file).append("';\n");
            }
            imports.append("jwebmpRegisterLocaleData(").append(alias).append(", '").append(supported).append("'");
            if (locale.extraData())
            {
                imports.append(", ").append(alias).append("Extra");
            }
            imports.append(");\n");
        }
    }

    private static String validate(String locale)
    {
        if (!locale.matches("[A-Za-z]{2,8}(?:-[A-Za-z0-9]{1,8})*"))
        {
            throw new IllegalArgumentException("Invalid @NgLocale identifier: " + locale);
        }
        return locale;
    }
}

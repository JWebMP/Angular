package com.jwebmp.core.base.angular.services.compiler.setup;

import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.client.annotations.angular.NgLocale;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AngularLocaleConfigurationTest
{
    @NgLocale("en-ZA")
    static class SouthAfricanApp {}

    @NgLocale(value = "fr-CA", dataLocale = "fr", extraData = true)
    static class FrenchApp {}

    @NgLocale("en-US")
    static class EnglishApp {}

    @NgLocale("../bad'")
    static class InvalidApp {}

    @NgLocale("de")
    static class BootComponent extends com.jwebmp.core.base.html.DivSimple<BootComponent>
            implements INgComponent<BootComponent> {}

    @NgApp(value = "test", bootComponent = BootComponent.class)
    static class BootLocaleApp {}

    @NgLocale("en-ZA")
    static class OverrideApp extends BootLocaleApp {}

    @NgLocale(value = "en-ZA", supportedLocales = {"de", "fr", "de", "en-ZA", "en-US"}, extraData = true)
    static class MultiLocaleApp {}

    @NgLocale(value = "en-ZA", supportedLocales = {"../bad"})
    static class InvalidSupportedApp {}

    @Test
    void registersDistinctSupportedLocalesWithoutChangingDefault()
    {
        var imports = new StringBuilder();
        var providers = new StringBuilder();
        AngularLocaleConfiguration.append(MultiLocaleApp.class, imports, providers);
        String output = imports.toString();
        assertTrue(output.contains("jwebmpRegisterLocaleData(jwebmpSupportedLocale0, 'de', jwebmpSupportedLocale0Extra);"));
        assertTrue(output.contains("jwebmpRegisterLocaleData(jwebmpSupportedLocale1, 'fr', jwebmpSupportedLocale1Extra);"));
        assertTrue(output.contains("import jwebmpSupportedLocale2 from '@angular/common/locales/en';"));
        assertFalse(output.contains("jwebmpSupportedLocale3"));
        assertEquals("{provide: jwebmpLocaleId, useValue: 'en-ZA'},\n", providers.toString());
        assertThrows(IllegalArgumentException.class, () -> AngularLocaleConfiguration.append(
                InvalidSupportedApp.class, new StringBuilder(), new StringBuilder()));
    }

    @Test
    void registersDefaultImportAndApplicationProvider()
    {
        var imports = new StringBuilder();
        var providers = new StringBuilder();
        AngularLocaleConfiguration.append(SouthAfricanApp.class, imports, providers);
        assertTrue(imports.toString().contains("import jwebmpLocaleData from '@angular/common/locales/en-ZA';"));
        assertTrue(imports.toString().contains("jwebmpRegisterLocaleData(jwebmpLocaleData, 'en-ZA');"));
        assertEquals("{provide: jwebmpLocaleId, useValue: 'en-ZA'},\n", providers.toString());
    }

    @Test
    void supportsExtraDataAndSeparateDataLocale()
    {
        var imports = new StringBuilder();
        AngularLocaleConfiguration.append(FrenchApp.class, imports, new StringBuilder());
        assertTrue(imports.toString().contains("from '@angular/common/locales/extra/fr';"));
        assertTrue(imports.toString().contains("jwebmpRegisterLocaleData(jwebmpLocaleData, 'fr-CA', jwebmpLocaleExtraData);"));
    }

    @Test
    void resolvesBootFallbackAndInheritedAppOverride()
    {
        var providers = new StringBuilder();
        AngularLocaleConfiguration.append(BootLocaleApp.class, new StringBuilder(), providers);
        assertTrue(providers.toString().contains("useValue: 'de'"));
        providers.setLength(0);
        AngularLocaleConfiguration.append(OverrideApp.class, new StringBuilder(), providers);
        assertTrue(providers.toString().contains("useValue: 'en-ZA'"));
        assertFalse(providers.toString().contains("useValue: 'de'"));
    }

    @Test
    void leavesUnannotatedAppsUntouched()
    {
        var imports = new StringBuilder("existing imports");
        var providers = new StringBuilder("existing providers");
        AngularLocaleConfiguration.append(Object.class, imports, providers);
        assertEquals("existing imports", imports.toString());
        assertEquals("existing providers", providers.toString());
    }

    @Test
    void usesExistingEnglishDataFile()
    {
        var imports = new StringBuilder();
        var providers = new StringBuilder();
        AngularLocaleConfiguration.append(EnglishApp.class, imports, providers);
        assertTrue(imports.toString().contains("from '@angular/common/locales/en';"));
        assertTrue(providers.toString().contains("useValue: 'en-US'"));
    }

    @Test
    void rejectsInvalidIdentifiers()
    {
        assertThrows(IllegalArgumentException.class, () -> AngularLocaleConfiguration.append(
                InvalidApp.class, new StringBuilder(), new StringBuilder()));
    }
}

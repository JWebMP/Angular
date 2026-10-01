package com.jwebmp.core.base.angular.services;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;

class DefinedRouteTest
{
    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void eagerRouteRendersComponentReference() throws Exception
    {
        DefinedRoute<?> route = new DefinedRoute<>().setPath("profile")
                .setComponentName("ProfilePage")
                .setRenderComponent(true);
        String json = mapper.writeValueAsString(route);
        assertTrue(json.contains("\"component\":ProfilePage"), json);
        assertFalse(json.contains("loadComponent"), json);
    }

    @Test
    void lazyRouteRendersDynamicImportInsteadOfComponent() throws Exception
    {
        DefinedRoute<?> route = new DefinedRoute<>().setPath("profile")
                .setComponentName("ProfilePage")
                .setRenderComponent(true)
                .setLazy(true)
                .setLazyImportPath("../../pages/ProfilePage/ProfilePage");
        String json = mapper.writeValueAsString(route);
        assertTrue(json.contains("\"loadComponent\":() => import('../../pages/ProfilePage/ProfilePage').then(m => m.ProfilePage)"), json);
        assertFalse(json.contains("\"component\""), json);
    }

    @Test
    void lazyRouteWithoutResolvedPathFallsBackToEager() throws Exception
    {
        DefinedRoute<?> route = new DefinedRoute<>().setPath("profile")
                .setComponentName("ProfilePage")
                .setRenderComponent(true)
                .setLazy(true);
        String json = mapper.writeValueAsString(route);
        assertTrue(json.contains("\"component\":ProfilePage"), json);
        assertFalse(json.contains("loadComponent"), json);
    }
}

package com.jwebmp.core.base.angular.modules.services.angular;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoutingModuleOptions
{
    private SameUrlNavigationType onSameUrlNavigation;
    private Boolean useHash;

    public SameUrlNavigationType getOnSameUrlNavigation()
    {
        return onSameUrlNavigation;
    }

    public RoutingModuleOptions setOnSameUrlNavigation(SameUrlNavigationType onSameUrlNavigation)
    {
        this.onSameUrlNavigation = onSameUrlNavigation;
        return this;
    }

    public Boolean getUseHash()
    {
        return useHash;
    }

    public RoutingModuleOptions setUseHash(Boolean useHash)
    {
        this.useHash = useHash;
        return this;
    }
}

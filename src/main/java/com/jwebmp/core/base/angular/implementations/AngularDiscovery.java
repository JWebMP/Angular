package com.jwebmp.core.base.angular.implementations;

import com.guicedee.client.services.IGuiceDiscovery;
import com.jwebmp.core.base.angular.modules.services.angular.AngularRoutingModule;

import java.util.List;

/** Angular annotation metadata declared by this module. */
public final class AngularDiscovery implements IGuiceDiscovery<AngularDiscovery>
{
    @Override
    public List<Class<?>> classes()
    {
        return List.of(AngularRoutingModule.class);
    }
}

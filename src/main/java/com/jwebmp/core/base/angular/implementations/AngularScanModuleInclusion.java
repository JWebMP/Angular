package com.jwebmp.core.base.angular.implementations;

import com.guicedee.client.services.config.IGuiceScanModuleInclusions;

import java.util.Set;

public class AngularScanModuleInclusion implements IGuiceScanModuleInclusions<AngularScanModuleInclusion>
{
    @Override
    public Set<String> includeModules()
    {
        return Set.of("com.jwebmp.core.angular");
    }
}

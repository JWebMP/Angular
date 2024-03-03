package com.jwebmp.core.base.angular.implementations;

import com.guicedee.guicedinjection.interfaces.IGuiceScanModuleInclusions;
import jakarta.validation.constraints.*;

import java.util.*;

public class AngularScanModuleInclusion implements IGuiceScanModuleInclusions<AngularScanModuleInclusion>
{
    @Override
    public @NotNull Set<String> includeModules()
    {
        return Set.of("com.jwebmp.core.angular");
    }
}

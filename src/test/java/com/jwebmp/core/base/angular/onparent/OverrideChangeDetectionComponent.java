package com.jwebmp.core.base.angular.onparent;

import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import com.jwebmp.core.base.html.DivSimple;

@NgComponent(value = "test-override-change-detection", standalone = false)
public class OverrideChangeDetectionComponent extends DivSimple<OverrideChangeDetectionComponent>
        implements INgComponent<OverrideChangeDetectionComponent>
{
    @Override
    public NgComponent.ChangeDetection changeDetectionOverride()
    {
        return NgComponent.ChangeDetection.ON_PUSH;
    }
}

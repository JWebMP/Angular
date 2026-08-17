package com.jwebmp.core.base.angular.onparent;

import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.annotations.references.NgComponentReference;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import com.jwebmp.core.base.angular.components.modules.RouterConfig;
import com.jwebmp.core.base.html.DivSimple;

@NgComponent("test-router-config-consumer")
@NgComponentReference(RouterConfig.class)
public class TestRouterConfigConsumerComponent extends DivSimple<TestRouterConfigConsumerComponent>
        implements INgComponent<TestRouterConfigConsumerComponent>
{
    public TestRouterConfigConsumerComponent()
    {
        add("Router config consumer component content");
    }
}

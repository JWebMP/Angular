package com.jwebmp.core.base.angular.onparent;

import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.annotations.references.NgComponentReference;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import com.jwebmp.core.base.html.DivSimple;

/**
 * A test component that references TestParentProvider.
 * The onParent=true fields/imports/signals/methods/constructorParams from TestParentProvider should render here.
 */
@NgComponent("test-provider-consumer")
@NgComponentReference(TestParentProvider.class)
public class TestProviderConsumerComponent extends DivSimple<TestProviderConsumerComponent>
        implements INgComponent<TestProviderConsumerComponent>
{
    public TestProviderConsumerComponent()
    {
        add("Provider consumer component");
    }
}


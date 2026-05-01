package com.jwebmp.core.base.angular.onparent;

import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.annotations.references.NgComponentReference;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import com.jwebmp.core.base.html.DivSimple;

/**
 * A test component that references TestAppConfig via @NgComponentReference.
 * The onParent=true fields/imports from TestAppConfig should render on this component's TypeScript.
 */
@NgComponent("test-config-consumer")
@NgComponentReference(TestAppConfig.class)
public class TestConfigConsumerComponent extends DivSimple<TestConfigConsumerComponent>
        implements INgComponent<TestConfigConsumerComponent>
{
    public TestConfigConsumerComponent()
    {
        add("Config consumer component content");
    }
}


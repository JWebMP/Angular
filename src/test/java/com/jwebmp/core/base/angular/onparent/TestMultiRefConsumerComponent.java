package com.jwebmp.core.base.angular.onparent;

import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.annotations.references.NgComponentReference;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import com.jwebmp.core.base.html.DivSimple;

/**
 * A test component that references multiple classes with onParent=true annotations.
 * All onParent items from all references should propagate here.
 */
@NgComponent("test-multi-ref-consumer")
@NgComponentReference(TestAppConfig.class)
@NgComponentReference(TestParentProvider.class)
@NgComponentReference(TestHighlightDirective.class)
public class TestMultiRefConsumerComponent extends DivSimple<TestMultiRefConsumerComponent>
        implements INgComponent<TestMultiRefConsumerComponent>
{
    public TestMultiRefConsumerComponent()
    {
        add("Multi-reference consumer component");
    }
}


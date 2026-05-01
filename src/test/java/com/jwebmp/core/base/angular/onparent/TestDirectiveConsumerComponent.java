package com.jwebmp.core.base.angular.onparent;

import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.annotations.references.NgComponentReference;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import com.jwebmp.core.base.html.DivSimple;

/**
 * A test component that references a directive with onParent annotations.
 * The onParent=true fields/imports/methods/constructorBodies from TestHighlightDirective should propagate here.
 */
@NgComponent("test-directive-consumer")
@NgComponentReference(TestHighlightDirective.class)
public class TestDirectiveConsumerComponent extends DivSimple<TestDirectiveConsumerComponent>
        implements INgComponent<TestDirectiveConsumerComponent>
{
    public TestDirectiveConsumerComponent()
    {
        add("Directive consumer component");
    }
}


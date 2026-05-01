package com.jwebmp.core.base.angular.onparent;

import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.annotations.references.NgComponentReference;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import com.jwebmp.core.base.html.DivSimple;

/**
 * Component that references AllAnnotationsDataType to verify ALL onParent annotations propagate.
 */
@NgComponent("test-all-annotations-consumer")
@NgComponentReference(AllAnnotationsDataType.class)
public class AllAnnotationsConsumerComponent extends DivSimple<AllAnnotationsConsumerComponent>
        implements INgComponent<AllAnnotationsConsumerComponent>
{
    public AllAnnotationsConsumerComponent()
    {
        add("All annotations consumer");
    }
}


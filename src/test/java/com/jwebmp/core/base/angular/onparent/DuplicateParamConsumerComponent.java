package com.jwebmp.core.base.angular.onparent;

import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.annotations.constructors.NgConstructorParameter;
import com.jwebmp.core.base.angular.client.annotations.references.NgComponentReference;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import com.jwebmp.core.base.html.DivSimple;

/**
 * Consumer component that pulls in the {@code contextIdService} parameter from THREE sources:
 * its own onSelf parameter (declared {@code private}) and two referenced parents
 * ({@link DuplicateParamParentA} / {@link DuplicateParamParentB}, both {@code public}). The
 * generated constructor must declare {@code contextIdService} exactly once, and must keep the
 * {@code public} declaration (most visible wins).
 */
@NgComponent("test-duplicate-param-consumer")
@NgComponentReference(DuplicateParamParentA.class)
@NgComponentReference(DuplicateParamParentB.class)
@NgConstructorParameter("private contextIdService: ContextIdService")
public class DuplicateParamConsumerComponent extends DivSimple<DuplicateParamConsumerComponent>
        implements INgComponent<DuplicateParamConsumerComponent>
{
    public DuplicateParamConsumerComponent()
    {
        add("Duplicate param consumer");
    }
}



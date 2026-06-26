package com.jwebmp.core.base.angular.onparent;

import com.jwebmp.core.base.angular.client.annotations.angular.NgDataType;
import com.jwebmp.core.base.angular.client.annotations.constructors.NgConstructorParameter;
import com.jwebmp.core.base.angular.client.services.interfaces.INgDataType;

/**
 * Second parent fixture contributing the SAME {@code contextIdService} parameter but with a
 * different whitespace layout ({@code foo : Bar} vs {@code foo: Bar}). This ensures de-duplication
 * is by parameter name, not by exact text.
 */
@NgDataType(NgDataType.DataTypeClass.Class)
@NgConstructorParameter(value = "public contextIdService : ContextIdService", onParent = true, onSelf = false)
public class DuplicateParamParentB implements INgDataType<DuplicateParamParentB>
{
}


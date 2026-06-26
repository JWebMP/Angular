package com.jwebmp.core.base.angular.onparent;

import com.jwebmp.core.base.angular.client.annotations.angular.NgDataType;
import com.jwebmp.core.base.angular.client.annotations.constructors.NgConstructorParameter;
import com.jwebmp.core.base.angular.client.services.interfaces.INgDataType;

/**
 * Parent fixture that contributes a {@code contextIdService} constructor parameter to consumers.
 * Used to verify duplicate constructor parameters are de-duplicated by name.
 */
@NgDataType(NgDataType.DataTypeClass.Class)
@NgConstructorParameter(value = "public contextIdService: ContextIdService", onParent = true, onSelf = false)
public class DuplicateParamParentA implements INgDataType<DuplicateParamParentA>
{
}


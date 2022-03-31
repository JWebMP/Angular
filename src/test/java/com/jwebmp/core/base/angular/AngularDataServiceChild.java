package com.jwebmp.core.base.angular;

import com.jwebmp.core.base.ajax.*;
import com.jwebmp.core.base.angular.data.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.annotations.references.*;

@NgDataTypeReference(value = DataComponentData.class)
@NgDataService("angularDataServiceTestChild")
public class AngularDataServiceChild extends AngularDataService<AngularDataServiceChild>
{
	@Override
	public DataComponentData getData(AjaxCall<?> call)
	{
		return new DataComponentData().setName("Name was set and sent!");
	}
}

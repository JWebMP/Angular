package com.jwebmp.core.base.angular;

import com.jwebmp.core.base.ajax.*;
import com.jwebmp.core.base.angular.client.*;
import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.annotations.references.*;

@NgDataTypeReference(value = DataComponentData.class)
@NgDataService("angularDataServiceTestChild")
public class AngularDataServiceChild extends AngularDataService<AngularDataServiceChild>
{
	@Override
	public DynamicData getData(AjaxCall<?> call,AjaxResponse<?> response)
	{
		return new DynamicData().addData(new DataComponentData().setName("Name was set and sent!"));
	}
}

package com.jwebmp.core.base.angular;

import com.jwebmp.core.base.ajax.*;
import com.jwebmp.core.base.angular.client.*;
import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.annotations.references.*;
import com.jwebmp.core.base.angular.client.services.interfaces.*;

@NgDataTypeReference(value = DataComponentData.class)
@NgDataService("updateDataComponent")
public class AngularDataService<J extends AngularDataService<J>> implements INgDataService<J>
{
	@Override
	public DynamicData getData(AjaxCall<?> call, AjaxResponse<?> response)
	{
		return new DynamicData().addData(new DataComponentData().setName("Name was set and sent!"));
	}
}

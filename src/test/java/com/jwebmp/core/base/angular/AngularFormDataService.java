package com.jwebmp.core.base.angular;

import com.jwebmp.core.base.ajax.*;
import com.jwebmp.core.base.angular.data.*;
import com.jwebmp.core.base.angular.modules.services.angular.forms.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.interfaces.*;

@NgDataTypeReference(value = DataComponentData.class)
@NgDataService("angularDataServiceTest")
public class AngularFormDataService extends FormDataService<AngularFormDataService>
		implements INgDataService<AngularFormDataService>
{
	@Override
	public DataComponentData getData(AjaxCall<?> call)
	{
		return new DataComponentData().setName("Name was set and sent!");
	}
}

package com.jwebmp.core.base.angular;

import com.google.inject.*;
import com.jwebmp.core.base.angular.client.annotations.angular.*;

import com.jwebmp.core.base.angular.client.annotations.references.*;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.services.interfaces.*;
import com.jwebmp.core.base.html.*;

@NgComponent("data-component")
@NgComponentReference(value = AngularFormDataProvider.class)
public class AngularDataComponent extends DivSimple<AngularDataComponent>
		implements INgComponent<AngularDataComponent>
{
	@Inject
	void initialize()
	{
		add("Name ? : {{data.appData.name}}");
	}
}

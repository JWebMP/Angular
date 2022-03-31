package com.jwebmp.core.base.angular;

import com.google.inject.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.annotations.references.*;
import com.jwebmp.core.base.angular.services.interfaces.*;
import com.jwebmp.core.base.html.*;

@NgComponent("data-component")
@NgComponentReference(value = AngularDataService.class, provides = true)
public class AngularDataComponent extends DivSimple<AngularDataComponent>
		implements INgComponent<AngularDataComponent>
{
	@Inject
	void initialize()
	{
		add("Name ? : {{angularDataService.data.name}}");
	}
}

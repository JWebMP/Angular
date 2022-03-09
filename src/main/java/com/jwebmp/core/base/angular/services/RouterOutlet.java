package com.jwebmp.core.base.angular.services;

import com.jwebmp.core.base.angular.modules.services.angular.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.html.*;

@NgModuleReference(NgRouterModule.class)
public class RouterOutlet extends DivSimple<RouterOutlet>
{
	public RouterOutlet()
	{
		setRenderIDAttribute(false);
		setTag("router-outlet");
	}
}

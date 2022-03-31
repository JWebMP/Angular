package com.jwebmp.core.base.angular.services;

import com.jwebmp.core.base.angular.services.annotations.references.*;
import com.jwebmp.core.base.html.*;

@NgImportReference(name = "RouterModule, ParamMap,Router", reference = "@angular/router")
public class RouterOutlet extends DivSimple<RouterOutlet>
{
	public RouterOutlet()
	{
		setRenderIDAttribute(false);
		setTag("router-outlet");
	}
}

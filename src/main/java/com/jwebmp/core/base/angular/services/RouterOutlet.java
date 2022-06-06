package com.jwebmp.core.base.angular.services;

import com.jwebmp.core.base.html.*;

public class RouterOutlet extends DivSimple<RouterOutlet>
{
	public RouterOutlet()
	{
		setRenderIDAttribute(false);
		setTag("router-outlet");
	}
}

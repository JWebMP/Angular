package com.jwebmp.core.base.angular.modules.services.angular;

import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

@TsDependency(value = "@angular/platform-browser-dynamic", version = "^13.2.0")
public class DynamicPlatformBrowserModule implements INgModule<DynamicPlatformBrowserModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("platformBrowserDynamic", "@angular/platform-browser-dynamic");
	}
	
}

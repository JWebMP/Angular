package com.jwebmp.core.base.angular.modules;

import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

@TsDependency(value = "@angular/platform-browser", version = "13.2.0")
public class PlatformBrowserModule implements INgModule<PlatformBrowserModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("BrowserModule", "@angular/platform-browser");
	}
	
}

package com.jwebmp.core.base.angular.modules.services.angular;

import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

@TsDependency(value = "@angular/animations", version = "13.2.0")
public class BrowserAnimationsModule implements INgModule<BrowserAnimationsModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("BrowserAnimationsModule", "@angular/platform-browser/animations");
	}
	
}

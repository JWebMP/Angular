package com.jwebmp.core.base.angular.modules.services.angular;

import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

@TsDependency(value = "@angular/router", version = "13.2.0")
public class NgRoutesModule implements INgModule<NgRoutesModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("Routes", "@angular/router");
	}
	
}

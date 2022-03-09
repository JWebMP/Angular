package com.jwebmp.core.base.angular.modules.services.angular;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class NgInputModule implements INgModule<NgInputModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("Input", "@angular/core");
	}
	
}

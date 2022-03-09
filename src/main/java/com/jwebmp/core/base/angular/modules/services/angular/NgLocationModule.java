package com.jwebmp.core.base.angular.modules.services.angular;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class NgLocationModule implements INgModule<NgLocationModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("Location", "@angular/common");
	}
	
}

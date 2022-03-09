package com.jwebmp.core.base.angular.modules.services.angular;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class NgComponentModule implements INgModule<NgComponentModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("Component", "@angular/core");
	}
	
}

package com.jwebmp.core.base.angular.modules.services.observable;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class ObserverModule implements INgModule<ObserverModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("Observer ", "!rxjs");
	}
	
}

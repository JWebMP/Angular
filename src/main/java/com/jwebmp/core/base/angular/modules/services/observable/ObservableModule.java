package com.jwebmp.core.base.angular.modules.services.observable;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class ObservableModule implements INgModule<ObservableModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("Observable", "!rxjs");
	}
	
}

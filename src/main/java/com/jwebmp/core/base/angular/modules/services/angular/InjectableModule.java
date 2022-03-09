package com.jwebmp.core.base.angular.modules.services.angular;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class InjectableModule implements INgModule<InjectableModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("Injectable", "@angular/core");
	}
	
}

package com.jwebmp.core.base.angular.modules.services.angular;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class HostListenerModule implements INgModule<HostListenerModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("HostListener ", "@angular/core");
	}
	
}

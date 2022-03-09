package com.jwebmp.core.base.angular.modules.services.angular;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class OnDestroyModule implements INgModule<OnDestroyModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("OnDestroy", "@angular/core");
	}
	
	public Set<String> interfaces()
	{
		return Set.of("OnDestroy");
	}
}

package com.jwebmp.core.base.angular.modules.services.rxtxjs;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class RxFromEventModule implements INgModule<RxFromEventModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("fromEvent", "!rxjs");
	}
	
}

package com.jwebmp.core.base.angular.modules.services.rxtxjs;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class RxAllModule implements INgModule<RxAllModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("* as Rx", "!rxjs/Rx");
	}
	
}

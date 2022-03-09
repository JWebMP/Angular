package com.jwebmp.core.base.angular.modules.services.rxtxjs;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class RxSwitchMapModule implements INgModule<RxSwitchMapModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("switchMap", "!rxjs/operators");
	}
	
}

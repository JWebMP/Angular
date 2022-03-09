package com.jwebmp.core.base.angular.modules.services.rxtxjs;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class RxJsOperatorsModule implements INgModule<RxJsOperatorsModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("filter, first, switchMap  ", "!rxjs/operators");
	}
	
}

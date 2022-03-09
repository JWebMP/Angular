package com.jwebmp.core.base.angular.modules.services.rxtxjs;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class RxSubjectModule implements INgModule<RxSubjectModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("Subject  ", "!rxjs");
	}
	
}

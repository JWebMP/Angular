package com.jwebmp.core.base.angular.modules.services.rxtxjs;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class RxJsBehaviourSubjectModule implements INgModule<RxJsBehaviourSubjectModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("BehaviorSubject  ", "!rxjs/internal/BehaviorSubject");
	}
	
}

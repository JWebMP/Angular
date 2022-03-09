package com.jwebmp.core.base.angular.modules.services.websocket;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class AnonymousSubjectModule implements INgModule<AnonymousSubjectModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("AnonymousSubject ", "!rxjs/internal/Subject");
	}
	
}

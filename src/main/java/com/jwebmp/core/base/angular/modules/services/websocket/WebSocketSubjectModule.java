package com.jwebmp.core.base.angular.modules.services.websocket;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class WebSocketSubjectModule implements INgModule<WebSocketSubjectModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("WebSocketSubject", "!rxjs/webSocket");
	}
	
}

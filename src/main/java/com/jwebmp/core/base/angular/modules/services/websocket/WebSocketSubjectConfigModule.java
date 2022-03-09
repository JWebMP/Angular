package com.jwebmp.core.base.angular.modules.services.websocket;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class WebSocketSubjectConfigModule implements INgModule<WebSocketSubjectConfigModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("WebSocketSubjectConfig", "!rxjs/webSocket");
	}
	
}

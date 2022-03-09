package com.jwebmp.core.base.angular.modules.services.websocket;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class WebSocketModule implements INgModule<WebSocketModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("webSocket", "!rxjs/webSocket");
	}
	
}

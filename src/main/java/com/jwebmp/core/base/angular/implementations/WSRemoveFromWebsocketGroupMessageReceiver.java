package com.jwebmp.core.base.angular.implementations;

import com.guicedee.guicedservlets.websockets.*;
import com.guicedee.guicedservlets.websockets.options.*;
import com.guicedee.guicedservlets.websockets.services.*;
import com.guicedee.logger.*;

import java.util.*;
import java.util.logging.*;

public class WSRemoveFromWebsocketGroupMessageReceiver
		implements IWebSocketMessageReceiver
{
	private static final Logger log = LogFactory.getLog("WSRemoveFromWebsocketGroupMessageReceiver");
	
	@Override
	public void receiveMessage(WebSocketMessageReceiver<?> message) throws SecurityException
	{
		try
		{
			String group = message.getData()
			                      .get("websocketGroup").toString();
			GuicedWebSocket.removeFromGroup(group, message.getSession());
		}
		catch (Exception e)
		{
			log.log(Level.WARNING, "Unable to check for local storage key", e);
		}
	}
	
	@Override
	public Set<String> messageNames()
	{
		Set<String> messageNames = new HashSet<>();
		messageNames.add("RemoveFromWebSocketGroup");
		return messageNames;
	}
	
}

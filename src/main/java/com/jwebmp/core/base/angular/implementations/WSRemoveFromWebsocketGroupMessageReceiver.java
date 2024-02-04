package com.jwebmp.core.base.angular.implementations;

import com.guicedee.guicedservlets.websockets.GuicedWebSocket;
import com.guicedee.guicedservlets.websockets.options.WebSocketMessageReceiver;
import com.guicedee.guicedservlets.websockets.services.IWebSocketMessageReceiver;
import lombok.extern.java.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

@Log
public class WSRemoveFromWebsocketGroupMessageReceiver
		implements IWebSocketMessageReceiver
{
	@Override
	public void receiveMessage(WebSocketMessageReceiver<?> message) throws SecurityException
	{
		try
		{
			String group = message.getData()
			                      .get("groupName").toString();
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

package com.jwebmp.core.base.angular.implementations;

import com.guicedee.guicedservlets.websockets.options.WebSocketMessageReceiver;
import com.guicedee.guicedservlets.websockets.services.IWebSocketMessageReceiver;
import lombok.extern.java.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import static com.guicedee.client.IGuiceContext.get;

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
			com.guicedee.vertx.websockets.GuicedWebSocket socket = get(com.guicedee.vertx.websockets.GuicedWebSocket.class);
			socket.removeFromGroup(group);
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

package com.jwebmp.core.base.angular.implementations;

import com.guicedee.guicedservlets.websockets.options.IGuicedWebSocket;
import com.guicedee.guicedservlets.websockets.options.WebSocketMessageReceiver;
import com.guicedee.guicedservlets.websockets.services.IWebSocketMessageReceiver;
import io.smallrye.mutiny.Uni;
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
    public Uni<Void> receiveMessage(WebSocketMessageReceiver<?> message) throws SecurityException
    {
        return Uni.createFrom()
                  .item(message)
                  .onItem()
                  .invoke(mr -> {
                      try
                      {
                          Object val = mr.getData().get("groupName");
                          String group = val != null ? val.toString() : null;
                          if (group != null && !group.isEmpty())
                          {
                              get(IGuicedWebSocket.class).removeFromGroup(group);
                          }
                      }
                      catch (Exception e)
                      {
                          log.log(Level.WARNING, "Unable to remove from web socket group", e);
                      }
                  })
                  .replaceWithVoid();
    }

    @Override
    public Set<String> messageNames()
    {
        Set<String> messageNames = new HashSet<>();
        messageNames.add("RemoveFromWebSocketGroup");
        return messageNames;
    }

}

package com.jwebmp.core.base.angular.implementations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guicedee.guicedservlets.websockets.options.WebSocketMessageReceiver;
import com.guicedee.guicedservlets.websockets.services.IWebSocketMessageReceiver;
import com.guicedee.vertx.websockets.GuicedWebSocket;
import com.jwebmp.core.base.ajax.*;
import com.jwebmp.core.utilities.TextUtilities;
import com.jwebmp.interception.services.AjaxCallIntercepter;
import lombok.extern.java.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import static com.guicedee.client.IGuiceContext.get;
import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.DefaultObjectMapper;
import static com.jwebmp.interception.services.JWebMPInterceptionBinder.AjaxCallInterceptorKey;

@Log
public abstract class WebSocketAbstractCallReceiver
        implements IWebSocketMessageReceiver
{
    public abstract String getMessageDirector();

    public abstract AjaxResponse<?> action(AjaxCall<?> call, AjaxResponse<?> response);

    @Override
    public Set<String> messageNames()
    {
        Set<String> messageNames = new HashSet<>();
        messageNames.add(getMessageDirector());
        return messageNames;
    }

    @Override
    public void receiveMessage(WebSocketMessageReceiver message) throws SecurityException
    {
        String output = "";
        AjaxResponse<?> ajaxResponse = get(AjaxResponse.class);
        try
        {
            AjaxCall<?> ajaxCall = get(AjaxCall.class);
            ObjectMapper om = get(DefaultObjectMapper);
            String originalValues = om.writeValueAsString(message.getData());
            AjaxCall<?> call = om.readValue(originalValues, AjaxCall.class);
            ajaxCall.fromCall(call);

            //      ajaxCall.setWebSocketCall(true);
            //   ajaxCall.setWebsocketSession(message.getSession());

            for (AjaxCallIntercepter<?> ajaxCallIntercepter : get(AjaxCallInterceptorKey))
            {
                ajaxCallIntercepter.intercept(ajaxCall, ajaxResponse);
            }
            ajaxResponse = action(ajaxCall, ajaxResponse);
            if (ajaxResponse != null)
            {
                GuicedWebSocket socket = get(GuicedWebSocket.class);
                socket.broadcastMessage(message.getBroadcastGroup(), ajaxResponse.toJson());
            }
        }
        catch (Exception T)
        {
            ajaxResponse.setSuccess(false);
            AjaxResponseReaction<?> arr = new AjaxResponseReaction<>("Unknown Error",
                                                                     "An AJAX call resulted in an unknown server error<br>" + T.getMessage() + "<br>" + TextUtilities.stackTraceToString(
                                                                             T), ReactionType.DialogDisplay);
            arr.setResponseType(AjaxResponseType.Danger);
            ajaxResponse.addReaction(arr);
            output = ajaxResponse.toString();
            WebSocketAbstractCallReceiver.log.log(Level.SEVERE, "Unknown in ajax reply\n", T);
        }
        catch (Throwable T)
        {
            ajaxResponse.setSuccess(false);
            AjaxResponseReaction<?> arr = new AjaxResponseReaction<>("Unknown Error",
                                                                     "An AJAX call resulted in an internal server error<br>" + T.getMessage() + "<br>" + TextUtilities.stackTraceToString(
                                                                             T), ReactionType.DialogDisplay);
            arr.setResponseType(AjaxResponseType.Danger);
            ajaxResponse.addReaction(arr);
            output = ajaxResponse.toString();
            WebSocketAbstractCallReceiver.log.log(Level.SEVERE, "Unknown in ajax reply\n", T);
        }
    }
}

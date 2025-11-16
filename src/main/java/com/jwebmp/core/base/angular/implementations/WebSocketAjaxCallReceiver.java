package com.jwebmp.core.base.angular.implementations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guicedee.guicedservlets.websockets.options.IGuicedWebSocket;
import com.guicedee.guicedservlets.websockets.options.WebSocketMessageReceiver;
import com.guicedee.guicedservlets.websockets.services.IWebSocketMessageReceiver;
import com.jwebmp.core.base.ajax.*;
import com.jwebmp.core.exceptions.InvalidRequestException;
import com.jwebmp.core.htmlbuilder.javascript.events.interfaces.IEvent;
import com.jwebmp.core.utilities.EscapeChars;
import com.jwebmp.interception.services.AjaxCallIntercepter;
import lombok.extern.java.Log;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import static com.guicedee.client.IGuiceContext.get;
import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.DefaultObjectMapper;
import static com.guicedee.services.jsonrepresentation.json.StaticStrings.CHAR_DOT;
import static com.guicedee.services.jsonrepresentation.json.StaticStrings.CHAR_UNDERSCORE;
import static com.jwebmp.interception.services.JWebMPInterceptionBinder.AjaxCallInterceptorKey;

@Log
public class WebSocketAjaxCallReceiver
        implements IWebSocketMessageReceiver<AjaxResponse<?>, WebSocketAjaxCallReceiver>
{
    @Override
    public Set<String> messageNames()
    {
        Set<String> messageNames = new HashSet<>();
        messageNames.add("ajax");
        return messageNames;
    }

    @Override
    public io.smallrye.mutiny.Uni<AjaxResponse<?>> receiveMessage(WebSocketMessageReceiver message) throws SecurityException
    {
        return io.smallrye.mutiny.Uni.createFrom()
                                     .item(message)
                                     .onItem()
                                     .transformToUni(m -> {
                                         AjaxResponse<?> ajaxResponse = get(AjaxResponse.class);
                                         try
                                         {
                                             AjaxCall<?> ajaxCall = get(AjaxCall.class);
                                             ObjectMapper om = get(DefaultObjectMapper);
                                             String originalValues = om.writeValueAsString(m.getData());
                                             AjaxCall<?> call = om.readValue(originalValues, AjaxCall.class);
                                             om.readerForUpdating(ajaxCall)
                                               .readValue(originalValues, AjaxCall.class);

                                             IEvent<?, ?> triggerEvent = processEvent(ajaxCall);
                                             for (AjaxCallIntercepter<?> ajaxCallIntercepter : get(AjaxCallInterceptorKey))
                                             {
                                                 ajaxCallIntercepter.intercept(ajaxCall, ajaxResponse);
                                             }

                                             return triggerEvent.fireEvent(ajaxCall, ajaxResponse)
                                                                .onItem()
                                                                .invoke(_ -> {
                                                                    try
                                                                    {
                                                                        get(IGuicedWebSocket.class).broadcastMessage(m.getBroadcastGroup(), ajaxResponse.toString());
                                                                    }
                                                                    catch (Exception e)
                                                                    {
                                                                        throw new RuntimeException(e);
                                                                    }
                                                                })
                                                                .onFailure()
                                                                .invoke(failure -> {
                                                                    try
                                                                    {
                                                                        AjaxResponse<?> err = new AjaxResponse<>();
                                                                        err.setSuccess(false);
                                                                        AjaxResponseReaction<?> arr = new AjaxResponseReaction<>(
                                                                                "Unknown Error",
                                                                                "An AJAX call resulted in an unknown server error<br>" + failure.getMessage() + "<br>" +
                                                                                        EscapeChars.forHTML(ExceptionUtils.getStackTrace(failure)),
                                                                                ReactionType.DialogDisplay);
                                                                        arr.setResponseType(AjaxResponseType.Danger);
                                                                        err.addReaction(arr);
                                                                        get(IGuicedWebSocket.class).broadcastMessage(m.getBroadcastGroup(), err.toString());
                                                                        WebSocketAjaxCallReceiver.log.log(Level.SEVERE, "Unknown in ajax reply\n", failure);
                                                                    }
                                                                    catch (Exception e)
                                                                    {
                                                                        throw new RuntimeException(e);
                                                                    }
                                                                })
                                                                .replaceWith(ajaxResponse);
                                         }
                                         catch (InvalidRequestException ie)
                                         {
                                             ajaxResponse.setSuccess(false);
                                             AjaxResponseReaction<?> arr = new AjaxResponseReaction<>("Invalid Request Value", "A value in the request was found to be incorrect.<br>" + ie.getMessage(),
                                                     ReactionType.DialogDisplay);
                                             arr.setResponseType(AjaxResponseType.Danger);
                                             ajaxResponse.addReaction(arr);
                                             try
                                             {
                                                 get(IGuicedWebSocket.class).broadcastMessage(m.getBroadcastGroup(), ajaxResponse.toString());
                                             }
                                             catch (Exception e)
                                             {
                                                 throw new RuntimeException(e);
                                             }
                                             WebSocketAjaxCallReceiver.log.log(Level.SEVERE, "[SessionID]-[" + m.getData()
                                                                                                                .get("sessionid")
                                                     + "];" + "[Exception]-[Invalid Request]", ie);
                                             return io.smallrye.mutiny.Uni.createFrom()
                                                                          .item(ajaxResponse);
                                         }
                                         catch (Exception T)
                                         {
                                             ajaxResponse.setSuccess(false);
                                             AjaxResponseReaction<?> arr = new AjaxResponseReaction<>("Unknown Error",
                                                     "An AJAX call resulted in an unknown server error<br>" + T.getMessage() + "<br>" +
                                                             EscapeChars.forHTML(ExceptionUtils.getStackTrace(T)), ReactionType.DialogDisplay);
                                             arr.setResponseType(AjaxResponseType.Danger);
                                             ajaxResponse.addReaction(arr);
                                             try
                                             {
                                                 get(IGuicedWebSocket.class).broadcastMessage(m.getBroadcastGroup(), ajaxResponse.toString());
                                             }
                                             catch (Exception e)
                                             {
                                                 throw new RuntimeException(e);
                                             }
                                             WebSocketAjaxCallReceiver.log.log(Level.SEVERE, "Unknown in ajax reply\n", T);
                                             return io.smallrye.mutiny.Uni.createFrom()
                                                                          .item(ajaxResponse);
                                         }
                                         catch (Throwable T)
                                         {
                                             ajaxResponse.setSuccess(false);
                                             AjaxResponseReaction<?> arr = new AjaxResponseReaction<>("Unknown Error",
                                                     "An AJAX call resulted in an internal server error<br>" + T.getMessage() + "<br>" +
                                                             EscapeChars.forHTML(ExceptionUtils.getStackTrace(T)), ReactionType.DialogDisplay);
                                             arr.setResponseType(AjaxResponseType.Danger);
                                             ajaxResponse.addReaction(arr);
                                             try
                                             {
                                                 get(IGuicedWebSocket.class).broadcastMessage(m.getBroadcastGroup(), ajaxResponse.toString());
                                             }
                                             catch (Exception e)
                                             {
                                                 throw new RuntimeException(e);
                                             }
                                             WebSocketAjaxCallReceiver.log.log(Level.SEVERE, "Unknown in ajax reply\n", T);
                                             return io.smallrye.mutiny.Uni.createFrom()
                                                                          .item(ajaxResponse);
                                         }
                                     });
    }

    protected IEvent<?, ?> processEvent(AjaxCall<?> call) throws InvalidRequestException
    {
        IEvent<?, ?> triggerEvent = null;
        try
        {
            Class<?> eventClass = Class.forName(call.getClassName()
                                                    .replace(CHAR_UNDERSCORE, CHAR_DOT));
            triggerEvent = (IEvent<?, ?>) get(eventClass);
        }
        catch (ClassNotFoundException cnfe)
        {
            WebSocketAjaxCallReceiver.log.log(Level.FINEST, "Unable to find the event class specified", cnfe);
            throw new InvalidRequestException("The Event To Be Triggered Could Not Be Found");
        }
        return triggerEvent;
    }
}

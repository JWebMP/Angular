package com.jwebmp.core.base.angular.implementations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.guicedee.guicedservlets.services.scopes.CallScoper;
import com.guicedee.guicedservlets.websockets.GuicedWebSocket;
import com.guicedee.guicedservlets.websockets.options.WebSocketMessageReceiver;
import com.guicedee.guicedservlets.websockets.services.IWebSocketMessageReceiver;
import com.jwebmp.core.base.ajax.*;
import com.jwebmp.core.utilities.TextUtilities;
import com.jwebmp.interception.services.AjaxCallIntercepter;
import lombok.extern.java.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import static com.guicedee.client.IGuiceContext.get;
import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.DefaultObjectMapper;
import static com.jwebmp.interception.JWebMPInterceptionBinder.AjaxCallInterceptorKey;

@Log
public abstract class WebSocketAbstractCallReceiver
        implements IWebSocketMessageReceiver
{
    @Inject
    @Named("callScope")
    private CallScoper scope;

    public CallScoper getScope()
    {
        return scope;
    }

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
        try
        {
            scope.enter();
        }
        catch (Throwable T)
        {
            log.log(Level.WARNING, "Check scope entries and exits, enter called twice", T);
        }
        AjaxResponse<?> ajaxResponse = get(AjaxResponse.class);
        try
        {
            AjaxCall<?> ajaxCall = get(AjaxCall.class);
            ObjectMapper om = get(DefaultObjectMapper);
            String originalValues = om.writeValueAsString(message.getData());
            AjaxCall<?> call = om.readValue(originalValues, AjaxCall.class);
            ajaxCall.fromCall(call);

            ajaxCall.setWebSocketCall(true);
            ajaxCall.setWebsocketSession(message.getSession());

            for (AjaxCallIntercepter<?> ajaxCallIntercepter : get(AjaxCallInterceptorKey))
            {
                ajaxCallIntercepter.intercept(ajaxCall, ajaxResponse);
            }
            ajaxResponse = action(ajaxCall, ajaxResponse);
            if (ajaxResponse != null)
            {
                GuicedWebSocket.broadcastMessage(message.getBroadcastGroup(), ajaxResponse.toString());
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
        finally
        {
            if (!Strings.isNullOrEmpty(output))
            {
                log.severe(output);
            }
            scope.exit();
        }
    }
}

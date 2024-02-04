package com.jwebmp.core.base.angular.implementations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.guicedservlets.services.scopes.CallScoper;
import com.guicedee.guicedservlets.websockets.GuicedWebSocket;
import com.guicedee.guicedservlets.websockets.options.WebSocketMessageReceiver;
import com.guicedee.guicedservlets.websockets.services.IWebSocketMessageReceiver;
import com.jwebmp.core.Event;
import com.jwebmp.core.base.ajax.*;
import com.jwebmp.core.exceptions.InvalidRequestException;
import com.jwebmp.core.utilities.TextUtilities;
import com.jwebmp.interception.services.AjaxCallIntercepter;
import lombok.extern.java.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import static com.guicedee.guicedinjection.GuiceContext.get;
import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.DefaultObjectMapper;
import static com.guicedee.services.jsonrepresentation.json.StaticStrings.CHAR_DOT;
import static com.guicedee.services.jsonrepresentation.json.StaticStrings.CHAR_UNDERSCORE;
import static com.jwebmp.interception.JWebMPInterceptionBinder.AjaxCallInterceptorKey;

@Log
public class WebSocketAjaxCallReceiver
		implements IWebSocketMessageReceiver
{
	@Inject
	@Named("callScope")
	CallScoper scope;
	
	@Override
	public Set<String> messageNames()
	{
		Set<String> messageNames = new HashSet<>();
		messageNames.add("ajax");
		return messageNames;
	}
	
	@Override
	public void receiveMessage(WebSocketMessageReceiver message) throws SecurityException
	{
		String output;
		try
		{
			scope.enter();
		}
		catch (Throwable T)
		{
			log.log(Level.WARNING, "Check scope entries and exits, enter called twice", T);
		}
		AjaxResponse<?> ajaxResponse = GuiceContext.get(AjaxResponse.class);
		try
		{
			AjaxCall<?> ajaxCall = GuiceContext.get(AjaxCall.class);
			ObjectMapper om = GuiceContext.get(DefaultObjectMapper);
			String originalValues = om.writeValueAsString(message.getData());
			AjaxCall<?> call = om.readValue(originalValues, AjaxCall.class);
			om.readerForUpdating(ajaxCall)
			  .readValue(originalValues, AjaxCall.class);
			
			ajaxCall.setWebSocketCall(true);
			ajaxCall.setWebsocketSession(message.getSession());
			Event<?, ?> triggerEvent = processEvent(ajaxCall);
			for (AjaxCallIntercepter<?> ajaxCallIntercepter : get(AjaxCallInterceptorKey))
			{
				ajaxCallIntercepter.intercept(ajaxCall, ajaxResponse);
			}
			triggerEvent.fireEvent(ajaxCall, ajaxResponse);
			
			output = ajaxResponse.toString();
		}
		catch (InvalidRequestException ie)
		{
			ajaxResponse.setSuccess(false);
			AjaxResponseReaction<?> arr = new AjaxResponseReaction<>("Invalid Request Value", "A value in the request was found to be incorrect.<br>" + ie.getMessage(),
					ReactionType.DialogDisplay);
			arr.setResponseType(AjaxResponseType.Danger);
			ajaxResponse.addReaction(arr);
			output = ajaxResponse.toString();
			WebSocketAjaxCallReceiver.log.log(Level.SEVERE, "[SessionID]-[" + message.getData()
			                                                                         .get("sessionid")
			                                                + "];" + "[Exception]-[Invalid Request]", ie);
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
			WebSocketAjaxCallReceiver.log.log(Level.SEVERE, "Unknown in ajax reply\n", T);
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
			WebSocketAjaxCallReceiver.log.log(Level.SEVERE, "Unknown in ajax reply\n", T);
		}
		finally
		{
			scope.exit();
		}
		GuicedWebSocket.broadcastMessage(message.getBroadcastGroup(), output);
	}
	
	protected Event<?, ?> processEvent(AjaxCall<?> call) throws InvalidRequestException
	{
		Event<?, ?> triggerEvent = null;
		try
		{
			Class<?> eventClass = Class.forName(call.getClassName()
			                                        .replace(CHAR_UNDERSCORE, CHAR_DOT));
			triggerEvent = (Event<?, ?>) get(eventClass);
		}
		catch (ClassNotFoundException cnfe)
		{
			WebSocketAjaxCallReceiver.log.log(Level.FINEST, "Unable to find the event class specified", cnfe);
			throw new InvalidRequestException("The Event To Be Triggered Could Not Be Found");
		}
		return triggerEvent;
	}
}

package com.jwebmp.core.base.angular.implementations;

import com.fasterxml.jackson.databind.*;
import com.google.inject.*;
import com.google.inject.name.*;
import com.guicedee.guicedinjection.*;
import com.guicedee.guicedservlets.services.scopes.*;
import com.guicedee.guicedservlets.websockets.*;
import com.guicedee.guicedservlets.websockets.options.*;
import com.guicedee.guicedservlets.websockets.services.*;
import com.guicedee.logger.*;
import com.jwebmp.core.*;
import com.jwebmp.core.base.ajax.*;
import com.jwebmp.core.exceptions.*;
import com.jwebmp.core.utilities.*;
import com.jwebmp.interception.services.*;

import java.util.*;
import java.util.logging.*;

import static com.guicedee.guicedinjection.GuiceContext.*;
import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.*;
import static com.guicedee.guicedinjection.json.StaticStrings.*;
import static com.jwebmp.interception.JWebMPInterceptionBinder.*;

public class WebSocketAjaxCallReceiver
		implements IWebSocketMessageReceiver
{
	private static final Logger log = LogFactory.getInstance()
	                                            .getLogger("AJAXWebSocket");
	
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
			ajaxCall.fromCall(call);
			
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

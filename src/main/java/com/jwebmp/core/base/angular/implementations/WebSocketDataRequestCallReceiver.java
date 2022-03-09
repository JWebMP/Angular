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
import com.jwebmp.core.base.angular.services.interfaces.*;
import com.jwebmp.core.exceptions.*;
import com.jwebmp.core.utilities.*;
import com.jwebmp.interception.services.*;

import java.util.*;
import java.util.logging.*;

import static com.guicedee.guicedinjection.GuiceContext.*;
import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.*;
import static com.guicedee.guicedinjection.json.StaticStrings.*;
import static com.jwebmp.interception.JWebMPInterceptionBinder.*;

public class WebSocketDataRequestCallReceiver
		implements IWebSocketMessageReceiver
{
	private static final Logger log = LogFactory.getInstance()
	                                            .getLogger("WebSocketDataFetch");
	
	@Inject
	@Named("callScope")
	CallScoper scope;
	
	@Override
	public Set<String> messageNames()
	{
		Set<String> messageNames = new HashSet<>();
		messageNames.add("data");
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
			
			Class<? extends INgDataService<?>> clazzy = (Class<? extends INgDataService<?>>) Class.forName(ajaxCall.getClassName());
			INgDataService<?> dataService = GuiceContext.get(clazzy);
			for (AjaxCallIntercepter<?> ajaxCallIntercepter : get(AjaxCallInterceptorKey))
			{
				ajaxCallIntercepter.intercept(ajaxCall, ajaxResponse);
			}
			
			var returned = dataService.getData(ajaxCall);
			GuicedWebSocket.broadcastMessage(message.getBroadcastGroup(), returned);
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
			WebSocketDataRequestCallReceiver.log.log(Level.SEVERE, "Unknown in ajax reply\n", T);
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
			WebSocketDataRequestCallReceiver.log.log(Level.SEVERE, "Unknown in ajax reply\n", T);
		}
		finally
		{
			scope.exit();
		}
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
			WebSocketDataRequestCallReceiver.log.log(Level.FINEST, "Unable to find the event class specified", cnfe);
			throw new InvalidRequestException("The Event To Be Triggered Could Not Be Found");
		}
		return triggerEvent;
	}
}

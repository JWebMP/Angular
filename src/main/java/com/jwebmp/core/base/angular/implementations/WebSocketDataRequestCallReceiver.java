package com.jwebmp.core.base.angular.implementations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.guicedservlets.services.scopes.CallScoper;
import com.guicedee.guicedservlets.websockets.GuicedWebSocket;
import com.guicedee.guicedservlets.websockets.options.WebSocketMessageReceiver;
import com.guicedee.guicedservlets.websockets.services.IWebSocketMessageReceiver;
import com.guicedee.logger.LogFactory;
import com.jwebmp.core.base.ajax.*;
import com.jwebmp.core.base.angular.services.interfaces.INgDataService;
import com.jwebmp.core.utilities.TextUtilities;
import com.jwebmp.interception.services.AjaxCallIntercepter;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.guicedee.guicedinjection.GuiceContext.get;
import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.DefaultObjectMapper;
import static com.jwebmp.interception.JWebMPInterceptionBinder.AjaxCallInterceptorKey;

public class WebSocketDataRequestCallReceiver
		implements IWebSocketMessageReceiver
{
	private static final Logger log = LogFactory.getInstance()
	                                            .getLogger("WebSocketDataFetch");

	@Inject
	@Named("callScope")
	private CallScoper scope;

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
			AjaxResponse<?> response = GuiceContext.get(AjaxResponse.class);
			response.addDataResponse(dataService.signalFetchName(),returned);
			GuicedWebSocket.broadcastMessage(message.getBroadcastGroup(), response.toString());
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
}

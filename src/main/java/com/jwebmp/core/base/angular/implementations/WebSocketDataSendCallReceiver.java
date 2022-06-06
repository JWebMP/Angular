package com.jwebmp.core.base.angular.implementations;

import com.guicedee.guicedinjection.*;
import com.guicedee.guicedservlets.websockets.services.*;
import com.jwebmp.core.base.ajax.*;
import com.jwebmp.core.base.angular.client.services.interfaces.*;

public class WebSocketDataSendCallReceiver
	extends WebSocketAbstractCallReceiver
		implements IWebSocketMessageReceiver
{
	@Override
	public String getMessageDirector()
	{
		return "dataSend";
	}
	
	@Override
	public AjaxResponse<?> action(AjaxCall<?> call, AjaxResponse<?> response)
	{
		Class<? extends INgDataService<?>> clazzy = null;
		try
		{
			clazzy = (Class<? extends INgDataService<?>>) Class.forName(call.getClassName());
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		INgDataService<?> dataService = GuiceContext.get(clazzy);
		dataService.receiveData(call,response);
		return response;
	}
}

package com.jwebmp.core.base.angular.implementations;

import com.guicedee.guicedinjection.*;
import com.guicedee.guicedservlets.websockets.services.*;
import com.jwebmp.core.base.ajax.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.interfaces.*;

import static com.jwebmp.core.base.angular.services.compiler.AnnotationsMap.*;

public class WebSocketDataRequestCallReceiver
	extends WebSocketAbstractCallReceiver
		implements IWebSocketMessageReceiver
{
	@Override
	public String getMessageDirector()
	{
		return "data";
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
		var returned = dataService.getData(call);
		NgDataService dService = getAnnotations(clazzy, NgDataService.class).get(0);
		response.addDataResponse(dService.value(), returned);
		return response;
	}
}

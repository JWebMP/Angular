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
import com.jwebmp.core.base.ajax.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.angular.services.interfaces.*;
import com.jwebmp.core.utilities.*;
import com.jwebmp.interception.services.*;

import java.util.*;
import java.util.logging.*;

import static com.guicedee.guicedinjection.GuiceContext.*;
import static com.guicedee.guicedinjection.interfaces.ObjectBinderKeys.*;
import static com.jwebmp.core.base.angular.services.compiler.AnnotationsMap.*;
import static com.jwebmp.core.base.angular.services.interfaces.ITSComponent.*;
import static com.jwebmp.interception.JWebMPInterceptionBinder.*;

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

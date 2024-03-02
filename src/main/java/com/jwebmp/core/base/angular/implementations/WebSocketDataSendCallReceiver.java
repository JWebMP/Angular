package com.jwebmp.core.base.angular.implementations;

import com.guicedee.client.IGuiceContext;
import com.guicedee.guicedservlets.websockets.services.IWebSocketMessageReceiver;
import com.jwebmp.core.base.ajax.AjaxCall;
import com.jwebmp.core.base.ajax.AjaxResponse;
import com.jwebmp.core.base.angular.client.services.interfaces.INgDataService;

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
        INgDataService<?> dataService = IGuiceContext.get(clazzy);
        dataService.receiveData(call, response);
        return response;
    }
}

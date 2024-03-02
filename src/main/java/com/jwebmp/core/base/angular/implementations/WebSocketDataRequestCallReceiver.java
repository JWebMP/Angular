package com.jwebmp.core.base.angular.implementations;

import com.guicedee.client.IGuiceContext;
import com.guicedee.guicedservlets.websockets.services.IWebSocketMessageReceiver;
import com.jwebmp.core.base.ajax.AjaxCall;
import com.jwebmp.core.base.ajax.AjaxResponse;
import com.jwebmp.core.base.angular.client.annotations.angular.NgDataService;
import com.jwebmp.core.base.angular.client.services.interfaces.INgDataService;

import static com.jwebmp.core.base.angular.client.services.AnnotationsMap.getAnnotations;

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
        INgDataService<?> dataService = IGuiceContext.get(clazzy);
        var returned = dataService.getData(call, response);
        if (returned != null)
        {
            NgDataService dService = getAnnotations(clazzy, NgDataService.class).get(0);
            response.addDataResponse(dService.value(), returned);
        }
        return response;
    }
}

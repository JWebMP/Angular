package com.jwebmp.core.base.angular.implementations;

import com.guicedee.client.IGuiceContext;
import com.guicedee.guicedservlets.websockets.services.IWebSocketMessageReceiver;
import com.jwebmp.core.base.ajax.AjaxCall;
import com.jwebmp.core.base.ajax.AjaxResponse;
import com.jwebmp.core.base.angular.client.annotations.angular.NgDataService;
import com.jwebmp.core.base.angular.client.services.AnnotationHelper;
import com.jwebmp.core.base.angular.client.services.interfaces.INgDataService;
import io.smallrye.mutiny.Uni;

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
    public Uni<AjaxResponse<?>> action(AjaxCall<?> call, AjaxResponse<?> response)
    {
        return Uni.createFrom().item(() -> {
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
                NgDataService dService = IGuiceContext.get(AnnotationHelper.class)
                                                      .getAnnotationFromClass(clazzy, NgDataService.class)
                                                      .get(0);
                response.addDataResponse(dService.value(), returned);
            }
            return response;
        });
    }
}

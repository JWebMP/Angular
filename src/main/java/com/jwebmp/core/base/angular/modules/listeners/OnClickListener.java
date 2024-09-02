package com.jwebmp.core.base.angular.modules.listeners;

import com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils;
import com.jwebmp.core.base.angular.modules.directives.OnClickListenerDirective;
import com.jwebmp.core.events.services.IOnClickService;
import com.jwebmp.core.htmlbuilder.javascript.events.interfaces.IEvent;

public class OnClickListener implements IOnClickService<OnClickListener>
{
    @Override
    public void onCreate(IEvent<?, ?> e)
    {
        if (e.getComponent() != null)
        {
            e.getComponent()
             .cast()
             .asAttributeBase()
             .addAttribute("clickClassName", e.getClass()
                                              .getCanonicalName());

            e.getComponent()
             .addConfiguration(AnnotationUtils.getNgComponentReference(OnClickListenerDirective.class));
        }
    }

    @Override
    public void onCall(IEvent<?, ?> e)
    {

    }
}

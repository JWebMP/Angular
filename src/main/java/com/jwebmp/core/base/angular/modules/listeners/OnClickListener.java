package com.jwebmp.core.base.angular.modules.listeners;

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
        }
    }

    @Override
    public void onCall(IEvent<?, ?> e)
    {

    }
}

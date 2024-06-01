package com.jwebmp.core.base.angular;

import com.guicedee.services.jsonrepresentation.IJsonRepresentation;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;
import jakarta.annotation.Nullable;

public class AngularWebSocket
{
    public static void bindToGroup(IComponentHierarchyBase<?, ?> component, String websocketName, @Nullable IJsonRepresentation<?> data)
    {
        component.asAttributeBase()
                 .addAttribute("websocketjw", "");
        component.asAttributeBase()
                 .addAttribute("websocketgroup", websocketName);
        component.asAttributeBase()
                 .setInvertColonRender(true);
        if (data != null)
        {
            component.asAttributeBase()
                     .addAttribute("websocketdata", data.toJson(true));
        }
    }

}

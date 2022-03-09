package com.jwebmp.core.base.angular;

import com.guicedee.guicedinjection.representations.*;
import com.jwebmp.core.base.*;
import com.jwebmp.core.base.interfaces.*;

public class AngularWebSocket
{
	public static void bindToGroup(IComponentHierarchyBase<?, ?> component, String websocketName, IJsonRepresentation<?> data)
	{
		component.asAttributeBase()
		         .addAttribute("websocketjw", "");
		component.asAttributeBase()
		         .addAttribute("websocketgroup", websocketName);
		if (data != null)
		{
			component.asAttributeBase()
			         .addAttribute("websocketdata", data.toJson(true));
		}
		
		@SuppressWarnings("rawtypes")
		ComponentHierarchyBase chb = (ComponentHierarchyBase) component;
		chb.setInvertColonRender(true);
	}
	
}

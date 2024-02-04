package com.jwebmp.core.base.angular;

import com.guicedee.services.jsonrepresentation.IJsonRepresentation;
import com.jwebmp.core.base.ComponentHierarchyBase;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;

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

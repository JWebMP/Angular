package com.jwebmp.core.base.angular.services.interfaces;

import com.jwebmp.core.base.*;
import com.jwebmp.core.base.angular.modules.services.angular.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.base.html.*;
import com.jwebmp.core.base.html.interfaces.*;
import com.jwebmp.core.databind.*;

public class OnComponentAdded implements IOnComponentAdded<OnComponentAdded>
{
	@Override
	public void onComponentAdded(ComponentHierarchyBase<GlobalChildren, ?, ?, ?, ?> parent, ComponentHierarchyBase<GlobalChildren, ?, ?, ?, ?> component)
	{
		if (component.getClass()
		             .isAnnotationPresent(NgComponent.class) && component instanceof INgComponent)
		{
			NgComponent annotation = component.getClass()
			                                  .getAnnotation(NgComponent.class);
			parent.add(new DivSimple<>().setTag(annotation.value())
			                            .setRenderIDAttribute(false));
			component.setRenderChildren(false);
			component.setRenderTag(false);
			parent.addConfiguration((IConfiguration) component);
		}
		
		if (component.getClass()
		             .isAssignableFrom(ITSComponent.class))
		{
			parent.addConfiguration((IConfiguration) component);
		}
		
		if (component.getClass()
		             .isAnnotationPresent(NgRoutable.class))
		{
			component.addConfiguration(new NgRouterModule());
		}
		
	}
}


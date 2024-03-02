package com.jwebmp.core.base.angular.services.interfaces;

import com.jwebmp.core.base.*;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;
import com.jwebmp.core.databind.*;

public class OnComponentRenderApplyAngular implements IOnComponentHtmlRender<OnComponentRenderApplyAngular>
{
    @Override
    public boolean onHtmlRender(IComponentHierarchyBase<?, ?> component)
    {
		/*if (!component.getConfigurations(IComponent.class)
		              .isEmpty())
		{
			System.out.println("Configurations found");
			for (IConfiguration configuration : component.getConfigurations(IComponent.class))
			{
				System.out.println(component.getTag() + " - " + configuration.getClass()
				                                                             .getCanonicalName());
			}
		}*/
        return true;
    }
}


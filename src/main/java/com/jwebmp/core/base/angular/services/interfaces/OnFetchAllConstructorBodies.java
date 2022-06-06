package com.jwebmp.core.base.angular.services.interfaces;

import com.jwebmp.core.base.*;
import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.annotations.constructors.*;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.client.services.spi.*;

import java.util.*;

public class OnFetchAllConstructorBodies implements OnGetAllConstructorBodies
{
	@Override
	public void perform(List<NgConstructorBody> allBodies, Object clazz)
	{
		if (clazz.getClass().isAnnotationPresent(NgComponent.class) && clazz instanceof ComponentHierarchyBase && clazz instanceof INgComponent)
		{
			ComponentHierarchyBase chb = (ComponentHierarchyBase) clazz;
			chb.toString(0);
			Set childrenHierarchy = chb.getChildrenHierarchy();
			for (Object o : childrenHierarchy)
			{
				ComponentHierarchyBase chb1 = (ComponentHierarchyBase) o;
				if (!chb1.getClass()
				         .isAnnotationPresent(NgComponent.class) && chb1 instanceof INgComponent)
				{
					INgComponent<?> ngComp = (INgComponent<?>) chb1;
					allBodies.addAll(ngComp.getAllConstructorBodies());
				}
			}
		}
	}
	
}

package com.jwebmp.core.base.angular.services.interfaces;

import com.jwebmp.core.base.angular.client.annotations.structures.NgField;
import com.jwebmp.core.base.angular.client.services.spi.OnGetAllFields;

import java.util.List;

public class OnFetchAllFields implements OnGetAllFields
{
    @Override
    public void perform(List<NgField> allBodies, Object clazz)
    {
		/*if (clazz.getClass().isAnnotationPresent(NgComponent.class) && clazz instanceof ComponentHierarchyBase && clazz instanceof INgComponent)
		{
			ComponentHierarchyBase chb = (ComponentHierarchyBase) clazz;
			for (Object o : chb.getChildrenHierarchy())
			{
				ComponentHierarchyBase chb1 = (ComponentHierarchyBase) o;
				if (!chb1.getClass()
				         .isAnnotationPresent(NgComponent.class) && chb1 instanceof INgComponent)
				{
					INgComponent<?> ngComp = (INgComponent<?>) chb1;
					allBodies.addAll(ngComp.getAllFields());
				}
			}
		}*/
    }

}

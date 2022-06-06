package com.jwebmp.core.base.angular.services.interfaces;

import com.jwebmp.core.base.*;
import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.annotations.references.*;
import com.jwebmp.core.base.angular.client.annotations.structures.*;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.client.services.spi.*;

import java.util.*;

import static com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils.*;

public class OnFetchAllImports implements OnGetAllImports
{
	@Override
	public void perform(List<NgImportReference> refs, Object clazz)
	{
		if (clazz.getClass()
		         .isAnnotationPresent(NgComponent.class) && clazz instanceof ComponentHierarchyBase && clazz instanceof INgComponent)
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
					for (NgImportReference allImportAnnotation : ngComp.getAllImportAnnotations())
					{
						if (allImportAnnotation.value()
						                       .trim()
						                       .equals(getTsFilename(getClass())))
						{
							continue;
						}
						refs.add(allImportAnnotation);
					}
				}
			}
		}
	}
	
}

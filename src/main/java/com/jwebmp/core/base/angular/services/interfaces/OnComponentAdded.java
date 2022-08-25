package com.jwebmp.core.base.angular.services.interfaces;

import com.google.common.base.*;
import com.jwebmp.core.base.*;
import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.client.annotations.components.*;
import com.jwebmp.core.base.angular.client.services.*;
import com.jwebmp.core.base.angular.client.services.interfaces.*;
import com.jwebmp.core.base.angular.implementations.*;

import com.jwebmp.core.base.angular.services.compiler.*;
import com.jwebmp.core.base.html.*;
import com.jwebmp.core.base.html.interfaces.*;
import com.jwebmp.core.databind.*;

import java.lang.annotation.*;
import java.util.*;
import java.util.List;

public class OnComponentAdded implements IOnComponentAdded<OnComponentAdded>
{
	@Override
	public void onComponentAdded(ComponentHierarchyBase<GlobalChildren, ?, ?, ?, ?> parent, ComponentHierarchyBase<GlobalChildren, ?, ?, ?, ?> component)
	{
		if (AngularTSPostStartup.loadTSOnStartup)
		{
			if (component.getClass()
			             .isAnnotationPresent(NgComponent.class) && component instanceof INgComponent)
			{
				NgComponent annotation = component.getClass()
				                                  .getAnnotation(NgComponent.class);
				
				DivSimple<?> displayDiv = new DivSimple<>().setTag(annotation.value())
				                                           .setRenderIDAttribute(false);
				
				
				List<NgInput> inputs = AnnotationsMap.getAnnotations(component.getClass(), NgInput.class);
				Set<NgInput> uniqueValues = new HashSet<>();
				for (NgInput a : inputs)
				{
					if (a.renderAttributeReference())
					{
						if (uniqueValues.add(a))
						{
							displayDiv.getAttributes()
							          .put("[" + a.value() + "]", "" + (Strings.isNullOrEmpty(a.attributeReference()) ? a.value() : a.attributeReference()) + "");
						}
					}
				}
				if (component.readChildrenPropertyFirstResult("renderAttributes", false))
				{
					Set<String> removables = new HashSet<>();
					
					displayDiv.getAttributes()
					          .putAll(component.getAttributes());
					displayDiv.getAttributes()
					          .keySet()
					          .stream()
					          .filter(a -> a.startsWith("#"))
					          .forEach(removables::add);
					removables.forEach(a -> displayDiv.getAttributes()
					                                  .remove(a));
					displayDiv.getClasses()
					          .addAll(component.getClasses());
				}
				parent.add(displayDiv);
				
				component.setRenderChildren(false);
				component.setRenderTag(false);
				parent.addConfiguration((IConfiguration) component);
			}
		}
	}
}


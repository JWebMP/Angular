package com.jwebmp.core.base.angular.services.interfaces;

import com.google.common.base.Strings;
import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.annotations.components.NgComponentTagAttribute;
import com.jwebmp.core.base.angular.client.annotations.components.NgInput;
import com.jwebmp.core.base.angular.client.services.AnnotationsMap;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import com.jwebmp.core.base.angular.implementations.AngularTSPostStartup;
import com.jwebmp.core.base.html.DivSimple;
import com.jwebmp.core.base.html.interfaces.GlobalChildren;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;
import com.jwebmp.core.databind.IConfiguration;
import com.jwebmp.core.databind.IOnComponentAdded;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OnComponentAdded implements IOnComponentAdded<OnComponentAdded>
{
    @Override
    public void onComponentAdded(IComponentHierarchyBase<GlobalChildren, ?> parent, IComponentHierarchyBase<GlobalChildren, ?> component)
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

                List<NgComponentTagAttribute> tagAttributes = AnnotationsMap.getAnnotations(component.getClass(), NgComponentTagAttribute.class);
                Set<NgComponentTagAttribute> uniqueTagValues = new HashSet<>();
                for (NgComponentTagAttribute a : tagAttributes)
                {
                    if (uniqueTagValues.add(a))
                    {
                        displayDiv.getAttributes()
                                  .put(a.key(), a.value());
                    }
                }


                if (component.readChildrenPropertyFirstResult("renderAttributes", false))
                {
                    Set<String> removables = new HashSet<>();

                    displayDiv.getAttributes()
                              .putAll(component.cast()
                                               .asAttributeBase()
                                               .getAttributes());
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

                for (Object loop : component.cast()
                                            .asAttributeBase()
                                            .getOverrideAttributes()
                                            .entrySet())
                {
                    Map.Entry<String, String> entry = (Map.Entry<String, String>) loop;
                    String key = entry.getKey();
                    String value = entry.getValue();
                    displayDiv.getAttributes()
                              .put(key, value);
                }


                parent.add(displayDiv);

                component.setRenderChildren(false);
                component.cast()
                         .asTagBase()
                         .setRenderTag(false);

                parent.addConfiguration((IConfiguration) component);
            }
        }
    }
}


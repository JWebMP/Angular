package com.jwebmp.core.base.angular.services.interfaces;

import com.google.common.base.Strings;
import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.annotations.components.NgInput;
import com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils;
import com.jwebmp.core.base.angular.client.services.interfaces.IComponent;
import com.jwebmp.core.base.html.DivSimple;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;
import com.jwebmp.core.databind.IAfterRenderComplete;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.jwebmp.core.base.angular.services.interfaces.IOnNgComponentAdded.findOwningNgComponent;

public class AngularTagReplacementAfterRender implements IAfterRenderComplete<AngularTagReplacementAfterRender>
{
    @Override
    public void process(IComponentHierarchyBase<?, ?> componentHierarchyBase)
    {
        //for this component, after the component is configured, and the configurations are moved across the tree - perform the tag replacements
        List<?> children = componentHierarchyBase.getChildren();
        for (Object child : children)
        {
            if (child instanceof IComponentHierarchyBase<?, ?> component)
            {
                if (!updateTag(component))
                {
                    process(component);
                }
                else
                {
                    IComponentHierarchyBase<?, ?> ngComponent = findOwningNgComponent(componentHierarchyBase);
                    if (ngComponent != null)
                    {
                        ngComponent.addConfiguration(AnnotationUtils.getNgComponentReference((Class<? extends IComponent<?>>) child.getClass()));
                    }
                }
            }
        }
    }

    private boolean updateTag(IComponentHierarchyBase<?, ?> component)
    {
        if (!component.getConfigurations(NgComponent.class)
                      .isEmpty())
        {
            var anno = component.getConfigurations(NgComponent.class)
                                .stream()
                                .findFirst()
                                .orElseThrow();
            //replace
            component.asTagBase()
                     .setTag(anno.value());
            component.getChildren()
                     .clear();
            component.asAttributeBase()
                     .getAttributes()
                     .clear();
            component.getClasses()
                     .clear();

            component.asAttributeBase()
                     .getAttributes()
                     .putAll(component.asAttributeBase()
                                      .getOverrideAttributes());

            var inputs = component.getConfigurations(NgInput.class, false);
            Set<NgInput> uniqueValues = new HashSet<>();
            for (NgInput a : inputs)
            {
                if (a.renderAttributeReference())
                {
                    if (uniqueValues.add(a))
                    {
                        component.asAttributeBase()
                                 .getAttributes()
                                 .put("[" + a.value() + "]", "" + (Strings.isNullOrEmpty(a.attributeReference()) ? a.value() : a.attributeReference()) + "");
                    }
                }
            }

            return true;
        }
        return false;
    }

    private DivSimple<?> renderAngularTag(IComponentHierarchyBase<?, ?> component)
    {
        NgComponent annotation = component.getClass()
                                          .getAnnotation(NgComponent.class);
        DivSimple<?> displayDiv = new DivSimple<>().setTag(annotation.value())
                                                   .setRenderIDAttribute(false);
        displayDiv.setID(component.asBase()
                                  .getID());

        return displayDiv;
    }

    @Override
    public Integer sortOrder()
    {
        return IAfterRenderComplete.super.sortOrder() + 10;
    }
}

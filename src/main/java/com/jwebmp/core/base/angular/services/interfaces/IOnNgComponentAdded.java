package com.jwebmp.core.base.angular.services.interfaces;

import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;
import com.jwebmp.core.databind.IOnComponentAdded;

public interface IOnNgComponentAdded<J extends IOnNgComponentAdded<J>> extends IOnComponentAdded<J>
{
    /**
     * Find the first instance of an ng component with a tag directive for the component
     *
     * @param component the component
     * @return the component with @NgComponent and INgComponent
     */
    static IComponentHierarchyBase<?, ?> findOwningNgComponent(IComponentHierarchyBase<?, ?> component)
    {
        if (component == null)
        {
            return null;
        }
        if (component instanceof INgComponent<?> && component.getClass()
                                                             .isAnnotationPresent(NgComponent.class))
        {
            return component;
        }
        var parent = component.getParent();
        while (parent != null)
        {
            if (parent instanceof INgComponent<?> && parent.getClass()
                                                           .isAnnotationPresent(NgComponent.class))
            {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

}

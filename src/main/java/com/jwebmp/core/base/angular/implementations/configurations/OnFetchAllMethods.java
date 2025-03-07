package com.jwebmp.core.base.angular.implementations.configurations;

import com.jwebmp.core.base.ComponentHierarchyBase;
import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.annotations.structures.NgMethod;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import com.jwebmp.core.base.angular.client.services.spi.OnGetAllMethods;

import java.util.List;

public class OnFetchAllMethods implements OnGetAllMethods
{
    @Override
    public void perform(List<NgMethod> allBodies, Object clazz)
    {
        if (clazz.getClass()
                .isAnnotationPresent(NgComponent.class) &&
                clazz instanceof ComponentHierarchyBase &&
                clazz instanceof INgComponent)
        {
            ComponentHierarchyBase chb = (ComponentHierarchyBase) clazz;
            for (Object o : chb.getChildrenHierarchy())
            {
                ComponentHierarchyBase chb1 = (ComponentHierarchyBase) o;
                if (!chb1.getClass()
                        .isAnnotationPresent(NgComponent.class) && chb1 instanceof INgComponent)
                {
                    INgComponent<?> ngComp = (INgComponent<?>) chb1;
                    allBodies.addAll(ngComp.renderAllMethods());
                }
            }
        }
    }

}

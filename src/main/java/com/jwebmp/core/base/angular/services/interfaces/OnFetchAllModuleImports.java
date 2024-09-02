package com.jwebmp.core.base.angular.services.interfaces;

import com.jwebmp.core.base.angular.client.services.spi.OnGetAllModuleImports;

import java.util.Set;

public class OnFetchAllModuleImports implements OnGetAllModuleImports
{
    @Override
    public void perform(Set<String> refs, Object clazz)
    {
        /*if (clazz.getClass()
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
                    Set<String> strings = ((INgComponent<?>) chb1).moduleImports();
                    refs.addAll(strings);
                }
            }
        }*/
    }

}

package com.jwebmp.core.base.angular.implementations.configurations;

import com.jwebmp.core.base.ComponentHierarchyBase;
import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.annotations.structures.NgField;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import com.jwebmp.core.base.angular.client.services.spi.OnGetAllImports;

import java.util.List;

public class OnFetchAllImports implements OnGetAllImports
{
    @Override
    public void perform(List<NgImportReference> allImportReferences, Object instance)
    {
        if (instance.getClass().isAnnotationPresent(NgImportReference.class) &&
                instance instanceof INgComponent<?> ngComponent &&
                instance instanceof ComponentHierarchyBase componentHierarchyBase)
        {
            var configs = componentHierarchyBase.getConfigurations(NgImportReference.class, false);
            allImportReferences.addAll(configs);
        }
    }

}

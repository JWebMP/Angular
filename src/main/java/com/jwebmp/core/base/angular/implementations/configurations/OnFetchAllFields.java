package com.jwebmp.core.base.angular.implementations.configurations;

import com.jwebmp.core.base.ComponentHierarchyBase;
import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.annotations.structures.NgField;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import com.jwebmp.core.base.angular.client.services.spi.OnGetAllFields;

import java.util.List;

public class OnFetchAllFields implements OnGetAllFields
{
    @Override
    public void perform(List<NgField> allFields, Object instance)
    {
        if (instance instanceof NgComponent annotation &&
                instance instanceof INgComponent<?> ngComponent &&
                instance instanceof ComponentHierarchyBase componentHierarchyBase)
        {
            var configs = componentHierarchyBase.getConfigurations(NgField.class, true);
            allFields.addAll(configs);
        }
    }

}

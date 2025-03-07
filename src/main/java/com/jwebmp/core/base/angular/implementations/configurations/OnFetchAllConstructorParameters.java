package com.jwebmp.core.base.angular.implementations.configurations;

import com.jwebmp.core.base.ComponentHierarchyBase;
import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.annotations.constructors.NgConstructorParameter;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import com.jwebmp.core.base.angular.client.services.spi.OnGetAllConstructorParameters;

import java.util.List;

public class OnFetchAllConstructorParameters implements OnGetAllConstructorParameters
{
    @Override
    public void perform(List<NgConstructorParameter> constructorParameters, Object instance)
    {
        if (instance.getClass().getCanonicalName().contains("TasksCreateModal"))
        {
            System.out.println("here");
        }
        if (instance.getClass().isAnnotationPresent(NgComponent.class) &&
                instance instanceof INgComponent<?> ngComponent &&
                instance instanceof ComponentHierarchyBase componentHierarchyBase)
        {
            var configs = componentHierarchyBase.getConfigurations(NgConstructorParameter.class, false);
            constructorParameters.addAll(configs);
        }
    }

}

package com.jwebmp.core.base.angular.services.interfaces;

import com.jwebmp.core.base.angular.client.annotations.angular.NgComponent;
import com.jwebmp.core.base.angular.client.annotations.angular.NgDirective;
import com.jwebmp.core.base.angular.client.annotations.angular.NgServiceProvider;
import com.jwebmp.core.base.angular.client.annotations.references.NgComponentReference;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.annotations.structures.NgField;
import com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;
import com.jwebmp.core.databind.IAfterRenderComplete;

import java.util.List;
import java.util.Set;

import static com.jwebmp.core.base.angular.services.interfaces.IOnNgComponentAdded.findOwningNgComponent;

public class AngularComponentReferencesAfterRender implements IAfterRenderComplete<AngularComponentReferencesAfterRender>
{
    @Override
    public void process(IComponentHierarchyBase<?, ?> componentHierarchyBase)
    {
        List<?> children = componentHierarchyBase.getChildren();
        for (Object child : children)
        {
            if (child instanceof IComponentHierarchyBase<?, ?> component)
            {
                process(component);
            }
        }

        Set<NgComponentReference> configs = componentHierarchyBase.getConfigurations(NgComponentReference.class);
        for (var config : configs)
        {
            //get the class and determine the type
            var configClass = config.value();
            IComponentHierarchyBase<?, ?> ngComponent = findOwningNgComponent(componentHierarchyBase);
            if (ngComponent != null)
            {
                if (configClass.isAnnotationPresent(NgComponent.class))
                {
                    //component
                    ngComponent.addConfiguration(AnnotationUtils.getNgImportModule(AnnotationUtils.getTsFilename(configClass)));
                } else if (configClass.isAnnotationPresent(NgDirective.class))
                {
                    //directive
                    ngComponent.addConfiguration(AnnotationUtils.getNgImportModule(AnnotationUtils.getTsFilename(configClass)));
                } else if (configClass.isAnnotationPresent(NgServiceProvider.class))
                {
                    //service provider
                    NgServiceProvider anno = configClass.getAnnotation(NgServiceProvider.class);
                    if (!anno.singleton())
                    {
                        ngComponent.addConfiguration(AnnotationUtils.getNgImportProvider(AnnotationUtils.getTsFilename(configClass)));
                    }
                    ngComponent.addConfiguration(AnnotationUtils.getNgImportReference("inject", "@angular/core"));
                    ngComponent.addConfiguration(AnnotationUtils.getNgField("private readonly " + anno.referenceName() + " = inject(" + AnnotationUtils.getTsFilename(configClass) + ");"));
                }
            }
        }
    }

    @Override
    public Integer sortOrder()
    {
        return IAfterRenderComplete.super.sortOrder() + 20;
    }
}

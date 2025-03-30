package com.jwebmp.core.base.angular.components.modules;

import com.google.common.base.Strings;
import com.jwebmp.core.base.angular.client.annotations.references.NgIgnoreRender;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportModule;
import com.jwebmp.core.base.angular.client.annotations.references.NgImportReference;
import com.jwebmp.core.base.angular.client.annotations.routing.NgRoutable;
import com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils;
import com.jwebmp.core.base.angular.client.services.interfaces.INgComponent;
import com.jwebmp.core.base.angular.client.services.interfaces.INgModule;
import com.jwebmp.core.base.interfaces.IComponentHTMLAttributeBase;
import com.jwebmp.core.base.interfaces.IComponentHierarchyBase;

@NgImportReference(value = "RouterModule", reference = "@angular/router", onParent = true)
@NgImportModule(value = "RouterModule", onParent = true)
@NgIgnoreRender
public class RouterModule implements INgModule<RouterModule>
{
    public static void applyRoute(IComponentHTMLAttributeBase<?, ?> component,
                                  String pathRoute,
                                  String variablePath)
    {
        if (pathRoute.startsWith("'"))
        {
            pathRoute = pathRoute.substring(1);
        }
        if (pathRoute.endsWith("'"))
        {
            pathRoute = pathRoute.substring(0, pathRoute.length() - 1);
        }


        component.addAttribute("[routerLink]", "['" + pathRoute + "'" + (Strings.isNullOrEmpty(variablePath) ? "" : "," +
                variablePath) + "]");

        component.asHierarchyBase()
                .addConfiguration(AnnotationUtils.getNgImportReference("RouterModule", "@angular/router"));
        component.asHierarchyBase()
                .addConfiguration(AnnotationUtils.getNgImportModule("RouterModule"));
    }

    public static void applyRequired(IComponentHierarchyBase<?, ?> component)
    {

        component.asHierarchyBase()
                .addConfiguration(AnnotationUtils.getNgImportReference("RouterModule", "@angular/router"));
        component.asHierarchyBase()
                .addConfiguration(AnnotationUtils.getNgImportModule("RouterModule"));
    }

    public static void applyRoute(IComponentHTMLAttributeBase<?, ?> component,
                                  Class<? extends INgComponent<?>> pathRoute)
    {
        applyRoute(component, pathRoute, "");

        component.asHierarchyBase()
                .addConfiguration(AnnotationUtils.getNgImportReference("RouterModule", "@angular/router"));
        component.asHierarchyBase()
                .addConfiguration(AnnotationUtils.getNgImportModule("RouterModule"));
    }

    public static void applyRoute(IComponentHTMLAttributeBase<?, ?> component,
                                  Class<? extends INgComponent<?>> pathRoute,
                                  String variablePath)
    {
        component.addAttribute("[routerLink]", "['" + pathRoute.getAnnotation(NgRoutable.class)
                .path() + "'" + (Strings.isNullOrEmpty(variablePath) ? "" : "," +
                variablePath) + "]");

        component.asHierarchyBase()
                .addConfiguration(AnnotationUtils.getNgImportReference("RouterModule", "@angular/router"));
        component.asHierarchyBase()
                .addConfiguration(AnnotationUtils.getNgImportModule("RouterModule"));
    }
}

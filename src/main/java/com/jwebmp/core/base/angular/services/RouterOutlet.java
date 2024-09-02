package com.jwebmp.core.base.angular.services;

import com.jwebmp.core.base.angular.client.services.interfaces.AnnotationUtils;
import com.jwebmp.core.base.html.DivSimple;

public class RouterOutlet extends DivSimple<RouterOutlet>
{
    public RouterOutlet()
    {
        setRenderIDAttribute(false);
        setTag("router-outlet");
        addConfiguration(AnnotationUtils.getNgImportReference("RouterOutlet", "@angular/router"));
        addConfiguration(AnnotationUtils.getNgImportModule("RouterOutlet"));
    }
}

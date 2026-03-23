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

    /**
     * Creates a named router-outlet. Produces {@code <router-outlet name="...">}.
     *
     * @param name the outlet name that routes target via {@code outlet: 'name'} in their route config
     */
    public RouterOutlet(String name)
    {
        this();
        if (name != null && !name.isBlank())
        {
            addAttribute("name", name);
        }
    }
}

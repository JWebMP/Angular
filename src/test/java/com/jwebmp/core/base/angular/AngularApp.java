package com.jwebmp.core.base.angular;

import com.jwebmp.core.base.angular.client.annotations.angular.NgApp;
import com.jwebmp.core.base.angular.services.NGApplication;

import java.util.List;

@NgApp(value = "main", bootComponent = AngularTestComponent.class)
public class AngularApp extends NGApplication<AngularApp>
{
    public AngularApp()
    {
        getOptions().setTitle("JWebMPAngularTest");
    }


    @Override
    public List<String> stylesheets()
    {
        return List.of("testasset.css");
    }
}

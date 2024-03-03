package com.jwebmp.core.base.angular;

import com.jwebmp.core.base.angular.client.annotations.angular.*;
import com.jwebmp.core.base.angular.services.*;


import java.util.*;

@NgApp(value = "main", bootComponent = AngularTestComponent.class)
public class AngularApp extends NGApplication<AngularApp>
{
    public AngularApp()
    {
        getOptions().setTitle("JWebMPAngularTest");
    }

    @Override
    public List<String> assets()
    {
        return List.of("testasset.css");
    }

    @Override
    public List<String> stylesheets()
    {
        return List.of("testasset.css");
    }
}

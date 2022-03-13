package com.jwebmp.core.base.angular;

import com.jwebmp.core.base.angular.services.*;
import com.jwebmp.core.base.angular.services.annotations.*;

@NgApp(value = "main", bootComponent = AngularTestComponent.class,
assets = {"testasset.css"},
stylesheets = {"testasset.css"})
public class AngularApp extends NGApplication<AngularApp>
{
	public AngularApp()
	{
		getOptions().setTitle("JWebMPAngularTest");
	}
	
}

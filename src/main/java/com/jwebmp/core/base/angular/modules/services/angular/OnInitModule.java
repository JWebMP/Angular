package com.jwebmp.core.base.angular.modules.services.angular;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class OnInitModule implements INgModule<OnInitModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("OnInit", "@angular/core");
	}
	
	public List<String> interfaces()
	{
		return List.of("OnInit");
	}
}

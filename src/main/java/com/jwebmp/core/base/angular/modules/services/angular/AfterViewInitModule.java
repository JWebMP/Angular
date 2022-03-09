package com.jwebmp.core.base.angular.modules.services.angular;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class AfterViewInitModule implements INgModule<AfterViewInitModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("AfterViewInit", "@angular/core");
	}
	
}

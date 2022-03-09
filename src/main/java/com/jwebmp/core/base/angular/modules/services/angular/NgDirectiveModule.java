package com.jwebmp.core.base.angular.modules.services.angular;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class NgDirectiveModule implements INgModule<NgDirectiveModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("Directive ", "@angular/core");
	}
	
}

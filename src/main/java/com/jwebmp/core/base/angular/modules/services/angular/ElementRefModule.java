package com.jwebmp.core.base.angular.modules.services.angular;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class ElementRefModule implements INgModule<ElementRefModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("ElementRef", "@angular/core");
	}
	
}

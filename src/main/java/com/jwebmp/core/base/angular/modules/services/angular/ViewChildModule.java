package com.jwebmp.core.base.angular.modules.services.angular;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class ViewChildModule implements INgModule<ViewChildModule>
{
	public Map<String, String> imports()
	{
		return Map.of("ViewChild", "@angular/core");
	}
}

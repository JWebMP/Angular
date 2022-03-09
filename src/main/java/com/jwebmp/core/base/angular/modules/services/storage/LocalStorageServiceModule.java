package com.jwebmp.core.base.angular.modules.services.storage;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class LocalStorageServiceModule implements INgModule<LocalStorageServiceModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("LocalStorageService", "!ngx-webstorage");
	}
	
}

package com.jwebmp.core.base.angular.modules.services.storage;

import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class SessionStorageServiceModule implements INgModule<SessionStorageServiceModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("SessionStorageService", "!ngx-webstorage");
	}
	
}

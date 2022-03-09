package com.jwebmp.core.base.angular.modules.services.angular;

import com.jwebmp.core.base.angular.services.annotations.NgModule;
import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

public class HttpClientModule implements INgModule<HttpClientModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("HttpClient, HttpResponse, HttpHeaders,HttpParams,HttpErrorResponse",
				"@angular/common/http");
	}
	
}

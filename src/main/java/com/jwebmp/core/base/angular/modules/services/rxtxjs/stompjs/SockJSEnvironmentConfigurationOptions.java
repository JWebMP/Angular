package com.jwebmp.core.base.angular.modules.services.rxtxjs.stompjs;

import com.fasterxml.jackson.annotation.*;
import com.guicedee.guicedinjection.representations.*;
import com.jwebmp.core.base.angular.modules.services.base.*;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

@JsonAutoDetect(fieldVisibility = ANY,
                getterVisibility = NONE,
                setterVisibility = NONE)
@JsonInclude(NON_NULL)
public class SockJSEnvironmentConfigurationOptions<J extends SockJSEnvironmentConfigurationOptions<J>>
		extends EnvironmentOptions<J>
		implements IJsonRepresentation<J>
{
	private String api;
	
	public String getApi()
	{
		return api;
	}
	
	public J setApi(String api)
	{
		this.api = api;
		return (J) this;
	}
}

package com.jwebmp.core.base.angular.modules.services.base;

import com.fasterxml.jackson.annotation.*;
import com.guicedee.guicedinjection.representations.*;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

@JsonAutoDetect(fieldVisibility = ANY,
                getterVisibility = NONE,
                setterVisibility = NONE)
@JsonInclude(NON_NULL)
public class EnvironmentOptions<J extends EnvironmentOptions<J>> implements IJsonRepresentation<J>
{
	private boolean production;
	private String appClass;
	
	public boolean isProduction()
	{
		return production;
	}
	
	public J setProduction(boolean production)
	{
		this.production = production;
		return (J) this;
	}
	
	public String getAppClass()
	{
		return appClass;
	}
	
	public EnvironmentOptions<J> setAppClass(String appClass)
	{
		this.appClass = appClass;
		return this;
	}
}

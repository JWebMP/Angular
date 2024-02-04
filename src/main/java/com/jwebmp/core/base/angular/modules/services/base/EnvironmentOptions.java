package com.jwebmp.core.base.angular.modules.services.base;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.guicedee.services.jsonrepresentation.IJsonRepresentation;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

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

package com.jwebmp.core.base.angular.typescript.JWebMP;

import com.fasterxml.jackson.annotation.*;
import com.jwebmp.core.base.angular.services.interfaces.*;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.*;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

@JsonAutoDetect(fieldVisibility = ANY,
                getterVisibility = NONE,
                setterVisibility = NONE)
@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefinedRoute
{
	private String path;
	@JsonRawValue
	@JsonProperty("component")
	private String componentName;
	@JsonIgnore
	private Class<? extends INgComponent<?>> component;
	
	public String getPath()
	{
		return path;
	}
	
	public DefinedRoute setPath(String path)
	{
		this.path = path;
		return this;
	}
	
	public String getComponentName()
	{
		return componentName;
	}
	
	public DefinedRoute setComponentName(String componentName)
	{
		this.componentName = componentName;
		return this;
	}
	
	public Class<? extends INgComponent<?>> getComponent()
	{
		return component;
	}
	
	public DefinedRoute setComponent(Class<? extends INgComponent<?>> component)
	{
		this.component = component;
		return this;
	}
}

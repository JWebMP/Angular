package com.jwebmp.core.base.angular.services;

import com.jwebmp.core.*;
import com.jwebmp.core.base.angular.modules.services.angular.*;
import com.jwebmp.core.base.angular.modules.services.base.*;
import com.jwebmp.core.base.angular.services.interfaces.*;
import com.jwebmp.core.base.html.*;
import com.jwebmp.core.databind.*;

import java.util.*;
import java.util.List;
import java.util.Map;

public class NGApplication<J extends NGApplication<J>> extends Page<J> implements ITSComponent<J>, INgApp<J>
{
	private List<String> renderAfterImports;
	
	public NGApplication()
	{
		addConfiguration(new EnableProdModeFunction());
		addConfiguration(new DynamicPlatformBrowserModule());
		addConfiguration(new EnvironmentModule());
		getHead()
				.add(new Meta(Meta.MetadataFields.Charset, "utf-8"));
		getHead()
				.add(new Meta(Meta.MetadataFields.ViewPort, "width=device-width, initial-scale=1"));
		getOptions().setBase(new Base<>("/"));
	}
	
	public List<String> getRenderAfterImports()
	{
		if (renderAfterImports == null)
		{
			renderAfterImports = new ArrayList<>();
		}
		return renderAfterImports;
	}
	
	public NGApplication setRenderAfterImports(List<String> renderAfterImports)
	{
		this.renderAfterImports = renderAfterImports;
		return this;
	}
	
	@Override
	public Map<String, String> imports()
	{
		Map<String, String> map = new java.util.HashMap<>(Map.of(getClass().getSimpleName(), "./" + getClass().getPackageName()
		                                                                                                      .replaceAll("\\.", "\\/")
		                                                                                     + "/" + getClass().getSimpleName()));
		
		for (IConfiguration configuration : getConfigurations(INgModule.class))
		{
			INgModule<?> module = (INgModule<?>) configuration;
			map.putAll(module.imports());
		}
		return map;
	}
}

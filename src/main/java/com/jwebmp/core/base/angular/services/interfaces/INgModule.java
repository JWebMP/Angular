package com.jwebmp.core.base.angular.services.interfaces;

import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.databind.*;

import java.util.*;

@NgSourceDirectoryReference()
public interface INgModule<J extends INgModule<J>>
		extends ITSComponent<J>, IConfiguration
{
	default Set<String> renderBeforeNgModuleDecorator()
	{
		return Set.of();
	}
	
	default List<String> declarations()
	{
		return new ArrayList<>();
	}
	
	default Set<String> providers()
	{
		return Set.of();
	}
	
	default List<String> bootstrap()
	{
		return new ArrayList<>();
	}
	
	default Set<String> assets()
	{
		return new HashSet<>();
	}
	
	default Set<String> exports()
	{
		return new HashSet<>();
	}
	
	default J setApp(INgApp<?> app)
	{
		return (J)this;
	}
	
	default Set<String> moduleImports()
	{
		return new HashSet<>();
	}
	
	default Set<String> schemas()
	{
		return new HashSet<>();
	}
}

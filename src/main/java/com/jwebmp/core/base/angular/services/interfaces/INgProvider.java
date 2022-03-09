package com.jwebmp.core.base.angular.services.interfaces;

import com.jwebmp.core.base.angular.services.annotations.*;

import java.util.*;

@NgProvider()
@NgSourceDirectoryReference()
public interface INgProvider<J extends INgProvider<J>> extends ITSComponent<J>
{
	default List<String> declarations()
	{
		return new ArrayList<>();
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
	
	default Set<String> schemas()
	{
		return new HashSet<>();
	}
	
	default Set<String> providers()
	{
		return Set.of();
	}
	
	
}

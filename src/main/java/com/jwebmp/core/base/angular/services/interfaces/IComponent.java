package com.jwebmp.core.base.angular.services.interfaces;

import com.guicedee.guicedinjection.interfaces.*;
import com.jwebmp.core.base.angular.services.annotations.*;

import java.util.*;

import static com.jwebmp.core.base.angular.services.annotations.NgSourceDirectoryReference.SourceDirectories.*;

@NgSourceDirectoryReference(App)
interface IComponent<J extends IComponent<J>> extends IDefaultService<J>
{
	default Set<String> constructorParameters()
	{
		return Set.of();
	}
	
	default Set<Class<? extends NgDataType>> types()
	{
		return Set.of();
	}
	
	default List<String> constructorBody()
	{
		return List.of();
	}
	
	default List<String> methods()
	{
		return List.of();
	}
	
	default Set<String> globalFields()
	{
		return Set.of();
	}
	
	default Set<String> fields()
	{
		return Set.of();
	}
	
	default String ofType()
	{
		return "";
	}
	
	default Set<String> interfaces()
	{
		return Set.of();
	}
	
	
	default Set<String> decorators()
	{
		return Set.of();
	}
}

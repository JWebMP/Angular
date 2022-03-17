package com.jwebmp.core.base.angular.services.interfaces;

import com.guicedee.guicedinjection.interfaces.*;
import com.jwebmp.core.base.ComponentHierarchyBase;
import com.jwebmp.core.base.angular.services.annotations.*;

import java.lang.reflect.Array;
import java.util.*;

import static com.jwebmp.core.base.angular.services.annotations.NgSourceDirectoryReference.SourceDirectories.*;

@NgSourceDirectoryReference(App)
interface IComponent<J extends IComponent<J>> extends IDefaultService<J>
{
	default List<String> constructorParameters()
	{
		return List.of();
	}

	default List<Class<? extends NgDataType>> types()
	{
		return List.of();
	}
	
	default List<String> constructorBody()
	{
		return List.of();
	}
	
	default List<String> methods()
	{
		return List.of();
	}
	
	default List<String> globalFields()
	{
		return List.of();
	}
	
	default List<String> fields()
	{
		return List.of();
	}
	
	default String ofType()
	{
		return "";
	}
	
	default List<String> interfaces()
	{
		return List.of();
	}
	
	default List<String> decorators()
	{
		return List.of();
	}
}

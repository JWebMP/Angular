package com.jwebmp.core.base.angular.services.interfaces;

import com.guicedee.guicedinjection.interfaces.*;
import com.jwebmp.core.base.ComponentHierarchyBase;
import com.jwebmp.core.base.angular.services.annotations.*;

import java.lang.reflect.Array;
import java.util.*;

import static com.jwebmp.core.base.angular.services.annotations.NgSourceDirectoryReference.SourceDirectories.*;

interface IComponent<J extends IComponent<J>> extends IDefaultService<J>
{
	default List<String> componentConstructorParameters()
	{
		return new ArrayList<>();
	}
	default List<String> constructorParameters()
	{
		return new ArrayList<>();
	}

	default List<Class<? extends NgDataType>> types()
	{
		return new ArrayList<>();
	}
	
	default List<String> componentConstructorBody()
	{
		return new ArrayList<>();
	}
	default List<String> constructorBody()
	{
		return new ArrayList<>();
	}
	
	default List<String> componentMethods()
	{
		return new ArrayList<>();
	}
	default List<String> methods()
	{
		return new ArrayList<>();
	}
	
	default List<String> globalFields()
	{
		return new ArrayList<>();
	}
	
	default List<String> componentFields()
	{
		return new ArrayList<>();
	}
	default List<String> fields()
	{
		return new ArrayList<>();
	}
	
	default String ofType()
	{
		return "";
	}
	
	default List<String> componentInterfaces()
	{
		return new ArrayList<>();
	}
	default List<String> interfaces()
	{
		return new ArrayList<>();
	}
	
	default List<String> componentDecorators()
	{
		return new ArrayList<>();
	}
	default List<String> decorators()
	{
		return new ArrayList<>();
	}
}

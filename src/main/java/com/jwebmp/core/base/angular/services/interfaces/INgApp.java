package com.jwebmp.core.base.angular.services.interfaces;

import com.jwebmp.core.base.angular.services.annotations.*;

import java.util.*;

import static com.jwebmp.core.base.angular.services.annotations.NgSourceDirectoryReference.SourceDirectories.*;

@NgSourceDirectoryReference(Main)
public interface INgApp<J extends INgApp<J>> extends ITSComponent<J>
{
	default NgApp getAnnotation()
	{
		return getClass().getAnnotation(NgApp.class);
	}
	
	default String name()
	{
		return getAnnotation().name();
	}
	
	/**
	 * The name of the .ts file for this app
	 *
	 * @return
	 */
	default List<String> assets()
	{
		return List.of();
	}
	
	
	/**
	 * The name of the .ts file for this app
	 *
	 * @return
	 */
	default List<String> stylesheets()
	{
		return List.of();
	}
	
	/**
	 * The name of the .ts file for this app
	 *
	 * @return
	 */
	default List<String> scripts()
	{
		return List.of();
	}
	
	/**
	 * Include the packages to render
	 *
	 * @return
	 */
	default List<String> includePackages()
	{
		return List.of(getClass().getPackageName());
	}
	
	;
	
}

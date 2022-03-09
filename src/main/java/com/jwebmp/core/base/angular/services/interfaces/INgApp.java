package com.jwebmp.core.base.angular.services.interfaces;

import com.jwebmp.core.base.angular.services.annotations.*;

import java.util.*;

import static com.jwebmp.core.base.angular.services.annotations.NgSourceDirectoryReference.SourceDirectories.*;

@NgSourceDirectoryReference(Main)
public interface INgApp<J extends INgApp<J>> extends  ITSComponent<J>
{
	default NgApp getAnnotation()
	{
		return getClass().getAnnotation(NgApp.class);
	}
	
	/**
	 * Import name, Import location
	 * <p>
	 * npm modules should now be specified inside of components properly with this implementation
	 *
	 * @return
	 */
	default Map<String, String> imports()
	{
		return new HashMap<>();
	}
}

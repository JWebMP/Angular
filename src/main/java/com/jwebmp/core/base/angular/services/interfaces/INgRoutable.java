package com.jwebmp.core.base.angular.services.interfaces;

import com.jwebmp.core.base.angular.services.annotations.*;

import java.util.*;

import static com.jwebmp.core.base.angular.services.annotations.NgSourceDirectoryReference.SourceDirectories.*;

public interface INgRoutable<J extends Enum<J> & INgRoutable<J>> extends ITSComponent<J>
{
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

package com.jwebmp.core.base.angular.services.interfaces;

import com.jwebmp.core.base.angular.modules.services.angular.*;
import com.jwebmp.core.base.angular.services.annotations.*;
import com.jwebmp.core.databind.*;

import java.util.*;

@NgSourceDirectoryReference()
@NgModuleReference(NgComponentModule.class)
public interface INgComponent<J extends INgComponent<J>>
		extends ITSComponent<J>, IConfiguration
{
	default List<String> styleUrls()
	{
		return List.of();
	}

	default List<String> styles()
	{
		return List.of();
	}

	default List<String> animations()
	{
		return List.of();
	}
	
	default List<String> providers()
	{
		return List.of();
	}

	default List<String> inputs()
	{
		return List.of();
	}

	default List<String> outputs()
	{
		return List.of();
	}

	default List<String> host()
	{
		return List.of();
	}
}

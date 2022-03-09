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
	default Set<String> styleUrls()
	{
		return new HashSet<>();
	}
	
	default Set<String> providers()
	{
		return Set.of();
	}
	
}

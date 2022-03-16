package com.jwebmp.core.base.angular.modules.services.angular.forms;

import com.jwebmp.core.base.angular.services.annotations.angularconfig.*;
import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

@NgBootModuleImportReference(name = "CommonModule",reference = "@angular/common")
public class CommonsModule implements INgModule<CommonsModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("CommonModule", "@angular/common");
	}
	
}

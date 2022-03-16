package com.jwebmp.core.base.angular.modules.services.angular.forms;

import com.jwebmp.core.base.angular.services.annotations.angularconfig.*;
import com.jwebmp.core.base.angular.services.interfaces.*;

import java.util.*;

@NgBootModuleImportReference(name = "FormsModule", reference = "@angular/forms")
public class FormsModule implements INgModule<FormsModule>
{
	@Override
	public Map<String, String> imports()
	{
		return Map.of("FormsModule", "@angular/forms");
	}
	
}

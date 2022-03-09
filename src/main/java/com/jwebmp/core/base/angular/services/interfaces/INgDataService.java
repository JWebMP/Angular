package com.jwebmp.core.base.angular.services.interfaces;

import com.jwebmp.core.base.ajax.*;
import com.jwebmp.core.base.angular.modules.services.angular.*;
import com.jwebmp.core.base.angular.services.annotations.*;

import java.util.*;

@NgModuleReference(InjectableModule.class)
public interface INgDataService<J extends INgDataService<J>> extends INgComponent<J>
{
	INgDataType<?> getData(AjaxCall<?> call);
	
	Class<? extends INgDataType<?>> dataTypeReturned();
	
	String signalFetchName();
	
	@Override
	default Set<String> decorators()
	{
		return Set.of("@Injectable({\n" +
		              "  providedIn: 'root'\n" +
		              "})");
	}
}
